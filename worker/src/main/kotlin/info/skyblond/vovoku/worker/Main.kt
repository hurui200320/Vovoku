package info.skyblond.vovoku.worker

import info.skyblond.vovoku.commons.*
import info.skyblond.vovoku.commons.dl4j.DataFetcherParameter
import info.skyblond.vovoku.commons.dl4j.DataSetIteratorParameter
import info.skyblond.vovoku.commons.dl4j.ModelPrototype
import info.skyblond.vovoku.commons.dl4j.MultiLayerNetworkParameter
import info.skyblond.vovoku.commons.models.TrainingTaskDistro
import info.skyblond.vovoku.commons.models.TrainingTaskReport
import info.skyblond.vovoku.commons.redis.JedisLock
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.evaluation.classification.Evaluation
import org.nd4j.evaluation.classification.ROCMultiClass
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisPubSub
import java.io.File
import java.time.Duration
import kotlin.math.max
import kotlin.math.min

fun main() {
    val logger = LoggerFactory.getLogger("Worker")
    val config = JacksonYamlUtil.readOrInitConfigFile(File("./config.yaml"), Config())

    // init
    val jedisPoolConfig = JedisPoolConfig()
    jedisPoolConfig.maxTotal = 100
    jedisPoolConfig.maxIdle = 100
    val jedisPool = JedisPool(jedisPoolConfig, config.redisHost, config.redisPort)
    val lockDuration = Duration.ofMinutes(5)

    var currentTask: TrainingTaskDistro? = null
    var jedisLock: JedisLock? = null
    var isBusy = false

    val jedisSub = object : JedisPubSub() {
        override fun onMessage(channel: String?, message: String?) {
            synchronized(isBusy) {
                if (isBusy) return
                // if not busy, try lock this task
                val task = JacksonJsonUtil.jsonToObject<TrainingTaskDistro>(message!!)
                logger.info("Get task id: ${task.taskId}")
                jedisLock = JedisLock(jedisPool.resource, "$RedisTaskLockKeyPrefix${task.taskId}", lockDuration)
                if (!jedisLock!!.acquire()) {
                    logger.info("Cannot lock ${jedisLock!!.lockKey}")
                    return
                }
                // locked, then start refresh thread
                Thread {
                    val localLogger = LoggerFactory.getLogger("LockUpdater")
                    val oldKey = jedisLock!!.lockKey
                    localLogger.info("Start using lock key: $oldKey")
                    while (isBusy && jedisLock?.lockKey == oldKey) {
                        synchronized(isBusy) {
                            isBusy = false
                            require(jedisLock!!.renew()) { "Failed renew lock: ${jedisLock!!.lockKey}" }
                            isBusy = true
                        }
                        localLogger.info("Renewed lock: ${jedisLock!!.lockKey}")
                        Thread.sleep(jedisLock!!.lockExpiryDuration.toMillis() / 2)
                    }
                    localLogger.info("Old key '$oldKey' expired")
                }.also {
                    it.isDaemon = true
                    it.start()
                }

                // mark busy, enable training loop
                currentTask = task
                isBusy = true
            }
        }
    }

    Thread {
        jedisPool.resource.use {
            it.subscribe(jedisSub, RedisTaskDistributionChannel)
        }
    }.also {
        it.isDaemon = true
        it.start()
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        jedisSub.unsubscribe()
        jedisLock?.release()
        jedisPool.close()
    })

    // wait 1s for all thread prepared
    Thread.sleep(1000)

    while (true) {
        while (!isBusy) {
            // sleep 5s for next query
            Thread.sleep(5 * 1000)
        }
        val startTime = System.currentTimeMillis()
        jedisPool.resource.use { jedis ->
            try {
                logger.info("Start training task: ${currentTask!!.taskId}")

                val trainingParameter = currentTask!!.parameter
                val prototype = ModelPrototype.getPrototype(trainingParameter.modelIdentifier)
                    ?: throw IllegalArgumentException("Invalid model identifier: ${trainingParameter.modelIdentifier}")

                val trainFetcher = prototype.dataFetcherFactory.getDataFetcher(
                    DataFetcherParameter(
                        FilePathUtil.readFromFilePath(currentTask!!.trainingDataBytePath)
                            .let {
                                val result = it.readBytes()
                                it.close()
                                result
                            },
                        FilePathUtil.readFromFilePath(currentTask!!.trainingLabelBytePath)
                            .let {
                                val result = it.readBytes()
                                it.close()
                                result
                            },
                        trainingParameter.inputSize, trainingParameter.outputSize,
                        currentTask!!.trainingSamplesCount, trainingParameter.seed
                    )
                )
                val customTrain: DataSetIterator = prototype.dataSetIteratorFactory.getDataSetIterator(
                    trainFetcher, DataSetIteratorParameter(
                        trainingParameter.batchSize,
                        currentTask!!.trainingSamplesCount,
                    )
                )

                val testFetcher = prototype.dataFetcherFactory.getDataFetcher(
                    DataFetcherParameter(
                        FilePathUtil.readFromFilePath(currentTask!!.testDataBytePath)
                            .let {
                                val result = it.readBytes()
                                it.close()
                                result
                            },
                        FilePathUtil.readFromFilePath(currentTask!!.testLabelBytePath)
                            .let {
                                val result = it.readBytes()
                                it.close()
                                result
                            },
                        trainingParameter.inputSize, trainingParameter.outputSize,
                        currentTask!!.testSamplesCount, trainingParameter.seed
                    )
                )
                val customTest: DataSetIterator = prototype.dataSetIteratorFactory.getDataSetIterator(
                    testFetcher, DataSetIteratorParameter(
                        trainingParameter.batchSize,
                        currentTask!!.testSamplesCount,
                    )
                )

                val conf = prototype.getMultiLayerConfiguration(
                    MultiLayerNetworkParameter(
                        prototype.getModelInputSizeFromDataInputSize(trainingParameter.inputSize),
                        prototype.getModelOutputSizeFromLabelSize(trainingParameter.outputSize),
                        trainingParameter.updater,
                        trainingParameter.updaterParameters,
                        trainingParameter.networkParameter,
                        trainingParameter.seed
                    )
                )

                val model = MultiLayerNetwork(conf)
                // TODO read out model and continue training
                model.init()
                model.fit(customTrain, trainingParameter.epochs)
                val eval = model.evaluate<Evaluation>(customTest)
                val roc = model.evaluateROCMultiClass<ROCMultiClass>(customTest, 0)

                val rocValue = roc.calculateAUC(0)

                val report = TrainingTaskReport(
                    currentTask!!.taskId,
                    true,
                    "OK",
                    eval.stats(),
                    eval.accuracy(),
                    eval.precision(),
                    eval.recall(),
                    roc.stats(),
                    rocValue
                )

                val temp = File.createTempFile("task_", currentTask!!.taskId.toString())
                model.save(temp, true)
                val output = FilePathUtil.writeToFilePath(currentTask!!.modelSavePath)
                output.write(temp.readBytes())
                output.close()
                temp.delete()

                // 训练时间太短，硬等一分钟
                Thread.sleep(max(1, 60 * 1000 + startTime - System.currentTimeMillis()))

                jedis.publish(
                    RedisTaskReportChannel, JacksonJsonUtil.objectToJson(report)
                )
                logger.info("Finished task: ${currentTask!!.taskId}")
            } catch (e: Exception) {
                logger.error("Error when training task: ${currentTask!!.taskId}", e)
                jedis.publish(
                    RedisTaskReportChannel, JacksonJsonUtil.objectToJson(
                        TrainingTaskReport(
                            currentTask!!.taskId,
                            false,
                            e.localizedMessage
                        )
                    )
                )
            } finally {
                jedisLock!!.release()
                currentTask = null
                isBusy = false
            }
        }
    }
}
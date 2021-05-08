package info.skyblond.vovoku.worker

import info.skyblond.vovoku.commons.*
import info.skyblond.vovoku.commons.redis.JedisLock
import info.skyblond.vovoku.worker.datavec.CustomDataFetcher
import info.skyblond.vovoku.worker.datavec.CustomDataSetIterator
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
                        isBusy = false
                        require(jedisLock!!.renew()) { "Failed renew lock: ${jedisLock!!.lockKey}" }
                        isBusy = true
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
        val jedis = jedisPool.resource
        jedis.subscribe(jedisSub, RedisTaskDistributionChannel)
        jedis.quit()
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

        val jedis = jedisPool.resource

        try {
            logger.info("Start training task: ${currentTask!!.taskId}")

            // parse file path
            val trainingParameter = currentTask!!.parameter
            val trainFetcher = CustomDataFetcher(
                FilePathUtil.readFromFilePath(currentTask!!.trainingDataBytePath, currentTask!!.dataAccessToken)
                    .readBytes(),
                FilePathUtil.readFromFilePath(currentTask!!.trainingLabelBytePath, currentTask!!.dataAccessToken)
                    .readBytes(),
                currentTask!!.trainingSamplesCount, trainingParameter.seed
            )
            val customTrain: DataSetIterator = CustomDataSetIterator(trainingParameter.batchSize, trainFetcher)
            val testFetcher = CustomDataFetcher(
                FilePathUtil.readFromFilePath(currentTask!!.testDataBytePath, currentTask!!.dataAccessToken)
                    .readBytes(),
                FilePathUtil.readFromFilePath(currentTask!!.testLabelBytePath, currentTask!!.dataAccessToken)
                    .readBytes(),
                currentTask!!.testSamplesCount, trainingParameter.seed
            )
            val customTest: DataSetIterator = CustomDataSetIterator(trainingParameter.batchSize, testFetcher)

            val updater = parseUpdater(trainingParameter.updater, trainingParameter.updateParameters)

            val conf = getNeuralNetworkConfig(
                trainingParameter.seed, updater,
                trainingParameter.l2,
                trainingParameter.hiddenLayerSize,
                trainingParameter.inputWidth * trainingParameter.inputHeight,
                trainingParameter.outputSize
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
            val output = FilePathUtil.writeToFilePath(currentTask!!.modelSavePath, currentTask!!.modelAccessToken)
            output.write(temp.readBytes())
            output.close()
            temp.delete()

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
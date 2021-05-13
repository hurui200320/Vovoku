package info.skyblond.vovoku.backend.redis

import info.skyblond.vovoku.backend.config.ConfigUtil
import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.ModelInfo
import info.skyblond.vovoku.backend.database.ModelInfos
import info.skyblond.vovoku.commons.JacksonJsonUtil
import info.skyblond.vovoku.commons.RedisTaskDistributionChannel
import info.skyblond.vovoku.commons.RedisTaskReportChannel
import info.skyblond.vovoku.commons.RedisTokenToUserPrefix
import info.skyblond.vovoku.commons.models.ModelTrainingParameter
import info.skyblond.vovoku.commons.models.ModelTrainingStatus
import info.skyblond.vovoku.commons.models.TrainingTaskDistro
import info.skyblond.vovoku.commons.models.TrainingTaskReport
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisPubSub
import java.time.Duration

object RedisUtil : AutoCloseable {
    private val logger = LoggerFactory.getLogger(RedisUtil::class.java)
    private val redisConfig = ConfigUtil.config.redis

    private val jedisPool: JedisPool

    private val jedisTaskReportSub = object : JedisPubSub() {
        override fun onMessage(channel: String?, message: String?) {
            logger.info("Get message from $channel: $message")
            if (message == null)
                return

            val report = JacksonJsonUtil.jsonToObject<TrainingTaskReport>(message)

            val model = DatabaseUtil.database.sequenceOf(ModelInfos)
                .find { it.modelId eq report.taskId }

            if (model == null) {
                logger.error("Cannot find model with id ${report.taskId}")
                return
            }

            if (report.success) {
                model.addTrainingStatus(
                    ModelTrainingStatus.FINISHED,
                    "training finished: ${report.message}\n" +
                            "evaluateStatus: ${report.evaluateStatus}\n" +
                            "evaluateAccuracy: ${report.evaluateAccuracy}\n" +
                            "evaluatePrecision: ${report.evaluatePrecision}\n" +
                            "evaluateRecall: ${report.evaluateRecall}\n" +
                            "rocStatus: ${report.rocStatus}\n" +
                            "rocValue: ${report.rocValue}\n"
                )
                model.lastStatus = ModelTrainingStatus.FINISHED.name
            } else {
                model.addTrainingStatus(
                    ModelTrainingStatus.ERROR,
                    "training error: ${report.message}"
                )
                model.lastStatus = ModelTrainingStatus.ERROR.name
            }
            model.flushChanges()
        }
    }

    var duration: Duration = Duration.ofMinutes(30)
        set(value) {
            if (value.toSeconds() > Int.MAX_VALUE)
                throw IllegalArgumentException("Duration too long")
            field = value
        }

    init {
        val jedisPoolConfig = JedisPoolConfig()
        jedisPoolConfig.maxTotal = 1000
        jedisPoolConfig.maxIdle = 1000
        jedisPool = JedisPool(jedisPoolConfig, redisConfig.host, redisConfig.port)

        // start task report subscriber
        Thread {
            jedisPool.resource.use {
                it.subscribe(jedisTaskReportSub, RedisTaskReportChannel)
                it.quit()
            }
        }.start()
    }

    /**
     * Store and assign a token to use in redis
     * */
    fun setUserToken(userId: Int, token: String) {
        jedisPool.resource.use {
            it.setex(RedisTokenToUserPrefix + token, duration.toSeconds().toInt(), userId.toString())
        }
    }

    /**
     * Query and update user's token for userId
     * */
    fun queryToken(token: String, refresh: Boolean = true): Int? {
        return jedisPool.resource.use { jedis ->
            jedis.get(RedisTokenToUserPrefix + token)
                ?.toIntOrNull()
                ?.also {
                    if (refresh)
                        setUserToken(it, token)
                }
        }
    }

    fun <T> useJedis(block: (Jedis) -> T): T {
        return jedisPool.resource.use(block)
    }


    fun queryLock(key: String): Boolean {
        return jedisPool.resource.use { jedis ->
            jedis.get(key) != null
        }
    }

    fun publishTrainingTask(model: ModelInfo): Long {
        val result =  jedisPool.resource.use {
            it.publish(
                RedisTaskDistributionChannel,
                JacksonJsonUtil.objectToJson(
                    TrainingTaskDistro(
                        model.modelId,
                        ModelTrainingParameter(
                            model.createInfo.trainingParameter.modelIdentifier,
                            model.createInfo.trainingParameter.batchSize,
                            model.createInfo.trainingParameter.epochs,
                            model.createInfo.trainingParameter.inputSize,
                            model.createInfo.trainingParameter.outputSize,
                            model.createInfo.trainingParameter.updater,
                            model.createInfo.trainingParameter.updaterParameters,
                            model.createInfo.trainingParameter.networkParameter,
                            model.createInfo.trainingParameter.seed
                        ),
                        "file://${model.getTrainingDataFile().canonicalPath}",
                        "file://${model.getTrainingLabelFile().canonicalPath}",
                        model.createInfo.trainingPics.size,
                        "file://${model.getTestingDataFile().canonicalPath}",
                        "file://${model.getTestingLabelFile().canonicalPath}",
                        model.createInfo.testingPics.size,
                        "file://${model.getModelFile().canonicalPath}"
                    )
                )
            )
        }
        model.filePath = "file://${model.getModelFile().canonicalPath}"
        model.flushChanges()
        return result
    }

    override fun close() {
        jedisTaskReportSub.unsubscribe()
        jedisPool.close()
    }
}
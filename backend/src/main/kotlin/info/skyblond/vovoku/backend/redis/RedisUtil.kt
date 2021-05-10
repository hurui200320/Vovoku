package info.skyblond.vovoku.backend.redis

import info.skyblond.vovoku.backend.config.ConfigUtil
import info.skyblond.vovoku.commons.JacksonJsonUtil
import info.skyblond.vovoku.commons.RedisTaskDistributionChannel
import info.skyblond.vovoku.commons.RedisTaskReportChannel
import info.skyblond.vovoku.commons.RedisTokenToUserPrefix
import info.skyblond.vovoku.commons.models.ModelTrainingParameter
import info.skyblond.vovoku.commons.models.TrainingTaskDistro
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisPubSub
import java.time.Duration
import kotlin.random.Random

object RedisUtil : AutoCloseable {
    private val logger = LoggerFactory.getLogger(RedisUtil::class.java)
    private val redisConfig = ConfigUtil.config.redis

    private val jedisPool: JedisPool

    private val jedisTaskReportSub = object : JedisPubSub() {
        override fun onMessage(channel: String?, message: String?) {
//                logger.info("Get message from channel $channel: $message")
            // TODO with message
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

    fun publishTrainingTask() {
        jedisPool.resource.use {
            it.publish(
                RedisTaskDistributionChannel,
                JacksonJsonUtil.objectToJson(
                    TrainingTaskDistro(
                        Random.nextInt(),
                        ModelTrainingParameter(
                            128,
                            3,
                            28,
                            28,
                            1000,
                            10,
                            ModelTrainingParameter.Updater.Nesterovs,
                            listOf(0.1, 0.9),
                            1e-4,
                            123L
                        ),
                        "file://D:\\Git\\github\\Vovoku\\worker\\train_image_byte.bin",
                        "file://D:\\Git\\github\\Vovoku\\worker\\train_label_byte.bin",
                        60000,
                        "file://D:\\Git\\github\\Vovoku\\worker\\test_image_byte.bin",
                        "file://D:\\Git\\github\\Vovoku\\worker\\test_label_byte.bin",
                        10000,
                        "file://D:\\Git\\github\\Vovoku\\test.model"
                    )
                )
            )
        }
        TODO()
    }

    override fun close() {
        jedisTaskReportSub.unsubscribe()
        jedisPool.close()
    }
}
package info.skyblond.vovoku.worker

import info.skyblond.vovoku.commons.*
import info.skyblond.vovoku.commons.models.ModelTrainingParameter
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import kotlin.random.Random

class WorkerTest {
    @Test
    fun demo() {

        val logger = LoggerFactory.getLogger("Application")

        val jedisSub = object : JedisPubSub() {
            override fun onMessage(channel: String?, message: String?) {
                logger.info("Get message from channel $channel: $message")
                // TODO 处理消息，分布式时通过Redis加锁更新数据库
            }
        }

        Thread {
            Jedis("127.0.0.1").also {
                it.subscribe(jedisSub, RedisTaskReportChannel)
                it.quit()
            }
        }.start()


        val jedis = Jedis("127.0.0.1")
        jedis.publish(
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
                    "",
                    "file://D:\\Git\\github\\Vovoku\\test.model",
                    ""
                )
            )
        )
    }
}
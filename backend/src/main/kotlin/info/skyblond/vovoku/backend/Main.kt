package info.skyblond.vovoku.backend

import info.skyblond.vovoku.commons.*
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import kotlin.random.Random


fun main() {
    val logger = LoggerFactory.getLogger("Application")

    val jedisSub = object : JedisPubSub() {
        override fun onMessage(channel: String?, message: String?) {
            logger.info("Get message from channel $channel: $message")
            // TODO with message
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
                    28 * 28,
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


//    JavalinJackson.configure(ObjectMapper())
//    val app = Javalin.create().start(7000)
//    app.get("/") { ctx ->
//        ctx.json(object {
//            val x = 10
//        })
//    }

//
//    database.sequenceOf(Users)
//        .add(User {
//            username = "hurui"
//            password = CryptoUtil.md5("some password")
//        })
//
//    println(database.sequenceOf(Users)
//        .find { it.username eq "hurui" })


}


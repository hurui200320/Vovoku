package info.skyblond.vovoku.backend

import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import java.util.concurrent.CountDownLatch


fun main() {
    val logger = LoggerFactory.getLogger("Application")

    val pubWaitLatch = CountDownLatch(1)
    val subWaitLatch = CountDownLatch(1)

    val jedisSub = object : JedisPubSub() {
        override fun onMessage(channel: String?, message: String?) {
            logger.info("Get message from channel $channel: $message")
            subWaitLatch.countDown()
        }

        override fun onSubscribe(channel: String?, subscribedChannels: Int) {
            logger.info("Sub channel: $channel")
        }

        override fun onUnsubscribe(channel: String?, subscribedChannels: Int) {
            logger.info("Unsub channel: $channel")
        }
    }

    Thread {
        val jedis = Jedis("127.0.0.1")
        pubWaitLatch.await()
        logger.info("Publishing message...")
        jedis.publish("test", "This is a test message, can be a task json")
        Thread.sleep(1000)
        logger.info("Publisher quit")
        jedis.quit()
    }.start()

    Thread {
        val jedis = Jedis("127.0.0.1")
        logger.info("Start sub")
        jedis.subscribe(jedisSub, "test")
        logger.info("Sub is done")
        jedis.quit()
    }.start()

    Thread.sleep(1000)

    pubWaitLatch.countDown()
    subWaitLatch.await()
    logger.info("Unsubing channel...")
    jedisSub.unsubscribe()

//    JavalinJackson.configure(ObjectMapper())
//    val app = Javalin.create().start(7000)
//    app.get("/") { ctx ->
//        ctx.json(object {
//            val x = 10
//        })
//    }

//    val hikariDataSource = HikariDataSource()
//    hikariDataSource.jdbcUrl = "jdbc:postgresql://localhost:5432/postgres"
//    hikariDataSource.username = "postgres"
//    hikariDataSource.password = "passwr0d"
//    hikariDataSource.dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
//
//    val database = Database.connect(
//        dataSource = hikariDataSource,
//        dialect = PostgreSqlDialect(),
//        logger = Slf4jLoggerAdapter(logger)
//    )
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


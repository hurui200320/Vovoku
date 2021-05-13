package info.skyblond.vovoku.backend

import info.skyblond.vovoku.backend.config.ConfigUtil
import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.ModelInfos
import info.skyblond.vovoku.backend.handler.setInterceptor
import info.skyblond.vovoku.backend.handler.setRouter
import info.skyblond.vovoku.backend.redis.RedisUtil
import info.skyblond.vovoku.backend.thread.ThreadUtil
import info.skyblond.vovoku.commons.JacksonJsonUtil
import info.skyblond.vovoku.commons.RedisTaskLockKeyPrefix
import info.skyblond.vovoku.commons.models.ModelTrainingStatus
import io.javalin.Javalin
import io.javalin.plugin.json.JavalinJackson
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.forEach
import org.ktorm.entity.sequenceOf
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter


fun main() {
    val logger = LoggerFactory.getLogger("Application")

    val apiConfig = ConfigUtil.config.api
    val aesKey = SecretKeySpec(DatatypeConverter.parseHexBinary(apiConfig.adminAesKey), "AES")

    JavalinJackson.configure(JacksonJsonUtil.jsonMapper)
    val app = Javalin.create { config ->
        config.enableCorsForAllOrigins()
    }.start(apiConfig.host, apiConfig.port)

    setInterceptor(app, aesKey)
    setRouter(app)

    // launch data generation thread
    ThreadUtil.launchDataGenThread(2, TimeUnit.MINUTES)
    ThreadUtil.launchDataGenThread(3, TimeUnit.MINUTES)
    ThreadUtil.launchDataGenThread(5, TimeUnit.MINUTES)
    // launch redis task distributing thread
    ThreadUtil.launchTaskDistributionThread(1, TimeUnit.MINUTES)
    // the thread checking if a lock is showed
    ThreadUtil.launchTaskCheckingThread(7, TimeUnit.SECONDS)

    logger.info("Main thread done")
}


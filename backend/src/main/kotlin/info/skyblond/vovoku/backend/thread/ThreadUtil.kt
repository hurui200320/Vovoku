package info.skyblond.vovoku.backend.thread

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.ModelInfos
import info.skyblond.vovoku.backend.redis.RedisUtil
import info.skyblond.vovoku.commons.RedisDataGenerationLockKeyPrefix
import info.skyblond.vovoku.commons.RedisTaskLockKeyPrefix
import info.skyblond.vovoku.commons.models.ModelTrainingStatus
import info.skyblond.vovoku.commons.redis.JedisLock
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.forEach
import org.ktorm.entity.sequenceOf
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


object ThreadUtil : AutoCloseable {
    private val logger = LoggerFactory.getLogger(ThreadUtil::class.java)
    private val executor = Executors.newScheduledThreadPool(
        Runtime.getRuntime().availableProcessors() * 2
    ) { runnable ->
        Executors.defaultThreadFactory().newThread(runnable)
            .also { it.isDaemon = true }
    } as ScheduledThreadPoolExecutor

    fun submit(
        initialDelay: Long,
        period: Long,
        unit: TimeUnit,
        block: () -> Unit
    ): ScheduledFuture<*> {
        return executor.scheduleAtFixedRate({
            try {
                block()
            } catch (e: Exception) {
                logger.error("Uncaught exception: ${e.localizedMessage}")
                throw e
            }
        }, initialDelay, period, unit)
    }

    fun launchDataGenThread(period: Long, unit: TimeUnit) {
        submit(0, period, unit) {
            RedisUtil.useJedis { jedis ->
                DatabaseUtil.database.sequenceOf(ModelInfos)
                    .filter { it.lastStatus eq ModelTrainingStatus.INITIALIZING.name }
                    .forEach { model ->
                        // in case we have a racing condition, lock it first
                        val jedisLock = JedisLock(
                            jedis, RedisDataGenerationLockKeyPrefix + model.modelId,
                            Duration.ofHours(1)
                        )
                        if (!jedisLock.acquire())
                            return@forEach
                        try {
                            logger.info("Start generating model ${model.modelId}'s training data...")
                            generateTrainingData(model)
                            model.addTrainingStatus(
                                ModelTrainingStatus.DISTRIBUTING,
                                "Training data generated"
                            )
                            model.lastStatus = ModelTrainingStatus.DISTRIBUTING.name
                            logger.info("Data for model ${model.modelId} is ready")
                        } catch (e: Exception) {
                            model.addTrainingStatus(
                                ModelTrainingStatus.ERROR,
                                "Error when generating training data: ${e.localizedMessage}"
                            )
                            model.lastStatus = ModelTrainingStatus.ERROR.name
                            logger.error("Cannot generate model ${model.modelId}'s training data", e)
                            jedisLock.release()
                        } finally {
                            model.flushChanges()
                            // in case later thread regenerate
                            jedisLock.renew()
                        }
                    }
            }
        }
    }

    fun launchTaskDistributionThread(period: Long, unit: TimeUnit) {
        submit(0, period, unit) {
            DatabaseUtil.database.sequenceOf(ModelInfos)
                .filter { it.lastStatus eq ModelTrainingStatus.DISTRIBUTING.name }
                .forEach { model ->
                    try {
                        logger.info("Distributing task ${model.modelId}...")
                        RedisUtil.publishTrainingTask(model)
                        model.addTrainingStatus(
                            ModelTrainingStatus.DISTRIBUTING,
                            "Distributing task to workers"
                        )
                        model.lastStatus = ModelTrainingStatus.DISTRIBUTING.name
                    } catch (e: Exception) {
                        model.addTrainingStatus(
                            ModelTrainingStatus.ERROR,
                            "Error when distributing task: ${e.localizedMessage}"
                        )
                        model.lastStatus = ModelTrainingStatus.ERROR.name
                        logger.error("Cannot distribute task ${model.modelId}:", e)
                    } finally {
                        model.flushChanges()
                    }
                }
        }
    }

    fun launchTaskCheckingThread(period: Long, unit: TimeUnit) {
        submit(0, period, unit) {
            DatabaseUtil.database.sequenceOf(ModelInfos)
                .filter { it.lastStatus eq ModelTrainingStatus.DISTRIBUTING.name }
                .forEach { model ->
                    if (RedisUtil.queryLock(RedisTaskLockKeyPrefix + model.modelId)) {
                        logger.info("Task ${model.modelId} id accepted by worker")
                        model.addTrainingStatus(
                            ModelTrainingStatus.TRAINING,
                            "Task accepted by worker"
                        )
                        model.lastStatus = ModelTrainingStatus.TRAINING.name
                        model.flushChanges()
                    }
                }
        }
    }

    override fun close() {
        executor.shutdown()
    }
}
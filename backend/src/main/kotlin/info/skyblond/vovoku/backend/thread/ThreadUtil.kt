@file:Suppress("SameParameterValue")

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
import java.util.concurrent.*


object ThreadUtil : AutoCloseable {
    private val logger = LoggerFactory.getLogger(ThreadUtil::class.java)
    private val scheduleExecutor = Executors.newScheduledThreadPool(8) { runnable ->
        Executors.defaultThreadFactory().newThread(runnable)
            .also { it.isDaemon = true }
    } as ScheduledThreadPoolExecutor

    private val workerThreadPool = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors() * 2
    ) { runnable ->
        Executors.defaultThreadFactory().newThread(runnable)
            .also { it.isDaemon = true }
    } as ThreadPoolExecutor

    private fun schedule(
        initialDelay: Long,
        period: Long,
        unit: TimeUnit,
        block: () -> Unit
    ): ScheduledFuture<*> {
        return scheduleExecutor.scheduleAtFixedRate({
            try {
                block()
            } catch (e: Exception) {
                logger.error("Uncaught exception: ${e.localizedMessage}")
                throw e
            }
        }, initialDelay, period, unit)
    }

    fun launchDataGenThread(period: Long, unit: TimeUnit) {
        schedule(0, period, unit) {
            logger.info("Scanning task for generating data")
            RedisUtil.useJedis { jedis ->
                DatabaseUtil.database.sequenceOf(ModelInfos)
                    .filter { it.lastStatus eq ModelTrainingStatus.INITIALIZING.name }
                    .forEach { model ->
                        workerThreadPool.submit {
                            // in case we have a racing condition, lock it first
                            val jedisLock = JedisLock(
                                jedis, RedisDataGenerationLockKeyPrefix + model.modelId,
                                Duration.ofHours(1)
                            )
                            if (jedisLock.acquire()) {
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
        }
    }

    fun launchTaskDistributionThread(period: Long, unit: TimeUnit) {
        schedule(0, period, unit) {
            logger.info("Scanning task for distributing task")
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
        schedule(0, period, unit) {
            DatabaseUtil.database.sequenceOf(ModelInfos)
                .filter { it.lastStatus eq ModelTrainingStatus.DISTRIBUTING.name }
                .forEach { model ->
                    if (RedisUtil.queryLock(RedisTaskLockKeyPrefix + model.modelId)) {
                        logger.info("Task ${model.modelId} accepted by worker")
                        model.addTrainingStatus(
                            ModelTrainingStatus.TRAINING,
                            "Task accepted by worker"
                        )
                        model.lastStatus = ModelTrainingStatus.TRAINING.name
                        model.flushChanges()
                    }
                }
            DatabaseUtil.database.sequenceOf(ModelInfos)
                .filter { it.lastStatus eq ModelTrainingStatus.TRAINING.name }
                .forEach { model ->
                    if (!RedisUtil.queryLock(RedisTaskLockKeyPrefix + model.modelId)) {
                        logger.info("Task ${model.modelId} training lock lost")
                        model.addTrainingStatus(
                            ModelTrainingStatus.ERROR,
                            "Training lock lost"
                        )
                        model.lastStatus = ModelTrainingStatus.ERROR.name
                        model.flushChanges()
                    }
                }
        }
    }

    override fun close() {
        scheduleExecutor.shutdown()
        workerThreadPool.shutdown()
    }
}
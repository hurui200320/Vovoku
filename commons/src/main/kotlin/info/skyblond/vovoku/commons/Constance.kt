package info.skyblond.vovoku.commons

/**
 * Channel name for backend distributing training tasks
 * */
const val RedisTaskDistributionChannel = "task"

/**
 * Channel name for works report tasks.
 * */
const val RedisTaskReportChannel = "taskReport"

/**
 * Redis lock prefix for workers locking a task with id.
 * key: ${prefix}.taskId
 * */
const val RedisTaskLockKeyPrefix = "task."

/**
 * Redis key prefix for backend storing token info.
 * Token -> UserId
 * */
const val RedisTokenToUserPrefix = "token_to_user."

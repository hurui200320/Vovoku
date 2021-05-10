package info.skyblond.vovoku.worker

data class Config(
    val redisHost: String = "localhost",
    val redisPort: Int = 6379
)
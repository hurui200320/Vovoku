package info.skyblond.vovoku.backend.config

data class RedisConfig(
    val host: String = "localhost",
    val port: Int = 6379
)

data class DatabaseConfig(
    val host: String = "localhost",
    val port: Int = 5432,
    val databaseName: String = "postgres",
    val username: String = "postgres",
    val password: String = ""
) {
    val jdbcUrl
        get() = "jdbc:postgresql://$host:$port/$username"
}

data class ApiConfig(
    val host: String = "localhost",
    val port: Int = 7000
)

data class Config(
    val redis: RedisConfig = RedisConfig(),
    val postgresql: DatabaseConfig = DatabaseConfig(),
    val api: ApiConfig = ApiConfig()
)
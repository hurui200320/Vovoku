package info.skyblond.vovoku.backend.config

import com.fasterxml.jackson.annotation.JsonIgnore
import info.skyblond.vovoku.commons.CryptoUtil

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
    @JsonIgnore
    val jdbcUrl = "jdbc:postgresql://$host:$port/$username"
}

data class ApiConfig(
    val host: String = "localhost",
    val port: Int = 7000,
    val adminAesKey: String = "",
    val dataFolderPath: String = "./data"
)

data class Config(
    val redis: RedisConfig = RedisConfig(),
    val postgresql: DatabaseConfig = DatabaseConfig(),
    val api: ApiConfig = ApiConfig()
)

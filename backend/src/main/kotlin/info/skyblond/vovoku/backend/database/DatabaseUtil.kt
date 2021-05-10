package info.skyblond.vovoku.backend.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import info.skyblond.vovoku.backend.config.ConfigUtil
import org.ktorm.database.Database
import org.ktorm.logging.Slf4jLoggerAdapter
import org.ktorm.support.postgresql.PostgreSqlDialect
import org.slf4j.LoggerFactory

object DatabaseUtil : AutoCloseable {
    private val logger = LoggerFactory.getLogger(DatabaseUtil::class.java)
    private val databaseConfig = ConfigUtil.config.postgresql

    val database: Database

    private val hikariDataSource: HikariDataSource

    init {
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = databaseConfig.jdbcUrl
        hikariConfig.username = databaseConfig.username
        hikariConfig.password = databaseConfig.password
        hikariDataSource = HikariDataSource(hikariConfig)

        database = Database.connect(
            dataSource = hikariDataSource,
            dialect = PostgreSqlDialect(),
            logger = Slf4jLoggerAdapter(logger)
        )

    }

    override fun close() {
        hikariDataSource.close()
    }
}
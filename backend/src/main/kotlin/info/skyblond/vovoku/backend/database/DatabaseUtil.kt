package info.skyblond.vovoku.backend.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariProxyPreparedStatement
import info.skyblond.vovoku.backend.config.ConfigUtil
import org.ktorm.database.Database
import org.ktorm.jackson.JsonSqlType
import org.ktorm.logging.Slf4jLoggerAdapter
import org.ktorm.support.postgresql.PostgreSqlDialect
import org.postgresql.ds.PGConnectionPoolDataSource
import org.postgresql.ds.PGSimpleDataSource
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
//        val tempDataSource = PGSimpleDataSource()
//        tempDataSource.databaseName = "postgres"
//        tempDataSource.serverNames = arrayOf("localhost")
//        tempDataSource.portNumbers = intArrayOf(5432)
//        tempDataSource.user = databaseConfig.username
//        tempDataSource.password = databaseConfig.password

        database = Database.connect(
            dataSource = hikariDataSource,
//            dataSource = tempDataSource,
            dialect = PostgreSqlDialect(),
            logger = Slf4jLoggerAdapter(logger)
        )

    }

    override fun close() {
        hikariDataSource.close()
    }
}
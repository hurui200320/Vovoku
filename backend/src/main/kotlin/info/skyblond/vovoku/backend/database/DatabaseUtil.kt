package info.skyblond.vovoku.backend.database

import com.zaxxer.hikari.HikariDataSource
import info.skyblond.vovoku.backend.config.ConfigUtil
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.logging.Slf4jLoggerAdapter
import me.liuwj.ktorm.support.postgresql.PostgreSqlDialect
import org.slf4j.LoggerFactory

object DatabaseUtil : AutoCloseable {
    private val logger = LoggerFactory.getLogger(DatabaseUtil::class.java)
    private val databaseConfig = ConfigUtil.config.postgresql

    val database: Database
    private val hikariDataSource: HikariDataSource = HikariDataSource()

    init {
        hikariDataSource.jdbcUrl = databaseConfig.jdbcUrl
        hikariDataSource.username = databaseConfig.username
        hikariDataSource.password = databaseConfig.password
        hikariDataSource.dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"

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
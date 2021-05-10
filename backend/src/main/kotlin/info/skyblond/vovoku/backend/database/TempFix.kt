package info.skyblond.vovoku.backend.database

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import org.ktorm.jackson.sharedObjectMapper
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column
import org.ktorm.schema.SqlType
import org.ktorm.schema.typeOf
import org.postgresql.util.PGobject
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

/**
 * TODO
 *   Due to https://github.com/kotlin-orm/ktorm/issues/268
 *   HikariCP will not trigger ktorm to use postgresql way for json type
 *   Thus I create this temp fix to force ktorm use the correct way
 * */

inline fun <reified C : Any> BaseTable<*>.postgresJson(
    name: String,
    mapper: ObjectMapper = sharedObjectMapper
): Column<C> {
    return registerColumn(name, PostgresJsonSqlType(mapper, mapper.constructType(typeOf<C>())))
}

class PostgresJsonSqlType<T : Any>(
    private val objectMapper: ObjectMapper,
    private val javaType: JavaType
) : SqlType<T>(Types.OTHER, "json") {

    override fun setParameter(ps: PreparedStatement, index: Int, parameter: T?) {
        if (parameter != null) {
            doSetParameter(ps, index, parameter)
        } else {
            ps.setNull(index, Types.OTHER)
        }
    }

    override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: T) {
        val obj = PGobject()
        obj.type = "json"
        obj.value = objectMapper.writeValueAsString(parameter)
        ps.setObject(index, obj)
    }

    override fun doGetResult(rs: ResultSet, index: Int): T? {
        val json = rs.getString(index)
        return if (json.isNullOrBlank()) {
            null
        } else {
            objectMapper.readValue(json, javaType)
        }
    }
}
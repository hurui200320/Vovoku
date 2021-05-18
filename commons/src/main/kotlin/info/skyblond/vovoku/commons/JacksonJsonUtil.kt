package info.skyblond.vovoku.commons

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.StandardCharsets


object JacksonJsonUtil {
    private val logger: Logger = LoggerFactory.getLogger(JacksonJsonUtil::class.java)
    val jsonMapper = ObjectMapper(JsonFactory()).also {
        it.findAndRegisterModules()
        it.registerKotlinModule()
    }

    inline fun <reified T> jsonToObject(json: String): T {
        return jsonMapper.readValue(json, T::class.java)
    }

    fun <T> jsonToObject(json: String, clazz: Class<T>): T {
        return jsonMapper.readValue(json, clazz)
    }

    @JvmOverloads
    fun objectToJson(obj: Any, pretty: Boolean = false): String {
        return if (pretty) {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
        } else {
            jsonMapper.writeValueAsString(obj)
        }
    }

    fun writeToFile(file: File, obj: Any) {
        file.writeText(objectToJson(obj), StandardCharsets.UTF_8)
    }

    inline fun <reified T> readFromFile(file: File): T {
        return jsonToObject(file.readText(StandardCharsets.UTF_8))
    }

}
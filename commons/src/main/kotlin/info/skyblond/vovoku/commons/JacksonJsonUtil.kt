package info.skyblond.vovoku.commons

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File


object JacksonJsonUtil {
    val logger: Logger = LoggerFactory.getLogger(JacksonJsonUtil::class.java)
    private val jsonMapper = ObjectMapper(JsonFactory()).also { it.findAndRegisterModules() }

    fun <T> jsonToObject(json: String, clazz: Class<T>): T{
        return jsonMapper.readValue(json, clazz)
    }

    fun objectToJson(obj: Any): String {
        return jsonMapper.writeValueAsString(obj)
    }

}
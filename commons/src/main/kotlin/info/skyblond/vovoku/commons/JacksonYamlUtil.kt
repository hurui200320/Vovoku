package info.skyblond.vovoku.commons

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.reflect.KClass


object JacksonYamlUtil {
    val logger: Logger = LoggerFactory.getLogger(JacksonYamlUtil::class.java)
    private val yamlMapper = ObjectMapper(YAMLFactory()).also { it.findAndRegisterModules() }

    fun <T> readYamlFile(file: File, clazz: Class<T>): T = yamlMapper.readValue(file, clazz)

    fun <T> writeYamlFile(file: File, obj: T) = yamlMapper.writeValue(file, obj)

    inline fun <reified T> readOrInitConfigFile(file: File, default: T): T {
        return try {
            readYamlFile(file, T::class.java)
        }catch (e: Exception) {
            logger.warn("Cannot read config file '${file.absolutePath}': ", e)
            logger.warn("Creating default config...")
            writeYamlFile(file, default)
            default
        }
    }

}
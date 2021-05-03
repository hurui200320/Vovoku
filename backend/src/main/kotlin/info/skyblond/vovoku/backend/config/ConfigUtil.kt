package info.skyblond.vovoku.backend.config

import info.skyblond.vovoku.commons.JacksonYamlUtil
import java.io.File

object ConfigUtil {
    val config: Config

    init {
        config = JacksonYamlUtil.readOrInitConfigFile(File("./config.yaml"), Config())
    }
}
package info.skyblond.vovoku.backend.config

import info.skyblond.vovoku.commons.JacksonYamlUtil
import java.io.File

object ConfigUtil {
    val config: Config
    val dataBaseDir: File
    val uploadBaseDir: File
    val modelBaseDir: File

    init {
        config = JacksonYamlUtil.readOrInitConfigFile(File("./config.yaml"), Config())
        dataBaseDir = File(config.api.dataFolderPath).also {
            if (!it.isDirectory)
                it.delete()
        }

        uploadBaseDir = File(dataBaseDir, "upload").also {
            if (!it.isDirectory)
                it.delete()
            if (!it.exists())
                require(it.mkdirs()) { "Cannot create upload storage folder: ${it.canonicalPath}" }
        }

        modelBaseDir = File(dataBaseDir, "model").also {
            if (!it.isDirectory)
                it.delete()
            if (!it.exists())
                require(it.mkdirs()) { "Cannot create temp folder: ${it.canonicalPath}" }
        }

    }
}
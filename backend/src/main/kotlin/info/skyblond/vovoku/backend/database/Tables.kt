package info.skyblond.vovoku.backend.database

import info.skyblond.vovoku.commons.models.ModelCreateInfo
import info.skyblond.vovoku.commons.models.ModelTrainingInfo
import info.skyblond.vovoku.commons.models.PictureTagEntry
import me.liuwj.ktorm.jackson.json
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.int
import me.liuwj.ktorm.schema.text
import me.liuwj.ktorm.schema.varchar

object Users : Table<User>("user") {
    val userId = int("user_id").primaryKey().bindTo { it.userId }
    val username = varchar("username").bindTo { it.username }
    val password = varchar("password").bindTo { it.password }
}

object PictureTags : Table<PictureTag>("picture_tag") {
    val tagId = int("tag_id").primaryKey().bindTo { it.tagId }
    val filePath = text("file_path").bindTo { it.filePath }
    val userId = int("user_id").bindTo { it.userId }
    val tagData = json<MutableList<PictureTagEntry>>("tag_data").bindTo { it.tagData }
}

object ModelInfos : Table<ModelInfo>("model_info") {
    val modelId = int("model_id").primaryKey().bindTo { it.modelId }
    val filePath = text("file_path").bindTo { it.filePath }
    val userId = int("user_id").bindTo { it.userId }
    val createInfo = json<ModelCreateInfo>("create_info").bindTo { it.createInfo }
    val trainingInfo = json<ModelTrainingInfo>("training_info").bindTo { it.trainingInfo }
}
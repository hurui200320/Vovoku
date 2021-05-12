package info.skyblond.vovoku.backend.database

import info.skyblond.vovoku.commons.models.ModelCreateInfo
import info.skyblond.vovoku.commons.models.ModelTrainingInfo
import info.skyblond.vovoku.commons.models.PictureTagEntry
import org.ktorm.jackson.json
import org.ktorm.schema.*

object Users : Table<User>("user") {
    val userId = int("user_id").primaryKey().bindTo { it.userId }
    val username = varchar("username").bindTo { it.username }
    val password = varchar("password").bindTo { it.password }
}

object PictureTags : Table<PictureTag>("picture_tag") {
    val tagId = int("tag_id").primaryKey().bindTo { it.tagId }
    val filePath = text("file_path").bindTo { it.filePath }
    val userId = int("user_id").bindTo { it.userId }
    val tagData = json<PictureTagEntry>("tag_data").bindTo { it.tagData }
    val usedForTrain = boolean("used_for_train").bindTo { it.usedForTrain }
    val folderName = text("folder_name").bindTo { it.folderName }
}

object ModelInfos : Table<ModelInfo>("model_info") {
    val modelId = int("model_id").primaryKey().bindTo { it.modelId }
    val filePath = text("file_path").bindTo { it.filePath }
    val userId = int("user_id").bindTo { it.userId }
    val createInfo = json<ModelCreateInfo>("create_info").bindTo { it.createInfo }
    val trainingInfo = json<ModelTrainingInfo>("training_info").bindTo { it.trainingInfo }
}
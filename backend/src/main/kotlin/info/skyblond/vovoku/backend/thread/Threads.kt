package info.skyblond.vovoku.backend.thread

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.ModelInfo
import info.skyblond.vovoku.backend.database.PictureTags
import info.skyblond.vovoku.commons.FilePathUtil
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import java.io.DataOutputStream
import java.io.OutputStream

/**
 * This function is used to generate data used by worker
 * */
fun generateTrainingData(model: ModelInfo) {

    // since we successfully get the lock
    // we can start generating data
    writeDataToFile(
        model.createInfo.trainingPics,
        model.getTrainingDataFile().also { it.delete() }.outputStream(),
        model.getTrainingLabelFile().also { it.delete() }.outputStream()
    )
    writeDataToFile(
        model.createInfo.testingPics,
        model.getTestingDataFile().also { it.delete() }.outputStream(),
        model.getTestingLabelFile().also { it.delete() }.outputStream()
    )
}


internal fun writeDataToFile(picIds: List<Int>, dataStream: OutputStream, labelStream: OutputStream) {
    dataStream.use { dataFileOutputStream ->
        DataOutputStream(labelStream).use { labelFileOutputStream ->
            picIds.forEach { picId ->
                val pic = DatabaseUtil.database.sequenceOf(PictureTags).find { it.tagId eq picId }
                    ?: throw Exception("Pic $picId not found")
                FilePathUtil.readFromFilePath(pic.filePath).also {
                    it.copyTo(dataFileOutputStream)
                    it.close()
                }
                labelFileOutputStream.writeInt(pic.tagData.tag)
            }
        }
    }
}
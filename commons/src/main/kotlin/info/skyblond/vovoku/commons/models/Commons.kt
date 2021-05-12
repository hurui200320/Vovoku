package info.skyblond.vovoku.commons.models

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonFormat
import info.skyblond.vovoku.commons.dl4j.PrototypeDescriptor
import java.sql.Timestamp


enum class ModelTrainingStatus {
    /**
     * Create a task, but backend are preparing data.
     * */
    INITIALIZING,

    /**
     * Pushing data to redis and waiting worker to claim.
     * */
    DISTRIBUTING,

    /**
     * A worker claimed and in training process.
     * */
    TRAINING,

    /**
     * Error occurred.
     * */
    ERROR,

    /**
     * Training is finished without error.
     * */
    FINISHED,

    /**
     * Training terminated
     * */
    TERMINATED
}


data class PictureTagEntry(
    val width: Int,
    val height: Int,
    val channelCount: Int,
    val tag: Int
) {
    init {
        require(channelCount == 1 || channelCount == 3) { "Field 'channelCount' must 1 or 3" }
        require(width >= 0) { "Field 'width' must bigger than 0" }
        require(height >= 0) { "Field 'height' must bigger than 0" }
        require(tag in 0..9) { "Field 'tag' must in range 0 to 9" }
    }
}

data class ModelCreateInfo(
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "s")
    val createTime: Timestamp,
    val trainingParameter: ModelTrainingParameter,
    val trainingPics: MutableList<Int>,
    val testingPics: MutableList<Int>,
    // save this in case model update
    val prototypeDescriptionSnapshot: PrototypeDescriptor
)

data class ModelTrainingInfo(
    /**
     * List of training status. Triple: Status, time and message.
     * */
    val statusList: MutableList<Triple<ModelTrainingStatus, Timestamp, String>> = mutableListOf()
)
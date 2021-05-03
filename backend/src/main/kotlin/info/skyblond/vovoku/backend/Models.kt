package info.skyblond.vovoku.backend

import com.fasterxml.jackson.annotation.JsonFormat
import java.sql.Timestamp

data class PictureTagEntry(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val tag: Int
) {
    init {
        require(x >= 0) {"Field 'x' must bigger than 0"}
        require(y >= 0) {"Field 'y' must bigger than 0"}
        require(width >= 0) {"Field 'width' must bigger than 0"}
        require(height >= 0) {"Field 'height' must bigger than 0"}
        require(tag in 0..9) {"Field 'tag' must in range 0 to 9"}
    }
}

data class ModelTrainingParameter(
    val learningRate: Double
    // TODO
)

data class ModelCreateInfo(
    @JsonFormat(shape=JsonFormat.Shape.NUMBER, pattern="s")
    val createTime: Timestamp,
    val trainingParameter: ModelTrainingParameter
)

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
    FINISHED
}

data class ModelTrainingInfo(
    /**
     * List of training status. Triple: Status, time and message.
     * */
    val statusList: MutableList<Triple<ModelTrainingStatus, Timestamp, String>> = mutableListOf()
)
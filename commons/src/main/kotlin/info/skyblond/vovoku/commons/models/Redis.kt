package info.skyblond.vovoku.commons

// TODO

data class ModelTrainingParameter(
    val batchSize: Int,
    val epochs: Int,
    val inputWidth: Int,
    val inputHeight: Int,
    val hiddenLayerSize: Int,
    val outputSize: Int,
    val updater: Updater,
    val updateParameters: List<Double>,
    val l2: Double,
    val seed: Long
){
    enum class Updater {
        /**
         * Parameter:
         *  learningRate = 1e-3,
         *  beta1 = 0.9,
         *  beta2 = 0.999,
         *  epsilon = 1e-8
         * */
        Adam,
        /**
         * Parameter:
         *  learningRate = 0.1,
         *  momentum = 0.9
         * */
        Nesterovs
    }
}

data class TrainingTaskDistro(
    val taskId: Int,
    val parameter: ModelTrainingParameter,
    val trainingDataBytePath: String,
    val trainingLabelBytePath: String,
    val trainingSamplesCount: Int,
    val testDataBytePath: String,
    val testLabelBytePath: String,
    val testSamplesCount: Int,
    val dataAccessToken: String,
    val modelSavePath: String,
    val modelAccessToken: String
)

data class TrainingTaskReport(
    val taskId: Int,
    val success: Boolean,
    val message: String,
    val evaluateStatus: String = "",
    val evaluateAccuracy: Double = Double.NaN,
    val evaluatePrecision: Double = Double.NaN,
    val evaluateRecall: Double = Double.NaN,
    val rocStatus: String = "",
    val rocValue: Double = Double.NaN,
)
package info.skyblond.vovoku.commons.models

import info.skyblond.vovoku.commons.dl4j.Updater


data class ModelTrainingParameter(
    val modelIdentifier: String,
    val batchSize: Int,
    val epochs: Int,
    val inputSize: IntArray,
    val outputSize: IntArray,
    val updater: Updater,
    val updaterParameters: DoubleArray,
    val networkParameter: DoubleArray,
    val seed: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelTrainingParameter

        if (modelIdentifier != other.modelIdentifier) return false
        if (batchSize != other.batchSize) return false
        if (epochs != other.epochs) return false
        if (!inputSize.contentEquals(other.inputSize)) return false
        if (!outputSize.contentEquals(other.outputSize)) return false
        if (updater != other.updater) return false
        if (!updaterParameters.contentEquals(other.updaterParameters)) return false
        if (!networkParameter.contentEquals(other.networkParameter)) return false
        if (seed != other.seed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = modelIdentifier.hashCode()
        result = 31 * result + batchSize
        result = 31 * result + epochs
        result = 31 * result + inputSize.contentHashCode()
        result = 31 * result + outputSize.contentHashCode()
        result = 31 * result + updater.hashCode()
        result = 31 * result + updaterParameters.contentHashCode()
        result = 31 * result + networkParameter.contentHashCode()
        result = 31 * result + seed.hashCode()
        return result
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
    val modelSavePath: String
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
package info.skyblond.vovoku.commons.dl4j

data class DataFetcherParameter(
    val inputByteArray: ByteArray,
    val labelByteArray: ByteArray,
    val inputSize: IntArray,
    val labelSize: IntArray,
    val numExamples: Int,
    val seed: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataFetcherParameter

        if (!inputByteArray.contentEquals(other.inputByteArray)) return false
        if (!labelByteArray.contentEquals(other.labelByteArray)) return false
        if (!inputSize.contentEquals(other.inputSize)) return false
        if (!labelSize.contentEquals(other.labelSize)) return false
        if (numExamples != other.numExamples) return false
        if (seed != other.seed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inputByteArray.contentHashCode()
        result = 31 * result + labelByteArray.contentHashCode()
        result = 31 * result + inputSize.contentHashCode()
        result = 31 * result + labelSize.contentHashCode()
        result = 31 * result + numExamples
        result = 31 * result + seed.hashCode()
        return result
    }
}

data class DataSetIteratorParameter(
    val batchSize: Int,
    val numExamples: Int,
)

data class MultiLayerNetworkParameter(
    val inputSize: IntArray,
    val outputSize: IntArray,
    val updater: Updater,
    val updateParameters: DoubleArray,
    val parameters: DoubleArray,
    val seed: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MultiLayerNetworkParameter

        if (!inputSize.contentEquals(other.inputSize)) return false
        if (!outputSize.contentEquals(other.outputSize)) return false
        if (updater != other.updater) return false
        if (!updateParameters.contentEquals(other.updateParameters)) return false
        if (!parameters.contentEquals(other.parameters)) return false
        if (seed != other.seed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inputSize.contentHashCode()
        result = 31 * result + outputSize.contentHashCode()
        result = 31 * result + updater.hashCode()
        result = 31 * result + updateParameters.contentHashCode()
        result = 31 * result + parameters.contentHashCode()
        result = 31 * result + seed.hashCode()
        return result
    }
}
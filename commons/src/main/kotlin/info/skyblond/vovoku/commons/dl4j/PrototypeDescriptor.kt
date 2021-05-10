package info.skyblond.vovoku.commons.dl4j

data class PrototypeDescriptor(
    val prototypeIdentifier: String,
    val inputSizeDim: Int,
    val inputSizeDescription: Array<String>,
    val labelSizeDim: Int,
    val labelSizeDescription: Array<String>,
    val updaters: Array<Updater>,
    val networkParameterDescription: Array<String>,
    val modelDescription: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrototypeDescriptor

        if (prototypeIdentifier != other.prototypeIdentifier) return false
        if (inputSizeDim != other.inputSizeDim) return false
        if (!inputSizeDescription.contentEquals(other.inputSizeDescription)) return false
        if (labelSizeDim != other.labelSizeDim) return false
        if (!labelSizeDescription.contentEquals(other.labelSizeDescription)) return false
        if (!updaters.contentEquals(other.updaters)) return false
        if (!networkParameterDescription.contentEquals(other.networkParameterDescription)) return false
        if (modelDescription != other.modelDescription) return false

        return true
    }

    override fun hashCode(): Int {
        var result = prototypeIdentifier.hashCode()
        result = 31 * result + inputSizeDim
        result = 31 * result + inputSizeDescription.contentHashCode()
        result = 31 * result + labelSizeDim
        result = 31 * result + labelSizeDescription.contentHashCode()
        result = 31 * result + updaters.contentHashCode()
        result = 31 * result + networkParameterDescription.contentHashCode()
        result = 31 * result + modelDescription.hashCode()
        return result
    }
}
package info.skyblond.vovoku.commons.dl4j

data class PrototypeDescriptor(
    val prototypeIdentifier: String,
    val inputSizeDim: Int,
    /**
     * 0 - No constrain on that dim,
     * 1 - Input size has to equal contrain on that dim,
     * */
    val inputSizeConstrain: IntArray,
    val labelSizeDim: Int,
    val labelSizeConstrain: IntArray,
    val updaters: Array<Updater>,
    val networkParameterDescription: List<String>,
    val modelDescription: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrototypeDescriptor

        if (prototypeIdentifier != other.prototypeIdentifier) return false
        if (inputSizeDim != other.inputSizeDim) return false
        if (!inputSizeConstrain.contentEquals(other.inputSizeConstrain)) return false
        if (labelSizeDim != other.labelSizeDim) return false
        if (!labelSizeConstrain.contentEquals(other.labelSizeConstrain)) return false
        if (!updaters.contentEquals(other.updaters)) return false
        if (networkParameterDescription != other.networkParameterDescription) return false
        if (modelDescription != other.modelDescription) return false

        return true
    }

    override fun hashCode(): Int {
        var result = prototypeIdentifier.hashCode()
        result = 31 * result + inputSizeDim
        result = 31 * result + inputSizeConstrain.contentHashCode()
        result = 31 * result + labelSizeDim
        result = 31 * result + labelSizeConstrain.contentHashCode()
        result = 31 * result + updaters.contentHashCode()
        result = 31 * result + networkParameterDescription.hashCode()
        result = 31 * result + modelDescription.hashCode()
        return result
    }
}
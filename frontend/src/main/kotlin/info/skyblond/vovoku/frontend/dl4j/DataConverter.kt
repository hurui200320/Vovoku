package info.skyblond.vovoku.frontend.dl4j

import info.skyblond.vovoku.commons.dl4j.ModelPrototype.Companion.MNIST_MLP_NAME
import org.nd4j.linalg.api.ndarray.INDArray

interface DataConverter {

    fun convert(data: ByteArray, vararg parameter: Any): INDArray

    companion object {
        val nameToConverter: Map<String, DataConverter> = mapOf(
            MNIST_MLP_NAME to MnistMlpDataConverter
        )

        fun getDataConverter(name: String): DataConverter? {
            return nameToConverter[name.toLowerCase()]
        }
    }
}
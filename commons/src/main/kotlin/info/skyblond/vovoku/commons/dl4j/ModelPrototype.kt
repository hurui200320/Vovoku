package info.skyblond.vovoku.commons.dl4j

import info.skyblond.vovoku.commons.dl4j.mlp.mnist.MnistMLPModelPrototype
import info.skyblond.vovoku.commons.dl4j.mlp.mnist2.MnistMLP2ModelPrototype
import org.deeplearning4j.nn.conf.MultiLayerConfiguration

interface ModelPrototype {
    /**
     * Getting DataSetFetcher, which translate ByteArray data into ND4J style
     * */
    val dataFetcherFactory: DataFetcherFactory

    /**
     * Getting DataSetIterator for training
     * */
    val dataSetIteratorFactory: DataSetIteratorFactory

    val descriptor: PrototypeDescriptor

    /**
     * Getting multilayer network configuration for training
     * */
    fun getMultiLayerConfiguration(parameter: MultiLayerNetworkParameter): MultiLayerConfiguration

    fun getModelInputSizeFromDataInputSize(inputSize: IntArray): IntArray
    fun getModelOutputSizeFromLabelSize(inputSize: IntArray): IntArray

    companion object {
        const val MNIST_MLP_NAME = "mnist_mlp"
        const val MNIST_MLP_2_NAME = "mnist_mlp_2"

        val nameToPrototype: Map<String, ModelPrototype> = mapOf(
            MNIST_MLP_NAME to MnistMLPModelPrototype(),
            MNIST_MLP_2_NAME to MnistMLP2ModelPrototype()
        )

        fun getPrototype(name: String): ModelPrototype? {
            return nameToPrototype[name.toLowerCase()]
        }
    }

}
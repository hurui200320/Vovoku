package info.skyblond.vovoku.commons.dl4j.mlp.mnist2

import info.skyblond.vovoku.commons.dl4j.*
import info.skyblond.vovoku.commons.dl4j.mlp.mnist.MnistMLPDataFetcher
import info.skyblond.vovoku.commons.dl4j.mlp.mnist.MnistMLPDataSetIterator
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.dataset.api.iterator.fetcher.DataSetFetcher
import org.nd4j.linalg.lossfunctions.LossFunctions

class MnistMLP2ModelPrototype : ModelPrototype {
    override val dataFetcherFactory: DataFetcherFactory = object : DataFetcherFactory {
        override fun getDataFetcher(parameter: DataFetcherParameter): DataSetFetcher {
            require(parameter.inputSize.size == 2) { "Input size should be [width, height]" }

            require(parameter.labelSize.size == 1) { "Label size should be [numLabel]" }

            return MnistMLPDataFetcher(
                parameter.inputByteArray,
                parameter.labelByteArray,
                parameter.numExamples,
                parameter.inputSize[0],
                parameter.inputSize[1],
                parameter.labelSize[0],
                parameter.seed
            )
        }

    }
    override val dataSetIteratorFactory: DataSetIteratorFactory = object : DataSetIteratorFactory {
        override fun getDataSetIterator(fetcher: DataSetFetcher, parameter: DataSetIteratorParameter): DataSetIterator {
            return MnistMLPDataSetIterator(
                parameter.batchSize,
                parameter.numExamples,
                fetcher
            )
        }
    }
    override val descriptor: PrototypeDescriptor = PrototypeDescriptor(
        prototypeIdentifier = ModelPrototype.MNIST_MLP_2_NAME,
        inputSizeDim = 2,
        inputSizeDescription = arrayOf(
            "width of input pic, default: 28",
            "height of input pic, default: 28"
        ),
        labelSizeDim = 1,
        labelSizeDescription = arrayOf(
            "total number of label, default: 10"
        ),
        updaters = arrayOf(Updater.Adam, Updater.Nesterovs),
        networkParameterDescription = arrayOf(
            "L2, default: 1e-4",
            "hidden layer 1 size, non-negative integer, default: 1000",
            "hidden layer 2 size, non-negative integer, default: 500"
        ),
        modelDescription = "带有两个隐藏层的多层感知机网络"
    )

    override fun getMultiLayerConfiguration(parameter: MultiLayerNetworkParameter): MultiLayerConfiguration {
        return NeuralNetConfiguration.Builder()
            .seed(parameter.seed)
            .updater(parseUpdater(parameter.updater, parameter.updateParameters))
            .l2(parameter.parameters[0])
            .list()
            .layer(
                DenseLayer.Builder()
                    .nIn(parameter.inputSize[0])
                    .nOut(parameter.parameters[1].toInt())
                    .activation(Activation.RELU)
                    .weightInit(WeightInit.XAVIER)
                    .build()
            )
            .layer(
                DenseLayer.Builder()
                    .nIn(parameter.parameters[1].toInt())
                    .nOut(parameter.parameters[2].toInt())
                    .activation(Activation.RELU)
                    .weightInit(WeightInit.XAVIER)
                    .build()
            )
            .layer(
                OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD) //create hidden layer
                    .nIn(parameter.parameters[2].toInt())
                    .nOut(parameter.outputSize[0])
                    .activation(Activation.SOFTMAX)
                    .weightInit(WeightInit.XAVIER)
                    .build()
            )
            .build()
    }

    override fun getModelInputSizeFromDataInputSize(inputSize: IntArray): IntArray {
        // width * height
        return intArrayOf(inputSize[0] * inputSize[1])
    }

    override fun getModelOutputSizeFromLabelSize(inputSize: IntArray): IntArray {
        return inputSize
    }
}
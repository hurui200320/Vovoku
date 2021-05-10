package info.skyblond.vovoku.commons.dl4j.mlp.mnist

import org.nd4j.common.util.MathUtils
import org.nd4j.linalg.api.buffer.DataType
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex
import java.io.ByteArrayInputStream
import java.util.*

class MnistMLPDataFetcher(
    imagesByteData: ByteArray,
    labelsByteData: ByteArray,
    val numExamples: Int,
    private val imageWidth: Int = 28,
    private val imageHeight: Int  = 28,
    labelNum: Int  = 10,
    seed: Long
) : BaseDataFetcher() {
    private var order: IntArray
    private var featureData: Array<FloatArray>? = null
    private var rng = Random(seed)
    private val imageByteArray: Array<ByteArray>
    private val labelArray: IntArray

    init {
        require(imagesByteData.size == numExamples * imageWidth * imageHeight) { "Byte data size not match examples number" }
        require(labelsByteData.size == numExamples * Integer.BYTES) { "Byte data size not match examples number" }
        require(imageWidth > 0) {"Input width must bigger than 0"}
        require(imageHeight > 0) {"Input height must bigger than 0"}
        require(labelNum > 0) {"Label size must bigger than 0"}

        numOutcomes = labelNum
        cursor = 0
        inputColumns = imageWidth * imageHeight
        totalExamples = numExamples

        order = IntArray(numExamples)
        for (i in order.indices) order[i] = i

        // init image data
        val imageByteInputStream = ByteArrayInputStream(imagesByteData)
        imageByteArray = Array(numExamples) { ByteArray(0) }
        for (i in 0 until numExamples) {
            imageByteArray[i] = ByteArray(imageWidth * imageHeight)
            imageByteInputStream.read(imageByteArray[i])
        }

        // init label data
        val labelByteInputStream = ByteArrayInputStream(labelsByteData)
        labelArray = IntArray(numExamples) { 0 }
        for (i in 0 until numExamples) {
            labelArray[i] = labelByteInputStream.read()
            require(labelArray[i] >= 0) { "Unexpected EOF" }
        }

        reset() //Shuffle order
    }

    override fun fetch(numExamples: Int) {
        check(hasMore()) { "Unable to get more; there are no more images" }

        var labels = Nd4j.zeros(DataType.FLOAT, numExamples.toLong(), numOutcomes.toLong())

        if (featureData == null || featureData!!.size < numExamples) {
            featureData = Array(numExamples) { FloatArray(imageWidth * imageHeight) }
        }

        var actualExamples = 0
        for (i in 0 until numExamples) {
            if (!hasMore()) break
            val img: ByteArray = imageByteArray[order[cursor]]
            val label: Int = labelArray[order[cursor]]

            labels.put(actualExamples, label, 1.0f)
            for (j in img.indices) {
                featureData!![actualExamples][j] = (img[j].toInt() and 0xFF).toFloat()
            }
            actualExamples++
            cursor++
        }

        val features: INDArray = if (featureData!!.size == actualExamples) {
            Nd4j.create(featureData)
        } else {
            Nd4j.create(Arrays.copyOfRange(featureData, 0, actualExamples))
        }

        if (actualExamples < numExamples) {
            labels = labels[NDArrayIndex.interval(0, actualExamples), NDArrayIndex.all()]
        }
        // normalization
        features.divi(255.0)

        curr = DataSet(features, labels)
    }

    override fun reset() {
        cursor = 0
        curr = null
        MathUtils.shuffleArray(order, rng)
    }
}
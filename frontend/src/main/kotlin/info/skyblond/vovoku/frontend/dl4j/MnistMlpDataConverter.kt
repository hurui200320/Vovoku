package info.skyblond.vovoku.frontend.dl4j

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

object MnistMlpDataConverter : DataConverter {
    /**
     * Return [sample, data]
     * */
    override fun convert(data: ByteArray, vararg parameter: Any): INDArray {
        val size = parameter[0] as Pair<*, *>
        val width = size.first as Int
        val height = size.second as Int
        val channelSize = parameter[1] as Int
        require(channelSize == 1) { "Unsupported pic channel count: $channelSize" }
        val input = Nd4j.zeros(1, width * height * channelSize)

        for (i in 0 until width * height) {
            for (x in 0 until width) {
                input.putScalar(intArrayOf(0, i), (data[i].toInt() and 0xFF).toFloat())
            }
        }

        input.divi(255.0)

        return input
    }
}
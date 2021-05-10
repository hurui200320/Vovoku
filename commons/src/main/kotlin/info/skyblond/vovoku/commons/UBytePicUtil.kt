package info.skyblond.vovoku.commons

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.image.BufferedImage


object UBytePicUtil {
    private val logger: Logger = LoggerFactory.getLogger(UBytePicUtil::class.java)

    fun picToUByteArray(image: BufferedImage): Triple<Pair<Int, Int>, Int, ByteArray> {
        val raster = image.raster
        val sampleModel = raster.sampleModel
        val size = Pair(sampleModel.width, sampleModel.height)
        val channel = sampleModel.numBands

        // data[x, y, channel]
        val data = ByteArray(size.first * size.second * channel)

        for (x in 0 until size.first) {
            val lineOffset = x * size.second * channel
            for (y in 0 until size.second) {
                val pixelData: IntArray = raster.getPixel(x, y, null as IntArray?)
                for (i in 0 until channel) {
                    data[lineOffset + y * channel + i] = (pixelData[i] and 0xFF).toByte()
                }
            }
        }
        return Triple(size, channel, data)
    }

    fun uByteArrayToPic(width: Int, height: Int, channel: Int, data: ByteArray): BufferedImage {
        val type = when (channel) {
            1 -> BufferedImage.TYPE_BYTE_GRAY
            3 -> BufferedImage.TYPE_3BYTE_BGR
            4 -> BufferedImage.TYPE_4BYTE_ABGR
            else -> throw IllegalArgumentException("Unsupported channel size")
        }
        val bufferedImage = BufferedImage(width, height, type)

        val raster = bufferedImage.raster
        for (x in 0 until width) {
            val lineOffset = x * height * channel
            for (y in 0 until height) {
                val pixelData = IntArray(channel) { i ->
                    data[lineOffset + y * channel + i].toInt()
                }
                raster.setPixel(x, y, pixelData)
            }
        }

        return bufferedImage
    }

    fun picToGrayScale(image: BufferedImage): BufferedImage {
        val bufferedImage = BufferedImage(
            image.width, image.height,
            BufferedImage.TYPE_BYTE_GRAY
        )

        val newImageRaster = bufferedImage.raster

        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val color = Color(image.getRGB(x, y))
                val bright = color.red * 0.299 + color.green * 0.587 + color.blue * 0.114
                newImageRaster.setPixel(
                    x,
                    y,
                    doubleArrayOf(bright)
                )
            }
        }

        return bufferedImage
    }
}
package info.skyblond.vovoku.commons

import org.junit.jupiter.api.Test
import java.io.File
import java.io.RandomAccessFile
import javax.imageio.ImageIO

class UByteToPic {
    @Test
    fun run() {
        // *.gz is not supported
        val imageDataFile =
            RandomAccessFile(File("C:\\Users\\天空Blond\\Downloads\\Compressed\\train-images.idx3-ubyte"), "r")
        val labelDataFile =
            RandomAccessFile(File("C:\\Users\\天空Blond\\Downloads\\Compressed\\train-labels.idx1-ubyte"), "r")
        val targetDir = File("D:\\Vovoku dataset\\train\\")

//        val imageDataFile = RandomAccessFile(File("C:\\Users\\天空Blond\\Downloads\\Compressed\\t10k-images.idx3-ubyte"), "r")
//        val labelDataFile = RandomAccessFile(File("C:\\Users\\天空Blond\\Downloads\\Compressed\\t10k-labels.idx1-ubyte"), "r")
//        val targetDir = File("D:\\Vovoku dataset\\test\\")

        imageDataFile.seek(0)
        require(imageDataFile.readByte() == 0x00.toByte())
        require(imageDataFile.readByte() == 0x00.toByte())
        require(imageDataFile.readByte() == 0x08.toByte())
        require(imageDataFile.readByte() == 0x03.toByte())

        labelDataFile.seek(0)
        require(labelDataFile.readByte() == 0x00.toByte())
        require(labelDataFile.readByte() == 0x00.toByte())
        require(labelDataFile.readByte() == 0x08.toByte())
        require(labelDataFile.readByte() == 0x01.toByte())

        val imageSampleCount = imageDataFile.readInt()
        println("Image sample count: $imageSampleCount")
        val imageNumRow = imageDataFile.readInt()
        println("Image row count: $imageNumRow")
        val imageNumColumn = imageDataFile.readInt()
        println("Image column count: $imageNumColumn")

        val labelSampleCount = labelDataFile.readInt()
        println("Label sample count: $labelSampleCount")

        require(labelSampleCount == imageSampleCount)
        val counter = mutableMapOf<Int, Int>()
        (0..9).forEach { counter[it] = 1 }
        for (i in 0 until imageSampleCount) {
            val imageData = ByteArray(imageNumRow * imageNumColumn)
            imageDataFile.read(imageData)
            // not those data are organized row-wise
            // thus need a flip
            val trueData = ByteArray(imageNumRow * imageNumColumn)
            for (x in 0 until imageNumColumn) {
                for (y in 0 until imageNumRow) {
                    trueData[y * imageNumColumn + x] = imageData[x * imageNumRow + y]
                }
            }
            val image = UBytePicUtil.uByteArrayToPic(28, 28, 1, trueData)

            val labelData = labelDataFile.readUnsignedByte()
            val dir = File(targetDir, labelData.toString())
            dir.mkdirs()
            ImageIO.write(image, "png", File(dir, CryptoUtil.md5(imageData) + ".png"))
            counter[labelData] = counter[labelData]!! + 1
        }

        println(counter)
    }
}
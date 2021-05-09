package info.skyblond.vovoku.worker

import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

class UByteTest {
    @Test
    fun convertMnistToByte() {
        val baseFile = File("C:\\Users\\hurui\\.deeplearning4j\\data\\MNIST")
//      train file
//        val imageDataFile = RandomAccessFile(File(baseFile, "train-images-idx3-ubyte"), "r")
//        val imageDataOutputStream = FileOutputStream(File("./train_image_byte.bin"))
//        val labelDataFile = RandomAccessFile(File(baseFile, "train-labels-idx1-ubyte"), "r")
//        val labelDataOutputStream = FileOutputStream(File("./train_label_byte.bin"))

        // test file
        val imageDataFile = RandomAccessFile(File(baseFile, "t10k-images-idx3-ubyte"), "r")
        val imageDataOutputStream = FileOutputStream(File("./test_image_byte.bin"))
        val labelDataFile = RandomAccessFile(File(baseFile, "t10k-labels-idx1-ubyte"), "r")
        val labelDataOutputStream = FileOutputStream(File("./test_label_byte.bin"))

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

        for (i in 0 until imageSampleCount) {
            val imageData = ByteArray(imageNumRow * imageNumColumn)
            imageDataFile.read(imageData)
            imageDataOutputStream.write(imageData)

            val labelData = ByteArray(Integer.BYTES)
            labelDataFile.read(labelData)
            labelDataOutputStream.write(labelData)
        }

        println("Done!")
        imageDataOutputStream.close()
        imageDataFile.close()
        labelDataOutputStream.close()
        labelDataFile.close()

    }
}
package info.skyblond.vovoku.commons

import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import javax.imageio.ImageIO

internal class UBytePicUtilTest {

    @Test
    fun testFunction() {
        val target = File("C:\\Users\\hurui\\Desktop\\imag0173.jpg")
        println(target.canonicalFile)
        val image = ImageIO.read(target)
        val result = UBytePicUtil.picToUByteArray(image)
        val resultGray = UBytePicUtil.picToUByteArray(UBytePicUtil.picToGrayScale(image))

        val restoredImage = UBytePicUtil.uByteArrayToPic(
            result.first.first,
            result.first.second,
            result.second,
            result.third
        )
        println("Size: ${result.first}, channel: ${result.second}")
        println(Arrays.toString(ImageIO.getWriterFormatNames()))
        ImageIO.write(restoredImage, "png", File("../output.png"))
        // it's better to use png, since channel > 3 is not supported by bmp and jpg

        val restoredGrayImage = UBytePicUtil.uByteArrayToPic(
            resultGray.first.first,
            resultGray.first.second,
            resultGray.second,
            resultGray.third
        )
        println("Size: ${resultGray.first}, channel: ${resultGray.second}")

        ImageIO.write(
            restoredGrayImage,
            "png", File("../output_gray.png")
        )
    }

}
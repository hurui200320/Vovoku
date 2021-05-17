package info.skyblond.vovoku.frontend;

import info.skyblond.vovoku.commons.UBytePicUtil;
import info.skyblond.vovoku.commons.dl4j.PrototypeDescriptor;
import info.skyblond.vovoku.frontend.dl4j.DataConverter;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Dl4jHelper {
    public static Integer infer(
            BufferedImage image,
            PrototypeDescriptor prototypeDescriptor,
            File modelFile
    ) {
        var converter = DataConverter.Companion.getDataConverter(prototypeDescriptor.getPrototypeIdentifier());
        if (converter == null) {
            throw new RuntimeException("Unsupported model");
        }
        MultiLayerNetwork model = null;
        try {
            model = MultiLayerNetwork.load(modelFile, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var data = UBytePicUtil.INSTANCE.picToUByteArray(image);
        var input = converter.convert(data.getThird(), data.getFirst(), data.getSecond());

        var output = model.output(input);

        return output.argMax(1).getInt(0);
    }

}

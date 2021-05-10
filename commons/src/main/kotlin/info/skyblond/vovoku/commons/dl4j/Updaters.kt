package info.skyblond.vovoku.commons.dl4j

import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.learning.config.IUpdater
import org.nd4j.linalg.learning.config.Nesterovs

enum class Updater(
    val description: List<String>
) {
    Adam(
        listOf(
            "learning rate, default: 1e-3",
            "beta1, default: 0.9",
            "beta2, default: 0.999",
            "epsilon, default: 1e-8"
        )
    ),

    /**
     * Parameter:
     *  learningRate = 0.1,
     *  momentum = 0.9
     * */
    Nesterovs(
        listOf(
            "learning rate, default: 0.1",
            "momentum, default: 0.9"
        )
    )
}


fun parseUpdater(updateType: Updater, parameters: DoubleArray): IUpdater {
    return when (updateType) {
        Updater.Adam -> Adam(
            parameters[0], parameters[1],
            parameters[2], parameters[3]
        )
        Updater.Nesterovs -> Nesterovs(
            parameters[0], parameters[1]
        )
    }
}

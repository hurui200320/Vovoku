package info.skyblond.vovoku.worker

import info.skyblond.vovoku.worker.datavec.CustomDataFetcher
import info.skyblond.vovoku.worker.datavec.CustomDataSetIterator
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.evaluation.classification.Evaluation
import org.nd4j.evaluation.classification.ROCMultiClass
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.learning.config.Nesterovs
import org.slf4j.LoggerFactory
import java.io.File


fun main() {
    val logger = LoggerFactory.getLogger("Worker")
    // 初始化
    // 初始化DL4J运行时？
    // 订阅Redis消息

    // 循环工作
    // 等待并取得一个Task
    // 竞争TaskId锁
    // 成功后注册Worker
    // 获取训练资源
    // 初始化模型
    // 开始训练
    // 完成后打包模型
    // 上传模型文件

    val numRows = 28
    val numColumns = 28
    val outputNum = 10 // number of output classes

    val batchSize = 128 // batch size for each epoch
    val rngSeed = 123L // random number seed for reproducibility
    val numEpochs = 15 // number of epochs to perform

    val trainCount = 60000
    val testCount = 10000

    //Get the DataSetIterators:
    val trainFetcher = CustomDataFetcher(
        File("./train_image_byte.bin").readBytes(),
        File("./train_label_byte.bin").readBytes(),
        trainCount, rngSeed
    )
    val customTrain: DataSetIterator = CustomDataSetIterator(batchSize, trainFetcher)
    val testFetcher = CustomDataFetcher(
        File("./test_image_byte.bin").readBytes(),
        File("./test_label_byte.bin").readBytes(),
        testCount, rngSeed
    )
    val customTest: DataSetIterator = CustomDataSetIterator(batchSize, testFetcher)

    logger.info("Build model....")
    val conf = getNeuralNetworkConfig(
        rngSeed, Nesterovs(0.006, 0.9),
        1e-4, 1000, numColumns * numRows, outputNum
    )

    val model = MultiLayerNetwork(conf)
    model.init()
    //print the score with every 1 iteration
    model.setListeners(ScoreIterationListener(1))

    logger.info("Train model....")
    model.fit(customTrain, numEpochs)

    logger.info("Evaluate model....")
    val eval = model.evaluate<Evaluation>(customTest)
    println(eval.accuracy())
    println(eval.precision())
    println(eval.recall())

// evaluate ROC and calculate the Area Under Curve
    val roc = model.evaluateROCMultiClass<ROCMultiClass>(customTest, 0)
    roc.calculateAUC(0)

// optionally, you can print all stats from the evaluations
    println(eval.stats())
    println(roc.stats())

}
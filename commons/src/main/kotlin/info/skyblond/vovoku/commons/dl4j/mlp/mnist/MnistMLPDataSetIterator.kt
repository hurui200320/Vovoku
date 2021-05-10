package info.skyblond.vovoku.commons.dl4j.mlp.mnist

import org.nd4j.linalg.dataset.api.iterator.BaseDatasetIterator
import org.nd4j.linalg.dataset.api.iterator.fetcher.DataSetFetcher


class MnistMLPDataSetIterator(
    batchSize: Int,
    numExamples: Int,
    dataFetcher: DataSetFetcher
) : BaseDatasetIterator(
    batchSize, numExamples, dataFetcher
)
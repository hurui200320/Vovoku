package info.skyblond.vovoku.worker.datavec

import org.nd4j.linalg.dataset.api.iterator.BaseDatasetIterator


class CustomDataSetIterator(
    batchSize: Int,
    dataFetcher: CustomDataFetcher
): BaseDatasetIterator(
    batchSize, dataFetcher.numExamples, dataFetcher
)
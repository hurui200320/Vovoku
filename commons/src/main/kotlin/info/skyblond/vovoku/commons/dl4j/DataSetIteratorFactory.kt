package info.skyblond.vovoku.commons.dl4j

import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.dataset.api.iterator.fetcher.DataSetFetcher

interface DataSetIteratorFactory {
    /**
     * Return a [DataSetFetcher] by given data fetcher and parameter
     * */
    fun getDataSetIterator(fetcher: DataSetFetcher, parameter: DataSetIteratorParameter): DataSetIterator
}
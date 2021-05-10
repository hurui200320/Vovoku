package info.skyblond.vovoku.commons.dl4j

import org.nd4j.linalg.dataset.api.iterator.fetcher.DataSetFetcher

interface DataFetcherFactory {
    /**
     * Return a [DataSetFetcher] by given parameter
     * */
    fun getDataFetcher(parameter: DataFetcherParameter): DataSetFetcher
}
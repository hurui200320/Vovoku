package info.skyblond.vovoku.frontend.api

import info.skyblond.vovoku.commons.FilePathUtil
import info.skyblond.vovoku.commons.UBytePicUtil
import info.skyblond.vovoku.commons.dl4j.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import java.awt.image.BufferedImage
import java.io.File


val jsonMediaType: MediaType = "application/json; charset=utf-8".toMediaType()

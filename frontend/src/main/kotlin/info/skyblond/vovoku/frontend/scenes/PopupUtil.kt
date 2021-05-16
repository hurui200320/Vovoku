package info.skyblond.vovoku.frontend.scenes

import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.layout.Region
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object PopupUtil : AutoCloseable {
    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    fun showError(headerText: String?, contentText: String){
        val error = Alert(Alert.AlertType.ERROR)
        error.headerText = headerText
        error.contentText = contentText
        error.dialogPane.minHeight = Region.USE_PREF_SIZE
        error.show()
    }

    fun <T> doWithProcessingPopupWithoutCancel(
        block: () -> T,
        recallOnSucceeded: (T) -> Unit,
        recallOnFailed: (Throwable?) -> Unit
    ) {
        // prepare processing window
        val processingPopup = Alert(Alert.AlertType.INFORMATION)
        processingPopup.buttonTypes.clear()
        processingPopup.buttonTypes.add(ButtonType.CANCEL)
        processingPopup.title = ""
        processingPopup.headerText = null
        processingPopup.contentText = "Processing... Please wait..."
        processingPopup.dialogPane.minHeight = Region.USE_PREF_SIZE
        processingPopup.show()

        // prepare task
        val task = object : Task<T>() {
            override fun call(): T {
                // wait processing window show up
                // shouldn't take so long
                while (!processingPopup.isShowing) {
                    TimeUnit.MICROSECONDS.sleep(10)
                }
                return block()
            }
        }
        // set up recall
        task.onSucceeded = EventHandler {
            processingPopup.close()
            recallOnSucceeded(task.value)
        }
        task.onFailed = EventHandler {
            processingPopup.close()
            recallOnFailed(it.source.exception)
        }
        // get task running
        executor.submit(task)
    }

    fun <T> doWithProcessingPopup(
        block: () -> T,
        recallOnSucceeded: (T) -> Unit,
        recallOnFailed: (Throwable?) -> Unit,
        recallOnCancelled: () -> Unit,
    ) {
        // prepare processing window
        val processingPopup = Alert(Alert.AlertType.INFORMATION)
        processingPopup.buttonTypes.clear()
        processingPopup.buttonTypes.add(ButtonType.CANCEL)
        processingPopup.title = ""
        processingPopup.headerText = null
        processingPopup.contentText = "Processing... Please wait..."
        processingPopup.dialogPane.minHeight = Region.USE_PREF_SIZE

        // prepare task
        val task = object : Task<T>() {
            override fun call(): T {
                // wait processing window show up
                // shouldn't take so long
                while (!processingPopup.isShowing) {
                    TimeUnit.MICROSECONDS.sleep(10)
                }
                return block()
            }
        }
        // set up recall
        task.onSucceeded = EventHandler {
            processingPopup.close()
            recallOnSucceeded(task.value)
        }
        task.onFailed = EventHandler {
            processingPopup.close()
            recallOnFailed(it.source.exception)
        }
        task.onCancelled = EventHandler {
            processingPopup.close()
            recallOnCancelled()
        }
        // get task running
        executor.submit(task)
        val optional = processingPopup.showAndWait()
        if (optional.isPresent && optional.get() == ButtonType.CANCEL) {
            task.cancel(true)
        }
    }

    override fun close() {
        executor.shutdownNow()
    }

}
package info.skyblond.vovoku.frontend.scenes

import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


object PopupUtil : AutoCloseable {
    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    @JvmOverloads
    fun showError(headerText: String?, contentText: String, wait: Boolean = false) {
        val error = Alert(Alert.AlertType.ERROR)
        error.title = "Error!"
        error.headerText = headerText
        error.contentText = contentText
        error.dialogPane.minHeight = Region.USE_PREF_SIZE
        if (wait)
            error.showAndWait()
        else
            error.show()
    }

    fun multiLineTextAreaPopup(
        title: String,
        headerText: String?,
        contentText: String,
        multiLineContent: String
    ) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = title
        alert.headerText = headerText
        alert.contentText = contentText

        val expContent = GridPane()
        expContent.maxWidth = Double.MAX_VALUE

        val textArea = TextArea()
        textArea.isEditable = false
        textArea.maxWidth = Double.MAX_VALUE
        textArea.maxHeight = Double.MAX_VALUE
        GridPane.setHgrow(textArea, Priority.ALWAYS)
        textArea.text = multiLineContent
        GridPane.setVgrow(textArea, Priority.ALWAYS)
        expContent.add(textArea, 0, 0)
        alert.dialogPane.expandableContent = expContent
        alert.dialogPane.expandedProperty().set(true)
        alert.show()
    }

    fun multiLineInputPopup(
        title: String,
        contentText: String,
        hint: String,
        content: String
    ): String? {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = title
        alert.headerText = null
        alert.contentText = contentText
        alert.buttonTypes.clear()
        alert.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

        val label = Label(hint)
        val textArea = TextArea(content)
        textArea.isEditable = true
        textArea.isWrapText = true

        textArea.maxWidth = Double.MAX_VALUE
        textArea.maxHeight = Double.MAX_VALUE
        GridPane.setVgrow(textArea, Priority.ALWAYS)
        GridPane.setHgrow(textArea, Priority.ALWAYS)

        val expContent = GridPane()
        expContent.maxWidth = Double.MAX_VALUE
        expContent.add(label, 0, 0)
        expContent.add(textArea, 0, 1)

        alert.dialogPane.expandableContent = expContent
        alert.dialogPane.expandedProperty().set(true)

        alert.showAndWait().let {
            return if (it.isPresent)
                if (it.get() == ButtonType.CANCEL) {
                    null
                } else {
                    textArea.text
                }
            else
                null
        }
    }

    fun yesOrNoPopup(
        title: String,
        headerText: String?,
        contentText: String
    ): Boolean {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = title
        alert.headerText = headerText
        alert.contentText = contentText
        alert.dialogPane.minHeight = Region.USE_PREF_SIZE

        return alert.showAndWait().get() != ButtonType.CANCEL
    }

    fun infoPopup(
        title: String,
        headerText: String?,
        contentText: String
    ) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = title
        alert.headerText = headerText
        alert.contentText = contentText
        alert.dialogPane.minHeight = Region.USE_PREF_SIZE

        alert.show()
    }

    @JvmOverloads
    fun textInputPopup(
        title: String,
        headerText: String?,
        contentText: String,
        defaultValue: String? = null
    ): String {
        val dialog = TextInputDialog()

        dialog.title = title
        dialog.headerText = headerText
        dialog.contentText = contentText
        dialog.dialogPane.minHeight = Region.USE_PREF_SIZE

        if (defaultValue != null){
            dialog.editor.text = defaultValue
        }

        val result = AtomicReference("")
        dialog.showAndWait().ifPresent { newValue: String ->
            result.set(newValue)
        }
        return result.get()
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
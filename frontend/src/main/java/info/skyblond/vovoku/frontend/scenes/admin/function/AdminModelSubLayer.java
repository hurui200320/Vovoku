package info.skyblond.vovoku.frontend.scenes.admin.function;

import info.skyblond.vovoku.commons.JacksonJsonUtil;
import info.skyblond.vovoku.commons.models.DatabaseModelInfoPojo;
import info.skyblond.vovoku.commons.models.ModelTrainingStatus;
import info.skyblond.vovoku.commons.models.Page;
import info.skyblond.vovoku.frontend.Dl4jHelper;
import info.skyblond.vovoku.frontend.api.admin.AdminApiClient;
import info.skyblond.vovoku.frontend.scenes.PopupUtil;
import info.skyblond.vovoku.frontend.scenes.TableViewTemplate;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import kotlin.Pair;
import kotlin.Triple;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;

public class AdminModelSubLayer extends TableViewTemplate<DatabaseModelInfoPojo> {
    private final AdminApiClient adminApiClient;

    private final Button inferButton = new Button("Infer");

    public AdminModelSubLayer(
            AdminApiClient adminApiClient
    ) {
        this.adminApiClient = adminApiClient;
    }

    private String lastStatusFilter = null;
    private Integer userIdFilter = null;

    @Override
    public Node getTopRightRoot() {
        this.labelPrefix = "Model";
        return super.getTopRightRoot();
    }

    @Override
    public Button[] getRightButtons() {
        this.uploadButton.setText("New model");
        this.uploadButton.setDisable(true);
        this.inferButton.setDisable(true);
        this.inferButton.setOnAction(e -> this.infer());
        var superButtons = super.getRightButtons();

        return new Button[]{
                superButtons[0], superButtons[1],
                superButtons[2], superButtons[3],
                this.inferButton, superButtons[4]
        };
    }

    @Override
    protected void updateFilter() {
        var current = this.configGenMap();
        current.put("lastStatusFilter", this.lastStatusFilter);
        current.put("userIdFilter", this.userIdFilter);

        try {
            var newFilter = this.configAskUser(current);
            if (newFilter != null) {
                this.lastStatusFilter = (String) newFilter.get("lastStatusFilter");
                this.userIdFilter = (Integer) newFilter.get("userIdFilter");
                this.loadData();
            }
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Invalid filter: " + e.getLocalizedMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public TableView<DatabaseModelInfoPojo> getCenterNode() {
        this.tableView = new TableView<>();
        this.tableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldSelection, newSelection) -> {
                    this.deleteButton.setDisable(newSelection == null);
                    this.detailButton.setDisable(newSelection == null);
                    this.inferButton.setDisable(newSelection == null);
                }
        );

        TableColumn<DatabaseModelInfoPojo, Integer> modelIdColumn = new TableColumn<>("Id");
        modelIdColumn.setCellValueFactory(
                param -> new SimpleIntegerProperty(param.getValue().getModelId()).asObject()
        );
        modelIdColumn.setSortable(false);

        TableColumn<DatabaseModelInfoPojo, Integer> userIdColumn = new TableColumn<>("User id");
        userIdColumn.setCellValueFactory(
                param -> new SimpleIntegerProperty(param.getValue().getUserId()).asObject()
        );
        userIdColumn.setSortable(false);

        TableColumn<DatabaseModelInfoPojo, String> prototypeIdColumn = new TableColumn<>("Prototype");
        prototypeIdColumn.setCellValueFactory(
                param -> new SimpleStringProperty(param.getValue().getCreateInfo().getPrototypeDescriptionSnapshot().getPrototypeIdentifier())
        );
        prototypeIdColumn.setSortable(false);

        TableColumn<DatabaseModelInfoPojo, Timestamp> createTimeColumn = new TableColumn<>("create time");
        createTimeColumn.setCellValueFactory(
                param -> new SimpleObjectProperty<>(param.getValue().getCreateInfo().getCreateTime())
        );
        createTimeColumn.setSortable(false);

        TableColumn<DatabaseModelInfoPojo, String> statusColumn = new TableColumn<>("status");
        statusColumn.setCellValueFactory(
                param -> new SimpleStringProperty(param.getValue().getLastStatus())
        );
        statusColumn.setSortable(false);


        this.tableView.getColumns().addAll(
                modelIdColumn, userIdColumn, prototypeIdColumn, createTimeColumn, statusColumn
        );

        // initial load
        this.loadData();
        return this.tableView;
    }

    @Override
    protected Triple<Boolean, String, DatabaseModelInfoPojo[]> getOne() {
        var result = this.adminApiClient.queryModel(this.idFilter, null, null, new Page(null, null));
        if (result.length == 0) {
            return new Triple<>(false, "Model id not found", result);
        } else {
            return new Triple<>(true, "OK", result);
        }
    }

    @Override
    protected Pair<Boolean, String> deleteOne() {
        var id = this.tableView.getSelectionModel().getSelectedItem().getModelId();
        var result = this.adminApiClient.deleteModel(id);
        if (result) {
            return new Pair<>(true, "OK");
        } else {
            return new Pair<>(false, "Cannot delete model");
        }
    }

    @Override
    protected void viewOne() {
        var selectedId = this.tableView.getSelectionModel().getSelectedItem().getModelId();

        PopupUtil.INSTANCE.doWithProcessingPopup(
                () -> this.adminApiClient.queryModel(selectedId, null, null, new Page(null, null)),
                result -> {
                    if (result.length != 1) {
                        PopupUtil.INSTANCE.showError(null, "Cannot fetch model");
                        return null;
                    }

                    var model = result[0];

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Model id: ")
                            .append(model.getModelId())
                            .append("Created by user id ")
                            .append(model.getUserId())
                            .append("\n---------- Create info ---------- \n")
                            .append(JacksonJsonUtil.INSTANCE.objectToJson(model.getCreateInfo(), true))
                            .append("\n---------- Create info end ----------")
                            .append("\nCurrent status: ")
                            .append(model.getLastStatus())
                            .append("\nStatus history: \n");


                    for (int i = 0; i < model.getTrainingInfo().length; i++) {
                        var info = model.getTrainingInfo()[i];
                        stringBuilder.append("\t ").append(info.getSecond()).append(": ")
                                .append(info.getFirst()).append("(").append(info.getThird()).append(")").append("\n");
                    }

                    PopupUtil.INSTANCE.multiLineTextAreaPopup(
                            "View Model", null, "Model details:",
                            stringBuilder.toString()
                    );
                    return null;
                },
                e -> {
                    PopupUtil.INSTANCE.showError(null, "Error when fetching model: " + e.getLocalizedMessage());
                    return null;
                },
                () -> null
        );

    }

    @Override
    protected void updateOne() {
    }

    @Override
    protected void uploadOne() {
    }

    /**
     * Inferred by pic id
     */
    private void infer() {
        var model = this.tableView.getSelectionModel().getSelectedItem();

        if (!model.getLastStatus().equals(ModelTrainingStatus.FINISHED.name())) {
            PopupUtil.INSTANCE.showError(null, "Only finished model can be used for infer");
            return;
        }

        BufferedImage bufferedImage;
        try {
            var picId = Integer.parseInt(PopupUtil.INSTANCE.textInputPopup(
                    "Infer",
                    "Which pic do you want to infer?",
                    "pic id"
            ));
            bufferedImage = this.adminApiClient.fetchPic(picId);
            if (bufferedImage == null) {
                throw new Exception("Cannot fetch pic");
            }
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Invalid picId: " + e.getLocalizedMessage());
            return;
        }

        File modelFile;
        try {
            modelFile = File.createTempFile("model_", ".zip");
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Error when fetching model: " + e.getLocalizedMessage());
            return;
        }
        modelFile.deleteOnExit();
        PopupUtil.INSTANCE.doWithProcessingPopup(
                () -> this.adminApiClient.fetchModel(model.getModelId()),
                result -> {
                    if (result == null) {
                        PopupUtil.INSTANCE.showError(null, "Cannot fetching model");
                        return null;
                    }
                    // write model to temp file
                    try {
                        var stream = new FileOutputStream(modelFile);
                        stream.write(result);
                        stream.close();
                    } catch (Exception e) {
                        PopupUtil.INSTANCE.showError(null, "Error when fetching model: " + e.getLocalizedMessage());
                        return null;
                    }
                    // start infer
                    PopupUtil.INSTANCE.doWithProcessingPopupWithoutCancel(
                            () -> Dl4jHelper.infer(bufferedImage, model.getCreateInfo().getPrototypeDescriptionSnapshot(), modelFile),
                            inferResult -> {
                                var alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Infer");
                                alert.setHeaderText(null);
                                alert.setContentText("Infer result is " + inferResult);

                                var expContent = new GridPane();
                                expContent.setMaxWidth(Double.MAX_VALUE);

                                var imageView = new ImageView();
                                var image = SwingFXUtils.toFXImage(bufferedImage, null);
                                imageView.setSmooth(true);
                                imageView.setImage(image);
                                imageView.setPreserveRatio(true);
                                imageView.setFitHeight(200);
                                GridPane.setVgrow(imageView, Priority.ALWAYS);
                                GridPane.setHgrow(imageView, Priority.ALWAYS);
                                expContent.add(imageView, 0, 0);
                                alert.getDialogPane().setExpandableContent(expContent);
                                alert.getDialogPane().expandedProperty().set(true);
                                alert.show();

                                return null;
                            },
                            e -> {
                                PopupUtil.INSTANCE.showError(null, "Error when doing infer: " + e.getLocalizedMessage());
                                return null;
                            }
                    );

                    return null;
                },
                e -> {
                    PopupUtil.INSTANCE.showError(null, "Error when fetching model: " + e.getLocalizedMessage());
                    return null;
                },
                () -> null
        );

    }

    @Override
    protected Triple<Boolean, String, DatabaseModelInfoPojo[]> getList() {
        var result = this.adminApiClient.queryModel(null, this.userIdFilter, this.lastStatusFilter, new Page(this.page, this.size));
        return new Triple<>(true, "OK", result);
    }
}

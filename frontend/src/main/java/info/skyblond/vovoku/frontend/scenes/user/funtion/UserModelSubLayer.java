package info.skyblond.vovoku.frontend.scenes.user.funtion;

import info.skyblond.vovoku.commons.JacksonJsonUtil;
import info.skyblond.vovoku.commons.dl4j.PrototypeDescriptor;
import info.skyblond.vovoku.commons.dl4j.Updater;
import info.skyblond.vovoku.commons.models.DatabaseModelInfoPojo;
import info.skyblond.vovoku.commons.models.ModelCreateRequest;
import info.skyblond.vovoku.commons.models.ModelTrainingParameter;
import info.skyblond.vovoku.commons.models.ModelTrainingStatus;
import info.skyblond.vovoku.frontend.Dl4jHelper;
import info.skyblond.vovoku.frontend.api.user.UserModelApiClient;
import info.skyblond.vovoku.frontend.api.user.UserPictureApiClient;
import info.skyblond.vovoku.frontend.api.user.UserPrototypeApiClient;
import info.skyblond.vovoku.frontend.scenes.PopupUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import kotlin.Pair;
import kotlin.Triple;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;

public class UserModelSubLayer extends TableViewTemplate<DatabaseModelInfoPojo> {
    private final UserModelApiClient modelApiClient;
    private final UserPictureApiClient pictureApiClient;
    private final UserPrototypeApiClient prototypeApiClient;

    private final Button inferButton = new Button("Infer");

    public UserModelSubLayer(
            UserModelApiClient modelApiClient,
            UserPictureApiClient pictureApiClient,
            UserPrototypeApiClient prototypeApiClient
    ) {
        this.modelApiClient = modelApiClient;
        this.pictureApiClient = pictureApiClient;
        this.prototypeApiClient = prototypeApiClient;
    }

    private String lastStatus = null;

    @Override
    public Node getTopRightRoot() {
        this.labelPrefix = "Model";
        return super.getTopRightRoot();
    }

    @Override
    public Button[] getRightButtons() {
        this.uploadButton.setText("New model");
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
        current.put("lastStatus", this.lastStatus);

        try {
            var newFilter = this.configAskUser(current);
            if (newFilter != null) {
                this.lastStatus = (String) newFilter.get("lastStatus");
                System.out.println(this.lastStatus);
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
                modelIdColumn, prototypeIdColumn, createTimeColumn, statusColumn
        );

        // initial load
        this.loadData();
        return this.tableView;
    }

    @Override
    protected Triple<Boolean, String, DatabaseModelInfoPojo[]> getOne() {
        return this.modelApiClient.getOneModel(this.idFilter);
    }

    @Override
    protected Pair<Boolean, String> deleteOne() {
        var id = this.tableView.getSelectionModel().getSelectedItem().getModelId();
        return this.modelApiClient.deleteOneModel(id);
    }

    @Override
    protected void viewOne() {
        var selectedId = this.tableView.getSelectionModel().getSelectedItem().getModelId();

        PopupUtil.INSTANCE.doWithProcessingPopup(
                () -> this.modelApiClient.getOneModel(selectedId),
                result -> {
                    if (!result.getFirst()) {
                        PopupUtil.INSTANCE.showError(null, "Error when fetching model: " + result.getSecond());
                        return null;
                    }
                    var alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("View Model");
                    alert.setHeaderText(null);
                    alert.setContentText("Model details:");

                    var expContent = new GridPane();
                    expContent.setMaxWidth(Double.MAX_VALUE);

                    var textArea = new TextArea();
                    textArea.setEditable(false);
                    var model = result.getThird()[0];

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Model id: ")
                            .append(model.getModelId())
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

                    textArea.setMaxWidth(Double.MAX_VALUE);
                    textArea.setMaxHeight(Double.MAX_VALUE);
                    GridPane.setHgrow(textArea, Priority.ALWAYS);
                    textArea.setText(stringBuilder.toString());
                    GridPane.setVgrow(textArea, Priority.ALWAYS);
                    expContent.add(textArea, 0, 0);
                    alert.getDialogPane().setExpandableContent(expContent);
                    alert.getDialogPane().expandedProperty().set(true);
                    alert.show();
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
        PrototypeDescriptor prototype;
        try {
            var re = PopupUtil.INSTANCE.textInputPopup(
                    "Create Model",
                    null, "Prototype identifier"
            );
            var protoRe = this.prototypeApiClient.getOnePrototype(re);
            if (!protoRe.getFirst()) {
                throw new Exception(protoRe.getSecond());
            }
            prototype = protoRe.getThird();
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Invalid prototype identifier: " + e.getLocalizedMessage());
            return;
        }

        int batchSize;
        try {
            batchSize = Integer.parseInt(PopupUtil.INSTANCE.textInputPopup(
                    "Create Model",
                    null, "batchSize"
            ));
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Invalid batchSize: " + e.getLocalizedMessage());
            return;
        }

        int epochs;
        try {
            epochs = Integer.parseInt(PopupUtil.INSTANCE.textInputPopup(
                    "Create Model",
                    null, "epochs"
            ));
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Invalid epochs: " + e.getLocalizedMessage());
            return;
        }

        int[] inputSize = new int[prototype.getInputSizeDim()];
        for (int i = 0; i < inputSize.length; i++) {
            try {
                inputSize[i] = Integer.parseInt(PopupUtil.INSTANCE.textInputPopup(
                        "Create Model",
                        prototype.getInputSizeDescription()[i],
                        "input dim " + (i + 1) + " size"
                ));
            } catch (Exception e) {
                PopupUtil.INSTANCE.showError(null, "Invalid input size: " + e.getLocalizedMessage());
                return;
            }
        }
        int[] labelSize = new int[prototype.getLabelSizeDim()];
        for (int i = 0; i < labelSize.length; i++) {
            try {
                labelSize[i] = Integer.parseInt(PopupUtil.INSTANCE.textInputPopup(
                        "Create Model",
                        prototype.getLabelSizeDescription()[i],
                        "label dim " + (i + 1) + " size"
                ));
            } catch (Exception e) {
                PopupUtil.INSTANCE.showError(null, "Invalid label size: " + e.getLocalizedMessage());
                return;
            }
        }
        Updater updater;
        try {
            updater = Updater.valueOf(PopupUtil.INSTANCE.textInputPopup(
                    "Create Model",
                    "Available updaters: " + Arrays.toString(prototype.getUpdaters()),
                    "updater name"
            ));
            Objects.requireNonNull(updater);
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Invalid updater: " + e.getLocalizedMessage());
            return;
        }

        double[] updaterParameters = new double[updater.getDescription().size()];
        for (int i = 0; i < updaterParameters.length; i++) {
            try {
                updaterParameters[i] = Double.parseDouble(PopupUtil.INSTANCE.textInputPopup(
                        "Create Model",
                        updater.getDescription().get(i),
                        "updater param " + (i + 1) + " value"
                ));
            } catch (Exception e) {
                PopupUtil.INSTANCE.showError(null, "Invalid updater param: " + e.getLocalizedMessage());
                return;
            }
        }
        double[] networkParameter = new double[prototype.getNetworkParameterDescription().length];
        for (int i = 0; i < networkParameter.length; i++) {
            try {
                networkParameter[i] = Double.parseDouble(PopupUtil.INSTANCE.textInputPopup(
                        "Create Model",
                        prototype.getNetworkParameterDescription()[i],
                        "network param " + (i + 1) + " value"
                ));
            } catch (Exception e) {
                PopupUtil.INSTANCE.showError(null, "Invalid network param: " + e.getLocalizedMessage());
                return;
            }
        }
        long seed;
        try {
            seed = Long.parseLong(PopupUtil.INSTANCE.textInputPopup(
                    "Create Model",
                    null, "seed"
            ));
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Invalid seed: " + e.getLocalizedMessage());
            return;
        }

        var trainingParameter = new ModelTrainingParameter(
                prototype.getPrototypeIdentifier(),
                batchSize, epochs,
                inputSize, labelSize,
                updater, updaterParameters, networkParameter,
                seed
        );

        String datasetName = PopupUtil.INSTANCE.textInputPopup(
                "Create Model",
                null, "dataset name"
        );

        if (datasetName.isBlank()) {
            PopupUtil.INSTANCE.showError(null, "Invalid dataset name: empty");
            return;
        }

        PopupUtil.INSTANCE.doWithProcessingPopupWithoutCancel(
                () -> this.modelApiClient.trainingNewModel(new ModelCreateRequest(
                        trainingParameter,
                        datasetName
                )),
                result -> {
                    if (result.getFirst()) {
                        this.loadData();
                    } else {
                        PopupUtil.INSTANCE.showError(null, "Error when creating model: " + result.getSecond());
                    }
                    return null;
                },
                e -> {
                    PopupUtil.INSTANCE.showError(null, "Error when creating model: " + e.getLocalizedMessage());
                    return null;
                }
        );


    }

    /**
     * Infered by pic id
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
            var result = this.pictureApiClient.getOnePic(picId);
            if (!result.getFirst()) {
                throw new Exception(result.getSecond());
            }
            var picResult = this.pictureApiClient.fetchPic(result.getThird()[0].getFilePath());
            if (!picResult.getFirst()) {
                throw new Exception(result.getSecond());
            }
            bufferedImage = picResult.getThird();
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
                () -> this.modelApiClient.fetchModel(Objects.requireNonNull(model.getFilePath())),
                result -> {
                    if (!result.getFirst()) {
                        PopupUtil.INSTANCE.showError(null, "Error when fetching model: " + result.getSecond());
                        return null;
                    }
                    // write model to temp file
                    try {
                        var stream = new FileOutputStream(modelFile);
                        stream.write(result.getThird());
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
        return this.modelApiClient.listModel(this.lastStatus, this.page, this.size);
    }
}

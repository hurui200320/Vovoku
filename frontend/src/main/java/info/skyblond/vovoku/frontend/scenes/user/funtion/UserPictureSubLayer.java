package info.skyblond.vovoku.frontend.scenes.user.funtion;

import info.skyblond.vovoku.commons.models.DatabasePictureTagPojo;
import info.skyblond.vovoku.frontend.api.user.UserPictureApiClient;
import info.skyblond.vovoku.frontend.scenes.PopupUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import kotlin.Pair;
import kotlin.Triple;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

public class UserPictureSubLayer extends TableViewTemplate<DatabasePictureTagPojo> {
    private final UserPictureApiClient pictureApiClient;
    private final Stage stage;

    public UserPictureSubLayer(
            UserPictureApiClient pictureApiClient, Stage stage) {
        this.pictureApiClient = pictureApiClient;
        this.stage = stage;
    }

    private Boolean usedForTrainFilter = null;
    private String folderNameFilter = null;

    @Override
    public Node getTopRightRoot() {
        this.labelPrefix = "Pictures";
        return super.getTopRightRoot();
    }

    @Override
    protected void updateFilter() {
        var current = this.configGenMap();
        current.put("usedForTrainFilter", this.usedForTrainFilter);
        current.put("datasetFilter", this.folderNameFilter);

        try {
            var newFilter = this.configAskUser(current);
            if (newFilter != null) {
                this.usedForTrainFilter = (Boolean) newFilter.get("usedForTrainFilter");
                this.folderNameFilter = (String) newFilter.get("datasetFilter");
                this.loadData();
            }
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Invalid filter: " + e.getLocalizedMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public TableView<DatabasePictureTagPojo> getCenterNode() {
        this.tableView = new TableView<>();
        this.tableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldSelection, newSelection) -> {
                    this.updateButton.setDisable(newSelection == null);
                    this.deleteButton.setDisable(newSelection == null);
                    this.detailButton.setDisable(newSelection == null);
                }
        );
        TableColumn<DatabasePictureTagPojo, Integer> tagIdColumn = new TableColumn<>("Id");
        tagIdColumn.setCellValueFactory(
                param -> new SimpleIntegerProperty(param.getValue().getTagId()).asObject()
        );
        tagIdColumn.setSortable(false);
        TableColumn<DatabasePictureTagPojo, ?> sizeColumn = new TableColumn<>("size");
        TableColumn<DatabasePictureTagPojo, Integer> widthColumn = new TableColumn<>("width");
        widthColumn.setCellValueFactory(
                param -> new SimpleIntegerProperty(param.getValue().getTagData().getWidth()).asObject()
        );
        widthColumn.setSortable(false);
        TableColumn<DatabasePictureTagPojo, Integer> heightColumn = new TableColumn<>("height");
        heightColumn.setCellValueFactory(
                param -> new SimpleIntegerProperty(param.getValue().getTagData().getHeight()).asObject()
        );
        heightColumn.setSortable(false);
        TableColumn<DatabasePictureTagPojo, Integer> channelColumn = new TableColumn<>("channel");
        channelColumn.setCellValueFactory(
                param -> new SimpleIntegerProperty(param.getValue().getTagData().getChannelCount()).asObject()
        );
        channelColumn.setSortable(false);
        sizeColumn.getColumns().addAll(widthColumn, heightColumn, channelColumn);
        TableColumn<DatabasePictureTagPojo, Integer> tagColumn = new TableColumn<>("tag");
        tagColumn.setCellValueFactory(
                param -> new SimpleIntegerProperty(param.getValue().getTagData().getTag()).asObject()
        );
        tagColumn.setSortable(false);
        TableColumn<DatabasePictureTagPojo, String> datasetNameColumn = new TableColumn<>("dataset");
        datasetNameColumn.setCellValueFactory(
                param -> new SimpleStringProperty(param.getValue().getFolderName())
        );
        datasetNameColumn.setSortable(false);
        TableColumn<DatabasePictureTagPojo, Boolean> usageColumn = new TableColumn<>("for train");
        usageColumn.setCellValueFactory(
                param -> new SimpleBooleanProperty(param.getValue().getUsedForTrain())

        );
        usageColumn.setSortable(false);
        this.tableView.getColumns().addAll(
                tagIdColumn, sizeColumn, tagColumn, datasetNameColumn, usageColumn
        );

        // initial load
        this.loadData();
        return this.tableView;
    }

    @Override
    protected Triple<Boolean, String, DatabasePictureTagPojo[]> getOne() {
        return this.pictureApiClient.getOnePic(this.idFilter);
    }

    @Override
    protected Pair<Boolean, String> deleteOne() {
        var id = this.tableView.getSelectionModel().getSelectedItem().getTagId();
        return this.pictureApiClient.deleteOnePic(id);
    }

    @Override
    protected void viewOne() {
        var pic = this.tableView.getSelectionModel().getSelectedItem();

        PopupUtil.INSTANCE.doWithProcessingPopup(
                () -> this.pictureApiClient.fetchPic(pic.getFilePath()),
                imageResult -> {
                    if (!imageResult.getFirst()) {
                        PopupUtil.INSTANCE.showError(null, "Error when fetching pic: " + imageResult.getSecond());
                        return null;
                    }
                    var alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("View Picture");
                    alert.setHeaderText(null);
                    alert.setContentText("Picture " + pic.getTagId());

                    var expContent = new GridPane();
                    expContent.setMaxWidth(Double.MAX_VALUE);

                    var imageView = new ImageView();
                    var image = SwingFXUtils.toFXImage(imageResult.getThird(), null);
                    imageView.setImage(image);
                    imageView.setSmooth(true);
                    imageView.setPreserveRatio(true);
                    imageView.setFitHeight(400);
                    GridPane.setVgrow(imageView, Priority.ALWAYS);
                    GridPane.setHgrow(imageView, Priority.ALWAYS);

                    expContent.add(imageView, 0, 0);

                    alert.getDialogPane().setExpandableContent(expContent);
                    alert.getDialogPane().expandedProperty().set(true);
                    alert.show();
                    return null;
                },
                e -> {
                    PopupUtil.INSTANCE.showError(null, "Error when fetching pic: " + e.getLocalizedMessage());
                    return null;
                },
                () -> null
        );

    }

    @Override
    protected void updateOne() {
        var pic = this.tableView.getSelectionModel().getSelectedItem();

        Integer newTag;
        Boolean usedForTrain;
        String datasetName;

        try {
            newTag = Integer.parseInt(PopupUtil.INSTANCE.textInputPopup(
                    "Update pic",
                    null, "new tag"
            ));
            Objects.requireNonNull(newTag);
        } catch (Exception e) {
            newTag = null;
            PopupUtil.INSTANCE.showError(null, "UsedForTraining flag will not update");
        }

        try {
            datasetName = PopupUtil.INSTANCE.textInputPopup(
                    "Update pic",
                    null, "dataset name"
            );
            if (Objects.requireNonNull(datasetName).isBlank()) {
                datasetName = null;
            }
            Objects.requireNonNull(datasetName);
        } catch (Exception e) {
            datasetName = null;
            PopupUtil.INSTANCE.showError(null, "DatasetName will not update");
        }

        try {
            var re = PopupUtil.INSTANCE.textInputPopup(
                    "Update pic",
                    null, "for train? (true or false)"
            );
            if (re.isBlank()) {
                throw new Exception();
            }
            usedForTrain = Boolean.parseBoolean(re);
            Objects.requireNonNull(usedForTrain);
        } catch (Exception e) {
            usedForTrain = null;
            PopupUtil.INSTANCE.showError(null, "Invalid tag, tag will not update");
        }

        Integer finalNewTag = newTag;
        Boolean finalUsedForTrain = usedForTrain;
        String finalDatasetName = datasetName;
        PopupUtil.INSTANCE.doWithProcessingPopupWithoutCancel(
                () -> this.pictureApiClient.updateOnePicTag(pic.getTagId(), finalNewTag, finalUsedForTrain, finalDatasetName),
                result -> {
                    if (result.getFirst()) {
                        this.loadData();
                    } else {
                        PopupUtil.INSTANCE.showError(null, "Error when updating pic: " + result.getSecond());
                    }
                    return null;
                },
                e -> {
                    PopupUtil.INSTANCE.showError(null, "Error when updating pic: " + e.getLocalizedMessage());
                    return null;
                }
        );

    }

    @Override
    protected void uploadOne() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(this.stage);
        if (selectedFile == null) {
            return;
        }
        BufferedImage image;
        try {
            image = ImageIO.read(selectedFile);
            Objects.requireNonNull(image);
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Invalid Image: " + e.getLocalizedMessage());
            return;
        }

        // TODO strip gray
        // TODO cut image

        int tag;
        try {
            tag = Integer.parseInt(PopupUtil.INSTANCE.textInputPopup(
                    "Upload image", "What number in this pic?", "Number"
            ));
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Invalid tag: " + e.getLocalizedMessage());
            return;
        }

        var folderName = PopupUtil.INSTANCE.textInputPopup(
                "Upload image", "What dataset this pic in?", "dataset"
        );

        boolean forTrain;
        try {
            forTrain = Boolean.parseBoolean(PopupUtil.INSTANCE.textInputPopup(
                    "Upload image", "Is this pic used for training?", "true?"
            ));
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Invalid boolean: " + e.getLocalizedMessage());
            return;
        }
        this.pictureApiClient.uploadPic(image, tag, forTrain, folderName);
        this.loadData();
    }

    @Override
    protected Triple<Boolean, String, DatabasePictureTagPojo[]> getList() {
        return this.pictureApiClient.listPic(this.usedForTrainFilter, this.folderNameFilter, this.page, this.size);
    }
}

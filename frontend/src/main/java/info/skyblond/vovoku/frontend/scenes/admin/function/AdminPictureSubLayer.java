package info.skyblond.vovoku.frontend.scenes.admin.function;

import info.skyblond.vovoku.commons.models.DatabasePictureTagPojo;
import info.skyblond.vovoku.commons.models.Page;
import info.skyblond.vovoku.frontend.api.admin.AdminApiClient;
import info.skyblond.vovoku.frontend.scenes.PopupUtil;
import info.skyblond.vovoku.frontend.scenes.TableViewTemplate;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.stage.FileChooser;
import kotlin.Pair;
import kotlin.Triple;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

public class AdminPictureSubLayer extends TableViewTemplate<DatabasePictureTagPojo> {
    private final AdminApiClient adminApiClient;

    public AdminPictureSubLayer(AdminApiClient adminApiClient) {
        this.adminApiClient = adminApiClient;
    }

    private Boolean usedForTrainFilter = null;
    private String folderNameFilter = null;
    private Integer userIdFilter = null;

    @Override
    public Node getTopRightRoot() {
        this.labelPrefix = "Pictures";
        return super.getTopRightRoot();
    }

    @Override
    public Button[] getRightButtons(){
        this.uploadButton.setDisable(true);
        return super.getRightButtons();
    }

    @Override
    protected void updateFilter() {
        var current = this.configGenMap();
        current.put("usedForTrainFilter", this.usedForTrainFilter);
        current.put("datasetFilter", this.folderNameFilter);
        current.put("userIdFilter", this.userIdFilter);

        try {
            var newFilter = this.configAskUser(current);
            if (newFilter != null) {
                this.usedForTrainFilter = (Boolean) newFilter.get("usedForTrainFilter");
                this.folderNameFilter = (String) newFilter.get("datasetFilter");
                this.userIdFilter = (Integer) newFilter.get("userIdFilter");
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
                    this.deleteButton.setDisable(newSelection == null);
                    this.detailButton.setDisable(newSelection == null);
                }
        );
        TableColumn<DatabasePictureTagPojo, Integer> tagIdColumn = new TableColumn<>("Id");
        tagIdColumn.setCellValueFactory(
                param -> new SimpleIntegerProperty(param.getValue().getTagId()).asObject()
        );
        tagIdColumn.setSortable(false);
        TableColumn<DatabasePictureTagPojo, Integer> userIdColumn = new TableColumn<>("User Id");
        userIdColumn.setCellValueFactory(
                param -> new SimpleIntegerProperty(param.getValue().getUserId()).asObject()
        );
        userIdColumn.setSortable(false);
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
                tagIdColumn, userIdColumn, sizeColumn, tagColumn, datasetNameColumn, usageColumn
        );

        // initial load
        this.loadData();
        return this.tableView;
    }

    @Override
    protected Triple<Boolean, String, DatabasePictureTagPojo[]> getOne() {
        var result = this.adminApiClient.queryPicture(this.idFilter, null, null, null, new Page(null, null));
        if (result.length == 0) {
            return new Triple<>(false, "Pic id not found", result);
        } else {
            return new Triple<>(true, "OK", result);
        }
    }

    @Override
    protected Pair<Boolean, String> deleteOne() {
        var id = this.tableView.getSelectionModel().getSelectedItem().getTagId();
        var result = this.adminApiClient.deletePicture(id);
        if (result) {
            return new Pair<>(true, "OK");
        } else {
            return new Pair<>(false, "Cannot delete pic");
        }
    }

    @Override
    protected void viewOne() {
        var pic = this.tableView.getSelectionModel().getSelectedItem();

        PopupUtil.INSTANCE.doWithProcessingPopup(
                () -> this.adminApiClient.fetchPic(pic.getTagId()),
                imageResult -> {
                    if (imageResult == null) {
                        PopupUtil.INSTANCE.showError(null, "Cannot fetching pic");
                        return null;
                    }
                    var alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("View Picture");
                    alert.setHeaderText(null);
                    alert.setContentText("Picture " + pic.getTagId());

                    var expContent = new GridPane();
                    expContent.setMaxWidth(Double.MAX_VALUE);

                    var imageView = new ImageView();
                    var image = SwingFXUtils.toFXImage(imageResult, null);
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
    }

    @Override
    protected void uploadOne() {
    }

    @Override
    protected Triple<Boolean, String, DatabasePictureTagPojo[]> getList() {
        var result = this.adminApiClient.queryPicture(null, userIdFilter, usedForTrainFilter, folderNameFilter, new Page(this.page, this.size));
        return new Triple<>(true, "OK", result);
    }
}

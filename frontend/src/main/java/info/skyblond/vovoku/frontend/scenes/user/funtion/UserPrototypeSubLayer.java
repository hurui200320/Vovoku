package info.skyblond.vovoku.frontend.scenes.user.funtion;

import info.skyblond.vovoku.commons.dl4j.PrototypeDescriptor;
import info.skyblond.vovoku.frontend.api.user.UserPrototypeApiClient;
import info.skyblond.vovoku.frontend.scenes.PopupUtil;
import info.skyblond.vovoku.frontend.scenes.TableViewTemplate;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import kotlin.Pair;
import kotlin.Triple;

public class UserPrototypeSubLayer extends TableViewTemplate<PrototypeDescriptor> {
    private final UserPrototypeApiClient prototypeApiClient;

    public UserPrototypeSubLayer(
            UserPrototypeApiClient prototypeApiClient) {
        this.prototypeApiClient = prototypeApiClient;
    }

    @Override
    public Node getTopRightRoot() {
        this.labelPrefix = "Prototype";
        return super.getTopRightRoot();
    }

    @Override
    public Button[] getRightButtons() {
        this.uploadButton.setDisable(true);
        this.filterButton.setDisable(true);
        return super.getRightButtons();
    }

    @Override
    protected void updateFilter() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public TableView<PrototypeDescriptor> getCenterNode() {
        this.tableView = new TableView<>();
        this.tableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldSelection, newSelection) -> this.detailButton.setDisable(newSelection == null)
        );

        TableColumn<PrototypeDescriptor, String> prototypeIdentifierColumn = new TableColumn<>("Identifier");
        prototypeIdentifierColumn.setCellValueFactory(
                param -> new SimpleStringProperty(param.getValue().getPrototypeIdentifier())
        );
        prototypeIdentifierColumn.setSortable(false);

        TableColumn<PrototypeDescriptor, Integer> inputSizeDimColumn = new TableColumn<>("Input dim");
        inputSizeDimColumn.setCellValueFactory(
                param -> new SimpleIntegerProperty(param.getValue().getInputSizeDim()).asObject()
        );
        inputSizeDimColumn.setSortable(false);
        TableColumn<PrototypeDescriptor, Integer> labelSizeDimColumn = new TableColumn<>("Label dim");
        labelSizeDimColumn.setCellValueFactory(
                param -> new SimpleIntegerProperty(param.getValue().getLabelSizeDim()).asObject()
        );
        labelSizeDimColumn.setSortable(false);
        TableColumn<PrototypeDescriptor, Integer> networkParameterCountColumn = new TableColumn<>("Parameter count");
        networkParameterCountColumn.setCellValueFactory(
                param -> new SimpleIntegerProperty(param.getValue().getNetworkParameterDescription().length).asObject()
        );
        networkParameterCountColumn.setSortable(false);

        TableColumn<PrototypeDescriptor, Integer> updaterCountColumn = new TableColumn<>("Updater count");
        updaterCountColumn.setCellValueFactory(
                param -> new SimpleIntegerProperty(param.getValue().getUpdaters().length).asObject()
        );
        updaterCountColumn.setSortable(false);
        TableColumn<PrototypeDescriptor, String> modelDescriptionColumn = new TableColumn<>("Description");
        modelDescriptionColumn.setCellValueFactory(
                param -> new SimpleStringProperty(param.getValue().getModelDescription())
        );
        modelDescriptionColumn.setSortable(false);


        this.tableView.getColumns().addAll(
                prototypeIdentifierColumn, inputSizeDimColumn, labelSizeDimColumn,
                networkParameterCountColumn, updaterCountColumn, modelDescriptionColumn
        );

        // initial load
        this.loadData();
        return this.tableView;
    }

    @Override
    protected Triple<Boolean, String, PrototypeDescriptor[]> getOne() {
        return this.prototypeApiClient.listPrototype(this.page, this.size);
    }

    @Override
    protected Pair<Boolean, String> deleteOne() {
        return new Pair<>(false, "You cannot delete a prototype");
    }

    @Override
    protected void viewOne() {
        var prototype = this.tableView.getSelectionModel().getSelectedItem();

        PopupUtil.INSTANCE.doWithProcessingPopup(
                () -> this.prototypeApiClient.getOnePrototype(prototype.getPrototypeIdentifier()),
                result -> {
                    if (!result.getFirst()) {
                        PopupUtil.INSTANCE.showError(null, "Error when fetching prototype: " + result.getSecond());
                        return null;
                    }

                    var proto = result.getThird();

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Prototype identifier: ")
                            .append(proto.getPrototypeIdentifier())
                            .append("\n")
                            .append("Input dim details: \n");

                    for (int i = 1; i <= proto.getInputSizeDim(); i++) {
                        stringBuilder.append("\tDim ").append(i).append(": ")
                                .append(proto.getInputSizeDescription()[i - 1]).append("\n");
                    }

                    stringBuilder.append("Label dim details: \n");
                    for (int i = 1; i <= proto.getLabelSizeDim(); i++) {
                        stringBuilder.append("\tDim ").append(i).append(": ")
                                .append(proto.getLabelSizeDescription()[i - 1]).append("\n");
                    }

                    stringBuilder.append("Available updaters: \n");
                    for (int i = 0; i < proto.getUpdaters().length; i++) {
                        stringBuilder.append("\t")
                                .append(proto.getUpdaters()[i]).append("\n");
                        var updater = proto.getUpdaters()[i];
                        for (int j = 0; j < updater.getDescription().size(); j++) {
                            stringBuilder.append("\t\t")
                                    .append("Param ").append(j + 1).append(": ")
                                    .append(updater.getDescription().get(j))
                                    .append("\n");
                        }
                    }

                    stringBuilder.append("Network parameters details: \n");
                    for (int i = 1; i <= proto.getNetworkParameterDescription().length; i++) {
                        stringBuilder.append("\tParam ").append(i).append(": ")
                                .append(proto.getNetworkParameterDescription()[i - 1]).append("\n");
                    }

                    PopupUtil.INSTANCE.multiLineTextAreaPopup(
                            "View Prototype", null, "Prototype details:",
                            stringBuilder.toString()
                    );
                    return null;
                },
                e -> {
                    PopupUtil.INSTANCE.showError(null, "Error when fetching prototype: " + e.getLocalizedMessage());
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
    protected Triple<Boolean, String, PrototypeDescriptor[]> getList() {
        return this.prototypeApiClient.listPrototype(this.page, this.size);
    }
}

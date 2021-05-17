package info.skyblond.vovoku.frontend.scenes.user.funtion;

import com.fasterxml.jackson.core.JsonProcessingException;
import info.skyblond.vovoku.commons.JacksonJsonUtil;
import info.skyblond.vovoku.frontend.scenes.PopupUtil;
import info.skyblond.vovoku.frontend.scenes.SubLayout;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import kotlin.Pair;
import kotlin.Triple;

import java.util.HashMap;

public abstract class TableViewTemplate<T> implements SubLayout {
    protected String labelPrefix;

    protected Label infoLabel;
    protected TableView<T> tableView;
    protected Button prevPageButton = new Button("Prev");
    protected Button nextPageButton = new Button("Next");

    protected Button detailButton = new Button("Detail");
    protected Button uploadButton = new Button("Upload");
    protected Button updateButton = new Button("Update");
    protected Button deleteButton = new Button("Delete");
    protected Button filterButton = new Button("Filter");

    protected int page = 1;
    protected int size = 20;
    protected Integer idFilter = null;

    @Override
    public Node getTopRightRoot() {
        this.infoLabel = new Label();
        this.infoLabel.setAlignment(Pos.TOP_RIGHT);
        this.updatePageLabel();
        this.infoLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return this.infoLabel;
    }


    @Override
    public Button[] getRightButtons() {
        this.detailButton.setDisable(true);
        this.detailButton.setOnAction(e -> this.viewOne());

        this.uploadButton.setOnAction(e -> this.uploadOne());
        this.uploadButton.setOnAction(e -> this.uploadOne());

        this.updateButton.setDisable(true);
        this.updateButton.setOnAction(e -> this.updateOne());

        this.deleteButton.setDisable(true);
        this.deleteButton.setOnAction(e ->
                PopupUtil.INSTANCE.doWithProcessingPopupWithoutCancel(
                        this::deleteOne,
                        result -> {
                            if (result.getFirst()) {
                                this.loadData();
                                this.updatePageLabel();
                            } else {
                                PopupUtil.INSTANCE.showError(null, "Error when doing delete:\n" + result.getSecond());
                            }
                            return null;
                        },
                        ex -> {
                            PopupUtil.INSTANCE.showError(null, "Error when doing delete: " + ex.getLocalizedMessage());
                            return null;
                        }
                ));

        this.filterButton.setOnAction(e -> this.updateFilter());
        return new Button[]{
                this.detailButton, this.uploadButton,
                this.updateButton, this.deleteButton,
                this.filterButton
        };
    }

    protected abstract void updateFilter();

    @Override
    public Button[] getBottomRightButtons() {
        this.prevPageButton.setOnAction(e -> {
            if (this.idFilter == null) {
                this.page--;
            } else {
                this.idFilter--;
            }
            this.loadData();
        });
        Button refreshPageButton = new Button("Refresh");
        refreshPageButton.setOnAction(e -> this.loadData());

        this.nextPageButton.setOnAction(e -> {
            if (this.idFilter == null) {
                this.page++;
            } else {
                this.idFilter++;
            }
            this.loadData();
        });

        return new Button[]{
                this.prevPageButton, refreshPageButton, this.nextPageButton
        };
    }

    protected HashMap<String, Object> configGenMap() {
        var current = new HashMap<String, Object>();
        current.put("size", this.size);
        current.put("idFilter", this.idFilter);
        return current;
    }

    protected HashMap<String, Object> configAskUser(HashMap<String, Object> current) throws JsonProcessingException {
        var json = PopupUtil.INSTANCE.multiLineInputPopup(
                "Change filter settings",
                "Change filter setting",
                "JSON:",
                JacksonJsonUtil.INSTANCE.objectToJson(current, true)
        );

        if (json == null) {
            return null;
        }

        //noinspection unchecked
        var result = (HashMap<String, Object>) JacksonJsonUtil.INSTANCE.getJsonMapper().readValue(json, HashMap.class);

        this.size = (Integer) result.get("size");
        this.idFilter = (Integer) result.get("idFilter");
        this.page = 1;
        return result;
    }

    private void updatePageLabel() {
        this.infoLabel.setText(this.labelPrefix + "\n" +
                "Page " + this.page + "\n" +
                this.size + " per page");
        this.prevPageButton.setDisable(this.page == 1);
        this.nextPageButton.setDisable(this.tableView.getItems().size() == 0);
    }

    protected abstract Triple<Boolean, String, T[]> getOne();

    protected abstract Pair<Boolean, String> deleteOne();

    protected abstract void viewOne();

    protected abstract void updateOne();

    protected abstract void uploadOne();

    protected abstract Triple<Boolean, String, T[]> getList();

    protected void loadData() {
        PopupUtil.INSTANCE.doWithProcessingPopupWithoutCancel(
                () -> {
                    if (this.idFilter == null) {
                        return this.getList();
                    } else {
                        this.page = this.idFilter;
                        return this.getOne();
                    }
                },
                result -> {
                    if (result.getFirst()) {
                        this.tableView.setItems(FXCollections.observableArrayList(result.getThird()));
                    } else {
                        PopupUtil.INSTANCE.showError(null, "Error when doing list:\n" + result.getSecond());
                        this.tableView.getItems().clear();
                    }
                    this.updatePageLabel();
                    return null;
                },
                e -> {
                    PopupUtil.INSTANCE.showError(null, "Error when doing list: " + e.getLocalizedMessage());
                    this.tableView.getItems().clear();
                    this.updatePageLabel();
                    return null;
                }
        );
    }
}

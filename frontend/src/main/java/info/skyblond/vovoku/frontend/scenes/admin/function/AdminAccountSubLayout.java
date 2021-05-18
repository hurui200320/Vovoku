package info.skyblond.vovoku.frontend.scenes.admin.function;

import info.skyblond.vovoku.commons.CryptoUtil;
import info.skyblond.vovoku.commons.models.DatabaseUserPojo;
import info.skyblond.vovoku.commons.models.Page;
import info.skyblond.vovoku.frontend.api.admin.AdminApiClient;
import info.skyblond.vovoku.frontend.scenes.PopupUtil;
import info.skyblond.vovoku.frontend.scenes.TableViewTemplate;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import kotlin.Pair;
import kotlin.Triple;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

public class AdminAccountSubLayout extends TableViewTemplate<DatabaseUserPojo> {
    private final AdminApiClient adminApiClient;

    public AdminAccountSubLayout(AdminApiClient adminApiClient) {
        this.adminApiClient = adminApiClient;
    }

    private String usernameFilter = null;

    @Override
    public Node getTopRightRoot() {
        this.labelPrefix = "Accounts";
        return super.getTopRightRoot();
    }

    @Override
    protected void updateFilter() {
        var current = this.configGenMap();
        current.put("usernameFilter", this.usernameFilter);

        try {
            var newFilter = this.configAskUser(current);
            if (newFilter != null) {
                this.usernameFilter = (String) newFilter.get("usernameFilter");
                this.loadData();
            }
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Invalid filter: " + e.getLocalizedMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public TableView<DatabaseUserPojo> getCenterNode() {
        this.tableView = new TableView<>();
        this.tableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldSelection, newSelection) -> {
                    this.updateButton.setDisable(newSelection == null);
                    this.deleteButton.setDisable(newSelection == null);
                    this.detailButton.setDisable(newSelection == null);
                }
        );

        TableColumn<DatabaseUserPojo, Integer> userIdColumn = new TableColumn<>("Id");
        userIdColumn.setCellValueFactory(
                param -> new SimpleIntegerProperty(param.getValue().getUserId()).asObject()
        );
        userIdColumn.setSortable(false);
        TableColumn<DatabaseUserPojo, String> usernameColumn = new TableColumn<>("username");
        usernameColumn.setCellValueFactory(
                param -> new SimpleStringProperty(param.getValue().getUsername())
        );
        usernameColumn.setSortable(false);
        TableColumn<DatabaseUserPojo, String> passwordColumn = new TableColumn<>("Password MD5");
        passwordColumn.setCellValueFactory(
                param -> new SimpleStringProperty(param.getValue().getPassword())
        );
        passwordColumn.setSortable(false);

        this.tableView.getColumns().addAll(
                userIdColumn, usernameColumn, passwordColumn
        );

        // initial load
        this.loadData();
        return this.tableView;
    }


    @Override
    protected Triple<Boolean, String, DatabaseUserPojo[]> getOne() {
        var result = this.adminApiClient.queryUser(this.idFilter, null, new Page(null, null));
        if (result.length == 0) {
            return new Triple<>(false, "User id not found", result);
        } else {
            return new Triple<>(true, "OK", result);
        }
    }

    @Override
    protected Pair<Boolean, String> deleteOne() {
        var id = this.tableView.getSelectionModel().getSelectedItem().getUserId();
        var result = this.adminApiClient.deleteUser(id);
        if (result) {
            return new Pair<>(true, "OK");
        } else {
            return new Pair<>(false, "Cannot delete user");
        }
    }


    @Override
    protected void viewOne() {
        var user = this.tableView.getSelectionModel().getSelectedItem();

        PopupUtil.INSTANCE.multiLineTextAreaPopup(
                "View user", null, "User details",
                "UserId: " + user.getUserId() + "\n" +
                        "Username: " + user.getUsername() + "\n" +
                        "Password MD5: " + user.getPassword()
        );
    }

    @Override
    protected void updateOne() {
        var user = this.tableView.getSelectionModel().getSelectedItem();

        String username;
        String password;

        try {
            username = PopupUtil.INSTANCE.textInputPopup(
                    "Update user",
                    null, "new username"
            );
            if (username.isBlank())
                username = null;
            Objects.requireNonNull(username);
        } catch (Exception e) {
            username = null;
            PopupUtil.INSTANCE.showError(null, "Username will not update", true);
        }

        try {
            var raw = PopupUtil.INSTANCE.textInputPopup(
                    "Update user",
                    null, "new password"
            );
            if (Objects.requireNonNull(raw).isBlank()) {
                raw = null;
            }
            password = CryptoUtil.INSTANCE.md5(Objects.requireNonNull(raw));
        } catch (Exception e) {
            password = null;
            PopupUtil.INSTANCE.showError(null, "Password will not update", true);
        }


        String finalUsername = username;
        String finalPassword = password;
        PopupUtil.INSTANCE.doWithProcessingPopupWithoutCancel(
                () -> this.adminApiClient.updateUser(user.getUserId(), finalUsername, finalPassword),
                result -> {
                    if (result) {
                        this.loadData();
                    } else {
                        PopupUtil.INSTANCE.showError(null, "Cannot update user");
                    }
                    return null;
                },
                e -> {
                    PopupUtil.INSTANCE.showError(null, "Cannot update user: " + e.getLocalizedMessage());
                    return null;
                }
        );

    }

    @Override
    protected void uploadOne() {
        String username;
        String password;

        try {
            username = PopupUtil.INSTANCE.textInputPopup(
                    "Create user",
                    null, "username"
            );
            Objects.requireNonNull(username);
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Username is required");
            return;
        }

        try {
            var raw = PopupUtil.INSTANCE.textInputPopup(
                    "Create user",
                    null, "password"
            );
            if (Objects.requireNonNull(raw).isBlank()) {
                raw = null;
            }
            password = CryptoUtil.INSTANCE.md5(Objects.requireNonNull(raw));
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(null, "Password is required");
            return;
        }

        PopupUtil.INSTANCE.doWithProcessingPopupWithoutCancel(
                () -> this.adminApiClient.addUser(username, password),
                result -> {
                    if (result) {
                        this.loadData();
                    } else {
                        PopupUtil.INSTANCE.showError(null, "Cannot create user");
                    }
                    return null;
                },
                e -> {
                    PopupUtil.INSTANCE.showError(null, "Cannot create user: " + e.getLocalizedMessage());
                    return null;
                }
        );
    }

    @Override
    protected Triple<Boolean, String, DatabaseUserPojo[]> getList() {
        var result = this.adminApiClient.queryUser(null, usernameFilter, new Page(page, size));
        return new Triple<>(true, "OK", result);
    }


    @Override
    public Button[] getRightButtons() {
        this.uploadButton.setText("Create");
        return super.getRightButtons();
    }
}

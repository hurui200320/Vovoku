package info.skyblond.vovoku.frontend.scenes.user.funtion;

import info.skyblond.vovoku.frontend.api.user.UserAccountApiClient;
import info.skyblond.vovoku.frontend.scenes.PopupUtil;
import info.skyblond.vovoku.frontend.scenes.SubLayout;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class UserAccountSubLayout implements SubLayout {
    private final UserAccountApiClient userAccountApiClient;

    public UserAccountSubLayout(UserAccountApiClient userAccountApiClient) {
        this.userAccountApiClient = userAccountApiClient;
    }

    @Override
    public Node getTopRightRoot() {
        Label label = new Label();
        label.setAlignment(Pos.TOP_RIGHT);
        label.setText("Accounts");
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return label;
    }

    @Override
    public Button[] getRightButtons() {
        Button deleteAccountButton = new Button("Delete Account");
        deleteAccountButton.setOnAction(e -> this.deleteAccount());
        return new Button[]{deleteAccountButton};
    }

    private void deleteAccount() {
        String username = PopupUtil.INSTANCE.textInputPopup(
                "Delete Account",
                "We have to confirm your account info before delete",
                "Your username:"
        );

        if (username.isEmpty()) {
            PopupUtil.INSTANCE.infoPopup(
                    "Delete Account",
                    null,
                    "Canceled"
            );
            return;
        }

        String password = PopupUtil.INSTANCE.textInputPopup(
                "Delete Account",
                "We have to confirm your account info before delete",
                "Your password:"
        );

        if (password.isEmpty()) {
            PopupUtil.INSTANCE.infoPopup(
                    "Delete Account",
                    null,
                    "Canceled"
            );
            return;
        }

        PopupUtil.INSTANCE.doWithProcessingPopupWithoutCancel(
                () -> this.userAccountApiClient.deleteAccount(username, password),
                result -> {
                    if (result.getFirst()) {
                        PopupUtil.INSTANCE.infoPopup(
                                "Delete Account",
                                null,
                                "Account has been deleted! Please exit manually."
                        );
                    } else {
                        PopupUtil.INSTANCE.showError(null, "Error when deleting account:\n" + result.getSecond());
                    }
                    return null;
                },
                e -> {
                    PopupUtil.INSTANCE.showError(null, "Error when getting account info: " + e.getLocalizedMessage());
                    return null;
                }
        );
    }

    @Override
    public Button[] getBottomRightButtons() {
        // no button is needed
        return new Button[]{};
    }

    @Override
    public Node getCenterNode() {
        Label bigInfoLabel = new Label();
        bigInfoLabel.setAlignment(Pos.CENTER);
        bigInfoLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        // load with fresh account info
        PopupUtil.INSTANCE.doWithProcessingPopupWithoutCancel(
                this.userAccountApiClient::whoAmI,
                result -> {
                    if (result.getFirst()) {
                        bigInfoLabel.setText(
                                "Current user info: \n" +
                                        "User id: " + result.getThird().getUserId() + "\n" +
                                        "Username: " + result.getThird().getUsername() + "\n" +
                                        "Password md5: " + result.getThird().getHashedPassword()
                        );
                    } else {
                        PopupUtil.INSTANCE.showError(null, "Error when getting account info: " + result.getSecond());
                    }
                    return null;
                },
                e -> {
                    PopupUtil.INSTANCE.showError(null, "Error when getting account info: " + e.getLocalizedMessage());
                    return null;
                }
        );
        return bigInfoLabel;
    }
}

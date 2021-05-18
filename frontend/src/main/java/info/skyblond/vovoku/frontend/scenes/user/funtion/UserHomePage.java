package info.skyblond.vovoku.frontend.scenes.user.funtion;

import info.skyblond.vovoku.frontend.api.user.UserApiClient;
import info.skyblond.vovoku.frontend.scenes.AbstractHomePage;
import info.skyblond.vovoku.frontend.scenes.PopupUtil;
import info.skyblond.vovoku.frontend.scenes.SubScene;
import info.skyblond.vovoku.frontend.scenes.user.login.UserLoginScene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;

public class UserHomePage extends AbstractHomePage {
    private final OkHttpClient httpClient;
    private final UserApiClient userApiClient;

    public UserHomePage(OkHttpClient httpClient, UserApiClient userApiClient) {
        this.httpClient = httpClient;
        this.userApiClient = userApiClient;
    }

    @Override
    public Pane getRootLayout() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5));

        // set up top
        Label accountInfo = new Label();
        this.genTop(root, accountInfo, null);


        // go back to User login
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> this.backToUserLogin());
        this.genBottom(root, exitButton);


        Button accountButton = new Button("Account");
        accountButton.setMaxWidth(Double.MAX_VALUE);
        accountButton.setOnAction(e -> {
            var accountSubLayout = new UserAccountSubLayout(this.userApiClient.getAccountApiClient());
            this.genBottom(root, exitButton, accountSubLayout.getBottomRightButtons());
            this.genCenter(root, accountSubLayout.getCenterNode());
            this.genTop(root, accountInfo, accountSubLayout.getTopRightRoot());
            this.genRight(root, accountSubLayout.getRightButtons());
        });

        Button pictureButton = new Button("Picture");
        pictureButton.setMaxWidth(Double.MAX_VALUE);
        pictureButton.setOnAction(e -> {
            var pictureSubLayer = new UserPictureSubLayer(this.userApiClient.getPictureApiClient(), this.primaryStage);
            this.genBottom(root, exitButton, pictureSubLayer.getBottomRightButtons());
            this.genCenter(root, pictureSubLayer.getCenterNode());
            this.genTop(root, accountInfo, pictureSubLayer.getTopRightRoot());
            this.genRight(root, pictureSubLayer.getRightButtons());
        });

        Button modelButton = new Button("Model");
        modelButton.setMaxWidth(Double.MAX_VALUE);
        modelButton.setOnAction(e -> {
            var modelSubLayer = new UserModelSubLayer(
                    this.userApiClient.getModelApiClient(),
                    this.userApiClient.getPictureApiClient(),
                    this.userApiClient.getPrototypeApiClient()
            );
            this.genBottom(root, exitButton, modelSubLayer.getBottomRightButtons());
            this.genCenter(root, modelSubLayer.getCenterNode());
            this.genTop(root, accountInfo, modelSubLayer.getTopRightRoot());
            this.genRight(root, modelSubLayer.getRightButtons());
        });

        Button prototypeButton = new Button("Prototype");
        prototypeButton.setMaxWidth(Double.MAX_VALUE);
        prototypeButton.setOnAction(e -> {
            var prototypeSubLayer = new UserPrototypeSubLayer(this.userApiClient.getPrototypeApiClient());
            this.genBottom(root, exitButton, prototypeSubLayer.getBottomRightButtons());
            this.genCenter(root, prototypeSubLayer.getCenterNode());
            this.genTop(root, accountInfo, prototypeSubLayer.getTopRightRoot());
            this.genRight(root, prototypeSubLayer.getRightButtons());
        });

        genLeft(root, accountButton, pictureButton, modelButton, prototypeButton);

        PopupUtil.INSTANCE.doWithProcessingPopupWithoutCancel(
                () -> this.userApiClient.getAccountApiClient().whoAmI(),
                result -> {
                    if (result.getFirst()) {
                        accountInfo.setText(
                                "User id: " + result.getThird().getUserId() + "\n" +
                                        "Username: " + result.getThird().getUsername() + "\n" +
                                        "Token: " + this.userApiClient.getToken()
                        );
                    } else {
                        PopupUtil.INSTANCE.showError(null, "Error when getting account info: " + result.getSecond());
                        this.backToUserLogin();
                    }
                    return null;
                },
                e -> {
                    PopupUtil.INSTANCE.showError(null, "Error when getting account info: " + e.getLocalizedMessage());
                    this.backToUserLogin();
                    return null;
                }
        );

        return root;
    }


    private void backToUserLogin() {
        UserLoginScene userLoginScene = new UserLoginScene(this.httpClient);
        Scene scene = new Scene(userLoginScene.getRootLayout(), this.currentScene.getWidth(), this.currentScene.getHeight());
        userLoginScene.initScene(this.primaryStage, scene);
        this.primaryStage.setScene(scene);
    }

}

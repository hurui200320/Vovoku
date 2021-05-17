package info.skyblond.vovoku.frontend.scenes.user.funtion;

import info.skyblond.vovoku.frontend.api.user.UserApiClient;
import info.skyblond.vovoku.frontend.scenes.PopupUtil;
import info.skyblond.vovoku.frontend.scenes.user.SubScene;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHomePage implements SubScene {
    private final Logger logger = LoggerFactory.getLogger(UserHomePage.class);

    private final OkHttpClient httpClient;
    private final UserApiClient userApiClient;
    private Stage primaryStage;
    private Scene currentScene;

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
//        accountInfo.setPadding(new Insets(5));
        accountInfo.setStyle("-fx-background-color: #31a547");
        // account info only take half
//        accountInfo.prefWidthProperty().bind(top.widthProperty().divide(2));
//        accountInfo.minWidthProperty().bind(top.widthProperty().divide(2));
//        accountInfo.prefHeightProperty().bind(top.heightProperty());
//        accountInfo.minHeightProperty().bind(top.heightProperty());
        this.genTop(root, accountInfo, null);


        // go back to User login
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> this.backToUserLogin());
        this.genBottom(root, exitButton);


        VBox left = new VBox(10);
        left.setFillWidth(true);
        left.setPadding(new Insets(5, 15, 5, 5));
        left.setStyle("-fx-background-color: #d1d25c");
        left.setPrefWidth(100);
        left.setAlignment(Pos.TOP_CENTER);

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

        left.getChildren().addAll(accountButton, pictureButton, modelButton, prototypeButton);
        root.setLeft(left);

        PopupUtil.INSTANCE.doWithProcessingPopupWithoutCancel(
                () -> this.userApiClient.getAccountApiClient().whoAmI(),
                result -> {
                    if (result.getFirst()) {
                        accountInfo.setText(
                                "User id: " + result.getThird().getUserId() + "\n" +
                                        "Username: " + result.getThird().getUsername() + "\n" +
                                        "Token: " + this.userApiClient.getToken$frontend()
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

    @Override
    public void initScene(Stage stage, Scene scene) {
        this.primaryStage = stage;
        this.currentScene = scene;
    }

    private void backToUserLogin() {
        UserLoginScene userLoginScene = new UserLoginScene(this.httpClient);
        Scene scene = new Scene(userLoginScene.getRootLayout(), this.currentScene.getWidth(), this.currentScene.getHeight());
        userLoginScene.initScene(this.primaryStage, scene);
        this.primaryStage.setScene(scene);
    }

    private HBox genTop(BorderPane root, Node n1, Node n2) {
        HBox top = new HBox(20);
        top.setFillHeight(true);
        top.setStyle("-fx-background-color: #0ae3ea");
        top.setPadding(new Insets(5, 5, 15, 5));
        top.maxHeightProperty().bind(root.heightProperty().multiply(0.25));

        if (n1 != null) {
            n1.maxHeight(Double.MAX_VALUE);
            top.getChildren().add(n1);
        }

        if (n2 != null) {
            n2.maxHeight(Double.MAX_VALUE);
            HBox.setHgrow(n2, Priority.ALWAYS);
            n2.setStyle("-fx-background-color: #cd05f3");
            top.getChildren().add(n2);
        }

        root.setTop(top);
        return top;
    }

    private HBox genBottom(BorderPane root, Button exitButton, Button... buttons) {
        HBox bottom = new HBox(20);
        bottom.setFillHeight(true);
        bottom.setPrefHeight(45);
        bottom.setStyle("-fx-background-color: #ea0a58");
        bottom.setPadding(new Insets(15, 5, 5, 5));
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.maxHeightProperty().bind(root.heightProperty().multiply(0.25));

        HBox buttonHBox = new HBox(20);
        buttonHBox.setStyle("-fx-background-color: #eac50a");
        buttonHBox.getChildren().addAll(buttons);

        for (Button b : buttons) {
            b.setMaxHeight(Double.MAX_VALUE);
        }

        // go back to User login
        buttonHBox.maxHeight(Double.MAX_VALUE);
        exitButton.setMaxHeight(Double.MAX_VALUE);
        bottom.getChildren().addAll(buttonHBox, exitButton);
        root.setBottom(bottom);
        return bottom;
    }

    private VBox genRight(BorderPane root, Button... buttons) {
        VBox right = new VBox(10);
        right.setFillWidth(true);
        right.setPadding(new Insets(5, 5, 5, 15));
        right.setStyle("-fx-background-color: #2d7fe3");
        right.setPrefWidth(100);
        right.setAlignment(Pos.TOP_CENTER);

        for (Button b : buttons) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setWrapText(true);
            b.setAlignment(Pos.CENTER);
        }

        right.getChildren().addAll(buttons);
        root.setRight(right);
        return right;
    }

    private Node genCenter(BorderPane root, Node center) {
        if (center != null) {
            center.setStyle("-fx-background-color: #35a17f");
        }
        root.setCenter(center);
        return center;
    }
}

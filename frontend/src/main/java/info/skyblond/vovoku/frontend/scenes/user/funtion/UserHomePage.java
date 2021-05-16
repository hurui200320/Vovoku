package info.skyblond.vovoku.frontend.scenes.user.funtion;

import info.skyblond.vovoku.frontend.api.user.UserApiClient;
import info.skyblond.vovoku.frontend.scenes.PopupUtil;
import info.skyblond.vovoku.frontend.scenes.user.login.UserLoginScene;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHomePage {
    private final Logger logger = LoggerFactory.getLogger(UserHomePage.class);

    private final OkHttpClient httpClient;
    private final UserApiClient userApiClient;
    private Stage primaryStage;
    private Scene currentScene;

    public UserHomePage(OkHttpClient httpClient, UserApiClient userApiClient) {
        this.httpClient = httpClient;
        this.userApiClient = userApiClient;
    }

    public Pane getRootLayout() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5));

        HBox top = new HBox(20);
        top.setStyle("-fx-background-color: #0ae3ea");
        top.setPadding(new Insets(5, 5, 15, 5));
        top.prefHeight(260);
        top.maxHeightProperty().bind(root.heightProperty().multiply(0.25));
        Label accountInfo = new Label();
//        accountInfo.setPadding(new Insets(5));
        accountInfo.setStyle("-fx-background-color: #31a547");
        // account info only take half
//        accountInfo.prefWidthProperty().bind(top.widthProperty().divide(2));
//        accountInfo.minWidthProperty().bind(top.widthProperty().divide(2));
//        accountInfo.prefHeightProperty().bind(top.heightProperty());
//        accountInfo.minHeightProperty().bind(top.heightProperty());

        top.getChildren().add(accountInfo);
        root.setTop(top);

        VBox left = new VBox(10);
        left.setPadding(new Insets(5, 15, 5, 5));
        left.setStyle("-fx-background-color: #d1d25c");
        left.setPrefWidth(100);
        left.setFillWidth(true);

        Button accountButton = new Button("Account");
        accountButton.setMaxWidth(Double.MAX_VALUE);
        Button pictureButton = new Button("Picture");
        pictureButton.setMaxWidth(Double.MAX_VALUE);
        Button modelButton = new Button("Model");
        modelButton.setMaxWidth(Double.MAX_VALUE);
        Button prototypeButton = new Button("Prototype");
        prototypeButton.setMaxWidth(Double.MAX_VALUE);

        left.getChildren().addAll(accountButton, pictureButton, modelButton, prototypeButton);
        root.setLeft(left);

        HBox bottom = new HBox(20);
        bottom.setStyle("-fx-background-color: #ea0a58");
        bottom.setPadding(new Insets(15, 5, 5, 5));
        bottom.prefHeight(260);
//        bottom.maxHeightProperty().bind(root.heightProperty().multiply(0.25));

        // go back to User login
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> this.backToUserLogin());
        bottom.getChildren().add(exitButton);
        root.setBottom(bottom);

        PopupUtil.INSTANCE.doWithProcessingPopupWithoutCancel(
                () -> this.userApiClient.getAccountApiClient().whoAmI(),
                result -> {
                    if (result.getFirst()) {
                        // TODO update label
                        accountInfo.setText(
                                "User id: " + result.getThird().getUserId() + "\n" +
                                        "Username: " + result.getThird().getUsername() + "\n" +
                                        "Token: " + this.userApiClient.getToken$frontend()
                        );
//                        logger.info(accountInfo.getText());
//                        logger.info(root.getHeight() + "");
//                        logger.info(top.getHeight() + "");
//                        logger.info(accountInfo.getWidth() + "");
                    } else {
                        PopupUtil.INSTANCE.showError(
                                null,
                                "Error when getting account info: " + result.getSecond()
                        );
                        this.backToUserLogin();
                    }
                    return null;
                },
                e -> {
                    PopupUtil.INSTANCE.showError(
                            null,
                            "Error when getting account info: " + e.getLocalizedMessage()
                    );
                    this.backToUserLogin();
                    return null;
                }
        );

        return root;
    }

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
}

package info.skyblond.vovoku.frontend.scenes.admin.function;

import info.skyblond.vovoku.frontend.api.admin.AdminApiClient;
import info.skyblond.vovoku.frontend.scenes.AbstractHomePage;
import info.skyblond.vovoku.frontend.scenes.admin.login.AdminLoginScene;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import okhttp3.OkHttpClient;

public class AdminHomePage extends AbstractHomePage {
    private final OkHttpClient httpClient;
    private final AdminApiClient adminApiClient;

    public AdminHomePage(OkHttpClient httpClient, AdminApiClient adminApiClient) {
        this.httpClient = httpClient;
        this.adminApiClient = adminApiClient;
    }

    @Override
    public Pane getRootLayout() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5));

        // set up top
        Label accountInfo = new Label("Admin Override");
        accountInfo.setStyle("-fx-text-fill: #ff0000");

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), evt -> accountInfo.setVisible(false)),
                new KeyFrame(Duration.seconds(0.9), evt -> accountInfo.setVisible(true))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        this.genTop(root, accountInfo, null);

        // go back to User login
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> {
            timeline.stop();
            this.backToLogin();
        });
        this.genBottom(root, exitButton);

        VBox left = new VBox(10);
        left.setFillWidth(true);
        left.setPadding(new Insets(5, 15, 5, 5));
        left.setPrefWidth(100);
        left.setAlignment(Pos.TOP_CENTER);

        Button accountButton = new Button("Account");
        accountButton.setMaxWidth(Double.MAX_VALUE);
        accountButton.setOnAction(e -> {
            var accountSubLayout = new AdminAccountSubLayout(this.adminApiClient);
            this.genBottom(root, exitButton, accountSubLayout.getBottomRightButtons());
            this.genCenter(root, accountSubLayout.getCenterNode());
            this.genTop(root, accountInfo, accountSubLayout.getTopRightRoot());
            this.genRight(root, accountSubLayout.getRightButtons());
        });

        Button pictureButton = new Button("Picture");
        pictureButton.setMaxWidth(Double.MAX_VALUE);
        pictureButton.setOnAction(e -> {
            var pictureSubLayer = new AdminPictureSubLayer(this.adminApiClient);
            this.genBottom(root, exitButton, pictureSubLayer.getBottomRightButtons());
            this.genCenter(root, pictureSubLayer.getCenterNode());
            this.genTop(root, accountInfo, pictureSubLayer.getTopRightRoot());
            this.genRight(root, pictureSubLayer.getRightButtons());
        });

        Button modelButton = new Button("Model");
        modelButton.setMaxWidth(Double.MAX_VALUE);
        modelButton.setOnAction(e -> {
            var modelSubLayer = new AdminModelSubLayer(this.adminApiClient);
            this.genBottom(root, exitButton, modelSubLayer.getBottomRightButtons());
            this.genCenter(root, modelSubLayer.getCenterNode());
            this.genTop(root, accountInfo, modelSubLayer.getTopRightRoot());
            this.genRight(root, modelSubLayer.getRightButtons());
        });


        left.getChildren().addAll(accountButton, pictureButton, modelButton);
        root.setLeft(left);

        return root;
    }

    private void backToLogin() {
        AdminLoginScene adminLoginScene = new AdminLoginScene(this.httpClient);
        Scene scene = new Scene(adminLoginScene.getRootLayout(), this.currentScene.getWidth(), this.currentScene.getHeight());
        adminLoginScene.initScene(this.primaryStage, scene);
        this.primaryStage.setScene(scene);
    }
}

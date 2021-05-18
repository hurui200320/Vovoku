package info.skyblond.vovoku.frontend.scenes.admin.login;

import info.skyblond.vovoku.frontend.api.admin.AdminApiClient;
import info.skyblond.vovoku.frontend.scenes.PopupUtil;
import info.skyblond.vovoku.frontend.scenes.SubScene;
import info.skyblond.vovoku.frontend.scenes.admin.function.AdminHomePage;
import info.skyblond.vovoku.frontend.scenes.user.login.UserLoginScene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class AdminLoginScene implements SubScene {
    private final Logger logger = LoggerFactory.getLogger(AdminLoginScene.class);

    private final OkHttpClient httpClient;
    private Stage primaryStage;
    private Scene currentScene;

    private TextField serverTextField;
    private TextField aesKeyTextField;

    public AdminLoginScene(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Pane getRootLayout() {
        GridPane root = new GridPane();
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setVgap(10);
        root.setHgap(8);

        root.setAlignment(Pos.CENTER);

        root.add(new Label("Server"), 0, 0);
        this.serverTextField = new TextField();
        root.add(this.serverTextField, 1, 0);
        this.serverTextField.setText("http://localhost:7000/");

        root.add(new Label("Key"), 0, 1);
        this.aesKeyTextField = new TextField();
        root.add(this.aesKeyTextField, 1, 1);

        Button login = new Button("Login");
        login.setDefaultButton(true);
        login.setOnAction(e -> this.handleLogin());
        GridPane.setFillWidth(login, true);
        login.setMaxWidth(Double.MAX_VALUE);
        root.add(login, 1, 2);

        Button admin = new Button("User");
        admin.setOnAction(e -> this.gotoUser());
        GridPane.setFillWidth(admin, true);
        admin.setMaxWidth(Double.MAX_VALUE);
        root.add(admin, 1, 3);

        return root;
    }

    @Override
    public void initScene(Stage stage, Scene scene) {
        this.primaryStage = stage;
        this.currentScene = scene;
    }


    private void handleLogin() {
        if (this.serverTextField.getText().isBlank()) {
            PopupUtil.INSTANCE.showError(null, "Server must not empty");
            return;
        }
        if (this.aesKeyTextField.getText().isBlank()) {
            PopupUtil.INSTANCE.showError(null, "Key must not empty");
            return;
        }

        SecretKeySpec keySpec;
        try {
            keySpec = new SecretKeySpec(
                    DatatypeConverter.parseHexBinary(this.aesKeyTextField.getText().toUpperCase()),
                    "AES"
            );
        } catch (Exception e) {
            PopupUtil.INSTANCE.showError(
                    "Login failed",
                    "Cannot parse key:\n" + e.getLocalizedMessage()
            );
            return;
        }

        var adminApiClient = new AdminApiClient(
                this.httpClient,
                this.serverTextField.getText(),
                keySpec
        );

        // there is no need to communicate with server
        // so just jump
        AdminHomePage adminHomePage = new AdminHomePage(this.httpClient, adminApiClient);
        Scene scene = new Scene(adminHomePage.getRootLayout(), this.currentScene.getWidth(), this.currentScene.getHeight());
        adminHomePage.initScene(this.primaryStage, scene);
        this.primaryStage.setScene(scene);
    }

    private void gotoUser() {
        UserLoginScene userLoginScene = new UserLoginScene(this.httpClient);
        Scene scene = new Scene(userLoginScene.getRootLayout(), this.currentScene.getWidth(), this.currentScene.getHeight());
        userLoginScene.initScene(this.primaryStage, scene);
        this.primaryStage.setScene(scene);
    }
}

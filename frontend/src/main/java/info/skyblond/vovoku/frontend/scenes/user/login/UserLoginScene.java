package info.skyblond.vovoku.frontend.scenes.user.login;

import info.skyblond.vovoku.frontend.api.user.UserApiClient;
import info.skyblond.vovoku.frontend.scenes.PopupUtil;
import info.skyblond.vovoku.frontend.scenes.admin.login.AdminLoginScene;
import info.skyblond.vovoku.frontend.scenes.user.SubScene;
import info.skyblond.vovoku.frontend.scenes.user.funtion.UserHomePage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserLoginScene implements SubScene {
    private final Logger logger = LoggerFactory.getLogger(UserLoginScene.class);

    private final OkHttpClient httpClient;
    private Stage primaryStage;
    private Scene currentScene;

    public UserLoginScene(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private TextField serverTextField;
    private TextField usernameTextField;
    private PasswordField passwordField;

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

        root.add(new Label("Username"), 0, 1);
        this.usernameTextField = new TextField();
        root.add(this.usernameTextField, 1, 1);

        root.add(new Label("Password"), 0, 2);
        this.passwordField = new PasswordField();
        root.add(this.passwordField, 1, 2);

        // TODO debug only
        this.usernameTextField.setText("hurui");
        this.passwordField.setText("passw0rd");

        Button login = new Button("Login");
        login.setDefaultButton(true);
        login.setOnAction(e -> this.handleLogin());
        GridPane.setFillWidth(login, true);
        login.setMaxWidth(Double.MAX_VALUE);
        root.add(login, 1, 3);

        Button admin = new Button("Admin");
        admin.setOnAction(e -> this.gotoAdmin());
        GridPane.setFillWidth(admin, true);
        admin.setMaxWidth(Double.MAX_VALUE);
        root.add(admin, 1, 4);

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
        if (this.usernameTextField.getText().isBlank()) {
            PopupUtil.INSTANCE.showError(null, "Username must not empty");
            return;
        }
        if (this.passwordField.getText().isBlank()) {
            PopupUtil.INSTANCE.showError(null, "Password must not empty");
            return;
        }
        var userApiClient = new UserApiClient(this.httpClient, this.serverTextField.getText());

        PopupUtil.INSTANCE.doWithProcessingPopup(
                () -> userApiClient.login(
                        this.usernameTextField.getText(),
                        this.passwordField.getText()
                ),
                result -> {
                    if (result.getFirst()) {
                        // goto user home page
                        UserHomePage userHomePage = new UserHomePage(this.httpClient, userApiClient);
                        Scene scene = new Scene(userHomePage.getRootLayout(), this.currentScene.getWidth(), this.currentScene.getHeight());
                        userHomePage.initScene(this.primaryStage, scene);
                        this.primaryStage.setScene(scene);
                    } else {
                        PopupUtil.INSTANCE.showError(
                                "Login failed",
                                result.getSecond()
                        );
                    }
                    return null;
                },
                e -> {
                    PopupUtil.INSTANCE.showError(
                            "Error when trying to login",
                            e.getLocalizedMessage()
                    );
                    return null;
                },
                () -> null
        );
    }

    private void gotoAdmin() {
        AdminLoginScene adminLoginScene = new AdminLoginScene(this.httpClient);
        Scene scene = new Scene(adminLoginScene.getRootLayout(), this.currentScene.getWidth(), this.currentScene.getHeight());
        adminLoginScene.initScene(this.primaryStage, scene);
        this.primaryStage.setScene(scene);
    }
}

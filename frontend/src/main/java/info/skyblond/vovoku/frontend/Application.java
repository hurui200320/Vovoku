package info.skyblond.vovoku.frontend;

import info.skyblond.vovoku.frontend.scenes.PopupUtil;
import info.skyblond.vovoku.frontend.scenes.user.login.UserLoginScene;
import javafx.scene.Scene;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application extends javafx.application.Application {
    private final Logger logger = LoggerFactory.getLogger(Application.class);

    private static final OkHttpClient httpClient = new OkHttpClient();
    private static UserLoginScene userLoginScene;
//    private static final UserLoginScene adminLoginScene = new UserLoginScene();

    public static void main(String[] args) {
        Application.userLoginScene = new UserLoginScene(Application.httpClient);
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(Application.userLoginScene.getRootLayout(), 800, 600);
        Application.userLoginScene.initScene(primaryStage, scene);

        primaryStage.setOnCloseRequest(e -> {
            PopupUtil.INSTANCE.close();
        });

        // let user login scene be the default one
        primaryStage.setTitle("Vovoku");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

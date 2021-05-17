package info.skyblond.vovoku.frontend.scenes.user;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public interface SubScene {
    Pane getRootLayout();

    void initScene(Stage stage, Scene scene);
}

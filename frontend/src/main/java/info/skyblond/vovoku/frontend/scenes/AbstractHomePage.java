package info.skyblond.vovoku.frontend.scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public abstract class AbstractHomePage implements SubScene {
    protected Stage primaryStage;
    protected Scene currentScene;

    @Override
    public void initScene(Stage stage, Scene scene) {
        this.primaryStage = stage;
        this.currentScene = scene;
    }


    protected void genTop(BorderPane root, Node n1, Node n2) {
        HBox top = new HBox(20);
        top.setFillHeight(true);
        top.setStyle("-fx-background-color: #95A7BF");
        top.setPadding(new Insets(5, 5, 15, 5));
        top.maxHeightProperty().bind(root.heightProperty().multiply(0.25));

        if (n1 != null) {
            n1.maxHeight(Double.MAX_VALUE);
            top.getChildren().add(n1);
        }

        if (n2 != null) {
            n2.maxHeight(Double.MAX_VALUE);
            HBox.setHgrow(n2, Priority.ALWAYS);
            top.getChildren().add(n2);
        }

        root.setTop(top);
    }

    protected void genBottom(BorderPane root, Button exitButton, Button... buttons) {
        HBox bottom = new HBox(20);
        bottom.setFillHeight(true);
        bottom.setPrefHeight(45);
        bottom.setStyle("-fx-background-color: #BF8888");
        bottom.setPadding(new Insets(15, 5, 5, 5));
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.maxHeightProperty().bind(root.heightProperty().multiply(0.25));

        HBox buttonHBox = new HBox(20);
        buttonHBox.getChildren().addAll(buttons);

        for (Button b : buttons) {
            b.setMaxHeight(Double.MAX_VALUE);
        }

        // go back to User login
        buttonHBox.maxHeight(Double.MAX_VALUE);
        exitButton.setMaxHeight(Double.MAX_VALUE);
        bottom.getChildren().addAll(buttonHBox, exitButton);
        root.setBottom(bottom);
    }

    protected void genLeft(BorderPane root, Button... buttons) {
        VBox left = new VBox(10);
        left.setFillWidth(true);
        left.setPadding(new Insets(5, 15, 5, 5));
        left.setStyle("-fx-background-color: #C1D4D9");
        left.setPrefWidth(100);
        left.setAlignment(Pos.TOP_CENTER);
        left.getChildren().addAll(buttons);
        root.setLeft(left);
    }

    protected void genRight(BorderPane root, Button... buttons) {
        VBox right = new VBox(10);
        right.setFillWidth(true);
        right.setPadding(new Insets(5, 5, 5, 15));
        right.setStyle("-fx-background-color: #F2E6DF");
        right.setPrefWidth(100);
        right.setAlignment(Pos.TOP_CENTER);

        for (Button b : buttons) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setWrapText(true);
            b.setAlignment(Pos.CENTER);
        }

        right.getChildren().addAll(buttons);
        root.setRight(right);
    }

    protected void genCenter(BorderPane root, Node center) {
        if (center != null){
            center.setStyle("-fx-background-color: #EEF2FA");
        }
        root.setCenter(center);
    }
}

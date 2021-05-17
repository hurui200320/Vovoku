package info.skyblond.vovoku.frontend.scenes;

import javafx.scene.Node;
import javafx.scene.control.Button;

import java.util.List;


public interface SubLayout {
    Node getTopRightRoot();

    Button[] getRightButtons();

    Button[] getBottomRightButtons();

    Node getCenterNode();
}

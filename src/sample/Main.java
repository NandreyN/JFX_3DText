package sample;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.fxyz3d.shapes.primitives.Text3DMesh;
import org.fxyz3d.utils.CameraTransformer;

import javax.swing.*;

public class Main extends Application {

    private SimpleStringProperty content3D, lightOneColor = new SimpleStringProperty(Color.RED.toString()),
            lightTwoColor = new SimpleStringProperty(Color.GREEN.toString());

    private static final int MAX_LEN = 6;

    private void setupMenu(Scene scene) {
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        menuBar.getMenus().add(menuFile);
        //((Group)(scene.getRoot())).getChildren().add(menuBar);
    }

    @Override
    public void start(Stage primaryStage) {
        content3D = new SimpleStringProperty("Text");
        Group sceneRoot = new Group();
        Scene scene = new Scene(sceneRoot, 500, 500, true, SceneAntialiasing.BALANCED);

        scene.setFill(Color.BLACK);

        setupMenu(scene);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        //setup camera transform for rotational support
        CameraTransformer cameraTransform = new CameraTransformer();
        cameraTransform.setTranslate(0, 0, 0);
        cameraTransform.getChildren().add(camera);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateX(800);
        camera.setTranslateZ(-3000);
        //        cameraTransform.ry.setAngle(-25.0);
        cameraTransform.rx.setAngle(10.0);
        //add a Point Light for better viewing of the grid coordinate system
        PointLight light1 = new PointLight();
        light1.colorProperty().bind(javafx.beans.binding.Bindings.createObjectBinding(() -> Color.web(lightOneColor.getValue()),
                lightOneColor));

        light1.setTranslateX(-250);
        light1.setTranslateY(250);
        light1.setTranslateZ(100);

        PointLight light2 = new PointLight();
        light2.setTranslateX(250);
        light2.setTranslateY(-250);
        light2.setTranslateZ(-100);

        light2.colorProperty().bind(javafx.beans.binding.Bindings.createObjectBinding(() -> Color.web(lightTwoColor.getValue()),
                lightTwoColor));

        cameraTransform.getChildren().addAll(light1, light2);
        scene.setCamera(camera);

        Group group = new Group(cameraTransform);
        Text3DMesh letters = new Text3DMesh("Hi", "Gadugi Bold", 400, true, 120, 0, 1);

        letters.text3DProperty().bind(content3D);

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            boolean handleLightsChange(String req) {
                switch (req) {
                    case "*":
                        java.awt.Color newColor = JColorChooser.showDialog(null, "Choose a color", java.awt.Color.YELLOW);
                        lightOneColor.setValue(
                                String.format("#%02x%02x%02x", newColor.getRed(), newColor.getGreen(), newColor.getBlue()));
                        return true;
                    case "-":
                        java.awt.Color newColor2 = JColorChooser.showDialog(null, "Choose a color", java.awt.Color.YELLOW);
                        lightTwoColor.setValue(
                                String.format("#%02x%02x%02x", newColor2.getRed(), newColor2.getGreen(), newColor2.getBlue()));
                        return true;
                }
                return false;
            }

            @Override
            public void handle(KeyEvent event) {
                boolean r = handleLightsChange(event.getText());
                if (r)
                    return;

                String txt = content3D.getValue();
                //txt += event.getText();
                if (txt.length() > 0 && event.getCode() == KeyCode.BACK_SPACE) {
                    txt = txt.substring(0, txt.length() - 1);
                } else if (txt.length() + 1 <= MAX_LEN && (event.getCode().isLetterKey() || event.getCode().isDigitKey())) {
                    txt += event.getText();
                }
                content3D.setValue(txt);
            }
        });

        group.getChildren().addAll(letters, light1);
        sceneRoot.getChildren().addAll(group);

        primaryStage.setTitle("F(X)yz - Text3D");
        primaryStage.setScene(scene);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}

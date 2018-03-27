package sample;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.fxyz3d.io.OBJWriter;
import org.fxyz3d.shapes.primitives.Text3DMesh;
import org.fxyz3d.shapes.primitives.TexturedMesh;
import org.fxyz3d.utils.CameraTransformer;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Main extends Application {

    private SimpleStringProperty content3D,
            lightTwoColor = new SimpleStringProperty(Color.WHITE.toString());

    private static final int MAX_LEN = 15;

    private double mouseOldX = 0;
    private double mouseOldY = 0;
    private double mousePosX = 0;
    private double mousePosY = 0;
    private double mouseDeltaX = 0;
    private double mouseDeltaY = 0;

    private Color prevTextColor;

    private void setupMenu(Scene scene) {
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        menuBar.getMenus().add(menuFile);
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

        PointLight light2 = new PointLight();
        light2.setTranslateX(250);
        light2.setTranslateY(-250);
        light2.setTranslateZ(-100);

        light2.colorProperty().bind(javafx.beans.binding.Bindings.createObjectBinding(() -> Color.web(lightTwoColor.getValue()),
                lightTwoColor));

        cameraTransform.getChildren().addAll(light2);
        scene.setCamera(camera);

        Group group = new Group(cameraTransform);
        Text3DMesh letters = new Text3DMesh("Hi", "Gadugi Bold", 400, true, 120, 0, 1);

        letters.text3DProperty().bind(content3D);
        prevTextColor = Color.hsb(200, 1, 1);
        letters.setTextureModeNone(prevTextColor);

        //letters.setTextureModeFaces(2500);
        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            boolean handleLightsChange(String req) {
                switch (req) {
                    case "*":
                        java.awt.Color newColor = JColorChooser.showDialog(null, "Choose a color", java.awt.Color.YELLOW);
                        letters.setTextureModeNone(Color.rgb(newColor.getRed(), newColor.getGreen(), newColor.getBlue()));
                        prevTextColor = Color.rgb(newColor.getRed(), newColor.getGreen(), newColor.getBlue());
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
                boolean complexNotControlKeys = event.getCode() != KeyCode.W &&
                        event.getCode() != KeyCode.A && event.getCode() != KeyCode.S && event.getCode() != KeyCode.D;

                //txt += event.getText();
                if (txt.length() > 0 && event.getCode() == KeyCode.BACK_SPACE) {
                    txt = txt.substring(0, txt.length() - 1);
                } else if (complexNotControlKeys && txt.length() + 1 <= MAX_LEN && (event.getCode().isLetterKey() || event.getCode().isDigitKey())) {
                    txt += event.getText();
                }
                content3D.setValue(txt);
                if (prevTextColor != null)
                    letters.setTextureModeNone(prevTextColor);
            }
        });

        scene.setOnKeyPressed(event -> {
            double change = 10.0;
            //Add shift modifier to simulate "Running Speed"
            if (event.isShiftDown()) {
                change = 50.0;
            }
            //What key did the user press?
            KeyCode keycode = event.getCode();
            //Step 2c: Add Zoom controls
            if (keycode == KeyCode.W) {
                light2.setTranslateZ(light2.getTranslateZ() + change);
            }
            if (keycode == KeyCode.S) {
                light2.setTranslateZ(light2.getTranslateZ() - change);
            }
            //Step 2d:  Add Strafe controls
            if (keycode == KeyCode.A) {
                light2.setTranslateX(light2.getTranslateX() - change);
            }
            if (keycode == KeyCode.D) {
                light2.setTranslateX(light2.getTranslateX() + change);
            }
        });

        scene.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnMouseDragged((MouseEvent me) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            double modifier = 10.0;
            double modifierFactor = 0.1;

            if (me.isControlDown()) {
                modifier = 0.1;
            }
            if (me.isShiftDown()) {
                modifier = 50.0;
            }
            if (me.isPrimaryButtonDown()) {
                cameraTransform.ry.setAngle(((cameraTransform.ry.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);  // +
                cameraTransform.rx.setAngle(((cameraTransform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);  // -
            } else if (me.isSecondaryButtonDown()) {
                double z = camera.getTranslateZ();
                double newZ = z + mouseDeltaX * modifierFactor * modifier;
                camera.setTranslateZ(newZ);
            } else if (me.isMiddleButtonDown()) {
                cameraTransform.t.setX(cameraTransform.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
                cameraTransform.t.setY(cameraTransform.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
            }
        });

        primaryStage.setTitle("F(X)yz - Text3D");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Letter transformations


        OBJWriter writer = new OBJWriter((TriangleMesh) ((TexturedMesh) (letters.getChildren().get(0))).getMesh(),
                "letter");
        writer.setTextureColors(6);
        writer.exportMesh();


        group.getChildren().addAll(letters, light2);
        sceneRoot.getChildren().addAll(group);

        primaryStage.setTitle("F(X)yz - Text3D");
        primaryStage.setScene(scene);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}

package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.io.WriteAbortedException;


/**
 * Hold down an arrow key to have your hero move around the screen.
 * Hold down the shift key to have the hero run.
 */
public class Main extends Application {

    private static final double W = 1000, H = 640;

    private static final String HERO_IMAGE_LOC = "images/car2.0.png";
    private static final String MAP_IMAGE_LOC = "images/map.jpg";


    private Image heroImage, mapImage;
    private Node hero;
    private ImageView map, cameraView;
    private double rotateFactor;
    boolean running, goNorth, goSouth, goEast, goWest;
    WritableImage camera;
    private JProgressBar statusBar;

    @Override
    public void start(Stage stage) throws Exception {
        heroImage = new Image(HERO_IMAGE_LOC);
        mapImage = new Image(MAP_IMAGE_LOC);
        map = new ImageView(mapImage);
        hero = new ImageView(heroImage);
        camera = new WritableImage(200, 50);
        cameraView = new ImageView(camera);
        cameraView.setX(0);
        cameraView.setY(H - camera.getHeight());

       /* ((ImageView) hero).setFitHeight(100);
        ((ImageView) hero).setFitWidth(50);*/
        Group dungeon = new Group();
        dungeon.getChildren().add(map);
        dungeon.getChildren().add(hero);
        dungeon.getChildren().add(cameraView);
        moveHeroTo(W / 2, H / 2);
        rotateFactor = 0;
        Scene scene = new Scene(dungeon, W, H);

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case W:
                    case UP:
                        goNorth = true;
                        break;
                    case S:
                    case DOWN:
                        goSouth = true;
                        break;
                    case A:
                    case LEFT:
                        goWest = true;
                        break;
                    case D:
                    case RIGHT:
                        goEast = true;
                        break;
                    case SHIFT:
                        running = true;
                        break;
                }
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case W:
                    case UP:
                        goNorth = false;
                        break;
                    case S:
                    case DOWN:
                        goSouth = false;
                        break;
                    case A:
                    case LEFT:
                        goWest = false;
                        break;
                    case D:
                    case RIGHT:
                        goEast = false;
                        break;
                    case SHIFT:
                        running = false;
                        break;
                }
            }
        });

        stage.setScene(scene);
        stage.show();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                int d = 0;
                double dx = 0, dy = 0;
                if (goNorth) d -= 1;
                if (goSouth) d += 1;
                if (goEast) hero.setRotate(rotateFactor++);
                if (goWest) hero.setRotate(rotateFactor--);
                if (running) {
                    d *= 3;
                }


                dx = (Math.sin(hero.getRotate() * Math.PI / 180) * d);
                dy = -(Math.cos(hero.getRotate() * Math.PI / 180) * d);
                drawCamera();
                moveHeroBy(dx, dy);
            }
        };
        timer.start();
    }

    private void moveHeroBy(double dx, double dy) {
        if (dx == 0 && dy == 0) return;

        final double cx = hero.getBoundsInLocal().getWidth() / 2;
        final double cy = hero.getBoundsInLocal().getHeight() / 2;
        double x = cx + hero.getLayoutX() + dx;
        double y = cy + hero.getLayoutY() + dy;

        moveHeroTo(x, y);
    }

    private void moveHeroTo(double x, double y) {
        final double cx = hero.getBoundsInLocal().getWidth() / 2;
        final double cy = hero.getBoundsInLocal().getHeight() / 2;

        if (x - cx >= 0 &&
                x + cx <= W &&
                y - cy >= 0 &&
                y + cy <= H) {
            hero.relocate(x - cx, y - cy);
        }
    }

    private void drawCamera() {


        PixelReader pixelReader = mapImage.getPixelReader();

        double x = (hero.getLayoutX() + (hero.getBoundsInLocal().getWidth() *  Math.sin( - hero.getRotate() * Math.PI / 180)));
        double y = (hero.getLayoutY() + (hero.getBoundsInLocal().getHeight() *  Math.cos( - hero.getRotate() * Math.PI / 180)));
        //getting the pixel writer
        PixelWriter writer = camera.getPixelWriter();
        for (int i = 0; i < camera.getWidth(); i++) {
            for (int j = 0; j < camera.getHeight(); j++) {
                if (hero.getRotate() >= 360) hero.setRotate(0);
                double px = x - (Math.cos(hero.getRotate() * Math.PI / 180.0) * (x / 20) - Math.sin(hero.getRotate() * Math.PI / 180.0) * (y / 20));
                double py = y + (Math.cos(hero.getRotate() * Math.PI / 180.0) * (y / 20) + Math.sin(hero.getRotate() * Math.PI / 180.0) * (x / 20));

                double pi = i / 2.0 + (Math.cos(hero.getRotate() * Math.PI / 180.0) * i - Math.sin(hero.getRotate() * Math.PI / 180.0) * j);
                double pj = j / 2.0 - (Math.cos(hero.getRotate() * Math.PI / 180.0) * j + Math.sin(hero.getRotate() * Math.PI / 180.0) * i);
                Color color = pixelReader.getColor((int) (px + pi), (int) (py + pj));

                //Setting the color to the writable image
                writer.setColor(i, j, color);
            }
        }
        cameraView = new ImageView(camera);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.omg.CORBA.BAD_INV_ORDER;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;

/**
 * Hold down an arrow key to have your hero move around the screen.
 * Hold down the shift key to have the hero run.
 */
public class Main extends Application {

    private static final double W = 1000, H = 640;

    private static final String HERO_IMAGE_LOC = "images/car2.0.png";
    private static final String MAP_IMAGE_LOC = "images/map.jpg";
    private static final String CAMERA_BORDER_LOC = "images/border.png";


    private Image heroImage, mapImage, borderImage;
    private Node hero;
    private ImageView map, cameraView, border;
    private double rotateFactor;
    boolean running, goNorth, goSouth, goEast, goWest;
    WritableImage camera;
    private JProgressBar statusBar;
    private File file;
    private FileInputStream fis;

    @Override
    public void start(Stage stage) throws Exception {
        heroImage = new Image(HERO_IMAGE_LOC);
        mapImage = new Image(MAP_IMAGE_LOC);
        borderImage = new Image(CAMERA_BORDER_LOC);
        border = new ImageView(borderImage);
        map = new ImageView(mapImage);
        hero = new ImageView(heroImage);
        camera = new WritableImage(50, 5);
        cameraView = new ImageView(camera);
        file = new File("C:/Users/Eugeniu/IdeaProjects/untitled2/src/files/input.in");
        fis = new FileInputStream(file);
        border.setScaleY(2);
        border.setX(0);
        border.setY(H-camera.getHeight() - 4);
        cameraView.setX(5);
        cameraView.setY(H - camera.getHeight() - 5);

       /* ((ImageView) hero).setFitHeight(100);
        ((ImageView) hero).setFitWidth(50);*/
        Group dungeon = new Group();
        dungeon.getChildren().add(map);
        dungeon.getChildren().add(hero);
        dungeon.getChildren().add(border);
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
                try {
                    Thread.sleep(55);
                } catch (InterruptedException e) {

                }
                int d = 0;
                double dx = 0, dy = 0;
                if (goNorth) d -= 1;
                if (goSouth) d += 1;
                if (goEast) hero.setRotate(rotateFactor++);
                if (goWest) hero.setRotate(rotateFactor--);
                if (running) {
                    d *= 3;
                }

                if (rotateFactor > 360 || rotateFactor < -360) {
                    rotateFactor = 0;
                    hero.setRotate(0);
                }
                try {
                    d += getNextMove().x;
                    hero.setRotate(rotateFactor += getNextMove().y);
                } catch (Exception e) {
                    System.out.println("Excepted");
                    try {
                        fis = new FileInputStream(file);
                        d += getNextMove().x;
                        hero.setRotate(rotateFactor += getNextMove().y);

                    } catch (Exception e1) {

                    }
                }
                dx = (Math.sin(hero.getRotate() * Math.PI / 180) * d);
                dy = (-(Math.cos(hero.getRotate() * Math.PI / 180) * d));
                moveHeroBy(dx, dy);
                drawCamera();
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

        double x = (hero.getLayoutX() + (hero.getBoundsInLocal().getWidth() * Math.sin(-hero.getRotate() * Math.PI / 180)));
        double y = (hero.getLayoutY() + (hero.getBoundsInLocal().getHeight() * Math.cos(-hero.getRotate() * Math.PI / 180)));
        //getting the pixel writer
        PixelWriter writer = camera.getPixelWriter();
        for (int i = 0; i < camera.getWidth(); i++) {
            for (int j = 0; j < camera.getHeight(); j++) {

                Color color = Color.BLACK;
                double px = x - (Math.cos(hero.getRotate() * Math.PI / 180.0) * (x / 20) - Math.sin(hero.getRotate() * Math.PI / 180.0) * (y / 20));
                double py = y + (Math.cos(hero.getRotate() * Math.PI / 180.0) * (y / 20) + Math.sin(hero.getRotate() * Math.PI / 180.0) * (x / 20));

                double pi = i / 10.0 + (Math.cos(hero.getRotate() * Math.PI / 180.0) * i/10 - Math.sin(hero.getRotate() * Math.PI / 180.0) * j/10);
                double pj = j / 10.0 - (Math.cos(hero.getRotate() * Math.PI / 180.0) * j/10 + Math.sin(hero.getRotate() * Math.PI / 180.0) * i/10);
                if ( px + pi < W && py+ pj < H && px+pi > 0 && py+pj > 0){
                   color = pixelReader.getColor(Math.abs((int) (px + pi)), Math.abs((int) (py + pj)));
                }


                //Setting the color to the writable image
                writer.setColor(i, j, color);
            }
        }
        cameraView = new ImageView(camera);
    }

    public Point getNextMove() throws Exception {
            int x = 0, y = 0;
            if (fis.available() <= 0) {
                fis = new FileInputStream(file);
            }
            x = fis.read();
            y = fis.read();
            if ( x != 45 || x != 32 || x!= 48 || x!= 49) {
                x = 0;
            }
        if ( y != 45 || y != 32 || y!= 48 || y!= 49) {
            y = 0;
        }
            if ( x == 32) {
                while (x == 32) {
                    x = fis.read();
                }
            }
            if ( y == 32){
                while(y ==32){
                    y = fis.read();
                }
            };

            if (x == 48) x = 0;
            if ( y == 48) y = 0;
            if (x == 49) x = 1;
            if(y == 49) y = 1;
            if (x==45) {
                x= fis.read();
                x = -1;
            }
            if(y==45){
                y = fis.read();
                y = -1;
            }
            return new Point(x, y);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
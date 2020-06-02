package sample;

import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Group root = new Group();
        Scene scene = new Scene(root, 1000, 600 +70);

        drawAngryBird(root);

        Path movement = new Path(
            new MoveTo(120, 120),
            new QuadCurveTo( 470, 500, 880, 480),
                new LineTo(120,120)
        );

        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(3000));
        pathTransition.setPath(movement);
        pathTransition.setNode(root);
        pathTransition.setAutoReverse(true);

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(3500), root);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(1);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(2000), root);
        scaleTransition.setToX(1.4);
        scaleTransition.setToY(1.4);
        scaleTransition.setAutoReverse(true);

        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(
            rotateTransition,
            scaleTransition,
            pathTransition
        );
        parallelTransition.setCycleCount(Timeline.INDEFINITE);
        parallelTransition.setAutoReverse(true);
        parallelTransition.play();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawAngryBird(Group group) {
        Color black = Color.BLACK;
        Color bodyColor = Color.rgb(216, 31, 45);

        // background
        {
            Color grassColor = Color.rgb(187, 205, 134);
            Color deepGrassColor = Color.rgb(135, 175, 102);
            Color backgroundColor = Color.rgb(179, 220, 242);
            Color horizontColor = Color.rgb(183, 211, 225);

            Rectangle background = new Rectangle(241,223);
            background.setFill(backgroundColor);
            group.getChildren().add(background);

            Path horizont = new Path(
                    new MoveTo(0, 168),
                    new QuadCurveTo(16,167,34, 158),
                    new QuadCurveTo(185,143,241, 142),
                    new LineTo(241, 223),
                    new LineTo(0, 223),
                    new LineTo(0, 168)
            );
            horizont.setFill(horizontColor);
            horizont.setStrokeWidth(0);
            group.getChildren().add(horizont);

            Path grass1 = new Path(
                    new MoveTo(215, 223),
                    new QuadCurveTo(225,208,241, 203),
                    new LineTo(241, 223),
                    new LineTo(215, 223)
            );
            grass1.setFill(grassColor);
            grass1.setStrokeWidth(0);
            group.getChildren().add(grass1);

            Path grass2 = new Path(
                    new MoveTo(0, 202),
                    new QuadCurveTo(59,175,68, 223),
                    new LineTo(0, 223),
                    new LineTo(0, 202)
            );
            grass2.setFill(grassColor);
            grass2.setStrokeWidth(0);
            group.getChildren().add(grass2);

            Path deepGrass2 = new Path(
                    new MoveTo(7, 223),
                    new QuadCurveTo(36,194,45, 223),
                    new LineTo(7, 223)
            );
            deepGrass2.setFill(deepGrassColor);
            deepGrass2.setStrokeWidth(0);
            group.getChildren().add(deepGrass2);
        }

        // tail
        {
            Path feathers = new Path(
                    new MoveTo(34, 136),
                    new LineTo(22, 143),
                    new LineTo(18, 136),
                    new LineTo(30, 131),
                    new LineTo(5, 125),
                    new LineTo(11, 111),
                    new LineTo(36, 123),
                    new LineTo(22, 105),
                    new LineTo(30, 98),
                    new LineTo(40, 113),
                    new LineTo(34, 136)
            );
            feathers.setFill(black);
            feathers.setStrokeWidth(0);
            group.getChildren().add(feathers);
        }

        // body
        {
            Color bellyColor = Color.rgb(225, 195, 171);
            Color spotColor = Color.rgb(167, 23, 36);

            Path body = new Path(
                    new MoveTo(162, 217),
                    new CubicCurveTo(14,230,13,123,67,73),
                    new CubicCurveTo(104,35,173,30,212,90),
                    new CubicCurveTo(248,141,215,213,162,217)
            );
            body.setFill(bodyColor);
            body.setStrokeWidth(5);
            group.getChildren().add(body);

            Path belly = new Path(
                    new MoveTo(66, 200),
                    new CubicCurveTo(111,159,162,162,196,204),
                    new LineTo(165,217),
                    new LineTo(108,216),
                    new LineTo(66,200)
            );
            belly.setFill(bellyColor);
            belly.setStrokeWidth(0);
            group.getChildren().add(belly);

            Path bodyLine = new Path(body.getElements());
            bodyLine.setFill(Color.TRANSPARENT);
            bodyLine.setStrokeWidth(5);
            group.getChildren().add(bodyLine);

            Path spot1 = new Path(
                    new MoveTo(71, 162),
                    new CubicCurveTo(63,150,78,143,85,155),
                    new CubicCurveTo(89,170,77,173,71,162)
            );
            spot1.setFill(spotColor);
            spot1.setStrokeWidth(0);
            group.getChildren().add(spot1);

            Path spot2 = new Path(
                    new MoveTo(94, 149),
                    new CubicCurveTo(94,127,109,127,112,149),
                    new CubicCurveTo(110,172,94,172,94,149)
            );
            spot2.setFill(spotColor);
            spot2.setStrokeWidth(0);
            group.getChildren().add(spot2);

            Path spot3 = new Path(
                    new MoveTo(135, 124),
                    new CubicCurveTo(109,150,123,180,140,169),
                    new LineTo(163,149),
                    new LineTo(135,124)
            );
            spot3.setFill(spotColor);
            spot3.setStrokeWidth(0);
            group.getChildren().add(spot3);

            Path spot4 = new Path(
                    new MoveTo(206, 131),
                    new CubicCurveTo(220,141,215,163,199,161),
                    new LineTo(177,156),
                    new LineTo(206,131)
            );
            spot4.setFill(spotColor);
            spot4.setStrokeWidth(0);
            group.getChildren().add(spot4);
        }

        // ahoges
        {
            Path feather1 = new Path(
                    new MoveTo(146, 51),
                    new CubicCurveTo(48,63,60,16,106,30),
                    new LineTo(146,51)
            );
            feather1.setFill(bodyColor);
            feather1.setStrokeWidth(5);
            group.getChildren().add(feather1);

            Path feather2 = new Path(
                    new MoveTo(168, 57),
                    new CubicCurveTo(62,28,120,8,124,11),
                    new CubicCurveTo(135,11,156,26,168,57)
            );
            feather2.setFill(bodyColor);
            feather2.setStrokeWidth(5);
            group.getChildren().add(feather2);
        }

        // ahoge clear
        {
            Path clearer1 = new Path(
                    new MoveTo(88, 61),
                    new CubicCurveTo(118,39,177,41,205,87),
                    new LineTo(88,61)
            );
            clearer1.setFill(bodyColor);
            clearer1.setStrokeWidth(0);
            group.getChildren().add(clearer1);

            Path clearer2 = new Path(
                    new MoveTo(103, 31.5),
                    new LineTo(144.5,50),
                    new LineTo(123,53),
                    new LineTo(103,31.5)
            );
            clearer2.setFill(bodyColor);
            clearer2.setStrokeWidth(0);
            group.getChildren().add(clearer2);
        }

        // gleam
        {
            Color gleamColor = Color.rgb(225, 72, 81);

            Path gleam = new Path(
                    new MoveTo(163, 72),
                    new CubicCurveTo(150,67,160,55,177,70),
                    new CubicCurveTo(188,78,197,83,196,95),
                    new CubicCurveTo(180,97,173,77,163,72)
                    );
            gleam.setFill(gleamColor);
            gleam.setStrokeWidth(0);
            group.getChildren().add(gleam);
        }

        // eyes
        {
            Color white = Color.WHITE;

            Path eye1 = new Path(
                    new MoveTo(172, 130),
                    new LineTo(172,153),
                    new CubicCurveTo(120,162,130,123,137,121),
                    new LineTo(172,130)
            );
            eye1.setFill(white);
            eye1.setStrokeWidth(2);
            group.getChildren().add(eye1);

            Path eye2 = new Path(
                    new MoveTo(172, 130),
                    new LineTo(172,158),
                    new CubicCurveTo(217,163,210,126,202,123),
                    new LineTo(172,130)
            );
            eye2.setFill(white);
            eye2.setStrokeWidth(2);
            group.getChildren().add(eye2);

            // eyeballs
            group.getChildren().add(new Circle(157,136,7, Color.BLACK));
            group.getChildren().add(new Circle(183,137,6, Color.BLACK));
        }

        // eyebrows
        {
            Path eyebrows = new Path(
                    new MoveTo(172, 130),
                    new LineTo(118,117),
                    new LineTo(122,103),
                    new LineTo(171,120),
                    new LineTo(219,107),
                    new LineTo(224,120),
                    new LineTo(172,130)
            );
            eyebrows.setFill(black);
            eyebrows.setStrokeWidth(0);
            group.getChildren().add(eyebrows);
        }

        // beak
        {
            Color beakColor = Color.rgb(254, 184, 27);

            Path lowerBeak = new Path(
                    new MoveTo(200, 176),
                    new CubicCurveTo(170,210,164,210,142,170),
                    new LineTo(200,176)

            );
            lowerBeak.setFill(beakColor);
            lowerBeak.setStrokeWidth(2);
            group.getChildren().add(lowerBeak);

            Path upperBeak = new Path(
                    new MoveTo(211, 177),
                    new CubicCurveTo(131,179,129,170,154,151),
                    new CubicCurveTo(171,141,175,142,202,163),
                    new LineTo(211, 177)
            );
            upperBeak.setFill(beakColor);
            upperBeak.setStrokeWidth(2);
            group.getChildren().add(upperBeak);
        }
    }

    private void drawAngryBirdSource(Group group) throws Exception {
        ImageView imageView = new ImageView(new Image(new FileInputStream("C:\\Users\\Ihor\\Documents\\IdeaProjects\\CGSem6Lab3\\bird.png")));
        group.getChildren().add(imageView);
    }

}

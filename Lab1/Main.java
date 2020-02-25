package sample;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Group root = new Group();

        {
            Polygon rec = new Polygon(51,46 ,110,100 ,245,100 ,245,160 ,51,160);
            root.getChildren().add(rec);
            rec.setFill(Color.CYAN);
        }
        {
            Line rec = new Line(90,220,196,160);
            root.getChildren().add(rec);
            rec.setFill(Color.BLACK);
        }
        {
            Line rec = new Line(206,220,100,160);
            root.getChildren().add(rec);
            rec.setFill(Color.BLACK);
        }
        {
            Circle rec = new Circle(90,220,24);
            root.getChildren().add(rec);
            rec.setFill(Color.DARKVIOLET);
        }
        {
            Circle rec = new Circle(206,220,24);
            root.getChildren().add(rec);
            rec.setFill(Color.DARKVIOLET);
        }
        {
            Line rec = new Line(245,100, 245+40,100-40);
            root.getChildren().add(rec);
            rec.setFill(Color.BLACK);
        }

        primaryStage.setScene(new Scene(root, 300, 250, Color.ORANGE));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

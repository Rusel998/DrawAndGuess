package ru.itis.drawandguess.gameInterface;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class DrawAndGuess extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        canvas.setOnMouseDragged(event -> {
            double x = event.getX();
            double y = event.getY();
            gc.fillOval(x,y,5,5);
        });

        VBox root = new VBox(canvas);
        Scene scene = new Scene(root,800,600);

        stage.setTitle("Draw & Guess");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}

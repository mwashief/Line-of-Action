package loa;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.concurrent.ExecutionException;


public class Main extends Application {
    public static Match match;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("LoA");
        primaryStage.setHeight(600);
        primaryStage.setOnCloseRequest(event -> {
                    Platform.exit();
                    System.exit(0);
                }
        );
        match = new Match(8, primaryStage, new ManualPlayer(Piece.BLACK), new AIPlayer(Piece.WHITE));
        match.refresh(match.getBoardScene());
        new Thread(() -> {
            try {
                match.continueMatch();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        ).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

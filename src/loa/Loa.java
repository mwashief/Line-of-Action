package loa;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;


public class Loa extends Application {
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
        match = new Match(new AIPlayer(Piece.BLACK), new AIPlayer(Piece.WHITE));
        match.refresh(primaryStage, match.getBoardScene());
        Platform.runLater(() -> match.continueMatch(primaryStage));
    }

    public static void main(String[] args) {
        launch(args);
    }
}

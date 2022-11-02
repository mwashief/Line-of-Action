package loa;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;


/**
 * @author Md. Washief Hossain
 */
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
        match = new Match(8, primaryStage, new AIPlayer(Piece.BLACK), new AIPlayer(Piece.WHITE));
        match.refresh(match.getBoardScene(), false);
        new Thread(() -> match.continueMatch()).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

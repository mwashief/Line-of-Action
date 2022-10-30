package loa;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Optional;


public class Main extends Application {
    public static Stage gStage;
    public static Board gBoard;
    final public static int dimension = 5;
    public static GameState gameState;
    public static Player[] players = new Player[2];
    public static Player currentPlayer;

    @Override
    public void start(Stage primaryStage) {
        gStage = primaryStage;
        primaryStage.setTitle("LoA");
        primaryStage.setHeight(600);
        gBoard = new Board(dimension);
        gameState = new GameState(dimension);
        players[0] = new ManualPlayer(Piece.BLACK);
        players[1] = new ManualPlayer(Piece.WHITE);
        currentPlayer = players[0];

        refresh(primaryStage, getBoardScene());
        Platform.runLater(() -> routine(-1, -1));
    }

    public static void refresh(Stage stage, Scene scene) {
        stage.minWidthProperty().bind(scene.heightProperty());
        stage.minHeightProperty().bind(scene.widthProperty());
        stage.setScene(scene);
        stage.setTitle(currentPlayer.getPiece().getName() + "'s turn");
        stage.show();
    }

    public static Scene getBoardScene() {
        return new Scene((Parent) getBoardNode(gBoard));
    }

    public static Node getBoardNode(Board boardObj) {
        AnchorPane gameRoot = new AnchorPane();
        Node board = boardObj.getNode();
        AnchorPane.setBottomAnchor(board, 0d);
        AnchorPane.setRightAnchor(board, 0d);
        AnchorPane.setTopAnchor(board, 0d);
        AnchorPane.setLeftAnchor(board, 0d);
        gameRoot.getChildren().add(board);
        return gameRoot;
    }

    public static void routine(int i, int j) {
        gBoard.makePlain();
        if (currentPlayer instanceof AIPlayer && !gameState.isLegal(i, j)) {
            Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> result = currentPlayer.move(new GameState(gameState), gBoard);
            int i1 = result.getKey().getKey();
            int j1 = result.getKey().getValue();
            int i2 = result.getValue().getKey();
            int j2 = result.getValue().getValue();

            gBoard.removePiece(i1, j1);
            gBoard.addPiece(i2, j2, currentPlayer.getPiece());
            System.out.println(currentPlayer.getPiece() + " moved from (" + i1 + "," + j1 + ") to (" + i2 + "," + j2 + ")");
            gameState.transferPiece(i1, j1, i2, j2);


            if (gameState.isWon(currentPlayer.getPiece())) {
                refresh(gStage, getWinningScene(currentPlayer.getPiece()));
                return;
            } else if (gameState.isWon(currentPlayer.getPiece().getOtherPiece())) {
                refresh(gStage, getWinningScene(currentPlayer.getPiece().getOtherPiece()));
                return;
            }
            currentPlayer = currentPlayer == players[0] ? players[1] : players[0];
            Platform.runLater(() -> routine(-1, -1));
            refresh(gStage, getBoardScene());

        } else if (currentPlayer instanceof AIPlayer || !gameState.isLegal(i, j)) { //do nothing
        } else if (Optional.of(currentPlayer.getPiece()).equals(gameState.getState(i, j))) {
            ArrayList<ArrayList<Pair<Integer, Integer>>> all = gameState.findAll(i, j);
            if (all != null) {
                gBoard.addSelected(i, j);
                for (Pair<Integer, Integer> p : all.get(0)) {
                    gBoard.addInPath(p.getKey(), p.getValue());
                }
                for (Pair<Integer, Integer> p : all.get(1)) {
                    gBoard.addEnd(p.getKey(), p.getValue());
                }
                for (Pair<Integer, Integer> p : all.get(2)) {
                    gBoard.addCapturing(p.getKey(), p.getValue());
                }
                ((ManualPlayer) currentPlayer).setDestination(all.get(1));
                ((ManualPlayer) currentPlayer).setSource(new Pair<>(i, j));
            }
            refresh(gStage, getBoardScene());
        } else {
            boolean given = false;
            for (Pair<Integer, Integer> p : ((ManualPlayer) currentPlayer).getDestination()) {
                if (i == p.getKey() && j == p.getValue()) {
                    gBoard.removePiece(((ManualPlayer) currentPlayer).getX(), ((ManualPlayer) currentPlayer).getY());
                    gBoard.addPiece(i, j, currentPlayer.getPiece());
                    gameState.transferPiece(((ManualPlayer) currentPlayer).getX(), ((ManualPlayer) currentPlayer).getY(), i, j);

                    if (gameState.isWon(currentPlayer.getPiece())) {
                        refresh(gStage, getWinningScene(currentPlayer.getPiece()));
                        return;
                    } else if (gameState.isWon(currentPlayer.getPiece().getOtherPiece())) {
                        refresh(gStage, getWinningScene(currentPlayer.getPiece().getOtherPiece()));
                        return;
                    }
                    given = true;
                    break;
                }
            }
            ((ManualPlayer) currentPlayer).setDestination(new ArrayList<>());
            ((ManualPlayer) currentPlayer).setSource(null);

            if (given) currentPlayer = currentPlayer == players[0] ? players[1] : players[0];
            Platform.runLater(() -> routine(-1, -1));
            refresh(gStage, getBoardScene());
        }
    }

    public static Scene getWinningScene(Piece piece) {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        Label label = new Label(piece.getName() + " Won");
        label.setFont(new Font(35.0));
        vBox.getChildren().add(label);
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(getBoardNode(gBoard), vBox);
        return new Scene(stackPane);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

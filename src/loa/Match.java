package loa;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Match {
    public int dimension;
    public Board board;
    public Stage stage;
    public GameState gameState;
    public Player[] players = new Player[2];
    public Player currentPlayer;

    Match(int dimension, Stage stage, Player player1, Player player2) {
        this.dimension = dimension;
        this.stage = stage;
        if (player2.piece == player1.piece)
            player2.piece = player1.piece.getOtherPiece();

        board = new Board(this);
        gameState = new GameState(dimension);
        players[0] = player1;
        players[1] = player2;
        currentPlayer = players[0];
    }


    void switchPlayer() {
        currentPlayer = currentPlayer == players[0] ? players[1] : players[0];
    }

    void continueMatch() {
        if (gameState.isWon(currentPlayer.piece)) {
            Platform.runLater(() -> refresh(getWinningScene(currentPlayer.getPiece()), true));
            System.out.println(currentPlayer.getPiece().getName() + " Won");
            return;
        }

        var futureTask = new FutureTask<>(currentPlayer.move(gameState, board));
        new Thread(futureTask).start();

        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> result;
        try {
            result = futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        int sourceRow = result.getKey().getKey();
        int sourceColumn = result.getKey().getValue();
        int targetRow = result.getValue().getKey();
        int targetColumn = result.getValue().getValue();

        gameState.transferPiece(sourceRow, sourceColumn, targetRow, targetColumn);
        System.out.println(currentPlayer.getPiece() + " moved from (" + sourceRow + "," + sourceColumn + ") to (" + targetRow + "," + targetColumn + ")");

        board.removePiece(sourceRow, sourceColumn);
        board.addPiece(targetRow, targetColumn, currentPlayer.getPiece());

        if (gameState.isWon(currentPlayer.piece)) {
            Platform.runLater(() -> refresh(getWinningScene(currentPlayer.getPiece()), true));
            System.out.println(currentPlayer.getPiece().getName() + " Won");
            return;
        }

        switchPlayer();
        Platform.runLater(() -> refresh(getBoardScene(), false));

        continueMatch();

    }

    public Scene getWinningScene(Piece piece) {
        Label label = new Label(piece.getName() + " Won");
        label.setFont(new Font(35.0));
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(board.getNode(), label);
        stackPane.prefWidthProperty().bind(stage.widthProperty());
        stackPane.prefWidthProperty().bind(stage.widthProperty());
        return new Scene(stackPane);
    }


    public void refresh(Scene scene, boolean isWinning) {
        stage.minWidthProperty().bind(scene.heightProperty());
        stage.minHeightProperty().bind(scene.widthProperty());
        stage.setScene(scene);
        if (isWinning)
            stage.setTitle(currentPlayer.getPiece().getName() + " Won" + (currentPlayer instanceof AIPlayer ? "(Bot)" : ""));
        else
            stage.setTitle(currentPlayer.getPiece().getName() + "'s turn" + (currentPlayer instanceof AIPlayer ? "(Bot)" : ""));

        stage.show();
    }

    public Scene getBoardScene() {
        return new Scene((Parent) board.getNode());
    }
}

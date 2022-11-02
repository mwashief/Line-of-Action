package loa;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Match {
    public int dimension = 8;
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

    void continueMatch() throws ExecutionException, InterruptedException {
        if (gameState.isWon(currentPlayer.piece)) {
            Platform.runLater(() -> refresh(getWinningScene(currentPlayer.getPiece())));
            System.out.println(currentPlayer.getPiece().getName() + " Won");
            return;
        }
        if (gameState.isWon(currentPlayer.piece.getOtherPiece())) {
            Platform.runLater(() -> refresh(getWinningScene(currentPlayer.getPiece().getOtherPiece())));
            System.out.println(currentPlayer.getPiece().getOtherPiece().getName() + " Won");
            return;
        }

        var futureTask = new FutureTask<>(currentPlayer.move(gameState, board));
        new Thread(futureTask).start();

        var result = futureTask.get();

        int i1 = result.getKey().getKey();
        int j1 = result.getKey().getValue();
        int i2 = result.getValue().getKey();
        int j2 = result.getValue().getValue();


        gameState.transferPiece(i1, j1, i2, j2);
        System.out.println(currentPlayer.getPiece() + " moved from (" + i1 + "," + j1 + ") to (" + i2 + "," + j2 + ")");


        board.removePiece(i1, j1);
        board.addPiece(i2, j2, currentPlayer.getPiece());

        switchPlayer();
        Platform.runLater(() -> {
            refresh(getBoardScene());
        });
        try {
            continueMatch();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Scene getWinningScene(Piece piece) {
        Label label = new Label(piece.getName() + " Won");
        label.setFont(new Font(35.0));
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(board.getNode(), label);
        stackPane.prefWidthProperty().bind(stage.widthProperty());
        stackPane.prefWidthProperty().bind(stage.widthProperty());
        return new Scene((Parent) stackPane);
    }


    public void refresh(Scene scene) {
        stage.minWidthProperty().bind(scene.heightProperty());
        stage.minHeightProperty().bind(scene.widthProperty());
        stage.setScene(scene);
        stage.setTitle(currentPlayer.getPiece().getName() + "'s turn" + (currentPlayer instanceof AIPlayer ? "(Bot)" : ""));
        stage.show();
    }

    public Scene getBoardScene() {
        return new Scene((Parent) board.getNode());
    }
}

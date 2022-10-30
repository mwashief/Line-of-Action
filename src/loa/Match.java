package loa;

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

public class Match {
    public int dimension = 8;
    public Board board;
    public GameState gameState;
    public Player[] players = new Player[2];
    public Player currentPlayer;

    Match(Player player1, Player player2) {
        if (player2.piece == player1.piece)
            player2.piece = player1.piece.getOtherPiece();

        board = new Board(dimension);
        gameState = new GameState(dimension);
        players[0] = player1;
        players[1] = player2;
        currentPlayer = players[0];
    }

    Match(int dimension) {
        this.dimension = dimension;
        board = new Board(dimension);
        gameState = new GameState(dimension);
        players[0] = new AIPlayer(Piece.BLACK);
        players[1] = new AIPlayer(Piece.WHITE);
        currentPlayer = players[0];
    }

    void switchPlayer() {
        currentPlayer = currentPlayer == players[0] ? players[1] : players[0];
    }

    void continueMatch(Stage stage) {
        if (gameState.isWon(currentPlayer.piece)) {
            refresh(stage, getWinningScene(currentPlayer.getPiece()));
            System.out.println(currentPlayer.getPiece().getName() + " Won");
            return;
        }
        if (gameState.isWon(currentPlayer.piece.getOtherPiece())) {
            refresh(stage, getWinningScene(currentPlayer.getPiece().getOtherPiece()));
            System.out.println(currentPlayer.getPiece().getOtherPiece().getName() + " Won");
            return;
        }

        var result = currentPlayer.move(new GameState(gameState), board);
        int i1 = result.getKey().getKey();
        int j1 = result.getKey().getValue();
        int i2 = result.getValue().getKey();
        int j2 = result.getValue().getValue();


        gameState.transferPiece(i1, j1, i2, j2);
        System.out.println(currentPlayer.getPiece() + " moved from (" + i1 + "," + j1 + ") to (" + i2 + "," + j2 + ")");


        board.removePiece(i1, j1);
        board.addPiece(i2, j2, currentPlayer.getPiece());

        refresh(stage, getBoardScene());


        switchPlayer();
        Platform.runLater(() -> continueMatch(stage));

    }

    public Scene getWinningScene(Piece piece) {
        var vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        Label label = new Label(piece.getName() + " Won");
        label.setFont(new Font(35.0));
        vBox.getChildren().add(label);
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(getBoardNode(), vBox);
        return new Scene(stackPane);
    }

    public void refresh(Stage stage, Scene scene) {
        stage.minWidthProperty().bind(scene.heightProperty());
        stage.minHeightProperty().bind(scene.widthProperty());
        stage.setScene(scene);
        stage.setTitle(currentPlayer.getPiece().getName() + "'s turn");
        stage.show();
    }

    public Scene getBoardScene() {
        return new Scene((Parent) getBoardNode());
    }

    public Node getBoardNode() {
        var gameRoot = new AnchorPane();
        var boardNode = board.getNode();
        AnchorPane.setBottomAnchor(boardNode, 0d);
        AnchorPane.setRightAnchor(boardNode, 0d);
        AnchorPane.setTopAnchor(boardNode, 0d);
        AnchorPane.setLeftAnchor(boardNode, 0d);
        gameRoot.getChildren().add(boardNode);
        return gameRoot;
    }

}

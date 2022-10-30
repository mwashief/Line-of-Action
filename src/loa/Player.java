package loa;

import javafx.util.Pair;

public abstract class Player {
    Piece piece;
    public abstract Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> move(GameState state, Board board);
    public Piece getPiece() {
        return piece;
    }
    public Player(Piece piece) {
        this.piece = piece;
    }
}

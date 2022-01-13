package loa;

import javafx.util.Pair;

public abstract class Player {
    int col;
    public abstract Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> move(GameState state);

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public Player(int col) {
        this.col = col;
    }
}

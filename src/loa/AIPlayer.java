package loa;

import javafx.util.Pair;

import java.util.ArrayList;

public class AIPlayer extends Player {

    public AIPlayer(int col) {
        super(col);
    }

    @Override
    public Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> move(GameState state) {
        int i1 = 0, j1 = 0, i2 = 0, j2 = 0;
        int alpha = Integer.MIN_VALUE, beta = Integer.MAX_VALUE;
        for (int i = 0; i < Main.dimension; i++)
            for (int j = 0; j < Main.dimension; j++)
                if (state.getState(i, j) == col) {
                    ArrayList<Pair<Integer, Integer>> nextMoves = state.findNext(i, j);
                    for (Pair<Integer, Integer> nextMove : nextMoves) {
                        GameState g = new GameState(state);
                        g.transferPiece(i, j, nextMove.getKey(), nextMove.getValue());
                        int x = minValue(g, alpha + 1, beta, -2);
                        if (x > alpha) {
                            alpha = x;
                            i1 = i;
                            j1 = j;
                            i2 = nextMove.getKey();
                            j2 = nextMove.getValue();
                        }
                    }
                }
        return new Pair<>(new Pair<>(i1, j1), new Pair<>(i2, j2));
    }

    int maxValue(GameState state, int alpha, int beta, int level) {
        if (state.isWon(5 - col))
            return Integer.MIN_VALUE + 1;
        if (state.isWon(col))
            return Integer.MAX_VALUE;
        if (level >= 0) return state.utility(col) - state.utility(5 - col);


        int v = Integer.MIN_VALUE + 1;
        for (int i = 0; i < Main.dimension; i++)
            for (int j = 0; j < Main.dimension; j++)
                if (state.getState(i, j) == col) {
                    ArrayList<Pair<Integer, Integer>> nextMoves = state.findNext(i, j);
                    for (Pair<Integer, Integer> nextMove : nextMoves) {
                        GameState g = new GameState(state);
                        g.transferPiece(i, j, nextMove.getKey(), nextMove.getValue());
                        v = Integer.max(v, minValue(g, alpha, beta, level + 1) - 1);
                        if (v >= beta) return v;
                        alpha = Integer.max(alpha, v);
                    }
                }
        return v;
    }

    int minValue(GameState state, int alpha, int beta, int level) {
        if (state.isWon(col))
            return Integer.MAX_VALUE;
        if (state.isWon(5 - col))
            return Integer.MIN_VALUE + 1;
        if (level >= 0) return state.utility(col) - state.utility(5 - col);

        int v = Integer.MAX_VALUE;
        for (int i = 0; i < Main.dimension; i++)
            for (int j = 0; j < Main.dimension; j++)
                if (state.getState(i, j) == 5 - col) {
                    ArrayList<Pair<Integer, Integer>> nextMoves = state.findNext(i, j);
                    for (Pair<Integer, Integer> nextMove : nextMoves) {
                        GameState g = new GameState(state);
                        g.transferPiece(i, j, nextMove.getKey(), nextMove.getValue());
                        v = Integer.min(v, maxValue(g, alpha, beta, level + 1));
                        if (v <= alpha) return v;
                        beta = Integer.min(beta, v);
                    }
                }
        return v;
    }
}

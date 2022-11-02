package loa;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Callable;

public class AIPlayer extends Player {

    public AIPlayer(Piece piece) {
        super(piece);
    }

    @Override
    public Callable<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> move(GameState state, Board board) {
        return () -> {
            int i1 = 0, j1 = 0, i2 = 0, j2 = 0;
            int alpha = Integer.MIN_VALUE, beta = Integer.MAX_VALUE;
            for (int i = 0; i < state.dimension; i++)
                for (int j = 0; j < state.dimension; j++)
                    if (state.getState(i, j).equals(Optional.of(piece))) {
                        ArrayList<Pair<Integer, Integer>> nextMoves = state.findNext(i, j);
                        for (Pair<Integer, Integer> nextMove : nextMoves) {
                            GameState g = new GameState(state);
                            g.transferPiece(i, j, nextMove.getKey(), nextMove.getValue());
                            int x = minValue(g, alpha + 1, beta, 3);
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
        };
    }

    int maxValue(GameState state, int alpha, int beta, int level) {
        if (state.isWon(piece.getOtherPiece()))
            return Integer.MIN_VALUE + 1;
        if (state.isWon(piece))
            return Integer.MAX_VALUE;
        if (level <= 0) return utility(state, piece) - utility(state, piece.getOtherPiece());


        int v = Integer.MIN_VALUE + 1;
        for (int i = 0; i < state.dimension; i++)
            for (int j = 0; j < state.dimension; j++)
                if (state.getState(i, j).equals(Optional.of(piece))) {
                    ArrayList<Pair<Integer, Integer>> nextMoves = state.findNext(i, j);
                    for (Pair<Integer, Integer> nextMove : nextMoves) {
                        GameState g = new GameState(state);
                        g.transferPiece(i, j, nextMove.getKey(), nextMove.getValue());
                        v = Integer.max(v, minValue(g, alpha, beta, level - 1) - 1);
                        if (v >= beta) return v;
                        alpha = Integer.max(alpha, v);
                    }
                }
        return v;
    }

    int minValue(GameState state, int alpha, int beta, int level) {
        if (state.isWon(piece))
            return Integer.MAX_VALUE;
        if (state.isWon(piece.getOtherPiece()))
            return Integer.MIN_VALUE + 1;
        if (level <= 0) return utility(state, piece) - utility(state, piece.getOtherPiece());

        int v = Integer.MAX_VALUE;
        for (int i = 0; i < state.dimension; i++)
            for (int j = 0; j < state.dimension; j++)
                if (state.getState(i, j).equals(Optional.of(piece.getOtherPiece()))) {
                    ArrayList<Pair<Integer, Integer>> nextMoves = state.findNext(i, j);
                    for (Pair<Integer, Integer> nextMove : nextMoves) {
                        GameState g = new GameState(state);
                        g.transferPiece(i, j, nextMove.getKey(), nextMove.getValue());
                        v = Integer.min(v, maxValue(g, alpha, beta, level - 1));
                        if (v <= alpha) return v;
                        beta = Integer.min(beta, v);
                    }
                }
        return v;
    }

    int utility(GameState gameState, Piece piece) {
        Pair<Integer, Integer> CM = gameState.getCM(piece);
        int x = CM.getKey();
        int y = CM.getValue();
        Pair<Integer, Integer> comp = gameState.getComponent(piece);
        int compSize = comp.getValue();
        int compNo = comp.getKey();
        return gameState.getDensity(x, y, piece)
                + gameState.getQuad(x, y, piece)
                + gameState.dimension * compSize
                + 10 * gameState.getMobility(piece)
                + 50 * gameState.getPST(piece)
                - gameState.getArea(piece) / 2
                - 100 * compNo;
    }
}

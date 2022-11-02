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
            int sourceRow = 0, sourceColumn = 0, targetRow = 0, targetColumn = 0;
            int alpha = Integer.MIN_VALUE, beta = Integer.MAX_VALUE;
            for (int row = 0; row < state.getDimension(); row++)
                for (int column = 0; column < state.getDimension(); column++)
                    if (state.getState(row, column).equals(Optional.of(piece))) {
                        ArrayList<Pair<Integer, Integer>> nextMoves = state.findAll(row, column).get(1);
                        for (Pair<Integer, Integer> nextMove : nextMoves) {
                            GameState g = new GameState(state);
                            g.transferPiece(row, column, nextMove.getKey(), nextMove.getValue());
                            int x = minValue(g, alpha + 1, beta, 3);
                            if (x > alpha) {
                                alpha = x;
                                sourceRow = row;
                                sourceColumn = column;
                                targetRow = nextMove.getKey();
                                targetColumn = nextMove.getValue();
                            }
                        }
                    }
            return new Pair<>(new Pair<>(sourceRow, sourceColumn), new Pair<>(targetRow, targetColumn));
        };
    }

    int maxValue(GameState state, int alpha, int beta, int level) {
        if (state.isWon(piece.getOtherPiece()))
            return Integer.MIN_VALUE + 1;
        if (state.isWon(piece))
            return Integer.MAX_VALUE;
        if (level <= 0) return utility(state, piece) - utility(state, piece.getOtherPiece());


        int v = Integer.MIN_VALUE + 1;
        for (int row = 0; row < state.getDimension(); row++)
            for (int column = 0; column < state.getDimension(); column++)
                if (state.getState(row, column).equals(Optional.of(piece))) {
                    ArrayList<Pair<Integer, Integer>> nextMoves = state.findAll(row, column).get(1);
                    for (Pair<Integer, Integer> nextMove : nextMoves) {
                        GameState g = new GameState(state);
                        g.transferPiece(row, column, nextMove.getKey(), nextMove.getValue());
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
        for (int row = 0; row < state.getDimension(); row++)
            for (int column = 0; column < state.getDimension(); column++)
                if (state.getState(row, column).equals(Optional.of(piece.getOtherPiece()))) {
                    ArrayList<Pair<Integer, Integer>> nextMoves = state.findAll(row, column).get(1);
                    for (Pair<Integer, Integer> nextMove : nextMoves) {
                        GameState g = new GameState(state);
                        g.transferPiece(row, column, nextMove.getKey(), nextMove.getValue());
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
                + gameState.getDimension() * compSize
                + 10 * gameState.getMobility(piece)
                + 50 * gameState.getPST(piece)
                - gameState.getArea(piece) / 2
                - 100 * compNo;
    }
}

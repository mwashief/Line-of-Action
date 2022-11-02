package loa;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;


public class ManualPlayer extends Player {
    ArrayList<Pair<Integer, Integer>> destination;
    Pair<Integer, Integer> source;
    Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> chosenMove;


    public ManualPlayer(Piece piece) {
        super(piece);
        destination = new ArrayList<>();
        chosenMove = null;
    }

    @Override
    public Callable<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> move(GameState state, Board board) {
        return () -> {
            while (true) {
                synchronized (this) {
                    if (chosenMove != null) {
                        var returnValue = chosenMove;
                        chosenMove = null;
                        destination = new ArrayList<>();
                        return returnValue;
                    }
                }
            }
        };
    }

}

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
                        return returnValue;
                    }
                }
            }
        };
    }


    public ArrayList<Pair<Integer, Integer>> getDestination() {
        return destination;
    }

    public void setDestination(ArrayList<Pair<Integer, Integer>> destination) {
        this.destination = destination;
    }

    public void setSource(Pair<Integer, Integer> source) {
        this.source = source;
    }

    public int getX() {
        return source.getKey();
    }

    public int getY() {
        return source.getValue();
    }
}

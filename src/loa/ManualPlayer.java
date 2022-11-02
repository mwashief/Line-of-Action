package loa;

import javafx.util.Pair;

import java.util.ArrayList;


public class ManualPlayer extends Player {
    ArrayList<Pair<Integer, Integer>> targets;
    Pair<Integer, Integer> source;
    Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> chosenMove;


    public ManualPlayer(Piece piece) {
        super(piece);
        targets = new ArrayList<>();
        chosenMove = null;
    }

    @Override
    public Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> move(GameState state, Board board) {
        while (true) {
            synchronized (this) {
                if (chosenMove != null) {
                    var returnValue = chosenMove;
                    chosenMove = null;
                    targets = new ArrayList<>();
                    return returnValue;
                }
            }
        }
    }

}

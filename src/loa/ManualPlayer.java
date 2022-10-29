package loa;

import javafx.util.Pair;

import java.util.ArrayList;

public class ManualPlayer extends Player {
    ArrayList<Pair<Integer, Integer>> destination;
    Pair<Integer, Integer> source;

    public ManualPlayer(Piece piece) {
        super(piece);
        destination = new ArrayList<>();
    }

    @Override
    public Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> move(GameState state) {
        return null;
    }

    public ArrayList<Pair<Integer, Integer>> getDestination() {
        return destination;
    }

    public void setDestination(ArrayList<Pair<Integer, Integer>> destination) {
        this.destination = destination;
    }

    public Pair<Integer, Integer> getSource() {
        return source;
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

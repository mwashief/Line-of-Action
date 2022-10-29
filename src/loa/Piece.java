package loa;

import javafx.scene.paint.Color;

public enum Piece {
    WHITE(Color.rgb(255, 255, 255)),
    BLACK(Color.rgb(0, 0, 0));

    public final Color pieceColor;

    private Piece(Color color) {
        this.pieceColor = color;
    }

    public Piece getOtherPiece() {
        if (this == WHITE) return BLACK;
        return WHITE;
    }

    public String getName() {
        return switch (this) {
            case WHITE -> "White";
            case BLACK -> "Black";
        };
    }
}

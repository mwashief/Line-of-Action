package loa;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Optional;

public class Board {

    private enum Cell {
        WHITE_CELL     (Color.rgb(171, 143, 123)),
        BLACK_CELL     (Color.rgb(224, 172, 134)),
        CAPTURING_CELL (Color.rgb(255, 0,   0,   .5)),
        END_CELL       (Color.rgb(0,   255, 0,   .5)),
        IN_PATH_CELL   (Color.rgb(0,   0,   255, .2)),
        SELECTED_CELL  (Color.rgb(0,   255, 0,   .2));

        public final Color cellColor;

        Cell(Color color) {
            this.cellColor = color;
        }
    }


    private static class CheckerBox {
        public Optional<Cell> maybeOverriddenCell = Optional.empty();
        public Optional<Piece> maybePiece = Optional.empty();

    }

    private final Match match;

    private final int dimension;
    private CheckerBox[][] grid;

    public Board(Match match) {
        this.dimension = match.dimension;
        this.match = match;
        grid = new CheckerBox[dimension][dimension];

        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                grid[i][j] = new CheckerBox();
        for (int i = 1; i < dimension - 1; i++) {
            addPiece(i, 0,             Piece.WHITE);
            addPiece(i, dimension - 1, Piece.WHITE);
            addPiece(0, i,             Piece.BLACK);
            addPiece(dimension - 1, i, Piece.BLACK);
        }
    }

    public Node getNode() {
        GridPane gridPane = new GridPane();

        StackPane[][] stackPanes = new StackPane[dimension][dimension];
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++) {
                stackPanes[i][j] = new StackPane();
                Rectangle rectangle = new Rectangle();
                rectangle.widthProperty().bind(gridPane.widthProperty().divide(dimension));
                rectangle.heightProperty().bind(gridPane.heightProperty().divide(dimension));
                rectangle.setFill(
                        grid[i][j].maybeOverriddenCell
                                .map(cell -> cell.cellColor)
                                .orElse((i + j) % 2 == 0 ? Cell.WHITE_CELL.cellColor : Cell.BLACK_CELL.cellColor)
                );
                stackPanes[i][j].getChildren().add(rectangle);

                int finalI = i;
                int finalJ = j;

                grid[i][j].maybePiece.ifPresent(piece -> {
                    Circle circle = new Circle();
                    circle.setFill(piece.pieceColor);
                    circle.radiusProperty().bind(gridPane.widthProperty().divide(2.4 * dimension));
                    stackPanes[finalI][finalJ].getChildren().add(circle);
                });
                stackPanes[i][j].addEventHandler(
                        MouseEvent.MOUSE_PRESSED,
                        event -> Platform.runLater(
                                () -> {
                                    if (this.match.currentPlayer instanceof ManualPlayer currentPlayer) {
                                        System.out.println("clicked on " + finalI + ", " + finalJ);
                                        synchronized (currentPlayer) {
                                            if (currentPlayer.chosenMove == null) {
                                                if (currentPlayer.destination.stream().anyMatch(cell -> cell.getKey() == finalI && cell.getValue() == finalJ)) {
                                                    this.makePlain();
                                                    currentPlayer.chosenMove = new Pair<>(currentPlayer.source, new Pair<>(finalI, finalJ));
                                                } else {
                                                    this.makePlain();
                                                    if (this.grid[finalI][finalJ].maybePiece.equals(Optional.of(currentPlayer.piece))) {
                                                        var all = this.match.gameState.findAll(finalI, finalJ);
                                                        this.overrideCell(finalI, finalJ, Cell.SELECTED_CELL);
                                                        for (Pair<Integer, Integer> p : all.get(0)) {
                                                            this.overrideCell(p.getKey(), p.getValue(), Cell.IN_PATH_CELL);
                                                        }
                                                        for (Pair<Integer, Integer> p : all.get(1)) {
                                                            this.overrideCell(p.getKey(), p.getValue(), Cell.END_CELL);
                                                        }
                                                        for (Pair<Integer, Integer> p : all.get(2)) {
                                                            this.overrideCell(p.getKey(), p.getValue(), Cell.CAPTURING_CELL);
                                                        }

                                                        currentPlayer.source = new Pair<>(finalI, finalJ);
                                                        currentPlayer.destination = all.get(1);
                                                    } else {
                                                        currentPlayer.destination = new ArrayList<>();
                                                    }
                                                    this.match.refresh(match.getBoardScene());
                                                }
                                            }
                                        }
                                    }
                                }

                        )
                );
                gridPane.add(stackPanes[i][j], j, i);
            }
        return gridPane;
    }

    void overrideCell(int i, int j, Cell cell)
    {
        this.grid[i][j].maybeOverriddenCell = Optional.of(cell);
    }

    void removeOverriddenBackground(int i, int j) {
        grid[i][j].maybeOverriddenCell = Optional.empty();
    }

    void makePlain() {
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                removeOverriddenBackground(i, j);
    }

    void addPiece(int i, int j, Piece piece) {
        grid[i][j].maybePiece = Optional.of(piece);
    }
    void removePiece(int i, int j) {
        grid[i][j].maybePiece = Optional.empty();
    }
}

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
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public class Board {

    private enum Cell {
        WHITE_CELL(Color.rgb(171, 143, 123)),
        BLACK_CELL(Color.rgb(224, 172, 134)),
        SOURCE_CELL(Color.rgb(0, 255, 0, .2)),
        IN_PATH_CELL(Color.rgb(0, 0, 255, .2)),
        TARGET_CELL(Color.rgb(0, 255, 0, .5)),
        CAPTURING_CELL(Color.rgb(255, 0, 0, .5));

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
            addPiece(i, 0, Piece.WHITE);
            addPiece(0, i, Piece.BLACK);
            addPiece(i, dimension - 1, Piece.WHITE);
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
                                                this.makePlain();
                                                if (currentPlayer.destination.stream().anyMatch(cell -> cell.getKey() == finalI && cell.getValue() == finalJ)) {
                                                    currentPlayer.chosenMove = new Pair<>(currentPlayer.source, new Pair<>(finalI, finalJ));
                                                } else {
                                                    if (this.grid[finalI][finalJ].maybePiece.equals(Optional.of(currentPlayer.piece))) {
                                                        this.overrideCell(finalI, finalJ, Cell.SOURCE_CELL);

                                                        var relevantCells = this.match.gameState.findAll(finalI, finalJ);
                                                        final var cellTypes = Arrays.asList(Cell.IN_PATH_CELL, Cell.TARGET_CELL, Cell.CAPTURING_CELL);
                                                        IntStream.range(0, relevantCells.size()).forEach(idx ->
                                                                relevantCells.get(idx).forEach(cell -> this.overrideCell(cell.getKey(), cell.getValue(), cellTypes.get(idx)))
                                                        );

                                                        currentPlayer.source = new Pair<>(finalI, finalJ);
                                                        currentPlayer.destination = relevantCells.get(1);
                                                    } else {
                                                        currentPlayer.destination = new ArrayList<>();
                                                    }
                                                    this.match.refresh(match.getBoardScene(), false);
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

    void overrideCell(int i, int j, Cell cell) {
        this.grid[i][j].maybeOverriddenCell = Optional.of(cell);
    }

    void removeOverriddenCells(int i, int j) {
        grid[i][j].maybeOverriddenCell = Optional.empty();
    }

    void makePlain() {
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                removeOverriddenCells(i, j);
    }

    void addPiece(int i, int j, Piece piece) {
        grid[i][j].maybePiece = Optional.of(piece);
    }

    void removePiece(int i, int j) {
        grid[i][j].maybePiece = Optional.empty();
    }
}

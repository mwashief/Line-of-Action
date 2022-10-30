package loa;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.Optional;

public class Board {
    private final Color WHITE_CELL = Color.rgb(171, 143, 123);
    private final Color BLACK_CELL = Color.rgb(224, 172, 134);
    private final Color CAPTURING_CELL = Color.rgb(255, 0, 0, .5);
    private final Color END_CELL = Color.rgb(0, 255, 0, .5);
    private final Color IN_PATH_CELL = Color.rgb(0, 0, 255, .2);
    private final Color SELECTED_CELL = Color.rgb(0, 255, 0, .2);

    private class CheckerBox {
        public Optional<Color> overriddenBackgroundColor = Optional.empty();
        public Optional<Color> piece = Optional.empty();

    }

    private final int dimension;
    private CheckerBox[][] grid;

    public Board(int dimension) {
        this.dimension = dimension;
        grid = new CheckerBox[dimension][dimension];

        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                grid[i][j] = new CheckerBox();
        for (int i = 1; i < dimension - 1; i++) {
            addWhitePiece(i, 0);
            addWhitePiece(i, dimension - 1);
            addBlackPiece(0, i);
            addBlackPiece(dimension - 1, i);
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
                        grid[i][j].overriddenBackgroundColor
                                .orElse((i + j) % 2 == 0 ? WHITE_CELL : BLACK_CELL)
                );
                stackPanes[i][j].getChildren().add(rectangle);

                int finalI = i;
                int finalJ = j;

                grid[i][j].piece.ifPresent(color -> {
                    Circle circle = new Circle();
                    circle.setFill(color);
                    circle.radiusProperty().bind(gridPane.widthProperty().divide(2.4 * dimension));
                    stackPanes[finalI][finalJ].getChildren().add(circle);
                });
                stackPanes[i][j].addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        Platform.runLater(() -> Main.routine(finalI, finalJ));
                    }
                });
                gridPane.add(stackPanes[i][j], j, i);
            }
        return gridPane;
    }

    void addBlackPiece(int i, int j) {
        addPiece(i, j, Piece.BLACK);
    }

    void addWhitePiece(int i, int j) {
        addPiece(i, j, Piece.WHITE);
    }

    void addInPath(int i, int j) {
        grid[i][j].overriddenBackgroundColor = Optional.of(IN_PATH_CELL);
    }

    void addCapturing(int i, int j) {
        grid[i][j].overriddenBackgroundColor = Optional.of(CAPTURING_CELL);
    }

    void addSelected(int i, int j) {
        grid[i][j].overriddenBackgroundColor = Optional.of(SELECTED_CELL);
    }

    void addEnd(int i, int j) {
        grid[i][j].overriddenBackgroundColor = Optional.of(END_CELL);
    }

    void removePiece(int i, int j) {
        grid[i][j].piece = Optional.empty();
    }

    void removeOverriddenBackground(int i, int j) {
        grid[i][j].overriddenBackgroundColor = Optional.empty();
    }


    void makePlain() {
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                removeOverriddenBackground(i, j);
    }

    void addPiece(int i, int j, Piece piece) {
        grid[i][j].piece = Optional.of(piece.pieceColor);
    }

}

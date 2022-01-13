package loa;


import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;


public class Board {
    private int dimension;
    private int[][] checkerBox;

    public Board(int dimension) {
        this.dimension = dimension;
        checkerBox = new int[dimension][dimension];
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                checkerBox[i][j] = 1;

        for (int i = 1; i < dimension - 1; i++) {
            addWhite(i, 0);
            addWhite(i, dimension - 1);
            addBlack(0, i);
            addBlack(dimension - 1, i);
        }
    }

    public Node getNode() {
        GridPane gridPane = new GridPane();

        StackPane[][] stackPanes = new StackPane[dimension][dimension];
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++) {
                stackPanes[i][j] = new StackPane();
                Rectangle checker = new Rectangle();
                checker.widthProperty().bind(gridPane.widthProperty().divide(dimension));
                checker.heightProperty().bind(gridPane.heightProperty().divide(dimension));
                checker.setFill((i + j) % 2 == 0 ? Color.rgb(224, 172, 134) : Color.rgb(171, 143, 123));
                stackPanes[i][j].getChildren().add(checker);
                if (checkerBox[i][j] % 7 == 0) {
                    Rectangle rectangle = new Rectangle();
                    rectangle.widthProperty().bind(gridPane.widthProperty().divide(dimension));
                    rectangle.heightProperty().bind(gridPane.heightProperty().divide(dimension));
                    rectangle.setFill(Color.rgb(255, 0, 100, .5));
                    stackPanes[i][j].getChildren().add(rectangle);
                } else if (checkerBox[i][j] % 13 == 0) {
                    Rectangle rectangle = new Rectangle();
                    rectangle.widthProperty().bind(gridPane.widthProperty().divide(dimension));
                    rectangle.heightProperty().bind(gridPane.heightProperty().divide(dimension));
                    rectangle.setFill(Color.rgb(0, 255, 200, .5));
                    stackPanes[i][j].getChildren().add(rectangle);
                } else if (checkerBox[i][j] % 5 == 0) {
                    Rectangle rectangle = new Rectangle();
                    rectangle.widthProperty().bind(gridPane.widthProperty().divide(dimension));
                    rectangle.heightProperty().bind(gridPane.heightProperty().divide(dimension));
                    rectangle.setFill(Color.rgb(0, 0, 255, .2));
                    stackPanes[i][j].getChildren().add(rectangle);
                } else if (checkerBox[i][j] % 11 == 0) {
                    Rectangle rectangle = new Rectangle();
                    rectangle.widthProperty().bind(gridPane.widthProperty().divide(dimension));
                    rectangle.heightProperty().bind(gridPane.heightProperty().divide(dimension));
                    rectangle.setFill(Color.rgb(0, 255, 0, .2));
                    stackPanes[i][j].getChildren().add(rectangle);
                }
                if (checkerBox[i][j] % 2 == 0) {
                    Circle circle = new Circle();
                    circle.setFill(Color.BLACK);
                    circle.radiusProperty().bind(gridPane.widthProperty().divide(2.4 * dimension));
                    stackPanes[i][j].getChildren().add(circle);
                } else if (checkerBox[i][j] % 3 == 0) {
                    Circle circle = new Circle();
                    circle.setFill(Color.WHITE);
                    circle.radiusProperty().bind(gridPane.widthProperty().divide(2.4 * dimension));
                    stackPanes[i][j].getChildren().add(circle);
                }
                int finalI = i;
                int finalJ = j;
                stackPanes[i][j].addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        doIt(finalI, finalJ);
                    }
                });
                gridPane.add(stackPanes[i][j], j, i);
            }
        return gridPane;
    }

    void addBlack(int i, int j) {
        while (checkerBox[i][j] % 3 == 0)
            checkerBox[i][j] /= 3;
        checkerBox[i][j] *= 2;
    }

    void addWhite(int i, int j) {
        while (checkerBox[i][j] % 2 == 0)
            checkerBox[i][j] /= 2;
        checkerBox[i][j] *= 3;
    }

    void addInPath(int i, int j) {
        checkerBox[i][j] *= 5;
    }

    void addCapturing(int i, int j) {
        checkerBox[i][j] *= 7;
    }

    void addSelected(int i, int j) {
        checkerBox[i][j] *= 11;
    }

    void addEnd(int i, int j) {
        checkerBox[i][j] *= 13;
    }

    void removePiece(int i, int j) {
        checkerBox[i][j] = 1;
    }

    void removeInPath(int i, int j) {
        while (checkerBox[i][j] % 5 == 0)
            checkerBox[i][j] /= 5;
    }

    void removeCapturing(int i, int j) {
        while (checkerBox[i][j] % 7 == 0)
            checkerBox[i][j] /= 7;
    }

    void removeSelected(int i, int j) {
        while (checkerBox[i][j] % 11 == 0)
            checkerBox[i][j] /= 11;
    }

    void removeEnd(int i, int j) {
        while (checkerBox[i][j] % 13 == 0)
            checkerBox[i][j] /= 13;
    }

    int getPiece(int i, int j) {
        if (checkerBox[i][j] % 2 == 0) return 2;
        if (checkerBox[i][j] % 3 == 0) return 3;
        return 0;
    }

    void makePlain() {
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++) {
                removeInPath(i, j);
                removeCapturing(i, j);
                removeSelected(i, j);
                removeEnd(i, j);
            }
    }

    void doIt(int i, int j) {
        Main.routine(i, j);
    }

    void addPiece(int i, int j, int k) {
        int other = 5 - k;
        while (checkerBox[i][j] % other == 0)
            checkerBox[i][j] /= other;
        checkerBox[i][j] *= k;
    }

}

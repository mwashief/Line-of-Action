package loa;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class GameState {
    private int dimension;
    private Optional<Piece>[][] grid;
    private int[] positiveSlope;
    private int[] negativeSlope;
    private int[] row;
    private int[] col;
    private int[] pieceCount;
    private static final int[] dr = {0, -1, -1, -1, 0, 1, 1, 1};
    private static final int[] dc = {1, 1, 0, -1, -1, -1, 0, 1};

    private static int getIndexFromPiece(Piece piece) {
        if (piece == Piece.WHITE) return 1;
        return 0;
    }

    public int getDimension() {
        return dimension;
    }

    public GameState(int dimension) {
        this.dimension = dimension;
        grid = (Optional<Piece>[][]) new Optional<?>[dimension][dimension];
        pieceCount = new int[2];
        positiveSlope = new int[2 * dimension - 1];
        negativeSlope = new int[2 * dimension - 1];
        row = new int[dimension];
        col = new int[dimension];

        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                grid[i][j] = Optional.empty();

        for (int i = 1; i < dimension - 1; i++) {
            addWhite(i, 0);
            addWhite(i, dimension - 1);
            addBlack(0, i);
            addBlack(dimension - 1, i);
        }
        pieceCount[0] = 2 * (dimension - 2);
        pieceCount[1] = 2 * (dimension - 2);
    }

    public GameState(GameState g) {
        this.dimension = g.dimension;
        grid = (Optional<Piece>[][]) new Optional<?>[dimension][dimension];
        pieceCount = new int[2];
        positiveSlope = new int[2 * dimension - 1];
        negativeSlope = new int[2 * dimension - 1];
        row = new int[dimension];
        col = new int[dimension];

        for (int i = 0; i < dimension; i++)
            System.arraycopy(g.grid[i], 0, grid[i], 0, dimension);
        for (int i = 0; i < dimension; i++) {
            row[i] = g.row[i];
            col[i] = g.col[i];
        }
        for (int i = 0; i < 2 * dimension - 1; i++) {
            positiveSlope[i] = g.positiveSlope[i];
            negativeSlope[i] = g.negativeSlope[i];
        }
        pieceCount[0] = g.pieceCount[0];
        pieceCount[1] = g.pieceCount[1];
    }

    int getPositiveSlopeNo(int i, int j) {
        return dimension - i + j - 1;
    }

    int getNegativeSlopeNo(int i, int j) {
        return i + j;
    }

    boolean isLegal(int i, int j) {
        return i >= 0 && i < dimension && j >= 0 && j < dimension;
    }

    Optional<Piece> getState(int i, int j) {
        return grid[i][j];
    }

    void addPiece(int i, int j, Piece piece) {
        grid[i][j].ifPresentOrElse(
                existingPiece -> pieceCount[getIndexFromPiece(existingPiece)]--,
                () -> {
                    row[i]++;
                    col[j]++;
                    positiveSlope[getPositiveSlopeNo(i, j)]++;
                    negativeSlope[getNegativeSlopeNo(i, j)]++;
                }
        );
        grid[i][j] = Optional.of(piece);
        pieceCount[getIndexFromPiece(piece)]++;
    }

    void addBlack(int row, int column) {
        addPiece(row, column, Piece.BLACK);
    }

    void addWhite(int row, int column) {
        addPiece(row, column, Piece.WHITE);
    }

    void removePiece(int i, int j) {
        grid[i][j].ifPresent(piece -> {
            pieceCount[getIndexFromPiece(piece)]--;
            row[i]--;
            col[j]--;
            positiveSlope[getPositiveSlopeNo(i, j)]--;
            negativeSlope[getNegativeSlopeNo(i, j)]--;
            grid[i][j] = Optional.empty();
        });
    }

    boolean isWon(Piece piece) {
        Pair<Integer, Integer> start = null;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j].equals(Optional.of(piece))) {
                    start = new Pair<>(i, j);
                    break;
                }
        if (start == null) return true;
        Queue<Pair<Integer, Integer>> queue = new LinkedList<>();
        queue.add(start);
        int count = 0;
        boolean[][] visited = new boolean[dimension][dimension];
        visited[start.getKey()][start.getValue()] = true;
        while (!queue.isEmpty()) {
            int row = queue.peek().getKey();
            int column = queue.peek().getValue();
            queue.remove();
            count++;
            for (int k = 0; k < 8; k++) {
                int nextRow = row + dr[k];
                int nextColumn = column + dc[k];
                if (isLegal(nextRow, nextColumn) && grid[nextRow][nextColumn].equals(Optional.of(piece)) && !visited[nextRow][nextColumn]) {
                    visited[nextRow][nextColumn] = true;
                    queue.add(new Pair<>(nextRow, nextColumn));
                }
            }
        }
        return count == pieceCount[getIndexFromPiece(piece)];
    }

    void transferPiece(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {
        grid[sourceRow][sourceColumn].ifPresentOrElse(
                piece -> {
                    removePiece(sourceRow, sourceColumn);
                    addPiece(targetRow, targetColumn, piece);
                },
                () -> System.out.println("Bad Request!")
        );
    }

    /**
     * Takes a cell and return relevant cells.
     *
     * @param row    index of the row
     * @param column index of the column
     * @return
    | Arraylist |    Value        |
    |-----------|-----------------|
    | 1         | In path cells   |
    | 2         | Target cells    |
    | 3         | Capturing cells |
     */
    ArrayList<ArrayList<Pair<Integer, Integer>>> findAll(int row, int column) {
        Optional<Piece> maybePiece = grid[row][column];
        if (maybePiece.isEmpty()) return null;

        ArrayList<Pair<Integer, Integer>> inPath = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> target = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> capturing = new ArrayList<>();

        //right
        boolean pathClear = true;
        for (int k = 1; k < this.row[row]; k++)
            if (!isLegal(row, column + k) || (grid[row][column + k].isPresent() && !grid[row][column + k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(row, column + this.row[row]) && !grid[row][column + this.row[row]].equals(maybePiece)) {
            for (int k = 1; k < this.row[row]; k++)
                inPath.add(new Pair<>(row, column + k));
            target.add(new Pair<>(row, column + this.row[row]));
            if (grid[row][column + this.row[row]].isPresent())
                capturing.add(new Pair<>(row, column + this.row[row]));

        }
        //left
        pathClear = true;
        for (int k = 1; k < this.row[row]; k++)
            if (!isLegal(row, column - k) || (grid[row][column - k].isPresent() && !grid[row][column - k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(row, column - this.row[row]) && !grid[row][column - this.row[row]].equals(maybePiece)) {
            for (int k = 1; k < this.row[row]; k++)
                inPath.add(new Pair<>(row, column - k));
            target.add(new Pair<>(row, column - this.row[row]));
            if (grid[row][column - this.row[row]].isPresent())
                capturing.add(new Pair<>(row, column - this.row[row]));

        }
        //down
        pathClear = true;
        for (int k = 1; k < col[column]; k++)
            if (!isLegal(row + k, column) || (grid[row + k][column].isPresent() && !grid[row + k][column].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(row + col[column], column) && !grid[row + col[column]][column].equals(maybePiece)) {
            for (int k = 1; k < col[column]; k++)
                inPath.add(new Pair<>(row + k, column));
            target.add(new Pair<>(row + col[column], column));
            if (grid[row + col[column]][column].isPresent())
                capturing.add(new Pair<>(row + col[column], column));
        }
        //up
        pathClear = true;
        for (int k = 1; k < col[column]; k++)
            if (!isLegal(row - k, column) || (grid[row - k][column].isPresent() && !grid[row - k][column].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(row - col[column], column) && !grid[row - col[column]][column].equals(maybePiece)) {
            for (int k = 1; k < col[column]; k++)
                inPath.add(new Pair<>(row - k, column));
            target.add(new Pair<>(row - col[column], column));
            if (grid[row - col[column]][column].isPresent())
                capturing.add(new Pair<>(row - col[column], column));
        }
        //up right
        pathClear = true;
        int steps = positiveSlope[getPositiveSlopeNo(row, column)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(row + k, column + k) || (grid[row + k][column + k].isPresent() && !grid[row + k][column + k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(row + steps, column + steps)
                && !grid[row + steps][column + steps].equals(maybePiece)) {
            for (int k = 1; k < steps; k++)
                inPath.add(new Pair<>(row + k, column + k));
            target.add(new Pair<>(row + steps, column + steps));
            if (grid[row + steps][column + steps].isPresent())
                capturing.add(new Pair<>(row + steps, column + steps));
        }
        //down left
        pathClear = true;
        steps = positiveSlope[getPositiveSlopeNo(row, column)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(row - k, column - k) || (grid[row - k][column - k].isPresent() && !grid[row - k][column - k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(row - steps, column - steps)
                && !grid[row - steps][column - steps].equals(maybePiece)) {
            for (int k = 1; k < steps; k++)
                inPath.add(new Pair<>(row - k, column - k));
            target.add(new Pair<>(row - steps, column - steps));
            if (grid[row - steps][column - steps].isPresent())
                capturing.add(new Pair<>(row - steps, column - steps));
        }

        //down right
        pathClear = true;
        steps = negativeSlope[getNegativeSlopeNo(row, column)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(row + k, column - k) || (grid[row + k][column - k].isPresent() && !grid[row + k][column - k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(row + steps, column - steps)
                && !grid[row + steps][column - steps].equals(maybePiece)) {
            for (int k = 1; k < steps; k++)
                inPath.add(new Pair<>(row + k, column - k));
            target.add(new Pair<>(row + steps, column - steps));
            if (grid[row + steps][column - steps].isPresent())
                capturing.add(new Pair<>(row + steps, column - steps));
        }

        //up left
        pathClear = true;
        steps = negativeSlope[getNegativeSlopeNo(row, column)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(row - k, column + k) || (grid[row - k][column + k].isPresent() && !grid[row - k][column + k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(row - steps, column + steps)
                && !grid[row - steps][column + steps].equals(maybePiece)) {
            for (int k = 1; k < steps; k++)
                inPath.add(new Pair<>(row - k, column + k));
            target.add(new Pair<>(row - steps, column + steps));
            if (grid[row - steps][column + steps].isPresent())
                capturing.add(new Pair<>(row - steps, column + steps));
        }

        ArrayList<ArrayList<Pair<Integer, Integer>>> result = new ArrayList<>();
        result.add(inPath);
        result.add(target);
        result.add(capturing);
        return result;
    }

    /**
     * Takes the piece.
     *
     * @param piece The desired piece
     * @return The center of mass of the corresponding piece.
     */
    Pair<Integer, Integer> getCM(Piece piece) {
        int x = 0;
        int y = 0;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j].equals(Optional.of(piece))) {
                    x += i;
                    y += j;
                }
        return new Pair<>(x / pieceCount[getIndexFromPiece(piece)], y / pieceCount[getIndexFromPiece(piece)]);
    }

    Pair<Integer, Integer> getComponent(Piece piece) {
        boolean[][] visited = new boolean[dimension][dimension];
        int size = 0;
        int components = 0;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j].equals(Optional.of(piece)) && !visited[i][j]) {
                    components++;
                    Pair<Integer, Integer> start = new Pair<>(i, j);
                    visited[i][j] = true;
                    Queue<Pair<Integer, Integer>> Q = new LinkedList<>();
                    Q.add(start);
                    int count = 1;
                    visited[start.getKey()][start.getValue()] = true;
                    while (!Q.isEmpty()) {
                        int i2 = Q.peek().getKey();
                        int j2 = Q.peek().getValue();
                        Q.remove();
                        count++;
                        for (int k = 0; k < 8; k++) {
                            int x = i2 + dr[k];
                            int y = j2 + dc[k];
                            if (isLegal(x, y) && grid[x][y].equals(Optional.of(piece)) && !visited[x][y]) {
                                visited[x][y] = true;
                                Q.add(new Pair<>(x, y));
                            }
                        }
                    }
                    size = Integer.max(size, count);
                }
        return new Pair<>(components, size);
    }

    int getQuad(int x, int y, Piece piece) {
        int res = 0;
        int startX = Integer.max(0, x - 2);
        int startY = Integer.max(0, y - 2);
        int endX = Integer.min(dimension - 2, x + 2);
        int endY = Integer.min(dimension - 2, x + 2);
        for (int i = startX; i <= endX; i++)
            for (int j = startY; j <= endY; j++) {
                int count = 0;
                count += grid[i][j].equals(Optional.of(piece)) ? 1 : 0;
                count += grid[i][j + 1].equals(Optional.of(piece)) ? 1 : 0;
                count += grid[i + 1][j].equals(Optional.of(piece)) ? 1 : 0;
                count += grid[i + 1][j + 1].equals(Optional.of(piece)) ? 1 : 0;
                if (count >= 3) res++;
            }
        return res;
    }

    int getDensity(int x, int y, Piece piece) {
        int distance = 1;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j].equals(Optional.of(piece)))
                    distance += Double.max(Math.abs(i - x), Math.abs(j - y));

        return dimension * pieceCount[getIndexFromPiece(piece)] / distance;
    }

    int getMobility(Piece piece) {
        int res = 0;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j].equals(Optional.of(piece)))
                    res += findAll(i, j).get(1).size();
        return res;
    }

    int getPST(Piece piece) {
        int res = 0;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j].equals(Optional.of(piece))) {
                    int x = Integer.max(Math.abs((dimension - 1) / 2 - i), Math.abs(dimension / 2 - i));
                    int y = Integer.max(Math.abs((dimension - 1) / 2 - j), Math.abs(dimension / 2 - j));
                    res += 100 - (200 / dimension) * (x + y);
                }
        return res;
    }

    int getArea(Piece piece) {
        int minX = dimension - 1;
        int minY = dimension - 1;
        int maxX = 0, maxY = 0;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j].equals(Optional.of(piece))) {
                    minX = Integer.min(minX, i);
                    minY = Integer.min(minY, j);
                    maxX = i;
                    maxY = j;
                }
        return (maxX - minX + 1) * (maxY - minY + 1);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++)
                s.append(" ").append(grid[i][j]);
            s.append("\n");
        }
        return s.toString();
    }
}
package loa;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class GameState {
    int dimension;
    Optional<Piece>[][] grid;
    int[] positiveSlope;
    int[] negativeSlope;
    int[] row;
    int[] col;
    int[] pieceCount;
    static int[] dr = {0, -1, -1, -1, 0, 1, 1, 1};
    static int[] dc = {1, 1, 0, -1, -1, -1, 0, 1};

    private static int getIndexFromPiece(Piece piece) {
        if (piece == Piece.WHITE) return 1;
        return 0;
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

    void addBlack(int i, int j) {
        addPiece(i, j, Piece.BLACK);
    }

    void addWhite(int i, int j) {
        addPiece(i, j, Piece.WHITE);
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

    boolean isWon(Piece piece) {
        Pair<Integer, Integer> start = null;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j].equals(Optional.of(piece))) {
                    start = new Pair<>(i, j);
                    break;
                }
        if (start == null) return true;
        Queue<Pair<Integer, Integer>> Q = new LinkedList<>();
        Q.add(start);
        int count = 0;
        boolean[][] visited = new boolean[dimension][dimension];
        visited[start.getKey()][start.getValue()] = true;
        while (!Q.isEmpty()) {
            int i = Q.peek().getKey();
            int j = Q.peek().getValue();
            Q.remove();
            count++;
            for (int k = 0; k < 8; k++) {
                int x = i + dr[k];
                int y = j + dc[k];
                if (isLegal(x, y) && grid[x][y].equals(Optional.of(piece)) && !visited[x][y]) {
                    visited[x][y] = true;
                    Q.add(new Pair<>(x, y));
                }
            }
        }
        return count == pieceCount[getIndexFromPiece(piece)];
    }

    boolean isWhiteWon() {
        return isWon(Piece.WHITE);
    }

    boolean isBlackWon() {
        return isWon(Piece.BLACK);
    }

    void transferPiece(int i1, int j1, int i2, int j2) {
        grid[i1][j1].ifPresentOrElse(
                piece -> {
                    removePiece(i1, j1);
                    addPiece(i2, j2, piece);
                },
                () -> System.out.println("Bad Request!")
        );
    }

    ArrayList<ArrayList<Pair<Integer, Integer>>> findAll(int i, int j) {
        Optional<Piece> maybePiece = grid[i][j];
        if (maybePiece.isEmpty()) return null;

        ArrayList<Pair<Integer, Integer>> inPath = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> end = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> capturing = new ArrayList<>();

        //right
        boolean pathClear = true;
        for (int k = 1; k < row[i]; k++)
            if (!isLegal(i, j + k) || (grid[i][j + k].isPresent() && !grid[i][j + k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(i, j + row[i]) && !grid[i][j + row[i]].equals(maybePiece)) {
            for (int k = 1; k < row[i]; k++)
                inPath.add(new Pair<>(i, j + k));
            end.add(new Pair<>(i, j + row[i]));
            if (grid[i][j + row[i]].isPresent())
                capturing.add(new Pair<>(i, j + row[i]));

        }
        //left
        pathClear = true;
        for (int k = 1; k < row[i]; k++)
            if (!isLegal(i, j - k) || (grid[i][j - k].isPresent() && !grid[i][j - k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(i, j - row[i]) && !grid[i][j - row[i]].equals(maybePiece)) {
            for (int k = 1; k < row[i]; k++)
                inPath.add(new Pair<>(i, j - k));
            end.add(new Pair<>(i, j - row[i]));
            if (grid[i][j - row[i]].isPresent())
                capturing.add(new Pair<>(i, j - row[i]));

        }
        //down
        pathClear = true;
        for (int k = 1; k < col[j]; k++)
            if (!isLegal(i + k, j) || (grid[i + k][j].isPresent() && !grid[i + k][j].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(i + col[j], j) && !grid[i + col[j]][j].equals(maybePiece)) {
            for (int k = 1; k < col[j]; k++)
                inPath.add(new Pair<>(i + k, j));
            end.add(new Pair<>(i + col[j], j));
            if (grid[i + col[j]][j].isPresent())
                capturing.add(new Pair<>(i + col[j], j));
        }
        //up
        pathClear = true;
        for (int k = 1; k < col[j]; k++)
            if (!isLegal(i - k, j) || (grid[i - k][j].isPresent() && !grid[i - k][j].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(i - col[j], j) && !grid[i - col[j]][j].equals(maybePiece)) {
            for (int k = 1; k < col[j]; k++)
                inPath.add(new Pair<>(i - k, j));
            end.add(new Pair<>(i - col[j], j));
            if (grid[i - col[j]][j].isPresent())
                capturing.add(new Pair<>(i - col[j], j));
        }
        //up right
        pathClear = true;
        int steps = positiveSlope[getPositiveSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i + k, j + k) || (grid[i + k][j + k].isPresent() && !grid[i + k][j + k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i + steps, j + steps)
                && !grid[i + steps][j + steps].equals(maybePiece)) {
            for (int k = 1; k < steps; k++)
                inPath.add(new Pair<>(i + k, j + k));
            end.add(new Pair<>(i + steps, j + steps));
            if (grid[i + steps][j + steps].isPresent())
                capturing.add(new Pair<>(i + steps, j + steps));
        }
        //down left
        pathClear = true;
        steps = positiveSlope[getPositiveSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i - k, j - k) || (grid[i - k][j - k].isPresent() && !grid[i - k][j - k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i - steps, j - steps)
                && !grid[i - steps][j - steps].equals(maybePiece)) {
            for (int k = 1; k < steps; k++)
                inPath.add(new Pair<>(i - k, j - k));
            end.add(new Pair<>(i - steps, j - steps));
            if (grid[i - steps][j - steps].isPresent())
                capturing.add(new Pair<>(i - steps, j - steps));
        }

        //down right
        pathClear = true;
        steps = negativeSlope[getNegativeSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i + k, j - k) || (grid[i + k][j - k].isPresent() && !grid[i + k][j - k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i + steps, j - steps)
                && !grid[i + steps][j - steps].equals(maybePiece)) {
            for (int k = 1; k < steps; k++)
                inPath.add(new Pair<>(i + k, j - k));
            end.add(new Pair<>(i + steps, j - steps));
            if (grid[i + steps][j - steps].isPresent())
                capturing.add(new Pair<>(i + steps, j - steps));
        }

        //up left
        pathClear = true;
        steps = negativeSlope[getNegativeSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i - k, j + k) || (grid[i - k][j + k].isPresent() && !grid[i - k][j + k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i - steps, j + steps)
                && !grid[i - steps][j + steps].equals(maybePiece)) {
            for (int k = 1; k < steps; k++)
                inPath.add(new Pair<>(i - k, j + k));
            end.add(new Pair<>(i - steps, j + steps));
            if (grid[i - steps][j + steps].isPresent())
                capturing.add(new Pair<>(i - steps, j + steps));
        }

        ArrayList<ArrayList<Pair<Integer, Integer>>> result = new ArrayList<>();
        result.add(inPath);
        result.add(end);
        result.add(capturing);
        return result;
    }

    ArrayList<Pair<Integer, Integer>> findNext(int i, int j) {
        Optional<Piece> maybePiece = grid[i][j];
        if (maybePiece.isEmpty()) return null;
        ArrayList<Pair<Integer, Integer>> end = new ArrayList<>();

        //right
        boolean pathClear = true;
        for (int k = 1; k < row[i]; k++)
            if (!isLegal(i, j + k) || (grid[i][j + k].isPresent() && !grid[i][j + k].equals(maybePiece))) {
                pathClear = false;
                break;
            }

        if (pathClear && isLegal(i, j + row[i]) && !grid[i][j + row[i]].equals(maybePiece))
            end.add(new Pair<>(i, j + row[i]));

        //left
        pathClear = true;
        for (int k = 1; k < row[i]; k++)
            if (!isLegal(i, j - k) || (grid[i][j - k].isPresent() && !grid[i][j - k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(i, j - row[i]) && !grid[i][j - row[i]].equals(maybePiece))
            end.add(new Pair<>(i, j - row[i]));

        //down
        pathClear = true;
        for (int k = 1; k < col[j]; k++)
            if (!isLegal(i + k, j) || (grid[i + k][j].isPresent() && !grid[i + k][j].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(i + col[j], j) && !grid[i + col[j]][j].equals(maybePiece))
            end.add(new Pair<>(i + col[j], j));

        //up
        pathClear = true;
        for (int k = 1; k < col[j]; k++)
            if (!isLegal(i - k, j) || (grid[i - k][j].isPresent() && !grid[i - k][j].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(i - col[j], j) && !grid[i - col[j]][j].equals(maybePiece))
            end.add(new Pair<>(i - col[j], j));

        //up right
        pathClear = true;
        int steps = positiveSlope[getPositiveSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i + k, j + k) || (grid[i + k][j + k].isPresent() && !grid[i + k][j + k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i + steps, j + steps)
                && !grid[i + steps][j + steps].equals(maybePiece))
            end.add(new Pair<>(i + steps, j + steps));

        //down left
        pathClear = true;
        steps = positiveSlope[getPositiveSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i - k, j - k) || (grid[i - k][j - k].isPresent() && !grid[i - k][j - k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i - steps, j - steps)
                && !grid[i - steps][j - steps].equals(maybePiece))
            end.add(new Pair<>(i - steps, j - steps));


        //down right
        pathClear = true;
        steps = negativeSlope[getNegativeSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i + k, j - k) || (grid[i + k][j - k].isPresent() && !grid[i + k][j - k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i + steps, j - steps)
                && !grid[i + steps][j - steps].equals(maybePiece))
            end.add(new Pair<>(i + steps, j - steps));


        //up left
        pathClear = true;
        steps = negativeSlope[getNegativeSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i - k, j + k) || (grid[i - k][j + k].isPresent() && !grid[i - k][j + k].equals(maybePiece))) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i - steps, j + steps)
                && !grid[i - steps][j + steps].equals(maybePiece))
            end.add(new Pair<>(i - steps, j + steps));

        return end;
    }

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
                    res += findNext(i, j).size();
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

    int utility(Piece piece) {
        Pair<Integer, Integer> CM = getCM(piece);
        int x = CM.getKey();
        int y = CM.getValue();
        Pair<Integer, Integer> comp = getComponent(piece);
        int compSize = comp.getValue();
        int compNo = comp.getKey();
        return getDensity(x, y, piece)
                + getQuad(x, y, piece)
                + dimension * compSize
                + 10 * getMobility(piece)
                + 50 * getPST(piece)
                - getArea(piece) / 2
                - 100 * compNo;
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
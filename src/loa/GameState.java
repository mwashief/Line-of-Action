package loa;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class GameState {
    int dimension;
    int[][] grid;
    int[] positiveSlope;
    int[] negativeSlope;
    int[] row;
    int[] col;
    int[] pieceCount;
    static int[] dr = {0, -1, -1, -1, 0, 1, 1, 1};
    static int[] dc = {1, 1, 0, -1, -1, -1, 0, 1};

    public GameState(int dimension) {
        this.dimension = dimension;
        grid = new int[dimension][dimension];
        pieceCount = new int[2];
        positiveSlope = new int[2 * dimension - 1];
        negativeSlope = new int[2 * dimension - 1];
        row = new int[dimension];
        col = new int[dimension];
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
        grid = new int[dimension][dimension];
        pieceCount = new int[2];
        positiveSlope = new int[2 * dimension - 1];
        negativeSlope = new int[2 * dimension - 1];
        row = new int[dimension];
        col = new int[dimension];

        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                grid[i][j] = g.grid[i][j];
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
        addPiece(i, j, 2);
    }

    void addWhite(int i, int j) {
        addPiece(i, j, 3);
    }

    void removePiece(int i, int j) {
        if (grid[i][j] == 0) return;

        pieceCount[grid[i][j] - 2]--;
        row[i]--;
        col[j]--;
        positiveSlope[getPositiveSlopeNo(i, j)]--;
        negativeSlope[getNegativeSlopeNo(i, j)]--;
        grid[i][j] = 0;
    }

    boolean isLegal(int i, int j) {
        return i >= 0 && i < dimension && j >= 0 && j < dimension;
    }

    int getState(int i, int j) {
        return grid[i][j];
    }

    void addPiece(int i, int j, int k) {
        if (grid[i][j] == 0) {
            row[i]++;
            col[j]++;
            positiveSlope[getPositiveSlopeNo(i, j)]++;
            negativeSlope[getNegativeSlopeNo(i, j)]++;
        } else pieceCount[grid[i][j] - 2]--;
        grid[i][j] = k;
        pieceCount[grid[i][j] - 2]++;
    }

    boolean isWon(int color) {
        Pair<Integer, Integer> start = null;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j] == color) {
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
                if (isLegal(x, y) && grid[x][y] == color && !visited[x][y]) {
                    visited[x][y] = true;
                    Q.add(new Pair<>(x, y));
                }
            }
        }
        if (count == pieceCount[color - 2]) return true;
        return false;
    }

    boolean isWhiteWon() {
        return isWon(3);
    }

    boolean isBlackWon() {
        return isWon(2);
    }

    void transferPiece(int i1, int j1, int i2, int j2) {
        int v = grid[i1][j1];
        if (v == 0) {
            System.out.println("Conflict!");
            return;
        }
        removePiece(i1, j1);
        addPiece(i2, j2, v);
    }

    ArrayList<ArrayList<Pair<Integer, Integer>>> findAll(int i, int j) {
        int piece = grid[i][j];
        if (piece == 0) return null;
        ArrayList<Pair<Integer, Integer>> inPath = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> end = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> capturing = new ArrayList<>();
        //right
        boolean pathClear = true;
        for (int k = 1; k < row[i]; k++)
            if (!isLegal(i, j + k) || (grid[i][j + k] != 0 && grid[i][j + k] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(i, j + row[i]) && grid[i][j + row[i]] != piece) {
            for (int k = 1; k < row[i]; k++)
                inPath.add(new Pair<>(i, j + k));
            end.add(new Pair<>(i, j + row[i]));
            if (grid[i][j + row[i]] != 0)
                capturing.add(new Pair<>(i, j + row[i]));

        }
        //left
        pathClear = true;
        for (int k = 1; k < row[i]; k++)
            if (!isLegal(i, j - k) || (grid[i][j - k] != 0 && grid[i][j - k] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(i, j - row[i]) && grid[i][j - row[i]] != piece) {
            for (int k = 1; k < row[i]; k++)
                inPath.add(new Pair<>(i, j - k));
            end.add(new Pair<>(i, j - row[i]));
            if (grid[i][j - row[i]] != 0)
                capturing.add(new Pair<>(i, j - row[i]));

        }
        //down
        pathClear = true;
        for (int k = 1; k < col[j]; k++)
            if (!isLegal(i + k, j) || (grid[i + k][j] != 0 && grid[i + k][j] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(i + col[j], j) && grid[i + col[j]][j] != piece) {
            for (int k = 1; k < col[j]; k++)
                inPath.add(new Pair<>(i + k, j));
            end.add(new Pair<>(i + col[j], j));
            if (grid[i + col[j]][j] != 0)
                capturing.add(new Pair<>(i + col[j], j));
        }
        //up
        pathClear = true;
        for (int k = 1; k < col[j]; k++)
            if (!isLegal(i - k, j) || (grid[i - k][j] != 0 && grid[i - k][j] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(i - col[j], j) && grid[i - col[j]][j] != piece) {
            for (int k = 1; k < col[j]; k++)
                inPath.add(new Pair<>(i - k, j));
            end.add(new Pair<>(i - col[j], j));
            if (grid[i - col[j]][j] != 0)
                capturing.add(new Pair<>(i - col[j], j));
        }
        //up right
        pathClear = true;
        int steps = positiveSlope[getPositiveSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i + k, j + k) || (grid[i + k][j + k] != 0 && grid[i + k][j + k] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i + steps, j + steps)
                && grid[i + steps][j + steps] != piece) {
            for (int k = 1; k < steps; k++)
                inPath.add(new Pair<>(i + k, j + k));
            end.add(new Pair<>(i + steps, j + steps));
            if (grid[i + steps][j + steps] != 0)
                capturing.add(new Pair<>(i + steps, j + steps));
        }
        //down left
        pathClear = true;
        steps = positiveSlope[getPositiveSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i - k, j - k) || (grid[i - k][j - k] != 0 && grid[i - k][j - k] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i - steps, j - steps)
                && grid[i - steps][j - steps] != piece) {
            for (int k = 1; k < steps; k++)
                inPath.add(new Pair<>(i - k, j - k));
            end.add(new Pair<>(i - steps, j - steps));
            if (grid[i - steps][j - steps] != 0)
                capturing.add(new Pair<>(i - steps, j - steps));
        }

        //down right
        pathClear = true;
        steps = negativeSlope[getNegativeSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i + k, j - k) || (grid[i + k][j - k] != 0 && grid[i + k][j - k] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i + steps, j - steps)
                && grid[i + steps][j - steps] != piece) {
            for (int k = 1; k < steps; k++)
                inPath.add(new Pair<>(i + k, j - k));
            end.add(new Pair<>(i + steps, j - steps));
            if (grid[i + steps][j - steps] != 0)
                capturing.add(new Pair<>(i + steps, j - steps));
        }

        //up left
        pathClear = true;
        steps = negativeSlope[getNegativeSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i - k, j + k) || (grid[i - k][j + k] != 0 && grid[i - k][j + k] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i - steps, j + steps)
                && grid[i - steps][j + steps] != piece) {
            for (int k = 1; k < steps; k++)
                inPath.add(new Pair<>(i - k, j + k));
            end.add(new Pair<>(i - steps, j + steps));
            if (grid[i - steps][j + steps] != 0)
                capturing.add(new Pair<>(i - steps, j + steps));
        }

        ArrayList<ArrayList<Pair<Integer, Integer>>> result = new ArrayList<>();
        result.add(inPath);
        result.add(end);
        result.add(capturing);
        return result;
    }

    ArrayList<Pair<Integer, Integer>> findNext(int i, int j) {
        int piece = grid[i][j];
        if (piece == 0) return null;
        ArrayList<Pair<Integer, Integer>> end = new ArrayList<>();

        //right
        boolean pathClear = true;
        for (int k = 1; k < row[i]; k++)
            if (!isLegal(i, j + k) || (grid[i][j + k] != 0 && grid[i][j + k] != piece)) {
                pathClear = false;
                break;
            }

        if (pathClear && isLegal(i, j + row[i]) && grid[i][j + row[i]] != piece)
            end.add(new Pair<>(i, j + row[i]));

        //left
        pathClear = true;
        for (int k = 1; k < row[i]; k++)
            if (!isLegal(i, j - k) || (grid[i][j - k] != 0 && grid[i][j - k] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(i, j - row[i]) && grid[i][j - row[i]] != piece)
            end.add(new Pair<>(i, j - row[i]));

        //down
        pathClear = true;
        for (int k = 1; k < col[j]; k++)
            if (!isLegal(i + k, j) || (grid[i + k][j] != 0 && grid[i + k][j] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(i + col[j], j) && grid[i + col[j]][j] != piece)
            end.add(new Pair<>(i + col[j], j));

        //up
        pathClear = true;
        for (int k = 1; k < col[j]; k++)
            if (!isLegal(i - k, j) || (grid[i - k][j] != 0 && grid[i - k][j] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear && isLegal(i - col[j], j) && grid[i - col[j]][j] != piece)
            end.add(new Pair<>(i - col[j], j));

        //up right
        pathClear = true;
        int steps = positiveSlope[getPositiveSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i + k, j + k) || (grid[i + k][j + k] != 0 && grid[i + k][j + k] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i + steps, j + steps)
                && grid[i + steps][j + steps] != piece)
            end.add(new Pair<>(i + steps, j + steps));

        //down left
        pathClear = true;
        steps = positiveSlope[getPositiveSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i - k, j - k) || (grid[i - k][j - k] != 0 && grid[i - k][j - k] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i - steps, j - steps)
                && grid[i - steps][j - steps] != piece)
            end.add(new Pair<>(i - steps, j - steps));


        //down right
        pathClear = true;
        steps = negativeSlope[getNegativeSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i + k, j - k) || (grid[i + k][j - k] != 0 && grid[i + k][j - k] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i + steps, j - steps)
                && grid[i + steps][j - steps] != piece)
            end.add(new Pair<>(i + steps, j - steps));


        //up left
        pathClear = true;
        steps = negativeSlope[getNegativeSlopeNo(i, j)];
        for (int k = 1; k < steps; k++)
            if (!isLegal(i - k, j + k) || (grid[i - k][j + k] != 0 && grid[i - k][j + k] != piece)) {
                pathClear = false;
                break;
            }
        if (pathClear
                && isLegal(i - steps, j + steps)
                && grid[i - steps][j + steps] != piece)
            end.add(new Pair<>(i - steps, j + steps));

        return end;
    }

    Pair<Integer, Integer> getCM(int color) {
        int x = 0;
        int y = 0;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j] == color) {
                    x += i;
                    y += j;
                }
        return new Pair<>(x / pieceCount[color - 2], y / pieceCount[color - 2]);
    }

    Pair<Integer, Integer> getComponent(int color) {
        boolean[][] visited = new boolean[dimension][dimension];
        int size = 0;
        int components = 0;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j] == color && !visited[i][j]) {
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
                            if (isLegal(x, y) && grid[x][y] == color && !visited[x][y]) {
                                visited[x][y] = true;
                                Q.add(new Pair<>(x, y));
                            }
                        }
                    }
                    size = Integer.max(size, count);
                }
        return new Pair<>(components, size);
    }

    int getQuad(int x, int y, int color) {
        int res = 0;
        int startX = Integer.max(0, x - 2);
        int startY = Integer.max(0, y - 2);
        int endX = Integer.min(dimension - 2, x + 2);
        int endY = Integer.min(dimension - 2, x + 2);
        for (int i = startX; i <= endX; i++)
            for (int j = startY; j <= endY; j++) {
                int count = 0;
                count += grid[i][j] == color ? 1 : 0;
                count += grid[i][j + 1] == color ? 1 : 0;
                count += grid[i + 1][j] == color ? 1 : 0;
                count += grid[i + 1][j + 1] == color ? 1 : 0;
                if (count >= 3) res++;
            }
        return res;
    }

    int getDensity(int color, int x, int y) {
        int distance = 1;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j] == color)
                    distance += Double.max(Math.abs(i - x), Math.abs(j - y));

        return dimension * pieceCount[color - 2] / distance;
    }

    int getMobility(int color) {
        int res = 0;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j] == color)
                    res += findNext(i, j).size();
        return res;
    }

    int getPST(int color) {
        int res = 0;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j] == color) {
                    int x = Integer.max(Math.abs((dimension - 1) / 2 - i), Math.abs(dimension / 2 - i));
                    int y = Integer.max(Math.abs((dimension - 1) / 2 - j), Math.abs(dimension / 2 - j));
                    res += 100 - (200 / dimension) * (x + y);
                }
        return res;
    }

    int getArea(int color) {
        int minX = dimension - 1;
        int minY = dimension - 1;
        int maxX = 0, maxY = 0;
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (grid[i][j] == color) {
                    minX = Integer.min(minX, i);
                    minY = Integer.min(minY, j);
                    maxX = i;
                    maxY = j;
                }
        return (maxX - minX + 1) * (maxY - minY + 1);
    }

    int utility(int color) {

        Pair<Integer, Integer> CM = getCM(color);
        int x = CM.getKey();
        int y = CM.getValue();
        Pair<Integer, Integer> comp = getComponent(color);
        int compSize = comp.getValue();
        int compNo = comp.getKey();
        return getDensity(color, x, y)
                + getQuad(x, y, color)
                + dimension * compSize
                + 10 * getMobility(color)
                + 50 * getPST(color)
                - getArea(color) / 2
                - 100 * compNo;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++)
                s = s + " " + grid[i][j];
            s = s + "\n";
        }
        return s;
    }
}
package loa;

public class PSTView {
    public static void main(String[] args) {
        int dimension = 1;
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                int x = Integer.max(Math.abs((dimension - 1) / 2 - i), Math.abs(dimension / 2 - i));
                int y = Integer.max(Math.abs((dimension - 1) / 2 - j), Math.abs(dimension / 2 - j));
                System.out.print(100 - (200 / dimension) * (x + y) + " ");
            }
            System.out.println("");
        }
    }
}

package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

/**
 * x = row, y = col
 *
 * 1,1,0,1,1,1
 * 1,2,0,0,1,1
 * 1,1,1,1,2,1
 * 1,1,1,1,1,1
 * 1,0,0,1,1,1
 * 1,1,1,1,1,1
 */
public class Main {

    public static void main(String[] args) {
        ArgumentsParser parser = new ArgumentsParser(args);
        parser.parseStartAndEndPoint();
        System.out.println("Before start");
        System.out.println(parser);
        System.out.println("Searching path...\n");
        PathFinder pathFinder = new PathFinder(parser);

        pathFinder.findPath();
        pathFinder.printSolution();
    }
}

class PathFinder {
    enum PathState {DISCOVERED, UNDISCOVERED}

    private LinkedList<Point> neighbours;
    private int N;
    private int[][] matrix;
    private PathState[][] state;
    private Point startPoint;
    private Point endPoint;
    private Point teleport1;
    private Point teleport2;

    PathFinder(ArgumentsParser args) {
        neighbours = new LinkedList<>();
        N = args.getN();
        matrix = args.getMatrix();
        startPoint = args.getStartPoint();
        endPoint = args.getEndPoint();
        teleport1 = args.getTeleports()
                .pop();
        teleport2 = args.getTeleports()
                .pop();
        initPathState();
    }

    private void initPathState() {
        state = new PathState[N][];
        for (int i = 0; i < N; i++) {
            state[i] = new PathState[N];
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                state[i][j] = (matrix[i][j] == 0) ? PathState.DISCOVERED : PathState.UNDISCOVERED;
            }
        }
    }

    void findPath() {
        neighbours.add(startPoint);
        state[startPoint.x()][startPoint.y()] = PathState.DISCOVERED;
        findPathSolution();
    }

    private void findPathSolution() {
        if (neighbours.isEmpty()) {
            return;
        }

        Point currentPosition = neighbours.pop();
        if (currentPosition.equals(endPoint)) {
            endPoint = currentPosition; // inherit neighbours
            return;
        }

        Optional<Point> teleportation = tryTeleportation(currentPosition);
        if (teleportation.isPresent()) {
            Point teleportationPosition = teleportation.get();
            addNeighbours(teleportationPosition);
        }
        addNeighbours(currentPosition);
        findPathSolution();
    }

    private void addNeighbours(Point currentPosition) {
        if (canGoRight(currentPosition)) {
            neighbours.add(new Point(currentPosition.x(), currentPosition.y() + 1, currentPosition));
            state[currentPosition.x()][currentPosition.y() + 1] = PathState.DISCOVERED;
        }
        if (canGoDown(currentPosition)) {
            neighbours.add(new Point(currentPosition.x() + 1, currentPosition.y(), currentPosition));
            state[currentPosition.x() + 1][currentPosition.y()] = PathState.DISCOVERED;
        }
        if (canGoLeft(currentPosition)) {
            neighbours.add(new Point(currentPosition.x(), currentPosition.y() - 1, currentPosition));
            state[currentPosition.x()][currentPosition.y() - 1] = PathState.DISCOVERED;
        }
        if (canGoUp(currentPosition)) {
            neighbours.add(new Point(currentPosition.x() - 1, currentPosition.y(), currentPosition));
            state[currentPosition.x() - 1][currentPosition.y()] = PathState.DISCOVERED;
        }
    }

    private Optional<Point> tryTeleportation(Point currentPosition) {
        if (currentPosition.equals(teleport1)) {
            teleport2.setParent(currentPosition);
            state[teleport2.x()][teleport2.y()] = PathState.DISCOVERED;
            return Optional.of(teleport2);
        }
        if (currentPosition.equals(teleport2)) {
            teleport1.setParent(currentPosition);
            state[teleport1.x()][teleport1.y()] = PathState.DISCOVERED;
            return Optional.of(teleport1);
        }
        return Optional.empty();
    }

    private boolean canGoRight(Point currentPosition) {
        if (currentPosition.y() + 1 >= N) return false;

        PathState currentPathState = state[currentPosition.x()][currentPosition.y() + 1];
        return currentPathState == PathState.UNDISCOVERED;
    }

    private boolean canGoDown(Point currentPosition) {
        if (currentPosition.x() + 1 >= N) return false;

        PathState currentPathState = state[currentPosition.x() + 1][currentPosition.y()];
        return currentPathState == PathState.UNDISCOVERED;
    }

    private boolean canGoLeft(Point currentPosition) {
        if (currentPosition.y() - 1 < 0) return false;

        PathState currentPathState = state[currentPosition.x()][currentPosition.y() - 1];
        return currentPathState == PathState.UNDISCOVERED;
    }

    private boolean canGoUp(Point currentPosition) {
        if (currentPosition.x() - 1 < 0) return false;

        PathState currentPathState = state[currentPosition.x() - 1][currentPosition.y()];
        return currentPathState == PathState.UNDISCOVERED;
    }

    void printSolution() {
        if (Objects.isNull(endPoint.getParent())) {
            System.out.println("No solution found");
        }
        int steps = printRecursive(endPoint);
        System.out.println("\nSteps required = " + steps);
    }

    int printRecursive(Point currentPoint) {
        if (currentPoint == null) {
            return 0;
        }
        int steps = printRecursive(currentPoint.getParent());
        System.out.print("-> (" + currentPoint.x() + ", " + currentPoint.y() + ") ");
        steps += (currentPoint.equals(teleport1) || currentPoint.equals(teleport2)) ? 0 : 1;
        return steps;
    }
}

class Point {

    private int x;
    private int y;
    private Point parent;

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Point(int x, int y, Point p) {
        this(x, y);
        parent = p;
    }

    public void setParent(Point p) {
        parent = p;
    }

    public Point getParent() {
        return parent;
    }

    int x() {
        return x;
    }

    int y() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x &&
                y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, parent);
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                ", parent=" + parent +
                '}';
    }
}

class ArgumentsParser {
    private final int N;
    private final int k;
    private final int[][] matrix;
    private Point startPoint;
    private Point endPoint;
    private LinkedList<Point> teleports;

    ArgumentsParser(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("There should be three params N, k, path to matrix (space separated");
        }
        try {
            N = Integer.parseInt(args[0]);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("N (first param) is not a number");
        }
        try {
            k = Integer.parseInt(args[1]);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("k (second param) is not a number");
        }
        Path matrixFile = Paths.get(args[2]);
        if (!Files.exists(matrixFile)) {
            throw new IllegalArgumentException("Path '" + matrixFile.normalize() + "' does not exists or does not " +
                    "have permissions for it");
        }
        matrix = new int[N][];
        for (int i = 0; i < N; i++) {
            matrix[i] = new int[N];
        }
        teleports = new LinkedList<>();
        parseMatrix(matrixFile);
    }

    private void parseMatrix(Path matrixFile) {
        List<String> fileLines;
        try {
            fileLines = Files.readAllLines(matrixFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("Path '" + matrixFile.normalize() + "' cannot be read. It might be " +
                    "broken or does not have permissions for it");
        }
        if (fileLines.size() != N) {
            throw new IllegalArgumentException("File have different number of lines than expected. N=" + N +
                    " lines=" + fileLines.size());
        }
        int teleportCounter = 0;
        int pathlessCounter = 0;
        for (int i = 0; i < N; i++) {
            String line = fileLines.get(i);
            String[] rowValues = line.split(",");
            if (rowValues.length != N) {
                throw new IllegalArgumentException("Line " + (i + 1) + " have unexpected number of values " +
                        "(comma separated). N=" + N + " values=" + rowValues.length);
            }
            for (int j = 0; j < N; j++) {
                try {
                    matrix[i][j] = Integer.parseInt(rowValues[j]);
                } catch (RuntimeException ex) {
                    throw new IllegalArgumentException("Row=" + (i + 1) + " Col=" + (j + 1) +
                            " have invalid number=" + rowValues[j]);
                }
                if (matrix[i][j] != 0 && matrix[i][j] != 1 && matrix[i][j] != 2) {
                    throw new IllegalArgumentException("Matrix values should be between (0, 1, 2). It is "
                            + matrix[i][j]);
                }
                if (matrix[i][j] == 0) {
                    pathlessCounter++;
                }
                if (matrix[i][j] == 2) {
                    teleportCounter++;
                    teleports.add(new Point(i, j));
                }
            }
        }
        if (teleportCounter != 2) {
            throw new IllegalArgumentException("Unexpected number of teleports. They are "
                    + teleportCounter + " not 2");
        }
        if (pathlessCounter != k) {
            throw new IllegalArgumentException("Unexpected number of pathless fields. They are "
                    + pathlessCounter + " not " + k);
        }
    }

    void parseStartAndEndPoint() {
        Scanner s = new Scanner(System.in);
        System.out.println("Input start point (comma separated) ");
        this.startPoint = parsePoint(s.nextLine());

        System.out.println("Input end point (comma separated) ");
        this.endPoint = parsePoint(s.nextLine());

        if (startPoint.x() < 0) {
            throw new IllegalArgumentException("Start point cannot be negative");
        }
        if (endPoint.x() < 0) {
            throw new IllegalArgumentException("End point cannot be negative");
        }
        if (startPoint.x() >= N) {
            throw new IllegalArgumentException("Start point cannot be bigger or equal to N");
        }
        if (endPoint.x() >= N) {
            throw new IllegalArgumentException("End point cannot be bigger or equal to N");
        }
        if (matrix[startPoint.x()][startPoint.y()] == 2) {
            throw new IllegalArgumentException("Start point cannot be teleport");
        }
        if (matrix[endPoint.x()][endPoint.y()] == 2) {
            throw new IllegalArgumentException("End point cannot be teleport");
        }
        if (matrix[startPoint.x()][startPoint.y()] == 0) {
            throw new IllegalArgumentException("Start point cannot be blocked");
        }
        if (matrix[endPoint.x()][endPoint.y()] == 0) {
            throw new IllegalArgumentException("End point cannot be blocked");
        }

    }

    private static Point parsePoint(String pointText) {
        String[] points = pointText.split(",");
        if (points.length != 2) {
            throw new IllegalArgumentException("Points should be in format 'x,y' it is '" + pointText + "'");
        }
        int x;
        int y;
        try {
            x = Integer.parseInt(points[0]);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("First point is not a number. It is '" + points[0] + "'");
        }
        try {
            y = Integer.parseInt(points[1]);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Second point is not a number. It is '" + points[1] + "'");
        }
        return new Point(x, y);
    }

    int getN() {
        return N;
    }

    int[][] getMatrix() {
        return matrix;
    }

    Point getStartPoint() {
        return startPoint;
    }

    Point getEndPoint() {
        return endPoint;
    }

    LinkedList<Point> getTeleports() {
        return teleports;
    }

    @Override
    public String toString() {
        return "ArgumentsParser:" +
                "\nN=" + N +
                "\nk=" + k +
                "\nstartPoint=" + startPoint +
                "\nendPoint=" + endPoint +
                "\nmatrix=\n" + toMatrixString();
    }

    private String toMatrixString() {
        StringBuilder sb = new StringBuilder(N * N * 2 + N); // value + comma + space for every element and N new lines
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (j != 0) {
                    sb.append(", ");
                }
                sb.append(matrix[i][j]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}

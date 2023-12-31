package days.day10;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import jdk.jshell.execution.Util;
import utils.ImportUtils;
import utils.Utils;

public class Main {

    public static final String NORTH = "North";
    public static final String SOUTH = "South";
    public static final String EAST = "East";
    public static final String WEST = "West";

    private static final Map<String, List<String>> VALID_FOR_NORTH_MAP = Map.of(
            "L", List.of("|", "F", "7"),
            "J", List.of("|", "F", "7"),
            "|", List.of("|", "F", "7"),
            "S", List.of("|", "F", "7"),
            "F", Collections.emptyList(),
            "7", Collections.emptyList()
    );
    private static final Map<String, List<String>> VALID_FOR_SOUTH_MAP = Map.of(
            "7", List.of("|", "J", "L"),
            "F", List.of("|", "J", "L"),
            "|", List.of("|", "J", "L"),
            "S", List.of("|", "J", "L"),
            "J", Collections.emptyList(),
            "L", Collections.emptyList()
    );
    private static final Map<String, List<String>> VALID_FOR_WEST_MAP = Map.of(
            "J", List.of("-", "F", "L"),
            "7", List.of("-", "F", "L"),
            "-", List.of("-", "F", "L"),
            "S", List.of("-", "F", "L"),
            "F", Collections.emptyList(),
            "L", Collections.emptyList()
    );
    private static final Map<String, List<String>> VALID_FOR_EAST_MAP = Map.of(
            "L", List.of("-", "J", "7"),
            "F", List.of("-", "J", "7"),
            "-", List.of("-", "J", "7"),
            "S", List.of("-", "J", "7"),
            "J", Collections.emptyList(),
            "7", Collections.emptyList()
    );
    private static final Map<String, Map<String, List<String>>> VALID_FOR_DIRECTION_MAP = Map.of(
            NORTH, VALID_FOR_NORTH_MAP,
            SOUTH, VALID_FOR_SOUTH_MAP,
            WEST, VALID_FOR_WEST_MAP,
            EAST, VALID_FOR_EAST_MAP
    );

    private static final List<String> DIRECTIONS = List.of(NORTH, SOUTH, WEST, EAST);

    private static int TOP_TO_BOTTOM;
    private static int LEFT_TO_RIGHT;

    public static void main(String[] args) {
        //         final String filePath = System.getProperty("user.dir") + "/resources/days/day10/input_10_test_01.txt";
        //      final String filePath = System.getProperty("user.dir") + "/resources/days/day10/input_10_test_02.txt";
        //  final String filePath = System.getProperty("user.dir") + "/resources/days/day10/input_10_test_02_01.txt";
        //  final String filePath = System.getProperty("user.dir") + "/resources/days/day10/input_10_test_02_02.txt";
        //final String filePath = System.getProperty("user.dir") + "/resources/days/day10/input_10_test_02_03.txt";
        // final String filePath = System.getProperty("user.dir") + "/resources/days/day10/input_10_test_02_04.txt";
        final String filePath = System.getProperty("user.dir") + "/resources/days/day10/input_10.txt";

        String[][] part1Map = ImportUtils.readAsArray(filePath);

        TOP_TO_BOTTOM = part1Map.length;
        LEFT_TO_RIGHT = part1Map[0].length;

        Coordinate start = getStartCoordinate(part1Map);
        Utils.log("Start is: " + start.toString());

        Set<Coordinate> visited = goFurther(start, part1Map);

        long validVisits = visited.stream().filter(Coordinate::valid).count();

        Utils.log("Solution Part 1: We run in the circle with nodes: " + validVisits);
        Utils.log("So the farthest part is the half: " + validVisits / 2);

        // ---------------------------------------------------------------------------------------------------------------
        // Only for visualisation.

        String[][] modifiedPart1Map = part1Map.clone();

        for (Coordinate coordinate : visited) {
            if(coordinate.valid()) {
                modifiedPart1Map[coordinate.row()][coordinate.column()] = "X";
            }
        }
             printStringArray(modifiedPart1Map);

        // ---------------------------------------------------------------------------------------------------------------

        // Part 2
        String[][] part2Map = part1Map.clone();

        Coordinate startCoordinate = getStartCoordinate(part2Map);
        transformS(part2Map, startCoordinate.row(), startCoordinate.column());

        // Print map with replaced "S".
        printStringArray(part2Map);

        // Ray casting algorithm is used here.
        // We count the number of broder crossings if we go from our point to the farthest point on the left.
        // If the number of crossings is even, our point is outside the polygon.
        // If the number of crossings is odd, our point is inside the polygon.
        List<Coordinate> innerInvalidVisits = findInnerInvalidVisits(part1Map, visited);
        Utils.log("Solution Part 2: Found inner tiles: " + innerInvalidVisits.size());

    }

    private static List<Coordinate> findInnerInvalidVisits(String[][] map, Set<Coordinate> visited) {
        List<Coordinate> innerInvalidVisits = new ArrayList<>();
        for (int row = 0; row < TOP_TO_BOTTOM; row++) {
            for (int column = 0; column < LEFT_TO_RIGHT; column++) {
                boolean notMarkedValid = !isInVisited(row, column, visited);

                if (notMarkedValid) {
                    int borderCrossingCounter = 0;
                    boolean jfBorderPotentiallyCrossed = false;
                    boolean l7BorderPotentiallyCrossed = false;
                    for (int i = column - 1; i >= 0; i--) {
                        boolean validVisited2 = isInVisited(row, i, visited);
                        if (validVisited2) {
                            String coord = map[row][i];
                            if (Objects.equals(coord, "|")) {
                                borderCrossingCounter++;
                            } else if (Objects.equals(coord, "J")) {
                                jfBorderPotentiallyCrossed = true;
                            } else if (Objects.equals(coord, "7")) {
                                l7BorderPotentiallyCrossed = true;
                            } else if (Objects.equals(coord, "F")) {
                                if (jfBorderPotentiallyCrossed) {
                                    borderCrossingCounter++;
                                    jfBorderPotentiallyCrossed = false;
                                }
                                if (l7BorderPotentiallyCrossed) {
                                    l7BorderPotentiallyCrossed = false;
                                }
                            } else if (Objects.equals(coord, "L")) {
                                if (l7BorderPotentiallyCrossed) {
                                    borderCrossingCounter++;
                                    l7BorderPotentiallyCrossed = false;
                                }
                                if (jfBorderPotentiallyCrossed) {
                                    jfBorderPotentiallyCrossed = false;
                                }
                            }
                        }
                    }
                    if (borderCrossingCounter % 2 > 0) {
                        Coordinate innerInvalidVisit = new Coordinate(row, column, map[row][column], false);
                        innerInvalidVisits.add(innerInvalidVisit);
                        Utils.log(innerInvalidVisit.toString());
                    }
                }
            }
        }
        return innerInvalidVisits;
    }


    public static void transformS(
            String[][] array,
            int row,
            int col
    ) {
        // Check conditions and update the value at array[row][col] accordingly
        // Example conditions (you may need to adjust based on your exact requirements):
        if (row > 0 && ("|".equals(array[row - 1][col]) || "F".equals(array[row - 1][col]) || "7".equals(array[row - 1][col])) &&
                row < array.length - 1 && ("L".equals(array[row + 1][col]) || "|".equals(array[row + 1][col]) || "J".equals(
                array[row + 1][col]))) {
            array[row][col] = "|";
        } else if (col < array[0].length - 1 && ("-".equals(array[row][col + 1]) || "J".equals(array[row][col + 1]) || "7".equals(
                array[row][col + 1])) &&
                row < array.length - 1 && ("L".equals(array[row + 1][col]) || "|".equals(array[row + 1][col]) || "J".equals(
                array[row + 1][col]))) {
            array[row][col] = "F";
        } else if (col < array[0].length - 1 && ("-".equals(array[row][col + 1]) || "J".equals(array[row][col + 1]) || "7".equals(
                array[row][col + 1])) &&
                row > 0 && ("F".equals(array[row - 1][col]) || "|".equals(array[row - 1][col]) || "7".equals(array[row - 1][col]))) {
            array[row][col] = "L";
        } else if (col > 0 && ("-".equals(array[row][col - 1]) || "F".equals(array[row][col - 1]) || "L".equals(array[row][col - 1])) &&
                row < array.length - 1 && ("J".equals(array[row + 1][col]) || "|".equals(array[row + 1][col]) || "L".equals(
                array[row + 1][col]))) {
            array[row][col] = "7";
        } else if (col > 0 && ("-".equals(array[row][col - 1]) || "F".equals(array[row][col - 1]) || "L".equals(array[row][col - 1])) &&
                row > 0 && ("F".equals(array[row - 1][col]) || "|".equals(array[row - 1][col]) || "7".equals(array[row - 1][col]))) {
            array[row][col] = "J";
        } else if (col > 0 && ("-".equals(array[row][col - 1]) || "F".equals(array[row][col - 1]) || "L".equals(array[row][col - 1])) &&
                col < array[0].length - 1 && ("-".equals(array[row][col + 1]) || "J".equals(array[row][col + 1]) || "7".equals(
                array[row][col + 1]))) {
            array[row][col] = "-";
        }
    }

    private static boolean isInVisited(
            final int row,
            final int i,
            final Set<Coordinate> visited
    ) {
        for (Coordinate coordinate : visited) {
            if (coordinate.row() == row && coordinate.column() == i) {
                return coordinate.valid();
            }
        }
        return false;


    }


    private static void printStringArray(String[][] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                System.out.print(array[i][j] + " ");
            }
            System.out.println();
        }
    }

    private static Set<Coordinate> goFurther(Coordinate start, String[][] map) {
        Set<Coordinate> visited = new HashSet<>();
        Queue<Coordinate> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Coordinate current = queue.poll();

            Utils.log(current.toString() + " : " + visited.contains(current));

            if (!visited.contains(current)) {
                visited.add(current);

                for (String direction : DIRECTIONS) {
                    Coordinate nextCoordinate = checkDirection(current, direction, map);
                    if (nextCoordinate.valid() && !visited.contains(nextCoordinate)) {
                        queue.add(nextCoordinate);
                    }
                }
            }
        }

        return visited;
    }

    private static Coordinate checkDirection(
            final Coordinate start,
            final String direction,
            final String[][] map
    ) {
        int row = start.row() + (direction.equals(SOUTH) ? 1 : (direction.equals(NORTH) ? -1 : 0));
        int column = start.column() + (direction.equals(EAST) ? 1 : (direction.equals(WEST) ? -1 : 0));

        List<String> validValues = VALID_FOR_DIRECTION_MAP.get(direction).getOrDefault(start.value(), Collections.emptyList());

        if (checkDimensions(row, column) && validValues.contains(map[row][column])) {
            return new Coordinate(row, column, map[row][column], true);
        }

        return new Coordinate(row, column, "o", false);
    }


    private static boolean checkDimensions(
            final int row,
            final int column
    ) {
        return row >= 0 && row <= TOP_TO_BOTTOM - 1 && column >= 0 && column <= LEFT_TO_RIGHT - 1;
    }


    private static Coordinate getStartCoordinate(String[][] map) {
        for (int i = 0; i < TOP_TO_BOTTOM; i++) {
            for (int j = 0; j < LEFT_TO_RIGHT; j++) {
                if (Objects.equals(map[i][j], "S")) {
                    return new Coordinate(i, j, map[i][j], true);
                }
            }
        }
        throw new IllegalStateException();
    }


    private record Coordinate(int row, int column, String value, boolean valid) {

    }


}
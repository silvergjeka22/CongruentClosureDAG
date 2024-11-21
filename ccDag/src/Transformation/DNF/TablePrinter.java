package Transformation.DNF;

import java.util.*;
import java.util.stream.Stream;

public class TablePrinter {
    public static void printTable(String[][] table) {
        System.out.println();
        boolean leftJustifiedRows = false;
        Map<Integer, Integer> columnLengths = new HashMap<>();

        // Calculate column lengths
        Arrays.stream(table).forEach(row ->
            Stream.iterate(0, i -> i < row.length, i -> i + 1).forEach(i -> {
                columnLengths.putIfAbsent(i, 0);
                columnLengths.put(i, Math.max(columnLengths.get(i), row[i].length()));
            })
        );

        // Build format string for rows
        StringBuilder formatString = new StringBuilder();
        String flag = leftJustifiedRows ? "-" : "";
        columnLengths.forEach((key, value) -> formatString.append("| %").append(flag).append(value).append("s "));
        formatString.append("|\n");

        // Build line separator
        String line = columnLengths.entrySet().stream()
                .map(entry -> "+-" + "-".repeat(entry.getValue()) + "-")
                .reduce("", String::concat) + "+\n";

        // Print table
        System.out.print(line);
        Arrays.stream(table).limit(1).forEach(row -> System.out.printf(formatString.toString(), (Object[]) row));
        System.out.print(line);
        Stream.iterate(1, i -> i < table.length, i -> i + 1)
                .forEach(i -> System.out.printf(formatString.toString(), (Object[]) table[i]));
        System.out.print(line);
    }
}

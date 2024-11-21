import java.util.*;
import java.util.stream.Stream;

public class Table
{
    public static void print()
    {
        System.out.print("Type Your Formula: ");
    }

    private static void printTable(String[][] table)
    {
        boolean leftJustifiedRows = false;
        Map<Integer, Integer> columnLengths = new HashMap<>();
        Arrays.stream(table).forEach(a -> Stream.iterate(0, (i -> i < a.length), (i -> ++i)).forEach(i ->
        {
            if (columnLengths.get(i) == null)
            {
                columnLengths.put(i, 0);
            }
            if (columnLengths.get(i) < a[i].length())
            {
                columnLengths.put(i, a[i].length());
            }
        }));

        final StringBuilder formatString = new StringBuilder("");
        String flag = leftJustifiedRows ? "-" : "";
        columnLengths.entrySet().stream().forEach(e -> formatString.append("| %" + flag + e.getValue() + "s "));
        formatString.append("|\n");
        String line = columnLengths.entrySet().stream().reduce("", (ln, b) ->
        {
            String templn = "+-";
            templn = templn + Stream.iterate(0, (i -> i < b.getValue()), (i -> ++i)).reduce("", (ln1, b1) -> ln1 + "-",
                    (a1, b1) -> a1 + b1);
            templn = templn + "-";
            return ln + templn;
        }, (a, b) -> a + b);
        line = line + "+\n";
        System.out.print(line);
        Arrays.stream(table).limit(1).forEach(a -> System.out.printf(formatString.toString(), a));
        System.out.print(line);
        Stream.iterate(1, (i -> i < table.length), (i -> ++i))
                .forEach(a -> System.out.printf(formatString.toString(), table[a]));
        System.out.print(line);
    }
}

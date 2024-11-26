package Transformation.Selection;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NestedFunctionExtractor {

    // Method to extract and print top-level nested functions
    public static List<String> extractNestedFunctions(String formula) {
        List<String> nestedFunctions = new ArrayList<>();
        Stack<Integer> stack = new Stack<>();
        Map<Integer, Integer> matchingParentheses = new HashMap<>();
        
        // Identify matching parentheses using a stack
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c == '(') {
                stack.push(i);
            } else if (c == ')') {
                if (!stack.isEmpty()) {
                    int openIndex = stack.pop();
                    matchingParentheses.put(openIndex, i);
                }
            }
        }

        // Regex to identify valid function names (letters followed by parentheses)
        String functionPattern = "\\b([A-Za-z]+n|car|cdr|cons|select|store|atom|atoms)\\(";
        Pattern pattern = Pattern.compile(functionPattern);

        // List to store function bounds (start and end positions)
        List<int[]> functionBounds = new ArrayList<>();

        // Traverse the formula and extract complete functions
        Matcher matcher = pattern.matcher(formula);
        while (matcher.find()) {
            int start = matcher.start();
            if (matchingParentheses.containsKey(start + matcher.group().length() - 1)) {
                int end = matchingParentheses.get(start + matcher.group().length() - 1);
                functionBounds.add(new int[] { start, end });
            }
        }

        // Sort bounds by start position and length (descending for containment checking)
        functionBounds.sort((a, b) -> {
            if (a[0] != b[0]) {
                return Integer.compare(a[0], b[0]);
            }
            return Integer.compare(b[1], a[1]);
        });

        // Filter to retain only top-level functions
        List<int[]> topLevelBounds = new ArrayList<>();
        for (int[] bounds : functionBounds) {
            boolean isContained = false;
            for (int[] topLevel : topLevelBounds) {
                if (bounds[0] >= topLevel[0] && bounds[1] <= topLevel[1]) {
                    isContained = true;
                    break;
                }
            }
            if (!isContained) {
                topLevelBounds.add(bounds);
            }
        }

        // Extract top-level functions from the formula
        for (int[] bounds : topLevelBounds) {
            String fullFunction = formula.substring(bounds[0], bounds[1] + 1);
            nestedFunctions.add(fullFunction);
            System.out.println("Found nested function: " + fullFunction);
        }

        return nestedFunctions;
    }

    public static void main(String[] args) {
        // Array of complex formulas
        String[] formulas = {
            "(((a=b)&(Fn(x)=y)|(car(y)=z)&(cdr(z)=car(y))&(~(Hn(Fn(Gn(a,b)),z)=cons(x,y,z)))&(store(x)=((~(atom(z))))))->((a=b)&(Fn(x)=y)|(car(y)=z)&(cdr(z)=car(y))&(~(Hn(Fn(Gn(a,b)),z)=cons(x,y,z)))&(store(x)=((~(atom(z)))))))",
            "((p=store(x))|(Fn(a,b)=Hn(c))&(select(q)=z)&(~(Gn(z)))->((x=Fn(Hn(y)))|(car(z)=cons(a,b))))",
            "((atom(a))&(nil=Fn(x))|(select(store(a,b))=cdr(Fn(x,y))))",
            "(Hn(Fn(Gn(a,b)),c)=cons(x,car(Fn(b,c))))&(nil=Fn(Hn(z)))",
            "(~(store(a,b)=select(Fn(c,d))))|(nil=atom(Fn(Gn(x))))"
        };

        // Process each formula
        for (String formula : formulas) {
            System.out.println("------------------------------------------------------");
            System.out.println("Input formula: " + formula);
            System.out.println("Extracting nested functions:");
            extractNestedFunctions(formula);
        }
    }
}

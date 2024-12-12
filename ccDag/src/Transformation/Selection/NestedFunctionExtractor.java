package Transformation.Selection;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NestedFunctionExtractor {

    public static Map<String, String> functionMapping = new LinkedHashMap<>();
    public static Map<String, String> subFormulaMapping = new LinkedHashMap<>();

    /**
     * Extracts and maps nested functions like car, cdr, cons, or indexed functions.
     * @param formula Input string containing the formula
     * @return Map of indexed functions to their original expressions
     */
    public static Map<String, String> extractAndMapNestedFunctions(String formula) {
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

        // Regex to identify function calls
        String functionPattern = "\\b([A-Za-z]+n|car|cdr|cons|select|store|atom|atoms)\\(";
        Pattern pattern = Pattern.compile(functionPattern);

        List<int[]> functionBounds = new ArrayList<>();
        Matcher matcher = pattern.matcher(formula);

        while (matcher.find()) {
            int start = matcher.start();
            if (matchingParentheses.containsKey(start + matcher.group().length() - 1)) {
                int end = matchingParentheses.get(start + matcher.group().length() - 1);
                functionBounds.add(new int[]{start, end});
            }
        }

        StringBuilder updatedFormula = new StringBuilder(formula);
        int offset = 0;
        int counter = 0;

        for (int[] bounds : functionBounds) {
            String fullFunction = formula.substring(bounds[0], bounds[1] + 1);
            String functionVar = "f" + counter++; 
            functionMapping.put(functionVar, fullFunction);

            int adjustedStart = bounds[0] + offset;
            int adjustedEnd = bounds[1] + offset;
            updatedFormula.replace(adjustedStart, adjustedEnd + 1, functionVar);

            offset += functionVar.length() - fullFunction.length();
        }

        return functionMapping;
    }

    /**
     * Finds equality sub-formulas in a formula string.
     * @param formula Input formula string
     * @return List of equality sub-formulas found
     */
    public static List<String> findEqualitySubFormulas(String formula) {
        List<String> equalitySubFormulas = new ArrayList<>();
        String equalityPattern = "\\(~?\\([^()]*\\)\\)\\s*(=|!=)\\s*\\(~?\\([^()]*\\)\\)|" +  // 
                "\\(~?\\([^()]*\\)\\)\\s*(=|!=)\\s*[A-Za-z0-9_]+|" +  
                "[A-Za-z0-9_]+\\s*(=|!=)\\s*\\(~?\\([^()]*\\)\\)|" +
                "\\b([A-Za-z0-9_~(]+)\\s*(=|!=)\\s*([A-Za-z0-9_~(]+)\\b";

        Pattern pattern = Pattern.compile(equalityPattern);
        Matcher matcher = pattern.matcher(formula);

        while (matcher.find()) {
            String subFormula = matcher.group().trim();
            equalitySubFormulas.add(subFormula);
        }
        return equalitySubFormulas;
    }
}

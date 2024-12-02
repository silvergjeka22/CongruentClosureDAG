package Transformation.Selection;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NestedFunctionExtractor {

    public static Map<String, String> functionMapping = new LinkedHashMap<>();
    public static Map<String, String> subFormulaMapping = new LinkedHashMap<>();

    // Method to extract and map nested functions
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

        // Regex to identify valid function names (letters followed by parentheses)
        String functionPattern = "\\b([A-Za-z]+n|car|cdr|cons|select|store|atom|atoms)\\(";
        Pattern pattern = Pattern.compile(functionPattern);

        // List to store function bounds (start and end positions)
        List<int[]> functionBounds = new ArrayList<>();
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

        // Map and replace top-level functions in the formula
        StringBuilder updatedFormula = new StringBuilder(formula);
        int offset = 0; // Tracks length adjustments due to replacements
        int counter = 0;

        for (int[] bounds : topLevelBounds) {
            String fullFunction = formula.substring(bounds[0], bounds[1] + 1);
            String functionVar = "f" + counter++; // Assign variable name
            functionMapping.put(functionVar, fullFunction); // Store the mapping globally

            // Replace the function in the formula
            int adjustedStart = bounds[0] + offset;
            int adjustedEnd = bounds[1] + offset;
            updatedFormula.replace(adjustedStart, adjustedEnd + 1, functionVar);

            // Update the offset due to length change
            offset += functionVar.length() - fullFunction.length();
        }

        return functionMapping;
    }

    // Method to find equality sub-formulas
    public static List<String> findEqualitySubFormulas(String formula) {
        List<String> equalitySubFormulas = new ArrayList<>();
    
        // Updated regex to match equality or inequality sub-formulas with nested negations
        String equalityPattern = "\\(~?\\([^()]*\\)\\)\\s*(=|!=)\\s*\\(~?\\([^()]*\\)\\)|" +
                                 "\\(~?\\([^()]*\\)\\)\\s*(=|!=)\\s*[A-Za-z0-9_]+|" +
                                 "[A-Za-z0-9_]+\\s*(=|!=)\\s*\\(~?\\([^()]*\\)\\)|" +
                                 "\\b([A-Za-z0-9_~(]+)\\s*(=|!=)\\s*([A-Za-z0-9_~(]+)\\b";
    
        // Compile the regex
        Pattern pattern = Pattern.compile(equalityPattern);
        Matcher matcher = pattern.matcher(formula);
    
        while (matcher.find()) {
            // Extract and clean the matched sub-formula
            String subFormula = matcher.group().trim();
            equalitySubFormulas.add(subFormula);
        }
        return equalitySubFormulas;
    }

    // Method to replace equality sub-formulas with indexed terms (e0, e1, etc.)
    public static Map<String, String> replaceWithIndexedTerms(String formula, List<String> equalitySubFormulas) {
        String updatedFormula = formula;
        int counter = 0;
    
        // Replace equality sub-formulas with e0, e1, ...
        for (String subFormula : equalitySubFormulas) {
            String indexedTerm = "e" + counter++;
            subFormulaMapping.put(indexedTerm, subFormula); // Store globally
            updatedFormula = updatedFormula.replace(subFormula, indexedTerm);
        }
    
        subFormulaMapping.put("Updated Formula", updatedFormula); // Add the updated formula for consistency
        return subFormulaMapping;
    }

}

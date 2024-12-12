package Transformation.Selection;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import Transformation.Selection.ParserDnf;

public class NestedFunctionExtractor {

    public static Map<String, String> functionMapping = new LinkedHashMap<>();
    public static Map<String, String> subFormulaMapping = new LinkedHashMap<>();

    // Extract and map functions (e.g., car, cdr, cons, etc.)
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

        // Replace functions with indexed terms (f0, f1, etc.)
        StringBuilder updatedFormula = new StringBuilder(formula);
        int offset = 0;
        int counter = 0;

        for (int[] bounds : functionBounds) {
            String fullFunction = formula.substring(bounds[0], bounds[1] + 1);
            String functionVar = "f" + counter++; // Assign variable name
            functionMapping.put(functionVar, fullFunction); // Store mapping globally

            // Replace function in the formula
            int adjustedStart = bounds[0] + offset;
            int adjustedEnd = bounds[1] + offset;
            updatedFormula.replace(adjustedStart, adjustedEnd + 1, functionVar);

            // Update offset due to replacement
            offset += functionVar.length() - fullFunction.length();
        }

        return functionMapping;
    }

    // Find equality sub-formulas
    public static List<String> findEqualitySubFormulas(String formula) {
        List<String> equalitySubFormulas = new ArrayList<>();
        String equalityPattern = "\\(~?\\([^()]*\\)\\)\\s*(=|!=)\\s*\\(~?\\([^()]*\\)\\)|" +
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

    // Replace equality sub-formulas with indexed terms (e0, e1, etc.)
    public static String replaceWithIndexedTerms(String formula, List<String> equalitySubFormulas) {
        String updatedFormula = formula;
        int counter = 0;

        for (String subFormula : equalitySubFormulas) {
            String indexedTerm = "e" + counter++;
            subFormulaMapping.put(indexedTerm, subFormula); // Store globally
            updatedFormula = updatedFormula.replace(subFormula, indexedTerm);
        }

        return updatedFormula;
    }

    /* 

    public static String replaceComplexTerms(String formula, List<String> complexTerms) {
        String updatedFormula = formula;
        int counter = 0;
        boolean modified; // Track if changes were made in the loop
    
        do {
            modified = false; // Reset modification flag
    
            // Transform the formula and update it
            String transformedFormula = ParserDnf.transformFormula(updatedFormula);
            if (!transformedFormula.equals(updatedFormula)) {
                updatedFormula = transformedFormula;
                modified = true; // A change was made
            }
    
            // Replace complex terms with indexed terms
            for (String complexTerm : complexTerms) {
                if (updatedFormula.contains(complexTerm)) {
                    String indexedTerm = "c" + counter++;
                    subFormulaMapping.put(indexedTerm, complexTerm);
                    updatedFormula = updatedFormula.replace(complexTerm, indexedTerm);
                    modified = true; // A change was made
                }
            }
    
            System.out.println("Updated formula: " + updatedFormula);
    
        } while (modified && Pattern.compile("\\s*(=|!=)\\s*").matcher(updatedFormula).find());
    
        return updatedFormula;
    }

    */
    
    

}

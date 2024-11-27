package Transformation.Selection;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NestedFunctionExtractor {

    // Method to extract, map, and replace nested functions
    public static Map<String, String> extractAndMapNestedFunctions(String formula) {
        Stack<Integer> stack = new Stack<>();
        Map<Integer, Integer> matchingParentheses = new HashMap<>();
        Map<String, String> functionMapping = new LinkedHashMap<>();

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
            functionMapping.put(functionVar, fullFunction);

            // Replace the function in the formula
            int adjustedStart = bounds[0] + offset;
            int adjustedEnd = bounds[1] + offset;
            updatedFormula.replace(adjustedStart, adjustedEnd + 1, functionVar);

            // Update the offset due to length change
            offset += functionVar.length() - fullFunction.length();
        }

        System.out.println("Updated formula: " + updatedFormula);
        return functionMapping;
    }

    public static List<String> findEqualitySubFormulas(String formula) {
        List<String> equalitySubFormulas = new ArrayList<>();
        
        // Regex to match equality or inequality sub-formulas with potential nested negations
        String equalityPattern = "\\b([A-Za-z0-9_]+)\\s*(=|!=)\\s*~?\\(\\~?\\([^)]+\\)\\)|\\b([A-Za-z0-9_]+)\\s*(=|!=)\\s*([A-Za-z0-9_~(]+)\\b";
        
        // Compile the pattern to match either equality or inequality
        Pattern pattern = Pattern.compile(equalityPattern);
        Matcher matcher = pattern.matcher(formula);
    
        while (matcher.find()) {
            // Capture sub-formulas based on the regex matches
            String subFormula = matcher.group().trim();
            equalitySubFormulas.add(subFormula);
        }
    
        return equalitySubFormulas;
    }
    

    public static void main(String[] args) {
// Array of complex formulas
// Array of complex formulas
String[] formulas = {
    "(~(Fn(a,b)) = (~(Fn(c,d)))) | ((Gn(x,y) != cons(a,b)) & (Fn(z) = select(store(x))))",
    "(((atom(x) = (~(Gn(y,z))))) & ((p = q) | (r != s))) & ((select(a) = (~(Fn(a,b)))) | (store(x) = y))",
    "((Fn(a,b) = (~(Gn(c,d)))) & (select(x) != Fn(a,b))) | ((p = q) & (r != (~(Fn(x) = Gn(y))))))",
    "(((Hn(Fn(x,y), z) = (~(Gn(a,b)))) & (Fn(c,d) != store(x))) & (x = Fn(y,z)))",
    "((~(Fn(x,y)) = (~(Gn(z,w))))) & (Fn(a,b) = (~(select(c,d))))) | (x != (Fn(e,f)))",
    "((Fn(x,y) = (~(Hn(a,b,c)))) & (select(x) != (~(Gn(y,z))))) | ((Gn(a) != Fn(b,c)) & (car(d) = select(e)))",
    "(((~(Fn(a,b)) != c)) & (Fn(x,y) = Gn(z))) | (car(x) = Fn(y))) & (select(z) = (~(Fn(a,b))))",
    "(~((Fn(a,b) = Gn(c,d)) & (Hn(x,y) = Fn(z))) | (car(x) = (~(Fn(a,b)))))",
    "((~(Fn(a,b)) != (~(Gn(c,d)))) & (Fn(x,y) = Gn(z,w))) | (select(a) = (~(Fn(b,c))))",
    "(((Fn(a) = (~(Hn(b)))) & (Gn(x) = (~(Fn(c))))) | (Fn(d,e) = select(f))) & (car(g) = (~(Fn(h,i))))",
    "((~(Fn(a,b)) != Gn(x))) & ((Gn(y) != (~(Fn(z)))) | (Fn(x) = car(y)))) | (car(z) = (~(Fn(a,b))))",
    "(((Fn(a,b) = (~(Gn(c,d)))) | (select(x) != (~(Fn(a,b))))) & (Fn(x) = (Gn(a,b))))",
    "(~((select(x) = Fn(a)) | ((Fn(b) != (~(Gn(c))) & (select(d) = (~Fn(e))))))",
    "(((Fn(x,y) = (~(Gn(z)))) & (car(x) != (~(Fn(a,b)))))) | (Fn(y) != select(z))) & (x = (~(Gn(a,b))))",
    "(((Fn(a,b) = (~(Gn(c,d)))) & (select(x) != Fn(a,b))) | (Fn(y) != (select(a) = (~(Gn(z))))))",
    "((~(Fn(a,b)) = (~(Gn(c,d)))) & (Fn(x) = (~(select(y)))))) | ((p != q) & (Fn(z) = select(a)))",
    "(((~(Fn(a)) = (~(Fn(b)))) & (Gn(x) = (~(Fn(a,b)))))) & (Fn(c) != select(x))) | (Gn(y) = (~(Fn(z))))",
    "(((Fn(x,y) = (~(Gn(z)))) & (select(x) != (~(Gn(c,d)))))) | (select(a) = Fn(b))) & (Fn(a) != (~(Gn(y,z))))"
};




        // Process each formula
        for (String formula : formulas) {
            System.out.println("------------------------------------------------------");
            System.out.println("Input formula: " + formula);

            // Extract and map functions
            Map<String, String> mapping = extractAndMapNestedFunctions(formula);

            // Print function mappings
            System.out.println("Function mappings:");
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }

            // Updated formula with mappings applied
            String updatedFormula = formula;
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                updatedFormula = updatedFormula.replace(entry.getValue(), entry.getKey());
            }

            // Find sub-formulas with = or !=
            List<String> equalitySubFormulas = findEqualitySubFormulas(updatedFormula);
            System.out.println("Sub-formulas with = or !=:");
            for (String subFormula : equalitySubFormulas) {
                System.out.println(subFormula);
            }
        }
    }
}

package Transformation.Selection;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListTheory {

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

        // Simplify remaining expressions in the updated formula
        simplifyExpressions(updatedFormula);

        System.out.println("Updated formula: " + updatedFormula);
        return functionMapping;
    }

    // Method to simplify nested expressions like ((~(...))) -> (~(...)) or (~f7)
    private static void simplifyExpressions(StringBuilder formula) {
        String nestedPattern = "\\(\\((~?\\(?f\\d+\\)?)\\)\\)";
        Pattern pattern = Pattern.compile(nestedPattern);
        Matcher matcher = pattern.matcher(formula);

        while (matcher.find()) {
            String simplified = matcher.group(1);
            formula.replace(matcher.start(), matcher.end(), "(" + simplified + ")");
            matcher = pattern.matcher(formula); // Reapply to ensure full simplification
        }
    }

    // Method to find all sub-formulas with = or != in the updated formula
    public static List<String> findEqualitySubFormulas(String formula) {
        List<String> equalitySubFormulas = new ArrayList<>();
        // Regex to match binary equality or inequality
        String equalityPattern = "\\b([A-Za-z0-9_]+)\\s*(=|!=)\\s*([A-Za-z0-9_()~]+)\\b";
        Pattern pattern = Pattern.compile(equalityPattern);
        Matcher matcher = pattern.matcher(formula);

        while (matcher.find()) {
            String subFormula = matcher.group();
            equalitySubFormulas.add(subFormula);
        }

        return equalitySubFormulas;
    }

    public static void main(String[] args) {
        // Array of complex formulas
        String[] formulas = {
            "(((a=b)&(Fn(x)=y)|(car(y)=z)&(cdr(z)=car(y))&(~(Hn(Fn(Gn(a,b)),z)=cons(x,y,z)))&(store(x)=((~(atom(z))))))->((a=b)&(Fn(x)=y)|(car(y)=z)&(cdr(z)=car(y))&(~(Hn(Fn(Gn(a,b)),z)=cons(x,y,z)))&(store(x)=((~(atom(z)))))))",
            "(((p!=q)&(Fn(x)!=y)|(car(x)=z)&(cdr(z)=car(x))&(~(Hn(Fn(Gn(p,q)),z)!=cons(p,q,z)))&(store(p)!=((~(atom(q))))))->((p=q)&(Fn(x)=y)|(car(x)=z)&(cdr(z)=car(x))&(~(Hn(Fn(Gn(p,q)),z)=cons(p,q,z)))&(store(p)=((~(atom(q)))))))",
            "((x=y)&(~(z!=w))&((a=b)|(c=d))&(~(p!=q)))",
            "(((Fn(a)!=Fn(b))|(a=b))&(car(c)!=cdr(d))&(Fn(e)=Fn(f))&(~(Hn(Fn(g),Fn(h))=cons(i,j,k))))",
            "((a!=b)&(Fn(x)=y)|(Gn(a)=z)&(~(Hn(a,b)!=Fn(c,d))))",
            "((p!=q)&(Fn(a)!=Fn(b))&(car(x)=cdr(y))&(Fn(z)!=Gn(a,b)))",
            "(Fn(a)=b)&(Fn(x)!=car(y))&(~(cdr(z)!=Fn(cdr(y))))",
            "((Gn(a,b)!=Fn(c,d))&(car(a)!=cdr(b))|(Gn(c)=Fn(d))&(~(store(x)!=atom(y))))",
            "(((a=b)|(Fn(x)=y)&(Gn(z)=car(cdr(y)))&(~(Fn(a)!=Fn(b)))&(Fn(x)!=Gn(y))))",
            "((atom(x)!=nil)&(store(a,b)=select(Fn(c,d)))&(~(Gn(z)!=cons(a,b))))"
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

            // Simplify nested expressions in the updated formula
            StringBuilder updatedFormulaBuilder = new StringBuilder(updatedFormula);
            simplifyExpressions(updatedFormulaBuilder);
            updatedFormula = updatedFormulaBuilder.toString();

            // Find sub-formulas with = or !=
            List<String> equalitySubFormulas = findEqualitySubFormulas(updatedFormula);
            System.out.println("Sub-formulas with = or !=:");
            for (String subFormula : equalitySubFormulas) {
                System.out.println(subFormula);
            }
        }
    }
}

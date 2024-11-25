package Transformation.Selection;

import java.util.*;
import java.util.regex.*;

public class ListTheory {

    // Maps to store mappings from subformulas to variable names
    private Map<String, String> equalityMapping = new LinkedHashMap<>();
    private Map<String, String> arrayMapping = new LinkedHashMap<>();
    private Map<String, String> listMapping = new LinkedHashMap<>();

    private int equalityVarIndex = 0; // e0, e1, e2...
    private int arrayVarIndex = 0;    // a0, a1, a2...
    private int listVarIndex = 0;     // l0, l1, l2...

    public String transformFormula(String formula) {
        // Patterns for equality, array, and list operations
        Pattern equalityPattern = Pattern.compile("\\b\\w+\\([^)]*\\)\\s*=\\s*\\w+\\([^)]*\\)"); // e.g., car(x) = cdr(y)
        Pattern arrayPattern = Pattern.compile("\\b(select|store)\\([^)]*\\)");                // e.g., select(a, b, c)
        Pattern listPattern = Pattern.compile("\\b(car|cdr|cons|nil)\\([^)]*\\)");             // e.g., car(x), cons(x, y)

        // Replace equality formulas
        Matcher equalityMatcher = equalityPattern.matcher(formula);
        while (equalityMatcher.find()) {
            String subformula = equalityMatcher.group();
            if (!equalityMapping.containsKey(subformula)) {
                equalityMapping.put(subformula, "e" + equalityVarIndex);
                equalityVarIndex++;
            }
            formula = formula.replace(subformula, equalityMapping.get(subformula));
        }

        // Replace array operations
        Matcher arrayMatcher = arrayPattern.matcher(formula);
        while (arrayMatcher.find()) {
            String subformula = arrayMatcher.group();
            if (!arrayMapping.containsKey(subformula)) {
                arrayMapping.put(subformula, "a" + arrayVarIndex);
                arrayVarIndex++;
            }
            formula = formula.replace(subformula, arrayMapping.get(subformula));
        }

        // Replace list operations
        Matcher listMatcher = listPattern.matcher(formula);
        while (listMatcher.find()) {
            String subformula = listMatcher.group();
            if (!listMapping.containsKey(subformula)) {
                listMapping.put(subformula, "l" + listVarIndex);
                listVarIndex++;
            }
            formula = formula.replace(subformula, listMapping.get(subformula));
        }

        // Enclose the final formula in parentheses for consistency
        return "(" + formula + ")";
    }

    // Method to print all mappings
    public void printMappings() {
        System.out.println("Equality Mappings:");
        equalityMapping.forEach((k, v) -> System.out.println(v + " = (" + k + ")"));
        System.out.println("Array Mappings:");
        arrayMapping.forEach((k, v) -> System.out.println(v + " = (" + k + ")"));
        System.out.println("List Mappings:");
        listMapping.forEach((k, v) -> System.out.println(v + " = (" + k + ")"));
    }

    public static void main(String[] args) {
        ListTheory equalityTheory = new ListTheory();

        String[] formulas = {
                "(((~(car(x) = cdr(y))) & (f(x) = x)) | x & (select(a, b, c) & store(h)))",
                "((cons(x, y, z) = cdr(z)) | (f(x, y) = g(x)) | (select(store(z), x)))",
                "(((car(x) = nil) & (cdr(y) = cons(a, b))) | (store(a, b) = select(c, d, e)))"
        };

        for (String formula : formulas) {
            String transformedFormula = equalityTheory.transformFormula(formula);
            System.out.println("Original Formula: " + formula);
            System.out.println("Transformed Formula: " + transformedFormula);
            equalityTheory.printMappings();
            System.out.println();

            // Clear the mappings for the next formula
            equalityTheory.equalityMapping.clear();
            equalityTheory.arrayMapping.clear();
            equalityTheory.listMapping.clear();
            equalityTheory.equalityVarIndex = 0;
            equalityTheory.arrayVarIndex = 0;
            equalityTheory.listVarIndex = 0;
        }
    }
}

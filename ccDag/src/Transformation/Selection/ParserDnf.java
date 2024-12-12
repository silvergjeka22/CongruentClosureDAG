package Transformation.Selection;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserDnf {
    private String formula;

    // Constructor takes the formula as input
    public ParserDnf(String formula) {
        this.formula = formula;
    }

    // Method to transform the formula structure
    public static String transformFormula(String formula) {
        // Step 1.1: Normalize variables like (eX) to eX
        formula = formula.replaceAll("\\((e[0-9]+)\\)", "$1");

        // Step 1.2: Ensure negated variables like ~eX are written as (~eX)
        formula = formula.replaceAll("~(e[0-9]+)(?!\\))", "(~$1)");

        // Step 1.3: Wrap the entire formula in parentheses if they don't already exist
        // if (!formula.matches("^\\(.*\\)$")) {
        // formula = "(" + formula + ")";
        // }

        // Step 1.4: Handle negated formulas (~(formula)) properly to avoid
        // over-wrapping
        Pattern negatedFormulaPattern = Pattern.compile("~\\(([^()]+)\\)"); // Match negated formulas
        Matcher matcher = negatedFormulaPattern.matcher(formula);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String innerFormula = matcher.group(1); // Extract the inner formula
            // Recursively transform the inner formula
            String transformedInner = transformFormula(innerFormula); // Call the method with the formula as argument

            // If the inner formula is already wrapped in parentheses, no need to wrap again
            if (transformedInner.matches("^\\(.*\\)$")) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement("(~" + transformedInner + ")"));
            } else {
                // Otherwise, wrap the transformed inner formula with parentheses
                matcher.appendReplacement(sb, Matcher.quoteReplacement("(~(" + transformedInner + "))"));
            }
        }
        matcher.appendTail(sb);
        formula = sb.toString();

        // Step 1.5: Remove redundant parentheses around negated formulas
        formula = formula.replaceAll("\\(\\(~\\(([^()]+)\\)\\)\\)", "(~($1))");

        // Ensure logical operators like & and | are not overly wrapped in parentheses
        formula = formula.replaceAll("\\((e[0-9]+)\\) \\& \\((e[0-9]+)\\)", "$1 & $2");
        formula = formula.replaceAll("\\((e[0-9]+)\\) \\| \\((e[0-9]+)\\)", "$1 | $2");

        return formula;
    }

    // Enhanced processFormula to fix infinite loops and ensure proper
    // transformation
    public String processFormula() {
        // Step 1: Extract and map nested functions (Fn(a,b) => f0, etc.)
        NestedFunctionExtractor.extractAndMapNestedFunctions(formula);
        String updatedFormula = formula;

        // Replace extracted functions in the formula
        for (Map.Entry<String, String> entry : NestedFunctionExtractor.functionMapping.entrySet()) {
            updatedFormula = updatedFormula.replace(entry.getValue(), entry.getKey());
        }

        System.out.println("Updated Formula: " + updatedFormula);

        boolean modified;
        int equalityCounter = 0; // Counter for indexed equality terms
        //int complexCounter = 0; // Counter for complex indexed terms

        // Step 2: Iteratively process equality sub-formulas and complex terms
        do {
            modified = false;

            // 2.1: Replace equality sub-formulas (e.g., e0 = e1)
            List<String> equalitySubFormulas = NestedFunctionExtractor.findEqualitySubFormulas(updatedFormula);
            for (String subFormula : equalitySubFormulas) {
                String indexedTerm = "e" + equalityCounter++;
                NestedFunctionExtractor.subFormulaMapping.put(indexedTerm, subFormula);
                updatedFormula = updatedFormula.replace(subFormula, indexedTerm);
                modified = true;
            }

            /*
             * 2.2: Replace complex terms (e.g., c0 for nested equalities)
             * List<String> complexTerms =
             * NestedFunctionExtractor.findEqualitySubFormulas(updatedFormula);
             * for (String complexTerm : complexTerms) {
             * String indexedTerm = "c" + complexCounter++;
             * NestedFunctionExtractor.subFormulaMapping.put(indexedTerm, complexTerm);
             * updatedFormula = updatedFormula.replace(complexTerm, indexedTerm);
             * modified = true;
             * }
             */

            // 2.3: Transform the formula to normalize it
            String transformedFormula = ParserDnf.transformFormula(updatedFormula);
            if (!transformedFormula.equals(updatedFormula)) {
                updatedFormula = transformedFormula;
                modified = true;
            }

        } while (modified && Pattern.compile("\\s*(=|!=)\\s*").matcher(updatedFormula).find());

        return updatedFormula;
    }

    // Get all mappings (functions and equality)
    public Map<String, String> getMappings() {
        Map<String, String> allMappings = new LinkedHashMap<>();
        allMappings.putAll(NestedFunctionExtractor.functionMapping);
        allMappings.putAll(NestedFunctionExtractor.subFormulaMapping);
        return allMappings;
    }

    // Print all mappings for debugging or display
    public void printMappings() {
        System.out.println("Function Mappings:   | Equality Mappings:");
        Iterator<Map.Entry<String, String>> functionIterator = NestedFunctionExtractor.functionMapping.entrySet()
                .iterator();
        Iterator<Map.Entry<String, String>> equalityIterator = NestedFunctionExtractor.subFormulaMapping.entrySet()
                .iterator();

        while (functionIterator.hasNext() || equalityIterator.hasNext()) {
            String functionMappingEntry = functionIterator.hasNext() ? formatMapping(functionIterator.next()) : "";
            String equalityMappingEntry = equalityIterator.hasNext() ? formatMapping(equalityIterator.next()) : "";

            System.out.printf("%-20s | %-20s%n", functionMappingEntry, equalityMappingEntry);
        }
    }

    private String formatMapping(Map.Entry<String, String> entry) {
        return entry.getKey() + " = " + entry.getValue();
    }
}

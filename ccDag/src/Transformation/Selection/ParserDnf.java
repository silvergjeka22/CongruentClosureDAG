package Transformation.Selection;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserDnf {
    private String formula;
    private Map<String, String> functionMapping = new LinkedHashMap<>();
    private Map<String, String> subFormulaMapping = new LinkedHashMap<>();

    // Constructor takes the formula as input
    public ParserDnf(String formula) {
        this.formula = formula;
    }

    public static String transformFormula(String formula) {
        // Step 1.1: Normalize variables like (eX) to eX
        formula = formula.replaceAll("\\((e[0-9]+)\\)", "$1");

        // Step 1.2: Ensure negated variables like ~eX are written as (~eX)
        formula = formula.replaceAll("~(e[0-9]+)(?!\\))", "(~$1)");

        // Step 1.3: Wrap the entire formula in parentheses if they don't already exist
        if (!formula.matches("^\\(.*\\)$")) {
            formula = "(" + formula + ")";
        }

        // Step 1.4: Handle negated formulas (~(formula)) properly to avoid over-wrapping
        Pattern negatedFormulaPattern = Pattern.compile("~\\(([^()]+)\\)"); // Match negated formulas
        Matcher matcher = negatedFormulaPattern.matcher(formula);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String innerFormula = matcher.group(1); // Extract the inner formula
            // Recursively transform the inner formula
            String transformedInner = transformFormula(innerFormula);  // Call the method with the formula as argument

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

    // Parse and extract functions, then replace them in the formula
    public String processFormula() {
        NestedFunctionExtractor.extractAndMapNestedFunctions(formula);

        // Get function mappings and apply them to the formula
        String updatedFormula = formula;
        for (Map.Entry<String, String> entry : NestedFunctionExtractor.functionMapping.entrySet()) {
            updatedFormula = updatedFormula.replace(entry.getValue(), entry.getKey());
        }

        // Find equality sub-formulas
        List<String> equalitySubFormulas = NestedFunctionExtractor.findEqualitySubFormulas(updatedFormula);

        // Replace equality sub-formulas with indexed terms
        NestedFunctionExtractor.replaceWithIndexedTerms(updatedFormula, equalitySubFormulas);

        return NestedFunctionExtractor.subFormulaMapping.get("Updated Formula");
    }

    // Get all mappings (functions and equality)
    public Map<String, String> getMappings() {
        Map<String, String> allMappings = new LinkedHashMap<>();
        allMappings.putAll(NestedFunctionExtractor.functionMapping);
        allMappings.putAll(NestedFunctionExtractor.subFormulaMapping);
        return allMappings;
    }

    // Print mappings in the desired format
    public void printMappings() {
        System.out.println("Function Mappings:   | Equality Mappings:");
        Iterator<Map.Entry<String, String>> functionIterator = NestedFunctionExtractor.functionMapping.entrySet().iterator();
        Iterator<Map.Entry<String, String>> equalityIterator = NestedFunctionExtractor.subFormulaMapping.entrySet().iterator();

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

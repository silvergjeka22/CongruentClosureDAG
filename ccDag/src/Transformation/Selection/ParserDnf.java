package Transformation.Selection;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserDnf {
    private String formula;

    // Constructor to initialize formula
    public ParserDnf(String formula) {
        this.formula = formula;
    }

    /**
     * Transforms logical formula to normalize and preprocess negations.
     * Handles logical negations and ensures proper transformations.
     * @param formula Input formula
     * @return Transformed formula
     */
    public static String transformFormula(String formula) {
        formula = formula.replaceAll("\\((e[0-9]+)\\)", "$1");
        formula = formula.replaceAll("~(e[0-9]+)(?!\\))", "(~$1)");

        Pattern negatedFormulaPattern = Pattern.compile("~\\(([^()]+)\\)");
        Matcher matcher = negatedFormulaPattern.matcher(formula);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String innerFormula = matcher.group(1);
            String transformedInner = transformFormula(innerFormula);
            matcher.appendReplacement(sb, Matcher.quoteReplacement("(~" + transformedInner + ")"));
        }
        matcher.appendTail(sb);
        formula = sb.toString();

        formula = formula.replaceAll("\\(\\(~\\(([^()]+)\\)\\)\\)", "(~($1))");
        formula = formula.replaceAll("\\((e[0-9]+)\\) \\& \\((e[0-9]+)\\)", "$1 & $2");
        formula = formula.replaceAll("\\((e[0-9]+)\\) \\| \\((e[0-9]+)\\)", "$1 | $2");

        return formula;
    }

    /**
     * Processes and normalizes a logical formula iteratively by replacing equality
     * sub-formulas and transforming the string.
     * @return Updated normalized formula string
     */
    public String processFormula() {
        NestedFunctionExtractor.extractAndMapNestedFunctions(formula);
        String updatedFormula = formula;

        for (Map.Entry<String, String> entry : NestedFunctionExtractor.functionMapping.entrySet()) {
            updatedFormula = updatedFormula.replace(entry.getValue(), entry.getKey());
        }

        System.out.println("Updated Formula: " + updatedFormula);

        boolean modified;
        int equalityCounter = 0;

        do {
            modified = false;
            List<String> equalitySubFormulas = NestedFunctionExtractor.findEqualitySubFormulas(updatedFormula);

            for (String subFormula : equalitySubFormulas) {
                String indexedTerm = "e" + equalityCounter++;
                NestedFunctionExtractor.subFormulaMapping.put(indexedTerm, subFormula);
                updatedFormula = updatedFormula.replace(subFormula, indexedTerm);
                modified = true;
            }

            String transformedFormula = ParserDnf.transformFormula(updatedFormula);
            if (!transformedFormula.equals(updatedFormula)) {
                updatedFormula = transformedFormula;
                modified = true;
            }
        } while (modified && Pattern.compile("\\s*(=|!=)\\s*").matcher(updatedFormula).find());

        return updatedFormula;
    }

    public Map<String, String> getMappings() {
        Map<String, String> allMappings = new LinkedHashMap<>();
        allMappings.putAll(NestedFunctionExtractor.functionMapping);
        allMappings.putAll(NestedFunctionExtractor.subFormulaMapping);
        return allMappings;
    }

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
        return entry.getKey() + " -> " + entry.getValue();
    }
}

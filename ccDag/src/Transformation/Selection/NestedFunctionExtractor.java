package Transformation.Selection;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NestedFunctionExtractor {

    public static Map<String, String> functionMapping = new LinkedHashMap<>();
    public static Map<String, String> subFormulaMapping = new LinkedHashMap<>();
    private static int counter = 0;

    /**
     * Extracts and maps nested functions bottom-up, ensuring proper replacement in
     * the formula.
     *
     * @param formula Input string containing the formula.
     * @return The updated formula with function mappings.
     */
    public static String extractAndMapNestedFunctions(String formula) {
        // Regex to identify innermost function calls
        String functionPattern = "\\b([A-Za-z]+n|car|cdr|cons|store|select|atom|atoms)\\([^()]*\\)";

        // Process formula until no more functions can be matched
        Pattern pattern = Pattern.compile(functionPattern);
        Matcher matcher = pattern.matcher(formula);

        while (matcher.find()) {
            String fullFunction = matcher.group();

            // Check if this function is already mapped
            String mappedVar = getMappedVariable(fullFunction);
            if (mappedVar == null) {
                // Create a new mapping for this function
                mappedVar = "f" + counter++;
                functionMapping.put(mappedVar, fullFunction);
            }

            // Replace the function in the formula
            formula = formula.replace(fullFunction, mappedVar);

            // Restart matching process on the updated formula
            matcher = pattern.matcher(formula);
        }

        return formula;
    }

    /**
     * Gets the mapped variable for a function, if it exists.
     *
     * @param function The full function string.
     * @return The mapped variable name, or null if not mapped.
     */
    private static String getMappedVariable(String function) {
        for (Map.Entry<String, String> entry : functionMapping.entrySet()) {
            if (entry.getValue().equals(function)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Finds equality sub-formulas in a formula string.
     * 
     * @param formula Input formula string
     * @return List of equality sub-formulas found
     */
    public static List<String> findEqualitySubFormulas(String formula) {
        List<String> equalitySubFormulas = new ArrayList<>();
        String equalityPattern = "\\(~?\\([^()]*\\)\\)\\s*(=|!=)\\s*\\(~?\\([^()]*\\)\\)|" + //
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

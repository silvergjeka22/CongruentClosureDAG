package Transformation.Selection;

import java.util.*;
import Transformation.Selection.NestedFunctionExtractor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingDnf {

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
            String transformedInner = transformFormula(innerFormula);

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

        return formula;
    }

    public static String processFormulas(String[] formulas) {
        String resultFormula = null;
        for (int i = 0; i < formulas.length; i++) {
            String formula = formulas[i];

            // Extract and map functions
            Map<String, String> mapping = NestedFunctionExtractor.extractAndMapNestedFunctions(formula);

            // Updated formula with function mappings applied
            String updatedFormula = formula;
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                updatedFormula = updatedFormula.replace(entry.getValue(), entry.getKey());
            }

            // Find sub-formulas with = or !=
            List<String> equalitySubFormulas = NestedFunctionExtractor.findEqualitySubFormulas(updatedFormula);

            // Replace equality sub-formulas with indexed terms (e0, e1, ...)
            Map<String, String> equalityMappings = NestedFunctionExtractor.replaceWithIndexedTerms(updatedFormula, equalitySubFormulas);

            // Print function and equality mappings together
            System.out.println("Function and Equality Mappings:");
    
            // Determine the maximum length for 'f' and 'e' to properly align
            int maxLengthF = 0;
            int maxLengthE = 0;
    
            // Calculate the longest string lengths for f and e to align them
            for (int j = 0; j < mapping.size(); j++) {
                String fKey = "f" + j;
                String fVal = mapping.get(fKey);
                String eVal = equalityMappings.get("e" + j);
                maxLengthF = Math.max(maxLengthF, (fKey + " = " + (fVal != null ? fVal : "")).length());
                maxLengthE = Math.max(maxLengthE, (eVal != null ? "e" + j + " = " + eVal : "").length());
            }
    
            // Print each mapping with aligned columns and a straight dividing line
            for (int j = 0; j < Math.max(mapping.size(), equalityMappings.size()); j++) {
                String fKey = "f" + j;
                String eKey = "e" + j;
                String fVal = mapping.get(fKey);
                String eVal = equalityMappings.get(eKey);
    
                String fMapping = (fVal != null ? fKey + " = " + fVal : "");
                String eMapping = (eVal != null ? eKey + " = " + eVal : "");
    
                // Format and align f and e mappings with a dividing line
                System.out.printf("%-" + (maxLengthF + 2) + "s| %-" + (maxLengthE + 2) + "s\n", fMapping, eMapping);
            }
    
            System.out.println("------------------------------------------------------");

            // Apply transformation to respect the specified syntax
            resultFormula = transformFormula(equalityMappings.get("Updated Formula"));
        }
        return resultFormula;  // Return the transformed formula
    }
}

package Transformation.Selection;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Transformation.DNF2.*;
import Transformation.Selection.ParserDnf;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ParserDag {

    // Global array to store split formulas
    private static String[] splitFormulas;

    private String[] formulas;

    public ParserDag(String[] formulas) {
        this.formulas = formulas;
    }

    public void execute() {
        splitFormulas = formulas;
        callAll(splitFormulas);
    }

    // TODO: Implement the function to get the explanations
    public static void getExpleantions() {
    }

    public static void callAll(String[] formulas) {

        System.out.println("Starting the transformation process...");
        System.out.println("Original formulas: " + Arrays.toString(formulas));

        // Loop through each formula, process it and print results
        for (String formula : formulas) {

            // Initialize the Calculator and ParserDnf with the formula
            Calculator calculator = new Calculator(formula);
            calculator.calculate();

            ParserDnf parser = new ParserDnf(formula);

            System.out.println("---------------------------------------------- ");

            // DNF
            String dnf = calculator.getDnfFormula();
            System.out.println("DNF: " + dnf);

            // parser.printMappings();

            // Mappings
            Map<String, String> mappings = parser.getMappings();

            // Insert the mappings into the DNF formula
            String updatedDnfFormula = insertMappingsIntoDnf(dnf, mappings);

            System.out.println("---------------------------------------------- ");
            System.out.println("Updated DNF formula: " + updatedDnfFormula);
            System.out.println("---------------------------------------------- ");
            // Call the split and print function
            splitFormulas = splitAndSetUpdatedDnf(updatedDnfFormula);
            for (String splitFormula : splitFormulas) {
                System.out.println(splitFormula);
            }

            String[] newForm = finalParser(splitFormulas);

            System.out.println("---------------------------------------------- ");
            for (String s : newForm) {
                System.out.println(s);
            }

            // Write the updated DNF formula to a file
            addToFile(newForm);
        }
    }

    /**
     * Writes each formula in the array to a separate file.
     * Each execution creates new files with incremented numbers in their names.
     *
     * @param splitFormulas The array of formatted formula parts.
     */
    public static void addToFile(String[] splitFormulas) {
        String baseFilePath = "src/CCAlgorithm/alredyDnfFiles/dnfFormula";

        for (int i = 0; i < splitFormulas.length; i++) {
            String filePath = baseFilePath + i + ".txt";
            File file = new File(filePath);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(splitFormulas[i]);
                System.out.println("Formula successfully written to file: " + file.getPath());
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }
        }
    }

    /**
     * Splits the updated DNF formula by the '|' operator and returns an array of
     * formatted strings.
     * 
     * @param updatedDnfFormula The formula to split and format.
     * @return An array of formatted formula parts.
     */
    public static String[] splitAndSetUpdatedDnf(String updatedDnfFormula) {
        // Split the updated DNF formula by the '|' operator
        String[] splitFormulas = updatedDnfFormula.split("\\|");

        // Create a new array to store the formatted formula parts
        String[] formattedFormulas = new String[splitFormulas.length];

        // Format each formula part separately after replacing '&' with ';'
        for (int j = 0; j < splitFormulas.length; j++) {
            String formulaPart = splitFormulas[j].trim();
            formulaPart = formulaPart.replace("&", ";"); // Replace '&' with ';'
            formattedFormulas[j] = formulaPart;
        }

        return formattedFormulas;
    }

    /**
     * Inserts mappings into the DNF formula.
     * 
     * @param dnfFormula The original DNF formula.
     * @param mappings   The mappings to insert.
     * @return The updated formula.
     */
    public static String insertMappingsIntoDnf(String dnfFormula, Map<String, String> mappings) {
        String updatedFormula = dnfFormula;
        boolean modified;

        // Process 'e' mappings first, iteratively
        do {
            modified = false; // Reset modification flag
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                // Only process mappings with 'e'
                if (key.startsWith("e")) {
                    // Ensure value is enclosed in parentheses
                    if (!value.startsWith("(" + key) && !value.endsWith(key + ")")) {
                        value = "(" + value + ")";
                        // System.out.println("Value: " + value);
                    }

                    // Use regex with word boundaries to replace only exact matches
                    String regexKey = "\\b" + key + "\\b";
                    if (updatedFormula.matches(".*" + regexKey + ".*")) {
                        updatedFormula = updatedFormula.replaceAll(regexKey, value);
                        modified = true; // Mark as modified for further iterations
                    }
                }
            }
        } while (modified); // Repeat until no more replacements for 'e'

        // Process 'f' mappings iteratively to fully expand all nested mappings
        do {
            modified = false; // Reset modification flag
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                // Only process mappings with 'f'
                if (key.startsWith("f")) {
                    // Use regex with word boundaries to replace only exact matches
                    String regexKey = "\\b" + key + "\\b";
                    if (updatedFormula.matches(".*" + regexKey + ".*")) {
                        updatedFormula = updatedFormula.replaceAll(regexKey, value);
                        modified = true; // Mark as modified for further iterations
                    }
                }
            }
        } while (modified); // Repeat until no more replacements for 'f'

        return updatedFormula;
    }

    public static String[] finalParser(String[] formulas) {
        Map<String, String> functionMapping = new HashMap<>();
        boolean modified;

        for (int i = 0; i < formulas.length; i++) {

            System.out.println("---------------------------------------------- ");
            System.out.println("Original Formula: " + formulas[i]);

            String formula = formulas[i];

            // Regex to identify innermost function calls
            String functionPattern = "\\b([A-Za-z]+n|car|cdr|cons|store|select|atom|atoms)\\([^()]*\\)";
            Pattern pattern = Pattern.compile(functionPattern);

            // Process innermost functions
            Matcher matcher = pattern.matcher(formula);
            while (matcher.find()) {
                String fullFunction = matcher.group();
                String mappedVar = functionMapping.get(fullFunction);
                if (mappedVar == null) {
                    mappedVar = "s" + functionMapping.size();
                    functionMapping.put(fullFunction, mappedVar);
                }
                formula = formula.replace(fullFunction, mappedVar);
                matcher = pattern.matcher(formula);
            }

            /*
             * Debug: Output function mappings
             * System.out.println("Function Mappings: ");
             * for (Map.Entry<String, String> entry : functionMapping.entrySet()) {
             * System.out.println(entry.getKey() + " -> " + entry.getValue());
             * }
             */

            // Handle negations like ~(sn) -> ~sn
            String negationPattern = "\\(~\\(s\\d+\\)\\)";
            Pattern negationRegex = Pattern.compile(negationPattern);
            Matcher negationMatcher = negationRegex.matcher(formula);
            while (negationMatcher.find()) {
                String transformedNegation = "~"
                        + negationMatcher.group().substring(3, negationMatcher.group().length() - 2);
                formula = formula.replace(negationMatcher.group(), transformedNegation);
            }

            // Remove outer parentheses
            if (formula.startsWith("(") && formula.endsWith(")")) {
                formula = formula.substring(1, formula.length() - 1).trim();
            }

            // Remove double parentheses
            String doubleParenthesesPattern = "\\(\\(([^()]+)\\)\\)";
            Pattern doubleParentheses = Pattern.compile(doubleParenthesesPattern);
            Matcher doubleParenthesesMatcher = doubleParentheses.matcher(formula);
            while (doubleParenthesesMatcher.find()) {
                String inner = doubleParenthesesMatcher.group(1).trim();
                formula = formula.replace(doubleParenthesesMatcher.group(), "(" + inner + ")");
            }

            // Adjust negation parentheses
            String negationParenthesesPattern = "\\(~\\(([^()]+)\\)\\)";
            Pattern negationParentheses = Pattern.compile(negationParenthesesPattern);
            Matcher negationParenthesesMatcher = negationParentheses.matcher(formula);
            while (negationParenthesesMatcher.find()) {
                String inner = negationParenthesesMatcher.group(1).trim();
                formula = formula.replace(negationParenthesesMatcher.group(), "~(" + inner + ")");
            }

            /*
             * Debugging output
             * System.out.println("Before De Morgan: " + formula);
             */

            // Apply De Morgan's laws
            formula = applyDeMorgan(formula);

            /*
             * Debugging output
             * System.out.println("After De Morgan: " + formula);
             */

            // Remove all extraneous parentheses and replace `~` with `-`
            formula = formula.replaceAll("[()]", "");
            formula = formula.replaceAll("~", "-");

            // Re-map all the functions until stable
            do {
                modified = false;
                for (Map.Entry<String, String> entry : functionMapping.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    String regexKey = "\\b" + value + "\\b";
                    if (formula.matches(".*" + regexKey + ".*")) {
                        formula = formula.replaceAll(regexKey, key);
                        modified = true;
                    }
                }
            } while (modified);

            // Update the formula in the array
            formulas[i] = formula;

            // Debugging output
            System.out.println("Processed Formula: " + formula);
        }

        return formulas;
    }

    public static String applyDeMorgan(String formula) {
        boolean modified;
        do {
            modified = false;

            /*
             * Double negation: ~(~X) -> X
             * String doubleNegationPattern = "~\\(~([^()]+)\\)";
             * Pattern doubleNegation = Pattern.compile(doubleNegationPattern);
             * Matcher doubleNegationMatcher = doubleNegation.matcher(formula);
             * while (doubleNegationMatcher.find()) {
             * String inner = doubleNegationMatcher.group(1).trim();
             * formula = formula.replace(doubleNegationMatcher.group(), inner);
             * modified = true;
             * break;
             * }
             * 
             */

            // TODO: make the de morgan for the functuon nefations like ~((a=b) = ~(c=d))

            // Negation of inequality: ~(s0 != s1) -> s0 = s1
            String negInequalityPattern = "~\\(([^()]+) != ([^()]+)\\)";
            Pattern negInequality = Pattern.compile(negInequalityPattern);
            Matcher negInequalityMatcher = negInequality.matcher(formula);
            while (negInequalityMatcher.find()) {
                String left = negInequalityMatcher.group(1).trim();
                String right = negInequalityMatcher.group(2).trim();
                String replacement = left + " = " + right;
                formula = formula.replace(negInequalityMatcher.group(), replacement);
                modified = true;
                break;
            }

            // Negation of equality: ~(s0 = s1) -> s0 != s1
            String negEqualityPattern = "~\\(([^()]+) = ([^()]+)\\)";
            Pattern negEquality = Pattern.compile(negEqualityPattern);
            Matcher negEqualityMatcher = negEquality.matcher(formula);
            while (negEqualityMatcher.find()) {
                String left = negEqualityMatcher.group(1).trim();
                String right = negEqualityMatcher.group(2).trim();
                String replacement = left + " != " + right;
                formula = formula.replace(negEqualityMatcher.group(), replacement);
                modified = true;
                break;
            }

            // Negation of inequality with negated terms: ~(~s0 != s1) -> ~s0 = s1
            String negInequalityNegatedPattern = "~\\(\\(~([^()]+)\\) != ([^()]+)\\)";
            Pattern negInequalityNegated = Pattern.compile(negInequalityNegatedPattern);
            Matcher negInequalityNegatedMatcher = negInequalityNegated.matcher(formula);
            while (negInequalityNegatedMatcher.find()) {
                String left = negInequalityNegatedMatcher.group(1).trim();
                String right = negInequalityNegatedMatcher.group(2).trim();
                String replacement = "~" + left + " = " + right;
                formula = formula.replace(negInequalityNegatedMatcher.group(), replacement);
                modified = true;
                break;
            }

            // Negation of equality with negated terms: ~(~s0 = s1) -> ~s0 != s1
            String negEqualityNegatedPattern = "~\\(\\(~([^()]+)\\) = ([^()]+)\\)";
            Pattern negEqualityNegated = Pattern.compile(negEqualityNegatedPattern);
            Matcher negEqualityNegatedMatcher = negEqualityNegated.matcher(formula);
            while (negEqualityNegatedMatcher.find()) {
                String left = negEqualityNegatedMatcher.group(1).trim();
                String right = negEqualityNegatedMatcher.group(2).trim();
                String replacement = "~" + left + " != " + right;
                formula = formula.replace(negEqualityNegatedMatcher.group(), replacement);
                modified = true;
                break;
            }

            /*
             * Double negation: ~(~X) -> X
             * String doubleNegationPattern = "~\\(~([^()]+)\\)";
             * Pattern doubleNegation = Pattern.compile(doubleNegationPattern);
             * Matcher doubleNegationMatcher = doubleNegation.matcher(formula);
             * while (doubleNegationMatcher.find()) {
             * String inner = doubleNegationMatcher.group(1).trim();
             * formula = formula.replace(doubleNegationMatcher.group(), inner);
             * modified = true;
             * break;
             * }
             */

            // Add additional patterns as necessary for edge cases

        } while (modified);

        return formula;
    }

}
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

    public static void main(String[] args) {
        // Array of formulas using functions like store, cons, car, cdr, etc.
        String[] formulas = {
                // "(Fn(p,q) = store(x,y) | ~((~(Fn(p,q))) = store(x,y)))",
                //"(store(x,y) = cons(a,b) & ((car(cons(d,e)) & cdr(a)) | (cdr(a) = cdr(a) & cdr(a) = cdr(a))))",
                // "(store(x,y) = cons(a,b) & (car(cons(d,e)) & cdr(a)))",
                // "(store(x,y) = cons(a,b) & car(cons(d,e)) = cdr(cons(a,b)))",

                // "((~(Fn(x,y))) != Fn(z,w) & store(a,b) != car(cons(c,d)))",

                // "(Fn(p,q) = store(x,y) | ~(Fn(a,b) != cons(c,d)))",
                // "(Fn(p,q) = store(x,y) | ~(Fn(p,q) = store(x,y)))",
                // "select(store(car(x),cdr(y)),x) = y",

                "Fn(p, Hn(p, Dn(q,s)))",

                // "Fn(p,q) = store(x,y) = (~(Fn(a,b) != cons(c,d)))",
                // " (~(Fn(p,q))) = (~(car(x))) != (~(Fn(a,b) != cons(c,d))) ",

                // " ~( (~(Fn(a,b))) != (~(cons(c,d))) ) ",
                // "~( (~(Fn(a,b))) = (~(cons(c,d))) )",
                // "~( Fn(a,b) = (~(cons(c,d))) )",
                // "~( (~(Fn(a,b))) = cons(c,d) )",
                // "~( Fn(a,b) != (~(cons(c,d))) )",
                // "~( (~(Fn(a,b))) != cons(c,d) )",
                // "~( (~(~(Fn(a,b))) != (~(cons(cdr(c),d))) )", TODO: fix this before making
                // the calclator transformation if there are 2 negations in updated formuala it
                // have to take off it

                // "~( ( ~ (~(Fn(a,b))) = (~(cons(c,d))) ) | (Fn(a,b) != (~(cons(c,d))) !=
                // Fn(a,b) = (~(cons(Fn(x,y),cdr(Fn(d)))))) )"

        };

        // Loop through each formula, process it and print results
        int count = 0;
        for (String formula : formulas) {
            System.out.println("---------------###  " + "Formula: " + count + "  ###----------------------- ");
            System.out.println("\n");
            count++;
            System.out.println("Processing formula: " + formula);

            // Initialize the Calculator and ParserDnf with the formula
            Calculator calculator = new Calculator(formula);
            calculator.calculate();

            ParserDnf parser = new ParserDnf(formula);

            System.out.println("---------------------------------------------- ");

            // DNF
            String dnf = calculator.getDnfFormula();
            System.out.println("DNF: " + dnf);

            System.out.println("---------------------------------------------- ");

            parser.printMappings();

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
            System.out.println("---------------------------------------------- ");
            parsingFial(splitFormulas);
            System.out.println("---------------------------------------------- ");
            // System.out.println("Applying De Morgan's laws and parsing negations...");
            // Apply De Morgan's laws and parse negations
            // String[] transformedFormulas = applyDeMorganAndParse(splitFormulas);
            // for (String transformedFormula : transformedFormulas) {
            // System.out.println(transformedFormula);
            // }

            // Write the updated DNF formula to a file
            addToFile(splitFormulas);
        }
    }

    

    /**
     * Writes the updated DNF formulas to separate files.
     * Each execution creates a new file with an incremented number in its name.
     *
     * @param splitFormulas The array of formatted formula parts.
     */
    public static void addToFile(String[] splitFormulas) {
        String baseFilePath = "src/CCAlgorithm/inputFile/dnfFormula";
        int fileIndex = 0;
        File file;

        // Find the next available file index
        do {
            file = new File(baseFilePath + fileIndex + ".txt");
            fileIndex++;
        } while (file.exists());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String formula : splitFormulas) {
                writer.write(formula);
                writer.write("#");
                writer.newLine();
            }
            System.out.println("Formulas successfully written to file: " + file.getPath());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
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

    public static String[] parsingFial(String[] formulas) {

        // map the functions
        Map<String, String> functionMapping = new HashMap<>();
        boolean modified;

        // Regex to identify innermost function calls
        String functionPattern = "\\b([A-Za-z]+n|car|cdr|cons|store|select|atom|atoms)\\([^()]*\\)";

        Pattern pattern = Pattern.compile(functionPattern);

        for (String formula : formulas) {
            Matcher matcher = pattern.matcher(formula);

            // Process formula until no more functions can be matched
            while (matcher.find()) {
                String fullFunction = matcher.group();

                // Check if this function is already mapped
                String mappedVar = functionMapping.get(fullFunction);
                if (mappedVar == null) {
                    // Create a new mapping for this function
                    mappedVar = "s" + functionMapping.size();
                    functionMapping.put(fullFunction, mappedVar);
                }

                // Replace the function in the formula
                formula = formula.replace(fullFunction, mappedVar);

                // Restart matching process on the updated formula
                matcher = pattern.matcher(formula);
            }

            // print all the mappings
            System.out.println("Function Mappings: ");
            for (Map.Entry<String, String> entry : functionMapping.entrySet()) {
                System.out.println(entry.getKey() + " -> " + entry.getValue());
            }

            System.out.println("Updated Formula: " + formula);

            // find formulas like (~(sn)) where n is a number
            String negationPattern = "\\(~\\(s\\d+\\)\\)";
            Pattern negation = Pattern.compile(negationPattern);
            Matcher negationMatcher = negation.matcher(formula);
            String fullNegation = "";
            while (negationMatcher.find()) {
                fullNegation = negationMatcher.group();
                System.out.println("Negation: " + fullNegation);
                if (!fullNegation.isEmpty()) {
                    // transform the fullNegation to be form (~(sn)) -> ~sn
                    String transformedNegation = "~" + fullNegation.substring(3, fullNegation.length() - 2);
                    formula = formula.replace(fullNegation, transformedNegation);
                    System.out.println("Transformed Negation: " + transformedNegation);
                }
            }

            // Remove outer parentheses if they exist
            if (formula.startsWith("(") && formula.endsWith(")")) {
                formula = formula.substring(1, formula.length() - 1).trim();
            }
            System.out.println("Updated Formula: " + formula);

            // take off the doble aparethesis like ((sn = sn)) and make it (sn = sn)
            String doubleParenthesesPattern = "\\(\\(([^()]+)\\)\\)";
            Pattern doubleParentheses = Pattern.compile(doubleParenthesesPattern);
            Matcher doubleParenthesesMatcher = doubleParentheses.matcher(formula);
            while (doubleParenthesesMatcher.find()) {
                String inner = doubleParenthesesMatcher.group(1).trim();
                formula = formula.replace(doubleParenthesesMatcher.group(), "(" + inner + ")");
            }
            // System.out.println("Updated Formula no (()): " + formula);

            // take off () if is written like (~(sn = sn)) and make ~(sn = sn)
            String negationParenthesesPattern = "\\(~\\(([^()]+)\\)\\)";
            Pattern negationParentheses = Pattern.compile(negationParenthesesPattern);
            Matcher negationParenthesesMatcher = negationParentheses.matcher(formula);
            while (negationParenthesesMatcher.find()) {
                String inner = negationParenthesesMatcher.group(1).trim();
                formula = formula.replace(negationParenthesesMatcher.group(), "~(" + inner + ")");
            }
            System.out.println("Updated Formula no (~()): " + formula);
            // Apply De Morgan's laws
            formula = applyDeMorgan(formula);
            System.out.println("Formula after applying De Morgan's laws: " + formula);

            // check if there are double () like ((sn = sn)) and make it (sn = sn)

            // take off all the ()
            formula = formula.replaceAll("[()]", "");

            // replace all the ~ with -
            formula = formula.replaceAll("~", "-");

            // lets remap all the functions

            do {
                modified = false;
                for (Map.Entry<String, String> entry : functionMapping.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    // Use regex with word boundaries to replace only exact matches
                    String regexKey = "\\b" + value + "\\b";
                    if (formula.matches(".*" + regexKey + ".*")) {
                        formula = formula.replaceAll(regexKey, key);
                        modified = true; // Mark as modified for further iterations
                    }
                }
            } while (modified); // Repeat until no more replacements for 's'

            System.out.println("Final Formula: " + formula);

            // clean the splitFormulas array
            for (int i = 0; i < splitFormulas.length; i++) {
                splitFormulas[i] = "";
            }

            // isert all the formla into the splitFormulas array based on formula

            for (int i = 0; i < splitFormulas.length; i++) {
                splitFormulas[i] = formula;
            }

            System.out.println("#################################################################");

            for (String splitFormula : splitFormulas) {
                System.out.println(splitFormula);
            }

            System.out.println("#################################################################");

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
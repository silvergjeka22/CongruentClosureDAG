package Transformation.Selection;

import java.util.*;
import Transformation.DNF2.*;
import Transformation.Selection.ParserDnf;

public class ParserDag {

    // Global array to store split formulas
    private static String[] splitFormulas;

    public static void main(String[] args) {
        // Array of formulas using functions like store, cons, car, cdr, etc.
        String[] formulas = {
                "(Fn(p,q) = store(x,y) | ~((~(Fn(p,q))) = store(x,y)))",
                // "(store(x,y) = cons(a,b) & ((car(cons(d,e)) & cdr(a)) | (cdr(a) = cdr(a) &
                // cdr(a) = cdr(a))))",
                // "(store(x,y) = cons(a,b) & (car(cons(d,e)) & cdr(a)))",
                // "(store(x,y) = cons(a,b) & car(cons(d,e)) = cdr(cons(a,b)))",

                // "((~(Fn(x,y))) != Fn(z,w) & store(a,b) != car(cons(c,d)))",

                // "(Fn(p,q) = store(x,y) | ~(Fn(a,b) != cons(c,d)))",
                // "(Fn(p,q) = store(x,y) | ~(Fn(p,q) = store(x,y)))",
                // "select(store(car(x),cdr(y)),x) = y",

                // "Fn(p, Hn(p, Dn(q,s)))",

                // "Fn(p,q) = store(x,y) = (~(Fn(a,b) != cons(c,d)))",
                // " (~(Fn(p,q))) = (~(car(x))) != (~(Fn(a,b) != cons(c,d))) "
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
            System.out.println("Applying De Morgan's laws and parsing negations...");
            // Apply De Morgan's laws and parse negations
            String[] transformedFormulas = applyDeMorganAndParse(splitFormulas);
            for (String transformedFormula : transformedFormulas) {
                System.out.println(transformedFormula);
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
                        System.out.println("Value: " + value);
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

    /**
     * Applies De Morgan's laws and parses negations in the formulas.
     * 
     * @param formulas The array of formulas to transform.
     * @return The array of transformed formulas.
     */
    public static String[] applyDeMorganAndParse(String[] formulas) {
        String[] transformedFormulas = new String[formulas.length];
        for (int i = 0; i < formulas.length; i++) {
            String formula = formulas[i];

            // Apply double negation elimination: ~(~A) -> A
            formula = formula.replaceAll("~\\(~(.*?)\\)", "$1");

            // Apply De Morgan's laws for conjunctions: ~(A & B) -> (~A | ~B)
            formula = formula.replaceAll("~\\((.*?)\\s*&\\s*(.*?)\\)", "(~$1 | ~$2)");

            // Apply De Morgan's laws for disjunctions: ~(A | B) -> (~A & ~B)
            formula = formula.replaceAll("~\\((.*?)\\s*\\|\\s*(.*?)\\)", "(~$1 & ~$2)");

            // Negation of equalities: ~(A = B) -> A != B
            formula = formula.replaceAll("~\\((.*?)\\s*=\\s*(.*?)\\)", "$1 != $2");

            // Negation of inequalities: ~(A != B) -> A = B
            formula = formula.replaceAll("~\\((.*?)\\s*!=\\s*(.*?)\\)", "$1 = $2");

            // Replace all remaining standalone negations (~) with a logical NOT symbol (-)
            formula = formula.replaceAll("~", "-");

            // Add any additional parsing rules for better readability if needed

            // Store the transformed formula
            transformedFormulas[i] = formula;
        }
        return transformedFormulas;
    }
}

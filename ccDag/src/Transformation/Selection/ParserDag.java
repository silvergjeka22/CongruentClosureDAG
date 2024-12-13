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
            splitAndSetUpdatedDnf(updatedDnfFormula);
            System.out.println("---------------------------------------------- ");
            System.out.println("Applying De Morgan's laws and parsing negations...");
            // Apply De Morgan's laws and parse negations
            String[] transformedFormulas = applyDeMorganAndParse(splitFormulas);
            for (String transformedFormula : transformedFormulas) {
                System.out.println("Transformed formula: " + transformedFormula);
            }
        }
    }

    /**
     * Splits the updated DNF formula by the '|' operator and sets the global array.
     * 
     * @param updatedDnfFormula The formula to split and set.
     */
    public static void splitAndSetUpdatedDnf(String updatedDnfFormula) {
        // Split the updated DNF formula by the '|' operator
        splitFormulas = updatedDnfFormula.split("\\|");

        // Print each formula part separately after replacing '&' with ';'
        System.out.println("Split formulas:");
        for (int j = 0; j < splitFormulas.length; j++) {
            String formulaPart = splitFormulas[j].trim();
            formulaPart = formulaPart.replace("&", ";"); // Replace '&' with ';'
            System.out.println("Formula " + (j + 1) + ": " + formulaPart);
        }
        System.out.println("\n");
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
                    if (!value.startsWith("(")) {
                        value = "(" + value + ")";
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
                    // Ensure value is enclosed in parentheses
                    // if (!value.startsWith("(")) {
                    // value = "(" + value + ")";
                    // }

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

            // Replace double negations
            formula = formula.replaceAll("~\\(~(.*?)\\)", "$1");

            // Replace negations of equalities
            formula = formula.replaceAll("~\\((.*?)=(.*?)\\)", "-$1!=$2");

            // Replace other negations
            formula = formula.replaceAll("~", "-");

            // TODO: Parsing to take off some () and make it more readable

            // Store the transformed formula
            transformedFormulas[i] = formula;
        }
        return transformedFormulas;
    }
}

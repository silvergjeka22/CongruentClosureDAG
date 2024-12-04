package Transformation.Selection;

import java.util.*;
import Transformation.DNF2.*;
import Transformation.Selection.ParserDnf;

public class ParserDag {

    public static void main(String[] args) {
        // Array of formulas using functions like store, cons, car, cdr, etc.
        String[] formulas = {
            "((~(Fn(p,q))) = (~(Fn(r,s)))) & ((Fn(a) != cons(b,c)) | (~(Fn(d,e))) = Fn(f,g))",
            "(store(x,y) = cons(a,b)) & (car(cons(d,e)) = cdr(cons(a,b))",
            "((~(Fn(x,y))) = (~(Fn(z,w)))) & (store(a,b) != car(cons(c,d)))",
            "(Fn(p,q) = store(x,y)) | ~(Fn(a,b) != cons(c,d))"
        };
        
        // Loop through each formula, process it and print results
        for (String formula : formulas) {
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

            // Mappings
            Map<String, String> mappings = parser.getMappings();
            // Print mappings if needed
            /* 
            System.out.println("Mappings:");
            mappings.forEach((key, value) -> System.out.println(key + " -> " + value));
            */

            // Insert the mappings into the DNF formula
            String updatedDnfFormula = insertMappingsIntoDnf(dnf, mappings);

            System.out.println("---------------------------------------------- ");
            System.out.println("Updated DNF with mappings:");
            System.out.println(updatedDnfFormula);
            System.out.println("\n");
        }
    }

    // Method to insert mappings containing 'e' and 'f' into the DNF formula
    public static String insertMappingsIntoDnf(String dnfFormula, Map<String, String> mappings) {
        String updatedFormula = dnfFormula;

        // First, handle the 'e' mappings (replace e0, e1, etc. with their corresponding values inside parentheses)
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // If the mapping key contains 'e', replace it inside parentheses
            if (key.contains("e")) {
                // Check if the value is already inside parentheses
                if (!value.startsWith("(")) {
                    value = "(" + value + ")";
                }
                updatedFormula = updatedFormula.replace(key, value);
            }
        }

        // Then, handle the 'f' mappings (replace f0, f1, etc. with their corresponding values inside parentheses)
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // If the mapping key contains 'f', replace it inside parentheses
            if (key.contains("f")) {
                // Check if the value is already inside parentheses
                if (!value.startsWith("(")) {
                    value = "(" + value + ")";
                }
                updatedFormula = updatedFormula.replace(key, value);
            }
        }

        return updatedFormula;
    }
}

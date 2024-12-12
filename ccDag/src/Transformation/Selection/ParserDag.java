package Transformation.Selection;

import java.util.*;
import Transformation.DNF2.*;
import Transformation.Selection.ParserDnf;

public class ParserDag {

    public static void main(String[] args) {
        // Array of formulas using functions like store, cons, car, cdr, etc.
        String[] formulas = {
                //"(Fn(p,q) = store(x,y) | ~((~(Fn(p,q))) = store(x,y)))",
                //"(store(x,y) = cons(a,b) & ((car(cons(d,e)) & cdr(a))  | (cdr(a) = cdr(a) & cdr(a) = cdr(a))))",
                //"(store(x,y) = cons(a,b) & (car(cons(d,e)) & cdr(a)))",
                //"(store(x,y) = cons(a,b) & car(cons(d,e)) = cdr(cons(a,b)))",
                //"((~(Fn(x,y))) != Fn(z,w) & store(a,b) != car(cons(c,d)))",
                //"(Fn(p,q) = store(x,y) | ~(Fn(a,b) != cons(c,d)))",
                //"(Fn(p,q) = store(x,y) | ~(Fn(p,q) = store(x,y)))",

                "select(store(car(x),cdr(y)),x) = y",

                //"Fn(p,q) = store(x,y) = (~(Fn(a,b) != cons(c,d)))",
                //" (~(Fn(p,q))) = (~(car(x))) != (~(Fn(a,b) != cons(c,d))) "


                // TODO: When the formulas of equality are complex like ((&|)&|) = ((&|)&|), the program does not work
        };

        // Loop through each formula, process it and print results
        int count = 0;
        for (String formula : formulas) {
            System.out.println("---------------###  "+ "Formula: " + count +   "  ###----------------------- ");
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
            // Print mappings if needed
            /*
             * System.out.println("Mappings:");
             * mappings.forEach((key, value) -> System.out.println(key + " -> " + value));
             */

            // Insert the mappings into the DNF formula
            String updatedDnfFormula = insertMappingsIntoDnf(dnf, mappings);

            System.out.println("---------------------------------------------- ");
            //System.out.println("Updated DNF with mappings:");
            //System.out.println(updatedDnfFormula);

             // Split the updated DNF formula by the '|' operator
             String[] splitFormulas = updatedDnfFormula.split("\\|");

             // Print each formula part separately after replacing 'g' with ';'
             System.out.println("Split formulas:");
             for (int j = 0; j < splitFormulas.length; j++) {
                 String formulaPart = splitFormulas[j].trim();
                 formulaPart = formulaPart.replace("&", ";"); // Replace 'g' with ';'
                 System.out.println("Formula " + (j + 1) + ": " + formulaPart);
             }
             System.out.println("\n");
         }
    }

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
    
                    // Check if the formula contains the key and replace it
                    if (updatedFormula.contains(key)) {
                        updatedFormula = updatedFormula.replace(key, value);
                        modified = true; // Mark as modified for further iterations
                    }
                }
            }
        } while (modified); // Repeat until no more replacements for 'e'
    
        // Process 'f' mappings
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
    
            // Only process mappings with 'f'
            if (key.startsWith("f")) {
                // Ensure value is enclosed in parentheses
                //if (!value.startsWith("(")) {
                //    value = "(" + value + ")";
                //}
    
                // Replace the key with its mapped value
                updatedFormula = updatedFormula.replace(key, value);
            }
        }
    
        return updatedFormula;
    }
    
}

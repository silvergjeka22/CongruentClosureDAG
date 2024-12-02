package Transformation.Selection;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        String formula = "((~(Fn(p,q))) = (~(Fn(r,s)))) & ((Fn(a) != cons(b,c)) | (~(Fn(d,e))) = Fn(f,g)) & ((Fn(a) != cons(b,c)) | Fn(f,g) = (~(Fn(d,e))))";

        // Initialize the ParserDnf with the formula
        ParserDnf parser = new ParserDnf(formula);

        // Process the formula using processFormula to be with f and e
        String processedFormula = parser.processFormula();

        // Print the direct transformation of the formula to DNF
        String transformedFormula = ParserDnf.transformFormula(processedFormula);

        /*  Print the mappings generated
        Map<String, String> mappings = parser.getMappings();
        mappings.forEach((key, value) -> System.out.println(key + " -> " + value));
        */

        // printMappings
        parser.printMappings();


        System.out.println("Ready to be applyed:  " + transformedFormula);
    
    }
}

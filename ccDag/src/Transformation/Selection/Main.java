package Transformation.Selection;

import java.util.*;
import Transformation.DNF2.*;

public class Main {

    /* 
    public static void main(String[] args) {
        String formula = "((~(Fn(p,q))) = (~(Fn(r,s)))) & ((Fn(a) != cons(b,c)) | (~(Fn(d,e))) = Fn(f,g)) & ((Fn(a) != cons(b,c)) | (~(Fn(d,e))) = Fn(f,g))";

        // Initialize the ParserDnf with the formula
        ParserDnf parser = new ParserDnf(formula);

        // Process the formula using processFormula to be with f and e
        String processedFormula = parser.processFormula();

        // Print the direct transformation of the formula to DNF
        String transformedFormula = ParserDnf.transformFormula(processedFormula);

        /*  Print the mappings generated
        Map<String, String> mappings = parser.getMappings();
        mappings.forEach((key, value) -> System.out.println(key + " -> " + value));
        

        // printMappings
        parser.printMappings();


        System.out.println("Ready to be applyed:  " + transformedFormula);
    
    }

    */


    public static void main(String[] args) {
        String formula = "((~(Fn(p,q))) = (~(Fn(r,s)))) & ((Fn(a) != cons(b,c)) | (~(Fn(d,e))) = Fn(f,g))";
        Calculator calculator = new Calculator(formula);
        calculator.calculate();

        String dnf = calculator.getDnfFormula();
        System.out.println("DNF: " + dnf);

    }

}

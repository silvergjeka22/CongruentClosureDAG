package Transformation.Selection;

import java.util.*;
import Transformation.Selection.NestedFunctionExtractor;

public class ParsingDnf {

    public static String transformFormula(String formula) {
        // Step 1.1: Normalize variables like (x) to x
        formula = formula.replaceAll("\\((e[0-9]+)\\)", "$1");
    
        // Step 1.2: Ensure negated variables like ~eX are written as (~eX)
        formula = formula.replaceAll("~(e[0-9]+)(?!\\))", "(~$1)");

    
        return formula;
    }
    

    public static void processFormulas(String[] formulas) {
        for (int i = 0; i < formulas.length; i++) {
            String formula = formulas[i];
            System.out.println("------------------------------------------------------");
            System.out.println("Input formula: " + formula);

            // Extract and map functions
            Map<String, String> mapping = NestedFunctionExtractor.extractAndMapNestedFunctions(formula);

            // Print function mappings
            System.out.println("Function mappings:");
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }

            // Updated formula with mappings applied
            String updatedFormula = formula;
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                updatedFormula = updatedFormula.replace(entry.getValue(), entry.getKey());
            }

            // Find sub-formulas with = or !=
            List<String> equalitySubFormulas = NestedFunctionExtractor.findEqualitySubFormulas(updatedFormula);
            System.out.println("Sub-formulas with = or !=:");
            for (String subFormula : equalitySubFormulas) {
                System.out.println(subFormula);
            }

            // Replace equality sub-formulas with indexed terms (e0, e1, ...)
            String finalUpdatedFormula = NestedFunctionExtractor.replaceWithIndexedTerms(updatedFormula,
                    equalitySubFormulas);

            // Print the final updated formula
            System.out.println("Final updated formula: " + finalUpdatedFormula);

            // Apply transformation to respect the specified syntax
            String transformedFormula = transformFormula(finalUpdatedFormula);
            System.out.println("Transformed formula: " + transformedFormula);
            System.out.println("------------------------------------------------------");
        }
    }

    public static void main(String[] args) {
        String[] formulas = {
           "(e0) & (e1 | (e2)) | ~(e3)",
         
           /*
            "((~(Fn(a,b))) = (~(Fn(c,d)))) | ((Gn(x,y) != cons(a,b)) & (Fn(z) = select(store(x))))",
            "((~(select(store(a, b, c)))) = (~(Fn(Gn(x, y), z)))) & ((Fn(p) != cons(q, r)) | (~(cons(a, Fn(b, c)))) = Fn(~(store(x, y)), z)) | (~((select(x) != Fn(a, b))) = (~(Gn(c))))",
            "((~(Fn(p,q))) = (~(Fn(r,s)))) & ((Fn(a) != cons(b,c)) | (~(Fn(d,e))) = Fn(f,g))",
            "((~(Fn(a,b))) = (~(Fn(c,d)))) | (Gn(x) != select(store(y))) & ((Fn(z) != cons(a,b)) | (Fn(p,q) = store(x)))",
            "(Fn(x,y) != (~(Fn(a,b) = Fn(c,d)))) & (select(x) = select(y)) | (Fn(p) = cons(q, r))",
            "((~(Fn(p,q))) != Fn(r)) & (select(a,b) = (~(Fn(x,y)))) | (~(Fn(x) != Fn(y)))",
            "Fn(a) = select(store(a)) & ((~(Fn(x))) = (~(Gn(y)))) | (Fn(a,b) = cons(x,y))",
            "Fn(~(Fn(a,b))) = Fn(c,d) | ((~(Fn(e,f))) != cons(a,b)) & ((select(x) != Fn(y,z)) | Fn(p) = store(x))",
            "((~(Fn(a,b))) = (~(Fn(x,y)))) | (Fn(p,q) != select(store(a,b))) & (Fn(z) != cons(a,b))",
            "(select(store(x)) = Fn(a,b)) & ((Fn(a) != Fn(b)) | (Gn(a) = cons(b,c)))",
            "((~(Fn(p,q))) = (~(Fn(r,s)))) & ((Fn(a) != cons(b,c)) | (~(Fn(d,e))) = Fn(f,g))"
             */
    };

        // Process formulas
        processFormulas(formulas);
    }
}

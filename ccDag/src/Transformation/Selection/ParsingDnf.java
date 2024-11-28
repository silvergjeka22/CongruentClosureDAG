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
                "(e0) & (e1 | (e2)) | ~(e3&~(e4))",
                //"(e0) & (e1 | (e2)) | ~(e3&~(e4))",  // Nested negations
                //"~(e0 & ~(e1 | e2))",                // Multiple levels of negations
                //"~(~e0 & ~(e1 & e2))",               // Deeply nested negations
                //"~e0 | ~(~(e1))",                    // Single negations mixed with nested
         
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

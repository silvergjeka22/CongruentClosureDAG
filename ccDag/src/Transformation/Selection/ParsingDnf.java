package Transformation.Selection;

import java.util.*;
import Transformation.Selection.NestedFunctionExtractor;

public class ParsingDnf {

    // Method to transform the formula according to the specified rules
    public static String transformFormula(String formula) {
        // Remove spaces around operators and parentheses for consistency
        formula = formula.replaceAll("\\s+", "");

        // 1. Insert the formula in parentheses if not already enclosed in parentheses
        if (!formula.startsWith("(") && !formula.endsWith(")")) {
            formula = "(" + formula + ")";
        }

        // 2. Handle single vars like e1 without parentheses (e1 should appear as e1, not (e1))
        formula = formula.replaceAll("\\((e[0-9]+)\\)", "$1");

        // 3. If there is a '~', it should be written as (~(formula)) or (~var)
        formula = formula.replaceAll("~(\\w+)", "(~$1)");  // Handle simple variables with ~
        formula = formula.replaceAll("~\\(", "(~(");  // Handle negation of complex formulas

        // 4. Respect the formula syntax: (, ), &, |, ~, ->, <->
        // Ensure correct formatting for equality expressions (no spaces around =)
        formula = formula.replaceAll("(\\w+)\\s*(=|!=|->|<->)\\s*(\\w+)", "$1$2$3");

        // 5. Remove unnecessary parentheses around simple variables (e.g., (f0) should become f0)
        formula = formula.replaceAll("\\((e[0-9]+)\\)", "$1");

        // 6. Gripping the formula with parentheses where necessary for clarity
        // Add parentheses around grouped expressions that need to be grouped
        formula = formula.replaceAll("\\(([^()]+)\\)", "($1)");

        // 7. Ensure that equality expressions are formatted without unnecessary parentheses
        formula = formula.replaceAll("\\((f\\d+)\\)\\s*=\\s*(f\\d+)", "$1=$2");

        // 8. Ensure the entire formula is wrapped in parentheses
        if (!formula.startsWith("(") || !formula.endsWith(")")) {
            formula = "(" + formula + ")";
        }

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
            String finalUpdatedFormula = NestedFunctionExtractor.replaceWithIndexedTerms(updatedFormula, equalitySubFormulas);

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
            "((~(Fn(a,b))) = (~(Fn(c,d)))) | ((Gn(x,y) != cons(a,b)) & (Fn(z) = select(store(x))))",
            "((~(select(store(a, b, c)))) = (~(Fn(Gn(x, y), z)))) & ((Fn(p) != cons(q, r)) | (~(cons(a, Fn(b, c)))) = Fn(~(store(x, y)), z)) | (~((select(x) != Fn(a, b))) = (~(Gn(c))))",
            "((~(Fn(p,q))) = (~(Fn(r,s)))) & ((Fn(a) != cons(b,c)) | (~(Fn(d,e))) = Fn(f,g))",
            // New formulas
            "((~(Fn(a,b))) = (~(Fn(c,d)))) | (Gn(x) != select(store(y))) & ((Fn(z) != cons(a,b)) | (Fn(p,q) = store(x)))",
            "(Fn(x,y) != (~(Fn(a,b) = Fn(c,d)))) & (select(x) = select(y)) | (Fn(p) = cons(q, r))",
            "((~(Fn(p,q))) != Fn(r)) & (select(a,b) = (~(Fn(x,y)))) | (~(Fn(x) != Fn(y)))",
            "Fn(a) = select(store(a)) & ((~(Fn(x))) = (~(Gn(y)))) | (Fn(a,b) = cons(x,y))",
            "Fn(~(Fn(a,b))) = Fn(c,d) | ((~(Fn(e,f))) != cons(a,b)) & ((select(x) != Fn(y,z)) | Fn(p) = store(x))",
            "((~(Fn(a,b))) = (~(Fn(x,y)))) | (Fn(p,q) != select(store(a,b))) & (Fn(z) != cons(a,b))",
            "(select(store(x)) = Fn(a,b)) & ((Fn(a) != Fn(b)) | (Gn(a) = cons(b,c)))"
        };

        // Process formulas
        processFormulas(formulas);
    }
}

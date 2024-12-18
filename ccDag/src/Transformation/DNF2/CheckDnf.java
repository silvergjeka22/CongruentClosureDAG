package Transformation.DNF2;

public class CheckDNF {

    public static void main(String[] args) {
        String[] testFormulas = {
            "((A & (~B)) | C)",                       // Valid DNF
            "((A & (~B)) | ((~C) & D))",              // Valid DNF
            "((A | B) | C)",                          // Not DNF
            "((A & B) & C)",                          // Not DNF
            "((A & B) | (C & D))",                    // Valid DNF
            "((A | B) & C)",                          // Not DNF
            "((~A) | (B & C))",                       // Not DNF
            "(A | B)",                                // Valid DNF
            "A",                                      // Valid DNF
            "((A & (~B)) | C)",                       // Valid DNF
            "((X & (~Y)) | (Z & W) | (U & V))",       // Valid DNF
            "((P & (~Q)) | ((R & S) | (T & (~U))))",  // Valid DNF
            "((A & B & C) | (D & E))",                // Valid DNF
            "((A & B) | (C & (~D)) | (E & F))",       // Valid DNF
            "((A & B) | ((C & (~D)) | (E & F)))",     // Valid DNF
            "((A & B & C) | (D | E))",                // Not DNF
            "((X | Y) & (Z & W))",                    // Not DNF
            "(((A | B) & C) | D)",                    // Not DNF
            "((A & (B | C)) | D)",                    // Not DNF
            "((A | (B & C)) | D)",                    // Valid DNF
            "(((A & B) & C) | (D & E))",              // Not DNF
            "((P & (Q | R)) | (S & T))",              // Valid DNF
            "((A & B & C) | (D & (E & F)))",          // Valid DNF
            "((A & (~B)) | ((C & (~D)) & (E | F)))",  // Valid DNF
            "((A & B) | (C | (D & E)))",              // Not DNF
            "(((A | B) & C) | (D & E))"               // Not DNF
        };
        

        for (String formula : testFormulas) {
            boolean result = isInDNF(formula);
            System.out.println("Formula: \"" + formula + "\" is in DNF: " + result);
        }
    }

    public static boolean isInDNF(String formula) {
        formula = formula.trim();

        // Ensure the formula is fully enclosed in parentheses
        if (!formula.startsWith("(") || !formula.endsWith(")")) {
            return isLiteral(formula);
        }

        // Remove the outermost parentheses
        formula = removeOuterParentheses(formula);

        // Split the formula into disjunctions using '|'
        String[] disjunctions = formula.split("\\|");
        for (String clause : disjunctions) {
            clause = clause.trim();

            // Ensure each disjunction is enclosed in parentheses or is a single literal
            if (!isValidConjunction(clause)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidConjunction(String clause) {
        // Remove enclosing parentheses for clauses in "(...)" form
        if (clause.startsWith("(") && clause.endsWith(")")) {
            clause = removeOuterParentheses(clause);
        }

        // Split clause into literals using '&'
        String[] literals = clause.split("&");
        for (String literal : literals) {
            literal = literal.trim();

            // Check if the literal is valid: either a variable, a negated variable in parentheses, or enclosed parentheses
            if (!isLiteral(literal)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLiteral(String token) {
        // Check for a variable in form A, B, etc.
        if (token.matches("[a-zA-Z][a-zA-Z0-9]*")) {
            return true;
        }
        // Check for negated literals enclosed in parentheses
        if (token.matches("\\(~[a-zA-Z][a-zA-Z0-9]*\\)")) {
            return true;
        }
        return false;
    }

    private static String removeOuterParentheses(String formula) {
        // Remove one layer of outer parentheses if present
        if (formula.startsWith("(") && formula.endsWith(")")) {
            int openCount = 0;
            for (int i = 0; i < formula.length(); i++) {
                if (formula.charAt(i) == '(') openCount++;
                else if (formula.charAt(i) == ')') openCount--;

                // Ensure parentheses balance before the last character
                if (openCount == 0 && i < formula.length() - 1) {
                    return formula; // Not a fully enclosed formula
                }
            }
            return formula.substring(1, formula.length() - 1).trim();
        }
        return formula;
    }
}

package CCAlgo;

import CCAlgo.base.*;

import java.util.*;

public class FormulaParser {

    public static void parseFormula(String formula, CCobject ccobj) {
        String[] expressions = formula.split(";");
        for (String expr : expressions) {
            expr = expr.trim();
            if (expr.contains("!=")) {
                // Handle inequality
                String[] terms = expr.split("!=");
                String left = terms[0].trim();
                String right = terms[1].trim();
                ccobj.notEqualTerm.add(new TermPair(left, right));
                ccobj.atomTerm.add(left);
                ccobj.atomTerm.add(right);
            } else if (expr.contains("=")) {
                // Handle equality
                String[] terms = expr.split("=");
                String left = terms[0].trim();
                String right = terms[1].trim();

                // Handle function terms like Fn(x)
                if (right.matches("[a-zA-Z]+\\([a-zA-Z0-9,]+\\)")) {
                    // Detect function terms like Fn(x)
                    String fn = right.substring(0, right.indexOf("("));  // Extract function name (Fn)
                    String term = right.substring(right.indexOf("(") + 1, right.length() - 1);  // Extract the term inside parentheses

                    // Debug: Print to check if Fn(x) is detected
                    System.out.println("Detected function term: " + right);

                    // Add function term node to DAG
                    Node node = new Node(right, fn);
                    node.addArg(term);  // Add the argument of the function
                    ccobj.dag.put(node.getId(), node);  // Add to DAG
                    ccobj.atomTerm.add(fn + "(" + term + ")");  // Add to atom terms if necessary

                    // Debug output to confirm node addition
                    System.out.println("Added Node: " + node.getId()); 
                }

                // Handle other terms like cons or car/cdr
                if (right.startsWith("cons(") || right.startsWith("car(") || right.startsWith("cdr(")) {
                    // Handle cons terms and car/cdr
                    handleConsCarCdr(ccobj, right);
                }
            }
        }
    }

    private static void handleConsCarCdr(CCobject ccobj, String expr) {
        // Handle cons and car/cdr operations for the formula
        if (expr.startsWith("cons(")) {
            // Parse cons terms
            String[] args = parseConsArgs(expr);
            String left = args[0];
            String right = args[1];
            Node consNode = new Node("cons(" + left + "," + right + ")", "cons");
            consNode.addArg(left);
            consNode.addArg(right);
            ccobj.dag.put(consNode.getId(), consNode);
            System.out.println("Added Node: " + consNode.getId());
        } else if (expr.startsWith("car(") || expr.startsWith("cdr(")) {
            // Handle car/cdr operations
            String fn = expr.startsWith("car(") ? "car" : "cdr";
            String term = expr.substring(4, expr.length() - 1);
            Node node = new Node(fn + "(" + term + ")", fn);
            node.addArg(term);
            ccobj.dag.put(node.getId(), node);
            ccobj.atomTerm.add(fn + "(" + term + ")");
            System.out.println("Added Node: " + node.getId());
        }
    }

    private static String[] parseConsArgs(String consExpression) {
        // Extract arguments of the cons term
        consExpression = consExpression.substring(5, consExpression.length() - 1); // Remove "cons(" and ")"
        return consExpression.split(",");
    }
}

package CCAlgo;

import CCAlgo.base.*;

public class Main {

    public static void main(String[] args) throws Exception {
        // Initialize CCobject to hold DAG and formula components
        CCobject ccobj = new CCobject();

        // Define the formula
        String formula = "car(x)=car(y);cdr(x)=cdr(y);Fn(x)!=Fn(y);x=cons(u1,u2);y=cons(v1,v2);";

        // Parse the formula
        FormulaParser.parseFormula(formula, ccobj);

        // Run the Nelson-Oppen algorithm
        TermPair conflict = ccAlgorithm.NelsonOppen(
            ccobj.dag,
            ccobj.equalTerm,
            ccobj.notEqualTerm,
            ccobj.atomTerm,
            ccobj.consTerm
        );

        // Print results
        if (conflict == null) {
            System.out.println("Formula is SAT.");
        } else {
            System.out.println("Formula is UNSAT due to conflict: " + conflict);
        }
    }
}

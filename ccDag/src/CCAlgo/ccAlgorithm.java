package CCAlgo;

import CCAlgo.base.*;

import java.util.*;
import java.io.*;
import java.util.function.Supplier;


public class ccAlgorithm {

    private static Map<String, Node> dag;
    private static Set<TermPair> equalPred;
    private static Set<TermPair> notEqualPred;
    private static Set<String> atomPred;
    private static Set<String> consTerm;

    private static TermPair nelsonOppen_h() throws Exception {
        System.out.print("\rExecuting Congruent Closure Algorithm\t0%");

        // Step 1: Atom handling logic
        for (String id : atomPred) {
            if (consTerm.contains(id)) {
                System.out.println("\rConflict detected due to atom/cons term clash.");
                return new TermPair("atom(" + id + ")", id);
            }
        }

        for (String s : atomPred) {
            node(s).getBanned().addAll(consTerm);
        }
        for (String s : consTerm) {
            node(s).getBanned().addAll(atomPred);
        }

        // Step 2: Handle car/cdr projection axioms
        Node nTemp;
        TermPair ct = null;
        for (String s : consTerm) {
            nTemp = new Node("car(" + s + ")", "car");
            nTemp.addArg(s);
            node(s).addParent(nTemp.getId());
            dag.put(nTemp.getId(), nTemp);

            if ((ct = merge(nTemp.getId(), node(s).getArgs().get(0))) != null) {
                return ct;
            }

            nTemp = new Node("cdr(" + s + ")", "cdr");
            nTemp.addArg(s);
            node(s).addParent(nTemp.getId());
            dag.put(nTemp.getId(), nTemp);

            if ((ct = merge(nTemp.getId(), node(s).getArgs().get(1))) != null) {
                return ct;
            }
        }

        // Handle equality checks between terms
        float step = 100 / ((float) equalPred.size()), count = 0;
        int perc = 0;
        for (TermPair tp : equalPred) {
            count += step;
            for (; count >= perc && perc <= 100; perc++) {
                System.out.print("\rExecuting Congruent Closure Algorithm\t" + perc + "%");
            }

            if ((ct = merge(tp.getFirst(), tp.getSecond())) != null) {
                System.out.println("\nConflict detected with: " + tp.getFirst() + ", " + tp.getSecond());
                return ct;
            }
        }

        System.out.println("\rExecuting Congruent Closure Algorithm\t100%");
        return null;
    }

    public static TermPair NelsonOppen(
            Map<String, Node> Dag,
            Set<TermPair> EqualPred,
            Set<TermPair> NotEqualPred,
            Set<String> AtomPred,
            Set<String> ConsTerm) {

        dag = Dag;
        equalPred = EqualPred;
        notEqualPred = NotEqualPred;
        atomPred = AtomPred;
        consTerm = ConsTerm;

        try {
            return nelsonOppen_h();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static TermPair merge(String id1, String id2) throws Exception {
        System.out.println("\nAttempting to merge: " + id1 + " with " + id2);
        return merge_h(id1, id2);
    }

    private static TermPair merge_h(String id1, String id2) throws Exception {
        // Handle Array Axioms
        TermPair conflict = handleArrayAxioms(id1, id2);
        if (conflict != null) return conflict;

        // Standard merge logic
        if (!find(id1).equals(find(id2))) {
            if (node(find(id1)).getBanned().contains(id2)) {
                System.out.println("Merge blocked due to ban logic.");
                return new TermPair(id1, id2);
            }

            for (String ban : node(find(id1)).getBanned()) {
                if (find(ban).equals(find(id2))) {
                    System.out.println("Conflict found due to banned terms.");
                    return new TermPair(id1, id2);
                }
            }

            union(id1, id2);
        }
        return null;
    }

    private static TermPair handleArrayAxioms(String id1, String id2) throws Exception {
        // Select-Store Axiom: select(store(A, i, v), i) = v
        if (id1.startsWith("select(") && id2.startsWith("store(")) {
            String[] selectArgs = parseArgs(id1); // select(A, i)
            String[] storeArgs = parseArgs(id2);  // store(A, i, v)

            if (selectArgs[0].equals(storeArgs[0]) && selectArgs[1].equals(storeArgs[1])) {
                return merge(selectArgs[2], storeArgs[2]); // Enforce equality
            }
        }
        return null;
    }

    private static String[] parseArgs(String term) {
        int start = term.indexOf('(') + 1;
        int end = term.lastIndexOf(')');
        return term.substring(start, end).split(",");
    }

    private static Node node(String id) throws Exception {
        Node n = dag.get(id);
        if (n == null) {
            throw new NullPointerException("Node " + id + " does not exist.");
        }
        return n;
    }

    private static String find(String id) throws Exception {
        Node n = node(id);
        if (!id.equals(n.getFind())) {
            n.setFind(find(n.getFind()));
        }
        return n.getFind();
    }

    private static void union(String id1, String id2) throws Exception {
        Node n1 = node(find(id1));
        Node n2 = node(find(id2));

        if (n1.getRank() > n2.getRank()) {
            linkHeuristic(n1, n2);
        } else {
            linkHeuristic(n2, n1);
            if (n1.getRank() == n2.getRank()) {
                n2.setRank(n2.getRank() + 1);
            }
        }
    }

    private static void linkHeuristic(Node n1, Node n2) throws Exception {
        n2.setFind(n1.getFind());
        n1.getParents().addAll(n2.getParents());
        n2.setParents(new HashSet<>());

        n1.getBanned().addAll(n2.getBanned());
        n2.setBanned(new HashSet<>());
    }
}

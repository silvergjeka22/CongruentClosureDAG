package CCAlgo;

import CCAlgo.base.*;

import java.util.*;
import java.io.*;

public class ccAlgorithm {

    private static Map<String, Node> dag;
    private static Set<TermPair> equalPred;
    private static Set<TermPair> notEqualPred;
    private static Set<String> atomPred;
    private static Set<String> consTerm;

    public static TermPair NelsonOppen(
            Map<String, Node> Dag, Set<TermPair> EqualPred,
            Set<TermPair> NotEqualPred, Set<String> AtomPred,
            Set<String> ConsTerm) throws Exception {

        dag = Dag;
        equalPred = EqualPred;
        notEqualPred = NotEqualPred;
        atomPred = AtomPred;
        consTerm = ConsTerm;

        return nelsonOppen_h();
    }

    private static TermPair nelsonOppen_h() throws Exception {
        System.out.print("\rExecuting Congruent Closure Algorithm\t0%");
        for (String id : atomPred) {
            if (consTerm.contains(id)) {
                System.out.println("\rExecuting Congruent Closure Algorithm\t0%");
                return new TermPair("atom(" + id + ")", id);
            }
        }

        for (String s : atomPred) {
            node(s).getBanned().addAll(consTerm);
        }

        for (String s : consTerm) {
            node(s).getBanned().addAll(atomPred);
        }

        Node nTemp;
        TermPair ct = null;
        for (String s : consTerm) {
            nTemp = new Node("car(" + s + ")", "car");
            nTemp.addArg(s);
            node(s).addParent(nTemp.getId());
            dag.put(nTemp.getId(), nTemp);
            if ((ct = merge_h(nTemp.getId(), node(s).getArgs().get(0))) != null) {
                System.out.println("\rExecuting Congruent Closure Algorithm\t0%");
                return ct;
            }
            nTemp = new Node("cdr(" + s + ")", "cdr");
            nTemp.addArg(s);
            node(s).addParent(nTemp.getId());
            dag.put(nTemp.getId(), nTemp);
            if ((ct = merge_h(nTemp.getId(), node(s).getArgs().get(1))) != null) {
                System.out.println("\rExecuting Congruent Closure Algorithm\t0%");
                return ct;
            }
        }

        float step = 100 / ((float) equalPred.size()), count = 0;
        int perc = 0;
        for (TermPair tp : equalPred) {
            count += step;
            for (; count >= perc && perc <= 100; perc++)
                System.out.print("\rExecuting Congruent Closure Algorithm\t" + perc + "%");
            if ((ct = merge_h(tp.getFirst(), tp.getSecond())) != null) {
                System.out.println();
                return ct;
            }
        }
        System.out.println("\rExecuting Congruent Closure Algorithm\t100%");
        return null;
    }

    private static TermPair merge_h(String id1, String id2) throws Exception {
        if (!find_h(id1).equals(find_h(id2))) {
            if (node(find_h(id1)).getBanned().contains(id2))
                return new TermPair(id1, id2);
            for (String ban : node(find_h(id1)).getBanned()) {
                if (find_h(ban).equals(find_h(id2)))
                    return new TermPair(id1, id2);
            }
            for (String ban : node(find_h(id2)).getBanned()) {
                if (find_h(ban).equals(find_h(id1)))
                    return new TermPair(id1, id2);
            }

            Object[] p1 = ccpar(id1).toArray();
            Object[] p2 = ccpar(id2).toArray();
            union_h(id1, id2);
            String t1, t2;
            TermPair conflictTerms;
            for (int i = 0; i < p1.length; i++) {
                t1 = (String) p1[i];
                for (int j = 0; j < p2.length; j++) {
                    t2 = (String) p2[j];
                    if (!find_h(t1).equals(find_h(t2)) && congruent(t1, t2)) {
                        if ((conflictTerms = merge_h(t1, t2)) != null)
                            return conflictTerms;
                    }
                }
            }
        }
        return null;
    }

    private static String find_h(String id) throws Exception {
        Node n = node(id);
        if (!id.equals(n.getFind()))
            n.setFind(find_h(n.getFind()));
        return n.getFind();
    }

    private static void union_h(String id1, String id2) throws Exception {
        Node n1 = node(find_h(id1));
        Node n2 = node(find_h(id2));
        if (n1.getRank() > n2.getRank())
            linkHeuristic(n1, n2);
        else {
            linkHeuristic(n2, n1);
            if (n1.getRank() == n2.getRank())
                n2.setRank(n2.getRank() + 1);
        }
    }

    private static void linkHeuristic(Node n1, Node n2) throws Exception {
        n2.setFind(n1.getFind());
        n1.getParents().addAll(n2.getParents());
        n2.setParents(new HashSet<>());
    }

    private static boolean congruent(String id1, String id2) throws Exception {
        Node n1 = node(id1);
        Node n2 = node(id2);
        if (n1.getFn().equals(n2.getFn())) {
            List<String> arg1 = n1.getArgs();
            List<String> arg2 = n2.getArgs();
            if (arg1.size() == arg2.size()) {
                for (int i = 0; i < arg1.size(); i++)
                    if (!find_h(arg1.get(i)).equals(find_h(arg2.get(i))))
                        return false;
                return true;
            }
        }
        return false;
    }

    private static Node node(String id) throws Exception {
        Node n = dag.get(id);
        if (n == null)
            throw new NullPointerException("Does not exist any node with ID: " + id);
        return n;
    }

    private static Set<String> ccpar(String id) throws Exception {
        return node(find_h(id)).getParents();
    }
}

package CCAlgorithm;

import CCAlgorithm.bean.*;
import java.util.*;
import java.io.*;

public class CongruentClosureAlgorithm {

	private static Map<String, Node> dag;
	private static Set<TermPair> equalPred;
	private static Set<TermPair> notEqualPred;
	private static Set<String> atomPred;
	private static Set<String> consTerm;

	public static TermPair NelsonOppen(Map<String, Node> Dag, Set<TermPair> EqualPred,
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
		// Step 1: Handle the atom and consTerm axioms as in the original code
		for (String id : atomPred)
			if (consTerm.contains(id)) {
				System.out.println("\rExecuting Congruent Closure Algorithm\t0%");
				return new TermPair("atom(" + id + ")", id);
			}
		for (String s : atomPred)
			node(s).getBanned().addAll(consTerm);
		for (String s : consTerm)
			node(s).getBanned().addAll(atomPred);

		// Step 2: Handle car/cdr projection axioms as in the original code
		Node nTemp;
		TermPair ct = null;
		for (String s : consTerm) {
			nTemp = new Node("car(" + s + ")", "car");
			nTemp.addArg(s);
			node(s).addParent(nTemp.getId());
			dag.put(nTemp.getId(), nTemp);
			if ((ct = merge(nTemp.getId(), node(s).getArgs().get(0))) != null) {
				System.out.println("\rExecuting Congruent Closure Algorithm\t0%");
				return ct;
			}
			nTemp = new Node("cdr(" + s + ")", "cdr");
			nTemp.addArg(s);
			node(s).addParent(nTemp.getId());
			dag.put(nTemp.getId(), nTemp);
			if ((ct = merge(nTemp.getId(), node(s).getArgs().get(1))) != null) {
				System.out.println("\rExecuting Congruent Closure Algorithm\t0%");
				return ct;
			}
		}

		// Step 3: Equality terms in the same congruent class (i1 = j)
		float step = 100 / ((float) equalPred.size()), count = 0;
		int perc = 0;
		for (TermPair tp : equalPred) {
			count += step;
			for (; count >= perc && perc <= 100; perc++)
				System.out.print("\rExecuting Congruent Closure Algorithm\t" + perc + "%");
			if ((ct = merge(tp.getFirst(), tp.getSecond())) != null) {
				System.out.println();
				return ct;
			}
		}

		// Step 4: Handle inequality relations (i1 != i2)
		for (TermPair tp : notEqualPred) {
			String id1 = tp.getFirst();
			String id2 = tp.getSecond();
			if (find(id1).equals(find(id2))) {
				return new TermPair(id1, id2); // Conflict found, formula is unsatisfiable
			}
		}

		// Array Axioms: Process select/store relations
		// Step 5: Handle select operations
		for (String s : dag.keySet()) {
			Node n = node(s);
			if (n.getFn().equals("select") && n.getArgs().size() == 2) {
				String storeNode = n.getArgs().get(0);
				String index = n.getArgs().get(1);

				TermPair conflict = processSelect(storeNode, index, s);
				if (conflict != null) {
					return conflict; // Conflict found in select operation
				}
			}
		}

		// Step 6: Process store operations, handling nested stores
		for (String s : dag.keySet()) {
			Node n = node(s);
			if (n.getFn().equals("store")) {
				String storeNode = n.getArgs().get(0);
				String index = n.getArgs().get(1);
				String value = n.getArgs().get(2);

				TermPair conflict = processStore(storeNode, index, value, s);
				if (conflict != null) {
					return conflict; // Conflict found in store operation
				}
			}
		}

		// Step 7: satisfiable
		System.out.println("\rExecuting Congruent Closure Algorithm\t100%");
		return null;
	}

	// Method to process select operations, handling select after a store
	private static TermPair processSelect(String storeNode, String index, String selectNode) throws Exception {
		Node n = node(storeNode);

		// If storeNode is itself a store operation
		if (n.getFn().equals("store")) {
			String updatedIndex = n.getArgs().get(1);
			String updatedValue = n.getArgs().get(2);

			// If the index of the store matches the select index, apply the store
			if (index.equals(updatedIndex)) {
				TermPair conflict = merge(selectNode, updatedValue);
				if (conflict != null) {
					return conflict; // Conflict found in select operation
				}
			} else {
				// Recursively process the select operation on the previous store node
				TermPair conflict = processSelect(find(n.getArgs().get(0)), index, selectNode);
				if (conflict != null) {
					return conflict; // Conflict found in select operation
				}
			}
		}

		// No conflict found
		return null;
	}

	// Method to process store operations, ensuring the store affects the right
	// terms
	private static TermPair processStore(String storeNode, String index, String value, String storeNodeId)
			throws Exception {
		Node n = node(storeNode);

		// Check for conflicts in nested store operations
		if (n.getFn().equals("store")) {
			String nestedStoreIndex = n.getArgs().get(1);
			String nestedStoreValue = n.getArgs().get(2);

			if (nestedStoreIndex.equals(index)) {
				TermPair conflict = merge(value, nestedStoreValue);
				if (conflict != null) {
					return conflict; // Conflict found
				}
			} else {
				// Recursively process the store operation on the previous store node
				TermPair conflict = processStore(find(n.getArgs().get(0)), index, value, storeNodeId);
				if (conflict != null) {
					return conflict; // Conflict found
				}
			}
		}

		// No conflict found
		return null;
	}

	private static TermPair merge(String id1, String id2) throws Exception {
		return merge_h(id1, id2);
	}

	private static TermPair merge_h(String id1, String id2) throws Exception {
		if (!find(id1).equals(find(id2))) {
			if (node(find(id1)).getBanned().contains(id2)) {
				return new TermPair(id1, id2); // Conflict found
			}

			for (String ban : node(find(id1)).getBanned()) {
				if (find(ban).equals(find(id2))) {
					return new TermPair(id1, id2); // Conflict found
				}
			}

			for (String ban : node(find(id2)).getBanned()) {
				if (find(ban).equals(find(id1))) {
					return new TermPair(id1, id2); // Conflict found
				}
			}

			Object[] p1 = ccpar(id1).toArray();
			Object[] p2 = ccpar(id2).toArray();
			union(id1, id2);
			for (Object t1 : p1) {
				for (Object t2 : p2) {
					if (!find((String) t1).equals(find((String) t2)) && congruent((String) t1, (String) t2)) {
						TermPair conflictTerms = merge_h((String) t1, (String) t2);
						if (conflictTerms != null) {
							return conflictTerms;
						}
					}
				}
			}
		}
		return null;
	}

	private static boolean congruent(String id1, String id2) throws Exception {
		Node n1 = node(id1);
		Node n2 = node(id2);
		if (n1.getFn().equals(n2.getFn())) {
			List<String> arg1 = n1.getArgs();
			List<String> arg2 = n2.getArgs();
			if (arg1.size() == arg2.size()) {
				for (int i = 0; i < arg1.size(); i++) {
					if (!find(arg1.get(i)).equals(find(arg2.get(i)))) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	private static Node node(String id) throws Exception {
		Node n = dag.get(id);
		if (n == null) {
			throw new NullPointerException("Does not exist any node with ID: " + id);
		}
		return n;
	}

	private static Set<String> ccpar(String id) throws Exception {
		return node(find(id)).getParents();
	}

	private static String find(String id) throws Exception {
		return find_h(id);
	}

	private static String find_h(String id) throws Exception {
		Node n = node(id);
		if (!id.equals(n.getFind())) {
			n.setFind(find_h(n.getFind()));
		}
		return n.getFind();
	}

	private static void union(String id1, String id2) throws Exception {
		union_h(id1, id2);
	}

	private static void union_h(String id1, String id2) throws Exception {
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
		n2.setParents(new HashSet<String>());
		n1.getBanned().addAll(n2.getBanned());
		n2.setBanned(new HashSet<String>());
	}
}

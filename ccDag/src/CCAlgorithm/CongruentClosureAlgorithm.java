package CCAlgorithm;

import CCAlgorithm.bean.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.Array;

public class CongruentClosureAlgorithm {

	private static Map<String, Node> dag;
	private static Set<TermPair> equalPred;
	private static Set<TermPair> notEqualPred;
	private static Set<String> atomPred;
	private static Set<String> consTerm;
	private static Set<ArrayStructure> arrayOperations = new HashSet<>(); // Store array operations
	private static Map<String, String> selectValues = new HashMap<>(); // Store array values

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

	// public get dag method
	public static Map<String, Node> getDag() {
		return dag;
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

		// Step 4: Process inequality terms
		step = 100 / ((float) notEqualPred.size());
		count = 0;
		perc = 0;
		for (TermPair tp : notEqualPred) {
			count += step;
			for (; count >= perc && perc <= 100; perc++) {
				System.out.print("\rExecuting Congruent Closure Algorithm\t" + perc + "%");
			}
			if (find(tp.getFirst()).equals(find(tp.getSecond()))) {
				System.out.println();
				return tp;
			}
		}

		// Step 5: Process store relations first and save them all in the
		// arrayOperations
		for (String id : dag.keySet()) {
			Node n = node(id);
			if (n.getFn().equals("store")) {
				TermPair conflict = processStore(n);
				if (conflict != null) {
					return conflict; // Conflict found
				}
			}
		}
		// Process select relations and
		for (String id : dag.keySet()) {
			Node n = node(id);
			if (n.getFn().equals("select")) {
				TermPair conflict = processSelect(n);
				if (conflict != null) {
					return conflict; // Conflict found in select operation
				}
			}
		}

		TermPair conflictTerms = checkConflicts();
		if (conflictTerms != null) {
			return conflictTerms; // Conflict found
		}

		// Step 7: satisfiable
		System.out.println("\rExecuting Congruent Closure Algorithm\t100%");
		return null;
	}

	private static TermPair processSelect(Node selectNode) throws Exception {
		String array = selectNode.getArgs().get(0);
		String selectIndex = selectNode.getArgs().get(1);

		// Here comes all the store nodes with the 2 args that can be complex
		// select(a,i) where a is the array and i is the index

		for (ArrayStructure arrayOperation : arrayOperations) {
			if (arrayOperation.getNodeRepresentation().equals(array)) {
				// Check if the select index exists in the array operation's index
				if (arrayOperation.getIndex().equals(selectIndex)) { // nese do jet ne arrayOperation
					selectValues.put(selectNode.getId(), arrayOperation.getValue());
				} else {
					// If the index does not match, handle substitution based on equalPred
					for (TermPair tp : equalPred) {
						if (tp.getFirst().equals(selectIndex)) {
							selectIndex = tp.getSecond();
						} else if (tp.getSecond().equals(selectIndex)) {
							selectIndex = tp.getFirst();
						}
					}
				}
			}
			// Check if the select value is equal to the array operation value
			if (selectIndex.equals(arrayOperation.getIndex())) {
				selectValues.put(selectNode.getId(), arrayOperation.getValue());
			}
		}

		// System.out.println("Number of array operations: " + arrayOperations.size());

		if (isSimpleSelect(selectNode)) {
			for (TermPair tp : equalPred) {
				if (tp.getFirst().equals(selectIndex)) {
					selectIndex = tp.getSecond();
				} else if (tp.getSecond().equals(selectIndex)) {
					selectIndex = tp.getFirst();
				}
			}

			for (ArrayStructure arrayOperation : arrayOperations) {
				if (arrayOperation.getIndex().equals(selectIndex)) {
					selectValues.put(selectNode.getId(), arrayOperation.getValue());
				}
			}
			for (ArrayStructure arrayOperation : arrayOperations) {
				if (arrayOperation.getIndex().equals(selectIndex)) {
					selectValues.put(selectNode.getId(), arrayOperation.getValue());
				}
			}
		}

		return null; // No conflict found
	}

	private static TermPair checkConflicts() {
		// Check inequalities
		for (TermPair tp : notEqualPred) {
			String first = tp.getFirst();
			String second = tp.getSecond();

			String originalFirst = first;
			String originalSecond = second;

			if (selectValues.containsKey(first)) {
				first = selectValues.get(first);
			}
			if (selectValues.containsKey(second)) {
				second = selectValues.get(second);
			}

			if (first.equals(second)) {
				return new TermPair(originalFirst, originalSecond); // Conflict found
			}
		}

		// Check equalities
		for (TermPair tp : equalPred) {
			String first = tp.getFirst();
			String second = tp.getSecond();

			String originalFirst = first;
			String originalSecond = second;

			if (first.startsWith("select") || second.startsWith("select") || first.startsWith("store")
					|| second.startsWith("store")) {

				if (selectValues.containsKey(first)) {
					first = selectValues.get(first);
				}
				if (selectValues.containsKey(second)) {
					second = selectValues.get(second);
				}

				if (!first.equals(second)) {
					return new TermPair(originalFirst, originalSecond); // Conflict found
				}
			}
		}

		return null;
	}

	private static boolean isSimpleSelect(Node selectNode) {
		for (String arg : selectNode.getArgs()) {
			Node argNode = dag.get(arg);
			if (argNode != null && (argNode.getFn().equals("select") || argNode.getFn().equals("store"))) {
				return false;
			}
		}
		return true;
	}

	private static TermPair processStore(Node storeNode) throws Exception {
		// Recursively extract and save store operations
		extractAndSaveStores(storeNode);

		// Check for conflicts
		return null;
	}

	private static void extractAndSaveStores(Node node) throws Exception {
		// Traverse the `store` chain recursively and extract arguments
		String array = node.getArgs().get(0);
		String index = node.getArgs().get(1);
		String value = node.getArgs().get(2);

		Map<String, String> storeEqualStoreIndex = new HashMap<>();

		// Check if the store index is equal to any other store index in the equalPred
		for (ArrayStructure arrayOperation : arrayOperations) {
			for (TermPair tp : equalPred) {
				if ((tp.getFirst().equals(index) && tp.getSecond().equals(arrayOperation.getIndex())) ||
						(tp.getSecond().equals(index) && tp.getFirst().equals(arrayOperation.getIndex()))) {
					// System.out.println("Store index " + index + " is equal to " +
					// arrayOperation.getIndex());
					storeEqualStoreIndex.put(index, arrayOperation.getIndex());
				}
			}
		}

		// check if the map is not empty
		if (!storeEqualStoreIndex.isEmpty()) {
			// Temporary list to hold new ArrayStructure objects
			List<ArrayStructure> newArrayOperations = new ArrayList<>();

			// Iterate and give the values, e.g., i1 = i2 means that in array i1 I have to
			// insert the value of i2
			for (Map.Entry<String, String> entry : storeEqualStoreIndex.entrySet()) {
				String storeIndex = entry.getKey();
				String equalIndex = entry.getValue();

				// Find the value associated with the equalIndex in arrayOperations
				for (ArrayStructure arrayOperation : arrayOperations) {
					if (arrayOperation.getIndex().equals(equalIndex)) {
						// Create a new ArrayStructure with the index and the value of equalIndex
						ArrayStructure newArrayOperation = new ArrayStructure(node.getId(), array, storeIndex,
								arrayOperation.getValue());
						newArrayOperations.add(newArrayOperation);
					}
				}
			}

			// Add new ArrayStructure objects to arrayOperations
			arrayOperations.addAll(newArrayOperations);
		} else { // Save the store operation
			ArrayStructure arrayStructure = new ArrayStructure(node.getId(), array, index, value);
			arrayOperations.add(arrayStructure);
		}

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

	public static String find(String id) throws Exception {
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

package CCAlgorithm;

import CCAlgorithm.bean.*;
import java.util.*;
import java.io.*;

public class CongruentClosureAlgorithm{
	/**
	* Contains the Direct Acyclic Graph as a map of nodes
	*/
	private static Map<String,Node> dag;

	/**
	* Contains the ID of the termines that are pairwise equals 
	*/ 
	private static Set<TermPair> equalPred;

	/**
	* Contains the ID of the termines that are pairwise not equals 
	*/ 
	private static Set<TermPair> notEqualPred;

	/**
	* Contains the ID of the termines that are atom's arguments 
	*/ 
	private static Set<String> atomPred;

	/**
	* Contains the ID of the cons node in DAG
	*/
	private static Set<String> consTerm;
	
	/**
	 * Enum type to select if perform heyuristics or not heuristic 
	 * version of that algorithm
	 */
	private static Enum.Heuristics eur;
	
	/**
	 * Implement the Nelson-Oppen Congurent Closure Algorithm.
	 * 
	 *@param Dag directed acyclic graph with all the subterms
	 *@param EqualPred set of positive equality predicates
	 *@param EqualPred set of negative equality predicates
	 *@param AtomPred set of atoms' arguments
	 *@param ConsTerm set of cons' terms
	 *
	 *@return TermPair a couple of terms that are in conflict if the 
	 * formula is UNSATISFIABLE , null otherwise 
	 */ 
	public static TermPair NelsonOppen(
				Map<String,Node> Dag, Set<TermPair> EqualPred,
				Set<TermPair> NotEqualPred, Set<String> AtomPred, 
				Set<String> ConsTerm, Enum.Heuristics e) throws Exception{
		
		dag=Dag;
		equalPred=EqualPred;
		notEqualPred=NotEqualPred;
		atomPred=AtomPred;
		consTerm=ConsTerm;
		eur=e;
		switch (eur){
			case ENABLE:
				return nelsonOppen_h();
			case DISABLE:
				return nelsonOppen_();
			default:
				return nelsonOppen_();
		}
	}
		
	private static TermPair nelsonOppen_() throws Exception{
		// Step 2:
		Node nTemp;
		for(String s: consTerm){
			nTemp=new Node("car(" + s + ")", "car");
			nTemp.addArg(s);
			node(s).addParent(nTemp.getId());
			dag.put(nTemp.getId(), nTemp);
			//System.out.println("\t\t\t" + nTemp.getId() + " = " + node(s).getArgs().get(0));  
			merge(nTemp.getId(), node(s).getArgs().get(0));
			
			nTemp=new Node("cdr(" + s + ")", "cdr");
			nTemp.addArg(s);
			node(s).addParent(nTemp.getId());
			dag.put(nTemp.getId(), nTemp);
			//System.out.println("\t\t\t" + nTemp.getId() + " = " + node(s).getArgs().get(1));
			merge(nTemp.getId(), node(s).getArgs().get(1));
		}
		//System.out.println("\t\t\t" + dag.size());
		//System.out.println("\n" + dag + "\n");
		
		// Sep 3
		float step=100/((float) equalPred.size()),
				count=0;
		int perc=0;
		for(TermPair tp: equalPred){
			count+=step;
			for(;count>=perc && perc<=100; perc++)
				System.out.print("\rExecuting Congruent Closure Algorithm\t"
					+ perc + "%");
			/*
			for(;count>=perc && perc<1000; perc+=25){
				switch(perc%100){
					case 0:
						System.out.print((perc/10) + "%");
						break;
					case 25:
					case 50:
					case 75:
						System.out.print(".");
						break;
				}
			}*/
			merge(tp.getFirst(),tp.getSecond());
		}
		System.out.println("\rExecuting Congruent Closure Algorithm\t100%");
		// Step 4
		for(TermPair tp: notEqualPred){
			if(find(tp.getFirst()).equals(find(tp.getSecond())))
				return tp;
		}
		// Step 5
		for(String s1: atomPred){
			for(String s2: consTerm)
				if(find(s1).equals(find(s2)))
					return new TermPair("atom(" + s1 + ")", s2);
		}
		// Step 6: satisfiable
		return null;
	}
	
	private static TermPair nelsonOppen_h() throws Exception{
		System.out.print("\rExecuting Congruent Closure Algorithm\t0%");
		// atom axiom: a consTerm cannot be the argument of an atom predicate
		for(String id: atomPred)
			if(consTerm.contains(id)){
				System.out.println("\rExecuting Congruent Closure Algorithm\t0%");
				return new TermPair("atom(" + id + ")", id);
			} 
		// an atom term cannot be in the same congruent class of a term term
		for(String s: atomPred)
			node(s).getBanned().addAll(consTerm);
		// a cons term cannot be in the same congruent class of an atom term
		for(String s: consTerm)
			node(s).getBanned().addAll(atomPred);

		// Step 2: car/cdr projection axioms
		Node nTemp;
		TermPair ct=null;		// the two terms that are in conflict
		for(String s: consTerm){
			nTemp=new Node("car(" + s + ")", "car");
			nTemp.addArg(s);
			node(s).addParent(nTemp.getId());
			dag.put(nTemp.getId(), nTemp);
			//System.out.println("\t\t\t" + nTemp.getId() + " = " + node(s).getArgs().get(0));  
			if((ct=merge(nTemp.getId(), node(s).getArgs().get(0))) != null){
				System.out.println("\rExecuting Congruent Closure Algorithm\t0%");
				return ct;
			}
			nTemp=new Node("cdr(" + s + ")", "cdr");
			nTemp.addArg(s);
			node(s).addParent(nTemp.getId());
			dag.put(nTemp.getId(), nTemp);
			//System.out.println("\t\t\t" + nTemp.getId() + " = " + node(s).getArgs().get(1));
			if((ct=merge(nTemp.getId(), node(s).getArgs().get(1))) != null){
				System.out.println("\rExecuting Congruent Closure Algorithm\t0%");
				return ct;
			}
		}
		
		// Sep 3: the equality terms must be in the same congruent class
		float step=100/((float)equalPred.size()),
				count=0;
		int perc=0;
		for(TermPair tp: equalPred){
			count+=step;
			for(;count>=perc && perc<=100; perc++)
				System.out.print("\rExecuting Congruent Closure Algorithm\t"
					+ perc + "%"); // "\r" --> backspace			
			/*System.out.println(tp.getFirst() + " <-> " + tp.getSecond() + "padri1: " 
				+ node(tp.getFirst()).getParents().size() + " --padri2: "
				+ node(tp.getSecond()).getParents().size());*/
			if((ct=merge(tp.getFirst(),tp.getSecond()))!=null){
				System.out.println();
				return ct;
			}
		}
		System.out.println("\rExecuting Congruent Closure Algorithm\t100%");
		// Step 6: satisfiable
		return null;
	}
	
	/**
	 * Merges the congruence classes of the two node represent by
	 * the two id.
	 * If Enum.Heuristics is Enabled, it checks if a node is present in 
	 * the "banned list" of the others 
	 * 
	 * @param id1 first node' id
	 * @param id2 second node' id
	 * @return  a TermPair object if merges two terms that cannot
	 *			be in the same congurent class (unsatisfiable), null otherwise  
	 */
	private static TermPair merge(String id1, String id2) throws Exception{
		switch (eur){
			case ENABLE:
				return merge_h(id1, id2);
			case DISABLE:
				merge_(id1, id2);
				return null;
			default:
				merge_(id1, id2);
				return null;
		}
	}
	
	private static void merge_(String id1, String id2) throws Exception{
		String rId1=find(id1);
		String rId2=find(id2);
		if(!rId1.equals(rId2)){
			/*
			 * the reference of Set<String> ccpar cannot be copied
			 * because after union operation this object will be 
			 * modified.
			 * Solution: or clone the sets or create the array 
			 * containing all the objects in sets
			 */
			Object[] p1= ccpar(rId1).toArray();
			Object[] p2= ccpar(rId2).toArray();
			//System.out.println("\tUnion:\n\t\t" + id1 + "\tP: " + ccpar(id1) + "\n\t\t" 
			//									+ id2 + "\tP: " + ccpar(id2) + "");
			union(rId1, rId2);
			String t1, t2;
			for(int i=0;i<p1.length;i++){
				t1=(String) p1[i];
				for(int j=0;j<p2.length;j++){
					t2=(String) p2[j];
					if(!find(t1).equals(find(t2)) && congruent(t1,t2))
						//System.out.println("\t\tMerge Parent: " + t1 + ", " + t2);
						merge_(t1,t2);
				}
			}
		}
	}
	
	private static TermPair merge_h(String id1, String id2) throws Exception{
		if(!find(id1).equals(find(id2))){
			if(node(find(id1)).getBanned().contains(id2))
				return new TermPair(id1, id2);
			/* if 'node(find(id1)).getBanned()' does not contain 'id2' 
			 * then, before unions-by-rank, also 'id1.getBanned()' did not contain 'id2'. 
			 * So id1 and id2 are not in not-equal relation; hence we can union
			 * the two nodes
			 */
			// id1, id2 : nodes' id for which make union
			for(String ban: node(find(id1)).getBanned())
				if(find(ban).equals(find(id2)))	// CONFLICT
					return new TermPair(id1, id2);
			for(String ban: node(find(id2)).getBanned())
				if(find(ban).equals(find(id1))) // CONFLICT
					return new TermPair(id1, id2);
			/*
			for(String ban: node(find(id1)).getBanned())
				if(node(find(id2)).getRepr().contains(ban))
					return new TermPair(id1, id2);
			for(String ban: node(find(id2)).getBanned())
				if(node(find(id1)).getRepr().contains(ban))
					return new TermPair(id1, id2);
			*/		
			
			/*
			 * the reference of Set<String> ccpar cannot be copied
			 * because after union operation this object will be 
			 * modified.
			 * Solution: or clone the sets or create the array 
			 * containing all the objects in sets
			 */
			Object[] p1= ccpar(id1).toArray();
			Object[] p2= ccpar(id2).toArray();
			//System.out.println("\tUnion:\n\t\t" + id1 + "\tP: " + ccpar(id1) + "\n\t\t" 
			//									+ id2 + "\tP: " + ccpar(id2) + "");
			union(id1, id2);
			String t1, t2;
			TermPair conflictTerms;
			for(int i=0;i<p1.length;i++){
				t1=(String) p1[i];
				for(int j=0;j<p2.length;j++){
					t2=(String) p2[j];
					if(!find(t1).equals(find(t2)) && congruent(t1,t2))
						//System.out.println("\t\tMerge Parent: " + t1 + ", " + t2);
						/* exit from the two cycles if and only if the heuristic 
						 * is enable and two conflict terms are found
						*/ 
						if((conflictTerms=merge_h(t1,t2))!=null)
							return conflictTerms;
				}
			}
		}
		return null;
	}
	 
	/**
	 * Test if the nodes with that ids are congruent
	 * 
	 * @param id1 first node' id
	 * @param id2 second node' id
	 * @return true if congruent, false otherwise
	 */
	private static boolean congruent(String id1, String id2) throws Exception{
		// equals between the two arguments' lists
		Node n1=node(id1);
		Node n2=node(id2);
		//System.out.println("\t\t\tList 1: " + n1.getArgs() + "\n\t\t\tList 2: " + n2.getArgs());
		if(n1.getFn().equals(n2.getFn())){
			List<String> arg1=n1.getArgs();
			List<String> arg2=n2.getArgs();
			if(arg1.size()==arg2.size()){
				for(int i=0;i<arg1.size();i++)
					if(!find(arg1.get(i)).equals(find(arg2.get(i))))
						return false;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the node with that id
	 * 
	 * @param id the node's id
	 * @return the node with that id
	 */ 
	private static Node node(String id) throws Exception{
		Node n=dag.get(id);
		if(n==null)
			throw new NullPointerException("Does not exist any node with ID: " + id);
		return n;
	}
	
	private static Set<String> ccpar(String id) throws Exception{
		return node(find(id)).getParents();
	}
	/**
	 * Returns the representative of node's equivalence class
	 * 
	 * @param id the node's id
	 * @return the representative of node's equivalence class 
	 */ 
	private static String find(String id) throws Exception{
		switch (eur){
			case ENABLE:
				return find_h(id);
			case DISABLE:
				return find_(id);
			default:
				return find_(id);
		}
	}
	
	/**
	 * Implementation of find method without path compression heuristic
	 * 
	 * @see #find(String id) 
	 */ 
	private static String find_(String id) throws Exception{
		Node n=node(id);
		return (id.equals(n.getFind()))? id : find_(n.getFind());
	}
	
	/**
	 * Implementation of find method with path compression heuristic
	 * 
	 * @see #find(String id) 
	 */
	private static String find_h(String id) throws Exception{
		Node n=node(id);
		if(!id.equals(n.getFind()))
			n.setFind(find_h(n.getFind()));
		return n.getFind();
	}
	
	/**
	 * Union of the two classes in which are the two nodes
	 * 
	 * @param id1  the first node's id
	 * @param id2  the second node's id
	 */
	private static void union(String id1, String id2) throws Exception{
		switch (eur){
			case ENABLE:
				union_h(id1, id2);
			break;
			case DISABLE:
				union_(id1, id2);
			break;
			default:
				union_(id1, id2);
			break;
		}
	}
	
	/**
	 * Implementation of union method without union by rank heuristic
	 * 
	 * @see #union(String id2, String id2) 
	 */
	private static void union_(String id1, String id2) throws Exception{
		Node n1=node(find(id1));
		Node n2=node(find(id2));
		// find field of second node points to find field of first
		n2.setFind(n1.getFind());
		n1.getParents().addAll(n2.getParents());
		n2.setParents(new HashSet<String>()); 
		/*
		System.out.println("\n\t\t\t\tFind id1: " + n1.getFind() + "\tFind id2: " + n2.getFind());
		System.out.println("\t\t\t\t Ccpar " + id1 + ": " + n1.getParents());
		System.out.println("\t\t\t\t Ccpar " + id2 + ": " + n2.getParents() + "\n");
		*/ 
	}
	
	/**
	 * Implementation of union method with union by rank heuristic
	 * 
	 * @see #union(String id2, String id2)
	 */
	private static void union_h(String id1, String id2) throws Exception{
		// keep representatives of two nodes
		Node n1=node(find(id1));
		Node n2=node(find(id2));
		if (n1.getRank()>n2.getRank())
			linkHeuristic(n1, n2);	
		else{
			linkHeuristic(n2, n1);
			if (n1.getRank()==n2.getRank())
				n2.setRank(n2.getRank()+1);
		}
	}
	
	/**
	 * Links two nodes in a such way that the first
	 * becames the second's representative.  
	 * Subprocedure of both union_ and union_h.
	 * 
	 * @param n1 first node
	 * @param n2 second node
	 */
	private static void linkHeuristic(Node n1, Node n2) throws Exception{
		// find field of second node points to find field of first
		n2.setFind(n1.getFind());
		n1.getParents().addAll(n2.getParents());
		n2.setParents(new HashSet<String>());
		/* euristics that maintains all the banned nodes' id in representative
		 * node 
		 */
		n1.getBanned().addAll(n2.getBanned());
		n2.setBanned(new HashSet<String>());
		/* euristics that maintains all the represented nodes' id in representative
		 * node 
		 */
		//n1.getRepr().addAll(n2.getRepr());
		//n2.setRepr(new HashSet<String>());
	}
}

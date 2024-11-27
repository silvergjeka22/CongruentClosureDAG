package CCAlgorithm.bean;

import java.util.*;

public class CCobject{
	
	/**
	 * Contains the Direct Acyclic Graph as a map of nodes
	 */
	public Map<String,Node> dag;
	
	/**
	 * Contains the ID of the termines that are pairwise equals 
	 */
	public Set<TermPair> equalTerm;
	
	/**
	 * Contains the ID of the termines that are pairwise not equals 
	 */
	public Set<TermPair> notEqualTerm;
	
	/**
	 * Contains the ID of the termines that are atom's arguments 
	 */
	public Set<String> atomTerm;
	
	/**
	 * Contains the ID of the termines that are not-atom's arguments 
	 */
	public Set<String> notAtomTerm;
	
	/**
	 * Contains the ID of the cons node in DAG
	 */
	public Set<String> consTerm;  
	
	/**
	 * Contains the corrispondence between cons "artificial" node and
	 * -atom predicates
	 */
	public Map<String,String> consNotAtom;
	
	private int nEquals;
	private int nNotEquals;
	private int nAtomPos;
	private int nAtomNeg;
	private int nPredPos;
	private int nPredNeg;
	private int nEdges;
	
	/**
	 * Create a "container" object in order to maintain all references 
	 * at the data structures created by parser and some 
	 * information about the formula read.
	 */ 
	public CCobject(){
		nEdges=0;
		nEquals=0;
		nNotEquals=0;
		nAtomPos=0;
		nAtomNeg=0;
		nPredPos=0;
		nPredNeg=0;
		
		dag				= new HashMap<String, Node>();
		equalTerm		= new HashSet<TermPair>();
		notEqualTerm	= new HashSet<TermPair>();
		atomTerm		= new HashSet<String>();
		notAtomTerm		= new HashSet<String>();
		consTerm		= new HashSet<String>();
		consNotAtom		= new HashMap<String,String>();
	}
	
	
	public int numEdges(){
		return nEdges;
	}
	public void incrEdges(){
		nEdges++;
	}
	
	public int numEq(){
		return nEquals;
	}
	public void incrEq(){
		nEquals++;
	}
	
	public int numNotEq(){
		return nNotEquals;
	}
	public void incrNotEq(){
		nNotEquals++;
	}
	
	public int numPredPos(){
		return nPredPos;
	}
	public void incrPredPos(){
		nPredPos++;
	}
	
	public int numPredNeg(){
		return nPredNeg;
	}
	public void incrPredNeg(){
		nPredNeg++;
	}
	
	public int numAtomPos(){
		return nAtomPos;
	}
	public void incrAtomPos(){
		nAtomPos++;
	}
	
	public int numAtomNeg(){
		return nAtomNeg;
	}
	public void incrAtomNeg(){
		nAtomNeg++;
	}
	
	public Map<String,String> getNotAtoms(){
		return consNotAtom;
	}
	public void addCons(String cons, String notAtom){
		consNotAtom.put(cons,notAtom);
	}
	
	public int getTotal(){
			return nEquals + nNotEquals + nAtomPos + nAtomNeg
						+ nPredPos + nPredNeg;
	}
}	

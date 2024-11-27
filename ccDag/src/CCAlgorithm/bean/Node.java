package CCAlgorithm.bean;

import java.util.*;

public class Node{
	
	private String		id="",
						fn="",
						find="";
	private int			rank;
	private List<String>		args;
	private Set<String>			ccpar;
	private Set<String>			banned;	
	
	/**
	* Constructs a node with its id and fn fields
	*@param id the node's unique identificator number
	*@param fn the constant or function symbol
	*/
	public Node(String id, String fn){ 
		this.id = id;
		this.fn = fn; 
		this.find=id;
		rank=0; 
		args=new LinkedList<String>();
		ccpar=new HashSet<String>();
		banned=new HashSet<String>();
		
	}
	
	public String getId(){
	  	return id;
	}
	public String getFn(){
	  	return fn;
	}
	
	public String getFind(){
		return find;
	}
	public void setFind(String find){
		this.find=find;
	}
	
	public List<String> getArgs(){
		return args;
	}
	public void setArgs(List<String> arguments){
		args=arguments;
	}
	public void addArg(String argument){
		//if(!args.contains(argument))
			args.add(argument);
	}
	
	public Set<String> getParents(){
		return ccpar;
	}
	public void setParents(Set<String> parents){
		ccpar=parents;
	}
	public void addParent(String parent){
		ccpar.add(parent); 
	}
	
	public Set<String> getBanned(){
		return banned;
	}
	public void setBanned(Set<String> b){
		banned=b;
	}
	public void addBanned(String b){
		banned.add(b);
	}
	
	public int getRank(){
		return rank;
	}
	public void setRank(int r){
		rank=r;
	}
	public String toString(){
	 	return id + "\n\t arguments:\t" + args + "\n\t parents:\t" + ccpar + "\n\t find:\t\t" + find + "\n";
	}
	
}

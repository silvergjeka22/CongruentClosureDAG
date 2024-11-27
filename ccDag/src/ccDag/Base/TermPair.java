package CCAlgorithm.bean;

public class TermPair{
	private String first;
	private String second;
	public TermPair(String id1, String id2){
		first=id1;
		second=id2;
	}
	public String getFirst(){
		return first;
	}
	public void setFirst(String s){
		first=s;
	}
	public void setSecond(String s){
		second=s;
	} 
	public String getSecond(){
		return second;
	}
	public boolean equals(Object obj){
		if(this==obj) return true;
		if (obj instanceof TermPair){
			TermPair tp= (TermPair) obj;
			return	( first.equals(tp.getFirst()) && second.equals(tp.getSecond()) ) ||
					( first.equals(tp.getSecond()) && second.equals(tp.getFirst())  );
		}
		return false;
	}
	public int hashCode(){
		return (first==null ? 0 : first.hashCode()) ^
                   (second==null ? 0 : second.hashCode());
	}
	public String toString(){
		return first + ", " + second;
	}
}

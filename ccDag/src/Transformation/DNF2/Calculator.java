package Transformation.DNF2;

import java.util.*;

import Transformation.Selection.ParserDnf;

public class Calculator {
    private String transformedFormula;
    private ArrayList<Literal> literals;

    // Constructor
    public Calculator(String formula) {
        // Use ParserDnf to process and transform the formula
        ParserDnf parser = new ParserDnf(formula);
        String processedFormula = parser.processFormula();
        this.transformedFormula = ParserDnf.transformFormula(processedFormula);

        // Initialize literals from the transformed formula
        this.literals = extractLiterals(this.transformedFormula);
    }




    // Extract literals from the formula
    private ArrayList<Literal> extractLiterals(String formula) {
        ArrayList<String> literalNames = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(formula, " )(<>-~&|");
        while (tokenizer.hasMoreTokens()) {
            String temp = tokenizer.nextToken();
            if (!literalNames.contains(temp)) {
                literalNames.add(temp);
            }
        }

        ArrayList<Literal> literalList = new ArrayList<>();
        int[][] literalsTF = generateTruthTable(literalNames.size());
        for (int i = 0; i < literalNames.size(); i++) {
            int[] tempTF = new int[literalsTF.length];
            for (int j = 0; j < literalsTF.length; j++) {
                tempTF[j] = literalsTF[j][i];
            }
            literalList.add(new Literal(literalNames.get(i), tempTF));
        }
        return literalList;
    }

    // Generate truth table for given number of literals
    private int[][] generateTruthTable(int size) {
        int rows = (int) Math.pow(2, size);
        int[][] table = new int[rows][size];
        for (int i = 0; i < rows; i++) {
            String binary = Integer.toBinaryString(i);
            while (binary.length() < size) {
                binary = "0" + binary;
            }
            for (int j = 0; j < size; j++) {
                table[i][j] = binary.charAt(j) - '0';
            }
        }
        return table;
    }

	// used to get the DNF formula
	String dnf = "";

    // Perform calculation
    public void calculate() {
        System.out.println("Transformed Formula: " + this.transformedFormula);

        for (Literal literal : literals) {
            literal.printMe();
        }

        // Implement stack-based calculation logic here
        Stack<Literal> stack = new Stack<>();
        String formula = transformedFormula.replace("(", " ( ")
                                           .replace(")", " ) ")
                                           .replace("~", " ~ ")
                                           .replace("&", " & ")
                                           .replace("|", " | ")
                                           .replace("<->", " <-> ")
                                           .replace("->", " -> ");
        StringTokenizer tokenizer = new StringTokenizer(formula, " ");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("&") || token.equals("|") || token.equals("->") || token.equals("<->") || token.equals("~")) {
                stack.push(new Literal(token, null));
            } else if (token.equals(")")) {
                Literal b = stack.pop();
                Literal operator = stack.pop();
                if (operator.name.equals("~")) {
                    Literal result = Literal.opHandler(b, null, operator);
                    result.printMe();
                    stack.push(result);
                } else {
                    Literal a = stack.pop();
                    Literal result = Literal.opHandler(a, b, operator);
                    result.printMe();
                    stack.push(result);
                }
            } else {
                for (Literal literal : literals) {
                    if (token.equals(literal.name)) {
                        stack.push(literal);
                        break;
                    }
                }
            }
        }
        Literal finalAnswer = stack.pop();
        System.out.println("\nTruth Table Completed");
        System.out.println("\nCNF: " + CNF(literals, finalAnswer));
		dnf = DNF(literals, finalAnswer);
        System.out.println("DNF: " + DNF(literals, finalAnswer));
		System.out.println("\n<<DONE>>");
    }


    // Get the DNF formula
	public String getDnfFormula(){
		return dnf;
	}


	public static String CNF(ArrayList<Literal> literalData, Literal FinalAnswer) {
		String result = " ";
		for (int i = 0; i < FinalAnswer.myTF.length; i++) {
			boolean flag = false;
			if (FinalAnswer.myTF[i] == 0) {
				String temp = "(";
				for (Literal literal : literalData) {
					if (flag) {
						temp += " |";
					} else {
						flag = true;
					}
					if (literal.myTF[i] == 1)
						temp = temp + " ~" + literal.name;
					else
						temp = temp + " " + literal.name;
				}
				temp = temp + " )";
				result = result + " & " + temp;
			}
		}
		result = result.replace("  & (", " (");
		if (result.equals(" "))
			return "( " + literalData.get(0).name + " | ~" + literalData.get(0).name + " )" + "  *all true case";
		return result;
	}

	public static String DNF(ArrayList<Literal> literalData, Literal FinalAnswer) {
		String result = " ";
		for (int i = 0; i < FinalAnswer.myTF.length; i++) {
			boolean flag = false;
			if (FinalAnswer.myTF[i] == 1) {
				String temp = "(";
				for (Literal literal : literalData) {
					if (flag) {
						temp += " &";
					} else {
						flag = true;
					}
					if (literal.myTF[i] == 1)
						temp = temp + " " + literal.name;
					else
						temp = temp + " ~" + literal.name;
				}
				temp = temp + " )";
				result = result + " | " + temp;
			}
		}
		result = result.replace("  | (", " (");
		if (result.equals(" "))
			return "( " + literalData.get(0).name + " & ~" + literalData.get(0).name + " )" + "  *all false case";
		return result;
	}

}

class Literal {
	public Literal(String name, int[] myTF) {
		this.name = name;
		this.myTF = myTF;
	}

	String name;
	int[] myTF;

	void printMe() {
		System.out.println();
		System.out.print(name + " : ");
		for (int i : myTF) {
			if (i == 0)
				System.out.print("F ");
			else
				System.out.print("T ");
		}
		System.out.println();
	}

	public static Literal opHandler(Literal a, Literal b, Literal func) {
		if (func.name.equals("~"))
			return _not(a);
		if (func.name.equals("&"))
			return _and(a, b);
		if (func.name.equals("|"))
			return _or(a, b);
		if (func.name.equals("->"))
			return _eq(a, b);
		if (func.name.equals("<->"))
			return _deq(a, b);
		return null;
	}

	static Literal _and(Literal a, Literal b) {
		int[] temp = new int[a.myTF.length];
		for (int i = 0; i < a.myTF.length; i++) {
			temp[i] = a.myTF[i] * b.myTF[i];
		}
		return new Literal(a.name + "&" + b.name, temp);
	}

	static Literal _or(Literal a, Literal b) {
		int[] temp = new int[a.myTF.length];
		for (int i = 0; i < a.myTF.length; i++) {
			int t = a.myTF[i] + b.myTF[i];
			if (t == 2)
				t = 1;
			temp[i] = t;
		}
		return new Literal(a.name + "|" + b.name, temp);
	}

	static Literal _eq(Literal a, Literal b) {
		int[] temp = new int[a.myTF.length];
		for (int i = 0; i < a.myTF.length; i++) {
			int t1 = a.myTF[i];
			int t2 = b.myTF[i];
			int t = -1;
			if (t1 == 1 && t2 == 0)
				t = 0;
			else if (t1 == 1 && t2 == 1)
				t = 1;
			else if (t1 == 0 && t2 == 1)
				t = 1;
			else if (t1 == 0 && t2 == 0)
				t = 1;
			temp[i] = t;
		}
		return new Literal(a.name + "->" + b.name, temp);
	}

	static Literal _deq(Literal a, Literal b) {
		int[] temp = new int[a.myTF.length];
		for (int i = 0; i < a.myTF.length; i++) {
			int t1 = a.myTF[i];
			int t2 = b.myTF[i];
			int t = -1;
			if (t1 == 1 && t2 == 0)
				t = 0;
			else if (t1 == 1 && t2 == 1)
				t = 1;
			else if (t1 == 0 && t2 == 1)
				t = 0;
			else if (t1 == 0 && t2 == 0)
				t = 1;
			temp[i] = t;
		}
		return new Literal(a.name + "<->" + b.name, temp);
	}

	static Literal _not(Literal a) {
		int[] temp = new int[a.myTF.length];
		for (int i = 0; i < a.myTF.length; i++) {
			if (a.myTF[i] == 1)
				temp[i] = 0;
			else
				temp[i] = 1;
		}
		return new Literal("~" + a.name, temp);
	}
}

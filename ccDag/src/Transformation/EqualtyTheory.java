import java.util.*;
import java.util.regex.*;

public class EqualtyTheory {

    // Map to store mappings from subformulas to variable names
    private Map<String, String> mapping = new LinkedHashMap<>();
    private int currentVarIndex = 1; // Start with p1 for variable assignment

    public String transformFormula(String formula) {
        // Regex to match subformulas within parentheses (including nested functions or expressions)
        Pattern pattern = Pattern.compile("\\(([^()]+(?:\\([^()]*\\))?[^()]*?)\\)"); // Handles nested functions
        Matcher matcher = pattern.matcher(formula);

        // Transform matches into variables
        while (matcher.find()) {
            String subformula = matcher.group(1).trim();
            if (!mapping.containsKey(subformula)) {
                mapping.put(subformula, "e" + currentVarIndex);
                currentVarIndex++; // Increment the variable index for p1, p2, p3, ...
            }
            // Replace the subformula with its corresponding variable
            formula = formula.replace("(" + subformula + ")", mapping.get(subformula));
        }

        // Handle the case where the entire formula has redundant parentheses
        // Remove unnecessary parentheses around the whole formula
        formula = formula.replaceAll("^\\((.*)\\)$", "$1");

        // Finally, ensure the whole formula is enclosed in parentheses
        return "(" + formula + ")";
    }

    // Method to print the mappings (subformula -> variable assignment)
    public void printMappings() {
        System.out.println("Mappings:");
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            System.out.println(entry.getValue() + " = (" + entry.getKey() + ")");
        }
    }

    public static void main(String[] args) {
        EqualtyTheory equalityTheory = new EqualtyTheory();
        
        
        String[] formulas = {
                "(((a=b)&(f(x)=y))|x)",
                "(((x=y)&(z=w))|(a=b))",
                "(((p=q)&(r=s))|t)",
                "(((a=b)&(c=d))|x)",
                "((m=n)&(o=p))|(q=r)",
                "(((f(x)=y)&(y=x))|(h(y)=z)|x)",
                "(((a=b)|(f(x)=y))&((x=y)|(z=w)))",
                "(((car(x)=cdr(y))&(f(x)=h(y)))&(x=y)&x|y|(f(x,y)=d(y)))",
                "((cons(x,y,z) = cdr(z))|(f(x,y)=g(x))|(select(store(z),x)))" // to check becuse give another format of the 
            };
        

        for (String formula : formulas) {
            String transformedFormula = equalityTheory.transformFormula(formula);
            System.out.println("Original Formula: " + formula);
            System.out.println("Transformed Formula: " + transformedFormula);
            equalityTheory.printMappings();
            System.out.println();
            
            // Clear the mappings for the next formula
            equalityTheory.mapping.clear();
            equalityTheory.currentVarIndex = 1;
        }
    }
}

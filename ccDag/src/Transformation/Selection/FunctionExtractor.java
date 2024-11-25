package Transformation.Selection;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionExtractor {

    // Method to extract and print functions with parameters
    public static List<String> extractFunctions(String formula) {
        List<String> functions = new ArrayList<>();
        // Updated regex to match `Fn(...)`, `Qn(...)`, and specific functions
        String pattern = "\\b([A-Za-z]+n|car|cdr|cons|select|store|atom|atoms)\\([^()]*\\)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(formula);

        System.out.println("Searching for functions...");

        // Find and collect all functions
        while (m.find()) {
            String function = m.group();
            functions.add(function);
            System.out.println("Found function: " + function);
        }

        return functions;
    }

    public static void main(String[] args) {
        // Array of complex formulas with `Fn(...)`, `Qn(...)`, and specific functions
        String[] formulas = {
            "(((a=b)&(Fn(x)=y)|(car(y)=z)&(cdr(z)=car(y))&(~(Hn(x,y,z)=cons(x,y,z)))&(store(x)=((~(atom(z))))))->((a=b)&(Fn(x)=y)|(car(y)=z)&(cdr(z)=car(y))&(~(Hn(x,y,z)=cons(x,y,z)))&(store(x)=((~(atom(z)))))))",
            "((p=store(x))|(Fn(a,b)=Hn(c))&(select(q)=z)&(~(Gn(z)))->((x=Fn(y))|(car(z)=cons(a,b))))",
            "((atom(a))&(nil=Fn(x))|(select(store(a,b))=cdr(x)))",
            "(Hn(Fn(Gn(a,b)),c)=cons(x,car(b)))&(nil=Fn(Hn(z)))",
            "(~(store(a,b)=select(c,d)))|(nil=atom(Fn(Gn(x))))"
        };

        // Loop through each formula and process it
        for (String formula : formulas) {
            System.out.println("------------------------------------------------------");
            System.out.println("Input formula: " + formula);
            System.out.println("Extracting functions from formula:");
            extractFunctions(formula);
        }
    }
}

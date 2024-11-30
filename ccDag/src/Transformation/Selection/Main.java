package Transformation.Selection;

public class Main {

    public static void main(String[] args) {
        // Formula input for testing
        String[] formulas = {
            "((~(Fn(p,q))) = (~(Fn(r,s)))) & ((Fn(a) != cons(b,c)) | (~(Fn(d,e))) = Fn(f,g))"
        };

        

        // Process the formulas using the ParsingDnf class
        ParsingDnf.processFormulas(formulas);
    }
}

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import Transformation.Selection.ParserDag;


public class App {
   
    public static void main(String[] args) {
        // Array of formulas using functions like store, cons, car, cdr, etc.
        String[] formulas = {
                // "(Fn(p,q) = store(x,y) | ~((~(Fn(p,q))) = store(x,y)))",
                // "(store(x,y) = cons(a,b) & ((car(cons(d,e)) & cdr(a)) | (cdr(a) = cdr(a) &
                // cdr(a) = cdr(a))))",
                // "(store(x,y) = cons(a,b) & (car(cons(d,e)) & cdr(a)))",
                // "(store(x,y) = cons(a,b) & car(cons(d,e)) = cdr(cons(a,b)))",

                // "((~(Fn(x,y))) != Fn(z,w) & store(a,b) != car(cons(c,d)))",

                "(Fn(p,q) = store(x,y,z) | ~(Fn(a,b) != cons(c,d)))",
                // "(Fn(p,q) = store(x,y) | ~(Fn(p,q) = store(x,y)))",
                // "select(store(car(x),cdr(y)),x) = y",

                // "Fn(p, Hn(p, Dn(q,s)))",

                // "Fn(p,q) = store(x,y) = (~(Fn(a,b) != cons(c,d)))",
                // " (~(Fn(p,q))) = (~(car(x))) != (~(Fn(a,b) != cons(c,d))) ",

                // " ~( (~(Fn(a,b))) != (~(cons(c,d))) ) ",
                // "~( (~(Fn(a,b))) = (~(cons(c,d))) )",
                // "~( Fn(a,b) = (~(cons(c,d))) )",
                // "~( (~(Fn(a,b))) = cons(c,d) )",
                // "~( Fn(a,b) != (~(cons(c,d))) )",
                // "~( (~(Fn(a,b))) != cons(c,d) )",
                // "~( (~(~(Fn(a,b))) != (~(cons(cdr(c),d))) )", TODO: fix this before making
                // the calclator transformation if there are 2 negations in updated formuala it
                // have to take off it

                // "~( ( ~ (~(Fn(a,b))) = (~(cons(c,d))) ) | (Fn(a,b) != (~(cons(c,d))) !=
                // Fn(a,b) = (~(cons(Fn(x,y),cdr(Fn(d)))))) )"

        };



        // create a new object called ParserDag
        ParserDag convertToDnf = new ParserDag(formulas);
        convertToDnf.execute();

        //GraphVisualization.createAndDisplayGraph();


    }
}

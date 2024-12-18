### To Remember: 

* if we have vars: 
   1.   normal x that can be everything  car(...),cdr(..), Fn(..), eccc
   2.   normal (~x)  or if it is a formula (~(formula))

* if we have "="  how to write the formula
   1.  equality of 2 vars can be a = b or with not see the vars formula can be a = b  where a,b can be any car(...),cdr(..), Fn(..), eccc
   2.  when we include not in foruma have to be: ~(a = b) where a and be can be everything

* if we have operators | (or) , & (and), ->(implyes),
    1. we have to isert insde the () the operations that we want to do first example (a | (n&b)) have to be written like this 
    2. be cearful when we have the = :
     - we can make them by seeing the equality condition
    

* TO NOT DO: 
   1. write different form the rules the formulas
   2. to make complex formulas like (a=(a&b)) write it simple like (a = b & b = a)
   3. you can write complex equalities like a = b = c but check the equalities roule do not write bad the formulas
   4. <-> still working on iff only iff so is better to write it like ((a -> b) & (b -> a))
   5. still working on conplex functions like "(car(cons(d,e))"

* Formula examples:

  "(store(x,y) = cons(a,b) & ((car(cons(d,e)) & cdr(a))  | (cdr(a) = cdr(a) & cdr(a) = cdr(a))))",
                "(store(x,y) = cons(a,b) & (car(cons(d,e)) & cdr(a)))",
                "(store(x,y) = cons(a,b) & car(cons(d,e)) = cdr(cons(a,b))",
                "((~(Fn(x,y))) != Fn(z,w) & store(a,b) != car(cons(c,d)))",
                "(Fn(p,q) = store(x,y) | ~(Fn(a,b) != cons(c,d)))",
                "(Fn(p,q) = store(x,y) | ~(Fn(p,q) = store(x,y)))",


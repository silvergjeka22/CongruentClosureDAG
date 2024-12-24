
# Disjunctive Normal Form (DNF) Generator

## Overview
This project is designed to generate formulas in Disjunctive Normal Form (DNF), following strict syntax and rules to ensure consistency and correctness. The generator creates formulas and stores them in the `src/inputFiles/` directory. Below are the guidelines for writing and understanding these formulas.

---

## Rules and Syntax

### Variables
1. **Normal Variables**: A variable can represent anything, such as `car(...)`, `cdr(...)`, `Fn(...)`, etc.
2. **Negated Variables**: Negations are applied either to a single variable as `~x` or to a formula as `~(formula)`.

### Equality (`=`)
1. **Equality of Two Variables**: Use the format `a = b`, where `a` and `b` can be any expression, e.g., `car(...)`, `cdr(...)`, `Fn(...)`, etc.
2. **Negated Equality**: Use the format `~(a = b)` when negating equality.

### Operators
1. **Logical OR (`|`)**: Combines two or more expressions inside parentheses, e.g., `(a | (b & c))`.
2. **Logical AND (`&`)**: Combines two or more expressions inside parentheses, e.g., `(a & b)`.
3. **Logical IMPLICATION (`->`)**: Use parentheses to clarify precedence, e.g., `(a -> b)`.

   - **Important**: When combining equality with operators, follow the equality rules above.

### Restrictions
1. **Do Not Write Formulas Incorrectly**:
   - Avoid writing expressions like `(a = (a & b))`. Instead, write simpler and clear expressions, e.g., `(a = b & b = a)`.
2. **Complex Equalities**:
   - Avoid writing `a = b = c`. Instead, write each equality explicitly, e.g., `(a = b & b = c)`.
3. **IFF (`<->`)**:
   - Represent only as logical equivalence using implications, e.g., `((a -> b) & (b -> a))`.
4. **Complex Functions**:
   - Avoid overly complex nested functions, e.g., `car(cons(d,e))` is acceptable, but avoid excessive nesting.

---

## Formula Examples

Here are examples of correctly formatted formulas:

1. `(store(x,y) = cons(a,b) & ((car(cons(d,e)) & cdr(a))  | (cdr(a) = cdr(a) & cdr(a) = cdr(a))))`
2. `(store(x,y) = cons(a,b) & (car(cons(d,e)) & cdr(a)))`
3. `(store(x,y) = cons(a,b) & car(cons(d,e)) = cdr(cons(a,b)))`
4. `((~(Fn(x,y))) != Fn(z,w) & store(a,b) != car(cons(c,d)))`
5. `(Fn(p,q) = store(x,y) | ~(Fn(a,b) != cons(c,d)))`
6. `(Fn(p,q) = store(x,y) | ~(Fn(p,q) = store(x,y)))`

---

## Directory Structure
- **Source Files**: The main generator code is located in the `src/` directory.
- **Generated Formulas**: Formulas are saved as `.txt` files in the `src/inputFiles/` directory.

---

## How to Use
1. **Run the Generator**: Execute the generator program by specifying the number of formulas and maximum depth.
2. **Review the Output**: Check the `src/inputFiles/` folder for the generated `.txt` files containing formulas.
3. **Verify Compliance**: Ensure all generated formulas adhere to the syntax and rules outlined above.

---

## Notes
- Always respect the outlined rules to maintain formula correctness.
- For any modifications to the generator, ensure the new rules are consistent with the examples provided.


# CongruentClosureDAG

## Overview
This repository implements a Directed Acyclic Graph (DAG)-based congruence closure algorithm for solving the satisfiability of logical formulas in the quantifier-free fragment of equality theories. It supports multiple theories, including:

- **Equality Theory**: Manages formulas with equality and inequality relations.
- **Array Theory**: Handles array operations such as `select` and `store`.
- **List Theory**: Supports operations like `car`, `cdr`, and `cons`.

### Features
- **Optimizations**: Includes forbidden sets, efficient representative selection in `UNION`, and a non-recursive `FIND` function to enhance performance.
- **Scalability**: Built to handle complex logical formulas efficiently.
- **Transformation Support**: Includes the ability to convert formulas between Conjunctive Normal Form (CNF) and Disjunctive Normal Form (DNF).

---

## How to Run the Project

The project integrates formula parsing, congruence closure calculation, and visualization. To execute the project, follow the steps below from the `ccDag` directory:

### Run the Application
1. **Run the Application**:
   ```bash
   java -cp bin:libs/gs-core-2.0.jar:libs/gs-ui-swing-2.0.jar App
   ```

### Compile the Source Code
2. **Compile the Source Code**:
   ```bash
   javac -cp libs/gs-core-2.0.jar:libs/gs-ui-swing-2.0.jar -d bin \
    src/App.java \
    src/GraphVisualization.java \
    src/CCAlgorithm/*.java \
    src/CCAlgorithm/parser/*.java \
    src/CCAlgorithm/bean/*.java \
    src/Transformation/Selection/*.java \
    src/Transformation/DNF2/*.java
   ```

---

## Directory Structure
- **`src/`**: Contains all source code files.
  - **`CCAlgorithm/`**: Implements the core congruence closure algorithm.
  - **`CCAlgorithm/parser/`**: Includes parsers for reading logical formulas.
  - **`CCAlgorithm/bean/`**: Defines data structures used in the algorithm.
  - **`Transformation/Selection/`**: Contains selection transformation logic.
  - **`Transformation/DNF2/`**: Includes transformations to Disjunctive Normal Form and Conjunctive Normal Form.
  - **`GraphVisualization.java`**: Visualizes the congruence closure DAG.
  - **`App.java`**: Entry point of the application.

- **`bin/`**: Stores compiled `.class` files.
- **`libs/`**: Contains external dependencies, such as GraphStream libraries.

---

## Notes
- Ensure the `libs` folder contains the required dependencies: `gs-core-2.0.jar` and `gs-ui-swing-2.0.jar`.
- Before running, ensure all source files are properly compiled into the `bin` directory.
- The project leverages Java Swing for graphical visualization of the DAG.

---

## References
This implementation is inspired by Nelson-Oppen methods for combining decision procedures for multiple theories, with a focus on modularity and efficiency for practical use cases in SMT solving.


![General Schematic](./Doc/img/generalSchem.png)


## How DNF Works
- You can write a formula that respects the syntax specified in the `src/syntax.md` file. By following the rules outlined there, the formula will be correctly parsed and processed.
- The DNF (Disjunctive Normal Form) transformation works by converting logical formulas into a disjunction (OR) of conjunctions (ANDs) of literals. This form is particularly useful for logical evaluation and is a standardized representation of logical expressions.
- When applying DNF transformations, ensure that you follow these basic steps:
  1. Eliminate **implications** and **biconditionals** by using equivalences.
  2. Ensure that all terms are in the proper conjunctive or disjunctive structure.

## How DAG Works
- The DAG (Directed Acyclic Graph) is used to represent dependencies and relationships between different terms and operations in a formula.
- You need to be careful with the symbols used in the DAG, as they represent distinct operations, functions, and relations. Each node in the graph represents a formula or operation, and edges represent dependencies or logical relationships between these formulas.
- The DAG works by applying the syntax rules that are defined in the `src/dagSyntad.md` file. By following these rules, you can ensure that formulas are correctly represented and processed within the DAG structure.
- When constructing the DAG:
  1. Ensure that each formula or operation is represented as a node.
  2. Use directed edges to indicate logical relationships or dependencies between nodes.

By maintaining proper syntax and structure as defined in `dagSyntax.md`, the DAG ensures efficient evaluation and transformation of logical formulas, especially when dealing with congruence closure and other complex operations in the system.

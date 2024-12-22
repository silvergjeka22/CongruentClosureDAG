# CongruentClosureDAG
This repository implements a DAG-based congruence closure algorithm for solving the satisfiability of logical formulas in the quantifier-free fragment of equality theories. It supports arrays, lists, and uninterpreted functions, with optimizations including forbidden sets, efficient representative selection in UNION, a non-recursive FIND function.


to run conection with parsing and calcolator

in the project path

1. javac src/Transformation/Selection/ParserDnf.java src/Transformation/DNF2/Calculator.java src/Transformation/Selection/NestedFunctionExtractor.java src/App.java src/Transformation/Selection/ParserDag.java
2. java -cp src Transformation.DNF2.Calculator
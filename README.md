# CongruentClosureDAG
This repository implements a DAG-based congruence closure algorithm for solving the satisfiability of logical formulas in the quantifier-free fragment of equality theories. It supports arrays, lists, and uninterpreted functions, with optimizations including forbidden sets, efficient representative selection in UNION, a non-recursive FIND function.


to run conection with parsing and calcolator

in the project path


1. java -cp bin:libs/gs-core-2.0.jar:libs/gs-ui-swing-2.0.jar App 

2. javac -cp libs/gs-core-2.0.jar:libs/gs-ui-swing-2.0.jar -d bin \    
    src/App.java \
    src/GraphVisualization.java \
    src/CCAlgorithm/*.java \
    src/CCAlgorithm/parser/*.java \
    src/CCAlgorithm/bean/*.java \
    src/Transformation/Selection/*.java \
    src/Transformation/DNF2/*.java
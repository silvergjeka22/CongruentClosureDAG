package Transformation.DNF;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        String[] cnfDnf;
        String dnf = "", cnf = "";
        cnfDnf = Calculator.process().split("</flag>");
        dnf = cnfDnf[0];
        cnf = cnfDnf[1];
        String[][] table = new String[][] {
                { "Formula", Calculator.original_data },
                { "DNF Form", dnf },
                { "CNF Form", cnf },
        };

        // Use the TablePrinter class to print the table
        TablePrinter.printTable(table);
    }
}

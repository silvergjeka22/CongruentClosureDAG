import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
// dag
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.lang.management.*;
import java.util.concurrent.TimeUnit;

// dag parser
import CCAlgorithm.parser.*;
import CCAlgorithm.CongruentClosureAlgorithm;
import CCAlgorithm.bean.*;

//cnf to dnf
// import Transformation.Selection.ParserDag;


/*
 *  We need them after
        // create a new object called ParserDag
        ParserDag convertToDnf = new ParserDag(formulas);
        convertToDnf.execute();

        //GraphVisualization.createAndDisplayGraph();
 */

public class App {

    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
        if (args.length != 1) {
            System.out.println("Usage:\tjava App \"formula\"\n" +
                    "   or\tjava App filePath");
        } else {
            String input;
            if (args.length == 1)
                input = args[0];
            else {
                input = args[1];
            }

            try {
                BufferedReader in = new BufferedReader(new FileReader(input));
                System.out.println("Input interpreted as path of a file with the formula inside.");
                input = "";
                String s;
                while ((s = in.readLine()) != null)
                    input += s;
                if (input.length() <= 1500)
                    System.out.println("FORMULA:\n" + input);
                else
                    System.out.println("The formula is displayed only if it is less than 1500 characters");
            } catch (FileNotFoundException e) {
                System.out.println("Can not open file. Maybe path is wrong or file does not exist.");
                System.out.println("Try to interpret the input string as a formula.");
            } catch (IOException e) {
                throw new IOException("Failed to open the file");
            }

            System.out.println("");
            CCobject ccObj = new CCobject();
            long parserUser = 0, algoUser = 0, startUser = 0, start = 0,
                    parserTime = 0, algoTime = 0;
            try {
                System.out.println("Parsing...");

                start = System.currentTimeMillis();
                startUser = getUserTime();
                // PARSING
                Parser.parsing(input, ccObj);
                parserUser = TimeUnit.NANOSECONDS.toMillis(getUserTime() - startUser);
                parserTime = System.currentTimeMillis() - start;

            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }
            if (ccObj.dag != null && ccObj.dag.size() > 0) {
                int numAtomPos = ccObj.numAtomPos();
                int numAtomNeg = ccObj.numAtomNeg();
                int totalClauses = ccObj.getTotal();

                System.out.println("Created a DAG with " + ccObj.dag.size() + " nodes and " +
                        ccObj.numEdges() + " edges in " + String.format("%.3f", (parserTime / 1000d)) + "s");
                System.out.println("Found " + totalClauses + " clauses");
                System.out.println("\t" + (ccObj.numEq() + ccObj.numNotEq()) + " equality clauses: " +
                        ccObj.numEq() + " positives, " + ccObj.numNotEq() + " negatives");
                System.out.println("\t" + (numAtomPos + numAtomNeg) + " atoms: " +
                        numAtomPos + " positives, " + numAtomNeg + " negatives");
                System.out.println("\t" + (ccObj.numPredPos() + ccObj.numPredNeg()) + " predicates: " +
                        ccObj.numPredPos() + " positives, " + ccObj.numPredNeg() + " negatives");

                System.out.println("");
                // System.out.println("\n" + ccObj.dag + "\n");
                System.out.print("Executing Congruent Closure Algorithm");

                start = System.currentTimeMillis();
                startUser = getUserTime();
                // NELSON OPPEN CONGRUENT CLOSURE ALGORITHM
                TermPair term = CongruentClosureAlgorithm.NelsonOppen(
                        ccObj.dag, // DAG
                        ccObj.equalTerm, // equal's terms
                        ccObj.notEqualTerm, // not equals' terms
                        ccObj.atomTerm, // atom's terms
                        ccObj.consTerm);
                algoUser = TimeUnit.NANOSECONDS.toMillis(getUserTime() - startUser);
                algoTime = System.currentTimeMillis() - start;

                // System.out.println("\n\n" + ccObj.dag + "\n\n");
                // System.out.println("Total nodes: " + ccObj.dag.size());
                if (term == null)
                    System.out.println("\nSATISFIABLE");
                else {
                    System.out.println("\nUNSATISFIABLE\n");
                    String temp, temp1;
                    // predicates
                    if ((temp = term.getFirst()).contains("p_") ||
                            (temp = term.getSecond()).contains("p_")) {
                        term.setFirst(temp.substring(2, temp.length()));
                        term.setSecond("-" + temp.substring(2, temp.length()));
                    }
                    // atom vs cons
                    else if ((ccObj.atomTerm.contains(temp = term.getFirst())
                            || ccObj.atomTerm.contains(temp = term.getSecond())) &&
                            (ccObj.consTerm.contains(temp1 = term.getFirst())
                                    || ccObj.consTerm.contains(temp1 = term.getSecond()))) {
                        term.setFirst("atom(" + temp + ")");
                        term.setSecond(temp1);
                    }
                    // -atom
                    int i = -1, j = -1;
                    if ((i = (temp = term.getFirst()).indexOf("fv_")) > -1) {
                        j = temp.indexOf(")", i);
                        temp = temp.substring(i - 5, j + 1);
                        term.setFirst(ccObj.getNotAtoms().get(temp));
                    }
                    i = -1;
                    j = -1;
                    if ((i = (temp = term.getSecond()).indexOf("fv_")) > -1) {
                        j = temp.indexOf(")", i);
                        temp = temp.substring(i - 5, j + 1);
                        term.setSecond(ccObj.getNotAtoms().get(temp));
                    }
                    System.out.println("First conflict is between these terms and/or predicates:\n\t"
                            + term.getFirst() + "\n\t" + term.getSecond());
                }

                System.out.println();
                System.out.println("Time for parsing");
                System.out.println("\t\t\tClock time\t" +
                        (int) ((parserTime / 1000) / 60) + "m " +
                        String.format("%.4f", (parserTime / 1000d) % 60) + "s");
                System.out.println("\t\t\tCPU time\t" +
                        (int) ((parserUser / 1000) / 60) + "m " +
                        String.format("%.4f", (parserUser / 1000d) % 60) + "s");

                System.out.println("Time for CCAlgorithm");
                System.out.println("\t\t\tClock time\t" +
                        (int) ((algoTime / 1000) / 60) + "m " +
                        String.format("%.4f", (algoTime / 1000d) % 60) + "s");
                System.out.println("\t\t\tCPU time\t" +
                        (int) ((algoUser / 1000) / 60) + "m " +
                        String.format("%.4f", (algoUser / 1000d) % 60) + "s");

                System.out.println("Total time");
                System.out.println("\t\t\tClock time\t"
                        + (int) (((parserTime + algoTime) / 1000) / 60) + "m " +
                        String.format("%.4f", ((parserTime + algoTime) / 1000d) % 60) + "s\t"
                        + "(" + (parserTime + algoTime) + "ms)");
                System.out.println("\t\t\tCPU time\t" +
                        (int) (((parserUser + algoUser) / 1000) / 60) + "m " +
                        String.format("%.4f", ((parserUser + algoUser) / 1000d) % 60) + "s\t"
                        + "(" + (parserUser + algoUser) + "ms)");
            }
        }
    }

    /** Get CPU time in nanoseconds. */
    private static long getCpuTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported() ? bean.getCurrentThreadCpuTime() : 0L;
    }

    /** Get user time in nanoseconds. */
    private static long getUserTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported() ? bean.getCurrentThreadUserTime() : 0L;
    }

    /** Get system time in nanoseconds. */
    private static long getSystemTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported() ? bean.getCurrentThreadCpuTime() - bean.getCurrentThreadUserTime()
                : 0L;
    }

}

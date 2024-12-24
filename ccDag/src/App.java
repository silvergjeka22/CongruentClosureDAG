import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.*;
import java.lang.management.*;

// import dropQuantifiers from Transformation.Selection
import Transformation.Selection.dropQuantifiers;

// dag librarys
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.lang.management.*;
import java.util.concurrent.TimeUnit;

// dag parser algorithm
import CCAlgorithm.parser.*;
import CCAlgorithm.CongruentClosureAlgorithm;
import CCAlgorithm.bean.*;

//cnf to dnf transformation
import Transformation.Selection.ParserDag;

public class App {

    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String ask;

        while (true) {
            System.out.println("Do you want to go with the DNF parsing or the DAG? (Enter 'dnf' or 'dag'):");
            ask = reader.readLine().trim().toLowerCase();

            if (ask.equals("dnf")) {
                parseDnf();
                parseDag();
                break;
            } else if (ask.equals("dag")) {
                parseDag();
                break;
            } else {
                System.out.println("Invalid input. Please enter 'dnf' or 'dag'.");
            }
        }
    }

    private static void parseDnf() throws IOException {
        String readFile = "src/inputFiles/"; // Path to the directory containing your formula files
        File inputFile = new File(readFile);

        if (!inputFile.exists() || !inputFile.isDirectory()) {
            System.out.println("Error: Input directory does not exist or is not a directory.");
            return;
        }

        // List all files in the directory
        File[] inputFilesDnf = inputFile.listFiles((dir, name) -> name.endsWith(".txt"));

        if (inputFilesDnf == null || inputFilesDnf.length == 0) {
            System.out.println("No input files found in the directory: " + inputFile);
            return;
        }

        System.out.println("\nAvailable files:");
        for (int i = 0; i < inputFilesDnf.length; i++) {
            System.out.println((i + 1) + ". " + inputFilesDnf[i].getName());
        }

        System.out.println("\nEnter the number corresponding to the file you want to use as input:");
        BufferedReader readerDnf = new BufferedReader(new InputStreamReader(System.in));
        int selectedFileIndexDnf;

        try {
            selectedFileIndexDnf = Integer.parseInt(readerDnf.readLine()) - 1;
            if (selectedFileIndexDnf < 0 || selectedFileIndexDnf >= inputFilesDnf.length) {
                System.out.println("Invalid selection. Please restart and try again.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            return;
        }

        // Read the selected file and process its formula
        String formula = readFormulaFromFile(inputFilesDnf[selectedFileIndexDnf]);

        // call the dropQuantifiers 
       formula = dropQuantifiers.dropQuantifiersFromFormula(formula);

        // Convert the formula into an array, with the entire formula as a single element
        String[] formulaArray = new String[1]; // Create an array with one element
        formulaArray[0] = formula; // Assign the entire formula string to the first element

        // Display the formula as an array
        System.out.println("Formula: ");
        for (String formulaPart : formulaArray) {
            System.out.println(formulaPart);
        }

        // Execute DNF conversion with the formula array
        convertToDnf(formulaArray);
    }

    private static void parseDag() throws IOException, Exception {
        String inputDirectoryPath = "src/alredyDnfFiles/";
        File inputDirectory = new File(inputDirectoryPath);

        if (!inputDirectory.exists() || !inputDirectory.isDirectory()) {
            System.out.println("Error: Input directory does not exist or is not a directory.");
            return;
        }

        // List all files in the directory
        File[] inputFiles = inputDirectory.listFiles((dir, name) -> name.endsWith(".txt"));

        if (inputFiles == null || inputFiles.length == 0) {
            System.out.println("No input files found in the directory: " + inputDirectoryPath);
            return;
        }

        System.out.println("\nAvailable files:");
        for (int i = 0; i < inputFiles.length; i++) {
            System.out.println((i + 1) + ". " + inputFiles[i].getName());
        }

        System.out.println("\nEnter the number corresponding to the file you want to use as input:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int selectedFileIndex;

        try {
            selectedFileIndex = Integer.parseInt(reader.readLine()) - 1;
            if (selectedFileIndex < 0 || selectedFileIndex >= inputFiles.length) {
                System.out.println("Invalid selection. Please restart and try again.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            return;
        }

        String input = "";
        File selectedFile = inputFiles[selectedFileIndex];

        try (BufferedReader fileReader = new BufferedReader(new FileReader(selectedFile))) {
            System.out.println("Input interpreted as path of a file with the formula inside.");
            String line;
            while ((line = fileReader.readLine()) != null) {
                input += line;
            }
            if (input.length() <= 1500)
                System.out.println("FORMULA:\n" + input);
            else
                System.out.println("The formula is displayed only if it is less than 1500 characters.");
        } catch (IOException e) {
            System.out.println("Failed to read the file.");
            return;
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

            if (term == null)
                System.out.println("\nSATISFIABLE");
            else {
                System.out.println("\n\nUNSATISFIABLE\n");
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

            // Ask the user if they want to display the graph in a loop
            while (true) {
                System.out.println("\nDo you want to display the graph? (y/n)\n");
                String response = reader.readLine().trim().toLowerCase();
                if (response.equals("y")) {
                    System.out.println("Displaying the graph...\n");

                    GraphVisualization.createAndDisplayGraph();

                    break;
                } else if (response.equals("n")) {
                    System.out.println("Exiting the program...\n");
                    break;
                } else {
                    System.out.println("Invalid input. Please enter 'y' for yes or 'n' for no.");
                }
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

    private static String readFormulaFromFile(File file) throws IOException {
        StringBuilder input = new StringBuilder();
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                input.append(line).append("\n");
            }
        }
        return input.toString().trim();
    }

    private static void convertToDnf(String[] formula) {
        // Assuming ParserDag is a class that performs the DNF transformation
        // The following code assumes you have a ParserDag class that can execute the
        // DNF conversion

        System.out.println("Converting formula to DNF...");
        ParserDag convertToDnf = new ParserDag(formula); // Pass the formula array to the ParserDag constructor
        convertToDnf.execute(); // Execute the DNF conversion
    }
}

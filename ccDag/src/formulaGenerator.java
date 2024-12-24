import java.util.Random;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class formulaGenerator {

    private static final String[] VAR  = {"x", "y", "z", "a", "b", "c", "e", "i", "o", "u"};
    private static final String[] FUN  = { "cons", "car", "cdr", "Fn", "Gn", "Hn", "Rn", "Pn", "Qn", "Sn"};
    private static final String[] PRED = { "LEAVE", "GO", "OPEN", "FREE", "FORCE", "STAY", "BYE", "COME", "FLY", "DRIVE"};
    private static final int MAX_ARITY = 6;
    private static int[] funArity = new int[FUN.length];
    private static int[] predArity = new int[PRED.length];
    private static long seed = System.currentTimeMillis();
    private static Random random = new Random(seed);
    private static int equalities = 0,
                       disequalities = 0,
                       atoms = 0,
                       notAtoms = 0,
                       preds = 0,
                       notPreds = 0,
                       recMax = 3,
                       rdm = 0;

    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            int clauses = Integer.parseInt(args[0]);
            recMax = Integer.parseInt(args[1]);
            for (int i = 0; i < clauses; i++) {
                rdm = random(0, 5);
                switch (rdm) {
                    case 0: equalities++; break;
                    case 1: disequalities++; break;
                    case 2: atoms++; break;
                    case 3: notAtoms++; break;
                    case 4: preds++; break;
                    case 5: notPreds++; break;
                }
            }
            String formula = generateFormula();
            writeFormulaToFile(formula);
        }
        else if (args.length == 3) {
            equalities = Integer.parseInt(args[0]);
            disequalities = Integer.parseInt(args[1]);
            recMax = Integer.parseInt(args[2]);
            String formula = generateFormula();
            writeFormulaToFile(formula);
        }
        else if (args.length == 5) {
            equalities = Integer.parseInt(args[0]);
            disequalities = Integer.parseInt(args[1]);
            atoms = Integer.parseInt(args[2]);
            notAtoms = Integer.parseInt(args[3]);
            recMax = Integer.parseInt(args[4]);
            String formula = generateFormula();
            writeFormulaToFile(formula);
        }
        else if (args.length == 7) {
            equalities = Integer.parseInt(args[0]);
            disequalities = Integer.parseInt(args[1]);
            atoms = Integer.parseInt(args[2]);
            notAtoms = Integer.parseInt(args[3]);
            preds = Integer.parseInt(args[4]);
            notPreds = Integer.parseInt(args[5]);
            recMax = Integer.parseInt(args[6]);
            String formula = generateFormula();
            writeFormulaToFile(formula);
        }
        else {
            System.out.println("Usage:\tjava formulaGenerator CLAUSES MAX_RECURSION_DEPTH\n"
                             + "\tjava formulaGenerator EQUALITIES DISEQUALITIES MAX_RECURSION_DEPTH\n"
                             + "\tjava formulaGenerator EQUALITIES DISEQUALITIES ATOM NOT_ATOM MAX_RECURSION_DEPTH\n"
                             + "\tjava formulaGenerator EQUALITIES DISEQUALITIES ATOM NOT_ATOM PRED NOT_PRED MAX_RECURSION_DEPTH\n");
        }
    }

    private static String generateFormula() {
        funArity[0] = 2;                // cons
        funArity[1] = funArity[2] = 1;    // car, cdr
        for (int i = 3; i < FUN.length; i++)
            funArity[i] = random(1, MAX_ARITY);
        for (int i = 0; i < PRED.length; i++)
            predArity[i] = random(1, MAX_ARITY);

        int totalClauses = equalities + disequalities + atoms + notAtoms + preds + notPreds;
        String result = "";

        for (int i = 0; i < totalClauses; i++) {
            rdm = randomClause();
            switch (rdm) {
                case 0: result += makeEquality(true); 
                        equalities--; 
                        break;
                case 1: result += makeEquality(false); 
                        disequalities--;
                        break;
                case 2: result += makeAtom(true); 
                        atoms--;
                        break;
                case 3: result += makeAtom(false);
                        notAtoms--;
                        break;
                case 4: result += makePred(true); 
                        preds--;
                        break;
                case 5: result += makePred(false);
                        notPreds--;
                        break;
            }
            if (rdm != -1) result += ";\n";
        }
        return result;
    }

    private static String makeEquality(boolean type) {
        String r1, r2;
        r1 = makeTerm(1, random(1, recMax));
        while (r1.equals(r2 = makeTerm(1, random(1, recMax)))); // avoid two equals terms
        if (type)
            return r1 + "=" + r2;
        else
            return r1 + "!=" + r2;
    }

    private static String makeAtom(boolean type) {
        String result;
        if (type) {    
            result = "atom(";
            rdm = random(0, 4); // random choice if it is a function or a variable
            if (rdm == 0) // var
                result += VAR[random(0, VAR.length - 1)] + ")";
            else {    // avoid atom(cons(...)) term
                int rdmTmp = random(1, FUN.length - 1);            
                result += FUN[rdmTmp] + "(";
                int r = random(2, recMax);
                for (int i = 0; i < funArity[rdmTmp]; i++)
                    result += makeTerm(2, r) + ",";
                result = result.substring(0, result.length() - 1) + "))";
            }
        } else {        
            result = "-atom(";
            result += makeTerm(1, random(1, recMax)) + ")";
        }
        return result;
    }

    private static String makePred(boolean type) {
        String result;
        int rdmTmp = random(0, PRED.length - 1);
        if (type) result = PRED[rdmTmp] + "(";
        else        result = "-" + PRED[rdmTmp] + "(";
        for (int i = 0; i < predArity[rdmTmp]; i++)
            result += makeTerm(1, random(1, recMax)) + ",";
        result = result.substring(0, result.length() - 1) + ")";
        return result;
    }

    private static String makeTerm(int recLevel, int recTerm) {
        String result;
        rdm = random(0, 4); // random choice if it is a function or a variable
        if (rdm == 0 || recLevel == recTerm) // var
            result = VAR[random(0, VAR.length - 1)];
        else {    // fun
            int rdmTmp = random(0, FUN.length - 1);            
            result = FUN[rdmTmp] + "(";
            for (int i = 0; i < funArity[rdmTmp]; i++)
                result += makeTerm(recLevel + 1, recTerm) + ",";
            result = result.substring(0, result.length() - 1) + ")";
        }
        return result;
    }

    private static int randomClause() {
        int[] arr = new int[6];
        int i = 0;
        if (equalities > 0) { arr[i] = 0; i++; }
        if (disequalities > 0) { arr[i] = 1; i++; }
        if (atoms > 0) { arr[i] = 2; i++; }
        if (notAtoms > 0) { arr[i] = 3; i++; }
        if (preds > 0) { arr[i] = 4; i++; }
        if (notPreds > 0) { arr[i] = 5; i++; }
        if (i > 0)    
            return arr[random(0, i - 1)];
        else    
            return -1;
    }

    private static int random(int min, int max) {
        long t;
        if ((t = System.currentTimeMillis()) - seed >= 100) { // every 100ms the seed is changed
            seed = t;
            random.setSeed(seed);
        }
        int r = (int) ((max - min + 1) * random.nextDouble()) + min;
        return r;
    }

    private static void writeFormulaToFile(String formula) {
        try {
            // Specify the directory
            String path = "src/alredyDnfFiles/";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Create a file with a timestamp to avoid overwriting
            File file = new File(path + "formula_" + System.currentTimeMillis() + ".txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(formula);
            writer.close();

            System.out.println("Formula saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

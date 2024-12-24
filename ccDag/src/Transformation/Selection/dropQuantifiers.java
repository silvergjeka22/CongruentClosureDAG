package Transformation.Selection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dropQuantifiers {
    // Method to drop the quantifiers (∀x, ∃y) from a formula
    public static String dropQuantifiersFromFormula(String formula) {
        // Regular expression pattern to match quantifiers (∀x, ∃y)
        Pattern quantifierPattern = Pattern.compile("([∀∃][a-zA-Z]+)\\s*");
        Matcher matcher = quantifierPattern.matcher(formula);

        // Remove the quantifiers by replacing them with an empty string
        String result = matcher.replaceAll("");

        // Return the formula without the quantifiers, i.e., free variables
        return result;
    }
}

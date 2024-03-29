package atp;

import java.util.*;

public class LitSelection {

    public enum LitSelectors {NONE, FIRST,SMALLEST,LARGEST,LEASTVARS,EQLEASTVARS}

    public static HashMap<String,LitSelectors> LiteralSelectors;
    static {
        LiteralSelectors = new HashMap<>();
        LiteralSelectors.put("first",LitSelectors.FIRST);
        LiteralSelectors.put("smallest",LitSelectors.SMALLEST);
        LiteralSelectors.put("largest",LitSelectors.LARGEST);
        LiteralSelectors.put("leastvars",LitSelectors.LEASTVARS);
        LiteralSelectors.put("eqleastvars",LitSelectors.EQLEASTVARS);
    }

    /********************************************************
     */
    public static ArrayList<Literal> firstLit(ArrayList<Literal> litlist) {

        ArrayList<Literal> result = new ArrayList<>();
        result.add(litlist.get(0));
        return result;
    }

    /***************************************************************
     */
    class SortByWeight implements Comparator<Literal> {

        public int compare(Literal a, Literal b) {
            return a.weight(1,1) - b.weight(1,1);
        }
    }

    /***************************************************************
     */
    public ArrayList<Literal> smallestLit(ArrayList<Literal> litlist) {

        ArrayList<Literal> result = new ArrayList<>();
        Collections.sort((List) litlist, new SortByWeight());
        result.add(litlist.get(0));
        return result;
    }

    /***************************************************************
     * @return the literal with the largest literal weight (@see Literal.weight()).
     * Return the last element only, even if there might be a tie in the
     * weight metric for several literals.
     */
    public ArrayList<Literal> largestLit(ArrayList<Literal> litlist) {

        ArrayList<Literal> result = new ArrayList<>();
        Collections.sort((List) litlist, new SortByWeight());
        result.add(litlist.get(litlist.size()-1));
        return result;
    }

    /***************************************************************
     */
    class VarSizeEval implements Comparator<Literal> {

        public int compare(Literal a, Literal b) {

            int varListSizeA = a.collectVars().size();
            int varListSizeB = b.collectVars().size();
            if (varListSizeA != varListSizeB)
                return varListSizeA - varListSizeB;
            return b.weight(1,1) - a.weight(1,1);
        }
    }

    /***************************************************************
     * @return the literal with the fewest variables.  Return the first
     * element only, even if there might be a tie in the variable count.
     */
    public ArrayList<Literal> varSizeLit(ArrayList<Literal> litlist) {

        ArrayList<Literal> result = new ArrayList<>();
        Collections.sort((List) litlist, new VarSizeEval());
        result.add(litlist.get(0));
        return result;
    }

    /***************************************************************
     *  Return the first literal of the form X=Y, or the largest literal
     *  among those with the smallest variable set if no pure variable
     *  literal exists.
     */
    public ArrayList<Literal> eqResVarSizeLit(ArrayList<Literal> litlist) {

        ArrayList<Literal> result = new ArrayList<>();
        for (Literal l : litlist) {
            if (l.isPureVarLit()) {
                result.add(l);
                return result;
            }
        }
        Collections.sort((List) litlist, new VarSizeEval());
        result.add(litlist.get(0));
        return result;
    }
}

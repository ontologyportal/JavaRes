package atp;

import java.util.*;
import java.util.regex.*;

public class SINE {
    /** This code is copyright Krystof Hoder and Articulate Software
    2009. This software is released under the GNU Public License
    <http://www.gnu.org/copyleft/gpl.html>.  Users of this code also consent,
    by use of this code, to credit Articulate Software in any
    writings, briefings, publications, presentations, or other representations
    of any software which incorporates, builds on, or uses this code.  

    Please cite the following when describing SInE
    Hoder, K., (2008) The SUMO Inference Engine (SInE).  Master's thesis,
    Charles University, Prague.  See also http://www.cs.man.ac.uk/~hoderk/sine/
                                               
    Please cite the following article in any publication with references
    when addressing Sigma in general:

    Pease, A., (2003). The Sigma Ontology Development Environment, in Working
    Notes of the IJCAI-2003 Workshop on Ontology and Distributed Systems,
    August 9, Acapulco, Mexico. See also http://sigmakee.sourceforge.net 
        
     * @author Krystof Hoder - Adaptation by Adam Pease for new ATP system 2012
     */
    
    /* All the formulas in the knowledge base */
    private HashSet<Clause> formulas;
    
    /* These formulas will be always selected. The typically tiny 
     * number of formulas which contain no symbols are put here. */
    private HashSet<Clause> mandatoryFormulas;
    
    // Number of times each symbol appears in all formulas.
    private HashMap<String, Integer> degrees;
    
    /* A map between symbols and all the formulas in which the
     * symbol appears as the lowest-degree symbol. */
    private HashMap<String, ArrayList<Clause>> symbol2form;
    
    /* A parameter that sets how uncommon a symbol must be to place
     * an axiom in the filtered set.  Use a very high number for
     * an "untriggered" behavior, 1 for a "trigger".  1.5 appears
     * optimal for SUMO. */
    public float tolerance = 1.5f;
    
    /** *************************************************************
     */
    public SINE(ClauseSet cs) {

        formulas = new HashSet<Clause>();
        mandatoryFormulas = new HashSet<Clause>();
        degrees = new HashMap<String, Integer>();
        symbol2form = new HashMap<String, ArrayList<Clause>>();
    
        String error = null;
        //System.out.println("# INFO in SInE(): initializing"); 
        loadFormulas(cs);                            
    }
    
    /** *************************************************************
     * Loads formulas from given source.
     * 
     * @param formulaSource contains the formulas.
     */
    private void loadFormulas(ClauseSet formulaSource) {

        //System.out.println("# INFO in SINE.loadFormulas(): ");
        Iterator<Clause> it = formulaSource.clauses.iterator();
        
        // First we go through all formulas, and compute degree of each symbol,
        // which is the number of times it appears in all formulas.
        while (it.hasNext()) {
            Clause f = (Clause) it.next();            
            formulas.add(f);            
            for (String sym : getSymbols(f)) {
                Integer prev = degrees.get(sym);
                if (prev != null) 
                    degrees.put(sym, prev+1);
                else 
                    degrees.put(sym, 1);                
            }
        }        
        //System.out.println("# INFO in SINE.loadFormulas(): degrees: " + degrees);
        
        // Now we associate each formula with its lowest-degree symbols.
        for (Clause form : formulas) {
            ArrayList<String> symbols = getSymbols(form);
            if (symbols.size() == 0) {
                mandatoryFormulas.add(form);
                continue;
            }
            //ArrayList<String> minDegSyms = new ArrayList<String>();      
            int minDeg=5000000;
            for (String sym : symbols) {
                int deg = degrees.get(sym);
                if (deg < minDeg) 
                    minDeg = deg;
                    /*
                    minDegSyms.clear();
                    minDegSyms.add(sym);
                } 
                else if (deg == minDeg) 
                    minDegSyms.add(sym); */                    
            }

            //for (String sym : minDegSyms) {
            for (String sym : symbols) {
                float deg = (float) degrees.get(sym);
                if (deg <= minDeg * tolerance) {
                    ArrayList<Clause> reqForms = symbol2form.get(sym);
                    if (reqForms == null) {
                        reqForms = new ArrayList<Clause>();
                        reqForms.add(form);
                        symbol2form.put(sym, reqForms);
                    } 
                    else 
                        reqForms.add(form);    
                }
            }
        }
        //System.out.println("# INFO in SINE.loadFormulas(): symbol2form: " + symbol2form);
        //System.out.println("# INFO in SINE.loadFormulas(): mandatoryFormulas: " + mandatoryFormulas);
    }
      
    /** *************************************************************
     * Returns all symbols occurring in given formula.
     * 
     * @param form Formula to get symbols from.
     * @return Symbols occurring in given formula.
     */
    private ArrayList<String> getSymbols(Clause form) {

        return form.getConstantStrings();
    }
    
    /** *************************************************************
     * Returns formulas that are directly required by given symbols
     * (in the sense of requirements map, which keeps pointers only to
     * the lowest degree symbols in each formula).   
     * 
     * @param symbols Symbols whose required formulas will be found.
     * @return Formulas required by symbols.
     */
    private HashSet<Clause> get1RequiredFormulas(HashSet<String> symbols) {

        //System.out.println("INFO in SINE.get1RequiredFormulas(): symbols: " + symbols);
        HashSet<Clause> reqForms = new HashSet<Clause>();
        for (String sym : symbols) {
            //System.out.println("Checking: " + sym);
            ArrayList<Clause> symReqForms = symbol2form.get(sym);
            //System.out.println("Found: " + symReqForms);
            if (symReqForms == null)
                continue;
            for (Clause form : symReqForms) 
                reqForms.add(form);            
        }
        //System.out.println("INFO in SINE.get1RequiredFormulas(): formulas: " + reqForms);
        return reqForms;
    }

    /** *************************************************************
     * Get all the symbols in the formulas given in reqForms.
     */
    private ArrayList<String> getAllSymbols(HashSet<Clause> reqForms) {
        
        //System.out.println("# INFO in SInE.getAllSymbols(): ");
        ArrayList<String> result = new ArrayList<String>();
        Iterator<Clause> it = reqForms.iterator();
        while (it.hasNext()) {
            Clause c = it.next();
            result.addAll(c.getConstantStrings());
        }
        return result;
    }
    
    /** *************************************************************
     * Returns all symbols transitively required by given symbols
     * (in the sense of requirements map, which keeps pointers only to
     * the lowest degree symbols in each formula).
     * 
     * @param symbols Collection of symbols to be closed under requirements relation.
     * @return Closure of given collection of symbols under requirements relation.
     */
    private HashSet<String> getRequiredSymbols(Collection<String> symbols) {

        //System.out.println("# INFO in SInE.getRequiredSymbols(): ");
        HashSet<String> reqSyms = new HashSet<String>(symbols);            
        int prevSize;
        do {
            prevSize = reqSyms.size();                    
            HashSet<Clause> reqForms = get1RequiredFormulas(reqSyms);                    
            reqSyms.addAll(getAllSymbols(reqForms));
        } while (reqSyms.size() > prevSize);        // repeat until no more symbols are being added
        return reqSyms;
    }
    
    /** *************************************************************
     * Returns formulas that are transitively required by given symbols
     * (in the sense of requirements map, which keeps pointers only to
     * the lowest degree symbols in each formula).
     * 
     * @param symbols Symbols whose required formulas will be found.
     * @return Formulas transitively required by symbols.
     */
    private HashSet<Clause> getRequiredFormulas(Collection<String> symbols) {

        //System.out.println("# INFO in SInE.getRequiredFormulas(): ");
        HashSet<String> reqSyms = getRequiredSymbols(symbols);
        //System.out.println("INFO in SINE.getRequiredFormulas(): required symbols: " + reqSyms);
        return get1RequiredFormulas(reqSyms);
    }

    /** *************************************************************
     * Performs axiom selection for given query.
     * 
     * @param form, according to which axioms will be selected.
     * @return Selected formulas.
     */
    private HashSet<Clause> performSelection(Clause form) {

        //System.out.println("# INFO in SInE.performSelection(): ");
        HashSet<String> symbols = new HashSet<String>();
        symbols.addAll(form.getConstantStrings());
        symbols.addAll(getAllSymbols(mandatoryFormulas));            
        HashSet<Clause> res = getRequiredFormulas(symbols);            
        res.addAll(mandatoryFormulas);        
        return res;
    }
    
    /** *************************************************************
     * @return clauses determined to be relevant to the query
     */
    public ClauseSet filter(Clause query) {
        
        //System.out.println("# INFO in filter(): ");
        long t1 = System.currentTimeMillis();
        HashSet<Clause> selectedFormulas = performSelection(query);        
        long t_elapsed = (System.currentTimeMillis() - t1);

        System.out.println("# INFO in SInE.submitQuery(): "
                           + (t_elapsed / 1000.0)
                           + " seconds to perform axiom selection");
        System.out.println("# INFO in SInE.submitQuery(): "
                           + selectedFormulas.size() + " formula(s) selected out of " + 
                           formulas.size()); 
        ClauseSet cs = new ClauseSet();
        cs.addAll(selectedFormulas);                        
        return cs;
    }

    /** *************************************************************
     * Performs axiom selection for given query.
     * 
     * @param form, according to which axioms will be selected.
     * @return Selected formulas.
     */
    private HashSet<Clause> performSelection(HashSet<String> syms) {

        //System.out.println("# INFO in SInE.performSelection(): ");
        HashSet<String> symbols = new HashSet<String>();
        symbols.addAll(syms);
        symbols.addAll(getAllSymbols(mandatoryFormulas));            
        HashSet<Clause> res = getRequiredFormulas(symbols);            
        res.addAll(mandatoryFormulas);        
        return res;
    }
    
    /** *************************************************************
     * @return clauses determined to be relevant to the query
     */
    public ClauseSet filter(HashSet<String> syms) {
        
        //System.out.println("# INFO in filter(): ");
        long t1 = System.currentTimeMillis();
        HashSet<Clause> selectedFormulas = performSelection(syms);        
        long t_elapsed = (System.currentTimeMillis() - t1);

        System.out.println("# INFO in SInE.submitQuery(): "
                           + (t_elapsed / 1000.0)
                           + " seconds to perform axiom selection");
        System.out.println("# INFO in SInE.submitQuery(): "
                           + selectedFormulas.size() + " formula(s) selected out of " + 
                           formulas.size()); 
        ClauseSet cs = new ClauseSet();
        cs.addAll(selectedFormulas);                        
        return cs;
    }

    /** *************************************************************
     *  A simple test to load a KB file and pose a query, which are
     *  the first and second item, respectively, given on the
     *  command line.
     */
    public static void main (String[] args) throws Exception {

        System.out.println("# INFO in SInE.main(): ");
        String kbFileName = args[0];
        String queryStr = Formula.removeQuotes(args[1]);
        ClauseSet querySet = Formula.string2clauses(queryStr);
        System.out.println("# Selecting from " + kbFileName);

        SINE sine = new SINE(Formula.file2clauses(kbFileName,30));
        ClauseSet selectedFormulas = sine.filter(querySet.extractFirst());
        System.out.println(selectedFormulas);
    }
}

package atp;

/*
Copyright 2010-2011 Adam Pease, apease@articulatesoftware.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program ; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston,
MA  02111-1307 USA 
*/

import java.util.*;

public class TestGenerator {

    public static long time = 0;
    public static String SZSresult = "";
    public static int depthLimit = 1;
    public static int gensymCount = 0;

    /** ***************************************************************
     */  
    public static Clause instantiateGensyms(Clause c) {

        LinkedHashSet<Term> vars = c.collectVars();
        if (vars.size() < 1)
            return null;
        Substitutions subst = new Substitutions();
        for (Term var : vars) {
            String newConst = "Gensym" + Integer.toString(gensymCount++);
            subst.subst.put(var,Term.string2Term(newConst));
        }
        return c.substitute(subst);
    }

    /** ***************************************************************
     */  
    public static boolean containsBadTerms(Clause c) {
    
        System.out.println("INFO in TestGenerator.containsBadTerms(): checking: " + c);
        ArrayList<String> terms = c.getConstantStrings();
        System.out.println("INFO in TestGenerator.containsBadTerms(): with terms: " + terms);
        if (terms.contains("s__ListOrderFn") || terms.contains("s__ListLengthFn") || 
            terms.contains("s__ListFn_2") || terms.contains("s__ListFn_3") || 
            terms.contains("s__ListFn_4") || terms.contains("s__ListFn_5") ||
            terms.contains("s__ListLengthFn") || terms.contains("s__initialList"))
            return true;
        return false;
    }
    
    /** ***************************************************************
     */  
    public static Clause seed(ClauseSet cs) {
        
        Random randomGenerator = new Random(System.currentTimeMillis());
        int randomInt = 0;
        int safetyCounter = 0;
        Clause c = null;
        LinkedHashSet<Term> vars = null;
        do {
            randomInt = randomGenerator.nextInt(cs.clauses.size());
            c = cs.clauses.get(randomInt);
            //System.out.println("INFO in TestGenerator.seed(): checking potential seed: " + c);
            vars = c.collectVars();
        } while ((c.literals.size() != 1 || vars.size() < 1 || containsBadTerms(c)) && safetyCounter++ < 10000);
        if (safetyCounter >= 10000)
            return null;
        System.out.println("# Chosen Seed: " + c);
        c.literals.get(0).negated = ! c.literals.get(0).negated;
        /*
        Substitutions subst = new Substitutions();
        for (int i = 0; i < vars.size(); i++) {
            String newConst = "Gensym" + Integer.toString(gensymCount++);
            subst.subst.put(vars.get(i),Term.string2Term(newConst));
        }
        c = c.substitute(subst);
        */
        System.out.println("# Seed: " + c);
        return c;
    }
    
    /** ***************************************************************
     */  
    public static Clause saturateGen(ProofState state, int seconds) {

        System.out.println("INFO in TestGenerator.saturate()");
        long t1 = System.currentTimeMillis();
        int count = 0;
        while (state.unprocessed.length() > 0) {
            count++;       
            Clause res = state.processClause();
            if (res != null) {
                time = System.currentTimeMillis() - t1;
                return res;
            }
            if (count > 1000) {
                count = 0;
                Clause given_clause = state.unprocessed.selectBest();
                System.out.println("# Checking: " + given_clause.toStringDiag());
                if (given_clause.depth > depthLimit) {
                    given_clause = state.unprocessed.extractBest();
                    System.out.println("# Checking with good depth: " + given_clause);
                    given_clause = instantiateGensyms(given_clause);
                    if (given_clause != null) {
                        state.unprocessed.addClause(given_clause);
                        System.out.println("# asserting: " + given_clause);
                    }
                }
            }
            if (((System.currentTimeMillis() - t1) / 1000.0) > seconds) {
                SZSresult = "timeout";
                time = System.currentTimeMillis() - t1;
                return null;
            }
        }
        return null;
    }
    
    /** ***************************************************************
     */   
    public static ProofState processTestFile(String filename, HashMap<String,String> opts, ArrayList<SearchParams> evals) {

        int timeout = ProverFOF.getTimeout(opts);
        ClauseSet cs = Formula.file2clauses(filename,timeout);
        if (opts.containsKey("verbose"))
            System.out.println(cs);
        else
            System.out.println("# INFO in TestGenerator.processTestFile(): completed file read");
        if (cs != null) {
            EvalStructure eval = evals.get(0).heuristics;
            Clause c = seed(cs);
            if (c == null) {
                System.out.println("# INFO in TestGenerator.processTestFile(): failed to generate seed.");
                return null;
            }
            cs.addClause(c);
            
            ProofState state = new ProofState(cs,evals.get(0)); 
            ProverFOF.setStateOptions(state,opts);
            state.filename = filename;
            state.evalFunctionName = eval.name;  
            System.out.println("# INFO in TestGenerator.processTestFile(): start saturation");
            state.res = saturateGen(state,timeout);
            if (state.res != null)
                return state;
            else
                return null;                                            
        }                    
        return null;
    }
    
    /** ***************************************************************
     * Test method for this class.  
     */
    public static void main(String[] args) {
            
        if (!Term.emptyString(args[0])) {
            ClauseEvaluationFunction.setupEvaluationFunctions();
            ArrayList<SearchParams> evals = null;
            HashMap<String,String> opts = ProverFOF.processOptions(args);  // canonicalize options
            if (opts == null) {
                System.out.println("Error in ProverFOF.main(): bad command line options.");
                return;
            }                
            evals = new ArrayList<SearchParams>();
            SearchParams sp = new SearchParams();
            sp.heuristics = ClauseEvaluationFunction.PickGiven5;
            evals.add(sp);
            
            ProofState state = processTestFile(opts.get("filename"),opts,evals);
            if (state != null) 
                ProverFOF.printStateResults(opts, state,null);
        }
    }
}

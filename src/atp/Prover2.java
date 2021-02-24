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

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.text.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Prover2 {
    
    private static String doc = "Prover2.java 0.2\n" + 
        "\n" + 
        "Usage: atp.Prover2 [options] <problem_file>\n" + 
        "\n" + 
        "This is a straightforward implementation of a simple resolution-based\n" + 
        "prover for first-order clausal logic. Problem file should be in\n" + 
        "(restricted) TPTP-3 CNF or FOF syntax. Equality is parsed, but not\n" + 
        "interpreted so far.\n" + 
        "\n" + 
        "Options:\n" + 
        "\n" + 
        " -h\n" + 
        "--help\n" +
        "Print this help.\n" +
        "\n" +
        " -to\n" +
        "--timeout\n" +
        "Must be followed by an integer, which is a timeout in seconds. A '0' value indicates no timeout limit.\n" +
        "\n" +
        " -t\n" +
        "--delete-tautologies\n" +
        "Discard the given clause if it is a tautology.\n" +
        "\n" +
        " -f\n" +
        "--forward-subsumption\n" +
        "Discard the given clause if it is subsumed by a processed clause.\n" +
        " -v\n" +
        "Verbose mode.  Print extra debugging information.\n" +
        "\n" +
        " -b\n" +
        "--backward-subsumption\n" +
        "Discard processed clauses if they are subsumed by the given clause.\n" +
        " -i\n" +
        "File include path directive.\n" +
        " --result\n" +
        "process StarExec result files with category files in TPTP directory.\n" +
        " --experiment\n" +
        "Run an experiment to total times for all tests in a given directory (deprecated, use script instead).\n" +
        " --categories\n" +
        "Run an experiment using category files in TPTP directory.\n" +
        " --allOpts\n" +
        "Run all options.  Ignore -tfb command line options and try in all combination.\n" +
        " --allStrat\n" +
        "Run all clause selection strategies.\n" +
        " --eqax\n" +
        "Generate equality axioms.\n" +
        " --sine\n" +
        "Run SInE axiom selection.\n" +
        " --proof\n" +
        "Print proofs. Dotgraph and proof options are mutually exclusive. \n" +
        " --dotgraph\n" +
        "Generate the proof in dot format for graphviz\n" +
        " --stats\n" +
        "Print statistics.\n" +
        " --csvstats\n" +
        "Print statistics in comma delimited format.\n" +
        " -d\n" +
        "Generate proof output in dot-graph format. Dotgraph and proof options are mutually exclusive.\n" +
        " -c\n" +
        "not yet implemented - command line interactive mode.  Run query on file and keep loaded after result. Short timeout recommended.";

    public static String errors = "";
    public static int defaultTimeout = 30;
    public static String resultDir = "";  // StarExec results file root directory
    public static boolean debug = false;

    /** ***************************************************************
     * canonicalize options into a name/value list.
     * If the --allOpts flag is set remove all other subsumption/deletion
     * options, since all will be tried.
     * @return a HashMap of name/value pairs, or null if there's an error.
     */
    public static HashMap<String,String> processOptions(String[] args) {
        
        HashMap<String,String> result = new HashMap<String,String>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                if (arg.equals("--allOpts"))
                    result.put("allOpts", "true");
                else if (arg.equals("--proof"))
                    result.put("proof", "true");
                else if (arg.equals("--dotgraph"))
                    result.put("dotgraph", "true");
                else if (arg.equals("--stats"))
                    result.put("stats", "true");
                else if (arg.equals("--csvstats"))
                    result.put("csvstats", "true");
                else if (arg.equals("--experiment"))
                    result.put("experiment", "true");
                else if (arg.equals("--result")) {
                    result.put("result", "true");
                    resultDir = args[i+1];
                }
                else if (arg.equals("--categories"))
                    result.put("categories", "true");
                else if (arg.equals("--allStrat"))
                    result.put("allStrat", "true");
                else if (arg.equals("--sine"))
                    result.put("sine", "true");
                else if (arg.equals("--eqax"))
                    result.put("eqax", "true");
                else if (arg.equals("--delete-tautologies"))
                    result.put("delete-tautologies","true");
                else if (arg.equals("--forward-subsumption"))
                    result.put("forward-subsumption","true");
                else if (arg.equals("--backward_subsumption"))
                    result.put("backward_subsumption","true");
                else if (arg.equals("--timeout")) {
                    try {
                         int val = Integer.parseInt(args[i+1]);
                    }
                    catch (NumberFormatException n) {
                        return null;
                    }
                    result.put("timeout",args[i+1]);
                }
                else
                    System.out.println("Error in processOptions(): unrecognized option: " + arg);
            }
            else if (arg.startsWith("-")) {
                for (int j = 1; j < arg.length(); j++) {
                    if (arg.charAt(j) == 't')
                        result.put("delete-tautologies","true");
                    else if (arg.charAt(j) == 'f')
                        result.put("forward-subsumption","true");
                    else if (arg.charAt(j) == 'b')
                        result.put("backward_subsumption","true");
                    else if (arg.charAt(j) == 'd')
                        result.put("dotgraph","true");
                    else if (arg.charAt(j) == 'v') {
                        result.put("verbose", "true");
                        ProofState.verbose = true;
                    }
                    else if (arg.charAt(j) == 'c')
                        result.put("interactive","true");
                    else if (arg.equals("-to")) {
                        try {
                             int val = Integer.parseInt(args[i+1]);
                        }
                        catch (NumberFormatException n) {
                            return null;
                        }
                        result.put("timeout",args[i+1]);
                    }
                    else if (arg.equals("-i"))
                        Formula.includePath = args[i+1];
                    else
                        System.out.println("Error in processOptions(): unrecognized option: " + arg.charAt(j));
                }
            }
            else
                result.put("filename",arg);
        }
        if (result.containsKey("allOpts")) {
            if (result.containsKey("delete-tautologies"))                
                result.remove("delete-tautologies");
            if (result.containsKey("forward-subsumption"))
                result.remove("forward-subsumption");
            if (result.containsKey("backward_subsumption"))
                result.remove("backward_subsumption");
        }
        return result;
    }
    
    /** ***************************************************************
     * results are side effects on proofState
     */
    public static void setStateOptions(ProofState state, HashMap<String,String> opts) {
        
        if (opts.containsKey("delete-tautologies"))
            state.delete_tautologies = true;
        if (opts.containsKey("forward-subsumption"))
            state.forward_subsumption = true;
        if (opts.containsKey("backward_subsumption"))
            state.backward_subsumption = true;
    }

    /** ***************************************************************
     */
    public static ArrayList<SearchParams> setAllEvalOptions() {
        
        ArrayList<SearchParams> result = new ArrayList<SearchParams>();
        SearchParams sp = new SearchParams();
        sp.heuristics = ClauseEvaluationFunction.FIFOEval;
        result.add(sp);
        sp = new SearchParams();
        sp.heuristics = ClauseEvaluationFunction.SymbolCountEval;
        result.add(sp);
        sp = new SearchParams();
        sp.heuristics = ClauseEvaluationFunction.PickGiven5;
        result.add(sp);
        sp = new SearchParams();
        sp.heuristics = ClauseEvaluationFunction.PickGiven2;
        result.add(sp);
        return result;
    }
        
    /** ***************************************************************
     */
    public static ArrayList<ProofState> setAllStateOptions(ClauseSet clauses, SearchParams params) {
        
        ArrayList<ProofState> result = new ArrayList<ProofState>();
        for (int i = 0; i < 16; i++) {
            ProofState state = new ProofState(clauses,params);
            if ((i & 1) == 0)
                state.delete_tautologies = false;
            else
                state.delete_tautologies = true;
            if ((i & 2) == 0)
                state.forward_subsumption = false;
            else
                state.forward_subsumption = true;
            if ((i & 4) == 0)
                state.backward_subsumption = false;
            else
                state.backward_subsumption = true;
            if ((i & 8) == 0)
                state.indexed = false;
            else
                state.indexed = true;
            result.add(state);
        }
        return result;
    }

    /** ***************************************************************
     */
    public static int getTimeout(HashMap<String,String> opts) {
        
        if (opts.containsKey("timeout"))
            return Integer.parseInt(opts.get("timeout"));
        else
            return defaultTimeout;
    }
    
    /** ***************************************************************
     * Run the prover on all the problems in the directory given by
     * the "filename" option value
     */
    private static void runExperiment(HashMap<String,String> opts, ArrayList<SearchParams> evals) {

        System.out.println("Prover2.runExperiment(): ");
        System.out.println("Prover2.runExperiment(): opts: " + opts);
        ProofState.generateMatrixHeaderStatisticsString();
        File dir = new File(opts.get("filename")); 
        String[] children = dir.list();
        Arrays.sort(children);
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                String filename = opts.get("filename") + File.separator + children[i];
                if (filename.endsWith(".p")) {
                    //System.out.println("# testing file: " + filename);
                    ProofState ps = processTestFile(filename,opts,evals);
                    if (ps == null) {
                        System.out.println("Error in Prover2.runExperiment() on file: " + filename);
                        System.out.println("Error reading file or no input clauses in file");
                        continue;
                    }
                    ClauseSet conjectures = new ClauseSet();
                    conjectures.addClause(ps.conjecture);
                    printStateResults(opts,ps,conjectures);
                }
            }
        }
    }

    /** ***************************************************************
     * process a file that is a list of TPTP problem files
     */
    private static void processProblemList(String filename, HashMap<String,String> opts, ArrayList<SearchParams> evals) {

        String sep = File.separator;
        String TPTPdir = System.getenv("TPTP");
        String file = TPTPdir + sep + filename;
        System.out.println("processProblemList() read category file: " + file);
        try {
            LineNumberReader input = new LineNumberReader(new FileReader(file));
            String line = null;
            do {
                line = input.readLine();
                if (line != null) {
                    String prefix = line.substring(0,3);
                    String probFile = TPTPdir + sep + "Problems" + sep + prefix + sep + line;
                    //System.out.println("processProblemList() process problem file: " + probFile);
                    ProofState ps = processTestFile(probFile,opts,evals);
                    ClauseSet conjectures = new ClauseSet();
                    conjectures.addClause(ps.conjecture);
                    printStateResults(opts,ps,conjectures);
                }
            } while (line != null && line != "");
        }
        catch (Exception e) {
            System.out.println("Error in Prover2.processProblemList(): Error on reading file: " + file);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }



    /** ***************************************************************
     */
    private static void runCategoryExperiment(HashMap<String,String> opts, ArrayList<SearchParams> evals) {

        System.out.println("Prover2.runCategoryExperiment(): ");
        System.out.println("Prover2.runCategoryExperiment(): opts: " + opts);
        ProofState.generateMatrixHeaderStatisticsString();
        File dir = new File(System.getenv("TPTP"));
        String[] children = dir.list();  // get the problem list files first.
        Arrays.sort(children);
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                if (children[i].endsWith("_probs")) {
                    System.out.println("*** " + children[i] + " ***");
                    processProblemList(children[i], opts, evals);
                }
            }
        }
    }

    /** ***************************************************************
     */
    public static void saveProof(HashMap<String,String> opts, ProofState state, ClauseSet query) {

        String sep = File.separator;
        ZoneId zonedId = ZoneId.of( "America/Los_Angeles" );
        LocalDate today = LocalDate.now( zonedId );
        String date = today.toString();
        String dir = System.getenv("TPTP") + sep + "Output-" + date;
        System.out.println("saveProof(): in directory " + dir);
        if (state.res != null) {
            try {
                File dirFile = new File(dir);
                if (!dirFile.exists())
                    dirFile.mkdir();
                String inFile = state.filename.substring(state.filename.lastIndexOf("/") + 1);
                String filename = dir  + sep + inFile + "out";
                PrintWriter pw = new PrintWriter(filename);
                TreeMap<String, Clause> proof = state.generateProofTree(state.res);
                if (query != null)
                    pw.println(state.extractAnswer(proof, query.get(0)));
                pw.println("# SZS output start CNFRefutation");
                pw.println(state.generateProof(state.res, false));
                pw.println("# SZS output end CNFRefutation");
                pw.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** ***************************************************************
     */
    public static ProofState computeStateResults(HashMap<String,String> opts, ProofState state,
                                                 ClauseSet cs, String filename, SearchParams eval) {

        int timeout = getTimeout(opts);
        state.filename = filename;
        state.conjecture = cs.getConjecture();
        state.evalFunctionName = eval.heuristics.name;
        //System.out.println("# Prover2.processTestFile(): begin saturation");
        try {
            state.res = state.saturate(timeout);  // <--- real proving starts here
        }
        catch (Exception e) {
            if (Term.emptyString(state.SZSresult))
                state.SZSresult = "Error (ERR) " + e.getMessage();
            return state;
        }
        System.out.println("computeStateResults(): state: " + state.SZSresult);
        System.out.println("computeStateResults(): cs:" + cs.SZSresult);
        if (state.res != null) {
            state.SZSexpected = cs.SZSexpected;
            if (Term.emptyString(state.SZSresult)) {
                state.SZSresult = cs.SZSresult;
                if (cs.isFOF && cs.hasConjecture)
                    state.SZSresult = "Theorem";
                else
                    state.SZSresult = "Unsatisfiable";
            }
        }
        else {
            if (cs.SZSresult.startsWith("Timeout") || state.SZSresult.startsWith("Timeout"))
                state.SZSresult = "Timeout";
            else if (cs.isFOF && cs.hasConjecture)
                state.SZSresult = "CounterSatisfiable";
            else
                state.SZSresult = "Satisfiable";
        }
        return state;
    }

    /** ***************************************************************
     */
    public static void printStateResults(HashMap<String,String> opts, ProofState state, ClauseSet query) {

        //         System.out.println("# printStateResults(): " + opts);
        boolean dotgraph = false;
        if (opts.containsKey("dotgraph")) {
            System.out.println(state.generateDotGraphProof(state.res));
            return;
        }
        else if (opts.containsKey("proof")) {
            if (!opts.containsKey("allOpts") && !opts.containsKey("experiment")) {
                System.out.println("# printStateResults(): " + state.res);
                if (state.res != null) {
                    TreeMap<String, Clause> proof = state.generateProofTree(state.res);
                    if (query != null)
                        System.out.println(state.extractAnswer(proof, query.get(0)));
                    System.out.println("# SZS output start CNFRefutation");
                    System.out.println(state.generateProof(state.res, false));
                    System.out.println("# SZS output end CNFRefutation");
                }
                else {
                    if (state.SZSresult.equals("Satisfiable"))
                        for (Clause c : state.processed.clauses)
                            System.out.println(c);
                }
            }
            else
                saveProof(opts,state,query);
        }
        if (opts.containsKey("csvstats")) {
            System.out.println(state.generateMatrixStatisticsString());
        }
        else  // (opts.containsKey("stats"))
            System.out.println(state.generateStatisticsString());
    }
    
    /** ***************************************************************
     * Process a particular problem file with the given list of subsumption
     * options and clause evaluation strategies.
     */
    private static void runInteractive(HashMap<String,String> opts, ArrayList<SearchParams> evals) {
        
        if (evals == null || evals.size() < 1) {
            System.out.println("Error in Prover2.runInteractive(): no evaluation functions");
            return;
        }
        if (evals.size() > 1) 
            System.out.println("Warning in Prover2.runInteractive(): more than one evaluation function, using first only.");        
        String command = "";
        System.out.println("Enter a TPTP query or statement, or '$exit' to quit.");
        System.out.println("Enter $query for query mode or $assert to add assertions.");
        System.out.println("Starting in query mode.");
        String filename = opts.get("filename");
        boolean assertMode = false; // interpret all formulas as queries
        try {
            int timeout = getTimeout(opts);
            ClauseSet cs = Formula.file2clauses(filename,timeout);  
            if (opts.containsKey("verbose"))
                System.out.println(cs);
            if (opts.containsKey("eqax"))
                cs = cs.addEqAxioms();
            if (cs != null) {
                while (!command.startsWith("$exit")) {
                    System.out.print("TPTP> ");
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    try {
                       command = br.readLine();
                    } 
                    catch (IOException ioe) {
                       System.out.println("IO error trying to read query");
                       return;
                    }
                    if (!command.startsWith("$")) {
                        Lexer lex2 = new Lexer(command);
                        String id = lex2.look();
                        if (assertMode) {
                            ClauseSet csnew = Formula.command2clauses(id, lex2, timeout);
                            if (opts.containsKey("verbose"))
                                System.out.println(cs);
                            if (csnew != null)
                                cs.addAll(csnew);
                        }
                        else {               
                            ClauseSet csnew = cs.deepCopy();  // don't add query to the knowledge base
                            ClauseSet query = Formula.command2clauses(id, lex2, timeout);
                            if (opts.containsKey("sine")) {
                                SINE sine = new SINE(cs);                            
                                csnew = sine.filter(query.extractFirst());
                            }
                            if (opts.containsKey("verbose"))
                                System.out.println(query);
                            csnew.addAll(query);
                            ProofState state = new ProofState(csnew,evals.get(0));
                            setStateOptions(state,opts);
                            ProofState newstate = computeStateResults(opts,state,cs,filename,evals.get(0));
                        }
                    }
                    else if (command.equals("$assert"))
                        assertMode = true;
                    else if (command.equals("$query"))
                        assertMode = false;
                }
            }
            
        }
        catch (IOException pe) {
            System.out.println("Error in Prover2.runInteractive(): IOException: " + pe.getMessage());
            pe.printStackTrace();
        }
        catch (ParseException pe) {
            System.out.println("Error in Prover2.runInteractive(): Parse error reading command " + command + ": " + pe.getMessage());
            pe.printStackTrace();
        }
    }
    
    /** ***************************************************************
     * Process a particular problem file with the given list of subsumption
     * options and clause evaluation strategies.
     */
    public static ProofState processTestFile(String filename, HashMap<String,String> opts, ArrayList<SearchParams> evals) {

        System.out.println("# Prover2.processTestFile(): filename: " + filename);
        System.out.println("# Prover2.processTestFile(): opts: " + opts);
        System.out.println("# Prover2.processTestFile(): evals: " + evals);
        int timeout = getTimeout(opts);
        //System.out.println("# Prover2.processTestFile(): read file");
        ClauseSet cs = Formula.file2clauses(filename,timeout);
        System.out.println("hasConjecture: " + cs.hasConjecture + " isFOF: " + cs.isFOF);
        //System.out.println("# Prover2.processTestFile(): read file completed");
        //if (cs != null) {
        //    System.out.println("# Prover2.processTestFile(): SZSresult: " + cs.SZSresult);
        //    System.out.println("# Prover2.processTestFile(): SZSexpected: " + cs.SZSexpected);
        //}
        if (cs.SZSresult.toLowerCase().contains("error")) {
            //System.out.println("# Prover2.processTestFile(): read file error");
            ProofState state = new ProofState(cs,evals.get(0));
            state.filename = filename;
            state.conjecture = cs.getConjecture();
            state.SZSexpected = cs.SZSexpected;
            state.SZSresult = cs.SZSresult;
            return state;
        }
        if (opts.containsKey("eqax") && cs.containsEquality())
            cs = cs.addEqAxioms();
        if (opts.containsKey("sine")) {
        	System.out.println("# INFO in Prover2.processTestFile(): using sine");
            SINE sine = new SINE(cs);
            HashSet<String> syms = cs.getConjectureSymbols();
            if (syms != null && syms.size() > 0) {
            	System.out.println("# INFO in Prover2.processTestFile(): found conjecture symbols: " + syms);
                cs = sine.filter(syms);
            }
            else 
            	System.out.println("# INFO in Prover2.processTestFile(): conjecture not found - can't use SINE: ");            
        }
        if (opts.containsKey("verbose"))         	
            System.out.println("# Clauses:\n" + cs);        
        if (cs != null) {
            for (SearchParams eval : evals) {
                if (opts.containsKey("allOpts")) {
                    ArrayList<ProofState> states = setAllStateOptions(cs,eval);
                    for (ProofState state : states) {
                        ProofState newstate = computeStateResults(opts,state,cs,filename,eval);
                        if (newstate != null)
                            return newstate;
                    }
                }
                else {
                    ProofState state = new ProofState(cs,eval);
                    setStateOptions(state,opts);
                    ProofState newstate = computeStateResults(opts,state,cs,filename,eval);
                    if (newstate != null)
                        return newstate;
                }
            }
        }
        return null;
    }
    
    /** ***************************************************************
     */
    public static void main(String[] args) {
          
        Formula.defaultPath = System.getenv("TPTP");
        System.out.println("Using default include path : " + Formula.defaultPath);
        if (args == null || args[0].equals("-h") || args[0].equals("--help")) {
            System.out.println(doc);
            return;
        }
        if (!Term.emptyString(args[0])) {
            ClauseEvaluationFunction.setupEvaluationFunctions();
            ArrayList<SearchParams> evals = null;
            HashMap<String,String> opts = processOptions(args);  // canonicalize options
            if (opts == null) {
                System.out.println("Error in Prover2.main(): bad command line options.");
                return;
            }
                
            if (opts.containsKey("allStrat") || opts.containsKey("allOpts"))
                evals = setAllEvalOptions();            
            else {
                evals = new ArrayList<SearchParams>();
                SearchParams sp = new SearchParams();
                sp.heuristics = ClauseEvaluationFunction.PickGiven5;
                evals.add(sp);
            }
            boolean dotgraph = false;

            if (opts.containsKey("experiment")) 
                runExperiment(opts,evals);
            else if (opts.containsKey("categories"))
                runCategoryExperiment(opts,evals);
            else if (opts.containsKey("interactive"))
                runInteractive(opts,evals);
            else {
                System.out.println("# INFO in Prover2.main(): Processing file " + opts.get("filename"));
                ProofState state = processTestFile(opts.get("filename"),opts,evals);
                //System.out.println("# INFO in Prover2.main(): state: " + state);
                if (state != null && state.res != null) {
                    printStateResults(opts,state,null);
                    //System.out.println("# SZS status Theorem for problem " + opts.get("filename"));
                }
                else {
                    if (state == null || Term.emptyString(state.SZSresult))
                        System.out.println("# SZS status GaveUp for problem " + opts.get("filename"));
                    else
                        printStateResults(opts,state,null);
                }
            }                            
        }
        else
            System.out.println(doc);
    }
}


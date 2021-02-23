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

JavaRes is mainly a reimplementation of PyRes by Stephan Schulz

StarExec.java contains utilities to prepare for and process results
from tests run on the StarExec cluster and well as other general
purpose utilities

*/
package atp;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.*;

public class StarExec {

    public static int timeoutSecs = 300;

    /** ***************************************************************
     */
    public static ArrayList<String> readLineFile(String file) {

        ArrayList<String> result = new ArrayList<>();
        try {
            LineNumberReader input = new LineNumberReader(new FileReader(file));
            String line = null;
            do {
                line = input.readLine();
                if (line != null) {
                    result.add(line);
                }
            } while (line != null && line != "");
        }
        catch (Exception e) {
            System.out.println("Error in Prover2.readLineFile(): Error on reading file: " + file);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /** ***************************************************************
     */
    public static ProofState parseResultFile(String f) {

        //System.out.println("parseResultsFile(): file: " + f);
        Pattern pexpected = Pattern.compile("# SZS Expected\\s*: (.*)");
        Pattern pactual = Pattern.compile("# SZS status\\s*(.*)");
        Pattern ptime = Pattern.compile("# time\\s*: (\\d*)ms");

        ProofState ps = new ProofState();
        ArrayList<String> ar = readLineFile(f);
        for (String l : ar) {
            Matcher m = pexpected.matcher(l);
            if (m.find())
                ps.SZSexpected = m.group(1);
            m = pactual.matcher(l);
            if (m.find())
                ps.SZSresult = m.group(1);
            m = ptime.matcher(l);
            if (m.find())
                ps.time = Integer.parseInt(m.group(1));
        }
        //System.out.println("parseResultsFile(): expected, result, time: " + ps.SZSexpected + ", " + ps.SZSresult + ", " + ps.time);
        return ps;
    }

    /***************************************************************
     */
    public static void buildCatCollection(String filename) {

        try {
            String sep = File.separator;
            String TPTP = System.getenv("TPTP");
            ArrayList<String> lines = readLineFile(filename);
            String catFile = filename.substring(filename.lastIndexOf("/")+1);
            File dir = new File(catFile);
            if (!dir.exists())
                dir.mkdir();
            for (String f : lines) {
                String pref = f.substring(0, 3);
                Path fullpath = Paths.get(TPTP + sep + "Problems" + sep + pref + sep + f);
                Path target = Paths.get(catFile + sep + f);
                Files.copy(fullpath, target, REPLACE_EXISTING);
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /***************************************************************
     */
    public static ArrayList<String> execPyRes(String filename) {

        ArrayList<String> output = new ArrayList<>();
        try {
            ProcessBuilder _builder;
            Process _pyres;
            String cmd = "timeout 60 python3 /home/apease/workspace/PyRes/pyres-fof.py -tifbp -HPickGiven5 -nlargest --silent ";
            ArrayList<String> commands = new ArrayList<>(Arrays.asList(
                    "timeout", "60", "python3", "/home/apease/workspace/PyRes/pyres-fof.py",
                    "-tifbp", "-HPickGiven5", "-nlargest", "--silent", filename));

            System.out.println("execPyRes(): command: " + commands);
            _builder = new ProcessBuilder(commands);
            _builder.redirectErrorStream(true);
            _pyres = _builder.start();
            System.out.println("execPyRes(): process: " + _pyres);
            BufferedReader _reader = new BufferedReader(new InputStreamReader(_pyres.getInputStream()));
            String line = null;
            while ((line = _reader.readLine()) != null) {
                output.add(line);
            }
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
        return output;
    }

    /***************************************************************
     * @return SZS result and time as a colon-separated string
     */
    private static String processPyOut(ArrayList<String> pyout) {

         String result = "";
         for (String s : pyout) {
             if (s.startsWith("# SZS status "))
                 result = s.substring(s.lastIndexOf(" ") + 1) + " : ";
             if (s.startsWith("# Total time         : "))
                 result = result + s.substring(s.lastIndexOf(": ") + 1);
         }
         return result;
    }

    /***************************************************************
     * Compare results running tests on PyRes and JavaRes and report
     * any significant difference.
     * @param r is two pairs of results - one from PyRes and one from
     *          JavaRes
     */
    public static void compareResults(String file, String r) {

        if (Term.emptyString(r))
            return;
        //System.out.println("compareResults(): process: " + r);
        String[] provers = r.split("\\|");
        //System.out.println("compareResults(): provers: " + provers[0] + " | " + provers[1]);
        String[] pyres = provers[0].split(":");
        //System.out.println("compareResults(): pyres: " + pyres[0] + " : " + pyres[1]);
        String[] javares = provers[1].split(":");
        //System.out.println("compareResults(): javares: " + javares[0] + " : " + javares[1]);
        if (!pyres[0].trim().equals(javares[0].trim()))
            System.out.println(file + ": JavaRes: " + javares[0] + " PyRes: " + pyres[0]);
        else {
            double pytime = Double.parseDouble(pyres[1].substring(0,pyres[1].length()-2)); // remove " s"
            double jatime = Double.parseDouble(javares[1]) / 1000;
            //System.out.println(file + ": JavaRes: " + jatime + " PyRes: " + pytime);
            if (((pytime / jatime) > 2) ||
                    ((jatime / pytime) > 2))
                System.out.println("compareResults() file: " + file +
                        ": JavaRes: " + jatime + "ms, PyRes: " + pytime + "ms");
        }
    }

    /***************************************************************
     * Compare results running tests on PyRes and JavaRes and report
     * any difference.
     */
    public static void compareOne(String file, ArrayList<SearchParams> evals ) {

        String args = "--eqax --proof --delete-tautologies --forward-subsumption --backward_subsumption --delete-tautologies --timeout 60";
        HashMap<String,String> opts = Prover2.processOptions(args.split(" "));
        opts.put("filename",file);

        ArrayList<String> pyout = execPyRes(file);
        String pystats = processPyOut(pyout);
        //System.out.println("# INFO in Prover2.compareOne(): pystats " + pystats);

        //System.out.println("# INFO in Prover2.compareOne(): Processing file " + opts.get("filename"));
        ProofState state = Prover2.processTestFile(opts.get("filename"),opts,evals);
        Prover2.setStateOptions(state,opts);
        String javaStats = state.SZSresult + " : " + state.time;
        //System.out.println("# INFO in Prover2.compareOne(): javaStats " + javaStats);
        compareResults(file, pystats + " | " + javaStats);
    }

    /***************************************************************
     * Compare results running tests on PyRes and JavaRes and report
     * any difference.
     * @param dir is a directory of .p files to test on
     */
    public static void compare(String dir) {

        Formula.defaultPath = System.getenv("TPTP");
        System.out.println("Using default include path : " + Formula.defaultPath);
        ClauseEvaluationFunction.setupEvaluationFunctions();
        ArrayList<SearchParams> evals = new ArrayList<SearchParams>();
        SearchParams sp = new SearchParams();
        sp.heuristics = ClauseEvaluationFunction.PickGiven5;
        evals.add(sp);

        File tptpdir = new File(dir);
        String[] children = tptpdir.list();  // get the problem list files first.
        for (String f : children)
            compareOne(dir + File.separator + f,evals);
    }

    /***************************************************************
     * Read a PyRes spreadsheet of results that consists of the problem
     * name, whether it succeeded (T or F) and execution time if it
     * succeeded, followed by other fields that are irrelevant for now.
     */
    public static HashMap<String,ArrayList<String>> readPyResData(String filename) {

        HashMap<String,ArrayList<String>> result = new HashMap<>();
        ArrayList<String> ar = readLineFile(filename);
        for (String s : ar) {
            String[] fields = s.split("\\s+");
            ArrayList<String> row = new ArrayList();
            row.addAll(Arrays.asList(fields));
            ArrayList<String> newrow = new ArrayList();
            newrow.addAll(row.subList(1,row.size()));
            result.put(row.get(0),newrow);
        }
        return result;
    }

    /** ***************************************************************
     * Process StarExec problem file
     * test runs in the given directory
     * @return a HashMap of problem file name keys and ProofState values that
     * just make use of the SZSexpected, SZSresult and time fields
     * This assumes StarExec's output directory format
     * outputRoot->Problems->[1..N]->ProverName->ProblemName->resultName.txt
     * for example /home/apease/ontology/JavaRes/Job1639_output/Problems/ALG/JavaRes---1.0.3___JavaRes---1.0.3/ALG001-1.p/26273835.txt
     * where [1..N] is the set of three-letter problem types and there can
     * be many ProblemNames for each type.
     */
    private static HashMap<String,ProofState> processResultFiles(String topdir) {

        HashMap<String,ProofState> result = new HashMap<>();
        String sep = File.separator;
        String probsDirStr = topdir + File.separator + "Problems";
        File probsDir = new File(probsDirStr);
        String[] probDirs = probsDir.list();
        System.out.println("processResultFiles() problems " + Arrays.toString(probDirs));
        for (int i = 0; i < probDirs.length; i++) {
            if (probDirs[i].length() == 3) {  // problem types are three letters, like "AGT"
                String probDirStr = probsDir + sep + probDirs[i];
                File probDir = new File(probDirStr);
                String[] provers = probDir.list();
                if (provers.length > 1)
                    System.out.println("Error in processResultFiles() more than one prover in " + Arrays.toString(provers));

                String probNamesStr = probDirStr + sep + provers[0]; // like 'JavaRes---1.0.3'
                File probNamesDir = new File(probNamesStr);
                String[] probNames = probNamesDir.list(); // should be many

                for (int j = 0; j < probNames.length; j++) {
                    if (probNames[j].endsWith(".p")) {  // problem names are like 'AGT001+1.p'
                        String resultNames = probNamesStr + sep + probNames[j];
                        File resultNamesDir = new File(resultNames);
                        String[] resultFiles = resultNamesDir.list();
                        if (resultFiles.length > 1)
                            System.out.println("Error in processResultFiles() more than one problem name in " + resultNames);
                        ProofState ps = StarExec.parseResultFile(resultNamesDir + sep + resultFiles[0]);
                        result.put(probNames[j], ps);
                    }
                }
            }
        }
        return result;
    }

    /** ***************************************************************
     * Use category files to process downloaded StarExec problem file
     * test runs in the given directory
     * @return a HashMap of file category name keys and values that are
     * the set of problem files for that category.
     */
    private static HashMap<String,HashSet<String>> processCategories() {

        // category -> set of problem files
        HashMap<String,HashSet<String>> problemMap = new HashMap<>();

        System.out.println("Prover2.runCategoryExperiment(): ");
        ProofState.generateMatrixHeaderStatisticsString();
        File tptpdir = new File(System.getenv("TPTP"));
        String[] children = tptpdir.list();  // get the problem list files first.
        Arrays.sort(children);
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                if (children[i].endsWith("_probs")) {
                    ArrayList<String> probs = StarExec.readLineFile(tptpdir + File.separator + children[i]);
                    HashSet<String> hs = new HashSet<>();
                    for (String f : probs)
                        hs.add(f);
                    problemMap.put(children[i],hs);
                }
            }
        }
        return problemMap;
    }

    /** ***************************************************************
     * @param rfiles is a HashMap of problem file name keys and ProofState values that
     * just make use of the SZSexpected, SZSresult and time fields
     * @param cats is a HashMap of file category name keys and values that are
     * the set of problem files for that category
     *
     * Filter all results to include only files in the PyRes data.
     *
     * Show first ten successes and failures for each category
     *
     * @param successOnly determines whether all time results are added, and
     *                    failures assess the full timeout limit (false), or
     *                    whether only to count times for tests that are successful
     *                    for both provers (true)
     */
    private static void showCats(HashMap<String,HashSet<String>> cats, HashMap<String,ProofState> rfiles,
                                 HashMap<String,ArrayList<String>> pyresData, boolean successOnly) {

        System.out.println("showCats()");
        for (String cat : cats.keySet()) {
            HashSet<String> probs = cats.get(cat);
            int jcountCorrect = 0;
            int jcountFail = 0;
            long jtime = 0;
            int pcountCorrect = 0;
            int pcountFail = 0;
            long ptime = 0;
            int counter = 0;
            for (String prob : probs) {
                counter++;
                if (counter == 1000) {
                    System.out.print(".");
                    counter = 0;
                }
                boolean bothCorrect = false;
                if (!successOnly)
                    bothCorrect = true;  // just set it to true so we'll always accrue scores, if it's not successOnly
                ArrayList<String> pyresProbResult = pyresData.get(prob);
                //System.out.println("showCats(): prob: " + prob);
                //System.out.println("showCats(): pyres: " + pyresProbResult);
                if (pyresProbResult == null)
                    continue;  // ensure that only files in both data sets are counted
                ProofState ps = rfiles.get(prob);
                if (ps != null && !Term.emptyString(ps.SZSresult) && ps.SZSexpected.equals(ps.SZSresult) && pyresProbResult.get(0).trim().equals("T"))
                    bothCorrect = true;
                if (pyresProbResult.get(0).trim().equals("T")) {
                    pcountCorrect++;
                    try {
                        long timeInt = (long) Float.parseFloat(pyresProbResult.get(1)) * ((long) 1000);
                        if (bothCorrect)
                            ptime += timeInt;
                    }
                    catch (NumberFormatException nfe) {
                        pcountCorrect--;
                    }
                }
                else {
                    if (bothCorrect)
                        ptime += timeoutSecs * ((long) 1000);
                    pcountFail++;
                }

                if (ps != null && !Term.emptyString(ps.SZSresult) && ps.SZSexpected.equals(ps.SZSresult)) {
                    jcountCorrect++;
                    if (bothCorrect)
                        jtime+= ps.time;
                    if (jcountCorrect < 10)
                        System.out.println("showCats(): success: prob, expected, result, time: " + prob + ", " + ps.SZSexpected + ", " + ps.SZSresult + ", " + ps.time);
                }
                else {
                    if (bothCorrect)
                        jtime += timeoutSecs * ((long) 1000);
                    jcountFail++;
                    if (ps != null && jcountFail < 10)
                        System.out.println("showCats(): fail: prob, expected, result, time: " + prob + ", " + ps.SZSexpected + ", " + ps.SZSresult + ", " + ps.time);
                }
            }
            System.out.println("\nCategory : " + cat);
            System.out.println("JavaRes");
            System.out.println("  success  : " + jcountCorrect);
            System.out.println("  time     : " + jtime);
            System.out.println("  fail     : " + jcountFail);
            System.out.println("PyRes");
            System.out.println("  success  : " + pcountCorrect);
            System.out.println("  time     : " + ptime);
            System.out.println("  fail     : " + pcountFail);
        }
    }

    /***************************************************************
     */
    public static void showHelp() {

        System.out.println(" -c <file> : build a collection of TPTP problems ");
        System.out.println("      and put in a subdir of the same name as the file");
        System.out.println(" -p <file> : run PyRes");
        System.out.println(" -r <file> <pyfile> : process results files with respect to PyRes data");
        System.out.println(" -rc <file> <pyfile> : process results files with respect to PyRes data and only count timing where both JavaRes and PyResare correct");
        System.out.println(" -m <dir> : compare all files in a dir for PyRes and JavaRes results");
        System.out.println(" -o <file> : compare one file for PyRes and JavaRes results");
        System.out.println("  -h : show help");
    }

    /***************************************************************
     */
    public static void main(String[] args) {

        System.out.println(Arrays.toString(args));
        Formula.defaultPath = System.getenv("TPTP");
        if (args.length == 0)
            showHelp();
        else if (args.length == 1) {
            if (args[0].equals("-h"))
                showHelp();
            else
                showHelp();
        }
        else if (args.length == 2) {
            if (args[0].equals("-c"))
                buildCatCollection(args[1]);
            else if (args[0].equals("-p"))
                execPyRes(args[1]);
            else if (args[0].equals("-m"))
                compare(args[1]);
            else if (args[0].equals("-o")) {
                ClauseEvaluationFunction.setupEvaluationFunctions();
                ArrayList<SearchParams> evals = new ArrayList<SearchParams>();
                SearchParams sp = new SearchParams();
                sp.heuristics = ClauseEvaluationFunction.PickGiven5;
                evals.add(sp);
                compareOne(args[1],evals);
            }
            else
                showHelp();
        }
        else if (args.length == 3) {
            if (args[0].equals("-r")) {
                HashMap<String,ArrayList<String>> pydata = readPyResData(args[2]);
                HashMap<String,HashSet<String>> cats = processCategories();
                HashMap<String,ProofState> rfiles = processResultFiles(args[1]);
                showCats(cats,rfiles,pydata,false);
            }
            else if (args[0].equals("-rc")) {
                HashMap<String,ArrayList<String>> pydata = readPyResData(args[2]);
                HashMap<String,HashSet<String>> cats = processCategories();
                HashMap<String,ProofState> rfiles = processResultFiles(args[1]);
                showCats(cats,rfiles,pydata,true);
            }
            else
                showHelp();
        }
        else
            showHelp();

    }

}
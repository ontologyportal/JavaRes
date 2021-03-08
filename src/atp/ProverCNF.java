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

This is a straightforward implementation of a simple resolution-based
prover for first-order clausal logic. Problem file should be in
(restricted) TPTP-3 CNF syntax. Equality is parsed, but not
interpreted so far.

This code is largely a translation of https://github.com/eprover/PyRes
but in that code this class is called pyres-cnf.py
*/

package atp;

import java.io.*;
import java.util.*;

public class ProverCNF {

    public static String doc = " -h\n" +
            "--help\n" +
            "  Print this help.\n" +
            "\n" +
            " -t\n" +
            "--delete-tautologies\n" +
            "  Discard the given clause if it is a tautology.\n" +
            "\n" +
            " -f\n" +
            "--forward-subsumption\n" +
            "  Discard the given clause if it is subsumed by a processed clause.\n" +
            "\n" +
            " -b\n" +
            "--backward-subsumption\n" +
            "  Discard processed clauses if they are subsumed by the given clause.\n" +
            "\n" +
            " -H <heuristic>\n" +
            "--given-clause-heuristic=<heuristic>\n" +
            "  Use the specified heuristic for given-clause selection.";

    /** ***************************************************************
     *  Process the options given
     */
    public static SearchParams processOptions(ArrayList<String> opts) {

        SearchParams params = new SearchParams();
        for (int i = 0; i < opts.size(); i++) {
            String opt = opts.get(i);
            if (opt.equals("-h") || opt.equals("--help")) {
                System.out.println("JavaRes: ProverCNF: " + Version.version);
                System.out.println(doc);
                return null;
            }
            else if (opt.equals("-t" )|| opt.equals("--delete-tautologies"))
                params.delete_tautologies = true;
            else if  (opt.equals("-f") || opt.equals("--forward-subsumption"))
                params.forward_subsumption = true;
            else if  (opt.equals("-b") || opt.equals("--backward-subsumption"))
                params.backward_subsumption = true;
            else if  (opt.equals("-H") || opt.equals("--given-clause-heuristic"))
                try {
                    i++;
                    params.heuristics = EvalStructure.GivenClauseHeuristics.get(opts.get(i));
                } 
                catch (Exception e) {
                    System.out.println("processOptions(): Unknown clause evaluation function" + opts.get(i));
                    return null;
            }
            else if (opt.equals("-n") || opt.equals("--neg-lit-selection")) {
                try {
                    i++;
                    params.literal_selection = LitSelection.LiteralSelectors.get(opts.get(i));
                } 
                catch (Exception e) {
                    System.out.println("processOptions(): Unknown literal selection function" + opts.get(i));
                    return null;
                }
            }
            else if (opt.startsWith("-")){
                System.out.println("processOptions(): Unknown option: " + opt);
                return null;
            }
            else // it must be a filename
                params.filename = opt;
        }
        return params;
    }

    /** ***************************************************************
     * load options and problem file as specified on the command line
     */
    public static ClauseSet load(SearchParams sp) {

        if (!Term.emptyString(sp.filename)) {
            ClauseSet problem = new ClauseSet();
            FileReader fr = null;
            try {
                File file = new File(sp.filename);
                fr = new FileReader(file);
                if (fr != null) {
                    Lexer lex = new Lexer(file);
                    problem.parse(lex);
                    return problem;
                }
            }
            catch (Exception e) {
                System.out.println("Error in ProverCNF.main(): File error reading " + sp.filename + ": " + e.getMessage() + "\n");
                return null;
            }
        }
        return null;
    }

    /** ***************************************************************
     * run proving on a file
     */
    public static String run(ClauseSet problem, SearchParams sp) {

        StringBuilder sb = new StringBuilder();
        ProofState state = new ProofState(problem,sp);
        Clause res = state.saturate();

        if (res != null) {
            sb.append("# SZS status Unsatisfiable\n");
            ArrayList<Derivable> proof = res.orderedDerivation();
            Derivable.enableDerivationOutput();
            for (Derivable s : proof)
                sb.append(s + "\n");
            Derivable.disableDerivationOutput();
        }
        else
            sb.append("# SZS status Satisfiable\n");
        return sb.toString();
    }

    /** ***************************************************************
     */
    public static void main(String[] args) {

        SearchParams sp = processOptions((ArrayList<String>) Arrays.asList(args));
        ClauseSet cs = load(sp);
        System.out.println(run(cs,sp));
    }
}

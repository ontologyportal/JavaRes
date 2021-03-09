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
but in that code this class is called pyres-simple.py
*/

package atp;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

public class ProverSimple {

    public static int timeout = 600;
    public static String doc = " -h\n" +
            "--help\n" +
            "  Print this help.\n";

    /** ***************************************************************
     *  Process the options given
     */
    public static String processOptions(ArrayList<String> opts) {

        for (int i = 0; i < opts.size(); i++) {
            String opt = opts.get(i);
            if (opt.equals("-h") || opt.equals("--help")) {
                System.out.println("JavaRes: ProverCNF: " + Version.version);
                System.out.println(doc);
                return null;
            }
            else // it must be a filename
                return opt;
        }
        return null;
    }

    /** ***************************************************************
     * load options and problem file as specified on the command line
     */
    public static ClauseSet load(String filename) {

        if (!Term.emptyString(filename)) {
            ClauseSet problem = new ClauseSet();
            FileReader fr = null;
            try {
                File file = new File(filename);
                fr = new FileReader(file);
                if (fr != null) {
                    Lexer lex = new Lexer(file);
                    problem.parse(lex);
                    problem.SZSexpected = lex.SZS;
                    return problem;
                }
            }
            catch (Exception e) {
                System.out.println("Error in ProverCNF.main(): File error reading " + filename + ": " + e.getMessage() + "\n");
                return null;
            }
        }
        return null;
    }

    /** ***************************************************************
     * run proving on a file
     */
    public static String run(ClauseSet problem) {

        StringBuilder sb = new StringBuilder();
        SimpleProofState state = new SimpleProofState(problem);
        Clause res = state.saturate(timeout);

        if (res != null)
            sb.append("# SZS status Unsatisfiable\n");
        else
            sb.append("# SZS status Satisfiable\n");
        return sb.toString();
    }

    /** ***************************************************************
     */
    public static void main(String[] args) {

        String filename = processOptions((ArrayList<String>) Arrays.asList(args));
        ClauseSet cs = load(filename);
        System.out.println(run(cs));
    }
}

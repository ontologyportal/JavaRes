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

package atp;

import java.io.*;

public class Prover1 {
    
    /** ***************************************************************
     * Test method for this class.  
     */
    public static void main(String[] args) {
          
        if (args == null || args.length == 0 || args[0].equals("-h") || args[0].equals("--help"))
            System.out.println("usage: atp.Prover1 file.tptp");
        else {
            if (!Term.emptyString(args[0])) {
                ClauseSet problem = new ClauseSet();
                FileReader fr = null;
                try {
                    File fin  = new File(args[0]);
                    fr = new FileReader(fin);
                    if (fr != null) {
                        Lexer lex = new Lexer(fin);               
                        ClauseSet cs = new ClauseSet();
                        cs.parse(lex);
                        SimpleProofState state = new SimpleProofState(cs);
                        Clause res = state.saturate();

                        if (res != null)
                            System.out.println("# SZS status Unsatisfiable");
                        else
                            System.out.println("# SZS status Satisfiable");
                    }
                }
                catch (Exception e) {
                    System.out.println("Error in Prover1.main(): File error reading " + args[1] + ": " + e.getMessage());
                    return;
                }
                finally {
                    try {
                        if (fr != null) fr.close();
                    }
                    catch (Exception e) {
                        System.out.println("Exception in Prover1.main()" + e.getMessage());
                    }
                }                
            }
        }
    }
}

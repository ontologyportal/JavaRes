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

import java.util.*;

public class KIF {
    
    public static final String Or             = "|";
    public static final String And            = "&";
    public static final String Implies        = "=>";   
    public static final String BImplies       = "<=";    
    public static final String Equiv          = "<=>";
    public static final String Universal      = "!";
    public static final String Existential    = "?";
    public static final String Negation       = "~";
    public static final String Equals         = "=";
    
    public static HashMap<String,String> opMap = new HashMap<String,String>();

    /** ***************************************************************
     */
    public static void init() {
        opMap.put(Or,"or");
        opMap.put(And,"and");   
        opMap.put(Implies,"=>");   
        opMap.put(BImplies,"<=");   
        opMap.put(Equiv,"<=>");   
        opMap.put(Universal,"forall");   
        opMap.put(Existential,"exists");   
        opMap.put(Negation,"not");
        opMap.put(Equals,"equals");
    }

    /** ***************************************************************
     */
    public static String format(String form) {

        String indent = "  ";
        if (form == null)
            return "";
        String result = form;
        if (!Term.emptyString(form))
            form = form.trim();
        String legalTermChars = "-:";
        String varStartChars = "?@";
        StringBuilder token = new StringBuilder();
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        boolean inQuantifier = false;
        boolean inToken = false;
        boolean inVariable = false;
        boolean inVarlist = false;
        boolean inComment = false;

        int flen = form.length();
        char pch = '0';  // char at (i-1)
        char ch = '0';   // char at i
        for (int i = 0; i < flen; i++) {
            ch = form.charAt(i);
            if (inComment) {     // In a comment
                formatted.append(ch);
                if (ch == '"')
                    inComment = false;
            }
            else {
                if ((ch == '(')
                        && !inQuantifier
                        && ((indentLevel != 0) || (i > 1))) {
                    if ((i > 0) && Character.isWhitespace(pch)) {
                        formatted = formatted.deleteCharAt(formatted.length()-1);
                    }
                    formatted.append("\n");
                    for (int j = 0; j < indentLevel; j++)
                        formatted.append(indent);
                }
                if ((i == 0) && (indentLevel == 0) && (ch == '('))
                    formatted.append(ch);
                if (!inToken && !inVariable && Character.isJavaIdentifierStart(ch)) {
                    token = new StringBuilder(ch);
                    inToken = true;
                }
                if (inToken && (Character.isJavaIdentifierPart(ch)
                        || (legalTermChars.indexOf(ch) > -1)))
                    token.append(ch);
                if (ch == '(') {
                    if (inQuantifier) {
                        inQuantifier = false;
                        inVarlist = true;
                        token = new StringBuilder();
                    }
                    else
                        indentLevel++;
                }
                if (ch == '"')
                    inComment = true;
                if (ch == ')') {
                    if (!inVarlist)
                        indentLevel--;
                    else
                        inVarlist = false;
                }
                if ((token.indexOf("forall") > -1) || (token.indexOf("exists") > -1))
                    inQuantifier = true;
                if (inVariable
                        && !Character.isJavaIdentifierPart(ch)
                        && (legalTermChars.indexOf(ch) == -1))
                    inVariable = false;
                if (varStartChars.indexOf(ch) > -1)
                    inVariable = true;
                if (inToken
                        && !Character.isJavaIdentifierPart(ch)
                        && (legalTermChars.indexOf(ch) == -1)) {
                    inToken = false;
                    formatted.append(token);
                    token = new StringBuilder();
                }
                if ((i > 0) && !inToken && !(Character.isWhitespace(ch) && (pch == '('))) {
                    if (Character.isWhitespace(ch)) {
                        if (!Character.isWhitespace(pch))
                            formatted.append(" ");
                    }
                    else
                        formatted.append(ch);
                }
            }
            pch = ch;
        }
        if (inToken)   // A term which is outside of parenthesis, typically, a binding.
            formatted.append(token);            
        result = formatted.toString();

        return result;
    }
}

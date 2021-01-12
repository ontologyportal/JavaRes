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

This is a Java rewrite of PyRes - https://github.com/eprover/PyRes
*/
package atp;

import org.junit.*;

import java.io.StringReader;
import java.util.*;
import static org.junit.Assert.*;
public class IndexedClauseSetTest {

    /** ***************************************************************
     */
    public static String spec = null;

    /** ***************************************************************
     */
    @Test
    public void testIndexedClauseSetChanges() {

        //Test that clause set initialization and parsing work.

        System.out.println("---------------------");
        ClauseSet clauses = ClauseSet.parseFromFile("/home/apease/EProver/fod_pi/PYTHON/EXAMPLES/PUZ001-1.p");
        System.out.println("testClauseSetChanges() clauses: \n" + clauses);
        IndexedClauseSet iclauses = new IndexedClauseSet(clauses);
        System.out.println(iclauses);
        int oldlen = iclauses.clauses.size();
        Clause c = iclauses.clauses.get(0);
        clauses.extractClause(c);
        assertEquals(iclauses.clauses.size(), oldlen-1);
        Signature sig = clauses.collectSig();
        System.out.println(sig);
    }
}

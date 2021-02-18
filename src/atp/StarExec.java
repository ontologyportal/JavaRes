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
            String cmd = "timeout 300 python3 /home/apease/workspace/PyRes/pyres-fof.py -tifbp -HPickGiven5 -nlargest --silent ";
            ArrayList<String> commands = new ArrayList<>(Arrays.asList(
                    "timeout", "30", "python3", "/home/apease/workspace/PyRes/pyres-fof.py",
                    "-tifbp", "-HPickGiven5", "-nlargest", "--silent", filename));

            System.out.println("execPyRes(): command: " + commands);
            _builder = new ProcessBuilder(commands);
            _builder.redirectErrorStream(true);
            _pyres = _builder.start();
            System.out.println("EProver(): process: " + _pyres);
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
     * Compare results running tests on PyRes and JavaRes and report
     * any difference.
     * @param dir is a directory of .p files to test on
     */
    public static void compare(String dir) {

        File tptpdir = new File(dir);
        String[] children = tptpdir.list();  // get the problem list files first.
        for (String f : children) {
            ArrayList<String> pyout = execPyRes(dir + File.separator + f);
            //String stats = processPyOut(pyout)
        }
    }

    /***************************************************************
     */
    public static void showHelp() {

        System.out.println(" -c <file> : build a collection of TPTP problems ");
        System.out.println("      and put in a subdir of the same name as the file");
        System.out.println(" -p <file> : run PyRes");
        System.out.println("  -h : show help");
    }

    /***************************************************************
     */
    public static void main(String[] args) {

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
            else if (args[0].equals("-t"))
                compare(args[1]);
            else
                showHelp();
        }
        else
            showHelp();

    }

}
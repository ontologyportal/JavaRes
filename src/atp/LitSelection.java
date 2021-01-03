package atp;

import java.util.*;
import org.junit.Test;

public class LitSelection {

    /***************************************************************
     */
    @Test
    public static ArrayList<Literal> firstLit(ArrayList<Literal> litlist) {

        ArrayList<Literal> result = new ArrayList<>();
        result.add(litlist.get(0));
        return result;
    }
}

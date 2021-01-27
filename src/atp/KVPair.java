package atp;

public class KVPair implements Comparable {
    // A simple key value pair
    public Clause c = new Clause();
    public int value = -1;

    /** ***************************************************************
     */
    public KVPair(Clause k, int v) { c = k; value = v; }

    /** ***************************************************************
     */
    public int compareTo(Object o) {

        //System.out.println("KVPair.compareTo(): " + o.getClass().getName());
        if (!o.getClass().getName().equals("atp.KVPair"))
            throw new ClassCastException();
        KVPair okvp = (KVPair) o;
        if (!c.equals(okvp.c)) {
            return c.compareTo(okvp.c);
        }
        else {
            if (value != okvp.value)
                return (value > okvp.value) ? 1 : -1;
        }
        return 0;
    }

    /** ***************************************************************
     */
    @Override
    public boolean equals(Object o) {

        //System.out.println("KVPair.equals(): " + o.getClass().getName());
        if (!o.getClass().getName().equals("atp.KVPair"))
            throw new ClassCastException();
        KVPair okvp = (KVPair) o;
        if (c.equals(okvp.c) && value == okvp.value)
            return true;
        else
            return false;
    }

    /** ***************************************************************
     */
    @Override
    public int hashCode() {
        return c.hashCode() + value;
    }

    /** ***************************************************************
     */
    public String toString() {
        return "(" + c + "," + value + ")";
    }
}

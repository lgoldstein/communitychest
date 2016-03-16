/*
 *
 */
package net.community.chest.lang.math;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 27, 2009 12:08:02 PM
 */
public class DoublesComparator extends DefaultNumbersComparator<Double> {
    /**
     *
     */
    private static final long serialVersionUID = -3579620467814214876L;

    public DoublesComparator (boolean ascending)
    {
        super(Double.class, ascending);
    }

    public static final int compare (double v1, double v2)
    {
        return Double.compare(v1, v2);
    }

    public static final DoublesComparator    ASCENDING=new DoublesComparator(true),
                                            DESCENDING=new DoublesComparator(false);

}

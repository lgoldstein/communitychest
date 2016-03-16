/*
 *
 */
package net.community.chest.test.teasers;

import java.math.BigDecimal;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>Shows a quirk for {@link BigDecimal} where 2 numbers are not equal, but they yield zero
 * (i.e. equal) when compared via {@link BigDecimal#compareTo(BigDecimal)}</P>
 * @author Lyor G. - based on <A HREF="mailto:brian@quiotix.com">Brian Goetz</A>'s article
 * <A HREF="http://www.ibm.com/developerworks/java/library/j-jtp0114/index.html">Java theory and practice: Where's your point ?</A>
 * - section "All equals methods are not created equal"
 * @since Nov 23, 2010 3:56:00 PM
 */
public class BigNumbersEquality {
    public static void main (String[] args)
    {
        final BigDecimal    v1=new BigDecimal("100.00"), v2=new BigDecimal("100.000");
        System.out.append(v1.toString()).append(v1.equals(v2) ? " == " : " <> ").append(v2.toString()).println();
        System.out.append(v1.toString()).append((v1.compareTo(v2) == 0) ? " == " : " <> ").append(v2.toString()).println();
    }
}

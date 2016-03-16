/*
 *
 */
package net.community.chest.test.teasers;

import java.io.BufferedReader;
import java.io.PrintStream;

import net.community.chest.test.TestBase;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Start with 1 unit of money and keep buying at increments of 0.1 units
 * until running out of money. The problem with using <code>float</code> or
 * </code>double</code> is that negative powers of 10 (0.1, 0.01, etc.)
 * cannot be represented accurately using such types (see David Goldberg's
 * March 1991 <a href="http://www.validlab.com/goldberg/paper.pdf">article</a>
 * and also this <a href="http://www.concentric.net/~Ttwang/tech/javafloat.htm">article<a/>
 * about floating point "idiosyncrasies")</P>
 *
 * @author Lyor G.
 * @since Jun 10, 2009 10:46:09 AM
 */
public class FloatingPointAccuracy extends TestBase {
    private static final void testFloatAccuracy (final PrintStream out)
    {
        float    funds=1.00f;
        int        numItems=0;
        for (float price=.10f; funds >= price; price += .10f)
        {
            funds -= price;
            numItems++;
            out.println("\tBought item at price=" + price + ", left funds=" + funds);
        }

        out.println("Bought " + numItems + " items, change=" + funds);
    }

    private static final void testDoubleAccuracy (final PrintStream out)
    {
        double    funds=1.00d;
        int        numItems=0;
        for (double price=.10d; funds >= price; price += .10d)
        {
            funds -= price;
            numItems++;
            out.println("\tBought item at price=" + price + ", left funds=" + funds);
        }

        out.println("Bought " + numItems + " items, change=" + funds);
    }

    private static final void testIntegerAccuracy (final PrintStream out)
    {
        int    funds=100, numItems=0;
        for (int price=10; funds >= price; price += 10)
        {
            funds -= price;
            numItems++;
            out.println("\tBought item at price=" + price + ", left funds=" + funds);
        }

        out.println("Bought " + numItems + " items, change=" + funds);
    }

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
        final PrintStream        out=System.out;

        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    ans=
                (aIndex < numArgs) ? args[aIndex] : getval(out, in, "(D)ouble/(F)loat/[I]nteger/(Q)uit");
            if (isQuit(ans)) break;

            final char    c=((null == ans) || (ans.length() <= 0)) ? '\0' : Character.toUpperCase(ans.charAt(0));
            switch(c)
            {
                case '\0'    :
                case 'I'    : testIntegerAccuracy(out); break;
                case 'D'    : testDoubleAccuracy(out); break;
                case 'F'    : testFloatAccuracy(out); break;
                default        : // do nothing
            }
        }
    }
}

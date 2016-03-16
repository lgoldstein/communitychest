package net.community.chest.test.teasers;

import java.io.BufferedReader;
import java.io.PrintStream;

import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * Autobox/unbox quirks
 *
 * @author Lyor G.
 * @since Mar 12, 2008 1:10:57 PM
 */
public class AutoboxEquality extends TestBase {
    @SuppressWarnings("boxing")
    private static final void testAutoboxEquality (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    val=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "value (or Quit)");
            if ((null == val) || (val.length() <= 0))
                continue;
            if (isQuit(val)) break;

            final String    ans=getval(out, in, "treat  " + val + " as [N]umber/(B)oolean");
            final char        tch=((null == ans) || (ans.length() <= 0)) ? '\0' : Character.toLowerCase(ans.charAt(0));
            try
            {
                if (('\0' == tch) || ('n' == tch))
                {
                    final int        numVal=Integer.parseInt(val);
                    final Integer    a=new Integer(numVal), b=numVal;
                    if (a != numVal)    // NEVER
                        out.println("Object <> number");
                    if (a != b)    // ALWAYS
                        out.println("Object <> Object");

                    final Integer    x=Integer.valueOf(numVal), y=Integer.valueOf(numVal);
                    if (x != y)    // for values outside the range [-127,128]
                        out.println("valueOf <> valueOf");
                    if (a == x)    // NEVER
                        out.println("Object == valueOf");
                }
                else
                {
                    final boolean    flagVal=Boolean.valueOf(val).booleanValue();
                    final Boolean    a=new Boolean(flagVal), b=flagVal;
                    if (a != flagVal)    // NEVER
                        out.println("Object <> flag");
                    if (a != b)    // ALWAYS
                        out.println("Object <> Object");

                    final Boolean    x=Boolean.valueOf(flagVal);
                    if (x != a)    // ALWAYS
                        out.println("valueOf <> Object (A)");
                    if (x != b)    // NEVER
                        out.println("valueOf <> Object (B)");
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
        /*
        {
                      final int        TESTVAL=777;
              final Integer    i=TESTVAL, j=TESTVAL, k=Integer.valueOf(TESTVAL);
              if (i == TESTVAL)
            System.out.println("i == " + TESTVAL);
              if (i == j)
            System.out.println("i == j");
              if (i == k)
            System.out.println("i == k");
        }
        */
        /*
        {
            final float    TESTVAL=777.0f;
            final Float    f1=TESTVAL, f2=TESTVAL;
            if (f1 == TESTVAL)
                System.out.println("f1 == " + TESTVAL);
            if (f1 == f2)
                System.out.println("f1 == f2");
        }
        */

                /*
                {
            final Boolean    b1=true, b2=true;
            if (b1 == true)
            System.out.println("b1 == true");
            if (b1 == b2)
            System.out.println("b1 == b2");
                }
        */

                /*
                {
                  final Double    d1=Double.NaN, d2=Double.NaN;
           if (d1 == Double.NaN)
            System.out.println("d1 == NaN");
           if (Double.isNaN(d1))
            System.out.println("d1 is NaN");
           if (d1 == d2)
            System.out.println("d1 == d2");
                 }
        */
        testAutoboxEquality(System.out, in, args);
    }
}

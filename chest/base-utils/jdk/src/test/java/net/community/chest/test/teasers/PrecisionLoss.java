package net.community.chest.test.teasers;

import java.io.BufferedReader;
import java.util.Random;

import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * Conversions can cause loss of precision
 *
 * @author Lyor G.
 * @since Mar 12, 2008 2:39:08 PM
 */
public final class PrecisionLoss extends TestBase {
    public static void main (String[] args)
    {
        final int                numArgs=(null == args) ? 0 : args.length;
        final BufferedReader    in=getStdin();
        final Random            rnd=new Random(System.currentTimeMillis());
        for (int    aIndex=0; ; aIndex++)
        {
            final String    iNum=(aIndex < numArgs) ? args[aIndex] : getval(System.out, in, "# iterations (or Quit)");
            if ((null == iNum) || (iNum.length() <= 0))
                continue;
            if (isQuit(iNum)) break;

            try
            {
                final int    numTests=Integer.parseInt(iNum);
                int            numFailures=0;
                for (int    tIndex=1; tIndex <= numTests; tIndex++)
                {
                    final int i=rnd.nextInt();
                    if (Math.round(i) != i) // i "promoted" to float => causes ~1bit of precision loss
                        numFailures++;
                }

                System.out.println("\t" + numFailures + " out of " + numTests + " failed (" + ((100.0f * (double) numFailures) / numTests) + ")");
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }
}

/**
 *
 */
package net.community.chest.apache.httpclient.test;

import java.io.BufferedReader;
import java.io.PrintStream;

import net.community.chest.apache.httpclient.hotmail.HotmailClient;
import net.community.chest.io.FileUtil;
import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 3, 2008 12:08:07 PM
 */
public final class HotmailTester extends TestBase {
    private HotmailTester ()
    {
        // no instance
    }

    //////////////////////////////////////////////////////////////////////////

    private static final int testHotmailAccessor (final PrintStream out, final BufferedReader in, final HotmailClient c)
    {
        return 0;
    }
    /*----------------------------------------------------------------------*/

    // args[i]=username, args[i+1]=password
    public static final int testHotmailAccessor (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int        numArgs=(null == args) ? 0 : args.length;
        final String[]    prompts={ "username", "password" };
        for (int    aIndex=0; ; aIndex += 2)
        {
            final String[]    curArgs=((aIndex + 1) < numArgs) ?
                    new String[]{ args[aIndex], args[aIndex+1] } : null;

            try
            {
                final String[]    tstArgs=resolveTestParameters(out, in, curArgs, prompts);
                if ((null == tstArgs) || (tstArgs.length < 2))
                    break;

                final HotmailClient    c=new HotmailClient();
                try
                {
                    out.println("\tLogging in...");
                    final long    lStart=System.currentTimeMillis();
                    c.login(tstArgs[0], tstArgs[1]);
                    final long    lEnd=System.currentTimeMillis(), lDuration=lEnd - lStart;
                    out.println("\tLogged in after " + lDuration + " msec.");

                    testHotmailAccessor(out, in, c);
                }
                finally
                {
                    FileUtil.closeAll(c);
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
        final int                nErr=testHotmailAccessor(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }
}

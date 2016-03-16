/**
 *
 */
package net.community.chest.test;

import java.io.BufferedReader;
import java.io.PrintStream;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.datetime.DateUtil;
import net.community.chest.util.datetime.Duration;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 30, 2008 10:08:35 AM
 */
public final class DateUtilsTester extends TestBase {

    //////////////////////////////////////////////////////////////////////////

    // each argument is a date expression string (dd/mm/yyyy, dd/mm/yy, dd-mmm-yyyy, etc.)
    public static final int testDateComponentsParser (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    inString=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "date input string (or Quit)");
            final int        inLen=(null == inString) ? 0 : inString.length();
            if (inLen <= 0)
                continue;
            if (isQuit(inString))
                break;

            try
            {
                final char    ch3=(inLen > 3) ? inString.charAt(2) : '\0',
                            sep=(('-' == ch3) || ('/' == ch3)) ? ch3 : '\0';
                final int[]    da=DateUtil.getDateComponents(inString, sep);
                out.print("\t" + inString + " =>");
                for (final int dc : da)
                    out.print(" " + dc);
                out.println();
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while parse string=" + inString + ": " + e.getMessage());
            }
        }

        return 0;
    }


    //////////////////////////////////////////////////////////////////////////

    // each argument is a time expression string (hh:mm:ss, hh:mm, hh, etc.)
    public static final int testTimeComponentsParser (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    inString=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "time input string (or Quit)");
            final int        inLen=(null == inString) ? 0 : inString.length();
            if (inLen <= 0)
                continue;
            if (isQuit(inString))
                break;

            final int    sepCount=StringUtil.getInstancesCount(DateUtil.DEFAULT_TMSEP, inString);
            try
            {
                final int[]    da=DateUtil.getTimeComponents(inString, sepCount < 3);
                out.print("\t" + inString + " =>");
                for (final int dc : da)
                    out.print(" " + dc);
                out.println();
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while parse string=" + inString + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////
    // each argument is assumed to be a long duration value
    public static final int testDurationParsing (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    aVal=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "duration value");
            if ((null == aVal) || (aVal.length() <= 0))
                continue;
            if (isQuit(aVal)) break;

            try
            {
                final Long        v=Long.decode(aVal);
                final String    s=Duration.toString(v);
                out.println("\t" + aVal + " => " + s);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while parse value=" + aVal + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
//        final int                nErr=testDateComponentsParser(System.out, in, args);
//        final int                nErr=testTimeComponentsParser(System.out, in, args);
        final int                nErr=testDurationParsing(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }
}

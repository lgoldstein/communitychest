package net.community.chest.util.logging.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;
import java.util.Random;

import net.community.chest.io.EOLStyle;
import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.lang.StringUtil;
import net.community.chest.test.TestBase;
import net.community.chest.util.logging.AbstractLoggingPrintStream;
import net.community.chest.util.logging.LogLevelWrapper;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.LoggerWrapperOutputStream;
import net.community.chest.util.logging.factory.WrapperFactoryManager;
import net.community.chest.util.logging.io.LoggerWrapperWriter;

/**
 * Copyright 2007 as per GPLv2
 *
 * Runs some tests for the {@link LoggerWrapper}
 * @author Lyor G.
 * @since Jun 27, 2007 2:46:24 PM
 */
public class TestLoggerWrapper extends TestBase {
    protected TestLoggerWrapper ()
    {
        super();
    }

    /* --------------------------------------------------------------------- */

    public static final int testLoggerWrapper (final PrintStream out, final BufferedReader in, final LoggerWrapper l)
    {
        final Random    r=new Random(System.currentTimeMillis());
        for ( ; ; )
        {
            final String    ansNum=getval(out, in, "# messages to send (or Quit)");
            if ((null == ansNum) || (ansNum.length() <= 0))
                continue;
            if (isQuit(ansNum))
                break;

            final int    numMsgs;
            try
            {
                if ((numMsgs=Integer.parseInt(ansNum)) <= 0)
                    throw new NumberFormatException("Non-positive number");
            }
            catch(NumberFormatException e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                continue;
            }

            final String    ansSleep=getval(out, in, "max. sleep time msec. (ENTER=0,Quit)");
            if (isQuit(ansSleep))
                break;

            final long    maxSleep;
            try
            {
                if ((null == ansSleep) || (ansSleep.length() <= 0))
                    maxSleep = 0L;
                else if ((maxSleep=Long.parseLong(ansSleep)) < 0L)
                    throw new NumberFormatException("Negative duration");
            }
            catch(NumberFormatException e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                continue;
            }

            final String    ansStep=getval(out, in, "show step mark ([Y]/n/q)");
            if (isQuit(ansStep))
                break;

            int    stepChar=((null == ansStep) || (ansStep.length() <= 0) || ('y' == Character.toLowerCase(ansStep.charAt(0)))) ? 1 : 0;
            if (stepChar > 0)
            {
                if (numMsgs >= 100)
                    stepChar *= 10;
                if (numMsgs >= 1000)
                    stepChar *= 5;
                if (numMsgs >= 10000)
                    stepChar *= 2;
                if (numMsgs >= 100000)
                    stepChar *= 5;
                if (numMsgs >= 1000000)
                    stepChar *= 10;
                if (numMsgs >= 10000000)
                    stepChar *= 2;
            }

            final String    ansPct=getval(out, in, "% exceptions (ENTER=0)");
            if (isQuit(ansPct))
                break;

            int    errPct=0;
            if ((ansPct != null) && (ansPct.length() > 0))
            {
                try
                {
                    if (((errPct=Integer.parseInt(ansPct)) < 0) || (errPct >= 100))
                        throw new NumberFormatException("Bad percentage value");
                }
                catch(NumberFormatException e)
                {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    errPct = 0;
                }
            }

            for (int    i=1, numSteps=0; i <= numMsgs; i++)
            {
                final long        rVal=(maxSleep > 0L) ? (r.nextInt(Short.MAX_VALUE) % maxSleep) : 0L;
                final int        lChoice=r.nextInt(5);
                final String    msg="test message #" + i + " (wait=" + rVal + ";level=" + lChoice + ")";
                final int        tChoice=r.nextInt(100);
                final Throwable    t;
                if (tChoice <= errPct)    // generate an exception ~10% of the time
                {
                    final String    err="exception message choice=" + tChoice;
                    switch(tChoice)
                    {
                        case 0     : t = new RuntimeException(err); break;
                        case 1     : t = new IllegalArgumentException(err); break;
                        case 2     : t = new NullPointerException(err); break;
                        case 3     : t = new IllegalStateException(err); break;
                        case 4     : t = new IOException(err); break;
                        case 5     : t = new URISyntaxException(err, err); break;
                        case 6     : t = new SocketException(err); break;
                        case 7     : t = new OutOfMemoryError(err); break;

                        default    :
                            t = new NoSuchElementException(err);
                    }

                    if (r.nextBoolean())
                        t.initCause(new IOException("Cause test message"));
                }
                else
                    t = null;

                switch(lChoice)
                {
                    case 0    : l.fatal(msg, t); break;
                    case 1    : l.error(msg, t); break;
                    case 2    : l.warn(msg, t); break;
                    case 3    : l.info(msg, t); break;
                    case 4    : l.debug(msg, t); break;
                    default    :
                        l.fatal(msg + " - unknown level choice: " + lChoice);
                }

                if (stepChar > 0)
                {
                    if ((1 == stepChar) || (0 == (i % stepChar)))
                    {
                        numSteps++;
                        if (numSteps >= 64)
                            out.println();
                        out.print('#');
                    }
                }

                if (rVal > 0L)
                {
                    try
                    {
                        Thread.sleep(rVal);
                    }
                    catch(InterruptedException e)
                    {
                        l.error("Sleep duration=" + rVal + " interrupted", e);
                    }
                }
            }
        }

        return 0;
    }

    public static final int testLoggerWrapper (final PrintStream out, final BufferedReader in, final Class<?> logClass)
    {
        return testLoggerWrapper(out, in, WrapperFactoryManager.getLogger(logClass));
    }

    public static final int testLoggerWrapper (final PrintStream out, final BufferedReader in, final String... args)
    {
        return testLoggerWrapper(out, in, TestLoggerWrapper.class);
    }

    //////////////////////////////////////////////////////////////////////////

    public static final int testLoggerWrapperStream (final PrintStream out, final BufferedReader in, final LoggerWrapper logger, final String... args)
    {
        final LoggerWrapperOutputStream    lwo=new LoggerWrapperOutputStream(logger, LogLevelWrapper.INFO);
        final int                        numArgs=(null == args) ? 0 : args.length;
        for (int aIndex=0; ; aIndex++)
        {
            final String    fpath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "dump file path (Quit)");
            if ((null == fpath) || (fpath.length() <= 0))
                continue;
            if (isQuit(fpath)) break;

            for ( ; ; )
            {
                out.println("Copying file " + fpath);

                FileInputStream    fin=null;
                try
                {
                    try
                    {
                        fin = new FileInputStream(fpath);

                        final long    cpyStart=System.currentTimeMillis(),
                                    cpySize=IOCopier.copyStreams(fin, lwo),
                                    cpyEnd=System.currentTimeMillis(),
                                    cpyDuration=cpyEnd - cpyStart;
                        if (cpySize < 0L)
                            throw new StreamCorruptedException("Failed (" + cpySize + ") to copy");
                        out.println("\tCopied " + cpySize + " bytes in " + cpyDuration + " msec. - " + fpath);
                    }
                    finally
                    {
                        FileUtil.closeAll(fin);
                    }
                }
                catch(IOException e)
                {
                    System.err.println(e.getClass().getName() + " on copy of " + fpath + ": " + e.getMessage());
                }

                final String    ans=getval(out, in, "again [y]/n");
                if ((null == ans) || (ans.length() <= 0) || ('y' == Character.toLowerCase(ans.charAt(0))))
                    continue;
                break;
            }
        }

        try
        {
            FileUtil.closeAll(lwo);
        }
        catch(IOException e)
        {
            System.err.println(e.getClass().getName() + " on close: " + e.getMessage());
        }

        return 0;
    }

    public static final int testLoggerWrapperStream (final PrintStream out, final BufferedReader in, final Class<?> logClass, final String... args)
    {
        return testLoggerWrapperStream(out, in, WrapperFactoryManager.getLogger(logClass), args);
    }

    public static final int testLoggerWrapperStream (final PrintStream out, final BufferedReader in, final String... args)
    {
        return testLoggerWrapperStream(out, in, TestLoggerWrapper.class, args);
    }

    //////////////////////////////////////////////////////////////////////////

    private static class TestLogPrintStream extends AbstractLoggingPrintStream {
        private final Appendable    _out;
        protected TestLogPrintStream (final Appendable a)
        {
            _out = a;
        }
        /*
         * @see net.community.chest.util.logging.AbstractLoggingPrintStream#log(net.community.chest.util.logging.LogLevelWrapper, java.lang.StackTraceElement, java.lang.String)
         */
        @Override
        public void log (LogLevelWrapper l, StackTraceElement ce, String s) throws IOException
        {
            _out.append('[')
                .append((null == l) ? "???" : l.name())
                .append("]@")
                .append((null == ce) ? "???" : ce.toString())
                .append((null == s) ? "null" : s)
                .append(EOLStyle.LOCAL.getStyleString())
                ;
        }
    }

    public static final int testLoggingPrintStream (final PrintStream out, final BufferedReader in, final String... args)
    {
        @SuppressWarnings("resource")
        final PrintStream    ps=new TestLogPrintStream(out);
        final int            numArgs=(null == args) ? 0 : args.length;
        for (int aIndex=0; ; aIndex++)
        {
            final String    msg=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "text message (Quit)");
            if ((null == msg) || (msg.length() <= 0))
                continue;
            if (isQuit(msg)) break;
            ps.println(msg);
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static final int testLoggingWrapperWriter(
            final PrintStream out, final BufferedReader in, final LoggerWrapper logger, final LogLevelWrapper lvl, final String ... args)
    {
        try(PrintWriter    pw=new PrintWriter(new LoggerWrapperWriter(logger, lvl))) {
            final int            numArgs=(null == args) ? 0 : args.length;
            for (int aIndex=0; ; aIndex++)
            {
                final String    msg=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "text message (Quit)");
                if ((null == msg) || (msg.length() <= 0))
                    continue;
                if (isQuit(msg)) break;

                final String    s=StringUtil.replaceEscapedCharacters(msg);
                pw.print(s);
            }
        }

        return 0;
    }

    public static final int testLoggingWrapperWriter (final PrintStream out, final BufferedReader in, final String... args)
    {
        final LoggerWrapper logger=WrapperFactoryManager.getLogger(TestLoggerWrapper.class);
        for (LogLevelWrapper    lvl=LogLevelWrapper.INFO; ; )
        {
            if (null == (lvl=inputEnumValue(out, in, "select level", false, lvl, LogLevelWrapper.VALUES)))
                break;

            testLoggingWrapperWriter(out, in, logger, lvl, args);
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        try
        {
            final BufferedReader    in=getStdin();
//            final int                nErr=testLoggerWrapper(System.out, in, args);
//            final int                nErr=testLoggerWrapperStream(System.out, in, args);
//            final int                nErr=testLoggingPrintStream(System.out, in, args);
            final int                nErr=testLoggingWrapperWriter(System.out, in, args);
            if (nErr != 0)
                System.err.println("Failed: " + nErr);
            else
                System.err.println("OK completed.");
        }
        catch(Throwable t)
        {
            System.err.println(t.getClass().getName() + ": " + t.getMessage());
            t.printStackTrace(System.err);
        }
    }
}

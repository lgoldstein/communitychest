package net.community.chest.eclipse.classpath.test;

import java.io.BufferedReader;
import java.io.PrintStream;

import net.community.chest.eclipse.classpath.ClasspathFileTransformer;
import net.community.chest.eclipse.wst.WstComponentsFileTransformer;
import net.community.chest.test.DOMUtilsTester;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Nov 22, 2007 1:25:11 PM
 */
public final class ClasspathTester extends DOMUtilsTester {
    private ClasspathTester ()
    {
        // no instance
    }

    // each argument is an XML file representing an Eclipse '.classpath' structured file
    public static final int testClasspathTransformer (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    inPath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "'classpath' input file (or Quit)");
            if ((null == inPath) || (inPath.length() <= 0))
                continue;
            if (isQuit(inPath))
                break;

            try
            {
                testDocumentTransformer(out, in, inPath, ClasspathFileTransformer.DEFAULT, inPath + ".txt");
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while parse file=" + inPath + ": " + e.getMessage());
                continue;
            }

        }

        return 0;
    }

    // each argument is an XML file representing an Eclipse '.classpath' structured file
    public static final int testWstFileTransformer (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    inPath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "WST input file (or Quit)");
            if ((null == inPath) || (inPath.length() <= 0))
                continue;
            if (isQuit(inPath))
                break;

            try
            {
                testDocumentTransformer(out, in, inPath, WstComponentsFileTransformer.DEFAULT, inPath + ".txt");
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while parse file=" + inPath + ": " + e.getMessage());
                continue;
            }

        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
//        final int                nErr=testClasspathTransformer(System.out, in, args);
        final int                nErr=testWstFileTransformer(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }
}

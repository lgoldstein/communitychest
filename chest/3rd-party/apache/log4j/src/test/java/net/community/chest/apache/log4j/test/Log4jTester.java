package net.community.chest.apache.log4j.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.io.StreamCorruptedException;

import net.community.chest.apache.log4j.factory.Log4jLoggerWrapperFactory;
import net.community.chest.resources.ResourceDataRetriever;
import net.community.chest.util.logging.factory.WrapperFactoryManager;
import net.community.chest.util.logging.test.TestLoggerWrapper;

import org.apache.log4j.xml.DOMConfigurator;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 1, 2007 9:13:08 AM
 */
public class Log4jTester extends TestLoggerWrapper {

    public static final boolean log4jInit (final PrintStream out, final String filePath)
    {
        if ((null == filePath) || (filePath.length() <= 0))
            return false;

        try
        {
            final File    f=new File(filePath);
            if ((!f.exists()) || (!f.canRead()) || (f.isDirectory()) || (f.length() <= 0L))
                throw new StreamCorruptedException("Bad XML file: " + f);

            out.println("Initializing log4j from file: " + filePath);
            DOMConfigurator.configure(filePath);
            // make sure logger wrappers use log4j factory
            System.setProperty(WrapperFactoryManager.MANAGER_FACTORY_CLASS_PATH_PROPNAME, Log4jLoggerWrapperFactory.class.getName());
            return true;
        }
        catch(Exception e)
        {
            System.err.println("log4jInit(" + filePath + ") " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

    // args[0]=log4j XML file path ("*" means use the default one)
    private static final int testLog4jLoggerWrapper (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final String[]    tstArgs=resolveTestParameters(out, in, args, new String[] { "log4j.xml file path" });
        String            xmlFilePath=((null == tstArgs) || (tstArgs.length <= 0)) ? null : tstArgs[0];
        if ((null == xmlFilePath) || (xmlFilePath.length() <= 0))
            return (-1);

        final Class<?>    logClass=Log4jTester.class;
        if ("default".equalsIgnoreCase(xmlFilePath))
        {
            try
            {
                final File    f=ResourceDataRetriever.getAnchorClassContainerLocation(logClass);
                xmlFilePath = f.getAbsolutePath() + File.separator + "log4j.xml";
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while resolve default XML file path: " + e.getMessage());
                return (-2);
            }
        }

        if (!log4jInit(out, xmlFilePath))
            return (-3);

        return testLoggerWrapper(out, in, WrapperFactoryManager.getLogger(logClass));
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
        final int                nErr=testLog4jLoggerWrapper(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }
}

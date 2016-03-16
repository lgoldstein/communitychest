/*
 *
 */
package jnlp.sample.util.log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 23, 2009 2:34:51 PM
 */
public class DefaultLogger extends AbstractLogger implements Cloneable{
    // servlet configuration parameters
    public final static String LOG_LEVEL = "logLevel";
    public final static String LOG_PATH  = "logPath";

    private final ServletContext _servletContext;
    public final ServletContext getServletContext ()
    {
        return _servletContext;
    }

    private String _logFile    /* = null */;
    public String getLogFile ()
    {
        return _logFile;
    }

    public void setLogFile (String logFile)
    {
        _logFile = logFile;
    }

    private final String _servletName    /* = null */;
    public final String getServletName ()
    {
        return _servletName;
    }
    /**
     * Initialize logging object. It reads the logLevel and pathLevel
     * initialization parameters. Default is logging level FATAL, and
     * logging using the ServletContext.log
     * @param config The initial {@link ServletConfig} instance
     * @param resources The default {@link ResourceBundle} to use for internal
     * configuration of texts
     */
    public DefaultLogger (ServletConfig config, ResourceBundle resources)
    {
        _servletContext = (null == config) ? null : config.getServletContext();
        _servletName = (null == config) ? null : config.getServletName();

        if ((_logFile=(null == config) ? null : config.getInitParameter(LOG_PATH)) != null)
            _logFile = _logFile.trim();
        if ((null == _logFile) || (_logFile.length() <= 0))
            _logFile = null;

        final String     level=(null == config) ? null : config.getInitParameter(LOG_LEVEL);
        final int        numLevel=getLevelValue(level);
        if (numLevel < 0)
            setLoggingLevel(FATAL);
        else
            setLoggingLevel(numLevel);
        setResourceBundle(resources);
    }
    /*
     * @see jnlp.sample.util.log.Logger#logEvent(int, java.lang.String, java.lang.Throwable)
     */
    @Override
    public synchronized void logEvent (final int level, final String string, final Throwable throwable)
    {
        // Check if the event should be logged
        if (level > getLoggingLevel())
            return;

        final String    logFile=getLogFile(),
                        lvlName=getLevelName(level),
                        ln=getLoggerName(),
                        srvltName=getServletName(),
                        lvlStr=
                ((null == lvlName) || (lvlName.length() <= 0)) ? String.valueOf(level) : lvlName,
                        msgStr=
                srvltName + "[" + ln + "](" + lvlStr + "): " + string;
        if ((logFile != null) && (logFile.length() > 0))
        {
            PrintWriter pw = null;
            try
            {
                pw = new PrintWriter(new FileWriter(logFile, true));
                pw.println(msgStr);
                if (throwable != null)
                    throwable.printStackTrace(pw);
                // Do a return here. An exception will cause a fall through to
                // do _servletContext logging API
                return;
            }
            catch (IOException ioe)
            {
                /* just ignore - fall through to logging via servlet context */
            }
            finally
            {
                if (pw != null)
                    pw.close();
            }
        }

        // No log file specified, log using servlet context
        final ServletContext    ctx=getServletContext();
        if (ctx != null)
        {
            if (throwable == null)
                ctx.log(msgStr);
            else
                ctx.log(msgStr, throwable);
        }
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    public DefaultLogger /* co-variant return */ clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
}

/*
 * 
 */
package jnlp.sample.util.log;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 21, 2009 10:06:44 AM
 */
public abstract class AbstractLogger implements Logger {
	protected AbstractLogger ()
	{
		super();
	}
    // returns negative value if not found
    public static final int getLevelValue (final String l)
    {
    	final String	level=(null == l) ? null : l.trim();
        if ((level != null) && (level.length() > 0))
       	{
        	if (level.equalsIgnoreCase(NONE_KEY))
        		return NONE;
        	else if (level.equalsIgnoreCase(FATAL_KEY))
        		return FATAL;
        	else if (level.equalsIgnoreCase(WARNING_KEY))
        		return WARNING;
        	else if (level.equalsIgnoreCase(INFORMATIONAL_KEY))
        		return INFORMATIONAL;
        	else if (level.equalsIgnoreCase(DEBUG_KEY))
        		return DEBUG;
        	else if (level.equalsIgnoreCase(TRACE_KEY))
        		return TRACE;
        }

        return Integer.MIN_VALUE;
    }
    // returns null/empty if no match
    public static final String getLevelName (final int l)
    {
    	switch(l)
    	{
    		case NONE			: return NONE_KEY;
    		case FATAL			: return FATAL_KEY;
    		case WARNING		: return WARNING_KEY;
    		case INFORMATIONAL	: return INFORMATIONAL_KEY;
    		case DEBUG			: return DEBUG_KEY;
    		case TRACE			: return TRACE_KEY;

    		default				:
    			return null;
    	}
    }

    private int _loggingLevel=FATAL;
    /*
     * @see jnlp.sample.util.log.Logger#getLoggingLevel()
     */
    @Override
	public int getLoggingLevel ()
    {
    	return _loggingLevel;
    }
    /*
     * @see jnlp.sample.util.log.Logger#setLoggingLevel(int)
     */
    @Override
	public void setLoggingLevel (int level)
    {
    	_loggingLevel = level;
    }

    private String	_loggerName;
    /*
     * @see jnlp.sample.util.log.Logger#getLoggerName()
     */
    @Override
	public String getLoggerName ()
    {
    	return _loggerName;
    }

    public void setLoggerName (String n)
    {
    	_loggerName = n;
    }
    // Localization
    private ResourceBundle  _resources;
    protected ResourceBundle getResourceBundle ()
    {
    	return _resources;
    }

    protected void setResourceBundle (ResourceBundle b)
    {
    	_resources = b;
    }
    // The method that actually does the logging */
    public abstract void logEvent (final int level, final String string, final Throwable throwable);

    // Returns a string from the resources    
    protected String getResourceString (final String key)
    {
        try
        {
        	if ((null == _resources) || (null == key) || (key.length() <= 0))
        		return key;

        	if (!_resources.containsKey(key))
        		return key;	// assume simple string

        	final String	resValue=_resources.getString(key);
        	if ((null == resValue) || (resValue.length() <= 0))
        		return key;

        	return resValue;
        }
        catch (MissingResourceException mre)	// unexpected since asking "containsKey"
        {
      		return mre.getClass().getName() + "[" + mre.getMessage() + "] " + key;
        }
    }
    /* Helper function that applies the messageArguments to a message from the resource object */
    public String applyPattern (String key, Object ... messageArguments)
    {
        final String message=getResourceString(key);
        return MessageFormat.format(message, messageArguments);
    }
    /*
     * @see jnlp.sample.util.log.Logger#fatal(java.lang.String, java.lang.Throwable)
     */
    @Override
	public void fatal (String key, Throwable throwable)
    { 
    	logEvent(FATAL, getResourceString(key), throwable); 
    }    
    /*
     * @see jnlp.sample.util.log.Logger#logArgs(int, java.lang.String, java.lang.Throwable, java.lang.Object[])
     */
    @Override
	public void logArgs (int level, String key, Throwable t, Object ... messageArguments)
    {	
        logEvent(level, applyPattern(key, messageArguments), t);    
    }
    /*
     * @see jnlp.sample.util.log.Logger#warn(java.lang.String, java.lang.Throwable, java.lang.Object[])
     */
    @Override
	public void warn (String key, Throwable t, Object ... messageArguments)
    { 
    	logArgs(WARNING, key, t, messageArguments); 
    }
    /*
     * @see jnlp.sample.util.log.Logger#warn(java.lang.String, java.lang.String[])
     */
    @Override
	public void warn (String key, String ...messageArguments)
    {
    	warn(key, null, (Object[]) messageArguments);
    }
    /*
     * @see jnlp.sample.util.log.Logger#info(java.lang.String, java.lang.Throwable, java.lang.Object[])
     */
    @Override
	public void info (String key, Throwable t, Object ... messageArguments)
    { 
    	logArgs(INFORMATIONAL, key, t, messageArguments); 
    }
    /*
     * @see jnlp.sample.util.log.Logger#info(java.lang.String, java.lang.String[])
     */
    @Override
	public void info (String key, String ...messageArguments)
    {
    	info(key, null, (Object[]) messageArguments);
    }
    /*
     * @see jnlp.sample.util.log.Logger#debug(java.lang.String, java.lang.Throwable)
     */
    @Override
	public void debug (String msg, Throwable throwable)
    { 
    	logEvent(DEBUG, msg, throwable); 
    }
    /*
     * @see jnlp.sample.util.log.Logger#debug(java.lang.String)
     */
    @Override
	public void debug (String msg)
    { 
    	debug(msg, null);
    }
    /*
     * @see jnlp.sample.util.log.Logger#trace(java.lang.String, java.lang.Throwable)
     */
    @Override
	public void trace (String msg, Throwable throwable)
    { 
    	logEvent(TRACE, msg, throwable); 
    }
    /*
     * @see jnlp.sample.util.log.Logger#trace(java.lang.String)
     */
    @Override
	public void trace (String msg)
    { 
    	trace(msg, null);
    }
    /*
     * @see jnlp.sample.util.log.Logger#isNoneLevel()
     */
    @Override
	public boolean isNoneLevel () { return getLoggingLevel() >= NONE; }
    /*
     * @see jnlp.sample.util.log.Logger#isFatalevel()
     */
    @Override
	public boolean isFatalevel () { return getLoggingLevel() >= FATAL; }
    /*
     * @see jnlp.sample.util.log.Logger#isWarningLevel()
     */
    @Override
	public boolean isWarningLevel () { return getLoggingLevel() >= WARNING; }
    /*
     * @see jnlp.sample.util.log.Logger#isInformationalLevel()
     */
    @Override
	public boolean isInformationalLevel () { return getLoggingLevel() >= INFORMATIONAL; }
    /*
     * @see jnlp.sample.util.log.Logger#isDebugLevel()
     */
    @Override
	public boolean isDebugLevel () { return getLoggingLevel() >= DEBUG; }
    /*
     * @see jnlp.sample.util.log.Logger#isTraceLevel()
     */
    @Override
	public boolean isTraceLevel () { return getLoggingLevel() >= TRACE; }
}

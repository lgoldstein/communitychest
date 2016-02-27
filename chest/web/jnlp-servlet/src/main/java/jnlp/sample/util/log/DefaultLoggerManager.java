/*
 * 
 */
package jnlp.sample.util.log;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.servlet.ServletConfig;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 23, 2009 2:42:20 PM
 */
public class DefaultLoggerManager implements LoggerManager {
	public DefaultLoggerManager ()
	{
		super();
	}

	private DefaultLogger	_defaultLogger;
	/*
	 * @see jnlp.sample.util.log.LoggerManager#initLogger(javax.servlet.ServletConfig, java.util.ResourceBundle)
	 */
	@Override
	public void initLogger (ServletConfig config, ResourceBundle resources)
	{
    	if (null == _defaultLogger)
    		_defaultLogger = new DefaultLogger(config, resources);
	}

	private final Map<String,DefaultLogger>	_loggersMap=new TreeMap<String,DefaultLogger>(String.CASE_INSENSITIVE_ORDER);
	/*
	 * @see jnlp.sample.util.log.LoggerManager#getLogger(java.lang.String)
	 */
	@Override
	public Logger getLogger (String loggerName)
	{
		DefaultLogger	l=_defaultLogger;
    	if ((loggerName != null) && (loggerName.length() > 0))
    	{
    		synchronized(_loggersMap)
    		{
    			if ((l=_loggersMap.get(loggerName)) != null)
    				return l;

    			if (_defaultLogger != null)
    			{
    				try
    				{
						l = _defaultLogger.clone();
					}
					catch(CloneNotSupportedException e)
					{
						throw new UnsupportedOperationException("getLogger(" + loggerName + ") failed to clone");
					}

					l.setLoggerName(loggerName);
    				_loggersMap.put(loggerName, l);
    			}
    		}
    	}
 
    	if (null == l)
    		throw new IllegalStateException("getLogger(" + loggerName + ") no logger initializer");
    	return l;
    }
}

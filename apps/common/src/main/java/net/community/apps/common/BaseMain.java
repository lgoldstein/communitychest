/**
 * 
 */
package net.community.apps.common;

import java.util.Map;

import javax.swing.JFrame;
import javax.swing.UIManager;

import net.community.chest.resources.PropertiesResolver;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>A base &quot;main&quot; class for UI applications</P>
 * @author Lyor G.
 * @since Aug 12, 2008 10:34:42 AM
 */
public abstract class BaseMain implements Runnable {
	private static LoggerWrapper	_logger	/* =null */;
	private static final synchronized LoggerWrapper getLogger ()
	{
		if (null == _logger)
			_logger = WrapperFactoryManager.getLogger(BaseMain.class);
		return _logger;
	}

	public static final String resolveStringArg (
			final String a, final String[] args, final int numArgs, final int aIndex, final String curVal)
	{
		if (aIndex >= numArgs)
			throw new IllegalArgumentException("Missing " + a + " option argument");

		final String	v=args[aIndex];
		if ((null == v) || (v.length() <= 0))
			throw new IllegalArgumentException("Null/empty " + a + " option not allowed");
		if ((curVal != null) && (curVal.length() > 0))
			throw new IllegalStateException(a + " option argument re-specified (old=" + curVal + "/new=" + v + ")");

		return v;
	}
	/**
	 * Extracts the <U>next</U> argument from the provided arguments 
	 * @param opt Current parsed option name
	 * @param procArgs Collected {@link Map} of parsed options so far - key=
	 * option name, value=option value. If successful, the map is updated to
	 * contain the extracted value for the parsed option
	 * @param oIndex Index of the option in the <code>args</code> array
	 * @param args Available arguments
	 * @return The index of the option <U>value</U> used
	 * @throws IllegalArgumentException if no option value or option re-specified 
	 */
	public static final int addExtraArgument (
			final String opt, final Map<String,String>	procArgs, final int oIndex, final String ... args)
		throws IllegalArgumentException
	{
		final int	aIndex=(oIndex + 1), numArgs=(args == null) ? 0 : args.length;
		if (aIndex >= numArgs)
			throw new IllegalArgumentException("Missing option " + opt + " argument");

		final String	argVal=args[aIndex];
		if ((argVal == null) || (argVal.length() <= 0))
			throw new IllegalArgumentException("No value for option " + opt + " argument");

		final String	prev=procArgs.put(opt, argVal);
		if (prev != null)
			throw new IllegalArgumentException("Option " + opt + " re-specified");

		return aIndex;
	}
	/**
	 * Collects all the values up to first non-value or no more arguments 
	 * @param opt Current parsed option name
	 * @param procArgs Collected {@link Map} of parsed options so far - key=
	 * option name, value=option value. If successful, the map is updated to
	 * contain the extracted value for the parsed option
	 * @param oIndex Index of the option in the <code>args</code> array
	 * @param optSep The <U>1st</U> character used to detect if an option was encountered 
	 * @param valSep The separator to use to append the collected values - if '\0'
	 * then values are appended with no separation
	 * @param args Available arguments
	 * @return The index of the <U>last</U> option <U>value</U> used
	 * @throws IllegalArgumentException if no option value or option re-specified 
	 */
	public static final int collectExtraArguments (
			final String opt, final Map<String,String>	procArgs, final int oIndex,
			final char optSep, final char valSep, final String ... args)
	{
		final int			numArgs=(args == null) ? 0 : args.length;
		int					aIndex=oIndex + 1;
		final StringBuilder	sb=new StringBuilder(Math.max(numArgs - aIndex, 1) * 32);
		for ( ; aIndex < numArgs; aIndex++)
		{
			final String	argVal=args[aIndex];
			if ((argVal == null) || (argVal.length() <= 0))
				throw new IllegalArgumentException("No value for option " + opt + " argument");

			if (argVal.charAt(0) == optSep)
				break;

			if ((valSep != '\0') && (sb.length() > 0))
				sb.append(valSep);
			sb.append(argVal);
		}

		if (sb.length() <= 0)
			throw new IllegalArgumentException("Missing option " + opt + " argument(s)");

		final String	prev=procArgs.put(opt, sb.toString());
		if (prev != null)
			throw new IllegalArgumentException("Option " + opt + " re-specified");

		return aIndex - 1;	// index of last value
	}

	private final String[] _args;
	public final String[] getMainArguments ()
	{
		return _args;
	}
	/**
	 * Default property name suffix for building the debug mode full name 
	 */
	public static final String	DEBUG_MODE_BASE_NAME="debug.mode";
	public String getDebugModePropertyName ()
	{
		return PropertiesResolver.getClassPropertyName(getClass(), DEBUG_MODE_BASE_NAME);
	}

	private Boolean	_debugMode	/* null */;
	public synchronized boolean isDebugMode ()
	{
		if (null == _debugMode)
		{
			final String	propName=getDebugModePropertyName(),
							propVal=System.getProperty(propName);
			_debugMode =
				((propVal != null) && (propVal.length() > 0)) ? Boolean.valueOf(propVal) : Boolean.FALSE;
		}

		return (null == _debugMode) ? false : _debugMode.booleanValue();
	}

	public void setDebugMode (boolean modeOn)
	{
		_debugMode = Boolean.valueOf(modeOn);
	}

	protected BaseMain (final String ... args)
	{
		_args = args;
	}
	/**
	 * Called by default {@link #run()} implementation
	 * @return The {@link JFrame} instance to be used as the main UI - may
	 * NOT be null
	 * @throws Exception if cannot create the frame
	 */
	protected abstract JFrame createMainFrameInstance () throws Exception;

	private static JFrame	_mainFrame;
	public static final JFrame getMainFrameInstance ()
	{
		return _mainFrame;
	}
	/*
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run ()
	{
		/* To change dynamically the look and feel AFTER initialization:
		 *
		 * 		UIManager.setLookAndFeel(lnfName);
		 *		SwingUtilities.updateComponentTreeUI(mainFrame);
		 * 		mainFrame.pack();
		 */
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Throwable t)
		{
			getLogger().error(t.getClass().getName() + " on set look and feel(): " + t.getMessage(), t);
		}

		try
		{
			if (null == (_mainFrame=createMainFrameInstance()))
				throw new IllegalStateException("No main frame instance created");

			_mainFrame.pack();
			_mainFrame.setVisible(true);
		}
		catch(Throwable t)
		{
			getLogger().error(t.getClass().getName() + " on run(): " + t.getMessage(), t);
			BaseOptionPane.showMessageDialog(null, t);
			t.printStackTrace(System.err);
			System.exit(-2);
		}
	}
}

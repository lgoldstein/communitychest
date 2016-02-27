/*
 * 
 */
package net.community.chest.io.jsch;

import java.io.PrintStream;

import net.community.chest.util.logging.LogLevelWrapper;

import com.jcraft.jsch.Logger;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 24, 2009 9:11:34 AM
 */
public class JschTestLogger implements Logger {
	private PrintStream	_out;
	public PrintStream getPrintStream ()
	{
		return _out;
	}

	public void setPrintStream (PrintStream out)
	{
		_out = out;
	}

	public JschTestLogger (final PrintStream out)
	{
		_out = out;
	}
	
	public JschTestLogger ()
	{
		this(null);
	}
	/*
	 * @see com.jcraft.jsch.Logger#isEnabled(int)
	 */
	@Override
	public boolean isEnabled (int level)
	{
		return (level >= 0) && (getPrintStream() != null);
	}
	/*
	 * @see com.jcraft.jsch.Logger#log(int, java.lang.String)
	 */
	@Override
	public void log (int level, String message)
	{
		if ((null == message) || (message.length() <= 0))
			return;

		final LogLevelWrapper	ll=JschLogger.toLogLevelWrapper(level);
		if (null == ll)
			return;

		final PrintStream	out=getPrintStream();
		if (null == out)
			return;

		out.append(ll.toString())
		   .append(' ')
		   .println(message)
		   ;
	}
}

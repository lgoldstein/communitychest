/*
 * 
 */
package net.community.chest.util.logging;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.Channel;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.io.EOLStyle;
import net.community.chest.io.output.NullOutputStream;
import net.community.chest.reflect.MethodUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to &quot;intercept&quot; printed data and log it</P>
 * @author Lyor G.
 * @since Sep 1, 2008 1:29:18 PM
 */
public abstract class AbstractLoggingPrintStream extends PrintStream implements Channel {
	private final Map<String,StackTraceElement>	_hierarchyMap;
	protected AbstractLoggingPrintStream ()
	{
		super(new NullOutputStream());

		// build a map of the hierarchy so we can ignore it when resolving the call sequence
		final StackTraceElement[]	sa=new Exception().getStackTrace();
		if ((null == sa) || (sa.length <= 1))
			throw new IllegalStateException("No stack trace hierarchy");

		final int	cIndex=MethodUtil.getFirstConstructorElement(sa);
		if (cIndex < 0)
			throw new IllegalStateException("Cannot determine class hierarchy");

		_hierarchyMap = new TreeMap<String,StackTraceElement>();

		for (int	sIndex=cIndex; sIndex >= 0; sIndex--)
		{
			final StackTraceElement	elem=sa[sIndex];
			final String			clsName=(null == elem) ? null : elem.getClassName();
			if ((null == clsName) || (clsName.length() <= 0))
				continue;

			_hierarchyMap.put(clsName, elem);
		}
	}
	/*
	 * @see java.io.PrintStream#append(char)
	 */
	@Override
	public PrintStream append (char c)
	{
		print(c);
		return this;
	}
	/*
	 * @see java.io.PrintStream#append(java.lang.CharSequence)
	 */
	@Override
	public PrintStream append (CharSequence csq)
	{
		print(csq.toString());
		return this;
	}
	/*
	 * @see java.io.PrintStream#append(java.lang.CharSequence, int, int)
	 */
	@Override
	public PrintStream append (CharSequence csq, int start, int end)
	{
		return append(csq.subSequence(start, end));
	}

	private boolean	_open	/* =false */;
	/*
	 * @see java.nio.channels.Channel#isOpen()
	 */
	@Override
	public boolean isOpen ()
	{
		return _open;
	}

	public void setOpen (boolean o)
	{
		_open = o;
	}
	/*
	 * @see java.io.PrintStream#close()
	 */
	@Override
	public void close ()
	{
		if (isOpen())
			setOpen(false);
		super.close();
	}
	/**
	 * TRUE=Attach the {@link StackTraceElement} of the caller 
	 */
	private boolean	_resolveCaller=true;
	public boolean isResolveCaller ()
	{
		return _resolveCaller;
	}

	public void setResolveCaller (boolean resolveCaller)
	{
		_resolveCaller = resolveCaller;
	}

	protected StackTraceElement	resolveCallerElement ()
	{
		final StackTraceElement[]	sa=new Exception().getStackTrace();
		if ((null == sa) || (sa.length <= 1))
			return null;	// debug breakpoint

		for (final StackTraceElement elem : sa)
		{
			final String	eClass=(null == elem) ? null : elem.getClassName();
			if ((null == eClass) || (eClass.length() <= 0))
				continue;

			// stop at first method that is NOT part of the hierarchy
			if (_hierarchyMap.get(eClass) != null)
				continue;

			return elem;
		}

		return null;
	}
	/**
	 * The default {@link LogLevelWrapper} used for logging the data 
	 */
	private LogLevelWrapper	_defLevel=LogLevelWrapper.DEBUG;
	public LogLevelWrapper getDefaultLogLevel ()
	{
		return _defLevel;
	}

	public void setDefaultLogLevel (LogLevelWrapper l)
	{
		_defLevel = l;
	}
	/**
	 * Called by default implementation of {@link #print(String)} to execute
	 * the actual logging
	 * @param l The {@link LogLevelWrapper} value
	 * @param ce The location (if {@link #isResolveCaller()}=TRUE) and
	 * successfully resolved the caller
	 * @param s The written data {@link String} &quot;line&quot;
	 * @throws IOException if failed to write the data
	 */
	public abstract void log (LogLevelWrapper l, StackTraceElement ce, String s) throws IOException;

	private StringBuilder	_workBuf	/* =null */;
	protected StringBuilder getWorkBuffer (final int len)
	{
		if (null == _workBuf)
			_workBuf = new StringBuilder(Math.max(len, Byte.MAX_VALUE));

		return _workBuf;
	}

	private static StringBuilder appendCleanData (final StringBuilder sb, final String s)
	{
		final int	sLen=(null == s) ? 0 : s.length();
		for (int	curPos=0; curPos < sLen; )
		{
			final int	crPos=s.indexOf('\r', curPos);
			if (crPos < curPos)
			{
				final String	remString=(curPos > 0) ? s.substring(curPos) : s;
				if ((remString != null) && (remString.length() > 0))
					sb.append(remString);
				break;
			}

			final String	clrText=(curPos == crPos) ? null : s.substring(curPos, crPos);
			if ((clrText != null) && (clrText.length() > 0))
				sb.append(clrText);

			curPos = crPos + 1;
		}

		return sb;
	}

	private static String getCleanData (final StringBuilder sb, final String s)
	{
		if ((null == s) || (s.length() <= 0) || (s.indexOf('\r') < 0))
			return s;

		final int			prevLen=sb.length();
		final StringBuilder	res=appendCleanData(sb, s);
		final String		ret=res.toString();
		res.setLength(prevLen);
		return ret;
	}
	/*
	 * @see java.io.PrintStream#print(java.lang.String)
	 */
	@Override
	public void print (final String s)
	{
		if (!isOpen())
		{
			setError();
			return;
		}

		StackTraceElement	ce=null;
		final int			sLen=(null == s) ? 0 : s.length();
		final StringBuilder	sb=getWorkBuffer(sLen);
		// break it up into LF separated lines
		for (int	curPos=0; curPos < sLen; )
		{
			final int		lfPos=s.indexOf('\n', curPos);
			// if no more LF(s) then accumulate whatever is left for next time
			if (lfPos < curPos)
			{
				final String	remString=(curPos > 0) ? s.substring(curPos) : s;
				if ((remString != null) && (remString.length() > 0))
					appendCleanData(sb, remString);
				break;
			}

			final String	clrText=(curPos == lfPos) ? null : s.substring(curPos, lfPos), msgText;
			// check if have data from previous call
			if (sb.length() > 0)
			{
				if ((clrText != null) && (clrText.length() > 0))
					appendCleanData(sb, clrText);

				msgText = sb.toString();
				sb.setLength(0);	// re-start accumulation
			}
			else
				msgText = getCleanData(sb, clrText);

			if ((msgText != null) && (msgText.length() > 0))
			{
				if ((null == ce) && isResolveCaller())
					 ce = resolveCallerElement();

				try
				{
					log(getDefaultLogLevel(), ce, msgText);
				}
				catch(IOException e)
				{
					setError();
				}
			}

			curPos = lfPos + 1;
		}
	}
	/*
	 * @see java.io.PrintStream#print(boolean)
	 */
	@Override
	public void print (boolean b)
	{
		print(String.valueOf(b));
	}
	/*
	 * @see java.io.PrintStream#print(char)
	 */
	@Override
	public void print (char c)
	{
		print(String.valueOf(c));
	}
	/*
	 * @see java.io.PrintStream#print(char[])
	 */
	@Override
	public void print (char[] s)
	{
		print(new String(s));
	}
	/*
	 * @see java.io.PrintStream#print(double)
	 */
	@Override
	public void print (double d)
	{
		print(String.valueOf(d));
	}
	/*
	 * @see java.io.PrintStream#print(float)
	 */
	@Override
	public void print (float f)
	{
		print(String.valueOf(f));
	}
	/*
	 * @see java.io.PrintStream#print(int)
	 */
	@Override
	public void print (int i)
	{
		print(String.valueOf(i));
	}
	/*
	 * @see java.io.PrintStream#print(long)
	 */
	@Override
	public void print (long l)
	{
		print(String.valueOf(l));
	}
	/*
	 * @see java.io.PrintStream#print(java.lang.Object)
	 */
	@Override
	public void print (Object obj)
	{
		print(String.valueOf(obj));
	}
	/*
	 * @see java.io.PrintStream#println()
	 */
	@Override
	public void println ()
	{
		print(EOLStyle.LOCAL.getStyleString());
	}
	/*
	 * @see java.io.PrintStream#println(boolean)
	 */
	@Override
	public void println (boolean x)
	{
		print(x);
		println();
	}
	/*
	 * @see java.io.PrintStream#println(char)
	 */
	@Override
	public void println (char x)
	{
		print(x);
		println();
	}
	/*
	 * @see java.io.PrintStream#println(char[])
	 */
	@Override
	public void println (char[] x)
	{
		print(x);
		println();
	}
	/*
	 * @see java.io.PrintStream#println(double)
	 */
	@Override
	public void println (double x)
	{
		print(x);
		println();
	}
	/*
	 * @see java.io.PrintStream#println(float)
	 */
	@Override
	public void println (float x)
	{
		print(x);
		println();
	}
	/*
	 * @see java.io.PrintStream#println(int)
	 */
	@Override
	public void println (int x)
	{
		print(x);
		println();
	}
	/*
	 * @see java.io.PrintStream#println(long)
	 */
	@Override
	public void println (long x)
	{
		print(x);
		println();
	}
	/*
	 * @see java.io.PrintStream#println(java.lang.Object)
	 */
	@Override
	public void println (Object x)
	{
		print(x);
		println();
	}
	/*
	 * @see java.io.PrintStream#println(java.lang.String)
	 */
	@Override
	public void println (String x)
	{
		print(x);
		println();
	}
	/*
	 * @see java.io.PrintStream#write(byte[], int, int)
	 */
	@Override
	public void write (byte[] buf, int off, int len)
	{
		if (len > 0)
		{
			final char[]	ca=new char[len];
			for (int	cIndex=0, cOffset=off; cIndex < len; cOffset++, cIndex++)
				ca[cIndex] = (char) (buf[cOffset] & 0x00FF);
			print(ca);
		}
	}
	/*
	 * @see java.io.PrintStream#write(int)
	 */
	@Override
	public void write (int b)
	{
		print((char) (b & 0x00FF));
	}
}

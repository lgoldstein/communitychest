package net.community.chest.net.proto.text.pop3;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.io.Writer;

import net.community.chest.io.EOLStyle;
import net.community.chest.lang.StringUtil;
import net.community.chest.lang.math.NumberTables;
import net.community.chest.mail.message.EOMHunterWriter;
import net.community.chest.net.LineInfo;
import net.community.chest.net.TextNetConnection;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>POP3 accessor further helper class - provides a working buffer</P>
 * @author Lyor G.
 * @since Sep 19, 2007 11:40:28 AM
 */
public abstract class AbstractPOP3AccessorHelper extends AbstractPOP3Accessor {
	protected AbstractPOP3AccessorHelper ()
	{
		super();
	}
	/**
	 * Buffer used for parsing responses - "lazily" allocated
	 */
	private char[]	_workBuf /* =null */;
	// @see #_workBuf
	private char[] getWorkBuf ()
	{
		if (null == _workBuf)
			_workBuf = new char[POP3Protocol.MAX_POP3_LINE_LENGTH + EOLStyle.CRLF.length()];
		return _workBuf;
	}
	/**
	 * Line information structure used to parse read lines - "lazily" allocated
	 */
	private LineInfo	_lineInfo	/* =null */;
	private LineInfo getLineInfo ()
	{
		if (null == _lineInfo)
			_lineInfo = new LineInfo();
		else
			_lineInfo.reset();

		return _lineInfo;
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.AbstractPOP3Accessor#exhaustMessageData(int, java.io.Writer)
	 */
	@Override
	protected int exhaustMessageData (int msgNum, Writer out) throws IOException
	{
		if ((msgNum <= 0) || (null == out))
			return Integer.MIN_VALUE;

		final char[]	wb=getWorkBuf();
		if ((null == wb) || (wb.length <= 1))
			return Integer.MAX_VALUE;	// should not happen

		final TextNetConnection	conn=getTextNetConnection();
		if (null == conn)
			throw new IOException(ClassUtil.getArgumentsExceptionLocation(getClass(), "exhaustMessageData", Integer.valueOf(msgNum)) + " no " + TextNetConnection.class.getName() + "instance to read from");

		// we limit ourselves to ~2GB buffers at most to avoid infinite loops
		try(EOMHunterWriter	eow=new EOMHunterWriter(out, false /* no EOM echo */, false /* no real close */)) {
			for (int	readIndex=0; readIndex < (Integer.MAX_VALUE - Byte.MAX_VALUE) && (!eow.isEOMDetected()); readIndex++)
			{
				final int	readLen=conn.read(wb, 0, wb.length - EOLStyle.CRLF.length());
				if (readLen < 0)
					throw new IOException("Error (" + readLen + ") while reading message buffer #" + readIndex);
	
				if (readLen > 0)
				{
					wb[readLen] = '\0';	// for debug purposes
					eow.write(wb, 0, readLen);
				}
			}
	
			if (!eow.isEOMDetected())
				throw new IOException("Virtual infinite message data read loop exit");
		}

		return 0;
	}
	/*
	 * @see net.community.chest.net.proto.text.AbstractTextProtocolNetConnectionHelper#readLine()
	 */
	@Override
	public String readLine () throws IOException
	{
		final TextNetConnection	conn=getTextNetConnection();
		if (null == conn)
			throw new IOException(ClassUtil.getExceptionLocation(getClass(), "readLine") + " no " + TextNetConnection.class.getName() + "instance to read from");

		final LineInfo	li=getLineInfo();
		final char[]	wb=getWorkBuf();
		final int		readLen=conn.readLine(wb, 0, wb.length, li);
		if (readLen < 0)
			throw new IOException("Error (" + readLen + ") while attempting to read line");
		if (!li.isLFDetected())	// make sure can accomodate entire line in supplied buffer
			throw new IOException("Incomplete line read");

		return new String(wb, 0, li.getLength());
	}

	private POP3Response	_lastRsp	/* =null */;
	/**
	 * @return cached object to avoid creating new ones every time
	 */
	protected POP3Response getLastResponse ()
	{
		if (null == _lastRsp)
			_lastRsp = new POP3Response();
		else
			_lastRsp.reset();

		return _lastRsp;
	}
	/**
	 * Sends a FULL command line to the POP3 connection and retrieves the response line
	 * @param cmdLine FULL command line to be sent (including CRLF)
	 * @param offset offset in command line of first character to be sent (inclusive)
	 * @param len number of characters to send (including CRLF)
	 * @return POP3 response
	 * @throws IOException if errors
	 */
	protected POP3Response sendCommand (char[] cmdLine, int offset, int len) throws IOException
	{
		final TextNetConnection	conn=getTextNetConnection();
		if (null == conn)
			throw new IOException(ClassUtil.getArgumentsExceptionLocation(getClass(), "sendCommand", new String(cmdLine, offset, len)) + " no " + TextNetConnection.class.getName() + "instance to read from");

		final int	written=conn.write(cmdLine, offset, len, true);
		if (written != len)
			throw new IOException("Command write mismatch (" + written + " <> " + len + ")");

		final String		rspLine=readLine();
		final POP3Response	rsp=POP3Response.getResponse(rspLine, getLastResponse());
		if (null == rsp)
			throw new IOException("Not a valid POP3 response: " + rspLine);

		return rsp;
	}
	/**
	 * Builds a command in the given string buffer + terminating CRLF
	 * @param sb string buffer to append command into
	 * @param cmd command to be appended
	 * @param arg argument - if null/empty then not appended to command
	 * @return same as input string buffer
	 * @throws IOException if fail to append
	 */
	private static final StringBuilder buildCommand (final StringBuilder sb, final char[] cmd, final char[] arg) throws IOException
	{
		if ((null == sb) || (null == cmd) || (cmd.length <= 0))
			throw new StreamCorruptedException("No command/buffer");
		sb.append(cmd);

		if ((arg != null) && (arg.length > 0))
			sb.append(' ')
			  .append(arg)
			  ;

		sb.append(EOLStyle.CRLF.getStyleChars());
		return sb;
	}

	private StringBuilder	_cmdBuf;
	protected StringBuilder getCommandBuffer (final int initialSize)
	{
		if (null == _cmdBuf)
			_cmdBuf = new StringBuilder(Math.max(initialSize,Byte.MAX_VALUE));
		else
			_cmdBuf.setLength(0);
		return _cmdBuf;
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.AbstractPOP3Accessor#sendCommand(char[], char[])
	 */
	@Override
	protected POP3Response sendCommand (char[] cmd, char[] arg) throws IOException
	{
		final int			argLen=(null == arg) ? 0 : arg.length;
		final StringBuilder	sb=getCommandBuffer(cmd.length + Math.max(0, argLen) + 1 + EOLStyle.CRLF.length());
		buildCommand(sb, cmd, arg);

		return sendCommand(StringUtil.getBackingArray(sb), 0, sb.length());
	}
	/**
	 * Builds a command that refers to a message
	 * @param sb string buffer to append command into
	 * @param cmd command to be used
	 * @param msgNum message number
	 * @param msgArg extra argument - if <0 then not appended
	 * @return same as input string buffer
	 * @throws IOException if fail to append
	 */
	private static final StringBuilder buildMsgCommand (final StringBuilder sb, final char[] cmd, final int msgNum, final int msgArg) throws IOException
	{
		if ((null == sb) || (null == cmd) || (cmd.length <= 0))
			throw new StreamCorruptedException("No command/buffer");

		sb.append(cmd)
		  .append(' ')
		  .append(msgNum)
		  ;

		if (msgArg >= 0L)
			sb.append(' ')
			  .append(msgArg)
			  ;

		sb.append(EOLStyle.CRLF.getStyleChars());
		return sb;
	}
	/*
	 * @see net.community.chest.net.proto.text.pop3.AbstractPOP3Accessor#sendMsgCommand(char[], int, int)
	 */
	@Override
	protected POP3Response sendMsgCommand (char[] cmd, int msgNum, int msgArg) throws IOException
	{
		final StringBuilder	sb=getCommandBuffer(((null == cmd) ? 0 : Math.max(cmd.length,1)) + 1 + NumberTables.MAX_UNSIGNED_INT_DIGITS.length + 1 + EOLStyle.CRLF.length());
		buildMsgCommand(sb, cmd, msgNum, msgArg);

		return sendCommand(StringUtil.getBackingArray(sb), 0, sb.length());
	}
}

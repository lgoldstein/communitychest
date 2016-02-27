package net.community.chest.net.proto.text.smtp;

import java.io.IOException;
import java.io.StreamCorruptedException;

import net.community.chest.net.BufferedTextSocket;
import net.community.chest.net.TextNetConnection;
import net.community.chest.net.proto.text.NetServerWelcomeLine;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 8:27:20 AM
 */
public class SMTPSession extends AbstractSMTPAccessorHelper {
	public SMTPSession ()
	{
		super();
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolNetConnection#connect(java.lang.String, int, net.community.chest.net.proto.text.NetServerWelcomeLine)
	 */
	@Override
	public void connect (String host, int port, NetServerWelcomeLine wl) throws IOException
	{
		TextNetConnection	conn=getTextNetConnection();
		if (conn != null)
			throw new StreamCorruptedException("Already connected");

		setTextNetConnection(new BufferedTextSocket());
		super.connect(host, port);

		if (null == (conn=getTextNetConnection()))
			throw new StreamCorruptedException(ClassUtil.getArgumentsExceptionLocation(getClass(), "connect", host, Integer.valueOf(port)) + " no " + TextNetConnection.class.getName() + " instance though created");

		try
		{
			// first line is assumed to be the welcome
			final String	wlString=conn.readLine();
			if (wl != null)
				wl.setLine(wlString);

			String	rspLine=wlString;
			try
			{
				// (rarely happens) exhaust additional welcome response lines till get final one
				// we limit ourselves to ~127 additional response lines
				int	wlRspCode=SMTPResponse.getResponseCode(wlString);
				for (int rspIndex=0; rspIndex < Byte.MAX_VALUE; rspIndex++)
				{
					if (wlRspCode >= 0)
						break;

					rspLine = conn.readLine();
					wlRspCode = SMTPResponse.getResponseCode(rspLine);
				}

				// make sure found valid response code (and not "infinite" loop exit)
				if (wlRspCode < 0)
					throw new IOException("Virtual infinite loop exit on welcome line exhaustion");
			}
			catch(IllegalStateException ise)	// should not happen (result of trying to decode response line code)
			{
				throw new IOException("Failed to parse welcome line(s): " + rspLine);
			}
		}
		catch(IOException ioe)
		{
			conn.close();
			throw ioe;
		}
	}
	/*
	 * @see net.community.chest.net.proto.text.smtp.AbstractSMTPAccessorHelper#getFinalResponse(net.community.chest.net.proto.text.smtp.SMTPResponse)
	 */
	@Override
	protected SMTPResponse getFinalResponse (SMTPResponse rsp) throws IOException
	{
		return getFinalResponse(getTextNetConnection(), rsp);
	}
	/*
	 * @see net.community.chest.net.proto.text.smtp.AbstractSMTPAccessor#flushWrite()
	 */
	@Override
	protected void flushWrite () throws IOException
	{
		final TextNetConnection	conn=getTextNetConnection();
		if ((null == conn) || (!conn.isOpen()))
			throw new IOException("No current SMTP connection to flush");

		conn.flush();
	}
	/*
	 * @see net.community.chest.net.proto.text.smtp.AbstractSMTPAccessor#reportEhloCapabilities(net.community.chest.net.proto.text.smtp.ESMTPCapabilityHandler)
	 */
	@Override
	protected SMTPResponse reportEhloCapabilities (ESMTPCapabilityHandler reporter) throws IOException
	{
		if (null == reporter)	// should not happen, but be lenient
			return getFinalResponse();

		final TextNetConnection	conn=getTextNetConnection();
		if ((null == conn) || (!conn.isOpen()))	// should not happen
			throw new IOException("No connection to read EHLO responses from");

		// we limit ourselves to ~32K responses to avoid infinite loops
		final SMTPResponse	rsp=getModifiableResponse();
		for (int	rspIndex=0; rspIndex < Short.MAX_VALUE; rspIndex++)
		{
			final String		rspLine=conn.readLine();
			final SMTPResponse	r=SMTPResponse.getFinalResponse(rspLine, rsp);
			if (null == r)
				throw new IOException("Bad/illegal EHLO response: " + rspLine);

			final int	nErr=r.getRspCode(), repErr=reportEhloCapabilities(nErr, rspLine, reporter);
			if (repErr != 0)
				throw new IOException("Failed to report capabilities (err=" + repErr + "): " + rspLine);

			// if not a continuation line, then stop looking for responses
			if (nErr > 0)
				return r;
		}
	
		throw new IOException("Virtual infinite EHLO loop exit");
	}
	/*
	 * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#writeBytes(byte[], int, int, boolean)
	 */
	@Override
	public int writeBytes (byte[] buf, int startPos, int maxLen, boolean flushIt) throws IOException
	{
		final TextNetConnection	conn=getTextNetConnection();
		if ((null == conn) || (!conn.isOpen()))	// should not happen
			throw new IOException("No current SMTP connection to write bytes to");
		if ((startPos < 0) || (maxLen < 0))
			throw new IOException("Bad/Illegal write bytes buf specification");
		if (0 == maxLen)
			return 0;
		if ((null == buf) || ((startPos + maxLen) > buf.length))
			throw new IOException("Bad/Illegal write bytes buf array");

		return conn.writeBytes(buf, startPos, maxLen, flushIt);
	}
	/*
	 * @see net.community.chest.net.proto.text.smtp.SMTPAccessor#writeData(char[], int, int, boolean)
	 */
	@Override
	public int writeData (char[] data, int startOffset, int len, boolean flushIt) throws IOException
	{
		final TextNetConnection	conn=getTextNetConnection();
		if ((null == conn) || (!conn.isOpen()))	// should not happen
			throw new IOException("No current SMTP connection to write to");
		if ((startOffset < 0) || (len < 0))
			throw new IOException("Bad/Illegal write buf specification");
		if (0 == len)
			return 0;
		if ((null == data) || ((startOffset + len) > data.length))
			throw new IOException("Bad/Illegal write buf array");

		return conn.write(data, startOffset, len, flushIt);
	}
}

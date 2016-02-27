package net.community.chest.net.proto.text.pop3;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.net.proto.text.TextProtocolResponse;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 10:31:32 AM
 */
public class POP3Response extends TextProtocolResponse {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3392736727520581404L;

	public POP3Response ()
	{
		super();
	}

	private boolean	_isOK /* =false */;

	/*
	 * @see net.community.chest.net.proto.text.TextProtocolResponse#isOKResponse()
	 */
	@Override
	public boolean isOKResponse ()
	{
		return _isOK;
	}

	public void setOKResponse (boolean isOK)
	{
		_isOK = isOK;
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolResponse#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		final Class<?>	oc=(obj == null) ? null : obj.getClass();
		if (oc != getClass())
			return false;
		if (this == obj)
			return true;

		return isSameResponse((POP3Response) obj);
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolResponse#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return isOKResponse() ? 1 : 0;
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolResponse#clone()
	 */
	@Override
	@CoVariantReturn
	public POP3Response clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
 	/**
	 * @param isOK TRUE/FALSE if this is a "+OK/-ERR"
	 * @param rspLine response line - including "+OK/-ERR". <B>Note:</B>
	 * the string is NOT checked if null/empty or does not match
	 * the reported <I>isOK</I> value (or if it is a valid POP3
	 * response line at all)
	 */
	public void setResponseAndLine (boolean isOK, String rspLine)
	{
		setOKResponse(isOK);
		setResponseLine(rspLine);
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolResponse#reset()
	 */
	@Override
	public void reset ()
	{
		setOKResponse(false);
		super.reset();
	}
	/**
	 * Initializes the response string to the default "+OK/-ERR" value(s)
	 * according to specified flag 
	 * @param isOK TRUE/FALSE if this is a "+OK/-ERR"
	 */
	public void setResponse (boolean isOK)
	{
		setResponseAndLine(isOK, isOK ? POP3Protocol.POP3_OK : POP3Protocol.POP3_ERR);
	}
	/**
	 * @param isOK TRUE/FALSE if this is a "+OK/-ERR"
	 * @param text text to <U>follow</U> the "+OK/-ERR" - may be null
	 */
	public void setResponse (boolean isOK, String text)
	{
		if ((null == text) || (text.length() <= 0))
			setResponse(isOK);
		else
			setResponseAndLine(isOK, (isOK ? POP3Protocol.POP3_OK : POP3Protocol.POP3_ERR) + " " + text);
	}
	/**
	 * @param text text to <U>follow</U> the "+OK" - may be null
	 */
	public void setOKResponse (String text)
	{
		setResponse(true, text);
	}
	/**
	 * @param text text to follow the "-ERR" - may be null
	 */
	public void setERRResponse (String text)
	{
		setResponse(false, text);
	}

	public POP3Response (boolean isOK, String rspLine)
	{
		setResponseAndLine(isOK, rspLine);
	}	

	public POP3Response (boolean isOK)
	{
		this(isOK, isOK ? POP3Protocol.POP3_OK : POP3Protocol.POP3_ERR);
	}

	public POP3Response (POP3Response rsp)
	{
		this((null == rsp) ? false : rsp.isOKResponse(), (null == rsp) ? null : rsp.getResponseLine());
	}
	/**
	 * Checks is response sequence is a POP3 one, and if so returns its "value"
	 * @param rspLine response line to be checked
	 * @return 0-if "+OK", (>0) if "-ERR", (<0) if neither
	 */
	public static final int isOKResponse (final CharSequence rspLine)
	{
		final int	rspLen=(null == rspLine) ? 0 : rspLine.length();
		if ((rspLen >= POP3Protocol.POP3_ERRChars.length)
		  && ParsableString.compareTo(rspLine, 0, POP3Protocol.POP3_ERRChars.length, POP3Protocol.POP3_ERRChars, true))
			return 1;
		if ((rspLen >= POP3Protocol.POP3_OKChars.length)
		  && ParsableString.compareTo(rspLine, 0, POP3Protocol.POP3_OKChars.length, POP3Protocol.POP3_OKChars, true))
			return 0;
		
		return (-1);
	}
	/**
	 * Checks if character sequence is a POP3 response line. If so, returns a
	 * matching object (or null)
	 * @param rspLine response line to be checked
	 * @param rsp response to be initialized - may NOT be null
	 * @return <I>rsp</I> input response object (null if not a valid POP3
	 * response line/object)
	 */
	public static final POP3Response getResponse (final CharSequence rspLine, final POP3Response rsp)
	{
		final int	errCode=isOKResponse(rspLine);
		if ((null == rsp) || (errCode < 0))
			return null;

		rsp.setResponseAndLine((0 == errCode), rspLine.toString());
		return rsp;
	}
	/**
	 * Checks if character sequence is a POP3 response line. If so, returns a
	 * matching object (or null)
	 * @param rspLine response line to be checked
	 * @return response object (null if not a valid POP3 response line)
	 */
	public static final POP3Response getResponse (final CharSequence rspLine)
	{
		return getResponse(rspLine, new POP3Response());
	}
}

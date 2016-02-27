package net.community.chest.net.proto.text.smtp;

import net.community.chest.CoVariantReturn;
import net.community.chest.net.proto.text.TextProtocolResponse;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 3:43:22 PM
 */
public class SMTPResponse extends TextProtocolResponse {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4731222900548772370L;
	/**
	 * Response code value - non-positive if not initialized
	 */
	private int _rspCode	/* =0 */;
	public int getRspCode ()
	{
		return _rspCode;
	}

	public void setRspCode (int rspCode)
	{
		_rspCode = rspCode;
	}
	/**
	 * Default constructor
	 */
	public SMTPResponse ()
	{
		super();
	}
	/**
	 * Overrides the response code and response line string
	 * @param rspCode response code value
	 * @param rspLine response line (may be null/empty).
	 * Caveat emptor: the <I>toString</I> override returns
	 * the set response line
	 */
	public void setResponseAndLine (int rspCode, String rspLine)
	{
		setRspCode(rspCode);
		setResponseLine(rspLine);
	}
	/**
	 * Sets the current response value
	 * @param rspCode response code to be used
	 * @param rspText response text (without the code) to be appended.
	 * If be null/empty, then response line contains only the string
	 * value of the response code
	 * @see #_rspLine
	 */
	public void setResponse (int rspCode, String rspText)
	{
		if ((rspText != null) && (rspText.length() > 0))
			setResponseAndLine(rspCode, String.valueOf(rspCode) + " " + rspText);
		else
			setResponseAndLine(rspCode, String.valueOf(rspCode));
	}
	/**
	 * Sets the current response without any associated text 
	 * @param rspCode response code to be used.
	 * @see #setResponse(int, String)
	 */
	public void setResponse (int rspCode)
	{
		setResponse(rspCode, null);
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolResponse#reset()
	 */
	@Override
	public void reset ()
	{
		super.reset();
		setRspCode(0);
	}
	/**
	 * Pre-initialized constructor
	 * @param rspCode response code to be used
	 * @param rspText response text (without the code) to be appended.
	 * If be null/empty, then response line contains only the string
	 * value of the response code
	 */
	public SMTPResponse (int rspCode, String rspText)
	{
		setResponse(rspCode, rspText);
	}
	/**
	 * Pre-initialized constructor
	 * @param rspCode response code to be used
	 */
	public SMTPResponse (int rspCode)
	{
		this(rspCode, null);
	}
	/**
	 * @param rspCode response code to be checked
	 * @return TRUE if in [200-299] range (inclusive)
	 */
	public static boolean isOKResponseCode (final int rspCode)
	{
		return (200 <= rspCode) && (rspCode < 300);
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolResponse#isOKResponse()
	 */
	@Override
	public boolean isOKResponse ()
	{
		return isOKResponseCode(getRspCode());
	}

	public boolean isSameResponse (final SMTPResponse rsp)
	{
		if (rsp == null)
			return false;
		if (this == rsp)
			return true;

		final int	r1=getRspCode(), r2=rsp.getRspCode();
    	if (r1 == r2)
    		return true;

    	// if not same value, then declare "equals" if both response codes not initialized
    	if (r1 <= 0)
    		return (r2 <= 0);

    	return false;
	}
    /* @return TRUE if same response code set
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals (Object obj)
    {
		final Class<?>	oc=(obj == null) ? null : obj.getClass();
		if (oc != getClass())
			return false;

		return isSameResponse((SMTPResponse) obj);

    }
    /* @return response code
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode ()
    {
        return Math.max(getRspCode(),0);
    }
    /*
	 * @see net.community.chest.net.proto.text.TextProtocolResponse#clone()
	 */
	@Override
	@CoVariantReturn
	public SMTPResponse clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}

	/**
	 * @param lval response line from the SMTP server to be checked
	 * @return code in response line (<0 => continuation line - the positive
	 * value is the original response code)
	 * @throws IllegalStateException if parsing errors encountered
	 */
	public static final int getResponseCode (final CharSequence lval) throws IllegalStateException
	{
		final int   nLen=(null == lval) ? 0 : lval.length();
		if (nLen <= 2)	// must have at least 3 digits of response code
			throw new IllegalStateException("null/empty response line (len=" + nLen + ")");

		for (int	nRsp=0; nRsp < nLen; )
		{
			final char	ch=lval.charAt(nRsp);
			if ((ch >= '0') && (ch <= '9'))
			{
				nRsp++;

				/* 		Handle special case where response line is entirely made of
				 * the response code - in this case, we will reach "nLen" with all digits
				 * text.
				 */
				if (nRsp < nLen)
					continue;
			}

			// at this stage "nRsp" is the number of digits in the response
			final CharSequence	numSeq=lval.subSequence(0, nRsp);
			final String  		numVal=numSeq.toString();
			// Note: no need to catch NumberFromatException since we reached this place for digits only
			final int     		nErr=Integer.parseInt(numVal);
			if ((nErr < 100) || (nErr >= 1000))	// should not happen (all response codes are 3 digits)
				throw new IllegalStateException("unexpected zero/negative value in response=" + lval);

			// check if this is a continuation - if so, then return a negative value
			if ((nRsp < nLen) && ('-' == ch))
				return (0 - nErr);
			else
				return nErr;
		}

		throw new IllegalStateException("unexpected response loop exit");
	}
	/**
	 * Analyzes the input and extracts the response information
	 * @param lval input to be checked
	 * @param rsp (modifiable) response object to be set with parsing results.
	 * If null then a null return value is returned
	 * @return response information - same as input <I>rsp</I> (null if not a
	 * valid response). Note: if it is a continuation line, the response will
	 * be extracted anyway, but the returned error code will be <I>negative</I>.
	 * @see #getResponseCode(CharSequence lval)
	 */
	public static final SMTPResponse getFinalResponse (final CharSequence lval, final SMTPResponse rsp)
	{
		if (null == rsp)
			return null;

		try
		{
			rsp.setResponseAndLine(getResponseCode(lval), lval.toString());
			return rsp;
		}
		catch(IllegalStateException ise)
		{
			return null;
		}
	}
	/**
	 * Analyzes the input and extracts the response information
	 * @param lval input to be checked
	 * @return response information (null if not a valid response). Note: if it
	 * is a continuation line, the response will be extracted anyway, but the
	 * returned error code will be <I>negative</I>.
	 * @see #getResponseCode(CharSequence lval)
	 */
	public static final SMTPResponse getFinalResponse (final CharSequence lval)
	{
		return getFinalResponse(lval, new SMTPResponse());
	}
}

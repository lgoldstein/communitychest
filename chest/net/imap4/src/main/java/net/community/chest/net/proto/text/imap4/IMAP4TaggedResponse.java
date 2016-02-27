package net.community.chest.net.proto.text.imap4;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.lang.StringUtil;
import net.community.chest.net.proto.text.TextProtocolResponse;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 9:35:53 AM
 */
public class IMAP4TaggedResponse extends TextProtocolResponse {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2896716401520837870L;
	/* IMAP4 tagged responses strings */
	public static final String IMAP4_OK="OK";
		public static final char[] IMAP4_OKChars=IMAP4_OK.toCharArray();
	public static final String IMAP4_NO="NO";
		public static final char[] IMAP4_NOChars=IMAP4_NO.toCharArray();
	public static final String IMAP4_BAD="BAD";
		public static final char[] IMAP4_BADChars=IMAP4_BAD.toCharArray();
    /* special codes used to denote the matching responses */
	public static final int    EOK=0, ENO=0x00002090, EBAD=0x00002005;
	/**
	 * @param errCode error code to be translated
	 * @return the string associated with the error code (null if no match)
	 */
	public static final String xlateErrCode (int errCode)
	{
		switch(errCode)
		{
			case EOK    : return IMAP4_OK;
			case ENO    : return IMAP4_NO;
			case EBAD   : return IMAP4_BAD;
			default     : // do nothing
		}

		return null;
	}
	/**
	 * @param rspCode response code to be translated
	 * @return special code used to denote this response (or <0 if error)
	 */
	public static final int xlateRspCode (String rspCode)
	{
		if ((null == rspCode) || (rspCode.length() <= 0))
			return (-1);

		if (IMAP4_OK.equals(rspCode))
			return EOK;
		else if (IMAP4_NO.equals(rspCode))
			return ENO;
		else if (IMAP4_BAD.equals(rspCode))
			return EBAD;
		else
			return (-2);
	}
	/**
	 * @param ps response line to be checked
	 * @param rspStart index in parsed line where response is to be checked start (inclusive)
	 * @param rspEnd index in parsed line where response is to be checked ends (exclusive)
	 * @return special code used to denote this response (or <0 if error)
	 */
	public static final int xlateRspCode (ParsableString ps, int rspStart, int rspEnd)
	{
		if ((null == ps) || (rspStart < 0) || (rspEnd <= rspStart))
			return (-1);

		if (ps.compareTo(rspStart, rspEnd, IMAP4_OKChars, true))
			return EOK;
		else if (ps.compareTo(rspStart, rspEnd, IMAP4_NOChars, true))
			return ENO;
		else if (ps.compareTo(rspStart, rspEnd, IMAP4_BADChars, true))
			return EBAD;
		else
			return (-2);
	}
	/**
	 * Check if supplied char array represents one of the known responses
	 * @param rspCode array to be checked
	 * @param fromIndex index to start checking from (inclusive)
	 * @param toIndex index to stop checking (exclusive)
	 * @return special code used to denote this response (or <0 if error)
	 */
	public static final int xlateRspCode (char[] rspCode, int fromIndex, int toIndex)
	{
		if ((null == rspCode) || (fromIndex < 0) || (toIndex > rspCode.length) || (toIndex <= fromIndex))
			return (-1);
		else
			return xlateRspCode(new ParsableString(rspCode), fromIndex, toIndex);
	}
	/**
	 * Check if supplied char array represents one of the known responses
	 * @param rspCode array to be checked
	 * @param rspLen number of characters in array (starting at index 0)
	 * @return special code used to denote this response (or <0 if error)
	 */
	public static final int xlateRspCode (char[] rspCode, int rspLen)
	{
		return xlateRspCode(rspCode, 0, rspLen);
	}
	/**
	 * Check if supplied char array represents one of the known responses
	 * @param rspCode character array to be checked
	 * @return special code used to denote this response (or <0 if error)
	 */
	public static final int xlateRspCode (char[] rspCode)
	{
		return xlateRspCode(rspCode, 0, (null == rspCode) ? 0 : rspCode.length);
	}
	/**
	 * Translates a tagged response predicate (NO/BAD/OK) to an error code 
	 * @param s response code
	 * @param rspStart position to start checking the predicate (inclusive)
	 * @param rspEnd position to end checking the predicate (exclusive)
	 * @return error code translation (<0 if not recognized the predicate)
	 */
	public static final int xlateRspCode (CharSequence s, int rspStart, int rspEnd)
	{
		final int sLen=(null == s) ? 0 : s.length();
		if ((sLen <= 0) || (rspStart >= sLen) || (rspEnd > sLen) || (rspStart < 0) || (rspEnd <= rspStart))
			return (-1);

		if (ParsableString.compareTo(s, rspStart, rspEnd, IMAP4_OKChars, true))
			return EOK;
		else if (ParsableString.compareTo(s, rspStart, rspEnd, IMAP4_NOChars, true))
			return ENO;
		else if (ParsableString.compareTo(s, rspStart, rspEnd, IMAP4_BADChars, true))
			return EBAD;
		else
			return (-2);
	}

    public IMAP4TaggedResponse ()
	{
		super();
	}
	/**
	 * Special code used to denote the matching response
	 */
	private int _errCode=(-1);
	public int getErrCode ()
	{
		return _errCode;
	}

	public void setErrCode (int errCode)
	{
		_errCode = errCode;
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolResponse#isOKResponse()
	 */
	@Override
	public boolean isOKResponse ()
	{
		return (EOK == getErrCode());
	}
	/**
	 * @return true if tagged response was NO
	 */
	public boolean isNOResponse ()
	{
		return (ENO == getErrCode());
	}
	/**
	 * @return true if tagged response was BAD
	 */
	public boolean isBADResponse ()
	{
		return (EBAD == getErrCode());
	}
	/**
	 * @return true if this a valid response object
	 */
	public boolean isResponse ()
	{
		return (getErrCode() >= 0);
	}
	/**
	 * Tag value used in the response - if <=0 then not set
	 */
	private int	_tagValue=(-1);
	public int getTagValue ()
	{
	    return _tagValue;
	}

	public void setTagValue (int tagValue)
	{
		_tagValue = tagValue;
	}
	/**
	 * Tag string used in response - if null/empty then not set.
	 * <B>Caveat:</B> the {@link #getTagString()} and {@link #getTagValue()}
	 * attributes are <U>independent</U> - please be <U><B>consistent</B></U>
	 * and use only one or the other (e.g., the default IMAP4 accessor
	 * uses only the integer value)
	 */
	private String	_tagString	/* =null */;
    public String getTagString ()
	{
		return _tagString;
	}

    public void setTagString (String tagString)
	{
		_tagString = tagString;
	}
	/**
	 * @param tagString tag value - may NOT be null/empty
	 * @param nErr error code - MUST be one of the known codes: EOK, ENO, EBAD
	 * @param text text to be displayed - may be null/empty (without
	 * the tag and/or error code)
	 * @return TRUE if OK arguments
	 */
	public boolean setResponse (final String tagString, final int nErr, final String text)
	{
		final String	rspCode=xlateErrCode(nErr);
		if ((null == tagString) || (tagString.length() <= 0)
		 || (null == rspCode) || (rspCode.length() <= 0))
			return false;

		setTagString(tagString);

		final int	txtLen=(null == text) ? 0 : text.length();
		if (txtLen > 0)
			setResponseLine(_tagString + " " + rspCode + " " + text);
		else
			setResponseLine(_tagString + " " + rspCode);

		setTagValue(Integer.MIN_VALUE);
		setErrCode(nErr);
		return true;
	}
	/**
	 * @param tagString tag value - may NOT be null/empty
	 * @param text text to be displayed - may be null/empty (without
	 * the tag and/or error code)
	 * @return TRUE if OK arguments
	 */
	public boolean setOKResponse (final String tagString, final String text)
	{
		return setResponse(tagString, EOK, text);
	}
	/**
	 * Uses the last set tag value
	 * @param text text to be displayed - may be null/empty (without
	 * the tag and/or error code)
	 * @return TRUE if successful
	 */
	public boolean setOKResponseString (final String text)
	{
		return setOKResponse(getTagString(), text);
	}
	/**
	 * @param tagString tag value - may NOT be null/empty
	 * @param text text to be displayed - may be null/empty (without
	 * the tag and/or error code)
	 * @return TRUE if OK arguments
	 */
	public boolean setNOResponse (final String tagString, final String text)
	{
		return setResponse(tagString, ENO, text);
	}
	/**
	 * Uses the last set tag value
	 * @param text text to be displayed - may be null/empty (without
	 * the tag and/or error code)
	 * @return TRUE if successful
	 */
	public boolean setNOResponseString (final String text)
	{
		return setNOResponse(getTagString(), text);
	}
	/**
	 * @param tagString tag value - may NOT be null/empty
	 * @param text text to be displayed - may be null/empty (without
	 * the tag and/or error code)
	 * @return TRUE if OK arguments
	 */
	public boolean setBADResponse (final String tagString, final String text)
	{
		return setResponse(tagString, EBAD, text);
	}
	/**
	 * Uses the last set tag value
	 * @param text text to be displayed - may be null/empty (without
	 * the tag and/or error code)
	 * @return TRUE if successful
	 */
	public boolean setBADResponseString (final String text)
	{
		return setBADResponse(getTagString(), text);
	}
	/**
	 * @param tagValue tag value - may NOT be negative
	 * @param nErr error code - MUST be one of the known codes: EOK, ENO, EBAD
	 * @param text text to be displayed - may be null/empty (without
	 * the tag and/or error code)
	 * @return TRUE if OK arguments
	 */
	public boolean setResponse (final int tagValue, final int nErr, final String text)
	{
		if (tagValue < 0)
			return false;
		// build with string value
		if (!setResponse(String.valueOf(tagValue), nErr, text))
			return false;

		// update the tag value
		setTagValue(tagValue);
		return true;
	}
	/**
	 * @param tagValue tag value - may NOT be negative
	 * @param text text to be displayed - may be null/empty (without
	 * the tag and/or error code)
	 * @return TRUE if OK arguments
	 */
	public boolean setOKResponse (final int tagValue, final String text)
	{
		return setResponse(tagValue, EOK, text);
	}
	/**
	 * Uses the last set tag value
	 * @param text text to be displayed - may be null/empty (without
	 * the tag and/or error code)
	 * @return TRUE if successful
	 */
	public boolean setOKResponseValue (final String text)
	{
		return setOKResponse(getTagValue(), text);
	}
	/**
	 * @param tagValue tag value - may NOT be negative
	 * @param text text to be displayed - may be null/empty (without
	 * the tag and/or error code)
	 * @return TRUE if OK arguments
	 */
	public boolean setNOResponse (final int tagValue, final String text)
	{
		return setResponse(tagValue, ENO, text);
	}
	/**
	 * Uses the last set tag value
	 * @param text text to be displayed - may be null/empty (without
	 * the tag and/or error code)
	 * @return TRUE if successful
	 */
	public boolean setNOResponseValue (final String text)
	{
		return setNOResponse(getTagValue(), text);
	}
	/**
	 * @param tagValue tag value - may NOT be negative
	 * @param text text to be displayed - may be null/empty (without
	 * the tag and/or error code)
	 * @return TRUE if OK arguments
	 */
	public boolean setBADResponse (final int tagValue, final String text)
	{
		return setResponse(tagValue, EBAD, text);
	}
	/**
	 * Uses the last set tag value
	 * @param text text to be displayed - may be null/empty (without
	 * the tag and/or error code)
	 * @return TRUE if successful
	 */
	public boolean setBADResponseValue (final String text)
	{
		return setBADResponse(getTagValue(), text);
	}
	/**
	 * Copy construction
	 * @param rsp source response to copy from (ignored if null)
	 */
	public void update (IMAP4TaggedResponse rsp)
	{
		if (null == rsp)
			return;	// just so we have a debug breakpoint

		setErrCode(rsp.getErrCode());
		setTagValue(rsp.getTagValue());
		setResponseLine(rsp.getResponseLine());
		setTagString(rsp.getTagString());
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolResponse#reset()
	 */
	@Override
	public void reset ()
	{
		super.reset();

		setErrCode(-1);
		setTagValue(Integer.MIN_VALUE);
		setTagString(null);
	}

	public boolean isSameResponse (final IMAP4TaggedResponse rsp)
	{
		if (!super.isSameResponse(rsp))
			return false;
		if (this == rsp)
			return true;

		if (rsp.isResponse() != isResponse())
			return false;

		if (isResponse())
		{
			if (getErrCode() != rsp.getErrCode())
				return false;
		}

		{
			final int	tv=getTagValue(), rv=rsp.getTagValue();
			if (tv <= 0)
			{
				// OK if both not set
				if (rv > 0)
					return false;
			}
			else if (tv != rv)
				return false;
		}

		return (0 == StringUtil.compareDataStrings(getTagString(), rsp.getTagString(), true));
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

		return isSameResponse((IMAP4TaggedResponse) obj);
	}

	protected static final int adjustHashCode (int val)
	{
		return (val >= 0) ? val : 0;
	}

	protected static final int adjustHashCode (long val)
	{
		return (val <= 0L) ? 0 : (int) (val & 0x7FFFFFFF);
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolResponse#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		final int	tv=getTagValue();
		return super.hashCode()
			+ (isResponse() ? getErrCode() : 0)
			+ ((tv > 0) ? tv : 0)
			+ ((tv <= 0) ? StringUtil.getDataStringHashCode(getTagString(), true) : 0)
			;
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolResponse#clone()
	 */
	@Override
	@CoVariantReturn
	public IMAP4TaggedResponse clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/**
	 * Copy constructor
	 * @param rsp source response to copy from - if null, nothing is copied
	 * @see #update(IMAP4TaggedResponse rsp)
	 */
	public IMAP4TaggedResponse (IMAP4TaggedResponse rsp)
	{
        update(rsp);
	}
	/**
	 * Special constructor used to generate static "dummy" OK/BAD/NO responses
	 * @param errCode error code to use as initial value
	 * @throws IllegalArgumentException if bad error code (should not happen)
	 */
	public IMAP4TaggedResponse (final int errCode) throws IllegalArgumentException
	{
		_tagString = String.valueOf(errCode);
		switch(errCode)
		{
			case EOK	:
				setResponseLine(_tagString + " " + IMAP4_OK + " command (virtually) completed.");
				break;

			case ENO	:
				setResponseLine(_tagString + " " + IMAP4_NO + " command (virtually) denied.");
				break;

			case EBAD	:
				setResponseLine(_tagString + " " + IMAP4_BAD + " command (virtually) illegal.");
				break;

			default		:
				throw new IllegalArgumentException("Bad/Illeagl virtual error code: " + errCode);
		}

		_errCode = errCode;
		_tagValue = Integer.MIN_VALUE;
	}
	// static objects that can be used as "dummy" tagged responses
	public static final IMAP4TaggedResponse	OK=new IMAP4TaggedResponse(EOK);
	public static final IMAP4TaggedResponse	NO=new IMAP4TaggedResponse(ENO);
	public static final IMAP4TaggedResponse	BAD=new IMAP4TaggedResponse(EBAD);
	/**
     * Checks the response line if it is a tagged response and its tag matches the supplied value
     * @param tagValue expected tag value
     * @param rspLine received response line
	 * @param inRsp response object to be updated - if null, a new one is allocated, else the supplied
	 * object contents are updated and (if successful) it is returned as the function result
     * @return tagged response object (or null if not a final response)
     */
	public static final IMAP4TaggedResponse getFinalResponse (int tagValue, ParsableString rspLine, IMAP4TaggedResponse inRsp)
	{
		final int rspLen=(null == rspLine) ? 0 : rspLine.length();
	    if ((rspLen <= 0) || (tagValue < 0))
	        return null;

		final int tagStart=rspLine.getStartIndex(), tagEnd=rspLine.findNonEmptyDataEnd(tagStart);
		if (tagEnd <= tagStart) // should not happen (unless empty data)
			return null;

	    final IMAP4TaggedResponse rsp=(null == inRsp) ? new IMAP4TaggedResponse() : inRsp;
		try
		{
			final int	rspTag=rspLine.getUnsignedInt(tagStart, tagEnd);
			if (rspTag != tagValue)
				return null;

			rsp.setTagValue(tagValue);
		}
		catch(NumberFormatException nfe)
		{
			// should not happen, but ignore it
			return null;
		}

	    // skip all ' ' up to response code
		final int rspStart=rspLine.findNonEmptyDataStart(tagEnd+1);
		if (rspStart <= tagEnd)
			return null;

		final int rspEnd=rspLine.findNonEmptyDataEnd(rspStart+1);
		if (rspEnd <= rspStart)
			return null;

	    // extract response code
		final int	errCode=xlateRspCode(rspLine, rspStart, rspEnd);
	    rsp.setErrCode(errCode);
	    if (errCode < 0)    // make sure it is one of the known responses
	        return null;

        // add whatever information is still there in the response line
// TODO - uncomment only if no performance hit...	    rsp._tagString = String.valueOf(tagValue);
	    rsp.setResponseLine(rspLine.toString());
	    return rsp;
	}

	public static final IMAP4TaggedResponse getFinalResponse (int tagValue, ParsableString rspLine)
	{
		return getFinalResponse(tagValue, rspLine, (IMAP4TaggedResponse) null);
	}

	public static final IMAP4TaggedResponse getFinalResponse (int tagValue, char[] rspLine, int offset, int len)
	{
		if ((tagValue < 0)
		 || (null == rspLine) || (rspLine.length <= 0)
		 || (offset < 0) || (len <= 0) || ((offset + len) > rspLine.length))
			return null;

		return getFinalResponse(tagValue, new ParsableString(rspLine));
	}

	public static final IMAP4TaggedResponse getFinalResponse (int tagValue, char[] rspLine)
	{
		return getFinalResponse(tagValue, rspLine, 0, (null == rspLine) ? 0 : rspLine.length);
	}
	/**
	 * @param tagValue tag value to look for - may NOT be negative
	 * @param rspLine response line to be parsed - may NOT be null/empty
	 * @param rsp modifiable response to be initialized with the results - if
	 * null, then one is created (if this a valid tagged response). Otherwise,
	 * its contents are set
	 * @return null if this is not a valid final (tagged) response
	 */
	public static final IMAP4TaggedResponse getFinalResponse (final int tagValue, final CharSequence rspLine, final IMAP4TaggedResponse rsp)
	{
		if (rsp != null)
			rsp.reset();

		final int rspLen=(null == rspLine) ? 0 : rspLine.length();
		if ((rspLen <= 0) || (tagValue < 0))
			return null;

		int curPos=0;
		for ( ; curPos < rspLen; curPos++)
		{
			final char    ch=rspLine.charAt(curPos);
			if ((ch < '0') || (ch > '9'))
			{
				// make sure we have a number and stopped due to ' '
				if ((0 == curPos) || (ch != ' '))
					return null;

				try
				{
					final int rspTag=ParsableString.getUnsignedInt(rspLine, 0, curPos);
					if (rspTag != tagValue)
						return null;

					break;
				}
				catch(NumberFormatException nfe)
				{
					// should not happen, but ignore it
					return null;
				}
			}
		}

		// if all digits, then obviously not a tagged response
		if (curPos >= rspLen)
			return null;

		// if no response code, then obviously not a tagged response
		final int rspStart=ParsableString.findNonEmptyDataStart(rspLine, curPos+1);
		if ((rspStart <= curPos) || (rspStart >= rspLen))
			return null;

		final int rspEnd=ParsableString.findNonEmptyDataEnd(rspLine, rspStart+1);
		if ((rspEnd <= rspStart) || (rspEnd > rspLen))
			return null;

		final IMAP4TaggedResponse	effRsp=(null == rsp) ? new IMAP4TaggedResponse() : rsp;
		final int					errCode=xlateRspCode(rspLine, rspStart, rspEnd);
		if (errCode < 0)
			return null;	// if this is not a known response, then do nothing

		effRsp.setErrCode(errCode);
		effRsp.setTagValue(tagValue);
		effRsp.setResponseLine(rspLine.toString());
		return effRsp;
	}
	/**
	 * @param tagValue tag value to look for - may NOT be negative
	 * @param rspLine response line to be parsed - may NOT be null/empty
	 * @return null if this is not a valid final (tagged) response
	 */
	public static final IMAP4TaggedResponse getFinalResponse (final int tagValue, final CharSequence rspLine)
	{
		return getFinalResponse(tagValue, rspLine, null);
	}
	/**
	 * @param tagString tag value - may NOT be null/empty. <B>Note:</B> tag
	 * value comparison is <U>case sensitive</U>.
	 * @param rspLine response line to be parsed - may NOT be null/empty
	 * @param rsp modifiable response to be initialized with the results - if
	 * null, then one is created (if this a valid tagged response). Otherwise,
	 * its contents are set
	 * @return null if this is not a valid final (tagged) response
	 */
	public static final IMAP4TaggedResponse getFinalResponse (final String tagString, final CharSequence rspLine, final IMAP4TaggedResponse rsp)
	{
		if (rsp != null)
			rsp.reset();

		final int rspLen=(null == rspLine) ? 0 : rspLine.length();
		if ((rspLen <= 0) || (null == tagString) || (tagString.length() <= 0))
			return null;

		int curPos=0;
		for ( ; curPos < rspLen; curPos++)
		{
			final char    ch=rspLine.charAt(curPos);
			if (' ' == ch)
			{
				// make sure not at first position (like '*' or '+')
				if (0 == curPos)
					return null;

				final String	tagVal=rspLine.subSequence(0, curPos).toString();
				if (!tagString.equals(tagVal))
					return null;

				break;
			}
		}

		// if exhausted all characters, then obviously not a tagged response
		if (curPos >= rspLen)
			return null;

		// if no response code, then obviously not a tagged response
		final int rspStart=ParsableString.findNonEmptyDataStart(rspLine, curPos+1);
		if ((rspStart <= curPos) || (rspStart >= rspLen))
			return null;

		final int rspEnd=ParsableString.findNonEmptyDataEnd(rspLine, rspStart+1);
		if ((rspEnd <= rspStart) || (rspEnd > rspLen))
			return null;

		final IMAP4TaggedResponse	effRsp=(null == rsp) ? new IMAP4TaggedResponse() : rsp;
		final int					errCode=xlateRspCode(rspLine, rspStart, rspEnd);
		if (errCode < 0)
			return null;	// if this is not a known response, then do nothing

		effRsp.setTagString(tagString);
		effRsp.setTagValue(Integer.MIN_VALUE);
		effRsp.setResponseLine(rspLine.toString());
		return effRsp;
	}
	/**
	 * @param tagString tag value - may NOT be null/empty. <B>Note:</B> tag
	 * value comparison is <U>case sensitive</U>.
	 * @param rspLine response line to be parsed - may NOT be null/empty
	 * @return null if this is not a valid final (tagged) response
	 */
	public static final IMAP4TaggedResponse getFinalResponse (final String tagString, final CharSequence rspLine)
	{
		return getFinalResponse(tagString, rspLine, null);
	}
}
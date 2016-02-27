package net.community.chest.net.proto.text.imap4;

import java.io.IOException;

import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Results of the GETQUOTAROOT response - if a value is unknown then a
 * negative value is returned</P>
 * 
 * @author Lyor G.
 * @since Sep 20, 2007 12:34:29 PM
 */
public class IMAP4QuotarootInfo extends IMAP4TaggedResponse {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8457277904171541828L;
	/**
	 * Special constructor used by JNI
	 */
	public IMAP4QuotarootInfo ()
	{
		super();
	}

	private long _maxStorageKB=(-1L), _curStorageKB=(-1L);
	private int _maxMessages=(-1), _curMessages=(-1);
	/**
	 * @return maximum allowed storage (KB) - if <0 then unknown, 0=INFINITE
	 */
	public long getMaxStorageKB ()
	{
		return _maxStorageKB;
	}

	public void setMaxStorageKB (long maxStorageKB)
	{
		_maxStorageKB = maxStorageKB;
	}
	/**
	 * @return maximum allowed number of messages - if <0 then unknown, 0=INFINITE
	 */
	public int getMaxMessages ()
	{
		return _maxMessages;
	}

	public void setMaxMessages (int maxMessages)
	{
		_maxMessages = maxMessages;
	}
	/**
	 * @return current used storage (KB) - if <0 then unknown, 0=INFINITE
	 */
	public long getCurStorageKB ()
	{
		return _curStorageKB;
	}

	public void setCurStorageKB (long curStorageKB)
	{
		_curStorageKB = curStorageKB;
	}
	/**
	 * @return current number of messages - if <0 then unknown, 0=INFINITE
	 */
	public int getCurMessages ()
	{
		return _curMessages;
	}

	public void setCurMessages (int curMessages)
	{
		_curMessages = curMessages;
	}

	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		final Class<?>	oc=(obj == null) ? null : obj.getClass();
		if (oc != getClass())
			return false;
		if (this == obj)
			return true;

		final IMAP4QuotarootInfo	qi=(IMAP4QuotarootInfo) obj;
		if (!isSameResponse(qi))
			return false;

		return (qi.getCurMessages() == getCurMessages())
			&& (qi.getCurStorageKB() == getCurStorageKB())
			&& (qi.getMaxMessages() == getMaxMessages())
			&& (qi.getMaxStorageKB() == getMaxStorageKB())
			;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return super.hashCode()
			+ adjustHashCode(getCurMessages())
			+ adjustHashCode(getCurStorageKB())
			+ adjustHashCode(getMaxMessages())
			+ adjustHashCode(getMaxStorageKB())
			;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#reset()
	 */
	@Override
	public void reset ()
	{
		super.reset();

		setCurMessages(-1);
		setCurStorageKB(-1L);
		setMaxMessages(-1);
		setMaxStorageKB(-1L);
	}
	/**
	 * Parses the QUOTAROOT response
	 * @param conn connection through which response is to be read
	 * @param tagValue tag value used for the command (must be >0)
	 * @return QUOTAROOT response
	 * @throws IOException if unable to extract full response
	 */
	public static final IMAP4QuotarootInfo getFinalResponse (final TextNetConnection conn, final int tagValue) throws IOException
	{
		return (IMAP4QuotarootInfo) (new IMAP4QuotarootInfoRspHandler(conn)).handleResponse(tagValue);
	}
}

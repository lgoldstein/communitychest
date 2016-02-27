package net.community.chest.net.proto.text.imap4;

import java.io.IOException;

import net.community.chest.CoVariantReturn;
import net.community.chest.net.TextNetConnection;


/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 11:17:49 AM
 */
public class IMAP4SearchResponse extends IMAP4TaggedResponse {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6701725186621856586L;
	public IMAP4SearchResponse ()
	{
		super();	// special constructor used by JNI
	}
	/**
	 * True if ID(s) array contains UID(s) rather then sequence number(s)
	 */
	private boolean _isUID	/* =false */;
	public boolean isUID ()
	{
		return _isUID;
	}

	public void setUID (boolean isUID)
	{
		_isUID = isUID;
	}
	/**
	 * ID(s) - UID/sequence number(s) - according to <I>isUID</I> value
	 */
	private long[] _msgIds	/* =null */;
	public long[] getMsgIds ()
	{
		return _msgIds;
	}

	public void setMsgIds (final long[] msgIds)
	{
		_msgIds = msgIds;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#reset()
	 */
	@Override
	public void reset ()
	{
		super.reset();
		setMsgIds(null);
	}

	public static final int calculateIdsHashCode (final long[] ids)
	{
		int	nRes=0;
		if ((ids != null) && (ids.length > 0))
		{
			for (final long	i : ids)
			{
				nRes += (int) (i & 0x7FFFFFFF);
				if (i > Integer.MAX_VALUE)
				{
					final int	hiVal=(int) ((i >> 32) & 0x7FFFFFFF);
					nRes += (hiVal + 1);
				}
			}
		}

		return nRes;
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

		final IMAP4SearchResponse	rsp=(IMAP4SearchResponse) obj;
		if (!isSameResponse(rsp))
			return false;

		final long[]	id1=getMsgIds(), id2=rsp.getMsgIds();
		if ((null == id1) || (id1.length <= 0))
			return ((null == id2) || (id2.length <= 0));
		else if ((null == id2) || (id2.length <= 0))
			return false;
		else if (id1.length != id2.length)
			return false;

		// TODO use maps instead of hash code to really check for ID(s) equality
		return (calculateIdsHashCode(id1) == calculateIdsHashCode(id2));
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return super.hashCode()
			+ calculateIdsHashCode(getMsgIds());
	}
	/*
	 * @see net.community.chest.net.proto.text.TextProtocolResponse#clone()
	 */
	@Override
	@CoVariantReturn
	public IMAP4SearchResponse clone () throws CloneNotSupportedException
	{
		final IMAP4SearchResponse	rsp=getClass().cast(super.clone());
		final long[]				ids=rsp.getMsgIds();
		if (ids != null)
			rsp.setMsgIds(ids.clone());

		return rsp;
	}
	/**
	 * Parses the SEARCH command result
	 * @param conn connection for reading the response
	 * @param tagValue original tag value used when command was issued
	 * @param isUID if TRUE, then returned matches are UID(s) rather than sequence numbers
	 * @param maxResults maximum results to be returned - if (<0) then unlimited
	 * @return search response
	 * @throws IOException if errors while parsing the response
	 */
	public static final IMAP4SearchResponse getFinalResponse (final TextNetConnection conn, final int tagValue, final boolean isUID, final int maxResults) throws IOException
	{
		final IMAP4SearchRspHandler	handler=new IMAP4SearchRspHandler(conn, isUID, maxResults);
		final IMAP4SearchResponse		rsp=(IMAP4SearchResponse) handler.handleResponse(tagValue);
		int								nErr=rsp.getErrCode();
		if (0 == nErr)
		{
			if ((nErr=handler.updateResults(rsp)) != 0)
				throw new IMAP4RspHandleException("Cannot (err=" + nErr + ") update results");
		}
	
		return rsp;
	}
}

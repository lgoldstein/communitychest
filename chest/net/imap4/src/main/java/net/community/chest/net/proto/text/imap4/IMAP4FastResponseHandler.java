package net.community.chest.net.proto.text.imap4;

import java.util.Collection;
import java.util.LinkedList;

import net.community.chest.mail.address.MessageAddressType;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 1:55:00 PM
 */
public class IMAP4FastResponseHandler implements IMAP4FetchResponseHandler {
	public IMAP4FastResponseHandler ()
	{
		super();
	}

	private Collection<IMAP4FastMsgInfo>	_msgs	/* =null */;
	public Collection<IMAP4FastMsgInfo> getMessages ()
	{
		return _msgs;
	}
	/**
	 * Adds the message to the collection
	 * @param msgInfo instance to be added - ignored if null
	 * @return updated messages collection - may be null/empty
	 * if no previous messages and nothing added
	 */
	private Collection<IMAP4FastMsgInfo> addMsgInfo (final IMAP4FastMsgInfo msgInfo)
	{
		if (msgInfo != null)
		{
			if (null == _msgs)
				_msgs = new LinkedList<IMAP4FastMsgInfo>();
			_msgs.add(msgInfo);
		}

		return getMessages();
	}

	private IMAP4FastMsgInfo	_curInfo	/* =null */;
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgResponseState(int, boolean)
	 */
	@Override
	public int handleMsgResponseState (final int msgSeqNo, final boolean fStarting)
	{
		if (fStarting)
		{
			// make sure no previous active entry
			if (_curInfo != null)
				return (-100);

			_curInfo = new IMAP4FastMsgInfo();
			_curInfo.setSeqNo(msgSeqNo);
		}
		else
		{
			// make sure have active entry
			if (null == _curInfo)
				return (-101);
			// make sure refer to same entry
			if (_curInfo.getSeqNo() != msgSeqNo)
				return (-102);

			addMsgInfo(_curInfo);
			_curInfo = null;	// mark entry no longer active
		}

		return 0;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleUID(int, long)
	 */
	@Override
	public int handleUID (final int msgSeqNo, final long msgUID)
	{
		// make sure have active entry
		if (null == _curInfo)
			return (-101);
		// make sure refer to same entry
		if (_curInfo.getSeqNo() != msgSeqNo)
			return (-102);
		// make sure not already set
		if (_curInfo.getUid() > 0L)
			return (-103);

		_curInfo.setUid(msgUID);
		return 0;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgPartSize(int, java.lang.String, long)
	 */
	@Override
	public int handleMsgPartSize (final int msgSeqNo, final String msgPart, final long partSize)
	{
		// make sure have active entry
		if (null == _curInfo)
			return (-101);
		// make sure refer to same entry
		if (_curInfo.getSeqNo() != msgSeqNo)
			return (-102);
		// make sure refer to top-level part
		if (!ENVELOPE_MSG_PART_ID.equals(msgPart))
			return (-103);
		// make sure not already set
		if (_curInfo.getSize() > 0)
			return (-104);

		_curInfo.setSize(partSize);
		return 0;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgPartStage(int, java.lang.String, boolean)
	 */
	@Override
	public int handleMsgPartStage (int msgSeqNo, String msgPart, boolean fStarting)
	{
		// make sure have active entry
		if (null == _curInfo)
			return (-101);
		// make sure refer to same entry
		if (_curInfo.getSeqNo() != msgSeqNo)
			return (-102);
		// make sure refer to top-level part
		if (!ENVELOPE_MSG_PART_ID.equals(msgPart))
			return (-103);

		return 0;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleFlagsStage(int, boolean)
	 */
	@Override
	public int handleFlagsStage (final int msgSeqNo, final boolean fStarting)
	{
		// make sure have active entry
		if (null == _curInfo)
			return (-101);
		// make sure refer to same entry
		if (_curInfo.getSeqNo() != msgSeqNo)
			return (-102);

		if (fStarting)
		{
			// make sure no previous value(s)
			final Collection<IMAP4MessageFlag>	flags=_curInfo.getFlags();
			if ((flags != null) && (flags.size() > 0))
				return (-103);
		}

		return 0;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleFlagValue(int, java.lang.String)
	 */
	@Override
	public int handleFlagValue (final int msgSeqNo, final String flagValue)
	{
		// make sure have active entry
		if (null == _curInfo)
			return (-101);
		// make sure refer to same entry
		if (_curInfo.getSeqNo() != msgSeqNo)
			return (-102);
		if ((null == flagValue) || (flagValue.length() <= 0))
			return (-103);

		_curInfo.addFlag(new IMAP4MessageFlag(flagValue));
		return 0;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleInternalDate(int, java.lang.String)
	 */
	@Override
	public int handleInternalDate (final int msgSeqNo, final String dateValue)
	{
		// make sure have active entry
		if (null == _curInfo)
			return (-101);
		// make sure refer to same entry
		if (_curInfo.getSeqNo() != msgSeqNo)
			return (-102);
		// make sure not already set
		if (_curInfo.getInternalDate() != null)
			return (-103);

		try
		{
			_curInfo.setInternalDate(IMAP4Protocol.decodeInternalDate(dateValue));
		}
		catch(RuntimeException e)
		{
			return Integer.MIN_VALUE;
		}

		return 0;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgPartAddress(int, java.lang.String, net.community.chest.mail.address.MessageAddressType, java.lang.String, java.lang.String)
	 */
	@Override
	public int handleMsgPartAddress (int msgSeqNo, String msgPart, MessageAddressType addrType, String dispName, String addrVal)
	{
		return (-1);	// unexpected
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgPartHeader(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public int handleMsgPartHeader (int msgSeqNo, String msgPart, String hdrName, String attrName, String attrValue)
	{
		return (-2);	// unexpected
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handlePartData(int, java.lang.String, byte[], int, int)
	 */
	@Override
	public int handlePartData (int msgSeqNo, String msgPart, byte[] bData, int nOffset, int nLen)
	{
		return (-3);	// unexpected
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handlePartDataStage(int, java.lang.String, boolean)
	 */
	@Override
	public int handlePartDataStage (int msgSeqNo, String msgPart, boolean fStarting)
	{
		return (-4);	// unexpected
	}
}

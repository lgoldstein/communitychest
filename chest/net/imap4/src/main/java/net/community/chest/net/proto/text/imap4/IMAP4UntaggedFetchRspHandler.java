package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import net.community.chest.ParsableString;
import net.community.chest.mail.RFCMimeDefinitions;
import net.community.chest.mail.address.MessageAddressType;
import net.community.chest.mail.headers.RFCHeaderDefinitions;
import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 2:33:56 PM
 */
public class IMAP4UntaggedFetchRspHandler extends AbstractIMAP4UntaggedResponseHandlerHelper {
	/**
	 * Returns a non-null/empty part ID (ENVELOPE_MSG_PART_ID) for supplied message part
	 * @param msgPart original message part
	 * @return same as input if non-null/empty, ENVELOPE_MSG_PART_ID otherwise
	 */
	public static final String getEffectiveMsgPartId (final String msgPart)
	{
		return ((null == msgPart) || (msgPart.length() <= 0)) ? IMAP4FetchResponseHandler.ENVELOPE_MSG_PART_ID : msgPart;
	}
	// helper object
	private IMAP4FetchResponseHandler  _rspHandler /* =null */;
	// last tagged response
	private final IMAP4TaggedResponse	_fetchRsp /* =null */;
	// constructor
	public IMAP4UntaggedFetchRspHandler (final TextNetConnection conn, final IMAP4FetchResponseHandler rspHandler, IMAP4TaggedResponse fetchRsp)
	{
		super(conn);
	
		// should not happen
		if ((null == (_rspHandler=rspHandler)) || (null == (_fetchRsp=fetchRsp)))
			throw new IllegalArgumentException("No FETCH response handler(s) specified");
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#getResponse()
	 */
	@Override
	protected IMAP4TaggedResponse getResponse ()
	{
		return _fetchRsp;
	}
    /**
     * Current message sequence number for which this response refers
     */
	private int _msgSeqNo /* =0 */;
	/**
	 * Handles the UID modifier
	 * @param startPos index within parse buffer where UID value is to be looked for (inclusive)
	 * @return next index in parse buffer (after modifier value), or (<0) if error
     * @throws IOException if I/O error when trying to re-fill parsing buffer
	 */
	private int handleUID (final int startPos) throws IOException
	{
		final NumInfo	numInfo=extractSimpleNumber(startPos);
		if (null == numInfo)
			return (-361);

		final int	nErr=_rspHandler.handleUID(_msgSeqNo, numInfo.num);
		if (nErr != 0)  // make error negative so as not be confused with "good" index result
			return adjustErr(nErr);

		return numInfo.startPos;
	}
	/**
	 * Handles the RFC822.SIZE modifier
	 * @param startPos index within parse buffer where value is to be looked for (inclusive)
	 * @return next index in parse buffer (after modifier value), or (<0) if error
     * @throws IOException if I/O error when trying to re-fill parsing buffer
	 */
	private int handleRFC822Size (final int startPos) throws IOException
	{
		final NumInfo	numInfo=extractSimpleNumber(startPos);
		if (null == numInfo)
			return (-361);

		final int	nErr=_rspHandler.handleMsgPartSize(_msgSeqNo, IMAP4FetchResponseHandler.ENVELOPE_MSG_PART_ID, numInfo.num);
		if (nErr != 0)  // make error negative so as not be confused with "good" index result
			return adjustErr(nErr);

		return numInfo.startPos;
	}
	/**
	 * Handles the INTERNALDATE modifier
	 * @param startPos index within parse buffer where value is to be looked for (inclusive)
	 * @return next index in parse buffer (after modifier value), or (<0) if error
	 * @throws IOException if I/O error found
	 */
	private int handleInternalDate (final int startPos) throws IOException
	{
		final IMAP4ParseAtomValue	aVal=extractStringHdrVal(startPos, false);
		if (null == aVal)	// should not happen
			return Integer.MIN_VALUE;

		// don't bother the callback if empty header
		if (aVal.length() > 0)
		{
			int nErr=_rspHandler.handleInternalDate(_msgSeqNo, aVal.toString());
			if (nErr != 0)
				return adjustErr(nErr);
		}

		return aVal.startPos;
	}
	/**
	 * Handles the FLAGS modifier
	 * @param startPos index within parse buffer where flags are to be looked for (inclusive)
	 * @return next index in parse buffer (after modifier value), or (<0) if error
     * @throws IOException if I/O error when trying to re-fill parsing buffer
	 */
	private int handleFlags (final int startPos) throws IOException
	{
		int nErr=_rspHandler.handleFlagsStage(_msgSeqNo, true);
		if (nErr != 0)  // adjust the error
			return adjustErr(nErr);

		final Collection<String>	flags=new LinkedList<String>();
		final int         			listEnd=extractFlagsList(startPos, flags);
		if (listEnd < 0)
			return listEnd;

		for (final String flgVal : flags)
		{
			if ((null == flgVal) || (flgVal.length() <= 0))
				continue;	// should not happen

			if ((nErr=_rspHandler.handleFlagValue(_msgSeqNo, flgVal)) != 0)
				return adjustErr(nErr);
		}

		if ((nErr=_rspHandler.handleFlagsStage(_msgSeqNo, false)) != 0)
			return adjustErr(nErr);

		return listEnd;
	}
    /**
     * Handles the data of an envelope header
     * @param msgPart message part to which this envelope header refers
     * @param startPos index in parse buffer where data is to be parsed (inclusive)
     * @param hdrName name of header whose data is being parsed
     * @param allowOverflow if TRUE, then if the parsing buffer cannot accomodate the full value, no error is returned
     * @return next index in parse buffer (after modifier value), or (<0) if error
     * @see IMAP4FetchResponseHandler#ENVELOPE_MSG_PART_ID
     * @throws IOException if I/O error when trying to re-fill parsing buffer
     */
	private int parseEnvelopeStringHdrVal (final String msgPart, final int startPos, final String hdrName, final boolean allowOverflow) throws IOException
	{
		final IMAP4ParseAtomValue	aVal=extractStringHdrVal(startPos, allowOverflow);
		if (null == aVal)
			return Integer.MIN_VALUE;

		// don't bother the callback if empty header
		if (aVal.length() > 0)
		{
			int nErr=_rspHandler.handleMsgPartHeader(_msgSeqNo, msgPart, hdrName, null, aVal.toString());
			if (nErr != 0)
				return adjustErr(nErr);
		}

		return aVal.startPos;
	}
	// enumeration of addresses fields of interest - DO NOT CHANGE ORDER !!!
	private static final int    ENVADDR_DISPNAME=0,
	                            ENVADDR_EMDOMAIN=(ENVADDR_DISPNAME+1),
								ENVADDR_MBNAME=(ENVADDR_EMDOMAIN+1),
								ENVADDR_HOSTDOMAIN=(ENVADDR_MBNAME+1),
								NUM_ENVADDR_COMPS=(ENVADDR_HOSTDOMAIN+1)
							;
	// permutations in case of extra components - xtraPerms[i][j] - i=baseValue - ENVADDR_EMDOMAIN, j=modulu 3
	private static final int[][]    xtraPerms={
		{ ENVADDR_EMDOMAIN, ENVADDR_MBNAME, ENVADDR_HOSTDOMAIN },
		{ ENVADDR_MBNAME, ENVADDR_HOSTDOMAIN, ENVADDR_EMDOMAIN },
		{ ENVADDR_HOSTDOMAIN, ENVADDR_EMDOMAIN, ENVADDR_MBNAME },
	};
    /**
     * Calculates a cyclic extra address component index
     * @param baseVal requested base value (must be one of the above)
     * @param extraCompsIndex the current extra components cound
     * @return cyclic value (excluding the display name)
     */
	private static final int getExtraAddrCompIndex (final int baseVal, final int extraCompsIndex)
	{
        return xtraPerms[baseVal - ENVADDR_EMDOMAIN][extraCompsIndex % (NUM_ENVADDR_COMPS - 1)];
	}
	/**
	 * Special keyword that is used sometime by servers to show that some address component is missing
	 */
	private static final String IMAP4MissingKeyword=".MISSING";
		private static final char[] IMAP4MissingKeywordChars=IMAP4MissingKeyword.toCharArray();
	/**
	 * Internal work buffer used to speed up some string management functions
	 */
	protected StringBuilder _workBuf /* =null */;
	// @see #workBuf
	protected Appendable getWorkBuf (final int nMinSize)
	{
		if (null == _workBuf)
			_workBuf = new StringBuilder(nMinSize);
		else
			_workBuf.setLength(0);

		return _workBuf;
	}
	/**
	 * Appends the e-mail address to the buffer (without delimiting angle brackets)
	 * @param sb string buffer to build/append to
	 * @param mbName e-mail address local part
	 * @param dmName e-mail address domain part
	 * @return TRUE if successful (Note: the validity of the supplied mail address components is not checked)
	 */
	private static final <A extends Appendable> A buildEmailAddress (final A sb, final CharSequence mbName, final CharSequence dmName) throws IOException
	{
		sb.append(mbName)
		  .append('@')
		  .append(dmName)
		  ;
		return sb;
	}
	/**
	 * Initial size of string buffer used to hold address components
	 */
	private static final int ADDR_STRBUF_INITIAL_SIZE=
		2 + Math.max(RFCMimeDefinitions.NonMailUserMailDomainPart.length(),RFCMimeDefinitions.NonMailUserLocalPart.length());
	/**
	 * Handles a specified header address value
	 * @param msgPart message part to which this address refers
	 * @param addrType address type for which this call is made
	 * @param addrComps extracted address components
	 * @param extraCompsIndex extra components (beyond what RFC2060 allows)
	 * @param cfnCallIndex number of times callback has been called so far
	 * @param singleInstance if TRUE, then once the handler has been informed of one address, no more calls are made
	 * @return new callback index number (<0 if error
	 * @see IMAP4FetchResponseHandler#ENVELOPE_MSG_PART_ID
	 */
	private int handleEnvelopeAddrHdrVal (final String msgPart, final MessageAddressType addrType, final StringBuilder[] addrComps, final int extraCompsIndex, final int cfnCallIndex, final boolean singleInstance)
	{
		// compensate for possible extra parameters in list
		int     		mbIndex=getExtraAddrCompIndex(ENVADDR_MBNAME, extraCompsIndex), nErr=0;
		StringBuilder	mbName=addrComps[mbIndex];
		boolean			fHaveMboxName=(mbName != null) && (mbName.length() > 0);

		/*  Detect empty/missing host name
		 *
		 * Note: RFC2060 states that if unknown it must be NIL, but many servers use
		 *			a ".MISSING-HOST-NAME" or a variation of it - usually with a '.' as 1st char
		 */
		int     		hsIndex=getExtraAddrCompIndex(ENVADDR_HOSTDOMAIN, extraCompsIndex);
		StringBuilder	hsName=addrComps[hsIndex];
		boolean			fHaveHost=(hsName != null) && (hsName.length() > 0) && (hsName.charAt(0) != IMAP4MissingKeywordChars[0]);

		StringBuilder	dsName=addrComps[ENVADDR_DISPNAME];
		boolean			fHaveDispName=(dsName != null) && (dsName.length() > 0);
		// if only display name available, then it will appear as the mailbox name (all others are empty/bad)
		if (!fHaveDispName)
		{
			if (fHaveMboxName && (!fHaveHost))
			{
				addrComps[ENVADDR_DISPNAME] = mbName;
				addrComps[mbIndex] = null;
				mbName = null;
				fHaveMboxName = false;
				fHaveDispName = true;
			}
		}

		// if empty or single quoted display name, then mark as empty
		if (fHaveDispName && (IMAP4Protocol.IMAP4_QUOTE_DELIM == dsName.charAt(0)) && (dsName.length() <= 2))
		{
			addrComps[ENVADDR_DISPNAME] = null;
			dsName = null;
			fHaveDispName = false;
		}

		// if have display name but bad/illegal e-mail address, then generate a non-existing address
		if (fHaveDispName)
		{
			if ((!fHaveMboxName) /* TODO efficient check || ((nErr=MessageAddresseeCommon.validateRFC822LocalMailPart(mbName.array(), 0, mbName.length())) != 0) */)
			{
				if (null == mbName)
					mbName = new StringBuilder(ADDR_STRBUF_INITIAL_SIZE);
				else
					mbName.setLength(0);

				mbName.append(RFCMimeDefinitions.NonMailUserLocalPart);
				fHaveMboxName = true;
			}

			if ((!fHaveHost)  /* TODO efficient check || ((nErr=MessageAddresseeCommon.validateRFC822MailDomainPart(hsName.array(), 0, hsName.length())) != 0) */)
			{
				if (null == hsName)
					hsName = new StringBuilder(ADDR_STRBUF_INITIAL_SIZE);
				else
					hsName.setLength(0);

				hsName.append(RFCMimeDefinitions.NonMailUserMailDomainPart);
				fHaveHost = true;
			}
		}
		else    // if not have display name, then make sure have at least mail address
		{
			/* TODO efficient check 
			if (fHaveMboxName && ((nErr=MessageAddresseeCommon.validateRFC822LocalMailPart(mbName.array(), 0, mbName.length())) != 0))
			{
				mbName = null;
				fHaveMboxName = false;
			}
			*/
			if (fHaveHost  /* TODO efficient check && ((nErr=MessageAddresseeCommon.validateRFC822MailDomainPart(hsName.array(), 0, hsName.length())) != 0) */)
			{	
				// assume any domain name starting with '.' is a ".MISSING.xxxx" string
				if ('.' == hsName.charAt(0))
				{	
					hsName = null;
					fHaveHost = false;
				}
			}
		}

		if ((!fHaveHost) || (!fHaveMboxName) || ((singleInstance && (cfnCallIndex != 0))))
			return cfnCallIndex;

		String dispName=null;
		if (fHaveDispName)
		{
			int dsStart=0, dsEnd=dsName.length();

			// if display name quoted, then use enclosing quotes
			if ((IMAP4Protocol.IMAP4_QUOTE_DELIM == dsName.charAt(dsStart)) &&
			    (IMAP4Protocol.IMAP4_QUOTE_DELIM == dsName.charAt(dsEnd - 1)) &&
				(dsStart < (dsEnd-1)))
			{
				dsStart++;
				dsEnd--;
			}

			// make sure we have something other than ""
			if (dsStart < dsEnd)
				dispName = dsName.subSequence(dsStart, dsEnd).toString();
		}

		final Appendable   sb=getWorkBuf(hsName.length() + mbName.length() + 1);
		try
		{
			buildEmailAddress(sb, mbName, hsName);
		}
		catch(IOException e)	// should not happen
		{
			return (-333);
		}

		if ((nErr=_rspHandler.handleMsgPartAddress(_msgSeqNo, msgPart, addrType, dispName, sb.toString())) != 0)
			return adjustErr(nErr);

		return (cfnCallIndex + 1);
	}
    /**
     * Handles a list of addresses (which may be empty or NIL)
     * @param msgPart message part to which this list of addresses referes
     * @param startPos index in parse buffer where data is to be parsed (inclusive)
     * @param addrType address type for which this parsing occurs
     * @param singleInstance if TRUE, then once the handler has been informed of one address, no more calls are made
     * for any further address (e.g., "From:/Sender:" group addresses...)
     * @return next index in parse buffer (after modifier value), or (<0) if error
     * @see IMAP4FetchResponseHandler#ENVELOPE_MSG_PART_ID
     * @throws IOException if I/O error when trying to re-fill parsing buffer
     */
	private int parseEnvelopeAddrHdrVal (final String msgPart, final int startPos, final MessageAddressType addrType, final boolean singleInstance) throws IOException
	{
		int listPos=ensureParseBufferData(startPos, 1);
        if (listPos < 0)
			return listPos;

		// allow NIL as hdr value
		if (IMAP4Protocol.IMAP4_PARLIST_SDELIM != _psHelper.getCharAt(listPos))
		{
			TrackingInfo    trkInfo=checkNILParseBuffer(listPos);
			if (trkInfo.isOK())
				return trkInfo.startPos;

			if (TrackingInfo.ENIL != (listPos=trkInfo.err))
				return listPos;

			listPos = trkInfo.startPos;
		}
        else
			listPos++;

		// non-NIL list skip list starter
		if ((listPos=ensureParseBufferData(listPos, 1)) < 0)
			return listPos;

		StringBuilder[]    addrComps=new StringBuilder[NUM_ENVADDR_COMPS];     // address components holders

        // NOTE: we limit the maximum pairs to ~32,000 so we don't have an infinite loop
		for (int    rspIndex=0, cfnCallIndex=0; (_psHelper.getCharAt(listPos) != IMAP4Protocol.IMAP4_PARLIST_EDELIM) && (rspIndex < Short.MAX_VALUE); rspIndex++)
		{
			// MUST start with a sub-list
			if (_psHelper.getCharAt(listPos) != IMAP4Protocol.IMAP4_PARLIST_SDELIM)
				return (-901);
			listPos++;  // skip address structure starter

			for (int    acIndex=0; acIndex < addrComps.length; acIndex++)
			{
				final IMAP4ParseAtomValue	aVal=extractStringHdrVal(listPos, false);
				if (null == aVal)	// should not happen
					return Integer.MIN_VALUE;

				final int		aLen=aVal.length();
				StringBuilder	aComp=addrComps[acIndex];
				if (aComp != null)
					aComp.setLength(0);
				
				if (aLen > 0)
				{
					if (null == aComp)
					{	
						aComp = new StringBuilder(aLen + ADDR_STRBUF_INITIAL_SIZE);
						addrComps[acIndex] = aComp;
					}
					
					aVal.val.append(aComp);
				}

				listPos = aVal.startPos;
			}

			/*  At end of address structure there must be a list end delimiter
			 *
			 * Note: although RFC2060 states only 4 elements to an address structure, some servers
			 *			(e.g., SW.COM KX-4.2) do not handle correctly names with quotes in them, and
			 *			create a list with more elements. So, if not found end of list, we keep going
			 *			until we find it, and assume that the last 3 components are according to RFC2060.
			 */
			if ((listPos=ensureParseBufferData(listPos, 1)) < 0)
				return listPos;

			// we allow for up to 127 more extra components just in order to avoid infinite loops
			int extraCompsIndex=0;
			for (; (_psHelper.getCharAt(listPos) != IMAP4Protocol.IMAP4_PARLIST_EDELIM) && (extraCompsIndex < Byte.MAX_VALUE); extraCompsIndex++)
			{
				// use a cyclic update policy (excluding the display name)
				int     		acIndex=getExtraAddrCompIndex(ENVADDR_EMDOMAIN, extraCompsIndex);
				StringBuilder	emDomain=addrComps[acIndex];

				// obviously, the "emDomain" is part of the personal name since more to come
				if (emDomain != null)
				{
					final StringBuilder	aComp=addrComps[ENVADDR_DISPNAME];
					if (aComp != null)
						aComp.append(emDomain);
					else
						addrComps[ENVADDR_DISPNAME] = emDomain;
				}

				{
					final IMAP4ParseAtomValue	aVal=extractStringHdrVal(listPos, false);
					if (null == aVal)	// should not happen
						return Integer.MIN_VALUE;

					final int		aLen=aVal.length();
					StringBuilder	aComp=addrComps[acIndex];
					if (aComp != null)
						aComp.setLength(0);

					if (aLen > 0)
					{	
						if (null == aComp)
						{	
							aComp = new StringBuilder(aLen + ADDR_STRBUF_INITIAL_SIZE);
							addrComps[acIndex] = aComp;
						}
				
						aVal.val.append(aComp);
					}

					if ((listPos=ensureParseBufferData(aVal.startPos, 1)) < 0)
						return listPos;
				}
			}

			// make sure we stopped because found end of list
			if (extraCompsIndex >= Byte.MAX_VALUE)
				return (-905);
			listPos++;	// skip list end delimiter

			if ((cfnCallIndex=handleEnvelopeAddrHdrVal(msgPart, addrType, addrComps, extraCompsIndex, cfnCallIndex, singleInstance)) < 0)
				return cfnCallIndex;

			if ((listPos=ensureParseBufferData(listPos, 1)) < 0)
				return listPos;
		}

		return (listPos + 1);	// skip address list structures delimiter
	}
	/**
	 * Handles the ENVELOPE modifier
	 * @param startPos index within parse buffer where ENVELOPE results are to be looked for (inclusive)
	 * @param msgPart message part to which this envelope refers
	 * @param informPartStage if true, then response handler is informed about ENVELOPE part start/end - e.g., for
	 * embedded messages, this argument will be FALSE, since the parser will have already informed the handler about
	 * the message part start/end
	 * @return next index in parse buffer (after modifier value), or (<0) if error
	 * @see IMAP4FetchResponseHandler#ENVELOPE_MSG_PART_ID
	 * @throws IOException if I/O error when trying to re-fill parsing buffer
	 */
	private int handleEnvelope (final int startPos, final String msgPart, final boolean informPartStage) throws IOException
	{
		int listStart=ensureParseBufferData(startPos,IMAP4Protocol.IMAP4_NILChars.length+1), maxIndex=_psHelper.getMaxIndex();
		if ((listStart < _psHelper.getStartIndex()) || (listStart >= maxIndex)) // list MUST contain either '()' or NIL
			return (-2);

		int nErr=informPartStage ? _rspHandler.handleMsgPartStage(_msgSeqNo, msgPart, true) : 0;
		if (nErr != 0)
			return adjustErr(nErr);

		// if response does not start with '(' make sure it is the NIL atom
		int listEnd=(-13);
		if (_psHelper.getCharAt(listStart) != IMAP4Protocol.IMAP4_PARLIST_SDELIM)
		{
			listEnd = listStart + IMAP4Protocol.IMAP4_NILChars.length;
			if ((listEnd > maxIndex) || (!_psHelper.compareTo(listStart, listEnd, IMAP4Protocol.IMAP4_NILChars, false)))
				return (-3);
		}
		else // list starts with a '('
		{
			if ((listEnd=parseEnvelopeStringHdrVal(msgPart, (listStart+1), RFCHeaderDefinitions.stdDateHdr, false)) < 0)
				return listEnd;
			if ((listEnd=parseEnvelopeStringHdrVal(msgPart, listEnd, RFCHeaderDefinitions.stdSubjectHdr, true)) < 0)
				return listEnd;
			if ((listEnd=parseEnvelopeAddrHdrVal(msgPart, listEnd, MessageAddressType.FROM, true)) < 0)
				return listEnd;
			if ((listEnd=parseEnvelopeAddrHdrVal(msgPart, listEnd, MessageAddressType.SENDER, true)) < 0)
				return listEnd;
			if ((listEnd=parseEnvelopeAddrHdrVal(msgPart, listEnd, MessageAddressType.REPLY_TO, true)) < 0)
				return listEnd;
			if ((listEnd=parseEnvelopeAddrHdrVal(msgPart, listEnd, MessageAddressType.TO, false)) < 0)
				return listEnd;
			if ((listEnd=parseEnvelopeAddrHdrVal(msgPart, listEnd, MessageAddressType.CC, false)) < 0)
				return listEnd;
			if ((listEnd=parseEnvelopeAddrHdrVal(msgPart, listEnd, MessageAddressType.BCC, false)) < 0)
				return listEnd;
			if ((listEnd=parseEnvelopeStringHdrVal(msgPart, listEnd, RFCHeaderDefinitions.stdInReplyToHdr, true)) < 0)
				return listEnd;
			if ((listEnd=parseEnvelopeStringHdrVal(msgPart, listEnd, RFCHeaderDefinitions.stdMessageIDHdr, true)) < 0)
				return listEnd;

			// if no more data left in parse buffer, then re-fill it
			if ((listEnd=ensureParseBufferData(listEnd,1)) < 0)
				return listEnd;

			// make sure terminating with ')'
			if (_psHelper.getCharAt(listEnd) != IMAP4Protocol.IMAP4_PARLIST_EDELIM)
				return (-12);

			listEnd++;  // skip terminating ')'
		}

		if (informPartStage)
		{
			if ((nErr=_rspHandler.handleMsgPartStage(_msgSeqNo, msgPart, false)) != 0)
				return adjustErr(nErr);
		}

		return ensureParseBufferData(listEnd, (-1));
	}
	/**
	 * Special class used to return content type information
	 */
	protected static final class MsgCTypeInfo {
		public String  _mimeType /* =null */, _mimeSubType /* =null */;
		/**
		 * next position in parse buffer for handling information
		 */
		public int     _startPos=(-1);

		public void reset ()
		{
			_mimeType = null;
			_mimeSubType = null;
			_startPos = (-1);
		}
	}
	/**
	 * String used to replace missing MIME tag in reported message part content type
	 */
	public static final String XMIMETypeReplacement="X-MIME-TYPE";
	/**
	 * String used to replace missing MIME tag in reported message part content sub-type
	 */
	public static final String XMIMESubTypeReplacement="X-MIME-SUB-TYPE";
	/**
	 * Handles the "Content-Type:" part of a message part structure
	 * @param startPos start index of header data in parse buffer (inclusive)
	 * @param msgPart message part to which this header refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @param ctInfo content type info object (in/out)
	 * @return 0 if successful
	 * @throws IOException if I/O errors encountered
	 * @see IMAP4FetchResponseHandler#ENVELOPE_MSG_PART_ID
	 * @see MsgCTypeInfo
	 */
	private int handleMsgPartContentType (final int startPos, final String msgPart, MsgCTypeInfo ctInfo) throws IOException
	{
		ctInfo.reset();

		IMAP4ParseAtomValue	aVal=extractStringHdrVal(startPos, false);
		if (null == aVal)	// should not happen
			return Integer.MIN_VALUE;
		if (aVal.length() > 0)
			ctInfo._mimeType = aVal.toString();
		else
			ctInfo._mimeType = XMIMETypeReplacement;

		if (null == (aVal=extractStringHdrVal(aVal.startPos, false)))
			return Integer.MIN_VALUE;	// should not happen
		if (aVal.length() > 0)
			ctInfo._mimeSubType = aVal.toString();
		else
			ctInfo._mimeSubType = XMIMESubTypeReplacement;

		final int	typeLen=(null == ctInfo._mimeType) ? 0 : ctInfo._mimeType.length(),
					subTypeLen=(null == ctInfo._mimeSubType) ? 0 : ctInfo._mimeSubType.length();
		// just making sure again...
		if ((typeLen > 0) && (subTypeLen > 0))
		{
			final int	nErr=reportContentType(msgPart, ctInfo._mimeType, ctInfo._mimeSubType);
			if (nErr != 0)
				return adjustErr(nErr);
		}

		ctInfo._startPos = aVal.startPos;
		return 0;
	}
    /**
     * Cached object - allocated by need
     */
	private MsgCTypeInfo    _ctpInfo /* =null */;
	// @see #ctpInfo
	private MsgCTypeInfo getCTInfo ()
	{
		if (null == _ctpInfo)
			_ctpInfo = new MsgCTypeInfo();
		else
			_ctpInfo.reset();

		return _ctpInfo;
	}
	/**
	 * Handles the "Content-Type:" part of a message part structure
	 * @param startPos start index of header data in parse buffer (inclusive)
	 * @param msgPart message part to which this header refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @return content type info (null if error)
	 * @throws IOException if I/O errors encountered
	 * @see IMAP4FetchResponseHandler#ENVELOPE_MSG_PART_ID
	 * @see MsgCTypeInfo
	 */
	private MsgCTypeInfo handleMsgPartContentType (final int startPos, final String msgPart) throws IOException
	{
		final MsgCTypeInfo    ctInfo=getCTInfo();
		final int             nErr=handleMsgPartContentType(startPos, msgPart, ctInfo);
		if (nErr != 0)
			return null;
		else
			return ctInfo;
	}
	/**
	 * Handles a list of parameters for a header
	 * @param startPos start index of header data in parse buffer (inclusive)
	 * @param msgPart message part to which this header refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @param hdrName header name
	 * @return next position in parse string (<0 if error)
	 * @throws IOException if I/O errors encountered
	 * @see IMAP4FetchResponseHandler#ENVELOPE_MSG_PART_ID
	 */
	private int handleMsgPartParamsList (final int startPos, final String msgPart, final String hdrName) throws IOException
	{
		int curPos=ensureParseBufferData(startPos,2),
			startIndex=_psHelper.getStartIndex(),
			maxIndex=_psHelper.getMaxIndex();
		if ((curPos < startIndex) || (curPos >= maxIndex))
			return (-92);

		if (_psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_SDELIM)
			return skipNILParseBuffer(curPos);
		curPos++;   // skip '('

		// limit the loop to ~32K parameters list in order to avoid an infinite loop
		for (int rspIndex=0; (_psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_EDELIM) && (rspIndex < Short.MAX_VALUE); rspIndex++)
		{
			IMAP4ParseAtomValue	aVal=extractStringHdrVal(curPos, false);
			final String  		attrName=
				((null == aVal) || (aVal.length() <= 0)) ? null : aVal.toString();
			if (((curPos=ensureParseBufferData(aVal.startPos, 1)) < (startIndex=_psHelper.getStartIndex())) ||
				(curPos >= (maxIndex=_psHelper.getMaxIndex())))
				return (-94);
			
			// check if sub-attributes
			if (IMAP4Protocol.IMAP4_PARLIST_SDELIM == _psHelper.getCharAt(curPos))
			{
				if ((curPos=handleMsgPartParamsList(curPos, msgPart, hdrName)) < 0)
					return curPos;

				continue;
			}
			
			// simple attribute value
			aVal = extractStringHdrVal(curPos, true);

			final String  attrValue=((null == aVal) || (aVal.length() <= 0)) ? null : aVal.toString();
            if (((attrName != null) && (attrName.length() > 0)) ||
                ((attrValue != null) && (attrValue.length() > 0)))
            {
                int nErr=_rspHandler.handleMsgPartHeader(_msgSeqNo, msgPart, hdrName, attrName, attrValue);
                if (nErr != 0)
                    return adjustErr(nErr);
            }

            if (((curPos=ensureParseBufferData(aVal.startPos, 1)) < (startIndex=_psHelper.getStartIndex())) ||
            	(curPos >= (maxIndex=_psHelper.getMaxIndex())))
            	return (-95);
		}

		// make sure we did not stop because of infinite loop
		if (_psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_EDELIM)
			return (-93);

		// skip terminating paranthesis and position at first non-empty place
		return ensureParseBufferData(curPos + 1, 1);
	}
	/**
	 * Calls the handler to inform about a message part size
	 * @param startPos start index of part data in parse buffer (inclusive)
	 * @param msgPart message part to which this part refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @return next position in parse string (<0 if error)
	 * @throws IOException if I/O errors encountered
	 */
	private int handleSizeInfo (final int startPos, final String msgPart) throws IOException
	{
		final NumInfo	numInfo=extractSimpleNumber(startPos);
		if (null == numInfo)
			return (-78);

		// inform about sub-part size
		final int	nErr=_rspHandler.handleMsgPartSize(_msgSeqNo, msgPart, numInfo.num);
		if (nErr != 0)
			return adjustErr(nErr);

		return numInfo.startPos;
	}
	/**
	 * Handles a value that represents number of text lines in a part (for now, it is silently ignored)
	 * @param startPos start index of part number of lines in parse buffer (inclusive)
	 * @param msgPart message part to which this part refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @return next position in parse string (<0 if error)
	 * @throws IOException if I/O errors encountered
	 */
	private int handlePartLinesInfo (final int startPos, final String msgPart /* ignored */) throws IOException
	{
		if (null == msgPart)	// should not happen (we check this just so the IDE does not complain about the ignored parameter)
			return (-333);

		final NumInfo	numInfo=extractSimpleNumber(startPos);
		if (null == numInfo)
			return (-334);
		else
			return numInfo.startPos;
	}
	/**
	 * Handles an embedded message part information
	 * @param startPos start index of part data in parse buffer (inclusive)
	 * @param msgPart message part to which this part refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @param extInfo if TRUE, then BODYSTRUCTURE (extended information) requested
	 * @return next position in parse string (<0 if error)
	 * @throws IOException if I/O errors encountered
	 * @see IMAP4FetchResponseHandler#ENVELOPE_MSG_PART_ID
	 */
	private int handleEmbeddedMsgPart (final int startPos, final String msgPart, final boolean extInfo) throws IOException
	{
		int	curPos=handleEnvelope(startPos, msgPart, false);
		if (curPos < 0)
			return curPos;
		
		if ((curPos=handleBodyStructure(curPos, msgPart, extInfo)) < 0)
			return curPos;

		if (((curPos=ensureParseBufferData(curPos,1)) < 0) || (!_psHelper.isAccessibleIndex(curPos)))
			return (-261);

		// embedded message size in text lines follows
		if (!_psHelper.isDigit(curPos))
			return (-262);

		if ((curPos=handlePartLinesInfo(curPos, msgPart)) < 0)
			return curPos;

		return curPos;
	}
	/**
	 * Reports the content type MIME tag to the handler 
	 * @param msgPart message part to which this report refers
	 * @param mimeType MIME type tag
	 * @param mimeSubType MIME sub-type tag
	 * @return 0 if successful
	 */
	private int reportContentType (final String msgPart, final String mimeType, final String mimeSubType)
	{
		final Appendable	sb=getWorkBuf(mimeType.length() + mimeSubType.length() + 1);
		try
		{
			sb.append(mimeType)
			  .append(RFCMimeDefinitions.RFC822_MIMETAG_SEP)
			  .append(mimeSubType)
			  ;
		}
		catch(IOException e)	// should not happen
		{
			return (-301);
		}

		return _rspHandler.handleMsgPartHeader(_msgSeqNo, msgPart, RFCHeaderDefinitions.stdContentTypeHdr, null, sb.toString());
	}
	/**
	 * Handles a single header data
	 * @param startPos start index of header data in parse buffer (inclusive)
	 * @param msgPart message part to which this header refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @param hdrName header name
	 * @return next position in parse string (<0 if error)
	 * @throws IOException if I/O errors encountered
	 * @see IMAP4FetchResponseHandler#ENVELOPE_MSG_PART_ID
	 */
	private int handleMsgPartHdr (final int startPos, final String msgPart, final String hdrName) throws IOException
	{
		final IMAP4ParseAtomValue	aVal=extractStringHdrVal(startPos, true);
		if (null == aVal)	// should not happen
			return Integer.MIN_VALUE;

		// don't bother if empty value
		if (aVal.length() > 0)
		{
			int nErr=_rspHandler.handleMsgPartHeader(_msgSeqNo, msgPart, hdrName, null, aVal.toString());
			if (nErr != 0)
				return adjustErr(nErr);
		}

		return aVal.startPos;
	}
	/**
	 * Handles an (optional) header value - i.e., if ')' found (meaning no header data), then OK
	 * @param startPos start index of header data in parse buffer (inclusive)
	 * @param msgPart message part to which this header refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @param hdrName header name
	 * @return next position in parse string (<0 if error)
	 * @throws IOException if I/O errors encountered
	 */
	private int handleOptionalMsgPartHdr (final int startPos, final String msgPart, final String hdrName) throws IOException
	{
		final int	curPos=ensureParseBufferData(startPos,1);
		if ((curPos < _psHelper.getStartIndex()) || (curPos >= _psHelper.getMaxIndex()))
			return (-231);

		// if already at end of list, do nothing
		if (IMAP4Protocol.IMAP4_PARLIST_EDELIM == _psHelper.getCharAt(curPos))
			return curPos;

		return handleMsgPartHdr(startPos, msgPart, hdrName);
	}
	/**
	 * Handles the (optional) "Content-Disposition:" header information
	 * @param startPos start index of header data in parse buffer (inclusive)
	 * @param msgPart message part to which this header refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @return next position in parse string (<0 if error)
	 * @throws IOException if I/O errors encountered
	 */
	private int handleOptionalMsgPartDisposition (final int startPos, final String msgPart) throws IOException
	{
		int	curPos=ensureParseBufferData(startPos,1);
		if ((curPos < _psHelper.getStartIndex()) || (curPos >= _psHelper.getMaxIndex()))
			return (-221);

		// if not start of parameters list, then assume "simple" header data (if any)
		if (IMAP4Protocol.IMAP4_PARLIST_SDELIM != _psHelper.getCharAt(curPos))
			return handleOptionalMsgPartHdr(curPos, msgPart, RFCHeaderDefinitions.stdContentDisposition);

		// structure is disposition type string, followed by attribute/value pairs
		final	IMAP4ParseAtomValue	aVal=extractStringHdrVal(curPos+1, false);
		if ((null == aVal) || (aVal.length() <= 0))
			return (-222);   // should not happen

		final int	nErr=_rspHandler.handleMsgPartHeader(_msgSeqNo, msgPart,RFCHeaderDefinitions.stdContentDisposition, aVal.toString(), null);
		if (nErr != 0)
			return adjustErr(nErr);

		if ((curPos=handleOptionalMsgPartParamsList(aVal.startPos, msgPart, RFCHeaderDefinitions.stdContentDisposition)) < 0)
			return curPos;

		if (((curPos=ensureParseBufferData(curPos, 1)) < _psHelper.getStartIndex()) || (curPos >= _psHelper.getMaxIndex()))
			return (-223);

		// skip end of list
		if (IMAP4Protocol.IMAP4_PARLIST_EDELIM != _psHelper.getCharAt(curPos))
			return (-224);

		return ensureParseBufferData(curPos + 1, 1);
	}
	/**
	 * Handles a non-paired list of elements for the specified header
	 * @param startPos start index of header data in parse buffer (inclusive)
	 * @param msgPart message part to which this header refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @param hdrName header for which this list is being parsed
	 * @return next position in parse string (<0 if error)
	 * @throws IOException if I/O errors encountered
	 */
	private int handleMsgPartList (final int startPos, final String msgPart, final String hdrName) throws IOException
	{
		int	curPos=ensureParseBufferData(startPos, 1);
		if ((curPos < _psHelper.getStartIndex()) || (curPos >= _psHelper.getMaxIndex()))
			return (-251);
		
		if (IMAP4Protocol.IMAP4_PARLIST_SDELIM != _psHelper.getCharAt(curPos))
			return (-252);
		
		if (((curPos=ensureParseBufferData(curPos+1, 1)) < _psHelper.getStartIndex()) || (curPos >= _psHelper.getMaxIndex()))
			return (-253);

		// we limit the loop to ~32K parameters to avoid infinite loops
		for (int	rspIndex=0; (_psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_EDELIM) && (rspIndex < Short.MAX_VALUE); rspIndex++)
		{
			final IMAP4ParseAtomValue	aVal=extractStringHdrVal(curPos, true);
			if (null == aVal)	// should not happen
				return Integer.MIN_VALUE;

			// don't bother the callback if empty header
			if (aVal.length() > 0)
			{
				final int nErr=_rspHandler.handleMsgPartHeader(_msgSeqNo, msgPart, hdrName, null, aVal.toString());
				if (nErr != 0)
					return adjustErr(nErr);
			}

			if (((curPos=ensureParseBufferData(aVal.startPos, 1)) < _psHelper.getStartIndex()) || (curPos >= _psHelper.getMaxIndex()))
				return (-255);
		}

		// make sure we stopped because end of list and NOT due to virtual infinite loop break
		if (_psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_EDELIM)
			return (-256);

		// skip end of paranthesised list
		return ensureParseBufferData(curPos+1, (-1));
	}
	/**
	 * Handles the (optional) "Content-Language:" header that may appear either as a simple header or a list
	 * @param startPos start index of header data in parse buffer (inclusive)
	 * @param msgPart message part to which this header refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @return next position in parse string (<0 if error)
	 * @throws IOException if I/O errors encountered
	 */
	private int handleOptionalMsgPartLanguage (final int startPos, final String msgPart) throws IOException
	{
		final int	curPos=ensureParseBufferData(startPos,1);
		if ((curPos < 0) || (!_psHelper.isAccessibleIndex(curPos)))
			return (-263);

		if (IMAP4Protocol.IMAP4_PARLIST_SDELIM == _psHelper.getCharAt(curPos))
			return handleMsgPartList(curPos, msgPart, RFCHeaderDefinitions.stdContentLanguage);
		else	// a simple  string value (or NIL)
			return handleOptionalMsgPartHdr(curPos, msgPart, RFCHeaderDefinitions.stdContentLanguage);
	}
	/**
	 * Handles "simple" message part (i.e. not multipart or embedded)
	 * @param startPos start index of part info in parse buffer (inclusive)
	 * @param msgPart message part to which this structure refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @param extInfo if TRUE, then BODYSTRUCTURE (extended information) requested
	 * @return next position in parse string (<0 if error)
	 * @throws IOException if I/O errors encountered
	 * @see IMAP4FetchResponseHandler#ENVELOPE_MSG_PART_ID
	 */
	private int handleMsgPartSimple (final int startPos, final String msgPart, final boolean extInfo) throws IOException
	{
		int nErr=_rspHandler.handleMsgPartStage(_msgSeqNo, msgPart, true);
		if (nErr != 0)
            return adjustErr(nErr);

		// order is: type, sub-type, params-list, id, description, encoding, size
		final MsgCTypeInfo    ctInfo=handleMsgPartContentType(startPos, msgPart);
		if (null == ctInfo)
			return (-76);

		String  mimeType=ctInfo._mimeType, mimeSubType=ctInfo._mimeSubType;
		int     typeLen=(null == mimeType) ? 0 : mimeType.length(),
		        subTypeLen=(null == mimeSubType) ? 0 : mimeSubType.length(),
		        curPos=handleMsgPartParamsList(ctInfo._startPos, msgPart, RFCHeaderDefinitions.stdContentTypeHdr);
		if (curPos < 0)
			return curPos;

		if ((curPos=handleMsgPartHdr(curPos, msgPart, RFCHeaderDefinitions.stdContentIDHdr)) < 0)
			return curPos;
		if ((curPos=handleMsgPartHdr(curPos, msgPart, RFCHeaderDefinitions.stdContentDescription)) < 0)
			return curPos;
		if ((curPos=handleMsgPartHdr(curPos, msgPart, RFCHeaderDefinitions.stdContentXferEncoding)) < 0)
			return curPos;
		// this is the content-length part
		if ((curPos=handleSizeInfo(curPos, msgPart)) < 0)
			return curPos;

		// this is a special case (observed in Exchange 2000) that needs handling
		boolean	isMultipartEmbedded=(typeLen != 0) && RFCMimeDefinitions.MIMEMultipartType.equalsIgnoreCase(mimeType) && (subTypeLen != 0);
        // for "text/xxx" MIME type we need to read the size as well
		if ((typeLen > 0) && RFCMimeDefinitions.MIMETextType.equalsIgnoreCase(mimeType))
		{
			if (((curPos=ensureParseBufferData(curPos, 1)) < 0) || (!_psHelper.isAccessibleIndex(curPos)))
				return (-77);

			if (_psHelper.isDigit(curPos))
			{
				// if have a number, then this is the number of text lines in the part
				if ((curPos=handlePartLinesInfo(curPos, msgPart)) < 0)
					return curPos;
			}
/* $$$
		#if FALSE
				// this is a special case (observed in Exchange2000)
				else if (IMAP4_PARLIST_SDELIM == *m_lpszCurPos)
				{
					//		Call again the callback with the "Content-Type: message/rcf822" header
					// in order to make the "multipart/xxx" the "super" type
					if ((exc=ReReportContentType(lpszFetchRsp, pszMIMEMessageType, pszMIMERfc822SubType)) != EOK)
						return exc;

					if ((exc=(lpszFetchRsp)) != EOK)
						return exc;
				}
		#endif
*/
		}
		else if (isMultipartEmbedded
		         || (       (typeLen > 0) && RFCMimeDefinitions.MIMEMessageType.equalsIgnoreCase(mimeType)
						&&  (subTypeLen > 0) && RFCMimeDefinitions.MIMERfc822SubType.equalsIgnoreCase(mimeSubType)
					)
				)
		{
			if (isMultipartEmbedded)
			{
				//		Call again the callback with the "Content-Type: message/rcf822" header
				// in order to make the "multipart/xxx" the "super" type
				if ((nErr=reportContentType(msgPart, RFCMimeDefinitions.MIMEMessageType, RFCMimeDefinitions.MIMERfc822SubType)) != 0)
					return adjustErr(nErr);
			}

			if ((curPos=handleEmbeddedMsgPart(curPos, msgPart, extInfo)) < 0)
				return curPos;
		}

		// optional: MD5, disposition and more extension data
		if (((curPos=ensureParseBufferData(curPos, 1)) < 0) || (!_psHelper.isAccessibleIndex(curPos)))
			return (-79);

		if (_psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_EDELIM)
		{
			// only for extended information we are allowed more data
			if (!extInfo)
				return (-80);

			if ((curPos=handleOptionalMsgPartHdr(curPos, msgPart, RFCHeaderDefinitions.stdContentMD5Hdr)) < 0)
				return curPos;
			if ((curPos=handleOptionalMsgPartDisposition(curPos, msgPart)) < 0)
				return curPos;
			if ((curPos=handleOptionalMsgPartLanguage(curPos, msgPart)) < 0)
				return curPos;

			// skip ending list separator
			if (((curPos=ensureParseBufferData(curPos, 1)) < 0) || (!_psHelper.isAccessibleIndex(curPos)))
				return (-81);

			if (_psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_EDELIM)
				return (-82);
		}

		if ((nErr=_rspHandler.handleMsgPartStage(_msgSeqNo, msgPart, false)) != 0)
            return adjustErr(nErr);

		// skip end of list separator and position to 1st non-empty position
		return ensureParseBufferData(curPos+1, 1);
	}
	/**
	 * Calls the handler for the specified header name if there is a list of parameters following the current position 
	 * @param startPos start index of part info in parse buffer (inclusive)
	 * @param msgPart message part to which this header refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @param hdrName header name whose (optional) parameters will be listed
	 * @return index of next position in parse buffer (<0 if error)
	 * @throws IOException if I/O error encountered
	 */
	private int handleOptionalMsgPartParamsList (final int startPos, final String msgPart, final String hdrName) throws IOException
	{
		final int	curPos=ensureParseBufferData(startPos, 1);
		if ((curPos < 0) || (!_psHelper.isAccessibleIndex(curPos)))
			return (-211);

		// if already at end of list, do nothing
		if (IMAP4Protocol.IMAP4_PARLIST_EDELIM == _psHelper.getCharAt(curPos))
			return curPos;

		return handleMsgPartParamsList(curPos, msgPart, hdrName);
	}
	/**
	 * Handles "multipart" (or embedded) message part
	 * @param startPos start index of part info in parse buffer (inclusive)
	 * @param msgPart message part to which this structure refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @param extInfo if TRUE, then BODYSTRUCTURE (extended information) requested
	 * @return next position in parse string (<0 if error)
	 * @throws IOException if I/O errors encountered
	 * @see IMAP4FetchResponseHandler#ENVELOPE_MSG_PART_ID
	 */
	private int handleMsgPartMultipart (final int startPos, final String msgPart, final boolean extInfo) throws IOException
	{
		// asume "mixed" as default if no sub-type specified
		final IMAP4ParseAtomValue	aVal=extractStringHdrVal(startPos, false);
		final String	mimeType=RFCMimeDefinitions.MIMEMultipartType,
						mimeSubType=((aVal != null) && (aVal.length() > 0)) ? aVal.toString() : RFCMimeDefinitions.MIMEMixedSubType;

		final int	nErr=reportContentType(msgPart, mimeType, mimeSubType);
		if (nErr != 0)
			return adjustErr(nErr);

		int	curPos=aVal.startPos, rspIndex=0;
		if (!extInfo)
		{
			// make sure no further data for non-extensible BODY
			if (((curPos=ensureParseBufferData(curPos, 1)) < _psHelper.getStartIndex()) || (curPos >= _psHelper.getMaxIndex()))
				return (-202);

			if (_psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_EDELIM)
				return (-203);

			return curPos;
		}

		// order is: body-content-type-list, body-disposition-list, body-language
		if ((curPos=handleOptionalMsgPartParamsList(curPos, msgPart, RFCHeaderDefinitions.stdContentTypeHdr)) < 0)
			return curPos;
		if ((curPos=handleOptionalMsgPartDisposition(curPos, msgPart)) < 0)
			return curPos;
		if ((curPos=handleOptionalMsgPartLanguage(curPos, msgPart)) < 0)
			return curPos;
		
		// handle any more extension data (we limit the loop to ~32K extra parameters to avoid infinte loops)
		for (curPos=ensureParseBufferData(curPos, 1); (curPos >= _psHelper.getStartIndex()) && (curPos < _psHelper.getMaxIndex()) && (rspIndex < Short.MAX_VALUE); curPos=ensureParseBufferData(curPos,1), rspIndex++)
		{
			if (IMAP4Protocol.IMAP4_PARLIST_EDELIM == _psHelper.getCharAt(curPos))
			{	
				// NOTE !!! do not skip end of list - it is skipped by caller !!
				return curPos;
			}
			else if (IMAP4Protocol.IMAP4_PARLIST_SDELIM == _psHelper.getCharAt(curPos))
			{
				if ((curPos=handleMsgPartParamsList(curPos, msgPart, "X-List-???:")) < 0)
					return curPos;
			}
			else
			{	
				if ((curPos=handleMsgPartHdr(curPos, msgPart, "X-???:")) < 0)
					return curPos;
			}
		}

		// this point is reached if ran out of data before finding ')' delimiter (or virtual infinite loop
		return (-204);
	}
	/**
	 * Handle a BODY/BODY.PEEK/BODYSTRUCTURE message part
	 * @param startPos start index of part info in parse buffer (inclusive)
	 * @param msgPart message part to which this structure refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @param extInfo if TRUE, then BODYSTRUCTURE (extended information) requested
	 * @return next position in parse string (<0 if error)
	 * @throws IOException if I/O errors encountered
	 * @see IMAP4FetchResponseHandler#ENVELOPE_MSG_PART_ID
	 */
	private int parseMsgPart (final int startPos, final String msgPart, final boolean extInfo) throws IOException
	{
		int curPos=ensureParseBufferData(startPos, 1), nErr=0;
		if ((curPos < 0) || (!_psHelper.isAccessibleIndex(curPos)))
			return (-72);

		// check if have sub-part
		boolean	fIsMultipart=false, fIsEnvPart=IMAP4FetchResponseHandler.ENVELOPE_MSG_PART_ID.equals(msgPart);
		if (IMAP4Protocol.IMAP4_PARLIST_SDELIM == _psHelper.getCharAt(curPos))
		{
			curPos++;
			fIsMultipart = true;
		}

		if ((nErr=_rspHandler.handleMsgPartStage(_msgSeqNo, msgPart, true)) != 0)
			return adjustErr(nErr);
		
		// we limit the maximum number of sub parts to ~32K in order to avoid having infinite loops
		for (int	subPartNum=1; subPartNum < Short.MAX_VALUE; subPartNum++)
		{
			// build msg part for this sub-part
			String  newPartId=msgPart;

			// if parent is not the top-level, then build a new part
			if (fIsEnvPart)
				newPartId = String.valueOf(subPartNum);
			else    // parent is NOT top-level
				newPartId += IMAP4Protocol.IMAP4_BODYPART_DELIM + String.valueOf(subPartNum);

			if (((curPos=ensureParseBufferData(curPos, 1)) < 0) || (!_psHelper.isAccessibleIndex(curPos)))
				return (-73);

			if (IMAP4Protocol.IMAP4_PARLIST_SDELIM == _psHelper.getCharAt(curPos))
			{
				if ((curPos=parseMsgPart(curPos, newPartId, extInfo)) < 0)
					return curPos;
			}
			else
			{
				if ((curPos=handleMsgPartSimple(curPos, newPartId, extInfo)) < 0)
					return curPos;
			}

			if (((curPos=ensureParseBufferData(curPos, 1)) < 0) || (!_psHelper.isAccessibleIndex(curPos)))
				return (-74);

			// assume 1st non-starting parantheses signals end of sub-parts
			if (_psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_SDELIM)
				break;

			curPos++;	// prepare for next sub-part
		}

		if (fIsMultipart)
		{
			if ((curPos=handleMsgPartMultipart(curPos, msgPart, extInfo)) < 0)
				return curPos;

			// at end of multipart handling the current position MUST be the end of list (see code)
			if (_psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_EDELIM)
				return (-75);

			curPos++;
		}

		if ((nErr=_rspHandler.handleMsgPartStage(_msgSeqNo, msgPart, false)) != 0)
			return adjustErr(nErr);

		// if this the top-level part, try to "shift" the parse buffer as much as possible
        return ensureParseBufferData(curPos, fIsEnvPart ? (-1) : 1);
	}
	/**
	 * Handle a BODY/BODY.PEEK/BODYSTRUCTURE response
	 * @param startPos start index of modifier in parse buffer (inclusive)
	 * @param msgPart message part to which this structure refers - ENVELOPE_MSG_PART_ID refers to top-level part
	 * @param extInfo if TRUE, then BODYSTRUCTURE (extended information) requested
	 * @return next position in parse string (<0 if error)
	 * @throws IOException if I/O errors encountered
	 * @see IMAP4FetchResponseHandler#ENVELOPE_MSG_PART_ID
	 */
	private int handleBodyStructure (final int startPos, final String msgPart, final boolean extInfo) throws IOException
	{
		final int curPos=ensureParseBufferData(startPos, 1);
		if ((curPos < 0) || (!_psHelper.isAccessibleIndex(curPos)))
			return (-71);

		// if not starting with a '(' then check that it is NUL
		if (IMAP4Protocol.IMAP4_PARLIST_SDELIM != _psHelper.getCharAt(curPos))
			return skipNILParseBuffer(curPos);

		return parseMsgPart(curPos+1, msgPart, extInfo);
	}
	/**
	 * Handles a part data response
	 * @param startPos start position in parse buffer where parsing should start (inclusive)
	 * @param msgPart message part to which this data refers (if null/empty the whole message is assumed)
	 * @return next index in parse buffer to resume parsing
     * @throws IOException if I/O errors encountered
	 */
	private int handlePartData (final int startPos, final String msgPart) throws IOException
	{
		int curPos=ensureParseBufferData(startPos, 1);
		if ((curPos < _psHelper.getStartIndex()) || (curPos >= _psHelper.getMaxIndex()))
			return (-881);
		final String	effPart=getEffectiveMsgPartId(msgPart);

		// check if have literal count
		if (IMAP4Protocol.IMAP4_OCTCNT_SDELIM == _psHelper.getCharAt(curPos))
		{
			final long	litSize=extractLiteralCount(curPos);
			if (litSize < 0L)
				return adjustErr((int) (litSize | 0x01));

			return handlePartData(getConnection(), _rspHandler, _msgSeqNo, effPart, litSize);
		}
		else if (IMAP4Protocol.IMAP4_QUOTE_DELIM == _psHelper.getCharAt(curPos))
		{
			// skip starting quote
			if (((curPos=ensureParseBufferData(curPos+1, 1)) < _psHelper.getStartIndex()) || (curPos >= _psHelper.getMaxIndex()))
				return (-884);

			while (IMAP4Protocol.IMAP4_QUOTE_DELIM != _psHelper.getCharAt(curPos))
			{	
				return (-885);	// TODO - unexpected non-empty quoted value, though allowed by the protocol....
			}

			return ensureParseBufferData(curPos+1, 1);	// skip 2nd quote	
		}
		else	// if neither octet count nor "" value, allow NIL
			return skipNILParseBuffer(curPos);
	}
	/**
	 * Handles a part headers data response (usually obtained via "HEADER.FIELDS" modifier)
	 * @param startPos start position in parse buffer where parsing should start (inclusive)
	 * @param msgPart message part to which this data refers (if null/empty the whole message is assumed)
	 * @return next index in parse buffer to resume parsing
	 * @throws IOException if I/O errors encountered
	 */
	private int handlePartHdrsData (final int startPos, final String msgPart) throws IOException
	{
		int curPos=ensureParseBufferData(startPos, 1);
		if (!_psHelper.isAccessibleIndex(curPos))
			return (-891);
		String	effPart=((null == msgPart) || (msgPart.length() <= 0)) ? IMAP4FetchResponseHandler.ENVELOPE_MSG_PART_ID : msgPart;

		// check if have literal count
		if (IMAP4Protocol.IMAP4_OCTCNT_SDELIM == _psHelper.getCharAt(curPos))
		{
			final long	litSize=extractLiteralCount(curPos);
			if (litSize < 0L)
				return adjustErr((int) (litSize | 0x01));

			return handleHdrsData(getConnection(), _rspHandler, _msgSeqNo, effPart, litSize);
		}
		else if (IMAP4Protocol.IMAP4_QUOTE_DELIM == _psHelper.getCharAt(curPos))
		{
			// skip starting quote
			if (((curPos=ensureParseBufferData(curPos+1, 1)) < _psHelper.getStartIndex()) || (curPos >= _psHelper.getMaxIndex()))
				return (-899);

			while (IMAP4Protocol.IMAP4_QUOTE_DELIM != _psHelper.getCharAt(curPos))
			{	
				return (-901);	// TODO - unexpected non-empty quoted value, though (maybe) allowed by the protocol....
			}

			return ensureParseBufferData(curPos+1, 1);	// skip 2nd quote	
		}
		else	// if neither octet count nor "" value, allow NIL
			return skipNILParseBuffer(curPos);
	}
	/**
	 * Handles BODY (and equivalent) responses
	 * @param startPos index in parse buffer for next character to be parsed (inclusive)
	 * @param bodyPart body part for which the response is required ("" == full message data)  
     * @return next position in parse string (<0 if error)
     * @throws IOException if I/O errors encountered
	 */
	private int handleBody (final int startPos, final String bodyPart) throws IOException
	{
		int	bpLen=(null == bodyPart) ? 0 : bodyPart.length();
		if (bpLen <= 0)	// if empty modifier, then assume simple "part" data (a.k.a. entire message) 
			return handlePartData(startPos, "");

		// find out if this is a "1.2.TEXT", "1.2", "1", "TEXT", etc.
		int	lastPart=0;
		for ( ; lastPart < bpLen; lastPart++)
		{
			final char	c=bodyPart.charAt(lastPart);
			if (('.' == c) || ((c >= '0') && (c <= '9')))
				continue;

			// neither '.' nor a digit
			if (lastPart > 0)
			{
				// make sure the character BEFORE that is '.' and have something before that
				if ((bodyPart.charAt(lastPart-1) != '.') || (lastPart <= 1))
					return (-931);
				lastPart--;
			}
			
			break;
		}

		// split around the '.' (if found)
		final String	msgPart=(lastPart < bpLen) ? bodyPart.substring((lastPart > 0) ? lastPart+1 : 0, bpLen) : "",
						partPath=(lastPart < bpLen) ? bodyPart.substring(0, lastPart) : bodyPart;

		// if character immediately after the '.' is a digit, then this is a "1.2" case
		if (lastPart < (bpLen-1))
		{
			final char	nextCh=bodyPart.charAt(lastPart+1);
			if ((nextCh >= '0') && (nextCh <= '9'))
				return  handlePartData(startPos, msgPart);
		}

		// if no '.' found and the last part is a digit, then it is a "1" case
		if ((bpLen != 0) && (lastPart == bpLen))
		{	
			final char	prevCh=bodyPart.charAt(lastPart-1);
			if ((prevCh >= '0') && (prevCh <= '9'))
				return  handlePartData(startPos, bodyPart);
		}

		// check if this is a "1.2.TEXT" or "1.2.HEADER.FIELDS"
		if (IMAP4BodyFetchModifier.IMAP4BodyHeaderFields.equalsIgnoreCase(msgPart) ||
			IMAP4BodyFetchModifier.IMAP4BodyNotHeaderFields.equalsIgnoreCase(msgPart))
			return  handlePartHdrsData(startPos, partPath);
		else
			return  handlePartData(startPos, bodyPart);
	}
    /**
     * Handles the modifier specified in the range of the parsable string
     * @param startPos start index of modifier in parse buffer (inclusive)
     * @param endPos end index of modifier in parse buffer (exclusive)
     * @return next position in parse string (<0 if error)
     * @throws IOException if I/O errors encountered
     */
	private int handleModifier (final int startPos, final int endPos) throws IOException
	{
        if (_psHelper.compareTo(startPos, endPos, IMAP4FetchModifier.IMAP4_UIDChars, true))
            return handleUID(endPos);
        else if (_psHelper.compareTo(startPos, endPos, IMAP4FetchModifier.IMAP4_RFC822SIZEChars, true))
            return handleRFC822Size(endPos);
        else if (_psHelper.compareTo(startPos, endPos, IMAP4FetchModifier.IMAP4_INTERNALDATEChars, true))
	        return handleInternalDate(endPos);
        else if (_psHelper.compareTo(startPos, endPos, IMAP4FetchModifier.IMAP4_FLAGSChars, true))
	        return handleFlags(endPos);
        else if (_psHelper.compareTo(startPos, endPos, IMAP4FetchModifier.IMAP4_ENVELOPEChars, true))
	        return handleEnvelope(endPos, IMAP4FetchResponseHandler.ENVELOPE_MSG_PART_ID, true);
        else if (_psHelper.compareTo(startPos, endPos, IMAP4FetchModifier.IMAP4_BODYSTRUCTUREChars, true))
	        return handleBodyStructure(endPos, IMAP4FetchResponseHandler.ENVELOPE_MSG_PART_ID, true);
        else if (_psHelper.compareTo(startPos, endPos, IMAP4FetchModifier.IMAP4_BODYChars, true))
        	return handleBodyStructure(endPos, IMAP4FetchResponseHandler.ENVELOPE_MSG_PART_ID, false);
        else if (_psHelper.compareTo(startPos, endPos, IMAP4FetchModifier.IMAP4_RFC822Chars, true))
        	return handleBody(endPos, "");
        else if (_psHelper.compareTo(startPos, endPos, IMAP4FetchModifier.IMAP4_RFC822HDRChars, true))
        	return handleBody(endPos, IMAP4BodyPartFetchModifier.IMAP4BodyHeaders);
        else if (_psHelper.compareTo(startPos, endPos, IMAP4FetchModifier.IMAP4_RFC822TEXTChars, true))
        	return handleBody(endPos, IMAP4BodyPartFetchModifier.IMAP4BodyText);

        // this point is reached for BODY[xxx] responses
        int	modRange=(endPos - startPos), maxIndex=_psHelper.getMaxIndex();
        // make sure modifier start is AT LEAST as long as the "BODY" string
        if (modRange <= IMAP4FetchModifier.IMAP4_BODYChars.length)
        	return (-671);

        // make sure sub-modifier starts with bracket
        int	subModStart=_psHelper.indexOf(IMAP4Protocol.IMAP4_BRCKT_SDELIM, startPos + IMAP4FetchModifier.IMAP4_BODYChars.length);
        if ((subModStart <= startPos) || (subModStart >= maxIndex))
        	return (-672);
        subModStart++;	// skip '['

        // find end of brackets
        int	subModEnd=_psHelper.indexOf(IMAP4Protocol.IMAP4_BRCKT_EDELIM, subModStart);
        if ((subModEnd < subModStart) || (subModEnd >= maxIndex))
        {
        	// if not found, re-fill parse buffer - hopefully it will be there
        	if (null == (_psHelper=reReadFetchBufferLine(subModStart, false)))
        		return (-673);
        	
        	subModStart = _psHelper.getStartIndex();
        	maxIndex = _psHelper.getMaxIndex();
        	
        	// now that we re-filled the buffer, we fully expect to find the ']'
        	if (((subModEnd=_psHelper.indexOf(IMAP4Protocol.IMAP4_BRCKT_EDELIM, subModStart)) < subModStart) || (subModEnd > maxIndex))
        		return (-674);
        }

        // get only the 1st sub-modifier value (e.g. "HEADER.FIELDS (...)" => HEADER.FIELDS)
        int	effEnd=_psHelper.findNonEmptyDataEnd(subModStart, subModEnd);
        if ((effEnd < subModStart) || (effEnd >= subModEnd))
        	effEnd = subModEnd;

        return handleBody(subModEnd+1, _psHelper.substring(subModStart, effEnd));
	}
	// calculated value
	private static final int MAX_MODIFIER_CHARS_NUM=
		IMAP4FetchModifier.IMAP4_BODYPEEKChars.length + 1 + IMAP4BodyFetchModifier.IMAP4BodyNotHeaderFields.length() + 1;
	/*
	 * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#handleUntaggedResponse(net.community.chest.ParsableString, int)
	 */
	@Override
	public int handleUntaggedResponse (final ParsableString ps, final int startPos) throws IOException
	{
		int	startIndex=_psHelper.getStartIndex(),
			maxIndex=_psHelper.getMaxIndex(),
			curPos=_psHelper.findNonEmptyDataStart(startPos),
			nextPos=_psHelper.findNonEmptyDataEnd(curPos+1);
		if ((curPos <= (startIndex+1)) || (curPos >= maxIndex) || (nextPos <= curPos) || (nextPos >= maxIndex))
			return 0;   // ignore failure to find un-tagged data start or nothing follows it

		// format is "* n FETCH ..." where 'n' is the message sequence number
		try
		{
			// ignore any non-positive sequence number (should not happen, but ignore it)
			if ((_msgSeqNo=_psHelper.getUnsignedInt(curPos, nextPos)) <= 0)
				return 0;
		}
		catch(NumberFormatException nfe)
		{
			return 0;   // ignore if not an unsigned integer
		}

		// ignore failure to find un-tagged FETCH modifier response
		curPos = _psHelper.findNonEmptyDataStart(nextPos+1);
		nextPos = _psHelper.findNonEmptyDataEnd(curPos+1);
		if ((curPos <= (startIndex+1)) || (curPos >= maxIndex) || (nextPos <= curPos) || (nextPos >= maxIndex))
			return 0;
		if (!_psHelper.compareTo(curPos, nextPos, IMAP4Protocol.IMAP4FetchCmdChars, true))
			return 0;

		// if could not find first character AFTER the FETCH response, then assume not really a FETCH response
		curPos = _psHelper.findNonEmptyDataStart(nextPos+1);
		if ((curPos <= nextPos) || (curPos >= maxIndex))
			return 0;

        // ignore if missing start of FETCH results responses (always a list start)
		if (IMAP4Protocol.IMAP4_PARLIST_SDELIM != ps.getCharAt(curPos))
			return 0;

		// inform handler of new message results start
		int nErr=_rspHandler.handleMsgResponseState(_msgSeqNo, true);
		if (nErr != 0)
			return nErr;

		// start parsing responses till found matching ')'
		for (curPos++; _psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_EDELIM; startIndex=_psHelper.getStartIndex(), maxIndex=_psHelper.getMaxIndex())
		{
			if ((curPos=ensureParseBufferData(curPos, MAX_MODIFIER_CHARS_NUM)) < 0)
				return curPos;
			if ((nextPos=ensureParseBufferDataEnd(curPos+1)) < 0)
				return nextPos;

			// "nextPos" MUST be LESS than length, since ALL responses contain a modifier followed by at LEAST one argument
			if ((curPos >= 0) && (curPos < nextPos) && (nextPos < (maxIndex=_psHelper.getMaxIndex())))
			{
				if ((curPos=handleModifier(curPos, nextPos)) < 0)
					return curPos;
			
				// re-synchronize on next token (if any) 
				if ((curPos=ensureParseBufferData(curPos, MAX_MODIFIER_CHARS_NUM)) < 0)
					return curPos;
			}
			else    // ran out of data, so re-fill it
			{
				if (null == (_psHelper=reReadFetchBufferLine(curPos, false)))
					return (-791);

				curPos = _psHelper.getStartIndex();
			}
		}

		if ((nErr=skipTillCRLF(true)) != 0)
			return nErr;

		// inform handler of new message results ending
		if ((nErr=_rspHandler.handleMsgResponseState(_msgSeqNo, false)) != 0)
			return nErr;

		return 0;
	}
}

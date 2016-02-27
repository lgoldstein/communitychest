package net.community.chest.net.proto.text.imap4;

import net.community.chest.mail.address.MessageAddressType;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>This interface is used to report intermediate FETCH response parsing
 * results. In general, if a non-zero return value is returned, then parsing
 * stops immediately (causing an {@link java.io.IOException} at the "fetch" call).
 * <B>Note</B>: the results of stoping the parsing in this way are undefined !!!</P>
 * 
 * @author Lyor G.
 * @since Sep 20, 2007 10:13:06 AM
 */
public interface IMAP4FetchResponseHandler {
	/**
	 * Called to inform about the start/end of a sequence of calls regarding fetched information about the specified message.
	 * @param msgSeqNo message sequence number
	 * @param fStarting if TRUE, then sequence of calls is about to start, else it has ended (and a new message may start)
	 * @return 0 if OK to continue parsing
	 */
	int handleMsgResponseState (int msgSeqNo, boolean fStarting);
	/**
	 * Called to handle the UID response value
	 * @param msgSeqNo message sequence number
	 * @param msgUID UID value
	 * @return 0 if OK to continue parsing
	 */
	int handleUID (int msgSeqNo, long msgUID);
	/**
	 * Special message part ID used to report top-level message ENVELOPE related information
	 */
	public static final String ENVELOPE_MSG_PART_ID="0";
		public static final char[] ENVELOPE_MSG_PART_IDChars=ENVELOPE_MSG_PART_ID.toCharArray();
    /**
     * Called to inform about the start/end of a message part data stage
     * @param msgSeqNo message sequence number
     * @param msgPart shows the sub-part for which the data is starting/ending - if non-"0" then it is an embedded message
     * @param fStarting if TRUE, then responses are about to start, otherwise they have ended
     * @return 0 if OK to continue parsing
     */
	int handleMsgPartStage (int msgSeqNo, String msgPart, boolean fStarting);
	/**
	 * Called to inform about the size (in octets) of a message part
     * @param msgSeqNo message sequence number
     * @param msgPart shows the sub-part for which the size is reported - if "0" then it is the total message size
     * @param partSize size (in octets) of reported message part
     * @return 0 if OK to continue parsing
	 */
	int handleMsgPartSize (int msgSeqNo, String msgPart, long partSize);
	/**
	 * Called to inform about a message part header data
	 * @param msgSeqNo message sequence number
	 * @param msgPart if non-"0" then shows the sub-part for which the header is supplied - used
	 * mainly for embedded messages. Otherwise, it is the ENVELOPE of the main message
	 * @param hdrName name of header (Note: including a terminating ':')
	 * @param attrName name of a sub-attribute (may be NULL/empty for headers that have no sub-attribute)
	 * @param attrValue header attribute value data (may be null/empty for attributes that have only a name, but no value)
	 * @return 0 if OK to continue parsing
	 */
	int handleMsgPartHeader (int msgSeqNo, String msgPart, String hdrName, String attrName, String attrValue);
	/**
	 * Called to inform about an envelope address data
	 * @param msgSeqNo message sequence number
	 * @param msgPart if non-"0" then shows the sub-part for which the address is supplied - used
	 * mainly for embedded messages. Otherwise, it is the ENVELOPE of the main message
	 * @param addrType address type ("From:/To:/...")
	 * @param dispName display name part of the address (may be null/empty)
	 * @param addrVal e-mail address part
	 * @return 0 if OK to continue parsing
	 */
	int handleMsgPartAddress (int msgSeqNo, String msgPart, MessageAddressType addrType, String dispName, String addrVal);
	/**
	 * Called to inform about the start of a flags stage
	 * @param msgSeqNo message sequence number
	 * @param fStarting if TRUE, then FLAGS responses are about to start, otherwise they have ended
	 * @return 0 if OK to continue parsing
	 */
	int handleFlagsStage (int msgSeqNo, boolean fStarting);
    /**
     * Called to inform about a flag value
     * @param msgSeqNo message sequence number
     * @param flagValue flag value string
     * @return 0 if OK to continue parsing
     */
	int handleFlagValue (int msgSeqNo, String flagValue);
    /**
     * Called to inform about an INTERNALDATE value
     * @param msgSeqNo message sequence number
     * @param dateValue date/time value as received from the server - see RFC2060 as to its format
     * @return 0 if OK to continue parsing
     */
	int handleInternalDate (int msgSeqNo, String dateValue);
    /**
     * Called to inform about the start/end of a message part data
     * @param msgSeqNo message sequence number
     * @param msgPart if non-"0" then shows the sub-part for which the data is starting/ending
     * @param fStarting if TRUE, then data calls are about to start, otherwise they have ended
     * @return 0 if OK to continue parsing
     */
	int handlePartDataStage (int msgSeqNo, String msgPart, boolean fStarting);
	/**
	 * Called to inform about a buffer of data belonging to a message part
	 * @param msgSeqNo message sequence number
	 * @param msgPart if non-"0" then shows the sub-part for which the data is starting/ending
	 * @param bData data buffer in which returned data resides
	 * @param nOffset offset in data buffer where message part data starts
	 * @param nLen size of valid data in buffer (starting at specified offset)
	 * @return 0 if OK to continue parsing
	 */
	int handlePartData (int msgSeqNo, String msgPart, byte[] bData, int nOffset, int nLen);
}

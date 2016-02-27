package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import net.community.chest.net.proto.text.TextProtocolNetConnection;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 9:53:52 AM
 */
public interface IMAP4Accessor extends TextProtocolNetConnection {
	/**
	 * Performs login using plaintext username and password
	 * @param username username to be used for plaintext authentication
	 * @param password password to be used for plaintext authentication
	 * @return resulting IMAP4 tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse login (String username, String password) throws IOException;
	/**
	 * Logs out the user - Note: also closes the connection, so calling "close" has no other effect
	 * @return tagged response
	 * @throws IOException if unable to complete command - Note: it is highly recommended to call "close" anyway
	 */
	IMAP4TaggedResponse logout () throws IOException;
	/**
	 * Checks the protocol liveness
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse noop () throws IOException;
	/**
	 * Returns the reported server capabilities
	 * @return capabilities report (or un-successful tagged response)
	 * @throws IOException if unable to complete command
	 */
	IMAP4Capabilities capability () throws IOException;
	/**
	 * Issues the NAMESPACE command
	 * @return name spaces (if OK response, and reported capability includes this)
	 * @throws IOException if unable to complete command
	 */
	IMAP4NamespacesInfo namespace () throws IOException;
	/**
	 * @param rootName virtual root to be used to query the quota - null/empty == INBOX
	 * @return QUOTAROOT information using specified folder as root
	 * @throws IOException if unable to complete command
	 */
	IMAP4QuotarootInfo getquotaroot (String rootName) throws IOException;
	/**
	 * @return QUOTAROOT information (using INBOX as default)
	 * @throws IOException if unable to complete command
	 */
	IMAP4QuotarootInfo getquotaroot () throws IOException;
	/**
	 * Selects the current folder for which subsequent messages command refer
	 * @param folder folder path to be selected
	 * @param getFullInfo if TRUE then selection response are parsed and full contents of the response are returned.
	 * Otherwise, only the OK/BAD/NO part are filled, and all untagged responses are ignored
	 * @return selection info (depending on the "getFullInfo" state)
	 * @throws IOException if cannot complete the action
	 */
	IMAP4FolderSelectionInfo select (String folder, boolean getFullInfo) throws IOException;
	/**
	 * Selects the current folder for which subsequent messages command refer
	 * @param folder folder path to be selected
	 * @return full selection info
	 * @throws IOException if cannot complete the action
	 */
	IMAP4FolderSelectionInfo select (String folder) throws IOException;
	/**
	 * Selects the current folder for which READ-ONLY subsequent messages commands refer
	 * @param folder folder path to be selected
	 * @param getFullInfo if TRUE then selection response are parsed and full contents of the response are returned.
	 * Otherwise, only the OK/BAD/NO part are filled, and all untagged responses are ignored
	 * @return selection info (depending on the "getFullInfo" state)
	 * @throws IOException if cannot complete the action
	 */
	IMAP4FolderSelectionInfo examine (String folder, boolean getFullInfo) throws IOException;
	/**
	 * Selects the current folder for which READ-ONLY subsequent messages commands refer
	 * @param folder folder path to be selected
	 * @return full selection info
	 * @throws IOException if cannot complete the action
	 */
	IMAP4FolderSelectionInfo examine (String folder) throws IOException;
	/**
	 * Returns to un-selected state - i.e., no current folder
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse unselect () throws IOException;
	/**
	 * Creates the specified folder (see RFC2060 for limitiations and behavior)
	 * @param folder name/path of folder to be created
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse create (String folder) throws IOException;
	/**
	 * Deletes the specified folder (see RFC2060 for limitiations and behavior)
	 * @param folder name/path of folder to be deleted
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse delete (String folder) throws IOException;
	/**
	 * Renames the specified folder (see RFC2060 for limitiations and behavior)
	 * @param srcFolder original/old path of folder to be renamed
	 * @param dstFolder new path of folder to be renamed
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse rename (String srcFolder, String dstFolder) throws IOException;
	/**
	 * Deletes all messages marked for deletion in the currently selected folder
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse expunge () throws IOException;
	/**
	 * Changes the state flags of the specified message range
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @param isUID if TRUE then this range specifies UIDs rather than sequence numbers
	 * @param flags flags to be added/removed/set (may be NULL/empty)
	 * @param addRemoveSet if >0 then flags are added, <0 removed, and =0 set
	 * @return tagged response - Note: all message flags commands are executed using ".SILENT" mode
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse changeMsgFlags (String msgRange, boolean isUID, IMAP4MessageFlag[] flags, int addRemoveSet) throws IOException;
	/**
	 * Changes the state flags of the specified message numbers range
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @param flags flags to be added/removed/set (may be NULL/empty)
	 * @param addRemoveSet if >0 then flags are added, <0 removed, and =0 set
	 * @return tagged response - Note: all message flags commands are executed using ".SILENT" mode
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse store (String msgRange, IMAP4MessageFlag[] flags, int addRemoveSet) throws IOException;
	/**
	 * Changes the state flags of the specified message UID range
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @param flags flags to be added/removed/set (may be NULL/empty)
	 * @param addRemoveSet if >0 then flags are added, <0 removed, and =0 set
	 * @return tagged response - Note: all message flags commands are executed using ".SILENT" mode
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse storeUID (String msgRange, IMAP4MessageFlag[] flags, int addRemoveSet) throws IOException;
	/**
	 * Marks the specified messages range "\Deleted" flag state
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @param isUID if TRUE then this range specifies UIDs rather than sequence numbers
	 * @param markThem if TRUE, then messages are marked with the "\Deleted" state, otherwise they are un-marked as such
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse markDel (String msgRange, boolean isUID, boolean markThem) throws IOException;
	/**
	 * Marks the specified messages numbers range with the "\Deleted" flag state
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse delMsgs (String msgRange) throws IOException;
	/**
	 * Marks the specified messages UID range with the "\Deleted" flag state
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse delUIDMsgs (String msgRange) throws IOException;
	/**
	 * Removes the "\Deleted" flag state from the specified messages numbers range
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse undelMsgs (String msgRange) throws IOException;
	/**
	 * Removes the "\Deleted" flag state from the specified messages UID range
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse undelUIDMsgs (String msgRange) throws IOException;
	/**
	 * Marks the specified messages range "\Seen" flag state
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @param isUID if TRUE then this range specifies UIDs rather than sequence numbers
	 * @param markThem if TRUE, then messages are marked with the "\Deleted" state, otherwise they are un-marked as such
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse markSeen (String msgRange, boolean isUID, boolean markThem) throws IOException;
	/**
	 * Marks the specified messages numbers range "\Seen" flag state
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse seenMsgs (String msgRange) throws IOException;
	/**
	 * Marks the specified messages UID range "\Seen" flag state
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse seenUIDMsgs (String msgRange) throws IOException;
	/**
	 * Removes the "\Seen" flag state from the specified messages numbers range
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse unseenMsgs (String msgRange) throws IOException;
	/**
	 * Removes the "\Seen" flag state from the specified messages UID range
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse unseenUIDMsgs (String msgRange) throws IOException;
	/**
	 * Lists all folders matching the specified reference and mailbox (see RFC2060 for syntax)
	 * @param ref reference (according to RFC2060)
	 * @param mbox mailbox (according to RFC2060)
	 * @return reference information (according to RFC2060)
	 * @throws IOException if unable to complete command - Note: some folders may be
	 * available even if got a BAD/NO response, representing whatever has been parsed
	 * until the IOException
	 */
	IMAP4FoldersListInfo list (String ref, String mbox) throws IOException;
	/**
	 * Lists all sub-folders (including indirect descendents) of specified folder
	 * @param folder folder path whose sub-folders are requested (if null/empty the lists ALL folders)
	 * @param chSep separator to be used to build the sub-folders reference if not all folders requested (see RFC2060 LIST command)
	 * @return folder information (if OK tagged response)
	 * @throws IOException if unable to complete command
	 */
	IMAP4FoldersListInfo listSubFolders (String folder, char chSep) throws IOException;
	/**
	 * Lists all known folders (including indirect descendents)
	 * @return folder information (if OK tagged response)
	 * @throws IOException if unable to complete command
	 */
	IMAP4FoldersListInfo listAllFolders () throws IOException;
	/**
	 * Subscribes to all folders matching the specified reference and mailbox (see RFC2060 for syntax)
	 * @param ref reference (according to RFC2060)
	 * @param mbox mailbox (according to RFC2060)
	 * @return reference information (according to RFC2060)
	 * @throws IOException if unable to complete command
	 */
	IMAP4FoldersListInfo lsub (String ref, String mbox) throws IOException;
	/**
	 * Returns the full status of the specified folder
	 * @param folder folder whose status is requested
	 * @return folder status (if OK tagged response)
	 * @throws IOException if unable to complete command
	 */
	IMAP4StatusInfo status (String folder) throws IOException;
	/**
	 * Fetches the specified information for the messages in the specified range
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @param isUID if TRUE then this range specifies UIDs rather than sequence numbers
	 * @param mods modifiers string specifying the information to be fetched - see RFC2060 as to the format
	 * @param rspHandler handler for the intermediate results
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse fetchMsgsInfo (char[] msgRange, boolean isUID, char[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException;
	/**
	 * Fetches the specified information for the messages in the specified range
	 * @param tagValue command tag value assumed to appear in the command line
	 * @param cmdLine command line - including specified tag value, and terminating CRLF
	 * @param lineLen number of valid characters in the command line buffer
	 * @param rspHandler handler for the intermediate results
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse fetchFinalResponse (int tagValue, char[] cmdLine, int lineLen, IMAP4FetchResponseHandler rspHandler) throws IOException;
	/**
	 * Fetches the specified information for the messages in the specified range
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @param isUID if TRUE then this range specifies UIDs rather than sequence numbers
	 * @param mods modifiers string specifying the information to be fetched - see RFC2060 as to the format
	 * @param rspHandler handler for the intermediate results
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse fetchMsgsInfo (String msgRange, boolean isUID, String mods, IMAP4FetchResponseHandler rspHandler) throws IOException;
	/**
	 * Fetches the specified information for the messages in the specified range
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @param isUID if TRUE then this range specifies UIDs rather than sequence numbers
	 * @param mods modifiers specifying the information to be fetched
	 * @param rspHandler handler for the intermediate results
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse fetchMsgsInfo (String msgRange, boolean isUID, IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException;
	/**
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @param isUID if TRUE then this range specifies UIDs rather than sequence numbers
	 * @return tagged response + extracted information (if any)
	 * @throws IOException if unable to complete command
	 */
	IMAP4FastResponse fetchFastMsgsInfo (String msgRange, boolean isUID) throws IOException;
	/**
	 * @param useUID if TRUE then retrieve UID(s) as well as sequence numbers
	 * @return tagged response + extracted information (if any)
	 * @throws IOException if unable to complete command
	 */
	IMAP4FastResponse fetchFastMsgsAllInfo (boolean useUID) throws IOException;
	/**
	 * Fetches the specified information for the messages in the specified numbers range
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @param mods modifiers specifying the information to be fetched
	 * @param rspHandler handler for the intermediate results
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse fetch (String msgRange, IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException;
	/**
	 * Fetches the specified information for the messages in the specified UID range
	 * @param msgRange messages affected by the command (see RFC2060 for allowed formats)
	 * @param mods modifiers specifying the information to be fetched
	 * @param rspHandler handler for the intermediate results
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse fetchUID (String msgRange, IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException;
	/**
	 * Fetches the specified information for the specified message
	 * @param msgId ID of message affected by the command - the actual meaning (UID/number) depenas on the "isUID" state
	 * @param isUID if TRUE then this number specifies a UID rather than a sequence number
	 * @param mods modifiers specifying the information to be fetched
	 * @param rspHandler handler for the intermediate results
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse fetchMsgInfo (long msgId, boolean isUID, IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException;
	/**
	 * Fetches the specified information for the specified message number
	 * @param seqNo sequence number of message affected by the command
	 * @param mods modifiers specifying the information to be fetched
	 * @param rspHandler handler for the intermediate results
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse fetch (long seqNo, IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException;
	/**
	 * Fetches the specified information for the specified message number
	 * @param msgUID UID of message affected by the command
	 * @param mods modifiers specifying the information to be fetched
	 * @param rspHandler handler for the intermediate results
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse fetchUID (long msgUID, IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException;
	/**
	 * Fetches the specified information for ALL the messages in the currently selected folder
	 * @param useUID if TRUE, then uses a UID modifier
	 * @param mods modifiers specifying the information to be fetched
	 * @param rspHandler handler for the intermediate results
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse fetchAllMsgs (boolean useUID, IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException;
	/**
	 * Fetches the specified information for all the messages in the currently selected folder using sequence numbers
	 * @param mods modifiers specifying the information to be fetched
	 * @param rspHandler handler for the intermediate results
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse fetchAll (IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException;
	/**
	 * Fetches the specified information for all the messages in the currently selected folder using UID modifier
	 * @param mods modifiers specifying the information to be fetched
	 * @param rspHandler handler for the intermediate results
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse fetchUIDAll (IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException;
	/**
	 * Retrieves the specified message part raw data an copies it into the specified output stream
	 * @param msgId message ID whose raw data is to be retrieved
	 * @param isUID if TRUE then the message ID represents a UID value, otherwise this is a sequence number
	 * @param msgPart message part to be fetched (formatted according to RFC2060 rules) - if null/empty then entire message data is fetched
	 * @param os output stream to write into
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse fetchMsgPartRawData (long msgId, boolean isUID, String msgPart, OutputStream os) throws IOException;
	/**
	 * Retrieves the specified message raw data an copies it into the specified output stream
	 * @param msgId message ID whose raw data is to be retrieved
	 * @param isUID if TRUE then the message ID represents a UID value, otherwise this is a sequence number
	 * @param os output stream to write into
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse dumpRawMsgData (long msgId, boolean isUID, OutputStream os) throws IOException;
	/**
	 * Executes and returns the result(s) of a (UID) SEARCH command
	 * @param isUID if TRUE then returned results (if any) signify UID(s) of matching messages, otherwise these
	 * are sequence numbers
	 * @param condition condition string for searching - see RFC2060 for syntax
	 * @param maxResults maxium results to return - if (<0), then ALL results, otherwise up to specified maximum results
	 * are returned (Note: ZERO is invalid and will cause an exception
	 * @return search results (if OK response) - Note: may return an empty set
	 * @throws IOException if unable to complete command
	 */
	IMAP4SearchResponse search (boolean isUID, String condition, int maxResults) throws IOException;
	/**
	 * Executes and returns ALL the result(s) of a (UID) SEARCH command
	 * @param isUID if TRUE then returned results (if any) signify UID(s) of matching messages, otherwise these
	 * are sequence numbers
	 * @param condition condition string for searching - see RFC2060 for syntax
	 * are returned (Note: ZERO is invalid and will cause an exception
	 * @return search results (if OK response) - Note: may return an empty set
	 * @throws IOException if unable to complete command
	 */
	IMAP4SearchResponse searchAllMsgs (boolean isUID, String condition) throws IOException;
	/**
	 * Executes and returns ALL the result(s) of a sequence numbers SEARCH command
	 * @param condition condition string for searching - see RFC2060 for syntax
	 * are returned (Note: ZERO is invalid and will cause an exception
	 * @return search results (if OK response) - Note: may return an empty set
	 * @throws IOException if unable to complete command
	 */
	IMAP4SearchResponse searchAll (String condition) throws IOException;
	/**
	 * Executes and returns ALL the result(s) of a UID SEARCH command
	 * @param condition condition string for searching - see RFC2060 for syntax
	 * are returned (Note: ZERO is invalid and will cause an exception
	 * @return search results (if OK response) - Note: may return an empty set
	 * @throws IOException if unable to complete command
	 */
	IMAP4SearchResponse searchAllUID (String condition) throws IOException;
	/**
	 * APPENDs the given data to the specified folder
	 * @param folder folder name/path where data is to be APPEND-ed
	 * @param iDate INTERNALDATE value to be associated - formatted according to RFC2060 requirements (may be NULL/empty)
	 * @param flags flags values to be set for the data - formatted acccording to RFC2060 requirements (may be NULL/empty)
	 * @param dataSize (exactly) expected total data size (in bytes)
	 * @param prov actual data provider
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse append (String folder, String iDate, String flags, long dataSize, IMAP4AppendDataProvider prov) throws IOException;
	/**
	 * APPENDs the given data to the specified folder
	 * @param folder folder name/path where data is to be APPEND-ed
	 * @param iDate INTERNALDATE value to be associated with the data (may be NULL/empty)
	 * @param flags flags values to be set for the data (may be NULL/empty)
	 * @param dataSize (exactly) expected total data size (in bytes)
	 * @param prov actual data provider
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse append (String folder, Calendar iDate, IMAP4MessageFlag[] flags, long dataSize, IMAP4AppendDataProvider prov) throws IOException;
	/**
	 * APPENDs the given data to the specified folder
	 * @param folder folder name/path where data is to be APPEND-ed
	 * @param iDate INTERNALDATE value to be associated - formatted according to RFC2060 requirements (may be NULL/empty)
	 * @param flags flags values to be set for the data - formatted acccording to RFC2060 requirements (may be NULL/empty)
	 * @param in input stream from which to read (MIME formatted) data - Note:
	 * stream is not closed on end of append
	 * @param dataSize (exactly) expected total data size (in bytes)
	 * @param copyBufSize size of data buffer (bytes) to be used for copying the data from the file
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse appendData (String folder, String iDate, String flags, InputStream in, long dataSize, int copyBufSize) throws IOException;
	/**
	 * APPENDs the given data to the specified folder
	 * @param folder folder name/path where data is to be APPEND-ed
	 * @param iDate INTERNALDATE value to be associated with the data (may be NULL/empty)
	 * @param flags flags values to be set for the data (may be NULL/empty)
	 * @param in input stream from which to read (MIME formatted) data - Note:
	 * stream is not closed on end of append
	 * @param dataSize (exactly) expected total data size (in bytes)
	 * @param copyBufSize size of data buffer (bytes) to be used for copying the data from the file
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse appendData (String folder, Calendar iDate, IMAP4MessageFlag[] flags, InputStream in, long dataSize, int copyBufSize) throws IOException;
	/**
	 * APPENDs the given data to the specified folder
	 * @param folder folder name/path where data is to be APPEND-ed
	 * @param iDate INTERNALDATE value to be associated - formatted according to RFC2060 requirements (may be NULL/empty)
	 * @param flags flags values to be set for the data - formatted acccording to RFC2060 requirements (may be NULL/empty)
	 * @param filePath path of a file containing the data to be appended (formatted according to MIME requirements)
	 * @param copyBufSize size of data buffer (bytes) to be used for copying the data from the file
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse appendFile (String folder, String iDate, String flags, String filePath, int copyBufSize) throws IOException;
	/**
	 * APPENDs the given data to the specified folder
	 * @param folder folder name/path where data is to be APPEND-ed
	 * @param iDate INTERNALDATE value to be associated with the data (may be NULL/empty)
	 * @param flags flags values to be set for the data (may be NULL/empty)
	 * @param filePath path of a file containing the data to be appended (formatted according to MIME requirements)
	 * @param copyBufSize size of data buffer (bytes) to be used for copying the data from the file
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse appendFile (String folder, Calendar iDate, IMAP4MessageFlag[] flags, String filePath, int copyBufSize) throws IOException;
	/**
	 * Used to transfer (COPY/MOVE-if supported) messages from current folder to the destination
	 * @param cmd command to be used for transferring the messages - Note: currently, RFC2060 defines only COPY, but
	 * some servers implement proprietary MOVE commands with improved efficiency
	 * @param msgRange messages range to be transferred - encoded accroding to RFC2060 specifications
	 * @param isUID if TRUE then specified messages range is a UID range
	 * @param dstFolder destination folder to transfer the messages to
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse xferMsgs (char[] cmd, String msgRange, boolean isUID, String dstFolder) throws IOException;
	/**
	 * Copies specified messages from current folder to the destination folder
	 * @param msgRange messages range to be transferred - encoded accroding to RFC2060 specifications
	 * @param isUID if TRUE then specified messages range is a UID range
	 * @param dstFolder destination folder to transfer the messages to
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse copyMsgs (String msgRange, boolean isUID, String dstFolder) throws IOException;
	/**
	 * Copies specified messages sequence numbers from current folder to the destination folder
	 * @param msgRange messages range to be transferred - encoded accroding to RFC2060 specifications
	 * @param dstFolder destination folder to transfer the messages to
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse copy (String msgRange, String dstFolder) throws IOException;
	/**
	 * Copies specified messages UID numbers from current folder to the destination folder
	 * @param msgRange messages range to be transferred - encoded accroding to RFC2060 specifications
	 * @param dstFolder destination folder to transfer the messages to
	 * @return tagged response
	 * @throws IOException if unable to complete command
	 */
	IMAP4TaggedResponse copyUID (String msgRange, String dstFolder) throws IOException;
}

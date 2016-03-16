package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.Calendar;

import net.community.chest.net.proto.text.AbstractTextProtocolNetConnectionHelper;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 1:41:53 PM
 */
public abstract class AbstractIMAP4Accessor extends AbstractTextProtocolNetConnectionHelper implements IMAP4Accessor {
    protected AbstractIMAP4Accessor ()
    {
        super();
    }
    /*
     * @see net.community.chest.net.proto.ProtocolNetConnection#getDefaultPort()
     */
    @Override
    public int getDefaultPort ()
    {
        return IMAP4Protocol.IPPORT_IMAP4;
    }
    /**
     * Executes a command for which only the tagged response is of interest - any un-tagged intermediate responses are ignored
     * @param tagValue tag value to be used for executing the specified command
     * @param cmd command to be executed
     * @param arg argument for the command - if null/empty then not used
     * @return tagged response
     * @throws IOException if unable to complete command
     */
    protected abstract IMAP4TaggedResponse doSimpleCommand (int tagValue, char[] cmd, char[] arg) throws IOException;
    /**
     * Executes a command for which only the tagged response is of interest - any un-tagged intermediate responses are ignored
     * @param tagValue tag value to be used for executing the specified command
     * @param cmd command to be executed - Note: this may be a full line (no CRLF !!!)
     * @return tagged response
     * @throws IOException if unable to complete command
     */
    protected IMAP4TaggedResponse doSimpleCommand (int tagValue, char[] cmd) throws IOException
    {
        return doSimpleCommand(tagValue, cmd, (char[]) null);
    }
    /**
     * Executes a command for which only the tagged response is of interest - any un-tagged intermediate responses are ignored
     * @param tagValue tag value to be used for executing the specified command
     * @param cmd command to be executed - Note: this may be a full line (no CRLF !!!)
     * @return tagged response
     * @throws IOException if unable to complete command
     */
    public IMAP4TaggedResponse doSimpleCommand (int tagValue, String cmd) throws IOException
    {
        return doSimpleCommand(tagValue, (null == cmd) ? (char[]) null : cmd.toCharArray());
    }
    /**
     * Executes a command for which only the tagged response is of interest - any un-tagged intermediate responses are ignored
     * @param tagValue tag value to be used for executing the specified command
     * @param cmd command to be executed
     * @param arg argument for the command - if null/empty then not used
     * @return tagged response
     * @throws IOException if unable to complete command
     */
    protected IMAP4TaggedResponse doSimpleCommand (int tagValue, String cmd, String arg) throws IOException
    {
        return doSimpleCommand(tagValue, (null == cmd) ? (char[]) null : cmd.toCharArray(), (null == arg) ? (char[]) null : arg.toCharArray());
    }
    /**
     * @return a random NON-NEGATIVE integer tag value to be used internally
     */
    protected abstract int getAutoTag ();
    /**
     * Makes sure that the login credential (login/password) is non-empty and does not contain characters that
     * may confuse the IMAP4 protocol (e.g. quote, CR, LF
     * @param lc login credential to be checked
     * @return TRUE if valid login credential
     */
    protected static final boolean validateLoginCredential (final CharSequence lc)
    {
        final int    lcLen=(null == lc) ? 0 : lc.length();
        if (lcLen <= 0)
            return false;

        for (int    index=0; index < lcLen; index++)
        {
            final char    c=lc.charAt(index);
            if ((IMAP4Protocol.IMAP4_QUOTE_DELIM == c) || ('\r' == c) || ('\n' == c))
                return false;
        }

        return true;
    }
    /**
     * Appends the login username/password (after validation) to the string
     * buffer - Note: checks that username/password do not contain any
     * characters that may cause IMAP4 protocol problems.
     * @param sb The {@link Appendable} instance to append to - if null then
     * nothig is appended
     * @param username username to be used - must pass {@link #validateLoginCredential(CharSequence lc)}
     * @param password password to be used - must pass {@link #validateLoginCredential(CharSequence lc)}
     * @return Same instance as input
     * @throws IOException if failed to append values
     */
    protected static final <A extends Appendable> A appendLoginCredentials (final A sb, final String username, final String password) throws IOException
    {
        if (null == sb)
            throw new IOException("appendLoginCredentials() - no " + Appendable.class.getSimpleName() + " instance provided");

        if ((!validateLoginCredential(username)) || (!validateLoginCredential(password)))
            throw new StreamCorruptedException("appendLoginCredentials() bad/illegal username/password format");

        sb.append(IMAP4Protocol.IMAP4_QUOTE_DELIM)
          .append(username)
          .append(IMAP4Protocol.IMAP4_QUOTE_DELIM)
          .append(' ')
          .append(IMAP4Protocol.IMAP4_QUOTE_DELIM)
          .append(password)
          .append(IMAP4Protocol.IMAP4_QUOTE_DELIM)
          ;
        return sb;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#logout()
     */
    @Override
    public IMAP4TaggedResponse logout () throws IOException
    {
        try
        {
            return doSimpleCommand(getAutoTag(), IMAP4Protocol.IMAP4LogoutCmdChars);
        }
        finally
        {
            try
            {
                close();
            }
            catch(IOException ioe)
            {
                // ignore closure error
            }
        }
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#noop()
     */
    @Override
    public IMAP4TaggedResponse noop () throws IOException
    {
        return doSimpleCommand(getAutoTag(), IMAP4Protocol.IMAP4NoopCmdChars);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#getquotaroot()
     */
    @Override
    public IMAP4QuotarootInfo getquotaroot () throws IOException
    {
        return getquotaroot(IMAP4FolderInfo.IMAP4_INBOX);
    }
    /**
     * Focuses the IMAP4 operations on specified folder
     * @param cmd command to be used to switch to specified folder
     * @param folder folder name/path to switch to
     * @param getFullInfo if FALSE then only the tagged response part is updated
     * @return selection information (if successful)
     * @throws IOException if unable to complete command
     * @see IMAP4Protocol#IMAP4SelectCmd
     * @see IMAP4Protocol#IMAP4ExamineCmd
     */
    protected abstract IMAP4FolderSelectionInfo setCurFolder (char[] cmd, String folder, boolean getFullInfo) throws IOException;
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#select(java.lang.String, boolean)
     */
    @Override
    public IMAP4FolderSelectionInfo select (String folder, boolean getFullInfo) throws IOException
    {
        return setCurFolder(IMAP4Protocol.IMAP4SelectCmdChars, folder, getFullInfo);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#select(java.lang.String)
     */
    @Override
    public IMAP4FolderSelectionInfo select (String folder) throws IOException
    {
        return select(folder, true);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#examine(java.lang.String, boolean)
     */
    @Override
    public IMAP4FolderSelectionInfo examine (String folder, boolean getFullInfo) throws IOException
    {
        return setCurFolder(IMAP4Protocol.IMAP4ExamineCmdChars, folder, getFullInfo);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#examine(java.lang.String)
     */
    @Override
    public IMAP4FolderSelectionInfo examine (String folder) throws IOException
    {
        return examine(folder, true);
    }
    /**
     * Executes a command that involves at most 2 folders as arguments (used for CREATE/RENAME/DELETE)
     * @param cmd command to be executed
     * @param srcFolder source folder path
     * @param dstFolder (optional) destination folder path
     * @return tagged response
     * @throws IOException if errors encountered
     */
    protected abstract IMAP4TaggedResponse doFolderCmd (char[] cmd, String srcFolder, String dstFolder) throws IOException;
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#create(java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse create (String folder) throws IOException
    {
        return doFolderCmd(IMAP4Protocol.IMAP4CreateCmdChars, folder, null);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#delete(java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse delete (String folder) throws IOException
    {
        return doFolderCmd(IMAP4Protocol.IMAP4DeleteCmdChars, folder, null);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#rename(java.lang.String, java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse rename (String srcFolder, String dstFolder) throws IOException
    {
        return doFolderCmd(IMAP4Protocol.IMAP4RenameCmdChars, srcFolder, dstFolder);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#unselect()
     */
    @Override
    public IMAP4TaggedResponse unselect () throws IOException
    {
        return doSimpleCommand(getAutoTag(), IMAP4Protocol.IMAP4CloseCmdChars);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#expunge()
     */
    @Override
    public IMAP4TaggedResponse expunge () throws IOException
    {
        return doSimpleCommand(getAutoTag(), IMAP4Protocol.IMAP4XpngCmdChars);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#store(java.lang.String, net.community.chest.net.proto.text.imap4.IMAP4MessageFlag[], int)
     */
    @Override
    public IMAP4TaggedResponse store (String msgRange, IMAP4MessageFlag[] flags, int addRemoveSet) throws IOException
    {
        return changeMsgFlags(msgRange, false, flags, addRemoveSet);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#storeUID(java.lang.String, net.community.chest.net.proto.text.imap4.IMAP4MessageFlag[], int)
     */
    @Override
    public IMAP4TaggedResponse storeUID (String msgRange, IMAP4MessageFlag[] flags, int addRemoveSet) throws IOException
    {
        return changeMsgFlags(msgRange, true, flags, addRemoveSet);
    }
    // helper object for deletion marking
    protected static final IMAP4MessageFlag[] delMarkFlags={ IMAP4MessageFlag.DELETED };
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#markDel(java.lang.String, boolean, boolean)
     */
    @Override
    public IMAP4TaggedResponse markDel (String msgRange, boolean isUID, boolean markThem) throws IOException
    {
        return changeMsgFlags(msgRange, isUID, delMarkFlags, markThem ? (+1) : (-1));
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#delMsgs(java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse delMsgs (String msgRange) throws IOException
    {
        return markDel(msgRange, false, true);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#delUIDMsgs(java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse delUIDMsgs (String msgRange) throws IOException
    {
        return markDel(msgRange, true, true);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#undelMsgs(java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse undelMsgs (String msgRange) throws IOException
    {
        return markDel(msgRange, false, false);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#undelUIDMsgs(java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse undelUIDMsgs (String msgRange) throws IOException
    {
        return markDel(msgRange, true, false);
    }
    // helper object for read/unread marking
    protected static final IMAP4MessageFlag[] seenMarkFlags={ IMAP4MessageFlag.SEEN };
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#markSeen(java.lang.String, boolean, boolean)
     */
    @Override
    public IMAP4TaggedResponse markSeen (String msgRange, boolean isUID, boolean markThem) throws IOException
    {
        return changeMsgFlags(msgRange, isUID, seenMarkFlags, markThem ? (+1) : (-1));
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#seenMsgs(java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse seenMsgs (String msgRange) throws IOException
    {
        return markSeen(msgRange, false, true);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#seenUIDMsgs(java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse seenUIDMsgs (String msgRange) throws IOException
    {
        return markSeen(msgRange, true, true);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#unseenMsgs(java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse unseenMsgs (String msgRange) throws IOException
    {
        return markSeen(msgRange, false, false);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#unseenUIDMsgs(java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse unseenUIDMsgs (String msgRange) throws IOException
    {
        return markSeen(msgRange, true, false);
    }
    /**
     * Retrieves the specified mailbox reference information using the supplied command
     * @param cmd command to be used (LIST/LSUB - for now)
     * @param ref reference (according to RFC2060)
     * @param mbox mailbox (according to RFC2060)
     * @return reference information (according to RFC2060)
     * @throws IOException if unable to complete command
     * @see IMAP4FoldersListInfo
     */
    protected abstract IMAP4FoldersListInfo getMbRef (char[] cmd, String ref, String mbox) throws IOException;
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#list(java.lang.String, java.lang.String)
     */
    @Override
    public IMAP4FoldersListInfo list (String ref, String mbox) throws IOException
    {
        return getMbRef(IMAP4Protocol.IMAP4ListCmdChars, ref, mbox);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#listSubFolders(java.lang.String, char)
     */
    @Override
    public IMAP4FoldersListInfo listSubFolders (String folder, char chSep) throws IOException
    {
        return list("", (((null == folder) || (folder.length() <= 0)) ? "*" : folder + chSep + "*"));
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#listAllFolders()
     */
    @Override
    public IMAP4FoldersListInfo listAllFolders () throws IOException
    {
        return list("", "*");
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#lsub(java.lang.String, java.lang.String)
     */
    @Override
    public IMAP4FoldersListInfo lsub (String ref, String mbox) throws IOException
    {
        return getMbRef(IMAP4Protocol.IMAP4LSUBCmdChars, ref, mbox);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetchMsgsInfo(java.lang.String, boolean, java.lang.String, net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler)
     */
    @Override
    public IMAP4TaggedResponse fetchMsgsInfo (String msgRange, boolean isUID, String mods, IMAP4FetchResponseHandler rspHandler) throws IOException
    {
        return fetchMsgsInfo((null == msgRange) ? (char[]) null : msgRange.toCharArray(), isUID, (null == mods) ? (char[]) null : mods.toCharArray(), rspHandler);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetchFastMsgsInfo(java.lang.String, boolean)
     */
    @Override
    public IMAP4FastResponse fetchFastMsgsInfo (String msgRange, boolean isUID) throws IOException
    {
        final IMAP4FastResponseHandler    rspHandler=new IMAP4FastResponseHandler();
        final IMAP4TaggedResponse        rsp=fetchMsgsInfo(msgRange, isUID, IMAP4FetchModifier.IMAP4_FAST, rspHandler);
        return new IMAP4FastResponse(rsp, rspHandler.getMessages());
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetchFastMsgsAllInfo(boolean)
     */
    @Override
    public IMAP4FastResponse fetchFastMsgsAllInfo (boolean useUID) throws IOException
    {
        return fetchFastMsgsInfo("1:*", useUID);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetch(java.lang.String, net.community.chest.net.proto.text.imap4.IMAP4FetchModifier[], net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler)
     */
    @Override
    public IMAP4TaggedResponse fetch (String msgRange, IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException
    {
        return fetchMsgsInfo(msgRange, false, mods, rspHandler);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetchUID(java.lang.String, net.community.chest.net.proto.text.imap4.IMAP4FetchModifier[], net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler)
     */
    @Override
    public IMAP4TaggedResponse fetchUID (String msgRange, IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException
    {
        return fetchMsgsInfo(msgRange, true, mods, rspHandler);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetchMsgInfo(long, boolean, net.community.chest.net.proto.text.imap4.IMAP4FetchModifier[], net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler)
     */
    @Override
    public IMAP4TaggedResponse fetchMsgInfo (long msgId, boolean isUID, IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException
    {
        return fetchMsgsInfo(Long.toString(msgId), isUID, mods, rspHandler);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetch(long, net.community.chest.net.proto.text.imap4.IMAP4FetchModifier[], net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler)
     */
    @Override
    public IMAP4TaggedResponse fetch (long seqNo, IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException
    {
        return fetchMsgInfo(seqNo, false, mods, rspHandler);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetchUID(long, net.community.chest.net.proto.text.imap4.IMAP4FetchModifier[], net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler)
     */
    @Override
    public IMAP4TaggedResponse fetchUID (long msgUID, IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException
    {
        return fetchMsgInfo(msgUID, true, mods, rspHandler);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetchAllMsgs(boolean, net.community.chest.net.proto.text.imap4.IMAP4FetchModifier[], net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler)
     */
    @Override
    public IMAP4TaggedResponse fetchAllMsgs (boolean useUID, IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException
    {
        return fetchMsgsInfo("1:*", useUID, mods, rspHandler);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetchAll(net.community.chest.net.proto.text.imap4.IMAP4FetchModifier[], net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler)
     */
    @Override
    public IMAP4TaggedResponse fetchAll (IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException
    {
        return fetchAllMsgs(false, mods, rspHandler);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetchUIDAll(net.community.chest.net.proto.text.imap4.IMAP4FetchModifier[], net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler)
     */
    @Override
    public IMAP4TaggedResponse fetchUIDAll (IMAP4FetchModifier[] mods, IMAP4FetchResponseHandler rspHandler) throws IOException
    {
        return fetchAllMsgs(true, mods, rspHandler);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetchMsgPartRawData(long, boolean, java.lang.String, java.io.OutputStream)
     */
    @Override
    public IMAP4TaggedResponse fetchMsgPartRawData (long msgId, boolean isUID, String msgPart, OutputStream os) throws IOException
    {
        if (null == os)
            throw new IMAP4AccessParamsException("No output stream supplied for raw message part dump");

        final IMAP4BodyFetchModifier    rawDataMod=new IMAP4BodyFetchModifier(IMAP4FetchModifier.IMAP4_BODYPEEK, msgPart, true);
        final IMAP4FetchModifier[]         mods={ rawDataMod };
        return fetchMsgInfo(msgId, isUID, mods, new IMAP4RawMsgPartHandler(os, false, IMAP4UntaggedFetchRspHandler.getEffectiveMsgPartId(msgPart)));
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#dumpRawMsgData(long, boolean, java.io.OutputStream)
     */
    @Override
    public IMAP4TaggedResponse dumpRawMsgData (long msgId, boolean isUID, OutputStream os) throws IOException
    {
        return fetchMsgPartRawData(msgId, isUID, null, os);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#searchAllMsgs(boolean, java.lang.String)
     */
    @Override
    public IMAP4SearchResponse searchAllMsgs (boolean isUID, String condition) throws IOException
    {
        return search(isUID, condition, (-1));
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#searchAll(java.lang.String)
     */
    @Override
    public IMAP4SearchResponse searchAll (String condition) throws IOException
    {
        return searchAllMsgs(false, condition);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#searchAllUID(java.lang.String)
     */
    @Override
    public IMAP4SearchResponse searchAllUID (String condition) throws IOException
    {
        return searchAllMsgs(true, condition);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#copyMsgs(java.lang.String, boolean, java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse copyMsgs (String msgRange, boolean isUID, String dstFolder) throws IOException
    {
        return xferMsgs(IMAP4Protocol.IMAP4CopyCmdChars, msgRange, isUID, dstFolder);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#copy(java.lang.String, java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse copy (String msgRange, String dstFolder) throws IOException
    {
        return copyMsgs(msgRange, false, dstFolder);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#copyUID(java.lang.String, java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse copyUID (String msgRange, String dstFolder) throws IOException
    {
        return copyMsgs(msgRange, true, dstFolder);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#appendData(java.lang.String, java.lang.String, java.lang.String, java.io.InputStream, long, int)
     */
    @Override
    public IMAP4TaggedResponse appendData (String folder, String iDate, String flags, InputStream in, long dataSize, int copyBufSize) throws IOException
    {
        return append(folder, iDate, flags, dataSize, new IMAP4StreamAppendDataProvider(in, false, dataSize, copyBufSize));
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#appendData(java.lang.String, java.util.Calendar, net.community.chest.net.proto.text.imap4.IMAP4MessageFlag[], java.io.InputStream, long, int)
     */
    @Override
    public IMAP4TaggedResponse appendData (String folder, Calendar iDate, IMAP4MessageFlag[] flags, InputStream in, long dataSize, int copyBufSize) throws IOException
    {
        return append(folder, iDate, flags, dataSize, new IMAP4StreamAppendDataProvider(in, false, dataSize, copyBufSize));
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#appendFile(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
     */
    @Override
    public IMAP4TaggedResponse appendFile (String folder, String iDate, String flags, String filePath, int copyBufSize) throws IOException
    {
        try(IMAP4FileAppendDataProvider    fadp=new IMAP4FileAppendDataProvider(filePath, copyBufSize)) {
            return append(folder, iDate, flags, fadp.getTotalData(), fadp);
        }
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#appendFile(java.lang.String, java.util.Calendar, net.community.chest.net.proto.text.imap4.IMAP4MessageFlag[], java.lang.String, int)
     */
    @Override
    public IMAP4TaggedResponse appendFile (String folder, Calendar iDate, IMAP4MessageFlag[] flags, String filePath, int copyBufSize) throws IOException
    {
        try(IMAP4FileAppendDataProvider    fadp=new IMAP4FileAppendDataProvider(filePath, copyBufSize)) {
            return append(folder, iDate, flags, fadp.getTotalData(), fadp);
        }
    }
    /*
     * Overloading this function only to make sure network resources are closed on garbage collection
     * @see Object#finalize()
     */
    @Override
    protected void finalize () throws Throwable
    {
        close();
        super.finalize();
    }
}

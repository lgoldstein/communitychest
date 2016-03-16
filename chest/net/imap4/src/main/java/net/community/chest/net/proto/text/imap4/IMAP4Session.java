package net.community.chest.net.proto.text.imap4;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamCorruptedException;

import net.community.chest.io.EOLStyle;
import net.community.chest.lang.StringUtil;
import net.community.chest.net.BufferedTextSocket;
import net.community.chest.net.TextNetConnection;
import net.community.chest.net.proto.text.NetServerWelcomeLine;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 27, 2008 10:19:35 AM
 */
public class IMAP4Session extends AbstractIMAP4AccessorHelper {
    // buffered reader/writer sizes
    private static final int DEFAULT_INPUT_BUFSIZE=2048, DEFAULT_OUTPUT_BUFSIZE=512;
    /*
     * @see net.community.chest.net.proto.text.TextProtocolNetConnection#connect(java.lang.String, int, net.community.chest.net.proto.text.NetServerWelcomeLine)
     */
    @Override
    public void connect (final String host, final int nPort, final NetServerWelcomeLine wl) throws IOException
    {
        TextNetConnection    conn=getTextNetConnection();
        if (conn != null)
            throw new StreamCorruptedException("connect(" + host + "@" + nPort + ") already connected");

        setTextNetConnection(new BufferedTextSocket(DEFAULT_INPUT_BUFSIZE,DEFAULT_OUTPUT_BUFSIZE));
        super.connect(host, nPort);

        if (null == (conn=getTextNetConnection()))
            throw new StreamCorruptedException(ClassUtil.getArgumentsExceptionLocation(getClass(), "connect", host, Integer.valueOf(nPort)) + " no " + TextNetConnection.class.getName() + " instance though created");

        try
        {
            // first line is assumed to be the welcome
            final String    wlString=conn.readLine();
            if (wl != null)
                wl.setLine(wlString);
        }
        catch(IOException ioe)
        {
            conn.close();
            throw ioe;
        }
    }
    /**
     * Ignores any un-tagged responses until the tagged response appears
     * @param tagValue tag value to be hunted for
     * @return tagged response
     * @throws IOException if unable to complete command
     */
    private IMAP4TaggedResponse getFinalResponse (final int tagValue) throws IOException
    {
        final TextNetConnection    conn=getTextNetConnection();
        if (null == conn)
            throw new EOFException("getFinalResponse(" + tagValue + ") no current connection");

        /*      This is actually a FOREVER loop, but we do not expect ~32K responses (we expect to time-out before that).
         * We use this to only to break infinite loops that may occur due to coding errors.
         */
        for (int    rspIndex=0; rspIndex < Short.MAX_VALUE; rspIndex++)
        {
            final String  rspLine=conn.readLine();
            if ((null == rspLine) || (rspLine.length() <= 0))   // skip empty lines
                continue;

            // skip untagged response
            if (IMAP4Protocol.IMAP4_UNTAGGED_RSP == rspLine.charAt(0))
                continue;

            final IMAP4TaggedResponse rsp=IMAP4TaggedResponse.getFinalResponse(tagValue, rspLine, getModifiableResponse());
            if (rsp != null)
                return rsp;
        }

        throw new IMAP4RspHandleException("Virtually infinite loop in waiting for final response of tag=" + tagValue);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4AccessorHelper#doFinalCommand(int, char[], int)
     */
    @Override
    protected IMAP4TaggedResponse doFinalCommand (final int tagValue, final char[] cmdLine, final int cmdLen) throws IOException
    {
        if ((null == cmdLine) || (cmdLen < EOLStyle.CRLF.length()))
            throw new IMAP4AccessParamsException("Bad/Illegal simple IMAP4 command");

        final TextNetConnection    conn=getTextNetConnection();
        if ((null == conn) ||  (!conn.isOpen()))
            throw new IMAP4AccessParamsException("No current connection for simple command=" + new String(cmdLine));

        final int    nWritten=conn.write(cmdLine, 0, cmdLen, true);
        if (nWritten != cmdLen)
            throw new IMAP4AccessParamsException("Mismatched write len (" + nWritten + " <> " + cmdLen + ") on do final command");

        return getFinalResponse(tagValue);
    }
    /**
     * Sends a simple command to the server
     * @param cmd command characters to be sent
     * @return tag value assigned to the command
     * @throws IOException if errors encountered
     */
    protected int sendSimpleCommand (final char[] cmd) throws IOException
    {
        if ((null == cmd) || (cmd.length <= 0))
            throw new IMAP4AccessParamsException("No simple command supplied");

        final TextNetConnection    conn=getTextNetConnection();
        if ((null == conn) ||  (!conn.isOpen()))
            throw new IMAP4AccessParamsException("No current connection for simple command=" + new String(cmd));

        final int           tagValue=getAutoTag();
        final StringBuilder    sb=getWorkBuf(cmd.length + EOLStyle.CRLF.length())
                                .append(tagValue)
                                .append(' ')
                                .append(cmd)
                                .append(EOLStyle.CRLF.getStyleChars())
                                ;

        final int    cmdLen=sb.length(), nWritten=conn.write(StringUtil.getBackingArray(sb), 0, cmdLen, true);
        if (nWritten != cmdLen)
            throw new IMAP4AccessParamsException("Mismatched write len (" + nWritten + " <> " + cmdLen + ") on send simple command=" + new String(cmd));

        return tagValue;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#capability()
     */
    @Override
    public IMAP4Capabilities capability () throws IOException
    {
        return IMAP4Capabilities.getFinalResponse(getTextNetConnection(), sendSimpleCommand(IMAP4Protocol.IMAP4CapabilityCmdChars));
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#namespace()
     */
    @Override
    public IMAP4NamespacesInfo namespace () throws IOException
    {
        return IMAP4NamespacesInfo.getFinalResponse(getTextNetConnection(), sendSimpleCommand(IMAP4Protocol.IMAP4NamespaceCmdChars));
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#getquotaroot(java.lang.String)
     */
    @Override
    public IMAP4QuotarootInfo getquotaroot (final String rootName) throws IOException
    {
        final String    qtFolder=((null == rootName) || (rootName.length() <= 0))
                            ? IMAP4FolderInfo.IMAP4_INBOX
                            : rootName
                            ;
        return IMAP4QuotarootInfo.getFinalResponse(getTextNetConnection(), sendFolderData(IMAP4Protocol.IMAP4GetQuotaRootCmdChars, qtFolder, (char[]) null));
    }
    /**
     * Uses an internal buffer to make folder name canonization more efficient
     * @param folder original folder name to be canonized
     * @param orgSep separator used in the string to separate path components
     * @param hierSep separator expected by the server as separator
     * @return adjusted string (which may be same as input if no changes required)
     * @throws IllegalStateException if BOTH the original AND the hierarchy separators appear in the folder string
     */
    public String getAdjustedFolderName (final String folder, char orgSep, char hierSep)
    {
        return IMAP4Protocol.adjustFolderName(folder, orgSep, hierSep, getWorkBuf((null == folder) ? 0 : folder.length()));
    }
    /**
     * Waits for the server to request more data
     * @return true if successful
     * @throws IOException if error encountered
     */
    protected boolean waitForServerContinuation () throws IOException
    {
        return IMAP4Protocol.waitForServerContinuation(getTextNetConnection());
    }
    /**
     * Outputs the folder - if the folder contains special characters (e.g.,
     * double-quote) then it is sent as a literal.
     * @param sb The {@link StringBuilder} buffer containing command built so
     * far - without the folder argument. Upon (successful) return from the
     * function, the buffer may contain additional data that may need to be
     * otuput/flushed to the connection in order to complete the folder name
     * sending
     * @param folder folder to be output - must be built according to IMAP4
     * modified UTF-8 rules
     * @return Same as input instance if successful - null otherwise
     * @throws IOException if unable to output the folder to the network connection
     */
    private StringBuilder outputFolder (final StringBuilder sb, final String folder) throws IOException
    {
        if (null == sb)
            return sb;

        if (IMAP4FolderInfo.requiresLiteralSend(folder))
        {
            IMAP4Protocol.addLiteralCount(sb, (null == folder) ? 0 : folder.length());
            sb.append(EOLStyle.CRLF.getStyleChars());

            final TextNetConnection    conn=getTextNetConnection();
            if ((null == conn) ||  (!conn.isOpen()))
                throw new IMAP4AccessParamsException("No current connection for simple outputFolder=" + folder);

            final int    cmdLen=sb.length(), nWritten=conn.write(StringUtil.getBackingArray(sb), 0, cmdLen, true);
            if (nWritten != cmdLen)
                throw new IMAP4AccessParamsException("Mismatched write len (" + nWritten + " <> " + cmdLen + ") on output folder=" + folder);

            if (!waitForServerContinuation())
                return null;

            sb.setLength(0);
            sb.append(folder);
        }
        else
        {
            sb.append(IMAP4Protocol.IMAP4_QUOTE_DELIM)
              .append(folder)
              .append(IMAP4Protocol.IMAP4_QUOTE_DELIM)
              ;
        }

        return sb;
    }
    /**
     * Sends a folder as argument
     * @param sb string buffer to work with (in/out)
     * @param cmd command to be sent
     * @param folder folder argument to specified command
     * @param args arguments to folder command
     * @param flushIt if TRUE then string buffer is flushed to output after successful build
     * @return tag value assigned to the command (<0 if error)
     * @throws IOException if errors encountered
     */
    private int sendFolderData (final StringBuilder sb, final char[] cmd, final String folder, final char[] args, final boolean flushIt) throws IOException
    {
        if ((null == cmd) || (cmd.length <= 0) || (null == sb))
            throw new IMAP4AccessParamsException("No folder command specified");
        sb.setLength(0);

        int  tagValue=getAutoTag(), argLen=(null == args) ? 0 : args.length;
        IMAP4Protocol.buildCmdPrefix(sb, tagValue, cmd, false, true);
        if (outputFolder(sb, folder) != sb)
            throw new IMAP4AccessParamsException("Cannot output " + new String(cmd) + " folder command argument");

        if (argLen != 0)
            sb.append(' ')
              .append(args)
              ;

        if (flushIt)
        {
            sb.append(EOLStyle.CRLF.getStyleChars());

            final TextNetConnection    conn=getTextNetConnection();
            if ((null == conn) ||  (!conn.isOpen()))
                throw new IMAP4AccessParamsException("No current connection for sending folder command=" + new String(cmd));

            final int    cmdLen=sb.length(), nWritten=conn.write(StringUtil.getBackingArray(sb), 0, cmdLen, true);
            if (nWritten != cmdLen)
                throw new IMAP4AccessParamsException("Mismatched write len (" + nWritten + " <> " + cmdLen + ") on send data folder=" + folder);

            sb.setLength(0);
        }

        return tagValue;
    }
    /**
     * Sends a folder as argument (and flushes the data after sending the command + folder + arguments)
     * @param cmd command to be sent
     * @param folder folder argument to specified command
     * @param args arguments to folder command
     * @return tag value assigned to the command (<0 if error)
     * @throws IOException if errors encountered
     */
    private int sendFolderData (final char[] cmd, final String folder, final char[] args) throws IOException
    {
        return sendFolderData(getWorkBuf((null == cmd) ? 0 : cmd.length), cmd, folder, args, true);
    }
    /**
     * Handle a command that has (optionnally) 2 folders as its argument (e.g. RENAME, LIST)
     * @param cmd command
     * @param srcFolder source folder
     * @param dstFolder destination/2nd folder
     * @return assigned tag value (or <0 if error)
     * @throws IOException if error encountered
     */
    private int sendDualFolderData (final char[] cmd, final String srcFolder, final String dstFolder) throws IOException
    {
        final int srcLen=(null == srcFolder) ? 0 : srcFolder.length(), dstLen=(null == dstFolder) ? 0 : dstFolder.length();
        if ((null == cmd) || (cmd.length <= 0))
            throw new IMAP4AccessParamsException("Bad/Illegal folder cmd/source folder");

        final StringBuilder sb=getWorkBuf(cmd.length + Math.max(srcLen,0) + Math.max(dstLen, 0));
        final int              tagValue=sendFolderData(sb, cmd, srcFolder, (char[]) null, (dstLen <= 0));
        if (tagValue < 0)
            throw new IMAP4AccessParamsException("Cannot send 1st folder data - err=" + tagValue);

        if (dstLen != 0)
        {
            sb.append(' ');

            if (outputFolder(sb, dstFolder) != sb)
                throw new IMAP4AccessParamsException("Cannot output " + new String(cmd) + " dst folder command argument");

            sb.append(EOLStyle.CRLF.getStyleChars());

            final TextNetConnection    conn=getTextNetConnection();
            if ((null == conn) ||  (!conn.isOpen()))
                throw new IMAP4AccessParamsException("No current connection for sending dual folder command=" + new String(cmd));

            final int    cmdLen=sb.length(), nWritten=conn.write(StringUtil.getBackingArray(sb), 0, cmdLen, true);
            if (nWritten != cmdLen)
                throw new IMAP4AccessParamsException("Mismatched write len (" + nWritten + " <> " + cmdLen + ") on " + new String(cmd) + "dual folder send");
        }

        return tagValue;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4Accessor#setCurFolder(char[], java.lang.String, boolean)
     */
    @Override
    protected IMAP4FolderSelectionInfo setCurFolder (final char[] cmd, final String folder, final boolean getFullInfo) throws IOException
    {
        if ((null == cmd) || (cmd.length <= 0))
            throw new IMAP4AccessParamsException("No current folder command specified");

        final int tagValue=sendFolderData(cmd, folder, (char[]) null);
        if (tagValue < 0)
            throw new IMAP4AccessParamsException("Cannot set[" + new String(cmd) + "] cur folder=" + folder);

        if (getFullInfo)
            return IMAP4FolderSelectionInfo.getFinalResponse(getTextNetConnection(), tagValue);
        else // if not interested in full info, return a normal response, disguised as a folder selection one
            return new IMAP4FolderSelectionInfo(getFinalResponse(tagValue));
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4Accessor#doFolderCmd(char[], java.lang.String, java.lang.String)
     */
    @Override
    protected IMAP4TaggedResponse doFolderCmd (final char[] cmd, final String srcFolder, final String dstFolder) throws IOException
    {
        return getFinalResponse(sendDualFolderData(cmd, srcFolder, dstFolder));
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4Accessor#getMbRef(char[], java.lang.String, java.lang.String)
     */
    @Override
    protected IMAP4FoldersListInfo getMbRef (final char[] cmd, final String ref, final String mbox) throws IOException
    {
        final int tagValue=sendDualFolderData(cmd, ref, mbox);
        if (tagValue < 0)
            throw new IMAP4AccessParamsException("Cannot (err=" + tagValue + ") issue MBRef cmd=" + new String(cmd) + " " + ((null == ref) ? "NULL" : ref) + " " + ((null == mbox) ? "NULL" : mbox));

        return IMAP4FoldersListInfo.getFinalResponse(cmd, getTextNetConnection(), tagValue);
    }
    /**
     * Default status information fetched by the STATUS command
     */
    private static final String DEFAULT_STATUS=
            IMAP4Protocol.IMAP4_PARLIST_SDELIM
                      + IMAP4StatusInfo.IMAP4_MESSAGES
                + ' ' + IMAP4StatusInfo.IMAP4_UIDNEXT
                + ' ' + IMAP4StatusInfo.IMAP4_RECENT
                + ' ' + IMAP4StatusInfo.IMAP4_UNSEEN
                + ' ' + IMAP4StatusInfo.IMAP4_UIDVALIDITY
            + IMAP4Protocol.IMAP4_PARLIST_EDELIM;
    private static final char[] DEFAULT_STATUSChars=DEFAULT_STATUS.toCharArray();
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#status(java.lang.String)
     */
    @Override
    public IMAP4StatusInfo status (final String folder) throws IOException
    {
        final StringBuilder    sb=getWorkBuf(IMAP4Protocol.IMAP4StatusCmdChars.length + DEFAULT_STATUSChars.length);
        final int           tagValue=sendFolderData(sb, IMAP4Protocol.IMAP4StatusCmdChars, folder, DEFAULT_STATUSChars, true);
        if (tagValue < 0)
            throw new IMAP4AccessParamsException("Cannot send " + IMAP4Protocol.IMAP4StatusCmd + " folder data - err=" + tagValue);

        return IMAP4StatusInfo.getFinalResponse(getTextNetConnection(), tagValue);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#fetchFinalResponse(int, char[], int, net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler)
     */
    @Override
    public IMAP4TaggedResponse fetchFinalResponse (final int tagValue, final char[] cmdLine, final int lineLen, final IMAP4FetchResponseHandler rspHandler) throws IOException
    {
        if ((null == cmdLine) || (lineLen <= EOLStyle.CRLF.length()) || (lineLen > cmdLine.length) || (null == rspHandler))
            throw new IMAP4AccessParamsException("Bad/Illegal arguments to FETCH");

        final TextNetConnection    conn=getTextNetConnection();
        if ((null == conn) ||  (!conn.isOpen()))
            throw new IMAP4AccessParamsException("No current connection for sending final command=" + new String(cmdLine));

        final int    nWritten=conn.write(cmdLine, 0, lineLen, true);
        if (nWritten != lineLen)
            throw new IMAP4AccessParamsException("Mismatched write len (" + nWritten + " <> " + lineLen + ") on fetch final response");

        return (new IMAP4UntaggedFetchRspHandler(conn, rspHandler, getModifiableResponse())).handleResponse(tagValue);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#search(boolean, java.lang.String, int)
     */
    @Override
    public IMAP4SearchResponse search (final boolean isUID, final String condition, final int maxResults) throws IOException
    {
        final int    condLen=(null == condition) ? 0 : condition.length();
        if ((condLen <= 0) || (0 == maxResults))
            throw new IMAP4AccessParamsException("Bad/Illegal SEARCH command arguments");

        final int           tagValue=getAutoTag();
        final StringBuilder sb=getWorkBuf(condLen + IMAP4Protocol.IMAP4SearchCmdChars.length + (isUID ? IMAP4FetchModifier.IMAP4_UIDChars.length : 0) + 1);
        IMAP4Protocol.buildCmdPrefix(sb, tagValue, IMAP4Protocol.IMAP4SearchCmdChars, isUID, true);
        sb.append(condition)
          .append(EOLStyle.CRLF.getStyleChars())
          ;

        final TextNetConnection    conn=getTextNetConnection();
        if ((null == conn) ||  (!conn.isOpen()))
            throw new IMAP4AccessParamsException("No current connection for searching by condition=" + condition);

        final int    cmdLen=sb.length(), nWritten=conn.write(StringUtil.getBackingArray(sb), 0, cmdLen, true);
        if (nWritten != cmdLen)
            throw new IMAP4AccessParamsException("Mismatched write len (" + nWritten + " <> " + cmdLen + ") on " + IMAP4Protocol.IMAP4SearchCmd + "command send");

        return IMAP4SearchResponse.getFinalResponse(conn, tagValue, isUID, maxResults);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#append(java.lang.String, java.lang.String, java.lang.String, long, net.community.chest.net.proto.text.imap4.IMAP4AppendDataProvider)
     */
    @Override
    public IMAP4TaggedResponse append (final String folder, final String iDate, final String flags, final long dataSize, final IMAP4AppendDataProvider prov) throws IOException
    {
        final int    provLen=(null == prov) ? 0 : prov.getCopyBufferSize(),
                    dtLen=(null == iDate) ? 0 : iDate.length(),
                    flgLen=(null == flags) ? 0 : flags.length(),
                    tagValue=getAutoTag();
        if (provLen <= 64)
            throw new IMAP4AccessParamsException("Bad/Illegal " + IMAP4Protocol.IMAP4AppendCmd + " data provider");

        final StringBuilder   sb=getWorkBuf(dtLen + flgLen + IMAP4Protocol.IMAP4AppendCmdChars.length + 3);
        IMAP4Protocol.buildCmdPrefix(sb, tagValue, IMAP4Protocol.IMAP4AppendCmdChars, false, true);

        if (outputFolder(sb, folder) != sb)
            throw new IMAP4AccessParamsException("Cannot output " + IMAP4Protocol.IMAP4AppendCmd + " folder name");

        IMAP4Protocol.addAppendCmdArgs(sb, iDate, flags, dataSize);
        sb.append(EOLStyle.CRLF.getStyleChars());

        final TextNetConnection    conn=getTextNetConnection();
        if ((null == conn) ||  (!conn.isOpen()))
            throw new IMAP4AccessParamsException("No current connection for append to folder=" + folder);

        final int    cmdLen=sb.length();
              int    nWritten=conn.write(StringUtil.getBackingArray(sb), 0, cmdLen, true);
        if (nWritten != cmdLen)
            throw new IMAP4AccessParamsException("Mismatched write len (" + nWritten + " <> " + cmdLen + ") on send APPEND command");

        if (!waitForServerContinuation())
            throw new IMAP4AccessParamsException("Cannot get " + IMAP4Protocol.IMAP4AppendCmd +" continuation");

        byte[]    cpyBuf=new byte[provLen];
        long    remLen=dataSize;
        // we limit the loop to ~2GB calls
        for (int    rspIndex=0; rspIndex < (Integer.MAX_VALUE-1); rspIndex++)
        {
            int    readLen=(int) Math.min(remLen, provLen), nRead=prov.getData(cpyBuf, 0, readLen);
            if (nRead <= 0)
                throw new IMAP4AccessParamsException("Bad/illegal " + IMAP4Protocol.IMAP4AppendCmd + " buffer size (" + nRead + ")");

            if ((nWritten=conn.writeBytes(cpyBuf, 0, nRead)) != nRead)
                throw new IMAP4AccessParamsException("Read(" + nRead + ")/Write(" + nWritten +") mismatch while writing APPEND data");

            if ((remLen -= nRead) <= 0 /* actually == 0 is enough, but what the heck... */)
            {
                // send terminating CRLF
                if ((nWritten=conn.writeln(true)) != EOLStyle.CRLF.length())
                    throw new IMAP4AccessParamsException("Write mismatch (" + nWritten + ") while teminating APPEND data");

                return getFinalResponse(tagValue);
            }
        }

        throw new IMAP4AccessParamsException("Virtual infinite loop exit for " + IMAP4Protocol.IMAP4AppendCmd);
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4Accessor#xferMsgs(char[], java.lang.String, boolean, java.lang.String)
     */
    @Override
    public IMAP4TaggedResponse xferMsgs (final char[] cmd, final String msgRange, final boolean isUID, final String dstFolder) throws IOException
    {
        final int rangeLen=(null == msgRange) ? 0 : msgRange.length(), tagValue=getAutoTag();
        if (rangeLen <= 0)
            throw new IMAP4AccessParamsException("No message range specified");

        final StringBuilder   sb=getWorkBuf(rangeLen + Math.max(cmd.length,0));
        IMAP4Protocol.buildMsgRangeCmdPrefix(sb, tagValue, cmd, msgRange, isUID);

        if (outputFolder(sb, dstFolder) != sb)
            throw new IMAP4AccessParamsException("Cannot output " + new String(cmd) + " folder name");
        sb.append(EOLStyle.CRLF.getStyleChars());

        final TextNetConnection    conn=getTextNetConnection();
        if ((null == conn) ||  (!conn.isOpen()))
            throw new IMAP4AccessParamsException("No current connection for xferMsgs=" + new String(cmd));

        final int    cmdLen=sb.length(), nWritten=conn.write(StringUtil.getBackingArray(sb), 0, cmdLen, true);
        if (nWritten != cmdLen)
            throw new IMAP4AccessParamsException("Mismatched write len (" + nWritten + " <> " + cmdLen + ") on send XFER command=" + new String(cmd));

        return getFinalResponse(tagValue);
    }
}

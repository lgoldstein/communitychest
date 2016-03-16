package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.io.OutputStream;

import net.community.chest.io.output.OutputStreamEmbedder;
import net.community.chest.lang.StringUtil;
import net.community.chest.mail.address.MessageAddressType;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 2:00:22 PM
 */
public class IMAP4RawMsgPartHandler extends OutputStreamEmbedder implements IMAP4FetchResponseHandler {
    private final String    _msgPart;
    public final String getMsgPart ()
    {
        return _msgPart;
    }

    public IMAP4RawMsgPartHandler (OutputStream outStream, boolean realClosure, String msgPart)
    {
        super(outStream, realClosure);

        if ((null == (this._msgPart=msgPart)) || (msgPart.length() <= 0))
            throw new IllegalArgumentException("No message part supplied for raw message part dumper");
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleFlagsStage(int, boolean)
     */
    @Override
    public int handleFlagsStage (int msgSeqNo, boolean starting)
    {
        return 0;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleFlagValue(int, java.lang.String)
     */
    @Override
    public int handleFlagValue (int msgSeqNo, String flagValue)
    {
        return 0;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleInternalDate(int, java.lang.String)
     */
    @Override
    public int handleInternalDate (int msgSeqNo, String dateValue)
    {
        return (-2005);    // unexpected call
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgPartAddress(int, java.lang.String, net.community.chest.mail.address.MessageAddressType, java.lang.String, java.lang.String)
     */
    @Override
    public int handleMsgPartAddress (int msgSeqNo, String msgPart, MessageAddressType addrType, String dispName, String addrVal)
    {
        return (-2002);    // unexpected call
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgPartHeader(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public int handleMsgPartHeader (int msgSeqNo, String msgPart, String hdrName, String attrName, String attrValue)
    {
        return (-2001);    // unexpected call
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgPartSize(int, java.lang.String, long)
     */
    @Override
    public int handleMsgPartSize (int msgSeqNo, String msgPart, long partSize)
    {
        return 0;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgPartStage(int, java.lang.String, boolean)
     */
    @Override
    public int handleMsgPartStage (int msgSeqNo, String msgPart, boolean starting)
    {
        return 0;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleMsgResponseState(int, boolean)
     */
    @Override
    public int handleMsgResponseState (int msgSeqNo, boolean starting)
    {
        return 0;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handlePartData(int, java.lang.String, byte[], int, int)
     */
    @Override
    public int handlePartData (int msgSeqNo, String msgPart, byte[] data, int offset, int len)
    {
        // make sure this is the requested message part
        if (StringUtil.compareDataStrings(getMsgPart(), msgPart, true) != 0)
            return (-2007);

        try
        {
            write(data, offset, len);
            return 0;
        }
        catch(IOException ioe)
        {
            return (-2008);
        }
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handlePartDataStage(int, java.lang.String, boolean)
     */
    @Override
    public int handlePartDataStage (int msgSeqNo, String msgPart, boolean starting)
    {
        return 0;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4FetchResponseHandler#handleUID(int, long)
     */
    @Override
    public int handleUID (int msgSeqNo, long msgUID)
    {
        return 0;
    }
}

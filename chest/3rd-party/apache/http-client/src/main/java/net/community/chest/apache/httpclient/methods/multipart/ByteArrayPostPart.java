package net.community.chest.apache.httpclient.methods.multipart;

import java.io.IOException;
import java.io.OutputStream;

import net.community.chest.mail.MimeEncodingTypeEnum;
import net.community.chest.mail.RFCMimeDefinitions;

import org.apache.commons.httpclient.methods.multipart.PartBase;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>A more user-friendly POST multipart part for passing pure binary data
 * as "application/octet-stream" "8bit" no-name part</P>
 *
 * @author Lyor G.
 * @since Oct 10, 2007 12:16:19 PM
 */
public class ByteArrayPostPart extends PartBase {
    private final byte[]    _buf;
    private final int        _off, _len;
    /**
     * Default used content-type: "application/octet-stream"
     */
    public static final String    DEFAULT_CONTENT_TYPE=RFCMimeDefinitions.buildMIMETag(RFCMimeDefinitions.MIMEApplicationType,RFCMimeDefinitions.MIMEOctetStreamSubType);
    /**
     * @param name parameter name
     * @param b data buffer to be posted - may NOT be null
     * @param off offset of data in buffer to post - may NOT be <0
     * @param len number of elements - may NOT be <=0
     * @throws IllegalArgumentException if illegal buffer data specified
     */
    public ByteArrayPostPart (String name, byte[] b, int off, int len) throws IllegalArgumentException
    {
        super(name, DEFAULT_CONTENT_TYPE, "", MimeEncodingTypeEnum.BINARY8BIT.getXferEncoding());

        if ((null == (_buf=b)) || ((_off=off) < 0) || ((_len=len) <= 0) || ((off + len) > b.length))
            throw new IllegalArgumentException("Bad/Illegal POST buffer specification");
    }
    /**
     * @param name parameter name
     * @param b data buffer to be posted - may NOT be null/empty
     * @throws IllegalArgumentException if illegal buffer data specified
     */
    public ByteArrayPostPart (String name, byte[] b) throws IllegalArgumentException
    {
        this(name, b, 0, (null == b) ? 0 : b.length);
    }
    /*
     * @see org.apache.commons.httpclient.methods.multipart.Part#sendData(java.io.OutputStream)
     */
    @Override
    protected void sendData (OutputStream out) throws IOException
    {
        out.write(_buf, _off, _len);
    }
    /*
     * @see org.apache.commons.httpclient.methods.multipart.Part#lengthOfData()
     */
    @Override
    protected long lengthOfData () throws IOException
    {
        return _len;
    }
}

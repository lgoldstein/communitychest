package net.community.chest.io.encode.base64;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import net.community.chest.io.encode.DecodingException;
import net.community.chest.io.encode.OutputStreamDecoder;
import net.community.chest.io.output.BufferedOutputStreamEmbedder;

/**
 * Helper class that assumes everything written to it is BASE64 encoded, and
 * writes it as "clear" data to the "real" output stream after decoding it
 * @author lyorg
 * 04/03/2004
 */
public class Base64DecodeOutputStream extends BufferedOutputStreamEmbedder implements OutputStreamDecoder {
    private final boolean        _throwExceptions;
    private DecodingException    _decExc    /* =null */;
    /*
     * @see net.community.chest.io.encode.OutputStreamDecoder#getDecodeException()
     */
    @Override
    public DecodingException getDecodeException ()
    {
        return _decExc;
    }
    /**
     * Updates the last set decoding exception - provided one not already set
     * @param exc exception to be set
     * @throws DecodingException same as input if {@link #_throwExceptions} is TRUE
     */
    protected void setDecodeException (final DecodingException exc) throws DecodingException
    {
        if ((null == _decExc) && (exc != null) /* should not be otherwise */)
            _decExc = exc;
        if (_throwExceptions)
            throw exc;
    }

    private int     _inPos /* =0 */, _padCount /* =0 */;
    private int[]   _inBuffer=new int[Base64.BASE64_OUTPUT_BLOCK_LEN];
    private boolean    _done /* =false */;
    /**
     * Base constructor
     * @param ost "real" output stream into which decoded data is to be written
     * @param bufSize internal buffering size - MUST be at least BASE64_INPUT_BLOCK_LEN size
     * @param throwExceptions if TRUE, then non-BASE64 data causes exception to
     * be thrown on "write" - otherwise, ignored and can be retrieved via
     * {@link #getDecodeException()} call
     * @param realClose if true the call to {@link #close()} also closes
     * the underlying stream
     * @throws IllegalArgumentException if bad/illegal underlying stream/size
     */
    public Base64DecodeOutputStream (OutputStream ost, int bufSize, boolean throwExceptions, boolean realClose) throws IllegalArgumentException
    {
        super(ost, bufSize, realClose);

        if (bufSize < Base64.BASE64_INPUT_BLOCK_LEN)
            throw new IllegalArgumentException("Bad/illegal buffer size: " + bufSize);

        _throwExceptions = throwExceptions;
    }
    /**
     * Base constructor
     * @param ost "real" output stream into which decoded data is to be written
     * @param throwExceptions if TRUE, then non-BASE64 data causes exception to
     * be thrown on "write" (otherwise, ignored)
     * @param realClose if true the call to {@link #close()} also closes
     * the underlying stream
     * @throws IllegalArgumentException if bad/illegal underlying stream
     */
    public Base64DecodeOutputStream (OutputStream ost, boolean throwExceptions, boolean realClose) throws IllegalArgumentException
    {
        this(ost, Base64.BASE64_INPUT_BLOCK_LEN, throwExceptions, realClose);
    }
    /**
     * Simple constructor - Note: throws exception if non-BASE64 data found
     * @param ost "real" output stream into which decoded data is to be written
     * @param realClose if true the call to {@link #close()} also closes
     * the underlying stream
     * @throws IllegalArgumentException if bad/illegal underlying stream
     */
    public Base64DecodeOutputStream (OutputStream ost, boolean realClose) throws IllegalArgumentException
    {
        this(ost, true, realClose);
    }
    /**
     * Note: automatically closes the underlying stream on call to
     * {@link #close()} - unless {@link #setRealClosure(boolean)} is called
     * @param ost "real" output stream into which decoded data is to be written
     * @throws IllegalArgumentException if bad/illegal underlying stream
     */
    public Base64DecodeOutputStream (OutputStream ost) throws IllegalArgumentException
    {
        this(ost, true);
    }
    /**
     * @param buf original buffer
     * @param offset original offset in buffer
     * @param curOffset current decoding position in buffer
     * @param len original data length
     * @return partial buffer string for exceptions display - null if error/no data
     */
    private static final String getDecodeInputString (final byte[] buf, final int offset, final int curOffset, final int len)
    {
        final int    usedLen=curOffset - offset,
                    availLen=(len - usedLen),
                    dispLen=Math.min(availLen, 32 /* display enough to indicate problem */);
        if ((null == buf) || (offset < 0) || (usedLen <= 0) || (len <= 0) || (dispLen <= 0) || ((offset + len) > buf.length))
            return null;

        try
        {
            return new String(buf, curOffset, dispLen, "US-ASCII");
        }
        catch (UnsupportedEncodingException e)
        {
            // should not happen
            return e.getClass().getName() + ": " + e.getMessage();
        }
    }
    /*
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public synchronized void write (byte[] wbuf, int offset, int len) throws IOException
    {
        if (null == this.out)
            throw new IOException("No underlying stream to write to");
        if ((len < 0) || (offset < 0))
            throw new IOException("Negative buffer range");
        if (0 == len)
            return;

        final int    maxOffset=(offset+len);
        if ((null == wbuf) || (maxOffset > wbuf.length))
            throw new IOException("Bad/Mismatched buffer specification");

        for (int    curOffset=offset; curOffset < maxOffset; curOffset++)
        {
            final int    val=Base64.reverseBase64Chars[wbuf[curOffset]];
            if (Base64.NON_BASE_64_WHITESPACE == val)
                continue;    // skip white-space

            if (val == Base64.NON_BASE_64)
            {
                if (null == getDecodeException())
                    setDecodeException(new Base64DecodingException("Non-BASE64 char value at position=" + curOffset + ": " + getDecodeInputString(wbuf, offset, curOffset, len), (char) val));
                // this point is reached if exception throwing is deferred
                continue;
            }

            // not allowed to have any valid characters after end of decoding
            if (_done)
            {
                if (null == getDecodeException())
                    setDecodeException(new Base64DecodingException("BASE64 decode block after end: " + getDecodeInputString(wbuf, offset, curOffset, len), (char) val));
                // this point is reached if exception throwing is deferred
                return;
            }

            if (Base64.NON_BASE_64_PADDING == (_inBuffer[_inPos]=val))
                _padCount++;
            _inPos++;

            if (_inPos < Base64.BASE64_OUTPUT_BLOCK_LEN)
                continue;

            // make sure we can accomodate this input block
            if ((this.buf.length - this.count) < Base64.BASE64_INPUT_BLOCK_LEN)
                flush();

            if (_padCount > 0)
            {
                byte    outLen=Base64.BASE64_INPUT_BLOCK_LEN;

                // six A and two B
                this.buf[this.count]          = (byte) (((_inBuffer[0] < 0) || (_inBuffer[1] < 0)) ? (outLen=0) : (((_inBuffer[0] << 2) | (_inBuffer[1] >> 4)) & 0x00FF));
                // four B and four C
                this.buf[this.count + 1]    = (byte) ((_inBuffer[2] < 0) ? (outLen=1) : (((_inBuffer[1] << 4) | (_inBuffer[2] >> 2)) & 0x00FF));
                // two C and six D
                this.buf[this.count + 2]    = (byte) ((_inBuffer[3] < 0) ? (outLen=2) : (((_inBuffer[2] << 6) | _inBuffer[3]) & 0x00FF));

                this.count += outLen;

                // if decoded the buffer and have a '=' (pad) count, then obviously, end of stream reached
                _done = true;
            }
            else    // no padding, so "normal" calculation
            {
                // six A and two B
                this.buf[this.count] = (byte) (((_inBuffer[0] << 2) | (_inBuffer[1] >> 4)) & 0x00FF);
                // four B and four C
                this.buf[this.count + 1] = (byte) (((_inBuffer[1] << 4) | (_inBuffer[2] >> 2)) & 0x00FF);
                // two C and six D
                this.buf[this.count + 2] = (byte) (((_inBuffer[2] << 6) | _inBuffer[3]) & 0x00FF);

                this.count += Base64.BASE64_INPUT_BLOCK_LEN;
            }

            _inPos = 0;    // restart count
        }
    }
    /*
     * Override the default behavior and do NOT close the underlying stream
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close () throws IOException
    {
        if (this.out != null)
        {
            flush();

            try
            {
                super.close();

                if (_inPos > 0)    // make sure entire data decoded
                {
                    if (getDecodeException() != null)
                        setDecodeException(new Base64DecodingException("Incomplete BASE64 buffer leftover (" + _inPos + " characters)", (char) (_inBuffer[_inPos] & 0x00FF)));
                }
            }
            finally
            {
                this.out = null;    // make sure default implementation does not close the stream
            }
        }
    }
    /*
     * Just make sure "close" is called
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } catch(IOException e) {
            // ignored
        }
        super.finalize();
    }
}

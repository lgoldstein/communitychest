package net.community.chest.io.encode.base64;

import java.io.IOException;
import java.io.InputStream;

import net.community.chest.io.input.InputStreamEmbedder;
import net.community.chest.io.output.BoundedArrayOutputStream;

/**
 * Helper class that returns a "clear" byte stream given a BASE64 encoded input stream
 * @author lyorg
 * 04/03/2004
 */
public class Base64DecodeInputStream extends InputStreamEmbedder {
    private final boolean                  _throwsException;
    private BoundedArrayOutputStream    _ost /* =null */;
    private byte[]                        _obf=new byte[Base64.BASE64_INPUT_BLOCK_LEN];
    private int                           _obfPos /* =0 */, _obfCount /* =0 */;
    private int[]                         _inBuffer=new int[Base64.BASE64_OUTPUT_BLOCK_LEN];
    private boolean                       _done /* =false */;
    /**
     * Base constructor
     * @param ist original input stream (assumed to have BASE64 encoded data)
     * @param throwsException if TRUE, then exception is thrown if invalid
     * BASE64 encoding encountered. Otherwise, the data is ignored
     * @param realClose if TRUE then calling {@link #close()} also closes
     * the underlying input stream
     * @throws IllegalArgumentException if bad/illegal stream
     */
    public Base64DecodeInputStream (InputStream ist, boolean throwsException, boolean realClose) throws IllegalArgumentException
    {
        super(ist, realClose);

        _throwsException = throwsException;
        _ost = new BoundedArrayOutputStream(_obf);
    }
    /**
     * Simple constructor
     * @param ist original input stream (assumed to have BASE64 encoded data). Note:
     * if any non-BASE64 data is encountered, then exception is thrown
     * @param realClose if TRUE then calling {@link #close()} also closes
     * the underlying input stream
     * @throws IllegalArgumentException if bad/illegal stream
     */
    public Base64DecodeInputStream (InputStream ist, boolean realClose) throws IllegalArgumentException
    {
        this(ist, true, realClose);
    }

    public Base64DecodeInputStream (InputStream ist) throws IllegalArgumentException
    {
        this(ist, true);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        for (int    pos=off, maxPos=off+len; pos < maxPos; pos++) {
            int val=read();
            if (val == (-1)) {
                return (pos - off);
            }

            b[pos] = (byte) (val & 0x00FF);
        }

        return len;
    }

    @Override
    public long skip(long n) throws IOException {
        for (long   numSkipped=0; numSkipped < n; numSkipped++) {
            int val=read();
            if (val == (-1)) {
                return numSkipped;
            }
        }

        return n;
    }

    @Override
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException("mark(" + readlimit + ") N/A");
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("reset() N/A");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read () throws IOException
    {
        // check if exhausted last decoded block
        if (_obfPos >= _obfCount)
        {
            if (_done)
                return (-1);

            _ost.reset();    // reset output to prepare for decoding

            if (Base64.readBase64Block(this.in, _inBuffer, _throwsException))
                _done = true;
            else if (Base64.decodeBase64Block(_inBuffer, _ost))
                _done = true;

            _obfCount = _ost.size();
            _obfPos = 0;

            // check if have any valid data after decoding
            if (_obfPos >= _obfCount)
                return (-1);
        }

        int val=(_obf[_obfPos] & 0x00FF);  // avoid sign-extend of values higher than 127
        _obfPos++;
        return val;
    }
}

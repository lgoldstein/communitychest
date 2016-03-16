package net.community.chest.mail.message;

import java.io.IOException;
import java.io.OutputStream;

import net.community.chest.io.output.OutputStreamEmbedder;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 9:04:32 AM
 */
public class EOMHunterOutputStream extends OutputStreamEmbedder implements EOMDataStreamHunter {
    private boolean    _haveEOM /* =false */;
    /*
     * @see net.community.chest.mail.message.EOMDataStreamHunter#isEOMDetected()
     */
    @Override
    public boolean isEOMDetected ()
    {
        return _haveEOM;
    }
    /*
     * @see net.community.chest.mail.message.EOMDataStreamHunter#setEOMDetected(boolean)
     */
    @Override
    public void setEOMDetected (boolean haveEOM)
    {
        _haveEOM = haveEOM;
    }

    private boolean    _echoEOM /* =false */;
    /*
     * @see net.community.chest.mail.message.EOMDataStreamHunter#isEchoEOM()
     */
    @Override
    public boolean isEchoEOM ()
    {
        return _echoEOM;
    }
    /*
     * @see net.community.chest.mail.message.EOMDataStreamHunter#setEchoEOM(boolean)
     */
    @Override
    public void setEchoEOM (boolean echoEOM)
    {
        _echoEOM = echoEOM;
    }
    /**
     * @param os output stream to write data to while "hunting" for EOM
     * @param echoEOM If set, then EOM is "echoed" to the "real" stream
     * after its detection. Otherwise, it is omitted from the real stream.
     * @param realClose If set, the calling "close" on this stream also
     * closes the "real" stream
     */
    public EOMHunterOutputStream (OutputStream os, boolean echoEOM, boolean realClose)
    {
        super(os, realClose);
        setEchoEOM(echoEOM);
    }
    /**
     * Creates an output stream that does not echo the EOM (if found), and calls
     * the underlying stream's "close" method on calling its "close".
     * @param os output stream to write data to while "hunting" for EOM
     */
    public EOMHunterOutputStream (OutputStream os)
    {
        this(os, false, true);
    }
    // maximum EOM signal is CRLF.CRLF (we accept LF.LF as well as similar variations)
    private final byte[]    _lastBuf=new byte[EOMHunter.MAX_EOM_SEQLEN];
    // number of valid bytes in _lastBuf
    private int        _bufPos    /* =0 */;
    /**
     * Writes the specified buffer while analyzing it
     * @param buf buffer to be written
     * @param offset offset of data in buffer
     * @param len number of bytes to be written
     * @param echoToOutput if FALSE, then only the EOM state is updated,
     * but the data is not written to the real output stream.<B>Note:</B>
     * this method can be used to set the internal EOM state to some
     * initial value without affecting the real output stream
     * @throws IOException unable to write
     */
    protected void write (byte[] buf, int offset, int len, boolean echoToOutput) throws IOException
    {
        if (null == this.out)
            throw new IOException("No output stream to write buffer to");
        if ((offset < 0) || (len < 0))
            throw new IOException("Bad/Illegal buffer range");
        if (0 == len)
            return;

        final int    maxPos=(offset + len);
        if ((null == buf) || (maxPos > buf.length))
            throw new IOException("Bad buffer positions");

        // not allowed to write data after EOM has been detected
        if (isEOMDetected())
            throw new IOException("Data buffer write after EOM");

        // NOTE !!! we check only on buffer "edges" and not in between
        if (len >= _lastBuf.length)
        {
            if ((_bufPos > 0) && echoToOutput)    // flush out any previous data
                this.out.write(_lastBuf, 0, _bufPos);

            // do not write last sequence as it may be the EOM
            final int    writeLen=(len - _lastBuf.length);
            if ((writeLen > 0) && echoToOutput)
                this.out.write(buf, offset, writeLen);

            System.arraycopy(buf, offset + writeLen, _lastBuf, 0, _lastBuf.length);
            _bufPos = _lastBuf.length;
        }
        else
        {
            // check how much data we should carry over from current EOM buffer hunter
            final int    totalLen=_bufPos + len;
            // if appending to current data exceeds buffer size, then make room for appending
            if (totalLen > _lastBuf.length)
            {
                // flush out the extra bytes from start of buffer to make room
                final int    writeLen=(totalLen - _lastBuf.length);
                if (echoToOutput)
                    this.out.write(_lastBuf, 0, writeLen);

                // "shift" down the written bytes to make room for new data
                for (int    i=writeLen; i < _bufPos; i++)
                    _lastBuf[i-writeLen] = _lastBuf[i];
                _bufPos -= writeLen;
            }

            System.arraycopy(buf, offset, _lastBuf, _bufPos, len);
            _bufPos += len;
        }

        setEOMDetected(EOMHunter.checkEOMBuffer(_lastBuf, 0, _bufPos) > 0);
    }
    /*
     * @see java.io.FilterOutputStream#write(byte[], int, int)
     */
    @Override
    public void write (byte[] buf, int offset, int len) throws IOException
    {
        write(buf, offset, len , true);
    }
    /**
     * @param val value to be written
     * @param echoToOutput if FALSE, then only the EOM state is updated,
     * but the data is not written to the real output stream.<B>Note:</B>
     * this method can be used to set the internal EOM state to some
     * initial value without affecting the real output stream
     * @throws IOException unable to write
     */
    protected void write (int val, boolean echoToOutput) throws IOException
    {
        if (null == this.out)
            throw new IOException("No output stream to write character to");

        // not allowed to write data after EOM has been detected
        if (isEOMDetected())
            throw new IOException("Character write after EOM");

        /*     If character cannot possibly be part of a EOM sequence then
         * flush any previous buffer and this character as well
         */
        if ((val != '\r') && (val != '\n') && (val != '.'))
        {
            if (_bufPos > 0)
            {
                if (echoToOutput)
                    this.out.write(_lastBuf, 0, _bufPos);
                _bufPos = 0;
            }

            if (echoToOutput)
                this.out.write(val);
        }
        else    // character might complete a sequence
        {
            // make root for the new value
            if (_bufPos >= _lastBuf.length)
            {
                for (int    i=1; i < _lastBuf.length; i++)
                    _lastBuf[i-1] = _lastBuf[i];

                _lastBuf[_lastBuf.length-1] = (byte) val;
            }
            else    // append new value
            {
                _lastBuf[_bufPos] = (byte) val;
                _bufPos++;
            }

            setEOMDetected(EOMHunter.checkEOMBuffer(_lastBuf, 0, _bufPos) > 0);
        }
    }
    /*
     * @see net.community.chest.io.OutputStreamEmbedder#write(int)
     */
    @Override
    public void write (int val) throws IOException
    {
        write(val, true);
    }
    /* NOTE !!! must be called to ensure echoing of EOM and/or flushing of internal EOM buffer
     * @see net.community.chest.io.OutputStreamEmbedder#close()
     */
    @Override
    public void close () throws IOException
    {
        if (this.out != null)
        {
            // check if have any leftovers from EOM hunt
            if (_bufPos > 0)
            {
                // write the CRLF that comes before the '.' (but not the '.' or the following CRLF)
                if (isEOMDetected() && (!isEchoEOM()) && (_bufPos >= EOMHunter.MIN_EOM_SEQLEN))
                    this.out.write(_lastBuf, 0, _bufPos - EOMHunter.MIN_EOM_SEQLEN);
                else if ((!isEOMDetected()) || isEchoEOM())
                    this.out.write(_lastBuf, 0, _bufPos);
            }

            super.close();
        }
    }
}

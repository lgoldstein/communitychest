/*
 *
 */
package net.community.chest.io.file;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.community.chest.Triplet;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>An implementation of {@link OutputStream} that compares every written
 * data to the same data as read from a provided {@link InputStream} which is
 * assumed to have been positioned at the correct offset. If a difference is
 * found then a special {@link FileDiffException} is thrown with the relevant
 * information</P>
 * @author Lyor G.
 * @since Nov 11, 2010 9:41:29 AM
 */
public class FileDiffOutputStream extends OutputStream {
    private InputStream    _in;
    public InputStream getInputStream ()
    {
        return _in;
    }

    private long    _curOffset;
    public long getCurrentOffset ()
    {
        return _curOffset;
    }
    // CAVEAT EMPTOR if called while write in progress
    public void setInputStream (InputStream in)
    {
        _in = in;
    }

    private boolean    _waitTillClose;
    public boolean isWaitTillClose ()
    {
        return _waitTillClose;
    }

    public void setWaitTillClose (boolean waitTillClose)
    {
        _waitTillClose = waitTillClose;
    }

    public FileDiffOutputStream (final InputStream in, final boolean waitTillClose)
    {
        _in = in;
        _waitTillClose = waitTillClose;
    }

    public FileDiffOutputStream (final InputStream in)
    {
        this(in, false);
    }

    public FileDiffOutputStream ()
    {
        this(null);
    }
    /**
     * <P>Copyright as per GPLv2</P>
     * <P>Special exception thrown if a difference found</P>
     * @author Lyor G.
     * @since Nov 11, 2010 9:47:59 AM
     */
    public static class FileDiffException extends IOException {
        /**
         *
         */
        private static final long serialVersionUID = -6090475116551080201L;
        private final Triplet<Long,Byte,Byte>    _diffInfo;
        public final Triplet<Long,Byte,Byte> getDiffInfo ()
        {
            return _diffInfo;
        }

        public FileDiffException (String msg, long offset, byte inByte, byte outByte)
        {
            this(msg, new Triplet<Long,Byte,Byte>(Long.valueOf(offset), Byte.valueOf(inByte), Byte.valueOf(outByte)));
        }

        public FileDiffException (String msg, Triplet<Long,Byte,Byte> diffInfo)
        {
            super(msg);
            _diffInfo = diffInfo;
        }
    }

    private boolean    _lastBytesChecked;
    private FileDiffException    _savedException;
    public FileDiffException getFileDiffException ()
    {
        return _savedException;
    }

    protected void throwDiffException (String msg, Triplet<Long,Byte,Byte> diffInfo) throws IOException
    {
        if (_savedException == null)
            _savedException = new FileDiffException(msg, diffInfo);
        if (!_lastBytesChecked)
            _lastBytesChecked = true;    // avoid another exception on close
        if (isWaitTillClose())
            return;

        throw _savedException;
    }

    protected void throwDiffException (String msg, long offset, byte inByte, byte outByte) throws IOException
    {
        throwDiffException(msg, new Triplet<Long,Byte,Byte>(Long.valueOf(offset), Byte.valueOf(inByte), Byte.valueOf(outByte)));
    }
    /*
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write (int b) throws IOException
    {
        if (isWaitTillClose() && (_savedException != null))
            return;

        final InputStream    in=getInputStream();
        if (in == null)
            throw new EOFException("No input stream to read from");

        final int    rValue=in.read();
        if (rValue < 0)
        {
            throwDiffException("Premature EOF in input", new Triplet<Long,Byte,Byte>(Long.valueOf(_curOffset), null, Byte.valueOf((byte) b)));
            return;
        }

        if (rValue != b)
        {
            throwDiffException("Mismatched single input", _curOffset, (byte) rValue, (byte) b);
            return;
        }

        _curOffset++;
    }

    private byte[]    _cmpBuf;
    protected byte[] getComparisonBuffer (final int len)
    {
        if ((_cmpBuf == null) || (_cmpBuf.length < len))
            _cmpBuf = new byte[Math.max(len,256)];
        return _cmpBuf;
    }
    /*
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write (byte[] b, int off, int len) throws IOException
    {
        if (isWaitTillClose() && (_savedException != null))
            return;

        final InputStream    in=getInputStream();
        if (in == null)
            throw new EOFException("No input stream to read from");

        if (len <= 0)
            return;

        final byte[]    inBuf=getComparisonBuffer(len);
        final int        readLen=in.read(inBuf, 0, len);
        if (readLen < 0)
        {
            throwDiffException("Premature buffered input EOF", new Triplet<Long,Byte,Byte>(Long.valueOf(_curOffset), null, null));
            return;
        }

        final Triplet<Long,Byte,Byte>    cmpRes=
            FileIOUtils.findDifference(_curOffset, inBuf, 0, readLen, b, off, len);
        if (cmpRes != null)
        {
            throwDiffException("Mismatched buffered input", cmpRes);
            return;
        }

        _curOffset += len;
    }

    public void reset ()
    {
        if (_curOffset != 0L)
            _curOffset = 0L;
        if (_lastBytesChecked)
            _lastBytesChecked = false;
        if (_savedException != null)
            _savedException = null;
    }
    /*
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close () throws IOException
    {
        if (!_lastBytesChecked)
        {
            try
            {
                final InputStream    in=getInputStream();
                if (in == null)
                    return;

                final int    moreBytes=in.available();
                if (moreBytes > 0)
                    throwDiffException("Premature EOF in output", new Triplet<Long,Byte,Byte>(Long.valueOf(_curOffset), null, null));
            }
            finally
            {
                _lastBytesChecked = true;
            }
        }
    }
}

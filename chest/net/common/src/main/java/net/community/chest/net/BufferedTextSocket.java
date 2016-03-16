package net.community.chest.net;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Copyright 2007 as per GPLv2
 *
 * Uses internal buffering for read/write operations to make it more efficient
 *
 * @author Lyor G.
 * @since Jul 4, 2007 10:53:05 AM
 */
public class BufferedTextSocket extends TextSocket {
    /**
     * Default (and minimum) buffering size used for reading from channel
     */
    public static final int DEFAULT_READBUF_SIZE=DEFAULT_CACHEBUF_SIZE;
    /**
     * Default (and minimum) buffering size
     */
    public static final int DEFAULT_WRITEBUF_SIZE=DEFAULT_READBUF_SIZE;

    private final int    _readBufSize    /* =0 */, _writeBufSize    /* =0 */;
    public int getReadBufSize ()
    {
        return _readBufSize;
    }

    public int getWriteBufSize ()
    {
        return _writeBufSize;
    }
    /**
     * Creates a buffered text socket
     * @param readBufSizeVal size of buffering on read access - if below minimum/default, then minimum/default allocated
     * @param writeBufSizeVal size of buffering on write access - if below minimum/default, then minimum/default allocated
     * @see #DEFAULT_READBUF_SIZE
     * @see #DEFAULT_WRITEBUF_SIZE
     */
    public BufferedTextSocket (final int readBufSizeVal, final int writeBufSizeVal)
    {
        _readBufSize = Math.max(DEFAULT_READBUF_SIZE, readBufSizeVal);
        _writeBufSize = Math.max(DEFAULT_WRITEBUF_SIZE, writeBufSizeVal);
    }
    /**
     * Creates a buffered text socket having the default/minimum buffer(s) size(s)
     * @see #BufferedTextSocket(int readBufSize, int writeBufSize)
     * @see #DEFAULT_READBUF_SIZE
     * @see #DEFAULT_WRITEBUF_SIZE
     */
    public BufferedTextSocket ()
    {
        this(DEFAULT_READBUF_SIZE, DEFAULT_WRITEBUF_SIZE);
    }
    /**
     * Creates a buffered text socket that is attached to the supplied socket channel
     * @param sock socket channel to be attached to the object
     * @param readBufSizeVal size of read work buffer on channel access - if below minimum/default, then minimum/default allocated
     * @param writeBufSizeVal size write work buffer on channel access - if below minimum/default, then minimum/default allocated
     * @throws IOException if unable to attach to the channel
     * @see #DEFAULT_READBUF_SIZE
     * @see #DEFAULT_WRITEBUF_SIZE
     */
    public BufferedTextSocket (final SocketChannel sock, final int readBufSizeVal, final int writeBufSizeVal) throws IOException
    {
        this(readBufSizeVal, writeBufSizeVal);
        attach(sock);
    }
    /**
     * Creates a buffered text socket that is attached to the supplied socket channel
     * @param sock socket channel to be attached to the object
     * @throws IOException if unable to attach to the channel
     * @see #DEFAULT_READBUF_SIZE
     * @see #DEFAULT_WRITEBUF_SIZE
     */
    public BufferedTextSocket (final SocketChannel sock) throws IOException
    {
        this(sock, DEFAULT_READBUF_SIZE, DEFAULT_WRITEBUF_SIZE);
    }
    /**
     * Creates a buffered text socket that is attached to the supplied socket channel
     * @param sock socket object to be attached to the connection
     * @param readBufSizeVal size of read work buffer on channel access - if below minimum/default, then minimum/default allocated
     * @param writeBufSizeVal size write work buffer on channel access - if below minimum/default, then minimum/default allocated
     * @throws IOException if unable to attach to the channel
     * @see #DEFAULT_READBUF_SIZE
     * @see #DEFAULT_WRITEBUF_SIZE
     */
    public BufferedTextSocket (final Socket sock, final int readBufSizeVal, final int writeBufSizeVal) throws IOException
    {
        this((null == sock) ? null : sock.getChannel(), readBufSizeVal, writeBufSizeVal);
    }
    /**
     * Creates a buffered text socket that is attached to the supplied socket channel
     * @param sock socket object to be attached to the connection
     * @throws IOException if unable to attach to the channel
     * @see #DEFAULT_READBUF_SIZE
     * @see #DEFAULT_WRITEBUF_SIZE
     */
    public BufferedTextSocket (final Socket sock) throws IOException
    {
        this(sock, DEFAULT_READBUF_SIZE, DEFAULT_WRITEBUF_SIZE);
    }
    /**
     * Helper class for managing the read/write cache buffer
     * @author Lyor G.
     */
    protected static class DataCache {
        /**
         * Actual cache buffer
         */
        protected byte[]    buf /* =null */;
        /**
         * Current data size in buffer
         */
        protected int        curLen /* =0 */;
        /**
         * Next position in buffer to read from
         */
        protected int        curPos /* =0 */;
        /**
         * Creates a data cache of specified size
         * @param maxLen number of bytes to be allocated in the cache
         */
        protected DataCache (final int maxLen)
        {
            this.buf = new byte[maxLen];
        }
        /**
         * Marks cache buffer as empty
         */
        protected void reset ()
        {
            curLen = 0;
            curPos = 0;
        }
    }
    /**
     * Helper class for managing the read cache buffer
     * @author Lyor G.
     */
    private static final class ReadCache extends DataCache {
        /**
         * Creates a read cache of specified size
         * @param maxLen size of read cache to be used
         */
        protected ReadCache (final int maxLen)
        {
            super(maxLen);
        }
        /**
         * Reads from cache up to available size or requested one - whichever comes first
         * @param rbuf buffer to read into
         * @param offset offset to place read data into
         * @param len maximum number of characters to read
         * @return actual number of read characters
         */
        protected int read (final byte[] rbuf, final int offset, final int len)
        {
            if ((curPos >= curLen) || (len <= 0))
                return 0;

            final int    copyLen=Math.min(curLen - curPos, len);
            System.arraycopy(this.buf, curPos, rbuf, offset, copyLen);
            curPos += copyLen;

            return copyLen;
        }
        /**
         * @return number of valid characters that can still be read from the cache
         */
        protected int length ()
        {
            return (curLen >= curPos) ? (curLen - curPos) : 0;
        }
    }
    /**
     * Internal read cache - lazy allocation
     */
    private ReadCache    _rdCache /* =null */;
    /*
     * @see net.community.chest.net.TextSocket#available()
     */
    @Override
    public int available () throws IOException
    {
        final int    superAvail=super.available();    // find out how much the super class can provide
        if (_rdCache != null)    // if have cached, data then add it to the available length
            return superAvail + _rdCache.length();
        else
            return superAvail;
    }
    /**
     * Allocates a new read cache (if not already have one)
     * @return read cache
     * @see ReadCache
     * @see #_readBufSize
     */
    private ReadCache getReadCache ()
    {
        if (null == _rdCache)
            _rdCache = new ReadCache(_readBufSize);

        return _rdCache;
    }
    /*
     * @see net.community.chest.net.TextSocket#readBytes(byte[], int, int)
     */
    @Override
    public int readBytes (final byte[] buf, final int offset, final int len) throws IOException
    {
        // if no data requested, simply check that connection is alive
        if (0 == len)
        {
            if (!isOpen())
                throw new IOException("No current (buffered) connection to read bytes from");

            return 0;
        }

        ReadCache    rdc=_rdCache;
        if (null == rdc)
        {
            /*         If no current cache and requested more or less same as read cache size, pass along
             * the the request(makes no sense to cache this much data or allocate a cache for it)
             */
            if (len >= _readBufSize)
                return super.readBytes(buf, offset, len);

            // if less than cache size, then allocate a cache and fill it (will be done by subsequent code
            rdc = getReadCache();
        }

        // if no more data left in cache, then try to fill it
        if (rdc.length() <= 0)
        {
            if ((rdc.curLen=super.readBytes(rdc.buf, 0, rdc.buf.length)) <= 0)
                throw new IOException("Cannot re-fill read cache: " + rdc.curLen);

            rdc.curPos = 0;
        }

        // try to use cached data
        return rdc.read(buf, offset, len);
    }
    /*
     * @see net.community.chest.net.AbstractTextNetConnection#readLine(char[], int, int, net.community.chest.net.LineInfo)
     */
    @Override
    public int readLine (final char[] buf, final int startOffset, final int maxLen, final LineInfo li) throws IOException
    {
        li.reset();

        final ReadCache    rdc=getReadCache();
        // NOTE !!! we limit ourselves to ~32K re-fills of the read cache in order to avoid infinite loops
        for (int    curOffset=startOffset, refillIndex=0, readLen=0; refillIndex < Short.MAX_VALUE; refillIndex++)
        {
            // exhaust data in read cache
            for ( ; rdc.curPos < rdc.curLen; rdc.curPos++, readLen++)
            {
                if ('\n' == rdc.buf[rdc.curPos])
                {
                    rdc.curPos++;    // skip LF
                    li.setLFDetected(true);
                    return readLen+1;    // take into account the LF
                }
                else if ('\r' == rdc.buf[rdc.curPos])
                {
                    // NOTE !!! if the CR if not followed by a LF, then it will be omitted from the line data !!!
                    li.setCRDetected(true);
                    continue;
                }
                else    // "normal" character
                {
                    buf[curOffset] = (char) (rdc.buf[rdc.curPos] & 0x00FF);
                    curOffset++;

                    // if character BEFORE this was CR, then mark we do not have CR just before LF
                    li.setCRDetected(false);

                    if (li.incLength() >= maxLen)
                        return readLen;
                }
            }

            // this point is reached if exhausted read cache buffer - so re-fill it
            if ((rdc.curLen=super.readBytes(rdc.buf, 0, rdc.buf.length)) <= 0)
                throw new IOException("Cannot re-fill read cache: " + rdc.curLen);

            rdc.curPos = 0;
        }

        // this point is reached if exceeded max. re-fills
        throw new IOException("Virtual infinite loop exit while trying to read text line");
    }
    /*
     * @see net.community.chest.net.AbstractBinaryNetConnection#readBinaryLine(byte[], int, int, net.community.chest.net.LineInfo)
     */
    @Override
    public int readBinaryLine (final byte[] buf, final int startOffset, final int maxLen, final LineInfo li) throws IOException
    {
        if ((null == buf) || (startOffset < 0) || (maxLen < CRLF.length) || ((startOffset + maxLen) > buf.length) || (null == li))
            throw new IOException("Bad/Illegal bytes buffer and/or start offset/max length");
        li.reset();

        final ReadCache    rdc=getReadCache();
        // NOTE !!! we limit ourselves to ~32K re-fills of the read cache in order to avoid infinite loops
        for (int    curOffset=startOffset, refillIndex=0, readLen=0; refillIndex < Short.MAX_VALUE; refillIndex++)
        {
            // exhaust data in read cache
            for ( ; rdc.curPos < rdc.curLen; rdc.curPos++, readLen++)
            {
                if ('\n' == rdc.buf[rdc.curPos])
                {
                    rdc.curPos++;    // skip LF
                    li.setLFDetected(true);
                    return readLen+1;    // take into account the LF
                }
                else if ('\r' == rdc.buf[rdc.curPos])
                {
                    // NOTE !!! if the CR if not followed by a LF, then it will be omitted from the line data !!!
                    li.setCRDetected(true);
                    continue;
                }
                else    // "normal" character
                {
                    buf[curOffset] = rdc.buf[rdc.curPos];
                    curOffset++;

                    // if character BEFORE this was CR, then mark we do not have CR just before LF
                    li.setCRDetected(false);

                    if (li.incLength() >= maxLen)
                        return readLen;
                }
            }

            // this point is reached if exhausted read cache buffer - so re-fill it
            if ((rdc.curLen=super.readBytes(rdc.buf, 0, rdc.buf.length)) <= 0)
                throw new IOException("Cannot re-fill binary read cache: " + rdc.curLen);

            rdc.curPos = 0;
        }

        // this point is reached if exceeded max. re-fills
        throw new IOException("Virtual infinite loop exit while trying to read text line");
    }
    /**
     * Helper class for managing the write cache buffer
     * @author Lyor G.
     */
    private static final class WriteCache extends DataCache {
        /**
         * Creates a write cache of specified size
         * @param maxLen size of write cache to be allocated
         */
        protected WriteCache (final int maxLen)
        {
            super(maxLen);
        }
        /**
         * Appends buffer contents to current data
         * @param objBuf buffer from which to write
         * @param isBytes if TRUE then this is a byte[], otherwise a char[]
         * @param startPos index in buffer to start writing
         * @param maxLen number of bytes to write
         * @return true if successfully appended ENTIRE buffer
         */
        protected boolean appendBuffer (final Object objBuf, final boolean isBytes, final int startPos, final int maxLen)
        {
            if (0 == maxLen)
                return true;

            if (null == objBuf)
                return false;

            // make sure supplied parameters are OK
            final int    bufLen=isBytes ? ((byte[]) objBuf).length : ((char[]) objBuf).length;
            if ((startPos < 0) || (maxLen < 0) || ((startPos + maxLen) > bufLen))
                return false;
            // make sure can accommodate buffer
            if ((null == this.buf) || ((curPos + maxLen) > this.buf.length))
                return false;

            if (isBytes)
            {
                System.arraycopy(objBuf, startPos, this.buf, curPos, maxLen);
                curPos += maxLen;
            }
            else
            {
                final char[]    charBuf=(char[]) objBuf;
                // NOTE !!! character is NOT checked to ensure it is ASCII
                for (int    copyPos=startPos, copyLen=0; copyLen < maxLen; copyLen++, copyPos++, curPos++)
                    this.buf[curPos] = (byte) charBuf[copyPos];
            }

            curLen = curPos;
            return true;
        }
    }
    /**
     * Internal buffer used for accumulating written data
     */
    private WriteCache    _wrCache /* =null */;
    /**
     * @return the current write buffer (allocates if needed)
     * @see #_writeBufSize
     */
    private WriteCache getWriteCache ()
    {
        if (null == _wrCache)
            _wrCache = new WriteCache(_writeBufSize);

        return _wrCache;
    }
    /**
     * Outputs buffer contents to actual connection using the appropriate call
     * @param buf buffer from which to write
     * @param isBytes if TRUE then this is a byte[], otherwise a char[]
     * @param startPos index in buffer to start writing
     * @param maxLen number of bytes to write
     * @param flushIt if TRUE then channel is flushed AFTER writing the data
     * @return number of written bytes/characters (should be EXACTLY the same as <I>"maxLen"</I> parameter)
     * @throws IOException if network (or other errors)
     */
    private final int writeObject (final Object buf, final boolean isBytes, final int startPos, final int maxLen, final boolean flushIt) throws IOException
    {
        if (isBytes)
        {
            final int    written=super.writeBytes((byte[]) buf, startPos, maxLen);

            if (flushIt)
                flush();

            return written;
        }
        else
            return super.write((char[]) buf, startPos, maxLen, flushIt);
    }
    /**
     * Writes specified bytes as if they were 8-bit ASCII characters
     * @param buf buffer from which to write
     * @param isBytes if TRUE then this is a byte[], otherwise a char[]
     * @param startPos index in buffer to start writing
     * @param maxLen number of bytes to write
     * @param flushIt if TRUE then channel is flushed AFTER writing the data
     * @return number of written bytes/characters (should be EXACTLY the same as <I>"maxLen"</I> parameter)
     * @throws IOException if network (or other errors)
     */
    private final int writeCache (final Object buf, final boolean isBytes, final int startPos, final int maxLen, final boolean flushIt) throws IOException
    {
        if (!isOpen())
            throw new IOException("No current (buffered) I/O connection available for writing");
        if (0 == maxLen)
            return 0;

        // if don't have a write cache, and asked to flush anyway, do not create a write cache
        if ((null == _wrCache) && flushIt)
            return writeObject(buf, isBytes, startPos, maxLen, flushIt);

        if (null == _wrCache)
        {
            // if no current buffer, and requested data size is MORE than write buffer size,
            // then avoid caching it - more efficient to just write it in one stroke
            if (maxLen >= _writeBufSize)
                return writeObject(buf, isBytes, startPos, maxLen, flushIt);

            if (!getWriteCache().appendBuffer(buf, isBytes, startPos, maxLen))    // should not happen
                throw new IOException("Cannot append initial write bytes buffer data");
        }
        else    // at this point, we KNOW we have some previous cached data
        {
            final int    curBufSize=_wrCache.curLen, newBufSize=(curBufSize + maxLen);
            // check if can append to existing cached data
            if (newBufSize < _writeBufSize)
            {
                if (!_wrCache.appendBuffer(buf, isBytes, startPos, maxLen))
                    throw new IOException("Cannot append bytes to write buffer data");
            }
            else // at this point we know we cannot accomodate the new data - check how much will be left over
            {
                final int    remBufSize=newBufSize - _writeBufSize;
                // if remaining will be less than what we can cache, then append whatever we can, flush and cache the rest
                if (remBufSize < _writeBufSize)
                {
                    final int    appendSize=_writeBufSize - curBufSize;
                    if (!_wrCache.appendBuffer(buf, isBytes, startPos, appendSize))
                        throw new IOException("Cannot complete bytes write buffer with data");

                    flush();

                    if (!_wrCache.appendBuffer(buf, isBytes, startPos + appendSize, maxLen - appendSize))
                        throw new IOException("Cannot cache bytes write buffer with remaing data");
                }
                else    // even after completion and flushing we will have too much
                {
                    // simply flush our cache, and write the rest (without caching)
                    flush();

                    final int    nWritten=writeObject(buf, isBytes, startPos, maxLen, false);
                    if (nWritten != maxLen)
                        throw new StreamCorruptedException("Write bytes mismatch (" + maxLen + " <> " + nWritten + ") while writing");
                }
            }
        }

        if (flushIt)
            flush();

        return maxLen;
    }
    /*
     * @see net.community.chest.net.TextSocket#writeBytes(byte[], int, int, boolean)
     */
    @Override
    public int writeBytes (final byte[] buf, final int startPos, final int maxLen, final boolean flushIt) throws IOException
    {
        return writeCache(buf, true, startPos, maxLen, flushIt);
    }
    /*
     * @see net.community.chest.net.TextSocket#write(char[], int, int, boolean)
     */
    @Override
    public int write (final char[] buf, final int startOffset, final int maxLen, final boolean flushIt) throws IOException
    {
        return writeCache(buf, false, startOffset, maxLen, flushIt);
    }
    /*
     * @see net.community.chest.net.TextSocket#flush()
     */
    @Override
    public void flush () throws IOException
    {
        if (null == _wrCache)    // OK if no current data to write
            return;

        final int    writeLen=_wrCache.curLen;
        if (writeLen <= 0)
            return;

        final ByteBuffer    bb=ByteBuffer.wrap(_wrCache.buf, 0, writeLen);
        final int            writtenLen=getChannel().write(bb);
        if (writtenLen != writeLen)
            throw new IOException("Flush cache write mismatch (" + writeLen + " <> " + writtenLen + ")");
        _wrCache.reset();    // prepare for re-accumulating

        super.flush();
    }
    /*
     * @see net.community.chest.net.TextSocket#skip(long)
     */
    @Override
    public long skip (final long skipSize) throws IOException
    {
        if (skipSize <= 0)
            return skipSize;

        // if no currently cached data, then call the super-class
        ReadCache    rdc=_rdCache;
        if (null == rdc)
            return super.skip(skipSize);

        // calculate how much is "skipped" from the cache buffer
        final int    virtualSkip=(int) Math.min(skipSize, rdc.length());
        final long    remainSkip=(skipSize - virtualSkip);

        rdc.curPos += virtualSkip;
        // if by skipping exhausted cache, then prepare for next time
        if (rdc.curPos >= rdc.curLen)
            rdc.reset();

        // check if have something to skip
        if (remainSkip > 0)
        {
            final long    subSkip=super.skip(remainSkip);
            if (subSkip != remainSkip)
                throw new IOException("Mismatched sub-skip size (" + subSkip + " <> " + remainSkip + ")");
        }

        return skipSize;
    }
    /*
     * @see net.community.chest.net.TextSocket#close()
     */
    @Override
    public void close () throws IOException
    {
        if (_rdCache != null)
            _rdCache.reset();
        if (_wrCache != null)
            _wrCache.reset();

        super.close();
    }
}

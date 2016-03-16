/*
 *
 */
package net.community.chest.db.sql.io;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.nio.channels.Channel;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import net.community.chest.io.FileUtil;
import net.community.chest.io.file.FileIOUtils;
import net.community.chest.io.input.RandomAccessFileInputStream;
import net.community.chest.io.output.RandomAccessFileOutputStream;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 1, 2009 1:40:54 PM
 */
public class BlobFile extends File implements Blob, Closeable, Channel {
    /**
     *
     */
    private static final long serialVersionUID = 1733707678924887570L;
    public BlobFile (String pathname)
    {
        super(pathname);
    }

    public BlobFile (File f)
    {
        this(f.getAbsolutePath());
    }

    public BlobFile (URI uri)
    {
        super(uri);
    }

    public BlobFile (String parent, String child)
    {
        super(parent, child);
    }

    public BlobFile (File parent, String child)
    {
        super(parent, child);
    }

    private Boolean    _accessMode    /* null == read/write, TRUE=read, FALSE=write */;
    public Boolean getReadAccessMode ()
    {
        return _accessMode;
    }

    public void setReadAccessMode (Boolean m)
    {
        if (_accessMode != m)
            _accessMode = m;
    }

    private Collection<InputStream>    _inStreams;
    public void closeInputStreams () throws IOException
    {
        FileUtil.closeAll(_inStreams);
    }

    private Collection<OutputStream>    _outStreams;
    public void closeOutputStreams () throws IOException
    {
        FileUtil.closeAll(_outStreams);
    }

    private RandomAccessFile    _acc    /* =null */;
    public void closeAccessor () throws IOException
    {
        if (_acc != null)
        {
            try
            {
                FileUtil.closeAll(_acc);
            }
            finally
            {
                _acc = null;
            }
        }
    }

    private boolean    _closedState    /* =false */;
    /*
     * @see java.nio.channels.Channel#isOpen()
     */
    @Override
    public boolean isOpen ()
    {
        return _closedState;
    }
    // CAVEAT EMPTOR !!!
    public void setOpen (boolean f)
    {
        _closedState = (!f);
    }
    /*
     * @see java.sql.Blob#free()
     */
    @Override
    public void free () throws SQLException
    {
        IOException    ioe=null;

        try
        {
            closeInputStreams();
        }
        catch(IOException e)
        {
//            if (null == ioe)
                ioe = e;
        }

        try
        {
            closeOutputStreams();
        }
        catch(IOException e)
        {
            if (null == ioe)
                ioe = e;
        }

        try
        {
            closeAccessor();
        }
        catch(IOException e)
        {
            if (null == ioe)
                ioe = e;
        }

        if (isOpen())
            setOpen(false);

        if (ioe != null)
            throw new SQLException("free(" + getAbsolutePath() + ") " + ioe.getClass().getName() + ": " + ioe.getMessage());
    }
    /*
     * @see java.io.Closeable#close()
     */
    @Override
    public void close () throws IOException
    {
        try
        {
            free();
        }
        catch (SQLException e)
        {
            throw new EOFException("close(" + getAbsolutePath() + ") " + e.getClass().getName() + " while free-ing: " + e.getMessage());
        }
    }

    protected synchronized RandomAccessFile getAccessor (boolean createIfNotExist) throws IOException
    {
        if ((null == _acc) && createIfNotExist)
        {
            if (!isOpen())
                throw new StreamCorruptedException("createAccessor(" + getAbsolutePath() + ") closed");

            final Boolean    m=getReadAccessMode();
            final String    fm;
            if (null == m)
                fm = "rw";
            else
                fm = m.booleanValue() ? "r" : "w";
            _acc = new RandomAccessFile(this, fm);
        }

        return _acc;
    }

    public RandomAccessFile getAccessor () throws IOException
    {
        return getAccessor(false);
    }

    public void setAccessor (RandomAccessFile acc)
    {
        _acc = acc;
    }
    /*
     * @see java.sql.Blob#getBinaryStream(long, long)
     */
    @Override
    public InputStream getBinaryStream (long pos, long length) throws SQLException
    {
        try
        {
            final RandomAccessFile    acc=getAccessor(true);
            if (null == acc)
                throw new StreamCorruptedException("no accessor");

            final InputStream    in=new RandomAccessFileInputStream(acc, pos, length, false);
            if (null == _inStreams)
                _inStreams = new LinkedList<InputStream>();
            _inStreams.add(in);
            return in;
        }
        catch(IOException e)
        {
            throw new SQLException("getBinaryStream(" + getAbsolutePath() + ")[" + pos + "/" + length + "] " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
    /*
     * @see java.sql.Blob#getBinaryStream()
     */
    @Override
    public InputStream getBinaryStream () throws SQLException
    {
        return getBinaryStream(0L, length());
    }
    /*
     * @see java.sql.Blob#getBytes(long, int)
     */
    @Override
    public byte[] getBytes (long pos, int length) throws SQLException
    {
        try
        {
            final RandomAccessFile    acc=getAccessor(true);
            if (null == acc)
                throw new StreamCorruptedException("no accessor");

            if (length <= 0)
                return FileIOUtils.EMPTY_BYTES;

            acc.seek(pos);

            final byte[]    b=new byte[length];
            final int        rLen=acc.read(b);
            if (rLen >= length)
                return b;

            final byte[]    rb=new byte[rLen];
            System.arraycopy(b, 0, rb, 0, rLen);
            return rb;
        }
        catch(IOException e)
        {
            throw new SQLException("getBytes(" + getAbsolutePath() + ")[" + pos + "/" + length + "] " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
    /*
     * @see java.sql.Blob#position(byte[], long)
     */
    @Override
    public long position (byte[] pattern, long start) throws SQLException
    {
        throw new SQLException("position(" + getAbsolutePath() + ")[" + start + "] N/A");
    }
    /*
     * @see java.sql.Blob#position(java.sql.Blob, long)
     */
    @Override
    public long position (Blob pattern, long start) throws SQLException
    {
        throw new SQLException("position(" + getAbsolutePath() + ")[" + start + "] N/A");
    }
    /*
     * @see java.sql.Blob#setBinaryStream(long)
     */
    @Override
    public OutputStream setBinaryStream (long pos) throws SQLException
    {
        try
        {
            final RandomAccessFile    acc=getAccessor(true);
            if (null == acc)
                throw new StreamCorruptedException("no accessor");

            final OutputStream    out=new RandomAccessFileOutputStream(acc, pos, false);
            if (null == _outStreams)
                _outStreams = new LinkedList<OutputStream>();
            _outStreams.add(out);
            return out;
        }
        catch(IOException e)
        {
            throw new SQLException("setBinaryStream(" + getAbsolutePath() + ")[" + pos + "] " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
    /*
     * @see java.sql.Blob#setBytes(long, byte[], int, int)
     */
    @Override
    public int setBytes (long pos, byte[] bytes, int offset, int len) throws SQLException
    {
        try
        {
            final RandomAccessFile    acc=getAccessor(true);
            if (null == acc)
                throw new StreamCorruptedException("no accessor");

            acc.seek(pos);
            acc.write(bytes, offset, len);
            return len;
        }
        catch(IOException e)
        {
            throw new SQLException("setBytes(" + getAbsolutePath() + ")[" + pos + "/" + len + "] " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
    /*
     * @see java.sql.Blob#setBytes(long, byte[])
     */
    @Override
    public int setBytes (long pos, byte[] bytes) throws SQLException
    {
        return setBytes(pos, bytes, 0, bytes.length);
    }
    /*
     * @see java.sql.Blob#truncate(long)
     */
    @Override
    public void truncate (long len) throws SQLException
    {
        try
        {
            final RandomAccessFile    acc=getAccessor(true);
            if (null == acc)
                throw new StreamCorruptedException("no accessor");
            acc.setLength(len);
        }
        catch(IOException e)
        {
            throw new SQLException("truncate(" + getAbsolutePath() + ")[" + len + "] " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}

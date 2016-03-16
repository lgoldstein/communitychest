package net.community.chest.net.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamCorruptedException;

import net.community.chest.io.OptionallyCloseable;
import net.community.chest.net.NetConnectionEmbedder;
import net.community.chest.net.TextNetConnection;

/**
 * Copyright 2007 as per GPLv2
 *
 * Embeds a {@link TextNetConnection} as a {@link Reader}
 *
 * @author Lyor G.
 * @since Jul 4, 2007 8:33:19 AM
 */
public class NetTextReader extends Reader
                implements OptionallyCloseable, NetConnectionEmbedder<TextNetConnection> {
    /*
     * @see net.community.chest.io.OptionallyCloseable#isMutableRealClosure()
     */
    @Override
    public boolean isMutableRealClosure ()
    {
        return true;
    }

    private boolean    _realClosure;
    /*
     * @see net.community.chest.io.OptionallyCloseable#isRealClosure()
     */
    @Override
    public boolean isRealClosure ()
    {
        return _realClosure;
    }
    /*
     * @see net.community.chest.io.OptionallyCloseable#setRealClosure(boolean)
     */
    @Override
    public void setRealClosure (boolean enabled) throws UnsupportedOperationException
    {
        _realClosure = enabled;
    }

    private TextNetConnection    _conn    /* =null */;
    /*
     * @see net.community.chest.net.NetConnectionEmbedder#getConnection()
     */
    @Override
    public TextNetConnection getConnection ()
    {
        return _conn;
    }
    /*
     * @see net.community.chest.net.NetConnectionEmbedder#setConnection(net.community.chest.net.NetConnection)
     */
    @Override
    public void setConnection (TextNetConnection conn)
    {
        _conn = conn;
    }
     /**
      * @param conn {@link TextNetConnection} to "mask" as a {@link Reader}
      * @param autoClose TRUE if {@link #close()} call should also close the
      * underlying {@link TextNetConnection} (if any)
      * @throws IOException if no initial connection provided to mask
      * @see #setConnection(TextNetConnection)
      * @see #setRealClosure(boolean)
      */
    public NetTextReader (TextNetConnection conn, boolean autoClose) throws IOException
    {
        if (null == (_conn=conn))
            throw new IOException("No " + TextNetConnection.class.getName() + " instance to mask as a " + getClass().getName());

        _realClosure = autoClose;
    }
     /**
      * @param conn {@link TextNetConnection} to "mask" as an {@link Reader}
      * <B>Note:</B> automatically closes the the connection when {@link #close()}
      * is called on the stream (unless {@link #setRealClosure(boolean)} called
      * previous to that)
      * @throws IOException if no initial connection provided to mask
      */
    public NetTextReader (TextNetConnection conn) throws IOException
    {
        this(conn, true);
    }
    /*
     * @see java.io.Reader#mark(int)
     */
    @Override
    public void mark (int location) throws IOException
    {
        final TextNetConnection    conn=getConnection();
        if (null == conn)
            throw new EOFException("No connection to mark");
        else
            throw new StreamCorruptedException("Mark=" + location + " N/A for this object");
    }
    /*
     * @see java.io.Reader#markSupported()
     */
    @Override
    public boolean markSupported ()
    {
        return false;
    }
    /*
     * @see java.io.Reader#read()
     */
    @Override
    public int read () throws IOException
    {
        final TextNetConnection    conn=getConnection();
        if (null == conn)
            throw new EOFException("No connection to read(1) from");

        return conn.read();
    }
    /*
     * @see java.io.Reader#read(char[], int, int)
     */
    @Override
    public int read (char[] buf, int offset, int len) throws IOException
    {
        final TextNetConnection    conn=getConnection();
        if (null == conn)
            throw new EOFException("No connection to read from");

        return conn.read(buf, offset, len);
    }
    /*
     * @see java.io.Reader#read(char[])
     */
    @Override
    public int read (char[] buf) throws IOException
    {
        return read(buf, 0, (null == buf) ? 0 : buf.length);
    }
    /*
     * @see java.io.Reader#ready()
     */
    @Override
    public boolean ready() throws IOException
    {
        final TextNetConnection    conn=getConnection();
        if (null == conn)
            throw new EOFException("No connection to check if ready");

        return true;
    }
    /*
     * @see java.io.Reader#reset()
     */
    @Override
    public void reset() throws IOException
    {
        final TextNetConnection    conn=getConnection();
        if (null == conn)
            throw new EOFException("No connection to reset");
        else
            throw new StreamCorruptedException("Reset N/A for this object");
    }
    /*
     * @see java.io.Reader#skip(long)
     */
    @Override
    public long skip (long skipLen) throws IOException
    {
        final TextNetConnection    conn=getConnection();
        if (null == conn)
            throw new EOFException("No connection to reset");

        return conn.skip(skipLen);
    }
    /*
     * @see java.io.Reader#close()
     */
    @Override
    public void close() throws IOException
    {
        try
        {
            final TextNetConnection    conn=getConnection();
            if ((conn != null) && isRealClosure())
                conn.close();
        }
        finally
        {
            setConnection(null);
        }
    }
     /**
      * @param conn {@link TextNetConnection} to "mask" as a {@link Reader}
      * @param autoClose TRUE if {@link #close()} call should also close the
      * underlying {@link TextNetConnection} (if any)
      * @return The {@link NetTextReader} instance encompassing the connection
      * @throws IOException if no initial connection provided to mask
      */
    public static final NetTextReader asReader (TextNetConnection conn, boolean autoClose) throws IOException
    {
        return new NetTextReader(conn, autoClose);
    }
}

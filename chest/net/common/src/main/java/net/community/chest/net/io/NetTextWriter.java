package net.community.chest.net.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.io.Writer;

import net.community.chest.io.OptionallyCloseable;
import net.community.chest.net.NetConnectionEmbedder;
import net.community.chest.net.TextNetConnection;

/**
 * Copyright 2007 as per GPLv2
 *
 * <P>Embeds a {@link TextNetConnection} as a {@link Writer}</P>
 *
 * @author Lyor G.
 * @since Jul 4, 2007 8:42:58 AM
 */
public class NetTextWriter extends Writer
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
     * @param conn {@link TextNetConnection} to "mask" as a {@link Writer}
     * @param autoClose TRUE if {@link #close()} call should also close the
     * underlying {@link TextNetConnection} (if any)
     * @throws IOException if no initial connection provided to mask
     * @see #setConnection(TextNetConnection)
     * @see #setRealClosure(boolean)
     */
    public NetTextWriter (TextNetConnection conn, boolean autoClose) throws IOException
    {
        if (null == (_conn=conn))
            throw new IOException("No " + TextNetConnection.class.getName() + " instance to mask as a " + getClass().getName());

        _realClosure = autoClose;
    }
    /**
     * @param conn {@link TextNetConnection} to "mask" as an {@link Writer}
     * <B>Note:</B> automatically closes the the connection when {@link #close()}
     * is called on the stream (unless {@link #setRealClosure(boolean)} called
     * previous to that)
     * @throws IOException if no initial connection provided to mask
     */
    public NetTextWriter (TextNetConnection conn) throws IOException
    {
        this(conn, true);
    }
    /*
     * @see java.io.Writer#flush()
     */
    @Override
    public void flush() throws IOException
    {
        final TextNetConnection    conn=getConnection();
        if (null == conn)
            throw new EOFException("No connection to flush");

        conn.flush();
    }
    /*
     * @see java.io.Writer#write(char[], int, int)
     */
    @Override
    public void write (char[] buf, int offset, int len) throws IOException
    {
        final TextNetConnection    conn=getConnection();
        if (null == conn)
            throw new EOFException("No connection to write to");

        final int    nWritten=conn.write(buf, offset, len);
        if (nWritten != len)
            throw new StreamCorruptedException("Text output chars num (" + nWritten + ") mismatch than requested=" + len);
    }
    /*
     * @see java.io.Writer#write(char[])
     */
    @Override
    public void write (char[] buf) throws IOException
    {
        write(buf, 0, (null == buf) ? 0 : buf.length);
    }
    /*
     * @see java.io.Writer#write(int)
     */
    @Override
    public void write (int val) throws IOException
    {
        final TextNetConnection    conn=getConnection();
        if (null == conn)
            throw new EOFException("No connection to write(1)");

        final int    nWritten=conn.write(val);
        if (nWritten != 1)
            throw new StreamCorruptedException("Written " + nWritten + " instead of EXACTLY 1");
    }
    /*
     * @see java.io.Writer#write(java.lang.String, int, int)
     */
    @Override
    public void write (String s, int offset, int len) throws IOException
    {
        final TextNetConnection    conn=getConnection();
        if (null == conn)
            throw new EOFException("No connection to write string to");

        final int        sLen=(null == s) ? 0 : s.length();
        // if offset=0 AND len=s.length() then can use original string to write from
        final String    ws=((0 == offset) && (sLen == len)) ? s : s.substring(offset, offset+len);
        final int        nWritten=conn.write(ws);
        if (nWritten != len)
            throw new StreamCorruptedException("Mismatched written string=" + ws + " length(" + nWritten + "<> " + len + ")");
    }
    /*
     * @see java.io.Writer#write(java.lang.String)
     */
    @Override
    public void write (String s) throws IOException
    {
        write(s, 0, (null == s) ? 0 : s.length());
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
      * @param conn {@link TextNetConnection} to "mask" as a {@link Writer}
      * @param autoClose TRUE if {@link #close()} call should also close the
      * underlying {@link TextNetConnection} (if any)
      * @return The {@link NetTextWriter} instance encompassing the connection
      * @throws IOException if no initial connection provided to mask
      */
    public static final NetTextWriter asWriter (TextNetConnection conn, boolean autoClose) throws IOException
    {
        return new NetTextWriter(conn, autoClose);
    }
}

package net.community.chest.net.proto.text;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.nio.channels.SocketChannel;

import net.community.chest.io.FileUtil;
import net.community.chest.net.BufferedTextSocket;
import net.community.chest.net.TextNetConnection;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 19, 2007 11:14:28 AM
 */
public abstract class AbstractTextProtocolNetConnectionHelper extends
                                AbstractTextProtocolNetConnection {
    protected AbstractTextProtocolNetConnectionHelper ()
    {
        super();
    }
    /**
     * Actual text connection
     */
    private TextNetConnection    _conn    /* =null */;
    /*
     * @see net.community.chest.net.proto.text.AbstractTextProtocolNetConnection#getTextNetConnection()
     */
    @Override
    public TextNetConnection getTextNetConnection ()
    {
        return _conn;
    }
    /* Break the symmetry
     * @see net.community.chest.net.AbstractNetConnection#getChannel()
     */
    @Override
    public SocketChannel getChannel ()
    {
        final TextNetConnection    conn=getTextNetConnection();
        if (conn != null)
            return conn.getChannel();

        return null;
    }
    /* Break the symmetry
     * @see net.community.chest.net.AbstractNetConnection#detachChannel()
     */
    @Override
    public SocketChannel detachChannel () throws IOException
    {
        final TextNetConnection    conn=getTextNetConnection();
        if (conn != null)
            return conn.detachChannel();
        return null;
    }

    public void setTextNetConnection (TextNetConnection conn)
    {
        _conn = conn;
    }
    /**
     * Timeout (msec.) for waiting on response from the remote server
     */
    private int _timeMillis    /* =0 */;
    /*
     * @see net.community.chest.net.NetConnection#getReadTimeout()
     */
    @Override
    public int getReadTimeout ()
    {
        return _timeMillis;
    }
    /*
     * @see net.community.chest.net.NetConnection#setReadTimeout(int)
     */
    @Override
    public void setReadTimeout (final int timeMillis) throws IOException
    {
        final TextNetConnection    conn=getTextNetConnection();
        if (conn != null)
            conn.setReadTimeout(timeMillis);
        else if (timeMillis < 0)
            throw new IOException(ClassUtil.getArgumentsExceptionLocation(getClass(), "setReadTimeout", Integer.valueOf(timeMillis)) + " bad/illegal value");

        _timeMillis = timeMillis;
    }
    /*
     * @see net.community.chest.net.proto.text.AbstractTextProtocolNetConnection#connect(java.lang.String, int)
     */
    @Override
    public void connect (String host, int port) throws IOException
    {
        final TextNetConnection    conn=getTextNetConnection();
        if (null == conn)
            throw new IOException("No set text next connection to connect through");
        // do not allow connecting to an already connected session
        if (isOpen())
            throw new StreamCorruptedException(ClassUtil.getArgumentsExceptionLocation(getClass(), "connect", host, Integer.valueOf(port)) + " already connected/attached");

        conn.connect(host, port);
    }
    /*
     * @see net.community.chest.net.AbstractNetConnection#attach(java.nio.channels.SocketChannel)
     */
    @Override
    public void attach (SocketChannel channel) throws IOException
    {
        if (null == channel)
            throw new IOException(ClassUtil.getExceptionLocation(getClass(), "attach") + " no " + SocketChannel.class.getName() + " instance provided");

        // do not allow connecting to an already connected session
        if (isOpen())
            throw new StreamCorruptedException(ClassUtil.getExceptionLocation(getClass(), "attach") + " already connected/attached");

        final BufferedTextSocket    tmpConn=new BufferedTextSocket();
        tmpConn.setReadTimeout(_timeMillis);
        tmpConn.attach(channel);
        _conn = tmpConn;
    }
    /*
     * @see net.community.chest.net.AbstractNetConnection#close()
     */
    @Override
    public void close () throws IOException
    {
        IOException superExc=null;
        try
        {
            super.close();
        }
        catch(IOException e)
        {
            superExc = e;
        }

        if (_conn != null)
        {
            try
            {
                FileUtil.closeAll(_conn);
            }
            finally
            {
                _conn = null;
            }
        }

        if (superExc != null)
            throw superExc;
    }
    /* Override to ensure closure
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        close();
        super.finalize();
    }
}

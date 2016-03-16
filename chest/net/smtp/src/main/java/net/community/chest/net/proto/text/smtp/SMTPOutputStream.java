package net.community.chest.net.proto.text.smtp;

import java.io.IOException;
import java.io.OutputStream;

import net.community.chest.io.OptionallyCloseable;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 8:05:23 AM
 */
public class SMTPOutputStream extends OutputStream implements OptionallyCloseable {
    public SMTPOutputStream ()
    {
        super();
    }

    private boolean    _autoClose    /* =false */;
    /*
     * @see net.community.chest.io.OptionallyCloseable#isRealClosure()
     */
    @Override
    public boolean isRealClosure ()
    {
        return _autoClose;
    }
    /*
     * @see net.community.chest.io.OptionallyCloseable#setRealClosure(boolean)
     */
    @Override
    public void setRealClosure (boolean enabled) throws UnsupportedOperationException
    {
        _autoClose = enabled;
    }
    /*
     * @see net.community.chest.io.OptionallyCloseable#isMutableRealClosure()
     */
    @Override
    public boolean isMutableRealClosure ()
    {
        return true;
    }

    private SMTPAccessor    _sess /* =null */;
    public SMTPAccessor getAccessor ()
    {
        return _sess;
    }

    public void setAccessor (SMTPAccessor sess)
    {
        _sess = sess;
    }

    private boolean _addCRLFOnClose /* =false */;
    public boolean isAddCRLFOnClose ()
    {
        return _addCRLFOnClose;
    }

    public void setAddCRLFOnClose (boolean enabled)
    {
        _addCRLFOnClose = enabled;
    }
    /**
     * Constructor for derived classess
     * @param sess accessor instance - may NOT be null
     * @param autoClose if TRUE then {@link #close()} call also closes
     * @param addCRLFOnClose if TRUE then {@link #close()} adds a CRLF
     * before ending the DATA stage
     * the session object
     */
    protected SMTPOutputStream (SMTPAccessor sess, boolean autoClose, boolean addCRLFOnClose)
    {
        if (null == (_sess=sess))
            throw new IllegalArgumentException("No SMTP accessor to mask as output stream");
        _autoClose = autoClose;
        _addCRLFOnClose = addCRLFOnClose;
    }
    /**
     * Constructor for derived classess
     * @param sess accessor instance - may NOT be null
     * @param autoClose if TRUE then {@link #close()} call also closes
     * the session object
     */
    protected SMTPOutputStream (SMTPAccessor sess, boolean autoClose)
    {
        this(sess, autoClose, true);
    }
    /*
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush () throws IOException
    {
        final SMTPAccessor    sess=getAccessor();
        if (null == sess)
            throw new IOException("No SMTP session to flush");
        sess.flush();
    }
    /*
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close () throws IOException
    {
        final SMTPAccessor    sess=getAccessor();
        if (isRealClosure() && (sess != null))
        {
            SMTPResponse    rsp=null;
            IOException        rspExc=null;
            try
            {
                rsp = sess.endData(isAddCRLFOnClose());

                sess.quit();    // don't care what the response is...
            }
            catch(IOException ioe)
            {
                rspExc = ioe;
            }
            finally
            {
                try
                {
                    sess.close();
                }
                catch(IOException ce)
                {
                    // ignored
                }
            }

            // if reached this point and no DATA end stage response, then exception occurred in it
            if (null == rsp)
                throw rspExc;

            // check DATA stage end result and make sure ended successfully
            if (rsp.getRspCode() != SMTPProtocol.SMTP_E_ACTION_OK)
                throw new IOException("Failed to end DATA stage: " + rsp.toString());
        }

        if (sess != null)
            setAccessor(null);
    }
    /*
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write (byte[] buf, int offset, int len) throws IOException
    {
        final SMTPAccessor    sess=getAccessor();
        if (null == sess)
            throw new IOException("No SMTP accessor to write into");

        final int    written=sess.writeBytes(buf, offset, len, false);
        if (written != len)
            throw new IOException("SMTP session output stream write mismatch (" + written + " <> " + len + ")");
    }
    /*
     * @see java.io.OutputStream#write(byte[])
     */
    @Override
    public void write (byte[] buf) throws IOException
    {
        write(buf, 0, (null == buf) ? 0 : buf.length);
    }
    /*
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write (int val) throws IOException
    {
        // make sure written value is a byte
        if ((val < Byte.MIN_VALUE) || (val > Byte.MAX_VALUE))
            throw new IOException("Non-byte value to write: " + val);

        write(new byte[] { (byte) val }, 0, 1);
    }
    /*
     * just making sure stream is being closed
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        try
        {
            close();
        }
        catch(IOException ioe)
        {
            /* ignored */
        }
        super.finalize();
    }
}

package net.community.chest.io.encode.qp;

import java.io.IOException;
import java.io.InputStream;

import net.community.chest.io.encode.hex.Hex;
import net.community.chest.io.input.InputStreamEmbedder;

/**
 * Helper class that reads from a quoted-printable input stream and
 * returns decoded data
 * @author lyorg
 * 04/03/2004
 */
public class QPDecodeInputStream extends InputStreamEmbedder {
    private final int    _nOptions;
    /**
     * Base constructor
     * @param ist "real" input stream assumed to contain QP data
     * @param nOptions decoding options
     * @param realClose if TRUE then call to {@link #close()} also closes
     * the underlying stream
     * @see QuotedPrintable#DECOPT_UNDERLINE_AS_SPACE
     * @see QuotedPrintable#DECOPT_THROW_EXCEPTION
     */
    public QPDecodeInputStream (InputStream ist, int nOptions, boolean realClose)
    {
        super(ist, realClose);
        _nOptions = nOptions;
    }
    /**
     * Simple constructor - throws exceptions if non-QP data found
     * @param ist "real" input stream assumed to contain QP data
     * @param realClose if TRUE then call to {@link #close()} also closes
     * the underlying stream
     */
    public QPDecodeInputStream (InputStream ist, boolean realClose)
    {
        this(ist, QuotedPrintable.DECOPT_THROW_EXCEPTION, realClose);
    }
    /*
     * @see java.io.InputStream#read()
     */
    @Override
    public int read () throws IOException
    {
        if (null == this.in)
            return (-1);

        for (int    nRead=0; ; nRead++)
        {
            int c=this.in.read();
            if (c != QuotedPrintable.QPDELIM)   // if this is NOT the Q-P delimiter, then return it as-is
            {
                if (('_' == c) && (QuotedPrintable.DECOPT_UNDERLINE_AS_SPACE == (_nOptions & QuotedPrintable.DECOPT_UNDERLINE_AS_SPACE)))
                    return ' ';
                else
                    return c;
            }

            // we read a '=' - check what comes next
            if ((-1) == (c=this.in.read()))
                return (-1);

            // skip soft CR-LF
            if ('\r' == c)
            {
                if ((c=this.in.read()) != '\n')
                {
                    if (QuotedPrintable.DECOPT_THROW_EXCEPTION == (_nOptions & QuotedPrintable.DECOPT_THROW_EXCEPTION))
                        throw new IOException("Missing LF in soft line break after " + nRead + " characters");
                    else
                        return c;
                }

                continue;   // skip the LF
            }
            else if ('\n' == c)
                continue;

            final int hiChar=c, loChar=this.in.read();
            if (((-1) == hiChar) || ((-1) == loChar))
            {
                if (QuotedPrintable.DECOPT_THROW_EXCEPTION == (_nOptions & QuotedPrintable.DECOPT_THROW_EXCEPTION))
                    throw new IllegalStateException("bad/illegal QP encoding after " + nRead + " characters");
                else
                    return QuotedPrintable.QPDELIM;
            }
            else
                return (Hex.rebuild((char) hiChar, (char) loChar) & 0x00FF);
        }
    }
    /*
     * @see java.io.InputStream#skip(long)
     */
    @Override
    public long skip (long n) throws IOException
    {
        if (n <= 0)
            return 0;

        for (long   p=0; p < n; p++)
            if ((-1) == read())
                return p;

        // this point is reached if end of input not reached before N characters read
        return n;
    }
}

package net.community.chest.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.io.Writer;

import net.community.chest.io.EOLStyle;
import net.community.chest.net.io.NetTextReader;
import net.community.chest.net.io.NetTextWriter;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jul 4, 2007 8:47:19 AM
 */
public abstract class AbstractTextNetConnection extends AbstractBinaryNetConnection implements TextNetConnection {
    protected static final char[]    CRLF=EOLStyle.CRLF.getStyleChars();
    protected AbstractTextNetConnection ()
    {
        super();
    }
    /*
     * @see net.community.chest.net.TextNetConnection#read(char[])
     */
    @Override
    public int read (final char[] buf) throws IOException
    {
        return read(buf, 0, (null == buf) ? 0 : buf.length);
    }
    /*
     * @see net.community.chest.net.BinaryNetConnection#read()
     */
    @Override
    public int read () throws IOException
    {
        char[]    oneChar={ '\0' };
        int        nRead=read(oneChar, 0, 1);
        if (nRead != 1)
            throw new StreamCorruptedException("Read count mismatch while read one char: " + nRead);

        return oneChar[0];
    }
    /*
     * @see net.community.chest.net.TextNetConnection#fill(char[], int, int)
     */
    @Override
    public int fill (final char[] buf, final int offset, final int len) throws IOException
    {
        for (int curLen=0; curLen < len; )
        {
            final int    readLen=read(buf, offset + curLen, len - curLen);
            if (readLen <= 0)
                throw new EOFException("Premature EOF in text socket after " + curLen + " characters while trying to fill len=" + len);

            curLen += readLen;
        }

        return len;
    }
    /*
     * @see net.community.chest.net.TextNetConnection#fill(char[])
     */
    @Override
    public int fill (final char[] buf) throws IOException
    {
        return fill(buf, 0, (null == buf) ? 0 : buf.length);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#readLine(char[], int, int, net.community.chest.net.LineInfo)
     */
    @Override
    public int readLine (final char[] buf, final int startOffset, final int maxLen, final LineInfo li) throws IOException
    {
        if ((null == buf) || (startOffset < 0) || (maxLen < CRLF.length) || ((startOffset + maxLen) > buf.length) || (null == li))
            throw new IOException("Bad/Illegal char buffer and/or start offset/max length");
        li.reset();

        // we limit ourselves to ~32K character per-line (good for most known text protocols)
        for (int    curOffset=startOffset, readLen=0; readLen < Short.MAX_VALUE; readLen++)
        {
            final int    val=read();
            if ((-1) == val)
                throw new EOFException("Premature EOF while attempting to read line (char-by-char)");

            if ('\n' == val)
            {
                li.setLFDetected(true);
                return readLen+1;    // take into account the LF
            }
            else if ('\r' == val)
            {
                // NOTE !!! if the CR if not followed by a LF, then it will be omitted from the line data !!!
                li.setCRDetected(true);
                continue;
            }
            else    // "normal" character
            {
                buf[curOffset] = (char) val;
                curOffset++;

                // if character BEFORE this was CR, then mark we do not have CR just before LF
                li.setCRDetected(false);

                // check if exhausted
                if (li.incLength() >= maxLen)
                    return readLen;
            }
        }

        throw new StreamCorruptedException("Virtual infinite loop exit on read line char-by-char");
    }
    /*
     * @see net.community.chest.net.TextNetConnection#readLine(char[], net.community.chest.net.LineInfo)
     */
    @Override
    public int readLine (final char[] buf, final LineInfo li) throws IOException
    {
        return readLine(buf, 0, (null == buf) ? 0 : buf.length, li);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#readLine(char[], int, int)
     */
    @Override
    public LineInfo readLine (final char[] buf, final int startOffset, final int maxLen) throws IOException
    {
        final LineInfo    li=new LineInfo();
        final int        nErr=readLine(buf, startOffset, maxLen, li);
        if (nErr < 0)
            throw new IOException("Cannot fill line info: err=" + nErr);
        return li;
    }
    /*
     * @see net.community.chest.net.TextNetConnection#readLine(char[])
     */
    @Override
    public LineInfo readLine (final char[] buf) throws IOException
    {
        return readLine(buf, 0, (null == buf) ? 0 : buf.length);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#asReader(boolean)
     */
    @Override
    public Reader asReader (boolean autoClose) throws IOException
    {
        return NetTextReader.asReader(this, autoClose);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#write(char[], boolean)
     */
    @Override
    public int write (final char[] buf, final boolean flushIt) throws IOException
    {
        return write(buf, 0, (null == buf) ? 0 : buf.length, flushIt);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#write(char[], int, int)
     */
    @Override
    public int write (final char[] buf, final int startOffset, final int maxLen) throws IOException
    {
        return write(buf, startOffset, maxLen, false);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#write(char[])
     */
    @Override
    public int write (char ... buf) throws IOException
    {
        return write(buf, 0, (null == buf) ? 0 : buf.length);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#write(java.lang.String, boolean)
     */
    @Override
    public int write (String s, final boolean flushIt) throws IOException
    {
        return write((null == s) ? null : s.toCharArray(), flushIt);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#write(java.lang.String)
     */
    @Override
    public int write (String s) throws IOException
    {
        return write(s, false);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#writeln(boolean)
     */
    @Override
    public int writeln (final boolean flushIt) throws IOException
    {
        return write(CRLF, 0, CRLF.length, flushIt);
    }
    /*
     * @see java.lang.Appendable#append(char)
     */
    @Override
    public Appendable append (char c) throws IOException
    {
        write(new char[] { c });
        return this;
    }
    /*
     * @see java.lang.Appendable#append(java.lang.CharSequence, int, int)
     */
    @Override
    public Appendable append (CharSequence csq, int start, int end) throws IOException
    {
        final String    s=csq.subSequence(start, end).toString();
        write(s);
        return this;
    }
    /*
     * @see java.lang.Appendable#append(java.lang.CharSequence)
     */
    @Override
    public Appendable append (CharSequence csq) throws IOException
    {
        write(csq.toString());
        return this;
    }
    /*
     * @see net.community.chest.net.TextNetConnection#writeln(char[], int, int, boolean)
     */
    @Override
    public int writeln (final char[] buf, final int startOffset, final int maxLen, final boolean flushIt) throws IOException
    {
        int    nWritten=write(buf, startOffset, maxLen);
        if (nWritten != maxLen)
            return nWritten;

        int    nCRLF=writeln(flushIt);
        if (nCRLF != CRLF.length)
            return nCRLF;

        return (nWritten + nCRLF);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#writeln(char[], boolean)
     */
    @Override
    public int writeln (final char[] buf, final boolean flushIt) throws IOException
    {
        return writeln(buf, 0, (null == buf) ? 0 : buf.length, flushIt);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#writeln(java.lang.String, boolean)
     */
    @Override
    public int writeln (final String s, final boolean flushIt) throws IOException
    {
        return writeln((null == s) ? null : s.toCharArray(), flushIt);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#writeln()
     */
    @Override
    public int writeln () throws IOException
    {
        return writeln(false);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#writeln(char[], int, int)
     */
    @Override
    public int writeln (final char[] buf, final int startOffset, final int maxLen) throws IOException
    {
        return writeln(buf, startOffset, maxLen, false);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#writeln(char[])
     */
    @Override
    public int writeln (char ... buf) throws IOException
    {
        return writeln(buf, 0, (null == buf) ? 0 : buf.length);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#writeln(java.lang.String)
     */
    @Override
    public int writeln (final String s) throws IOException
    {
        return writeln(s, false);
    }
    /*
     * @see net.community.chest.net.BinaryNetConnection#write(int, boolean)
     */
    @Override
    public int write (final int val, final boolean flushIt) throws IOException
    {
        // make sure value represents an ASCII character
        if ((val < Byte.MIN_VALUE) || (val > Byte.MAX_VALUE))
            throw new IOException("Bad/Illegal single character to write: " + val);

        byte[]    buf={ (byte) val };
        return writeBytes(buf, flushIt);
    }
    /*
     * @see net.community.chest.net.BinaryNetConnection#write(int)
     */
    @Override
    public int write (final int val) throws IOException
    {
        return write(val, false);
    }
    /*
     * @see net.community.chest.net.TextNetConnection#asWriter(boolean)
     */
    @Override
    public Writer asWriter (boolean autoClose) throws IOException
    {
        return NetTextWriter.asWriter(this, autoClose);
    }
}

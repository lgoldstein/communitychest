package net.community.chest.io.encode.hex;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.CharBuffer;

import net.community.chest.io.EOLStyle;
import net.community.chest.io.IOAccessEmbedder;
import net.community.chest.io.IOPositionTracker;
import net.community.chest.io.OptionallyCloseable;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <W> The {@link Appendable} type used as output
 * @author Lyor G.
 * @since Jun 16, 2008 3:23:24 PM
 */
public class HexDumpOutputStream<W extends Appendable> extends OutputStream
        implements OptionallyCloseable, IOAccessEmbedder<W>, IOPositionTracker {
    private boolean    _realClose    /* =false */;
    /*
     * @see net.community.chest.io.OptionallyCloseable#isRealClosure()
     */
    @Override
    public boolean isRealClosure ()
    {
        return _realClose;
    }
    /*
     * @see net.community.chest.io.OptionallyCloseable#setRealClosure(boolean)
     */
    @Override
    public void setRealClosure (boolean enabled) throws UnsupportedOperationException
    {
        _realClose = enabled;
    }
    /*
     * @see net.community.chest.io.OptionallyCloseable#isMutableRealClosure()
     */
    @Override
    public boolean isMutableRealClosure ()
    {
        return true;
    }

    private long    _numWritten    /* =0L */;
    /*
     * @see net.community.chest.io.IOPositionTracker#getPos()
     */
    @Override
    public long getPos ()
    {
        return _numWritten;
    }

    private W    _w    /* =null */;
    /*
     * @see net.community.chest.io.IOAccessEmbedder#getEmbeddedAccess()
     */
    @Override
    public W getEmbeddedAccess ()
    {
        return _w;
    }
    /*
     * @see net.community.chest.io.IOAccessEmbedder#setEmbeddedAccess(java.io.Closeable)
     */
    @Override
    public void setEmbeddedAccess (W w) throws IOException
    {
        if (getPos() > 0L)
            throw new StreamCorruptedException("setEmbeddedAccess() not allowed after write start");

        _w = w;
    }

    public HexDumpOutputStream (W w, boolean realClosure) throws IOException
    {
        if (null == (_w=w))
            throw new IOException("No " + Appendable.class.getSimpleName() + " instance provided");

        _realClose = realClosure;
    }

    public HexDumpOutputStream (W w) throws IOException
    {
        this(w, true);
    }

    public HexDumpOutputStream (boolean realClosure)
    {
        _realClose = realClosure;
    }

    public HexDumpOutputStream ()
    {
        this(true);
    }

    private HexDumpPrecision    _prefix=HexDumpPrecision.P0032;
    public HexDumpPrecision getPrefixPrecision ()
    {
        return _prefix;
    }

    // null == P0000
    public void setPrefixPrecision (HexDumpPrecision p) throws IOException
    {
        if (getPos() > 0L)
            throw new StreamCorruptedException("setPrefixPrecision(" + p + ") not allowed after write start");

        _prefix = p;
    }

    public int getPrefixWidth ()
    {
        final HexDumpPrecision    p=getPrefixPrecision();
        if (null == p)
            return 0;
        else
            return p.getWidth();
    }

    public void setPrefixWidth (final int width) throws IOException
    {
        final HexDumpPrecision    p=HexDumpPrecision.fromWidth(width);
        if (null == p)
            throw new IOException("setPrefixWidth(" + width + ") N/A");

        setPrefixPrecision(p);
    }

    private HexDumpPrecision    _data=HexDumpPrecision.P0016;
    public HexDumpPrecision getDumpPrecision ()
    {
        return _data;
    }

    public void setDumpPrecision (HexDumpPrecision p) throws IOException
    {
        if (getPos() > 0L)
            throw new StreamCorruptedException("setDumpPrecision(" + p + ") not allowed after write start");

        if (null == (_data=p))
            throw new IOException("setDumpPrecision() null value provided");
    }

    public int getDumpWidth ()
    {
        final HexDumpPrecision    p=getDumpPrecision();
        return (null == p) ? 0 : p.getWidth();
    }

    public void setDumpWidth (final int w) throws IOException
    {
        final HexDumpPrecision    p=HexDumpPrecision.fromWidth(w);
        if (null == p)
            throw new IOException("setDumpWidth(" + w + ") N/A");
        setDumpPrecision(p);
    }

    private EOLStyle    _eol=EOLStyle.LOCAL;
    public EOLStyle getEOLStyle ()
    {
        return _eol;
    }

    public void setEOLStyle (EOLStyle eol) throws IOException
    {
        if (getPos() > 0L)
            throw new StreamCorruptedException("setEOLStyle(" + ((null == eol) ? null : eol.name()) + ") not allowed after write start");

        if (null == (_eol=eol))
            throw new IOException("setEOLStyle() no value provided");
    }

    private boolean    _useUppercase    /* =false */;
    public boolean isUseUppercase ()
    {
        return _useUppercase;
    }

    public void setUseUppercase (boolean enabled) throws IOException
    {
        if (getPos() > 0L)
            throw new StreamCorruptedException("setUseUppercase(" + enabled + ") not allowed once write started");

        _useUppercase = enabled;
    }

    private boolean    _showChars=true;
    public boolean isShowCharsData ()
    {
        return _showChars;
    }

    public void setShowCharsData (boolean enabled) throws IOException
    {
        if (getPos() > 0L)
            throw new StreamCorruptedException("setShowCharsData(" + enabled + ") not allowed once write started");

        _showChars = enabled;
    }

    private StringBuilder    _sbData    /* =null */;
    private synchronized StringBuilder getCharsDataBuffer () throws IOException
    {
        if (!isShowCharsData())
            return null;

        if (null == _sbData)
        {
            final int    w=getDumpWidth();
            if (w <= 0)
                throw new IOException("getCharsDataBuffer() invalid dump width: " + w);
            _sbData = new StringBuilder(w);
        }

        return _sbData;
    }

    public static final <A extends Appendable> A appendPrefix (
            final A writer, final long curPos, final int pWidth, final int dWidth) throws IOException
    {
        if (pWidth <= 0)
            return writer;

        if (dWidth <= 1)
            throw new StreamCorruptedException("appendPrefix() illegal dump width: " + dWidth);

        final long    linePos=curPos % dWidth;
        if (linePos > 0L)
            return writer;

        final long    maxPos;
        switch(pWidth)
        {
            case 8    :
                maxPos = 0x00FFL;
                break;

            case 16    :
                maxPos = 0x00FFFFL;
                break;

            case 32    :
                maxPos = 0x00FFFFFFFFL;
                break;

            case 64    :
                maxPos = 0x7FFFFFFFFFFFFFFFL;
                break;

            default    :
                throw new StreamCorruptedException("appendPrefix() illegal prefix width: " + dWidth);
        }

        if (curPos >= maxPos)
            throw new IOException("appendPrefix() curOffset(" + Long.toString(curPos, 16) + ") >= maxOffset(" + Long.toString(maxPos, 16) + ")");

        final String    sPos=Long.toString(curPos, 16), pVal;
        final int        hWidth=pWidth / 4, posLen=sPos.length();
        if (posLen > hWidth)    // should not happen since this means that curPos >= maxPos
            pVal = sPos.substring(posLen - hWidth);
        else if (posLen < hWidth)
        {
            final int            zeroPadLen=hWidth - posLen;
            final StringBuilder    sb=
                StringUtil.repeat(new StringBuilder(hWidth), '0', zeroPadLen);
            sb.append(sPos);
            pVal = sb.toString();
        }
        else
            pVal = sPos;

        if (null == writer)
            throw new IOException("appendPrefix() no Writer instance provided");

        writer.append(pVal);
        return writer;
    }

    public static final <A extends Appendable> A appendSuffix (
            final A writer, final long curPos, final int dWidth, final StringBuilder data, final EOLStyle eol) throws IOException
    {
        if (dWidth <= 1)
            throw new StreamCorruptedException("appendSuffix() illegal dump width: " + dWidth);

        if ((curPos % dWidth) != 0L)
            return writer;

        if (null == writer)
            throw new IOException("appendSuffix() no Writer instance provided");

        if (data != null)
        {
            writer.append(' ')
                  .append(data.toString())
                  ;
            data.setLength(0);
        }

        if (eol != null)
            eol.appendEOL(writer);

        return writer;
    }

    private boolean    _closed    /* =false */;
    /*
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write (final int b) throws IOException
    {
        if (_closed)
            throw new IOException("write(" + b + ") stream is closed");

        final Appendable    w=getEmbeddedAccess();
        if (null == w)
            throw new IOException("write(" + b + ") no accessor");

        final int    dWidth=getDumpWidth();
        appendPrefix(w, getPos(), getPrefixWidth(), dWidth);

        final char[]    vc=Hex.getChars((byte) (b & 0x00FF), isUseUppercase());
        w.append(' ');    // separate from previous value
        w.append(CharBuffer.wrap(vc));

        final StringBuilder    sb=getCharsDataBuffer();
        if (sb != null)
        {
            final char    c=(char) (b & 0x00FF);
            if ((c < ' ') || (c >= 0x7E))
                sb.append('.');
            else
                sb.append(c);
        }

        _numWritten++;

        appendSuffix(w, getPos(), dWidth, sb, getEOLStyle());
    }
    /*
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush () throws IOException
    {
        if (_closed)
            throw new IOException("flush() stream is closed");

        final Appendable    w=getEmbeddedAccess();
        if (null == w)
            throw new IOException("flush() no accessor");

        if (w instanceof Flushable)
            ((Flushable) w).flush();
    }

    /*
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close () throws IOException
    {
        if (!_closed)
        {
            try
            {
                // handle incomplete last line
                final StringBuilder    sb=getCharsDataBuffer();
                final int            sbLen=(null == sb) ? 0 : sb.length();
                if (sbLen > 0)
                {
                    final int            dWidth=getDumpWidth(),
                                        remLen=dWidth - sbLen,
                                        padLen=remLen * (Hex.MAX_HEX_DIGITS_PER_BYTE + 1);
                    final StringBuilder    padding=
                        StringUtil.repeat(new StringBuilder(padLen + 4), ' ', padLen);

                    final Appendable    w=getEmbeddedAccess();
                    if (null == w)    // should not happen since we have some leftover data
                        throw new StreamCorruptedException("close() no writer instance");
                    w.append(padding);

                    appendSuffix(w, getPos() + remLen, dWidth, sb, getEOLStyle());
                }

                if (isRealClosure())
                {
                    final Appendable    w=getEmbeddedAccess();
                    if (w instanceof Closeable)
                        ((Closeable) w).close();
                }
            }
            finally
            {
                _closed = true;
            }
        }
    }
}

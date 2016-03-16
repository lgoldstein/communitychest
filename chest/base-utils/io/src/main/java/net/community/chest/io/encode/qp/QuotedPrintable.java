package net.community.chest.io.encode.qp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.community.chest.io.encode.hex.Hex;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 22, 2007 11:58:26 AM
 */
public final class QuotedPrintable {
    private QuotedPrintable ()
    {
        // prevent instantiation
    }
    /**
     * Quoted-Printable (QP) encoding delimiter
     */
    public static final char QPDELIM='=';
    /**
     * @param c character to be tested
     * @return TRUE if character can be transfered as-is WITHOUT any Q-P encoding
     */
    public static boolean isQPXferChar (char c)
    {
        if ((c >= (char) 0x20) && (c <= (char) 0x7e))
            return (c != QPDELIM);

        if (('\t' == c) || ('\r' == c) || ('\n' == c))
            return true;

        return false;
    }
    /**
     * Number of characters required to QP-encode a value
     */
    public static final int QPENCLEN=3;
    /**
     * Places the QP encoding characters of the byte value into the supplied
     * char buffer.
     * @param b byte to be QP encoded
     * @param c char buffer to place the encoding into
     * @param offset offset in buffer to place encoding
     * @param len number of available (free) places starting at specified offset
     * @return number of characters used - <=0 if error encountered
     */
    public static int getQPChars (byte b, char[] c, int offset, int len)
    {
        if ((null == c) || (offset < 0) || (len < QPENCLEN) || ((offset+len) > c.length))
            return Integer.MIN_VALUE;

        c[offset] = QPDELIM;
        c[offset + 1] = Hex.upperHex[(b >> 4) & 0x0F];
        c[offset + 2] = Hex.upperHex[b & 0x0F];

        return QPENCLEN;
    }
    /**
     * Places the QP encoding characters of the byte value into the supplied
     * char buffer.
     * @param b byte to be QP encoded
     * @param c char buffer to place the encoding into
     * @return number of characters used - <=0 if error encountered
     */
    public static int getQPChars (byte b, char[] c)
    {
        return getQPChars(b, c, 0, (null == c) ? 0 : c.length);
    }
    /**
     * @param b byte/character to be QP encoded
     * @return QP encoding - regardless of whether this character really needs encoding
     */
    public static char[] getQPChars (byte b)
    {
        final char[]    qpc=new char[QPENCLEN];
        if (getQPChars(b, qpc) <= 0)
            return null;    // should not happen

        return qpc;
    }
    /**
     * Checks if specified byte requires QP encoding - if so, it encodes it,
     * otherwise it simply places the character in the output buffer
     * @param b byte value to be checked
     * @param c buffer into which to place the encoding result
     * @param offset offset in buffer into which to place the encoding
     * @param len number of available characters in buffer
     * @return number of characters in the encoded result (<=0 if error)
     */
    public static int getChars (byte b, char[] c, int offset, int len)
    {
        if ((null == c) || (offset < 0) || (len <= 0) || ((offset+len) > c.length))
            return Integer.MIN_VALUE;

        final char    cb=(char) (b & 0x00FF);
        if (isQPXferChar(cb))
        {
            c[offset] = cb;
            return 1;
        }

        return getQPChars(b, c, offset, len);
    }
    /**
     * Checks if specified byte requires QP encoding - if so, it encodes it,
     * otherwise it simply places the character in the output buffer
     * @param b byte value to be checked
     * @param c buffer into which to place the encoding result
     * @return number of characters in the encoded result (<=0 if error)
     */
    public static int getChars (byte b, char[] c)
    {
        return getChars(b, c, 0, (null == c) ? 0 : c.length);
    }
    /**
     * @param b byte/character to be QP encoded
     * @return QP encoding - could be only the character if this is a "transparent" one
     */
    public static char[] getChars (byte b)
    {
        final char[]    qpc=new char[QPENCLEN];
        if (getChars(b, qpc) <= 0)
            return null;    // should not happen

        return qpc;
    }
    /**
     * Option for dis-allowing spaces to be transferred transparently
     */
    public static final int ENCOPT_NOSPACE=1;
    /**
     * Option for encoding space as underline (instead of "=20")
     */
    public static final int ENCOPT_SPACE_AS_UNDERLINE=(ENCOPT_NOSPACE << 1);
    /**
     * Option for encoding separators as well
     */
    public static final int ENCOPT_SEPARATORS=(ENCOPT_SPACE_AS_UNDERLINE << 1);
    /**
     * Option for breaking up long lines according to {@link #MAX_QPENC_LINE_LEN}
     */
    public static final int ENCOPT_BREAKLINES=(ENCOPT_SEPARATORS << 1);
    /**
     * Calculates the estimated size of a data after QP decoding
     * @param qpSize original QP encoded data size
     * @param qpPct percentage of characters QP encoded
     * @return estimated size after QP decoding
     */
    public static int calculateDecodedSize (final int qpSize, final int qpPct)
    {
        if ((qpSize <= 0) || (qpPct <= 0) || (qpPct >= 100))
            return qpSize;

        final int    qpNum=(qpSize * qpPct) / 100;
        return (qpNum / QPENCLEN) + (qpSize - qpNum);
    }
    /**
     * Checks if supplied character requires encoding
     * @param c character to be checked
     * @param nOptions encoding options
     * @return TRUE if need to encode the character. Note: returns FALSE
     * if character is SPACE and ENCOPT_SPACE_AS_UNDERLINE+ENCOPT_NOSPACE
     * options set
     * @see #ENCOPT_NOSPACE
     * @see #ENCOPT_SPACE_AS_UNDERLINE
     * @see #ENCOPT_SEPARATORS
     */
    public static boolean requiresEncoding (final char c, final int nOptions)
    {
        if (!isQPXferChar(c))
            return true;

        // if not required to encode then check some more options
        if ((' ' == c) && (ENCOPT_NOSPACE == (nOptions & ENCOPT_NOSPACE)))
        {
            if (ENCOPT_SPACE_AS_UNDERLINE == (nOptions & ENCOPT_SPACE_AS_UNDERLINE))
                return false;
            else
                return true;
        }
        else if ((ENCOPT_SEPARATORS == (nOptions & ENCOPT_SEPARATORS)) &&
                (('\t' == c) || ('\r' == c) || ('\n' == c)))
            return true;
        else
            return false;
    }

    public static void write (final char c, final OutputStream os, final int nOptions) throws IOException
    {
        if (requiresEncoding(c, nOptions))
        {
            os.write(QPDELIM);
            Hex.write((byte) c, os, true);
        }
        else if ((' ' == c) && (ENCOPT_NOSPACE == (nOptions & ENCOPT_NOSPACE)) && (ENCOPT_SPACE_AS_UNDERLINE == (nOptions & ENCOPT_SPACE_AS_UNDERLINE)))
        {
            os.write('_');
        }
        else
            os.write(c);
    }
    /**
     * Max. Q-P encoded line length - lines longer than this are folded
     */
    public static final int    MAX_QPENC_LINE_LEN=74;
    /**
     * Sequence used as soft-break
     */
    public static final byte[]    QP_SOFTBREAK={ (byte) QPDELIM, (byte) '\r', (byte) '\n' };
    /**
     * QP encodes all data from the input stream and writes it into the output
     * @param ist input stream to read from till EOF
     * @param os output stream to write QP encoded data
     * @param nOptions encoding options
     * @throws IOException if unable to read/write
     * @throws NullPointerException if no input/output stream supplied
     */
    public static void encode (InputStream ist, OutputStream os, int nOptions) throws IOException
    {
        for (int    c=ist.read(); c != (-1); c=ist.read())
            write((char) c, os, nOptions);
    }

    public static <A extends Appendable> A append (final A sb, final byte bVal, final int nOptions) throws IOException
    {
        if (null == sb)
            throw new IOException(ClassUtil.getArgumentsExceptionLocation(QuotedPrintable.class, "append", Byte.valueOf(bVal)) + " no " + Appendable.class.getName() + " instance");

        final char c=(char) (bVal & 0x00FF);
        if (requiresEncoding(c, nOptions))
        {
            sb.append(QPDELIM);
            Hex.appendHex(sb, bVal, true);
        }
        else if ((' ' == c) && (ENCOPT_NOSPACE == (nOptions & ENCOPT_NOSPACE)) && (ENCOPT_SPACE_AS_UNDERLINE == (nOptions & ENCOPT_SPACE_AS_UNDERLINE)))
        {
            sb.append('_');
        }
        else
            sb.append(c);

        return sb;
    }

    public static <A extends Appendable> A append (final A sb, byte[] b, int startOffset, int len, int nOptions) throws IOException
    {
        if ((null == b) || (startOffset < 0) || (len < 0) || (null == sb) || ((startOffset + len) > b.length))
            throw new IOException(ClassUtil.getExceptionLocation(QuotedPrintable.class, "append[]") + " incomplete parameters");

        for (int    i=0, j=startOffset; i < len; i++, j++)
            append(sb, b[j], nOptions);

        return sb;
    }

    public static <A extends Appendable> A append (final A sb, final byte[] b, final int nOptions) throws IOException
    {
        return append(sb, b, 0, (null == b) ? 0 : b.length, nOptions);
    }
    /**
     * If set, then interprets '_' as ' '
     */
    public static final int DECOPT_UNDERLINE_AS_SPACE=ENCOPT_SPACE_AS_UNDERLINE;
    /**
     * If set, then throws exception if non-QP encoded data found
     */
    public static final int DECOPT_THROW_EXCEPTION=(DECOPT_UNDERLINE_AS_SPACE << 1);
    /**
     * Decodes what it <U>assumes</U> is a HEX pair (OK if soft-break(s))
     * @param hiChar high HEX value character
     * @param loChar low HEX value character
     * @param os output stream to write to - may NOT be null
     * @param nOptions decoding options
     * @throws IOException if unable to write to output stream
     * @throws QuotedPrintableDecodingException if decoding exception (and
     * allowed to throw by supplied options)
     */
    public static void decode (short hiChar, short loChar, OutputStream os, int nOptions)
        throws IOException, QuotedPrintableDecodingException
    {
        if (null == os)
            throw new IOException("decode(" + String.valueOf((char) hiChar) + String.valueOf((char) loChar) + ") no output stream");

        if ('\n' == hiChar) // ignore soft-break
            return;

        if ('\n' == loChar)
        {
            if (hiChar != '\r')
            {
                if (DECOPT_THROW_EXCEPTION == (nOptions & DECOPT_THROW_EXCEPTION))
                    throw new QuotedPrintableDecodingException("bad/illegal QP soft-break", (char) hiChar);

                os.write(hiChar);
                os.write(loChar);
            }

            // ignore soft-break
            return;
        }

        if ((-1) == loChar)
        {
            if (DECOPT_THROW_EXCEPTION == (nOptions & DECOPT_THROW_EXCEPTION))
                throw new QuotedPrintableDecodingException("Premature QP encoding end", (char) loChar);

            return;
        }

        final boolean    isHiHex=Hex.isHexDigitValue((byte) hiChar),
                        isLoHex=Hex.isHexDigitValue((byte) loChar);
        if ((!isHiHex) || (!isLoHex))
        {
            if (DECOPT_THROW_EXCEPTION == (nOptions & DECOPT_THROW_EXCEPTION))
                throw new QuotedPrintableDecodingException("Bad/Illegal QP encoding", (char) (isHiHex ? loChar : hiChar));

            os.write(new byte[]{ QPDELIM, (byte) hiChar, (byte) loChar });
        }
        else
        {
            os.write(Hex.rebuild((char) hiChar, (char) loChar));
        }
    }
    /**
     * Decodes the input stream and writes the result to the output - Note: input
     * stream may be a "pure" data - i.e., if no QP encoding in it, then the result
     * is simply a copy of all data in the input stream
     * @param ist input stream to read from
     * @param os output stream to write decoded data to
     * @param nOptions decoding options
     * @throws IOException if I/O errors
     * @throws QuotedPrintableDecodingException if decoding options do not match current state
     * @see QuotedPrintable#DECOPT_THROW_EXCEPTION
     * @see QuotedPrintable#DECOPT_UNDERLINE_AS_SPACE
     */
    public static void decode (InputStream ist, OutputStream os, int nOptions)
        throws IOException, QuotedPrintableDecodingException
    {
        if (null == os)
            throw new IOException("No output stream to decode into");

        for (int    c=ist.read(); c != (-1); c=ist.read())
        {
            switch(c)
            {
                case QPDELIM    :
                    {
                        final int    hiChar=ist.read(),
                                    loChar=('\n' == hiChar) /* ignore soft-break */ ? '\0' : ist.read();
                        decode((short) hiChar, (short) loChar, os, nOptions);
                    }
                    break;

                case '_'        :
                    if (DECOPT_UNDERLINE_AS_SPACE == (nOptions & DECOPT_UNDERLINE_AS_SPACE))
                    {
                        os.write((byte) ' ');
                        break;
                    }
                    // else fall through

                default         : // output as is
                    os.write(c & 0x00FF);
            }
        }
    }

    public static void decode (byte[] bytes, OutputStream os, int nOptions) throws IOException
    {
        if ((bytes != null) && (bytes.length > 0))
            decode(new ByteArrayInputStream(bytes), os, nOptions);
    }

    public static byte[] decodeToBytes (byte[] bytes, int nOptions) throws IOException
    {
        if ((null == bytes) || (bytes.length <= 0))
            return null;

        final ByteArrayOutputStream   os=new ByteArrayOutputStream(bytes.length);
        decode(bytes, os, nOptions);

        return os.toByteArray();
    }

    public static void decode (String s, OutputStream os, int nOptions) throws IOException
    {
        final int sLen=((null == s) ? 0 : s.length());
        if (sLen > 0)
            decode(s.getBytes(), os, nOptions);
    }

    public static String decode (String s, int nOptions)
    {
        final int sLen=((null == s) ? 0 : s.length());
        if (sLen <= 0)
            return s;

        try
        {
            final ByteArrayOutputStream   os=new ByteArrayOutputStream(sLen);
            decode(s, os, nOptions);
            return os.toString();
        }
        catch(IOException ioe)
        {
            throw ExceptionUtil.toRuntimeException(ioe);
        }
    }

    public static byte[] decodeToBytes (String s, int nOptions)
    {
        final String ds=decode(s, nOptions);
        if (null == ds)
            return null;
        else
            return ds.getBytes();
    }
}

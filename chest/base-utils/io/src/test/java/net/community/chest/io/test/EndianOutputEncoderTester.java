/*
 *
 */
package net.community.chest.io.test;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteOrder;
import java.nio.charset.CharsetEncoder;

import net.community.chest.io.encode.endian.AbstractEndianOutputEncoder;
import net.community.chest.io.encode.hex.Hex;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 13, 2009 1:20:46 PM
 */
public class EndianOutputEncoderTester extends AbstractEndianOutputEncoder {
    private PrintStream    _out;
    public PrintStream getPrintStream ()
    {
        return _out;
    }

    public void setPrintStream (PrintStream out)
    {
        _out = out;
    }

    public EndianOutputEncoderTester (ByteOrder inOrder, PrintStream out)
    {
        super(inOrder);
        _out = out;
    }

    public EndianOutputEncoderTester (PrintStream out)
    {
        this(ByteOrder.nativeOrder(), out);
    }

    public EndianOutputEncoderTester ()
    {
        this(null);
    }

    public static final void hexDump (
            final PrintStream    out, final String linePrefix, final byte[] data, final int off, final int len)
        throws IOException
    {
        if ((null == out) || (len <= 0))
            return;

        for (int    curPos=off, curLen=0; curLen < len; curPos++, curLen++)
        {
            if (0 == (curLen % 16))
            {
                out.println();

                if (linePrefix != null)
                    out.append(linePrefix);
            }

            out.append(' ');    // separate from previous
            Hex.appendHex(out, data[curPos], true);
        }

        out.println();
    }
    /*
     * @see net.community.chest.io.encode.endian.AbstractEndianOutputEncoder#writeStringBytes(java.lang.String, java.nio.charset.CharsetEncoder, byte[], int, int)
     */
    @Override
    protected void writeStringBytes (
            String s, CharsetEncoder charsetEnc, byte[] data, int off, int len)
        throws IOException
    {
        final PrintStream    out=getPrintStream();
        if (null == out)
            return;

        out.println("\twriteStringBytes(" + s + ")[" + charsetEnc.charset() + "]{" + getByteOrder() + "} - " + len + " bytes");
        hexDump(out, "\t\t", data, off, len);
    }
    /*
     * @see java.io.DataOutput#write(byte[], int, int)
     */
    @Override
    public void write (byte[] b, int off, int len) throws IOException
    {
        final PrintStream    out=getPrintStream();
        if (null == out)
            return;

        out.println("\twrite(" + off + "/" + len + "){" + getByteOrder() + "}");
        hexDump(out, "\t\t", b, off, len);
    }
    /*
     * @see java.io.DataOutput#writeByte(int)
     */
    @Override
    public void writeByte (int v) throws IOException
    {
        final PrintStream    out=getPrintStream();
        if (null == out)
            return;

        out.println("\twriteByte(" + v + ")");
    }
}

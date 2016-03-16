/*
 *
 */
package net.community.chest.io.input;

import java.io.ByteArrayInputStream;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * Allows changing the current underlying data without closing the stream
 * @author Lyor G.
 * @since Nov 18, 2009 9:47:30 AM
 */
public class DynamicByteArrayInputStream extends ByteArrayInputStream {
    public DynamicByteArrayInputStream (byte[] b, int offset, int length)
    {
        super(b, offset, length);
    }

    public DynamicByteArrayInputStream (byte[] b)
    {
        this(b, 0, b.length);
    }
    // NOTE: not all the data may be valid
    public byte[] toByteArray ()
    {
        return buf;
    }

    public int getMaxIndex ()
    {
        return count;
    }

    public int getReadPosition ()
    {
        return pos;
    }

    public void setData (byte[] b, int offset, int length)
    {
        buf = b;
        pos = offset;
        count = Math.min(offset + length, b.length);
        mark = offset;
    }

    public void setData (byte[] b)
    {
        setData(b, 0, b.length);
    }
}

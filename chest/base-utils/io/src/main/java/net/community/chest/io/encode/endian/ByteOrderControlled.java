/*
 *
 */
package net.community.chest.io.encode.endian;

import java.nio.ByteOrder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Used to indicate an input/output that depends on the {@link ByteOrder}</P>
 *
 * @author Lyor G.
 * @since Jul 13, 2009 8:42:05 AM
 */
public interface ByteOrderControlled {
    ByteOrder getByteOrder ();
    void setByteOrder (ByteOrder o) throws UnsupportedOperationException;
    /**
     * @return TRUE if call to {@link #setByteOrder(ByteOrder)} is allowed
     * (e.g., some input/output encoder/decoder(s) have a built-in
     * {@link ByteOrder} which cannot be changed)
     */
    boolean isMutableByteOrder ();
}

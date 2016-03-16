/*
 *
 */
package net.community.chest.win32.core.serial;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.community.chest.AbstractTestSupport;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 5, 2013 10:54:07 AM
 */
public class SerializationFormatConverterTest extends AbstractTestSupport
{
    public SerializationFormatConverterTest ()
    {
        super();
    }

    @Test
    public void testReadLengthPrefixedStringLength() throws IOException
    {
        for (byte b=0; b < Byte.MAX_VALUE; b++)
            assertEquals("Mismatched 7 bit value", b, readLengthPrefixedStringLength(b));

        assertEquals("Mismatched 14-bit value", 232, readLengthPrefixedStringLength((byte) 0xE8, (byte) 0x01));
    }

    private static final int readLengthPrefixedStringLength(byte...bytes) throws IOException
    {
        try(InputStream    in=new ByteArrayInputStream(bytes)) {
            return SerializationFormatConverter.readLengthPrefixedStringLength(in);
        }
    }
}

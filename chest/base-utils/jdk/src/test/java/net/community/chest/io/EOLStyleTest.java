/*
 *
 */
package net.community.chest.io;

import java.io.IOException;

import net.community.chest.lang.AbstractEnumTestSupport;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 24, 2011 1:18:21 PM
 */
public class EOLStyleTest extends AbstractEnumTestSupport {
    public EOLStyleTest ()
    {
        super();
    }

    @Test
    public void testFromString () throws Exception
    {
        assertFromNameValidity(EOLStyle.class);
    }

    @Test
    public void testFromStyleChars ()
    {
        assertSame("Mismatched CRLF style", EOLStyle.CRLF, EOLStyle.fromStyleChars("\r\n"));
        assertSame("Mismatched LF style", EOLStyle.LF, EOLStyle.fromStyleChars("\n"));
    }

    @Test
    public void testLocalStyle ()
    {
        assertEquals("Mismatched line separator", EOLStyle.LOCAL.getStyleString(), System.getProperty("line.separator"));
    }

    @Test
    public void testAppendEOL () throws IOException
    {
        final StringBuilder    sb=new StringBuilder();
        for (final EOLStyle style : EOLStyle.VALUES)
        {
            sb.setLength(0);

            assertSame("Mismatched appendable instance for style=" + style.name(), sb, style.appendEOL(sb));
            assertEquals("Mismatched EOL value for style=" + style.name(), style.getStyleString(), sb.toString());
        }
    }
}

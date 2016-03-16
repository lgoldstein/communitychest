/*
 *
 */
package net.community.chest.lang;

import java.sql.ClientInfoStatus;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 11, 2011 9:00:39 AM
 */
public class EnumUtilTest extends AbstractEnumTestSupport {
    public EnumUtilTest ()
    {
        super();
    }

    @Test
    public void testGetValueHashCode ()
    {
        assertEquals("Mismatched null value hash code", (-1), EnumUtil.getValueHashCode((ClientInfoStatus) null));
        for (final ClientInfoStatus st : ClientInfoStatus.values())
            assertEquals("Mismatched hash code for " + st, st.ordinal(), EnumUtil.getValueHashCode(st));
    }
}

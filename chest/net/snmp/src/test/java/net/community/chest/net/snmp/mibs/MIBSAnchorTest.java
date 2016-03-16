/*
 *
 */
package net.community.chest.net.snmp.mibs;

import org.junit.Assert;
import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 2, 2011 1:23:56 PM
 */
public class MIBSAnchorTest extends Assert {
    public MIBSAnchorTest ()
    {
        super();
    }

    @Test
    public void testAnchorAccess ()
    {
        final String[]    MIBS={
                "SNMPv2-MIB",
                "IF-MIB",
                "INET-ADDRESS-MIB",
                "TCP-MIB",
                "UDP-MIB"
            };
        for (final String mibName : MIBS)
            assertNotNull("Missing " + mibName, MIBSAnchor.class.getResource(mibName));
    }
}

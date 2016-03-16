/*
 *
 */
package net.community.chest.net.test;

import net.community.chest.AbstractTestSupport;
import net.community.chest.net.address.IPv4Address;
import net.community.chest.net.address.IPv6Address;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 17, 2010 9:45:32 AM
 *
 */
public class IPAddressTest extends AbstractTestSupport {
    public IPAddressTest ()
    {
        super();
    }

    @Test
    public void testInvalidIPv4Addresses ()
    {
        // even index=value, odd index=description of the test
        final String[]    valPairs={
                "12.a.13.x",    "non-numerical characters in address",
                "5.-3.2.6",        "negative address component value",
                "1.2.+6.9",        "illegal '+' sign",
                "3.5",            "incomplete address",
                ".1.2.3.4",        "illegal starting dot",
                "1.2.3.4.",        "illegal ending dot"
            };
        for (int    vpIndex=0; vpIndex < valPairs.length; vpIndex += 2)
        {
            final String    a=valPairs[vpIndex], d=valPairs[vpIndex + 1];
            try
            {
                final IPv4Address    av=new IPv4Address(a);
                fail("Unexpected success for " + d + " on " + a + ": " + av);
            }
            catch(NumberFormatException e)
            {
                // ignored - expected
            }
        }
    }

    @Test
    public void testValidIPv6Addresses ()
    {
        // index=value, index+1=expected shortest form, index+2=description of the test
        final String[]    valPairs={
                "2001:0db8:1234:00ff:0ff:ff:ffff:ffff",        "2001:db8:1234:ff:ff:ff:ffff:ffff",     "with leading zeros",
                "2001:db8:85a3:0:0:8a2e:370:7334",            "2001:db8:85a3::8a2e:370:7334",            "zero contraction translator",
                "2001:db8:85a3::8a2e:370:7334",                "2001:db8:85a3::8a2e:370:7334",            "zero contraction",
                "::1",                                        "::1",                                    "loopback",
                "::",                                        "::",                                    "all zeros",
                "fe70:0000:00::",                            "fe70::",                                "network part 1",
                "fe70::",                                    "fe70::",                                "network part 2",
                "2001:0db8:1234:00ff:0ff:ff:192.0.2.128",    "2001:db8:1234:ff:ff:ff:c000:280",        "full dotted quad notation",
                "2001:0db8::0ff:ff:192.0.2.128",            "2001:db8::ff:ff:c000:280",                "dotted quad notation + middle zero contraction",
                "::ffff:192.0.2.128",                        "::ffff:c000:280",                        "dotted quad notation + prefix zero contraction",
                "::192.0.2.128",                            "::c000:280",                            "dotted quad notation + full zero contraction",
                "fe80::226:b9ff:feee:c4a1%5",                "fe80::226:b9ff:feee:c4a1",                "with scope ID",
                "fec0:0:0:ffff::3%2",                        "fec0::ffff:0:0:0:3",                    "scope ID + zero contraction",
                "fe80::5efe:192.0.2.128%2",                    "fe80::5efe:c000:280",                    "scope ID + dotted quad notation"
            };
        for (int    vpIndex=0; vpIndex < valPairs.length; vpIndex += 3)
        {
            final String    a=valPairs[vpIndex],
                            x=valPairs[vpIndex+1],
                            d=valPairs[vpIndex + 2];
            try
            {
                final IPv6Address    av=new IPv6Address(a);
                final String        vs=av.toString();
                assertEquals(d, x, vs);
            }
            catch(NumberFormatException e)
            {
                fail(e.getClass().getName() + " on " + d + " test of " + a);
            }
        }
    }

    @Test
    public void testDotQuadTranslation ()
    {
        // index=expected, index+1=actual, index+2=description of the test
        final String[]    valPairs={
                "2001:db8:1234:ff:ff:ff:192.0.2.128",    "2001:0db8:1234:00ff:00ff:00ff:c000:0280",    "full dotted quad notation",
                "2001:db8::ff:ff:192.0.2.128",            "2001:0db8::ff:ff:c000:280",                "dotted quad notation + middle zero contraction",
                "::ffff:192.0.2.128",                    "::ffff:c000:280",                            "dotted quad notation + prefix zero contraction",
                "::192.0.2.128",                        "::c000:280",                                "dotted quad notation + full zero contraction",
                "fe80::5efe:192.0.2.128",                "fe80::5efe:192.0.2.128%2",                    "scope ID + dotted quad notation"
            };
        for (int    vpIndex=0; vpIndex < valPairs.length; vpIndex += 3)
        {
            final String    x=valPairs[vpIndex],
                            a=valPairs[vpIndex+1],
                            d=valPairs[vpIndex + 2];
            try
            {
                final IPv6Address    av=new IPv6Address(a);
                final String        vs=av.toString(true, true);
                assertEquals(d, x, vs);
            }
            catch(NumberFormatException e)
            {
                fail(e.getClass().getName() + " on " + d + " test of " + a);
            }
        }

    }
}

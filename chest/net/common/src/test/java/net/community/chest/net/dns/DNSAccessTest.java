/*
 *
 */
package net.community.chest.net.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.NamingException;

import org.junit.Ignore;
import org.junit.Test;

import net.community.chest.AbstractTestSupport;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Sep 11, 2011 12:45:42 PM
 *
 */
public class DNSAccessTest extends AbstractTestSupport {
    public DNSAccessTest ()
    {
        super();
    }

    @Test
    @Ignore("For some reason there are differences")
    public void testALookup () throws NamingException, UnknownHostException
    {
        final DNSAccess    dns=new DNSAccess();
        for (final String name : new String[] { "www.oracle.com" })
        {
            final InetAddress[]    aa=InetAddress.getAllByName(name);
            final Set<String>    expValues=new TreeSet<String>();
            for (final InetAddress a : aa)
                expValues.add(a.getHostAddress());

            final Set<String>    actValues=new TreeSet<String>(dns.aLookup(name));
            assertEquals("Mismatched resolved number of values for " + name, expValues.size(), actValues.size());
            assertContainsAll("Mismatched default addresses for " + name, expValues, actValues);
            assertContainsAll("Mismatched resolved addresses for " + name, actValues, expValues);
        }
    }
}

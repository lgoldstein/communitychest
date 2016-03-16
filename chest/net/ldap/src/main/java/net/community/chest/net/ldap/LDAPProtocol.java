/**
 *
 */
package net.community.chest.net.ldap;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 20, 2008 12:31:37 PM
 */
public final class LDAPProtocol {
    private LDAPProtocol ()
    {
        // no instance
    }
    /**
     * Default LDAP port number
     */
    public static final int    IPPORT_LDAP=389;
    /**
     * Property used to override the default LDAP context factory class
     */
    public static final String    DEFAULT_LDAP_FACTORY_PROPNAME="javax.naming.ldap.factory",
    /**
     * Default LDAP context factory class - unless overridden via the {@link #DEFAULT_LDAP_FACTORY_PROPNAME} property
     */
                                DEFAULT_LDAP_FACTORY_PROPVAL="com.sun.jndi.ldap.LdapCtxFactory";
    /**
     * Property for setting the LDAP version
     * @see <A HREF="http://docs.oracle.com/javase/jndi/tutorial/ldap/misc/version.html">Protocol Versions</A>
     */
    public static final String DEFAULT_LDAP_VERSION_PROPNAME="java.naming.ldap.version";
}

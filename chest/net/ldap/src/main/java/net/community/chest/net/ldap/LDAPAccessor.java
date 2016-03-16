/**
 *
 */
package net.community.chest.net.ldap;

import java.io.Closeable;
import java.nio.channels.Channel;

import javax.naming.NamingException;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 20, 2008 12:37:56 PM
 */
public interface LDAPAccessor extends Closeable, Channel {
    void setConnectTimeout (long msec) throws NamingException;
    void setProtocolVersion (int ver) throws NamingException;

    void bind (String host, int port, String bindDN, String bindPassword) throws NamingException;
    void bind (String host, String bindDN, String bindPassword)throws NamingException;
    // anonymous access
    void bind (String host, int port) throws NamingException;
    void bind (String host) throws NamingException;
}

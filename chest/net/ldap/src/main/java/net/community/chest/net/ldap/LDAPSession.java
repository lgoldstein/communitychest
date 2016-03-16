/**
 *
 */
package net.community.chest.net.ldap;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InvalidAttributeValueException;


/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 20, 2008 12:28:22 PM
 */
public class LDAPSession extends AbstractLDAPAccessorHelper {
    public LDAPSession ()
    {
        final Properties    env=getContextEnvironment();
        final String        facName=System.getProperty(LDAPProtocol.DEFAULT_LDAP_FACTORY_PROPNAME,LDAPProtocol.DEFAULT_LDAP_FACTORY_PROPVAL);
        env.put(Context.INITIAL_CONTEXT_FACTORY, facName);
    }
    /*
     * @see net.community.chest.net.ldap.LDAPAccessor#setConnectTimeout(long)
     */
    @Override
    public void setConnectTimeout (long msec) throws NamingException
    {
        if (msec <= 0L)
            throw new InvalidAttributeValueException("setConnectTimeout(" + msec + ") invalid value");

        updateEnvironmentIntParam("com.sun.jndi.ldap.connect.timeout", String.valueOf(msec));
    }
}

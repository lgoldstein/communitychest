/*
 *
 */
package net.community.chest.net.snmp.io;

import java.net.URL;

import net.community.chest.net.snmp.mibs.MIBSAnchor;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>Uses the {@link MIBSAnchor} to access the built-in MIB definitions</P>
 * @author Lyor G.
 * @since Aug 2, 2011 12:53:38 PM
 */
public class DefaultMIBDefinitionResolver extends AbstractMIBDefinitionResolver {
    /**
     * Singleton reentrant instance
     */
    public static final DefaultMIBDefinitionResolver    INSTANCE=new DefaultMIBDefinitionResolver();

    public DefaultMIBDefinitionResolver ()
    {
        super();
    }
    /*
     * @see net.community.chest.net.snmp.io.MIBDefinitionResolver#lookupMIB(java.lang.String)
     */
    @Override
    public URL lookupMIB (String mibName)
    {
        return MIBSAnchor.class.getResource(mibName);
    }
}

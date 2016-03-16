/*
 *
 */
package net.community.chest.net.snmp.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.community.chest.io.input.LoggingInputStream;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 2, 2011 2:36:16 PM
 *
 */
public class LoggingMIBDefinitionResolver implements MIBDefinitionResolver {
    private final Appendable    _out;
    private final MIBDefinitionResolver    _resolver;
    public LoggingMIBDefinitionResolver (Appendable out, MIBDefinitionResolver resolver)
    {
        _out = out;
        _resolver = resolver;
    }
    /*
     * @see net.community.chest.net.snmp.io.MIBDefinitionResolver#lookupMIB(java.lang.String)
     */
    @Override
    public URL lookupMIB (String mibName)
    {
        return _resolver.lookupMIB(mibName);
    }
    /*
     * @see net.community.chest.net.snmp.io.MIBDefinitionResolver#openMIB(java.lang.String)
     */
    @Override
    public InputStream openMIB (String mibName) throws IOException
    {
        return new LoggingInputStream(_out, _resolver.openMIB(mibName));
    }
}

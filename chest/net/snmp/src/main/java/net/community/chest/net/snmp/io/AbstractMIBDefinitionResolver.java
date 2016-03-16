/*
 *
 */
package net.community.chest.net.snmp.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Provides some default implementations for {@link MIBDefinitionResolver}
 * @author Lyor G.
 * @since Aug 2, 2011 12:49:53 PM
 */
public abstract class AbstractMIBDefinitionResolver implements MIBDefinitionResolver {
    protected AbstractMIBDefinitionResolver ()
    {
        super();
    }
    /*
     * @see net.community.chest.net.snmp.io.MIBDefinitionResolver#openMIB(java.lang.String)
     */
    @Override
    public InputStream openMIB (String mibName) throws IOException
    {
        final URL    url=lookupMIB(mibName);
        if (url == null)
            throw new FileNotFoundException("Cannot resolve MIB: " + mibName);

        return url.openStream();
    }
}

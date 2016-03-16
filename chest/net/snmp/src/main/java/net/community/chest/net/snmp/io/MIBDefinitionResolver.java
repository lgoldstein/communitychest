/*
 *
 */
package net.community.chest.net.snmp.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>Used to resolve references to other MIB(s)</P>
 * @author Lyor G.
 * @since Aug 2, 2011 12:46:47 PM
 */
public interface MIBDefinitionResolver {
    /**
     * @param mibName The &quot;pure&quot; MIB name
     * @return The {@link URL} for the MIB's definition
     */
    URL lookupMIB (String mibName);
    /**
     * @param mibName The &quot;pure&quot; MIB name
     * @return An {@link InputStream} that can be used to read the MIB's definition
     * @throws IOException If cannot open the stream
     */
    InputStream openMIB (String mibName) throws IOException;
}

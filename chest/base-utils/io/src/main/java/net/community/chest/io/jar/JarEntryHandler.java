package net.community.chest.io.jar;

import java.util.jar.JarEntry;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used for dynamic enumeration of {@link JarEntry}-es</P>
 * @author Lyor G.
 * @since Oct 21, 2007 8:47:17 AM
 */
public interface JarEntryHandler {
    /**
     * @param je The {@link JarEntry} to be handled
     * @return Zero to keep enumerating the entries, positive to stop
     * enumeration (no error), negative - error + stop enumeration
     */
    int handleJAREntry (JarEntry je);
}

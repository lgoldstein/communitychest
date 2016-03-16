/*
 *
 */
package net.community.chest.io.filter;

import java.io.File;
import java.io.FilenameFilter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 27, 2011 2:18:24 PM
 */
public abstract class AbstractEmbeddedFilenameFilter implements FilenameFilter {
    protected FilenameFilter    _filter;
    protected AbstractEmbeddedFilenameFilter (FilenameFilter filter)
    {
        if ((_filter=filter) == null)
            throw new IllegalArgumentException("No initial filter provided");
    }
    /*
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    @Override
    public boolean accept (File dir, String name)
    {
        if (_filter != null)
            return _filter.accept(dir, name);

        return false;
    }
}

/*
 *
 */
package net.community.apps.tools.svn;

import net.community.chest.eclipse.EclipseMetadataFoldersFilter;
import net.community.chest.io.filter.AbstractRootFolderFilesFilter;
import net.community.chest.svnkit.SVNFoldersFilter;
import net.community.chest.svnkit.SVNLocation;
import net.community.chest.svnkit.SVNLocationFilter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 19, 2010 10:09:11 AM
 *
 */
public class SVNSyncFilesFilter extends AbstractRootFolderFilesFilter implements SVNLocationFilter {
    public SVNSyncFilesFilter ()
    {
        super(false, SVNFoldersFilter.SVN_SUBFOLDER_NAME, EclipseMetadataFoldersFilter.METADATA_SUBFOLDER_NAME);
        setDescription("Synchronization filter");
    }
    /*
     * @see net.community.chest.svnkit.SVNLocationFilter#accept(net.community.chest.svnkit.SVNLocation)
     */
    @Override
    public boolean accept (SVNLocation location)
    {
        final String    locName=(location == null) ? null : location.getName();
        if (isExcludedParentFolderName(locName))
            return false;

        return true;
    }

    public static final SVNSyncFilesFilter    DEFAULT=new SVNSyncFilesFilter();
}

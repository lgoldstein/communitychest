/*
 *
 */
package net.community.chest.svnkit.core.wc;

import java.util.ArrayList;
import java.util.Collection;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNInfoHandler;
import org.tmatesoft.svn.core.wc.SVNInfo;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 8, 2010 3:12:32 PM
 *
 */
public class SVNInfoCollection extends ArrayList<SVNInfo> implements ISVNInfoHandler {
    /**
     *
     */
    private static final long serialVersionUID = -7825144260277851890L;
    public SVNInfoCollection ()
    {
        super();
    }

    public SVNInfoCollection (Collection<? extends SVNInfo> c)
    {
        super(c);
    }

    public SVNInfoCollection (int initialCapacity)
    {
        super(initialCapacity);
    }
    /*
     * @see org.tmatesoft.svn.core.wc.ISVNInfoHandler#handleInfo(org.tmatesoft.svn.core.wc.SVNInfo)
     */
    @Override
    public void handleInfo (SVNInfo info) throws SVNException
    {
        if ((info != null) && add(info))
            return;
    }
}

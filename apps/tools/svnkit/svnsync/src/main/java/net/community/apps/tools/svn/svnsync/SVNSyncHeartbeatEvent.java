/*
 *
 */
package net.community.apps.tools.svn.svnsync;

import java.awt.AWTEvent;

import net.community.chest.awt.AWTUtils;

/**
 * <P>An {@link AWTEvent} used to inform the main frame about the progress of the synchronizer</P>
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 10, 2010 8:35:22 AM
 */
final class SVNSyncHeartbeatEvent extends AWTEvent {
    /**
     *
     */
    private static final long serialVersionUID = 5344047987256156073L;
    public static final int    ID=AWTUtils.assignUserEventID();
    private final SVNSynchronizer    _sync;
    public final SVNSynchronizer getSynchronizer ()
    {
        return _sync;
    }

    public SVNSyncHeartbeatEvent (SVNSynchronizer sourceValue, SVNSyncMainFrame target)
    {
        super(target, ID);

        if ((_sync=sourceValue) == null)
            throw new IllegalStateException("No synchronizer instance provided");
    }
}

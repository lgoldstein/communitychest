/*
 * 
 */
package net.community.apps.tools.svn.wc;

import java.io.File;
import java.util.List;

import net.community.chest.svn.ui.filesmgr.SNVLocalFolderStatusUpdater;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNStatus;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 9, 2009 1:12:33 PM
 */
public class WCLocalFilesStatusUpdater extends SNVLocalFolderStatusUpdater {
	private static final LoggerWrapper	_logger=WrapperFactoryManager.getLogger(WCLocalFilesStatusUpdater.class);

	private final WCMainFrame	_frame;
	public final WCMainFrame getMainFrame ()
	{
		return _frame;
	}

	public WCLocalFilesStatusUpdater (WCMainFrame frame, SVNClientManager mgr, File f)
	{
		super(mgr, f);

		if (null == (_frame=frame))
			throw new IllegalArgumentException("No frame instance provided");
	}
	/*
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process (List<SVNStatus> chunks)
	{
		final WCMainFrame	f=getMainFrame(); 
		if ((null == f) || (null == chunks) || (chunks.size() <= 0))
			return;

		for (final SVNStatus status : chunks)
		{
			try
			{
				f.handleStatus(status);
			}
			catch(Exception e)
			{
				_logger.error("process(" + status + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
			}
		}
	}
	/*
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done ()
	{
		final WCMainFrame	f=getMainFrame();
		if (f != null)
			f.stopUpdating(this, false);
	}
}

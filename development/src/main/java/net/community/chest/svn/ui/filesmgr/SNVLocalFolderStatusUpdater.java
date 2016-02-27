/*
 * 
 */
package net.community.chest.svn.ui.filesmgr;

import java.io.File;

import javax.swing.SwingWorker;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusClient;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 9, 2009 1:02:40 PM
 */
public abstract class SNVLocalFolderStatusUpdater extends SwingWorker<Long,SVNStatus> implements ISVNStatusHandler {
	private SVNStatusClient	_stClient;
	public SVNStatusClient getStatusClient ()
	{
		return _stClient; 
	}

	public void setStatusClient (SVNStatusClient clnt)
	{
		_stClient = clnt;
	}

	private File _f;
	public File getLocalFolder ()
	{
		return _f;
	}

	public void setLocalFolder (File f)
	{
		_f = f;
	}

	protected SNVLocalFolderStatusUpdater (SVNStatusClient c, File f)
	{
		_stClient = c;
		_f = f;
	}

	protected SNVLocalFolderStatusUpdater (SVNClientManager mgr, File f)
	{
		this((null == mgr) ? null : mgr.getStatusClient(), f);
	}
	/*
	 * @see org.tmatesoft.svn.core.wc.ISVNStatusHandler#handleStatus(org.tmatesoft.svn.core.wc.SVNStatus)
	 */
	@Override
	public void handleStatus (SVNStatus status) throws SVNException
	{
		if (status != null)
		{
			if (isCancelled())
				throw new SVNException(SVNErrorMessage.create(SVNErrorCode.CANCELLED));

			publish(status);
		}
	}
	/*
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Long doInBackground () throws Exception
	{
		final SVNStatusClient	c=getStatusClient();
		final File				f=getLocalFolder();
		if ((null == c) || (null == f))
			return null;

		return Long.valueOf(c.doStatus(f, SVNRevision.HEAD, SVNDepth.IMMEDIATES, false, true, false, false, this, null));
	}
}

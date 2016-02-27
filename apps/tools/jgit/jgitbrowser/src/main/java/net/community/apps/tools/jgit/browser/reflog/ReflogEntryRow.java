/*
 * 
 */
package net.community.apps.tools.jgit.browser.reflog;

import org.eclipse.jgit.revwalk.RevCommit;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 20, 2011 11:49:21 AM
 */
public class ReflogEntryRow {
	private final RevCommit	_logEntry;
	public final RevCommit getLogEntry ()
	{
		return _logEntry;
	}

	public ReflogEntryRow (RevCommit logEntry)
	{
		if ((_logEntry=logEntry) == null)
			throw new IllegalStateException("No log entry provided");
	}
}

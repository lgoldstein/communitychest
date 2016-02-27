package net.community.apps.tools.svn.wc.state;

import java.util.List;

import net.community.chest.svnkit.core.wc.SVNLocalCopyData;

import org.tmatesoft.svn.core.wc.SVNClientManager;

/**
 * 
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Interface implemented by the various popup menu actions</P>
 * 
 * @author Lyor G.
 * @since Aug 9, 2009 11:21:50 AM
 */
public interface StateChecker {
	String getActionCommand ();
	boolean checkState (List<? extends SVNLocalCopyData> selValues);
	Throwable actionPerformed (
			SVNClientManager 					mgr,
			List<? extends SVNLocalCopyData> 	selValues);
}
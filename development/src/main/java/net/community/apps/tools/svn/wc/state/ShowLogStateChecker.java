package net.community.apps.tools.svn.wc.state;

import java.util.List;

import net.community.chest.svnkit.core.wc.SVNLocalCopyData;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Handles the properties command</P>
 * @author Lyor G.
 * @since Aug 9, 2009 12:51:51 PM
 */
public class ShowLogStateChecker extends AbstractStateChecker {
	public static final String	SHOWLOG_CMD="svnshowlog";
	public ShowLogStateChecker ()
	{
		super(SHOWLOG_CMD);
	}
	/*
	 * @see net.community.apps.tools.svn.wc.WCFilesManagerPopupMenu.StateChecker#checkState(java.util.List)
	 */
	@Override
	public boolean checkState (List<? extends SVNLocalCopyData> selValues)
	{
		return (checkVersionedLocalCopy(selValues) != null);
	}

	public static final ShowLogStateChecker	DEFAULT=new ShowLogStateChecker();
}
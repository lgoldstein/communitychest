package net.community.apps.tools.svn.wc.state;

import java.util.Collection;
import java.util.List;

import net.community.chest.svnkit.core.wc.SVNLocalCopyData;

import org.tmatesoft.svn.core.wc.SVNClientManager;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Base class helper for {@link StateChecker}-s implementations</P>
 * 
 * @author Lyor G.
 * @since Aug 9, 2009 11:22:27 AM
 */
public abstract class AbstractStateChecker implements StateChecker {
	private final String	_cmd;
	/*
	 * @see net.community.apps.tools.svn.wc.WCFilesManagerPopupMenu.StateChecker#getActionCommand()
	 */
	@Override
	public final String getActionCommand ()
	{
		return _cmd;
	}
	
	protected AbstractStateChecker (String cmd) throws IllegalArgumentException
	{
		if ((null == (_cmd=cmd)) || (cmd.length() <= 0))
			throw new IllegalArgumentException("No action command provided");
	}
	/*
	 * @see net.community.apps.tools.svn.wc.state.StateChecker#actionPerformed(org.tmatesoft.svn.core.wc.SVNClientManager, java.util.List)
	 */
	@Override
	public Throwable actionPerformed (
			final SVNClientManager 					mgr,
			final List<? extends SVNLocalCopyData> 	selValues)
	{
		if (null == mgr)
			throw new IllegalStateException("actionPerformed(" + getActionCommand() + ") no " + SVNClientManager.class.getSimpleName());
		if ((null == selValues) || (selValues.size() <= 0))
			return null;

		return new UnsupportedOperationException("actionPerformed(" + getActionCommand() + ") N/A");
	}
	// returns null if more than one value selected and it is NOT versioned
	public static final SVNLocalCopyData checkVersionedLocalCopy (
			final List<? extends SVNLocalCopyData> selValues)
	{
		final SVNLocalCopyData	lclData=
			((null == selValues) || (selValues.size() != 1)) ? null : selValues.get(0);
		if ((null == lclData) || (!lclData.isVersioned()))
			return null;

		return lclData;
	}
	// returns number of versioned instances
	public static final int countVersionedLocalCopy (
			final Collection<? extends SVNLocalCopyData> selValues)
	{
		if ((null == selValues) || (selValues.size() <= 0))
			return 0;

		int	numVersioned=0;
		for (final SVNLocalCopyData lclData : selValues)
		{
			if ((null == lclData) || (!lclData.isVersioned()))
				continue;
			numVersioned++;
		}

		return numVersioned;
	}
	// returns 1st non-versioned value - null if all versioned or null/empty Collection
	public static final SVNLocalCopyData checkAllVersionedLocalCopy ( 
			final Collection<? extends SVNLocalCopyData> selValues)
	{
		if ((null == selValues) || (selValues.size() <= 0))
			return null;

		for (final SVNLocalCopyData lclData : selValues)
		{
			if (null == lclData)
				continue;
			if (!lclData.isVersioned())
				return lclData;
		}

		return null;
	}
	// returns 1st versioned value - null if all un-versioned or null/empty Collection
	public static final SVNLocalCopyData checkNoVersionedLocalCopy ( 
			final Collection<? extends SVNLocalCopyData> selValues)
	{
		if ((null == selValues) || (selValues.size() <= 0))
			return null;

		for (final SVNLocalCopyData lclData : selValues)
		{
			if (null == lclData)
				continue;
			if (lclData.isVersioned())
				return lclData;
		}

		return null;
	}
}
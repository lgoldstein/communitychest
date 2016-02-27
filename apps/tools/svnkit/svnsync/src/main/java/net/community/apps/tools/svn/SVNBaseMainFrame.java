/*
 * 
 */
package net.community.apps.tools.svn;

import java.io.File;

import net.community.apps.common.BaseMainFrame;
import net.community.apps.common.resources.BaseAnchor;

/**
 * <P>Copyright as per GPLv2</P>
 * @param <A> Type of {@link BaseAnchor} being used 
 * @author Lyor G.
 * @since Aug 19, 2010 11:35:00 AM
 */
public abstract class SVNBaseMainFrame<A extends BaseAnchor> extends BaseMainFrame<A> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4148760834004780853L;
	protected SVNBaseMainFrame (final boolean autoInit, final String... args) throws Exception
	{
		super(autoInit, args);
	}

	public abstract String getWCLocation ();
	// returns true if view refresh initiated
	public abstract boolean setWCLocation (File orgFile, String orgPath, boolean forceRefresh);
}

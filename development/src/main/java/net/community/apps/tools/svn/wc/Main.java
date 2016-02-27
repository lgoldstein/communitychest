/*
 * 
 */
package net.community.apps.tools.svn.wc;

import javax.swing.SwingUtilities;

import net.community.apps.tools.svn.SVNBaseMain;
import net.community.apps.tools.svn.resources.DefaultResourcesAnchor;
import net.community.chest.CoVariantReturn;
import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.resources.SystemPropertiesResolver;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 10:16:32 AM
 */
public class Main extends SVNBaseMain<DefaultResourcesAnchor,WCMainFrame> {
	private Main (final String ... args)
	{
		super(args);
	}
	/*
	 * @see net.community.apps.tools.svn.SVNBaseMain#processArgument(net.community.apps.tools.svn.SVNBaseMainFrame, java.lang.String, int, int, java.lang.String[])
	 */
	@Override
	protected int processArgument (WCMainFrame f, String a, int oIndex, int numArgs, String... args)
	{
		if ("-wc".equals(a) || "--working-copy".equals(a))
		{
			final int	aIndex=oIndex + 1;
			final String	loc=resolveStringArg(a, args, numArgs, aIndex, f.getWCLocation()),
							eff=SystemPropertiesResolver.SYSTEM.format(loc);
			f.setWCLocation(null, eff, false);
			return aIndex;
		}

		return super.processArgument(f, a, oIndex, numArgs, args);
	}
	/*
	 * @see net.community.apps.common.BaseMain#createMainFrameInstance()
	 */
	@Override
	@CoVariantReturn
	protected WCMainFrame createMainFrameInstance () throws Exception
	{
		return processMainArgs(new WCMainFrame(), getMainArguments());
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (final String[] args)
	{
		// 1st thing we do before any UI startup
		AbstractXmlProxyConverter.setDefaultLoader(DefaultResourcesAnchor.getInstance());
		SwingUtilities.invokeLater(new Main(args));
	}
}

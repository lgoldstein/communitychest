/*
 * 
 */
package net.community.apps.tools.svn.svnsync;

import javax.swing.SwingUtilities;

import net.community.apps.tools.svn.SVNBaseMain;
import net.community.apps.tools.svn.svnsync.resources.ResourcesAnchor;
import net.community.chest.CoVariantReturn;
import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.lang.SysPropsEnum;
import net.community.chest.resources.SystemPropertiesResolver;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 19, 2010 11:31:15 AM
 *
 */
public final class Main extends SVNBaseMain<ResourcesAnchor,SVNSyncMainFrame> {
	public Main (String... args)
	{
		super(args);
	}
	/*
	 * @see net.community.apps.tools.svn.SVNBaseMain#processArgument(net.community.apps.tools.svn.SVNBaseMainFrame, java.lang.String, int, int, java.lang.String[])
	 */
	@Override
	protected int processArgument (SVNSyncMainFrame f, String a, int oIndex, int numArgs, String... args)
	{
		int	aIndex=oIndex;
		if ("-t".equals(a) || "--target".equals(a))
		{
			aIndex++;
			final String	loc=resolveStringArg(a, args, numArgs, aIndex, f.getWCLocation()),
							eff=SystemPropertiesResolver.SYSTEM.format(loc);
			f.setWCLocation(null, eff, false);
		}
		else if ("-s".equals(a) || "--source".equals(a))
		{
			aIndex++;
			final String	loc=resolveStringArg(a, args, numArgs, aIndex, f.getSynchronizationSource()),
							eff=SystemPropertiesResolver.SYSTEM.format(loc);
			f.setSynchronizationSource(eff);
		}
		else if ("-c".equals(a) || "--confirm".equals(a))
		{
			aIndex++;

			final String	loc=resolveStringArg(a, args, numArgs, aIndex, null);
			if (!f.addConfirmLocation(loc))
				throw new IllegalStateException("Re-specified " + a + " value: " + loc);
		}
		else if ("--show-skipped".equals(a))
		{
			f.setShowSkippedTargetsEnabled(true);
		}
		else if ("--skip-props".equals(a))
		{
			f.setPropertiesSyncAllowed(false);
		}
		else if ("--use-merge".equals(a))
		{
			f.setUseMergeForUpdate(true);
		}
		else
			aIndex = super.processArgument(f, a, oIndex, numArgs, args);

		return aIndex;
	}
	/*
	 * @see net.community.apps.common.BaseMain#createMainFrameInstance()
	 */
	@Override
	@CoVariantReturn
	protected SVNSyncMainFrame createMainFrameInstance () throws Exception
	{
		final SVNSyncMainFrame	f=processMainArgs(new SVNSyncMainFrame(), getMainArguments());
		final String			wcLoc=f.getSynchronizationSource();
		if ((wcLoc == null) || (wcLoc.length() <= 0))
			f.setSynchronizationSource(SysPropsEnum.USERDIR.getPropertyValue());
		return f;
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (final String[] args)
	{
		// 1st thing we do before any UI startup
		AbstractXmlProxyConverter.setDefaultLoader(ResourcesAnchor.getInstance());
		SwingUtilities.invokeLater(new Main(args));
	}
}

/*
 * 
 */
package net.community.apps.apache.maven.pom2cpsync;

import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.chest.CoVariantReturn;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 14, 2008 11:33:31 AM
 */
public final class Main extends BaseMain {
	private Main (final String ... args)
	{
		super(args);
	}
	/*
	 * @see net.community.apps.common.BaseMain#createMainFrameInstance()
	 */
	@Override
	@CoVariantReturn
	protected MainFrame createMainFrameInstance () throws Exception
	{
		return new MainFrame(getMainArguments());
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		SwingUtilities.invokeLater(new Main(args));
	}
}

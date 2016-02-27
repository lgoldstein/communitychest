package net.community.apps.tools.hdrxlate;

import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.chest.CoVariantReturn;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Translates MIME encoded headers into their &quot;real&quot; strings</P>
 * @author Lyor G.
 * @since Aug 22, 2007 12:26:17 PM
 */
public class Main extends BaseMain {
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
		return new MainFrame();
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (final String[] args)
	{
		SwingUtilities.invokeLater(new Main(args));
	}
}

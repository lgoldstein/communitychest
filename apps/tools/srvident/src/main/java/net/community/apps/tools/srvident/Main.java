package net.community.apps.tools.srvident;

import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.chest.CoVariantReturn;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Identifies the SMTP/POP3/IMAP4/HTTP server type and version</P>
 * @author Lyor G.
 * @since Oct 25, 2007 9:31:47 AM
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
		return new MainFrame(getMainArguments());
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (final String[] args)
	{
		SwingUtilities.invokeLater(new Main(args));
	}
}

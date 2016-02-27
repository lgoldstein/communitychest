/*
 * 
 */
package net.community.apps.eclipse.cp2pom;

import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.chest.CoVariantReturn;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Translates the contents of a classpath file into a Maven dependencies
 * XML fragment</P>
 * 
 * @author Lyor G.
 * @since Jul 27, 2009 9:27:34 AM
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

	public static void main (final String[] args)
	{
		SwingUtilities.invokeLater(new Main(args));
	}
}

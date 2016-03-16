/*
 *
 */
package net.community.apps.tools.filesync;

import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.chest.CoVariantReturn;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 15, 2009 3:25:43 PM
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

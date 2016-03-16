package net.community.apps.eclipse.cparrange;

import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.chest.CoVariantReturn;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Arranges the classpath entries in the <I>.classpath</I> Eclipse file
 * according to lexicographical order</P>
 *
 * @author Lyor G.
 * @since Nov 22, 2007 2:10:55 PM
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
    protected MainFrame createMainFrameInstance ()
    {
        return new MainFrame(getMainArguments());
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (final String[] args)
    {
        SwingUtilities.invokeLater(new Main(args));
    }
}

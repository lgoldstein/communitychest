package net.community.apps.tools.srcextract;

import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.chest.CoVariantReturn;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Extracts all source files from a JAR/ZIP file</P>
 *
 * @author Lyor G.
 * @since Nov 25, 2007 11:12:51 AM
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

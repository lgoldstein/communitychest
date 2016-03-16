/*
 *
 */
package net.community.apps.tools.itext.pdfconcat;

import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.chest.CoVariantReturn;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 30, 2009 11:58:57 AM
 */
public final class Main extends BaseMain {
    private Main (String... args)
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

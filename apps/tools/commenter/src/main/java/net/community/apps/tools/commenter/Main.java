/*
 *
 */
package net.community.apps.tools.commenter;

import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.chest.CoVariantReturn;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Adds a comment at the top a bunch of file types (e.g., C/C++/Java/XML)
 * from either a text argument or another file</P>
 *
 * @author Lyor G.
 * @since Jun 25, 2009 1:28:46 PM
 */
public class Main extends BaseMain implements Runnable {
    private Main (String... args)
    {
        super(args);
    }

    private MainFrame    _frame;
    /*
     * @see net.community.apps.common.BaseMain#createMainFrameInstance()
     */
    @Override
    @CoVariantReturn
    protected synchronized MainFrame createMainFrameInstance () throws Exception
    {
        if (null == _frame)
            _frame = new MainFrame(getMainArguments());
        else if (_frame.isGUIAllowed())
            _frame.layoutComponent();
        return _frame;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        try
        {
            final Main        m=new Main(args);
            final MainFrame    f=m.createMainFrameInstance();
            if (f.isGUIAllowed())
                SwingUtilities.invokeLater(m);
            else
                f.run();
        }
        catch(Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }
}

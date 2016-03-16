/*
 *
 */
package net.community.apps.tools.jardiff;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.apps.tools.jardiff.resources.ResourcesAnchor;
import net.community.chest.CoVariantReturn;
import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.swing.options.BaseOptionPane;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 2, 2011 9:10:20 AM
 *
 */
public class Main extends BaseMain {
    public Main (String... args)
    {
        super(args);
    }

    private static final MainFrame processMainArgs (MainFrame frame, String ...args)
    {
        final int                    numArgs=(args == null) ? 0 : args.length;
        final Map<String,String>    procArgs=new TreeMap<String,String>();
        for (int    aIndex=0; aIndex < numArgs; aIndex++)
        {
            final String    opt=args[aIndex];
            if ("-l".equalsIgnoreCase(opt))
            {
                aIndex = addExtraArgument(opt, procArgs, aIndex, args);
                frame.setLeftJarPath(procArgs.get(opt));
            }
            else if ("-r".equalsIgnoreCase(opt))
            {
                aIndex = addExtraArgument(opt, procArgs, aIndex, args);
                frame.setRightJarPath(procArgs.get(opt));
            }
            else if ("-c".equals(opt))
            {
                final String    prev=procArgs.put(opt, opt);
                if (prev != null)
                    throw new IllegalArgumentException("Option " + opt + " re-specified");

                frame.setCheckContents(true);
            }
            else
                throw new IllegalArgumentException("Unknown option: " + opt);
        }

        return frame;
    }

    /*
     * @see net.community.apps.common.BaseMain#createMainFrameInstance()
     */
    @Override
    @CoVariantReturn
    protected MainFrame createMainFrameInstance () throws Exception
    {
        try
        {
            return processMainArgs(new MainFrame(), getMainArguments());
        }
        catch(Exception e)
        {
            throw BaseOptionPane.showMessageDialog(null, e);
        }
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (final String[] args)
    {
        // 1st thing we do before any UI startup
        AbstractXmlProxyConverter.setDefaultLoader(ResourcesAnchor.getInstance());
        SwingUtilities.invokeLater(new Main(args));
    }
}

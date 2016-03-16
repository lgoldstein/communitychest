/*
 *
 */
package net.community.apps.tools.jgit.browser;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.apps.tools.jgit.browser.resources.ResourcesAnchor;
import net.community.chest.CoVariantReturn;
import net.community.chest.dom.proxy.AbstractXmlProxyConverter;

import org.eclipse.jgit.lib.Constants;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 16, 2011 8:50:14 AM
 *
 */
public final class Main extends BaseMain {
    public Main (String... args)
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
        return processMainArguments(new MainFrame(), getMainArguments());
    }

    private static final MainFrame processMainArguments (final MainFrame frame, String ...args)
    {
        final int                    numArgs=(args == null) ? 0 : args.length;
        final Map<String,String>    procArgs=new TreeMap<String,String>();
        for (int    aIndex=0; aIndex < numArgs; aIndex++)
        {
            final String    opt=args[aIndex];
            if ("-f".equalsIgnoreCase(opt))
            {
                aIndex = addExtraArgument(opt, procArgs, aIndex, args);
                frame.loadFile(new File(procArgs.get(opt)), false);
            }
            else if ("-b".equalsIgnoreCase(opt))
            {
                aIndex = addExtraArgument(opt, procArgs, aIndex, args);
            }
            else
                throw new IllegalArgumentException("Unknown option: " + opt);
        }

        return setSpecificBranch(frame, procArgs.get("-b"));
    }

    private static final MainFrame setSpecificBranch (final MainFrame frame, final String refName)
    {
        if (frame == null)
            return frame;

        if ((refName != null) && (refName.length() > 0)
         && (!Constants.HEAD.equals(refName))
         && (frame.setSelectedReference(refName, true) != null))
            return frame;

        // reached if either HEAD requested or original reference name failed
        frame.setSelectedReference(Constants.HEAD, true);
        return frame;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (final String[] args)
    {
        // 1st thing we do before any UI startup
        AbstractXmlProxyConverter.setDefaultLoader(ResourcesAnchor.getInstance());
        SwingUtilities.invokeLater(new Main(args));
    }

}

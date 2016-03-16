/*
 *
 */
package net.community.apps.apache.maven.conv2maven;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.chest.CoVariantReturn;
import net.community.chest.lang.SysPropsEnum;
import net.community.chest.resources.SystemPropertiesResolver;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 7, 2011 9:14:56 AM
 *
 */
public final class Main extends BaseMain {
    private Main (final String ... args)
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
            if ("--location".equalsIgnoreCase(opt))
            {
                aIndex = addExtraArgument(opt, procArgs, aIndex, args);

                final String    argVal=procArgs.get(opt),
                                filePath=SystemPropertiesResolver.SYSTEM.format(argVal);
                frame.loadFile(new File(filePath), opt, null);
            }
            else if ("--recursive".equalsIgnoreCase(opt))
            {
                frame.setRecursiveScanning(true);
            }
            else
                throw new IllegalArgumentException("Unknown option: " + opt);
        }

        // if no POM file specified by the user then try to load the CWD one (if any)
        final String    curdir=SysPropsEnum.USERDIR.getPropertyValue();
        if ((curdir != null) && (curdir.length() > 0))
        {
            final String    rootFolder=frame.getRootFolder();
            if ((rootFolder == null) || (rootFolder.length() <= 0))
                frame.loadFile(new File(curdir), SysPropsEnum.USERDIR.getPropertyName(), null);
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
        return processMainArgs(new MainFrame(), getMainArguments());
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (final String[] args)
    {
        SwingUtilities.invokeLater(new Main(args));
    }
}

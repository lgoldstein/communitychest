/*
 *
 */
package net.community.chest.javaagent.dumper.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMain;
import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.javaagent.dumper.ui.resources.ResourcesAnchor;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 14, 2011 11:07:59 AM
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
    protected JFrame createMainFrameInstance () throws Exception
    {
        return new JavaAgentDumperMainFrame(getMainArguments());
    }

    public static void main (String[] args)
    {
        // 1st thing we do before any UI startup
        AbstractXmlProxyConverter.setDefaultLoader(ResourcesAnchor.getInstance());
        SwingUtilities.invokeLater(new Main(args));
    }
}

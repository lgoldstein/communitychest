package net.community.apps.common.test;

import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;

import net.community.apps.common.BaseMainFrame;
import net.community.apps.common.test.resources.TestResourcesAnchor;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 19, 2008 11:33:17 AM
 */
public class TestMainFrame extends BaseMainFrame<TestResourcesAnchor> {
    /**
     *
     */
    private static final long serialVersionUID = 7869816840380547177L;
    public TestMainFrame (String... args) throws Exception
    {
        super(args);
    }

    protected static final LoggerWrapper    _logger=
        WrapperFactoryManager.getLogger(TestMainFrame.class);
    /*
     * @see net.community.apps.common.BaseMainFrame#getLogger()
     */
    @Override
    protected LoggerWrapper getLogger ()
    {
        return _logger;
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#getResourcesAnchor()
     */
    @Override
    public TestResourcesAnchor getResourcesAnchor ()
    {
        return TestResourcesAnchor.getInstance();
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#getActionListenersMap(boolean)
     */
    @Override
    protected Map<String,? extends ActionListener> getActionListenersMap (boolean createIfNotExist)
    {
        final Map<String,? extends ActionListener>    org=super.getActionListenersMap(createIfNotExist);
        if (((org != null) && (org.size() > 0)) || (!createIfNotExist))
            return org;

        final Map<String,ActionListener>    lm=new TreeMap<String,ActionListener>(String.CASE_INSENSITIVE_ORDER);
        lm.put("load",getLoadFileListener());
        lm.put("exit", getExitActionListener());
        lm.put("about", getShowManifestActionListener());

        setActionListenersMap(lm);
        return lm;
    }
}

/*
 *
 */
package net.community.apps.apache.http.jmxaccessor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import net.community.apps.apache.http.jmxaccessor.resources.ResourcesAnchor;
import net.community.apps.common.BaseMainFrame;
import net.community.chest.apache.httpclient.jmx.JMXSession;
import net.community.chest.apache.log4j.Log4jUtils;
import net.community.chest.apache.log4j.factory.Log4jLoggerWrapperFactory;
import net.community.chest.jmx.JMXErrorHandler;
import net.community.chest.jmx.dom.MBeanEntryDescriptor;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.LoggerWrapperFactory;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 14, 2011 2:32:04 PM
 *
 */
final class MainFrame extends BaseMainFrame<ResourcesAnchor> implements JMXErrorHandler {
    /**
     *
     */
    private static final long serialVersionUID = -4362345400725400074L;
    private static LoggerWrapper    _logger    /* =null */;
    /*
     * @see net.community.apps.common.BaseMainFrame#getLogger()
     */
    @Override
    protected synchronized LoggerWrapper getLogger ()
    {
        if (null == _logger)
            _logger = WrapperFactoryManager.getLogger(getClass());
        return _logger;
    }
    /*
     * @see net.community.chest.jmx.JMXErrorHandler#mbeanError(java.lang.String, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void mbeanError (String mbName, String msg, Throwable t)
    {
        JOptionPane.showMessageDialog(this, msg, t.getClass().getName(), JOptionPane.ERROR_MESSAGE);
        getLogger().error("[" + mbName + "] " + msg, t);
    }
    /*
     * @see net.community.chest.jmx.JMXErrorHandler#mbeanWarning(java.lang.String, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void mbeanWarning (String mbName, String msg, Throwable t)
    {
        JOptionPane.showMessageDialog(this, msg, t.getClass().getName(), JOptionPane.WARNING_MESSAGE);
        getLogger().warn("[" + mbName + "] " + msg, t);
    }
    /*
     * @see net.community.chest.jmx.JMXErrorHandler#errorThrowable(java.lang.Throwable)
     */
    @Override
    public <T extends Throwable> T errorThrowable (T t)
    {
        BaseOptionPane.showMessageDialog(this, t);
        return getLogger().errorThrowable(t);
    }
    /*
     * @see net.community.chest.jmx.JMXErrorHandler#warnThrowable(java.lang.Throwable)
     */
    @Override
    public <T extends Throwable> T warnThrowable (T t)
    {
        BaseOptionPane.showMessageDialog(this, t);
        return getLogger().warnThrowable(t);
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#getResourcesAnchor()
     */
    @Override
    public ResourcesAnchor getResourcesAnchor ()
    {
        return ResourcesAnchor.getInstance();
    }

    void process (Collection<? extends MBeanEntryDescriptor> descs)
    {
        // TODO process the descriptors
    }

    private MBeansPopulator    _popl;
    void done (MBeansPopulator popl)
    {
        if (_popl != popl)
            throw new IllegalStateException("Mismatched populators instances");

        _popl = null;
    }

    private File    _curFile;
    protected void refresh ()
    {
        if (_popl != null)
            return;

        if (_curFile != null)
            _popl = new MBeansPopulator(this, _curFile);
        else
            _popl = new MBeansPopulator(this, getJMXSession());
        _popl.execute();
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void loadFile (File f, String cmd, Element dlgElement)
    {
        final String filePath=(null == f) ? null : f.getAbsolutePath();
        if ((null == filePath) || (filePath.length() <= 0))
            return;

        disconnect();

        _curFile = f;
        updateStatusBar(f.getAbsolutePath());
        refresh();
    }

    private final JMXSession    _session=new JMXSession();
    public final JMXSession getJMXSession ()
    {
        return _session;
    }

    public boolean isConnected ()
    {
        if (_session.isOpen() || (_curFile != null))
            return true;
        else
            return false;
    }

    protected void disconnect ()
    {
        final JMXSession    session=getJMXSession();
        try
        {
            session.close();
        }
        catch(Exception e)
        {
            getLogger().warnThrowable(e);
            BaseOptionPane.showMessageDialog(this, e);
        }

        if (_curFile != null)
            _curFile = null;
    }

    protected void connect (String connValue, boolean autoRefresh)
    {
        disconnect();

        try
        {
            final JMXSession    session=getJMXSession();
            session.connect(new URI(connValue));
            updateStatusBar(connValue);

            if (autoRefresh)
                refresh();
        }
        catch(Exception e)
        {
            getLogger().warnThrowable(e);
            BaseOptionPane.showMessageDialog(this, e);
        }
    }

    protected void connect (boolean autoRefresh)
    {
        final JMXSession    session=getJMXSession();
        final URI            uri=session.getAccessURL();
        final Object        reply=JOptionPane.showInputDialog(this, "URL:", "JMX Server URL", JOptionPane.PLAIN_MESSAGE, null, null, (uri == null) ? null : uri.toString());
        if (reply == null)
            return;

        connect(reply.toString(), autoRefresh);
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#saveFile(java.io.File, org.w3c.dom.Element)
     */
    @Override
    public void saveFile (File f, Element dlgElement)
    {
        // TODO implement it
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
        lm.put(EXIT_CMD, getExitActionListener());
        lm.put(ABOUT_CMD, getShowManifestActionListener());
        lm.put(LOAD_CMD, getLoadFileListener());
        lm.put(SAVE_CMD, getSaveFileListener());
        lm.put("connect", new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    connect(true);
                }
            });
        lm.put("refresh", new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    refresh();
                }
            });

        setActionListenersMap(lm);
        return lm;
    }

    private JTextField    _statusBar    /* =null */;
    public void updateStatusBar (final String text)
    {
        if (_statusBar != null)
            _statusBar.setText((null == text) ? "" : text);
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final Container    ctPane=getContentPane();
        {
            final JToolBar                                b=getMainToolBar();
            final Map<String,? extends AbstractButton>    hm=setToolBarHandlers(b);
            if ((hm != null) && (hm.size() > 0))
                ctPane.add(b, BorderLayout.NORTH);
        }

        if (_statusBar == null)
        {
            _statusBar = new JTextField("Ready");
            _statusBar.setEditable(false);
            ctPane.add(_statusBar, BorderLayout.SOUTH);
        }
    }

    private void processMainArguments (final String ... args) throws Exception
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; aIndex < numArgs; aIndex++)
        {
            if (isConnected())
                throw new IllegalStateException("Already connected");

            final String    op=args[aIndex];
            if ("-f".equals(op))
            {
                aIndex++;
                loadFile(new File(args[aIndex]), LOAD_CMD, null);
            }
            else if ("-s".equals(op))
            {
                aIndex++;
                connect(args[aIndex], true);
            }
            else
                throw new NoSuchElementException("Unknown command line argument: " + op);
        }
    }
    /**
     * @param args original arguments as received by <I>main</I> entry point
     * @throws Exception if unable to start main frame and application
     */
    MainFrame (final String ... args) throws Exception
    {
        super(false /* don't layout the frame yet */, args);

        // initialize log4j and set the default logging factory to be the log4j one
        try
        {
            final ResourcesAnchor    a=getResourcesAnchor();
            final Document            log4jDoc=Log4jUtils.log4jInit(a);
            if (null == log4jDoc)
                throw new MissingResourceException("Missing log4j configuration file", Log4jUtils.class.getName(), Log4jUtils.DEFAULT_CONFIG_FILE_NAME);

            final LoggerWrapperFactory    cur=Log4jLoggerWrapperFactory.replaceCurrentFactory();
            if (!(cur instanceof Log4jLoggerWrapperFactory))
                getLogger().info("set " + Log4jLoggerWrapperFactory.class.getSimpleName() + " instance");
        }
        catch(Exception e)
        {
            BaseOptionPane.showMessageDialog(this, e);
            getLogger().error(e.getClass().getName() + " while initialize log4j: " + e.getMessage());
        }

        try
        {
            layoutComponent();
        }
        catch(Exception e)
        {
            getLogger().error(e.getClass().getName() + " while layout component: " + e.getMessage());
            BaseOptionPane.showMessageDialog(this, e);
        }

        try
        {
            processMainArguments(args);
        }
        catch(Exception e)
        {
            getLogger().error(e.getClass().getName() + " while process arguments: " + e.getMessage());
            BaseOptionPane.showMessageDialog(this, e);
        }
    }

}

/*
 *
 */
package net.community.apps.tools.svn.svnsync;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import net.community.apps.tools.svn.SVNBaseMain;
import net.community.apps.tools.svn.SVNBaseMainFrame;
import net.community.apps.tools.svn.svnsync.resources.ResourcesAnchor;
import net.community.chest.awt.AWTUtils;
import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.dom.UIReflectiveAttributesProxy;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.common.MethodsComparator;
import net.community.chest.svnkit.SVNAccessor;
import net.community.chest.svnkit.SVNFoldersFilter;
import net.community.chest.svnkit.SVNLocation;
import net.community.chest.svnkit.core.io.SVNRepositoryFactoryType;
import net.community.chest.svnkit.core.wc.SVNEventActionEnum;
import net.community.chest.swing.component.menu.MenuItemExplorer;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.swing.resources.UIAnchoredResourceAccessor;
import net.community.chest.ui.components.logging.LogMessagesArea;
import net.community.chest.ui.components.text.FileAutoCompleter;
import net.community.chest.ui.components.text.FolderAutoCompleter;
import net.community.chest.ui.helpers.button.HelperCheckBox;
import net.community.chest.ui.helpers.panel.input.LRFieldWithButtonPanel;
import net.community.chest.ui.helpers.panel.input.LRFieldWithLabelPanel;
import net.community.chest.ui.helpers.text.InputTextField;
import net.community.chest.util.compare.RegexpPatternComparator;
import net.community.chest.util.logging.LogLevelWrapper;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 19, 2010 11:40:23 AM
 *
 */
class SVNSyncMainFrame extends SVNBaseMainFrame<ResourcesAnchor> implements Runnable {
    /**
     *
     */
    private static final long serialVersionUID = -5894339453538002310L;
    private static final LoggerWrapper    _logger=WrapperFactoryManager.getLogger(SVNSyncMainFrame.class);

    SVNSyncMainFrame () throws Exception
    {
        super(true);
    }
    /*
     * @see net.community.apps.common.MainComponent#getResourcesAnchor()
     */
    @Override
    public ResourcesAnchor getResourcesAnchor ()
    {
        return ResourcesAnchor.getInstance();
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#getLogger()
     */
    @Override
    protected LoggerWrapper getLogger ()
    {
        return _logger;
    }
    /*
     * @see net.community.apps.common.FilesLoadMainFrame#getFileChooser(org.w3c.dom.Element, java.lang.String, java.lang.Boolean)
     */
    @Override
    protected JFileChooser getFileChooser (
            final Element dlgElement, final String cmd, final Boolean isSaveDialog)
    {
        final JFileChooser    fc=super.getFileChooser(dlgElement, cmd, isSaveDialog);
        if (fc != null)
            fc.setFileFilter(SVNFoldersFilter.DEFAULT);
        return fc;
    }

    private LRFieldWithButtonPanel    _wcTarget;
    private FileAutoCompleter<JTextComponent>    _wcTargetAutoComplete;
    /*
     * @see net.community.apps.tools.svn.SVNBaseMainFrame#getWCLocation()
     */
    @Override
    public String getWCLocation ()
    {
        return (null == _wcTargetAutoComplete) ? null : _wcTargetAutoComplete.getText();
    }

    protected AbstractButton    _runBtn, _stopBtn;
    private final KeyListener _runOptionKeyListener=new KeyAdapter() {
            /*
             * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
             */
            @Override
            public void keyReleased (KeyEvent e)
            {
                if ((e == null) || (_runBtn == null) || (_runMenuItem == null))
                    return;

                final boolean    okToRun=isOkToRun();
                _runBtn.setEnabled(okToRun);
                _runMenuItem.setEnabled(okToRun);
            }
        };
    /*
     * @see net.community.apps.tools.svn.SVNBaseMainFrame#setWCLocation(java.io.File, java.lang.String, boolean)
     */
    @Override
    public boolean setWCLocation (File orgFile, String orgPath, boolean forceRefresh)
    {
        if (null == _wcTarget)
            return false;

        final String    filePath;
        if ((null == orgPath) || (orgPath.length() <= 0))
            filePath = (null == orgFile) ? "" : orgFile.getAbsolutePath();
        else
            filePath = orgPath;

        final String    prev=_wcTarget.getText();
        if ((!forceRefresh) && (0 == StringUtil.compareDataStrings(prev, filePath, true)))
            return false;

        _wcTarget.setText(filePath);
        setTitle(filePath);

        if (!filePath.contains("://"))
        {
            final File        saveDir=getInitialFileChooserFolder();
            final String    savePath=(saveDir == null) ? null : saveDir.getAbsolutePath();
            if (!filePath.equals(savePath))
                setInitialFileChooserFolder(new File(filePath), Boolean.FALSE);
        }

        if (_runBtn != null)
            _runBtn.setEnabled(isOkToRun());

        if ((null == filePath) || (filePath.length() <= 0))
            return false;    // can occur for the top-level volume root(s)

        return true;
    }
    /*
     * @see net.community.apps.common.FileLoadComponent#saveFile(java.io.File, org.w3c.dom.Element)
     */
    @Override
    public void saveFile (File f, Element dlgElement)
    {
        if ((null == f) || (!f.isDirectory()))
            return;

        setWCLocation(f, f.getAbsolutePath(), false);

        if (_runBtn != null)
            _runBtn.setEnabled(isOkToRun());
    }

    private LRFieldWithButtonPanel    _syncSource;
    private SVNSyncAutoCompleter<JTextComponent>    _syncSourceCompleter;
    public String getSynchronizationSource ()
    {
        return (null == _syncSourceCompleter) ? null : _syncSourceCompleter.getText();
    }

    public void setSynchronizationSource (String filePath)
    {
        if (null == _syncSourceCompleter)
            return;

        final String    prev=_syncSourceCompleter.getText();
        if (0 == StringUtil.compareDataStrings(prev, filePath, true))
            return;

        _syncSourceCompleter.setText(filePath);

        if (!filePath.contains("://"))
        {
            final File        saveDir=getInitialFileChooserFolder();
            final String    savePath=(saveDir == null) ? null : saveDir.getAbsolutePath();
            if (!filePath.equals(savePath))
                setInitialFileChooserFolder(new File(filePath), Boolean.TRUE);
        }

        if (_runBtn != null)
            _runBtn.setEnabled(isOkToRun());
    }

    private final Set<Pattern>    _confirmLocations=new TreeSet<Pattern>(RegexpPatternComparator.ASCENDING);
    public void setConfirmLocations (Collection<? extends Pattern> patterns)
    {
        if (!_confirmLocations.isEmpty())
            _confirmLocations.clear();
        if ((patterns == null) || (patterns.size() <= 0))
            return;    // debug breakpoint
        _confirmLocations.addAll(patterns);
    }

    public boolean isSyncConfirmationRequired (final String srcPath)
    {
        if ((srcPath == null) || (srcPath.length() <= 0))
            return false;

        if (_confirmLocations.isEmpty())
            return false;

        for (final Pattern p : _confirmLocations)
        {
            final Matcher    m=p.matcher(srcPath);
            if (m.matches())
                return true;
        }

        return false;
    }

    public boolean addConfirmLocation (String loc)
    {
        if ((loc == null) || (loc.length() <= 0))
            return false;

        for (final Pattern p : _confirmLocations)
        {
            final String    pValue=p.pattern();
            if (loc.equals(pValue))
                return false;
        }

        final Pattern    p=Pattern.compile(loc);
        return _confirmLocations.add(p);
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void loadFile (File f, String cmd, Element dlgElement)
    {
        if ((null == f) || (!f.isDirectory()))
            return;

        setSynchronizationSource(f.getAbsolutePath());
    }

    private LogMessagesArea    _logsArea;
    protected void clearLogMessagesArea ()
    {
        if (_logsArea != null)
            _logsArea.setText("");
        if (_numSyncEvents > 0L)
            _numSyncEvents = 0L;
    }

    private static final LogLevelWrapper action2level (final SVNEventActionEnum    action)
    {
        if (SVNEventActionEnum.SKIP.equals(action))
            return LogLevelWrapper.DEBUG;
        else if (SVNEventActionEnum.DELETE.equals(action))
            return LogLevelWrapper.VERBOSE;
        else
            return LogLevelWrapper.INFO;
    }

    private Map<SVNEventActionEnum,MutableAttributeSet>    _actionIcons;
    private Map<SVNEventActionEnum,MutableAttributeSet> createActionIconsMap (Element root) throws Exception
    {
        final Collection<? extends Element>    el=
            DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
        final int                            numElems=(null == el) ? 0 : el.size();
        if (numElems <= 0)
            return null;

        final Map<SVNEventActionEnum,MutableAttributeSet>    ret=
            new EnumMap<SVNEventActionEnum,MutableAttributeSet>(SVNEventActionEnum.class);
        final UIAnchoredResourceAccessor    resLoader=getResourcesAnchor();
        for (final Element elem : el)
        {
            final String    n=elem.getAttribute(UIReflectiveAttributesProxy.NAME_ATTR),
                            i=elem.getAttribute(Iconable.ATTR_NAME);
            if ((null == n) || (n.length() <= 0)
             || (null == i) || (i.length() <= 0))
                continue;

            final SVNEventActionEnum    a=SVNEventActionEnum.fromString(n);
            if (null == a)
                throw new NoSuchElementException("createActionIconsMap(" + DOMUtils.toString(elem) + ") unknown action: " + n);

            final Icon    icon=resLoader.getIcon(i);
            if (null == icon)
                continue;

            final MutableAttributeSet    s=new SimpleAttributeSet();
            StyleConstants.setIcon(s, icon);

            final MutableAttributeSet    prev=ret.put(a, s);
            if (prev != null)
                throw new IllegalStateException("createActionIconsMap(" + DOMUtils.toString(elem) + ") multiple icons for action=" + n);
        }

        return ret;
    }

    private HelperCheckBox    _showSkipped;
    public boolean isShowSkippedTargetsEnabled ()
    {
        return (_showSkipped != null) && _showSkipped.isSelected();
    }

    public void setShowSkippedTargetsEnabled (boolean enabled)
    {
        if ((_showSkipped == null) || (_showSkipped.isSelected() == enabled))
            return;

        _showSkipped.setSelected(enabled);
    }

    private HelperCheckBox    _useMerge;
    public boolean isUseMergeForUpdate ()
    {
        return (_useMerge != null) && _useMerge.isSelected();
    }

    public void setUseMergeForUpdate (boolean enabled)
    {
        if ((_useMerge == null) || (_useMerge.isSelected() == enabled))
            return;

        _useMerge.setSelected(enabled);
    }

    private long    _numSyncEvents;
    // return TRUE if OK to continue
    boolean handleSyncEvent (SVNSyncEvent event)
    {
        if (null == event)
            return false;

        if (null == _logsArea)
            return true;

        final SVNEventActionEnum    action=event.getSyncAction();
        final boolean                skipAction=SVNEventActionEnum.SKIP.equals(action);
        if (!skipAction)
            _numSyncEvents++;

        final Throwable                t=event.getActionError();
        final StackTraceElement[]    ste=(t == null) ? null : t.getStackTrace();
        if ((ste != null) && (ste.length > 0))
        {
            try
            {
                _logsArea.log(LogLevelWrapper.ERROR,
                        "Failed (" + t.getClass().getName() + ")"
                      + " to synchronize " + event.getSourceFile()
                      + " with " + event.getTargetFile()
                      + ": " + t.getMessage());
            }
            catch(BadLocationException e)
            {
                // ignored
            }

            _logger.warn(event.toString());
            return false;
        }

        updateHeartbeatInfo(_runner);

        if (skipAction)
        {
            if (_logger.isDebugEnabled())
                _logger.debug(event.toString());

            // check if enable progress messages
            if (!isShowSkippedTargetsEnabled())
                return true;
        }

        final LogLevelWrapper    level=action2level(action);
        if (null == level)
            return true;

        final SVNLocation    f=event.getTargetFile();
        final AttributeSet    la=_logsArea.getLogLevelAttributes(level),
                            ia=(null == _actionIcons) ? null : _actionIcons.get(action);
        try
        {
            _logsArea.log(level, String.valueOf(f), la, ia);
        }
        catch(BadLocationException e)
        {
            // ignored
        }

        if (SVNEventActionEnum.UPDATE_REPLACE.equals(action))
            handleProperties(event.getAddedProperties(), event.getDeletedProperties(), event.getUpdatedProperties());

        _logger.info(event.toString());
        return true;
    }

    private void handleProperties (final Map<String,String> addProps, final Map<String,String> delProps, final Map<String,String> updProps)
    {
        final Object[]    propMaps={
                SVNEventActionEnum.ADD,                addProps,
                SVNEventActionEnum.DELETE,            delProps,
                SVNEventActionEnum.UPDATE_EXISTS,    updProps
            };
        final AttributeSet    la=_logsArea.getLogLevelAttributes(LogLevelWrapper.INFO);

        for (int    mIndex=0; mIndex < propMaps.length; mIndex += 2)
        {
            final SVNEventActionEnum    action=(SVNEventActionEnum) propMaps[mIndex];
            @SuppressWarnings("unchecked")
            final Map<String,String>    propsMap=(Map<String,String>) propMaps[mIndex+1];
            if ((action == null) || (propsMap == null) || (propsMap.size() <= 0))
                continue;

            final AttributeSet        ia=(null == _actionIcons) ? null : _actionIcons.get(action);
            final LogLevelWrapper    level=action2level(action);
            for (final Map.Entry<String,String> pp : propsMap.entrySet())
            {
                final String    pName=pp.getKey(), pValue=pp.getValue();
                try
                {
                    _logsArea.log(level, "    " + pName + "=" + pValue, la, ia);
                }
                catch(BadLocationException e)
                {
                    // ignored
                }
            }
        }
    }
    /* coalesce SVNSynchronizer heartbeat events
     * @see java.awt.Component#coalesceEvents(java.awt.AWTEvent, java.awt.AWTEvent)
     */
    @Override
    protected AWTEvent coalesceEvents (AWTEvent existingEvent, AWTEvent newEvent)
    {
        final int    existID=(existingEvent == null) ? (-1) : existingEvent.getID(),
                    newID=(newEvent == null) ? (-1) : newEvent.getID();
        if ((existID == SVNSyncHeartbeatEvent.ID)
         && (newID == SVNSyncHeartbeatEvent.ID))
             return existingEvent;

        return super.coalesceEvents(existingEvent, newEvent);
    }
    /*
     * @see java.awt.Window#processEvent(java.awt.AWTEvent)
     */
    @Override
    protected void processEvent (AWTEvent e)
    {
        final int    id=(e == null) ? (-1) : e.getID();
        if (id == SVNSyncHeartbeatEvent.ID)
        {
            final SVNSynchronizer    src=((SVNSyncHeartbeatEvent) e).getSynchronizer();
            if (src == _runner)
                updateHeartbeatInfo(src);
            return;
        }

        super.processEvent(e);
    }

    private Map<Method,LRFieldWithLabelPanel>    _infoMap;
    private Map<Method,LRFieldWithLabelPanel> getHeartbeatInfoMap ()
    {
        if (_infoMap == null)
            _infoMap = new TreeMap<Method,LRFieldWithLabelPanel>(MethodsComparator.ASCENDING);
        return _infoMap;
    }

    private void updateHeartbeatInfo (final SVNSynchronizer r)
    {
        final Map<Method,LRFieldWithLabelPanel>    infoMap=(r == null) ? null : getHeartbeatInfoMap();
        if ((infoMap == null) || infoMap.isEmpty())
            return;

        for (final Map.Entry<Method,LRFieldWithLabelPanel> ie : infoMap.entrySet())
        {
            final Method    m=ie.getKey();
            Object            v=null;
            try
            {
                if ((v=m.invoke(r)) == null)
                    throw new IllegalStateException("No value returned by method=" + m);
            }
            catch(Exception e)
            {
                getLogger().error(e.getClass().getName() + " while invoke method=" + m + ": " + e.getMessage());
            }

            final LRFieldWithLabelPanel    p=ie.getValue();
            p.setText((v == null) ? "???" : v.toString());
        }
    }

    private boolean    _running    /* =false */;
    protected boolean isOkToRun ()
    {
        final String    srcFolder=getSynchronizationSource(),
                        dstFolder=getWCLocation();
        if ((null == srcFolder) || (srcFolder.length() <= 0)
         || (null == dstFolder) || (dstFolder.length() <= 0))
            return false;

        return !_running;
    }

    protected JMenuItem    _runMenuItem, _loadMenuItem, _saveMenuItem, _stopMenuItem;
    protected void setRunningMode (boolean running)
    {
        if (_running != running)
        {
            AttrUtils.setComponentEnabledState(!running,
                    _wcTarget, _syncSource, _runBtn,
                    _runMenuItem, _loadMenuItem, _saveMenuItem);

            final Cursor    c=running
                    ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                    : Cursor.getDefaultCursor()
                    ;
            if ((c != null) && (_logsArea != null))
                _logsArea.setCursor(c);

            AttrUtils.setComponentEnabledState(running, _stopMenuItem, _stopBtn);

            _running = running;
        }
    }

    private HelperCheckBox    _skipProps;
    public boolean isPropertiesSyncAllowed ()
    {
        return (_skipProps == null) || (!_skipProps.isSelected());
    }


    public void setPropertiesSyncAllowed (boolean enabled)
    {
        if ((_skipProps == null) || (_skipProps.isSelected() == enabled))
            return;

        _skipProps.setSelected(enabled);
    }

    private SVNAccessor resolveAuthenticationParameters (final SVNAccessor authData)
    {
        if (authData == null)
            return null;

        final SVNSyncAuthDataDialog    authDlg=new SVNSyncAuthDataDialog(this, authData);
        authDlg.setVisible(true);
        // NOTE: rely on the fact that it is a MODAL dialog
        if (authDlg.isOkExit())
        {
            authData.setSVNAuthManager(null);    // force re-creation
            return authData;
        }

        return null;
    }

    protected void editAuthentication ()
    {
        resolveAuthenticationParameters(SVNBaseMain.getSVNAccessor());
    }

    private static boolean isAuthRequired (final SVNAccessor acc)
    {
        final String[]        params={
                (acc == null) ? null : acc.getUsername(),
                (acc == null) ? null : acc.getPassword()
            };
        for (final String p : params)
        {
            if ((p == null) || (p.length() <= 0))
                return true;
        }

        return false;
    }

    SVNDiffClient createDiffClientInstance (final SVNWCClient wcc)
    {
        if (wcc == null)
            return null;

        final SVNClientManager    mgr=SVNBaseMain.getSVNClientManager(true);
        return mgr.getDiffClient();
    }

    Process executeMergeCommand (File srcFile, File dstFile, File resFile) throws IOException
    {
        if ((srcFile == null) || (dstFile == null) || (resFile == null))
            return null;

        final Runtime    r=Runtime.getRuntime();
        final String[]    cmdArray={
                "C:\\Program Files\\WinMerge\\WinMergeU.exe",
                "/e",
                "/x",
                "/s",
                srcFile.getAbsolutePath(),
                dstFile.getAbsolutePath(),
                resFile.getAbsolutePath()
            };
        // WinMergeU /e /x /s srcFile.getAbsolutePath dstFile.getAbsolutePath
        return r.exec(cmdArray);
    }

    private SVNWCClient createWCClientInstance () throws Exception
    {
        final String        srcPath=getSynchronizationSource(), dstPath=getWCLocation();
        final SVNLocation[]    locs={ SVNLocation.fromString(srcPath), SVNLocation.fromString(dstPath) };
        boolean                authRequired=false;
        for (final SVNLocation l : locs)
        {
            final SVNURL    url=(l == null) ? null : l.getURL();
            if (url == null)
                continue;

            final String    proto=url.getProtocol();
            if (proto.contains("ssh") || proto.contains("https"))
                authRequired = true;

            final SVNRepositoryFactoryType    facType=SVNRepositoryFactoryType.setup(url);
            if (facType == null)
                continue;

            getLogger().info("Initialized " + facType + " factory due to SVN URL=" + url);
        }

        // check if have username & password
        final SVNAccessor    acc=SVNBaseMain.getSVNAccessor();
        if (authRequired)
            authRequired = isAuthRequired(acc);

        if (authRequired)
        {
            final SVNAccessor    authData=resolveAuthenticationParameters(acc);
            if ((authData == null) || isAuthRequired(authData))
                return null;
        }

        SVNWCClient    wcc=null;
        for (final SVNLocation l : locs)
        {
            final File    f=(l == null) ? null : l.getFile();
            if (f == null)
                continue;

            if (wcc == null)
            {
                final SVNClientManager    mgr=SVNBaseMain.getSVNClientManager(true);
                wcc = mgr.getWCClient();
            }

            final SVNURL                    url=
                wcc.getReposRoot(f, null, SVNRevision.WORKING, null, null);
            final SVNRepositoryFactoryType    facType=
                SVNRepositoryFactoryType.setup(url);
            if (facType == null)
                continue;
            getLogger().info("Initialized " + facType + " factory due to file URL=" + url);
        }

        if (wcc == null)
        {
            final SVNClientManager    mgr=SVNBaseMain.getSVNClientManager(true);
            wcc = mgr.getWCClient();
        }

        return wcc;
    }

    private SVNSynchronizer    _runner;
    /*
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run ()
    {
        if (_runner != null)
        {
            JOptionPane.showMessageDialog(this, "Stop current synchronization before starting another", "Sync. in progress", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final String    srcFolder=getSynchronizationSource(),
                        dstFolder=getWCLocation();
        if ((null == srcFolder) || (srcFolder.length() <= 0)
         || (null == dstFolder) || (dstFolder.length() <= 0))
        {
            JOptionPane.showMessageDialog(this, "Missing source/target", "Incomplete arguments", JOptionPane.ERROR_MESSAGE);
            return;
        }

        clearLogMessagesArea();

        final SVNWCClient    wcc;
        try
        {
            if ((wcc=createWCClientInstance()) == null)
                return;    // aborted by the user
        }
        catch(Exception e)
        {
            BaseOptionPane.showMessageDialog(this, e);
            return;
        }

        _runner = new SVNSynchronizer(this, wcc);
           setRunningMode(true);
        _runner.execute();
    }

    protected void stop ()
    {
        if ((_runner == null) || _runner.isDone() || _runner.isCancelled())
            return;

        _runner.cancel(false);
        _logger.info("Canceled by user request");
    }

    private String buildSynchronizationStatisticalData (final SVNSynchronizer r)
    {
        if (r == null)
            return "";

        final String[]    values={
                "Num. processed files",     String.valueOf(r.getNumProcessedFiles()),
                "Num. processed folders",    String.valueOf(r.getNumProcessedFolders()),
                "Num. added nodes",            String.valueOf(r.getNumAddedNodes()),
                "Num. deleted nodes",        String.valueOf(r.getNumDeletedNodes()),
                "Num. updated nodes",        String.valueOf(r.getNumUpdatedNodes())
            };
        final StringBuilder    sb=new StringBuilder(values.length * 24);
        for (int    vIndex=0; vIndex < values.length; vIndex += 2)
        {
            final String    vName=values[vIndex], vValue=values[vIndex+1];
            if ("0".equals(vValue))
                continue;

            if (sb.length() > 0)
                sb.append('\n');
            sb.append('\t')
              .append(vName)
              .append(": ")
              .append(vValue)
              ;
        }

        if (sb.length() > 0)
            return sb.toString();
        return "";
    }

    void signalSynchronizationDone (final SVNSynchronizer r)
    {
        if (r != null)
        {
            if (_runner != r)
                _logger.warn("signalSynchronizationDone() mismatched instances");
            _runner = null;

            final String    dataInfo=buildSynchronizationStatisticalData(r);
            if (r.isCancelled())
                JOptionPane.showMessageDialog(this, "Synchronization aborted\n\n" + dataInfo, "Canceled by user request", JOptionPane.WARNING_MESSAGE);
            else if (_numSyncEvents <= 0L)
                JOptionPane.showMessageDialog(this, "Synchronized folders are identical\n\n" + dataInfo, "No synchronization events", JOptionPane.INFORMATION_MESSAGE);
            else
                JOptionPane.showMessageDialog(this, "Synchronized folders completed\n\n" + dataInfo, "Synchronization done", JOptionPane.INFORMATION_MESSAGE);
        }

           setRunningMode(false);
    }
    /*
     * @see net.community.chest.ui.helpers.frame.HelperFrame#layoutSection(java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void layoutSection (String name, Element elem) throws RuntimeException
    {
        if (_logger.isDebugEnabled())
            _logger.debug("layoutSection(" + name + ")[" + DOMUtils.toString(elem) + "]");

        if ("wc-target".equalsIgnoreCase(name))
        {
            if (_wcTarget != null)
                throw new IllegalStateException("layoutSection(" + name + ") already initialized for " + DOMUtils.toString(elem));

            // delay auto-layout till after setting the text field
            _wcTarget = new LRFieldWithButtonPanel(elem, false);
            _wcTarget.setTextField(new InputTextField());
            _wcTarget.layoutComponent();
            _wcTarget.addActionListener(getSaveFileListener());
            _wcTarget.addTextFieldKeyListener(_runOptionKeyListener);
            _wcTargetAutoComplete = new FolderAutoCompleter<JTextComponent>(_wcTarget.getTextField());
        }
        else if ("sync-source".equalsIgnoreCase(name))
        {
            if (_syncSource != null)
                throw new IllegalStateException("layoutSection(" + name + ") already initialized for " + DOMUtils.toString(elem));

            // delay initialization
            _syncSource = new LRFieldWithButtonPanel(elem, false);
            _syncSource.setTextField(new InputTextField());
            _syncSource.layoutComponent();
            _syncSource.addActionListener(getLoadFileListener());
            _syncSource.addTextFieldKeyListener(_runOptionKeyListener);
            _syncSourceCompleter = new SVNSyncAutoCompleter<JTextComponent>(_syncSource.getTextField());
        }
        else if ("log-msgs-area".equalsIgnoreCase(name))
        {
            if (_logsArea != null)
                throw new IllegalStateException("layoutSection(" + name + ") already initialized for " + DOMUtils.toString(elem));

            _logsArea = new LogMessagesArea(Font.getFont(Font.DIALOG), elem);
        }
        else if ("show-skipped".equalsIgnoreCase(name))
        {
            if (_showSkipped != null)
                throw new IllegalStateException("layoutSection(" + name + ") already initialized for " + DOMUtils.toString(elem));

            _showSkipped = new HelperCheckBox(elem);
        }
        else if ("use-merge".equalsIgnoreCase(name))
        {
            if (_useMerge != null)
                throw new IllegalStateException("layoutSection(" + name + ") already initialized for " + DOMUtils.toString(elem));

            _useMerge = new HelperCheckBox(elem);
        }
        else if ("skip-props".equalsIgnoreCase(name))
        {
            if (_skipProps != null)
                throw new IllegalStateException("layoutSection(" + name + ") already initialized for " + DOMUtils.toString(elem));

            _skipProps = new HelperCheckBox(elem);
        }
        else if ("sync-action-icon".equalsIgnoreCase(name))
        {
            if (_actionIcons != null)
                throw new IllegalStateException("layoutSection(" + name + ") already initialized for " + DOMUtils.toString(elem));

            try
            {
                _actionIcons = createActionIconsMap(elem);
            }
            catch(Exception e)
            {
                throw ExceptionUtil.toRuntimeException(e);
            }
        }
        else
            super.layoutSection(name, elem);
    }
    /*
     * @see net.community.apps.common.FilesLoadMainFrame#exitApplication()
     */
    @Override
    public void exitApplication ()
    {
        if (_runner != null)
        {
            final int    nRes=JOptionPane.showConfirmDialog(this, "Abort currently running synchronization ?", "Synchronization in progress",
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (nRes != JOptionPane.YES_OPTION)
                return;
        }

        super.exitApplication();
    }

    private SVNSyncConfirmationsDialog    _confirmDlg;
    void clearConfirmationsDialog (SVNSyncConfirmationsDialog dlg)
    {
        if (_confirmDlg != null)
        {
            if (_confirmDlg != dlg)
                getLogger().warn("Mismatched confirmation dialog instances");
            _confirmDlg = null;
        }
    }

    protected void editConfirmations ()
    {
        if (_confirmDlg != null)
            return;

        _confirmDlg = new SVNSyncConfirmationsDialog(this, _confirmLocations);
        _confirmDlg.setVisible(true);
    }

    private static final String    RUN_CMD="run", CLEAR_CMD="clear", STOP_CMD="stop", CONFIRM_CMD="confirm", AUTH_CMD="authenticate";
    /*
     * @see net.community.apps.common.FilesLoadMainFrame#getActionListenersMap(boolean)
     */
    @Override
    protected Map<String,? extends ActionListener> getActionListenersMap (boolean createIfNotExist)
    {
        final Map<String,? extends ActionListener>    org=super.getActionListenersMap(createIfNotExist);
        if (((org != null) && (org.size() > 0)) || (!createIfNotExist))
            return org;

        final Map<String,ActionListener>    lm=new TreeMap<String,ActionListener>(String.CASE_INSENSITIVE_ORDER);
        lm.put(LOAD_CMD, getLoadFileListener());
        lm.put(SAVE_CMD, getSaveFileListener());
        lm.put(EXIT_CMD, getExitActionListener());
        lm.put(ABOUT_CMD, getShowManifestActionListener());
        lm.put(RUN_CMD, new ActionListener() {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed (final ActionEvent event)
            {
                if (event != null)
                    run();
            }
        });
        lm.put(CLEAR_CMD, new ActionListener() {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed (final ActionEvent event)
            {
                if (event != null)
                    clearLogMessagesArea();
            }
        });
        lm.put(STOP_CMD, new ActionListener() {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed (final ActionEvent event)
            {
                if (event != null)
                    stop();
            }
        });
        lm.put(CONFIRM_CMD, new ActionListener() {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed (final ActionEvent event)
            {
                if (event != null)
                    editConfirmations();
            }
        });
        lm.put(AUTH_CMD, new ActionListener() {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed (final ActionEvent event)
            {
                if (event != null)
                    editAuthentication();
            }
        });

        setActionListenersMap(lm);
        return lm;
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#setMainMenuItemsActionHandlers(net.community.chest.swing.component.menu.MenuItemExplorer)
     */
    @Override
    protected Map<String,JMenuItem> setMainMenuItemsActionHandlers (MenuItemExplorer ie)
    {
        final Map<String,JMenuItem>    im=super.setMainMenuItemsActionHandlers(ie);
        _loadMenuItem = (null == im) ? null : im.get(LOAD_CMD);
        _saveMenuItem = (null == im) ? null : im.get(SAVE_CMD);
        _stopMenuItem = (null == im) ? null : im.get(STOP_CMD);
        _runMenuItem = (null == im) ? null : im.get(RUN_CMD);
        return im;
    }
    /*
     * @see net.community.chest.ui.helpers.frame.HelperFrame#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final JPanel    northPanel=new JPanel(new GridLayout(0, 1));
        try
        {
            final JToolBar                                b=getMainToolBar();
            final Map<String,? extends AbstractButton>    hm=setToolBarHandlers(b);
            if ((hm != null) && (hm.size() > 0))
            {
                _runBtn = hm.get(RUN_CMD);
                _stopBtn = hm.get(STOP_CMD);
            }

            northPanel.add(b);
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }

        final Container    ctPane=getContentPane();
        {
            if (_syncSource != null)
                northPanel.add(_syncSource);
            if (_wcTarget != null)
                northPanel.add(_wcTarget);
        }

        {
            final Container        optsPanel=new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            AWTUtils.addComponents(optsPanel, _showSkipped, _skipProps, _useMerge );
            northPanel.add(optsPanel);
        }

        ctPane.add(northPanel, BorderLayout.NORTH);

        try
        {
            final Container    centerPanel=new JPanel(new BorderLayout(5, 5)), infoPanel=createInfoPanel();
            if (infoPanel != null)
                centerPanel.add(infoPanel, BorderLayout.NORTH);
            if (_logsArea != null)
                centerPanel.add(new ScrolledComponent<LogMessagesArea>(_logsArea), BorderLayout.CENTER);
            ctPane.add(centerPanel, BorderLayout.CENTER);
        }
        catch(Throwable t)
        {
            BaseOptionPane.showMessageDialog(this, t);
            exitApplication();
        }

        // intercept and handle the closure via click on the "x" button
        addWindowListener(new WindowAdapter() {
                /*
                 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
                 */
                @Override
                public void windowClosing (WindowEvent e)
                {
                    if (e != null)
                        exitApplication();
                }
            });
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // allow SVNSynchronizer heartbeat events
        enableEvents(SVNSyncHeartbeatEvent.ID);
    }

    private Container createInfoPanel () throws SecurityException, NoSuchMethodException
    {
        final String[]    infoFields={
                "Elapsed time:",    "getFormattedElapsedTime",
                "Num. Files:",         "getNumProcessedFiles",
                "Num. Folders:",    "getNumProcessedFolders",
                "Num. Added:",        "getNumAddedNodes",
                "Num. Deleted:",    "getNumDeletedNodes",
                "Num. Updated:",    "getNumUpdatedNodes"
            };
        final Container    infoPanel=new JPanel(new GridLayout(0, infoFields.length / 2, 5, 5));
        for (int    fIndex=0; fIndex < infoFields.length; fIndex += 2)
        {
            final String                fieldText=infoFields[fIndex], methodName=infoFields[fIndex+1];
            final Method                m=SVNSynchronizer.class.getDeclaredMethod(methodName);
            final LRFieldWithLabelPanel    p=new LRFieldWithLabelPanel();
            p.setName(methodName);
            p.setTitle(fieldText);
            p.setEditable(false);
            infoPanel.add(p);

            final Map<Method,LRFieldWithLabelPanel>    infoMap=getHeartbeatInfoMap();
            final LRFieldWithLabelPanel                prev=infoMap.put(m, p);
            if (prev != null)
                continue;
        }

        return infoPanel;
    }
}

/*
 *
 */
package net.community.apps.tools.jardiff;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import net.community.apps.common.BaseMainFrame;
import net.community.apps.tools.jardiff.resources.ResourcesAnchor;
import net.community.chest.awt.AWTUtils;
import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.button.BaseCheckBox;
import net.community.chest.swing.component.menu.MenuItemExplorer;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 2, 2011 9:08:05 AM
 *
 */
final class MainFrame extends BaseMainFrame<ResourcesAnchor> {
    /**
     *
     */
    private static final long serialVersionUID = -8850566574435995333L;
    private static final LoggerWrapper    _logger=WrapperFactoryManager.getLogger(MainFrame.class);
    /*
     * @see net.community.apps.common.BaseMainFrame#getLogger()
     */
    @Override
    protected LoggerWrapper getLogger ()
    {
        return _logger;
    }
    /*
     * @see net.community.apps.common.MainComponent#getResourcesAnchor()
     */
    @Override
    public ResourcesAnchor getResourcesAnchor ()
    {
        return ResourcesAnchor.getInstance();
    }

    private static final String    REFRESH_CMD="refresh", STOP_CMD="stop";
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
        lm.put(EXIT_CMD, getExitActionListener());
        lm.put(ABOUT_CMD, getShowManifestActionListener());
        lm.put(REFRESH_CMD, new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    if (e != null)
                        refresh();
                }
            });
        lm.put(STOP_CMD, new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    if (e != null)
                        stop();
                }
            });
        return lm;
    }

    private Map<String,JarComparisonPane>    _diffPanes;
    private JarComparisonPane setPanePath (final String ownerId, final String filePath)
    {
        final JarComparisonPane        pane=
            ((_diffPanes == null) || (ownerId == null) || (ownerId.length() <= 0)) ? null : _diffPanes.get(ownerId);
        if (pane != null)
            pane.setText(filePath);

        updateRunButtonState();
        return pane;
    }

    static final String    LEFT_JAR_OWNER_ID=Boolean.TRUE.toString(), RIGHT_JAR_OWNER_ID=Boolean.FALSE.toString();
    JarComparisonPane setLeftJarPath (final String filePath)
    {
        return setPanePath(LEFT_JAR_OWNER_ID, filePath);
    }

    JarComparisonPane setRightJarPath (final String filePath)
    {
        return setPanePath(RIGHT_JAR_OWNER_ID, filePath);
    }

    private JSplitPane    _splitter;
    protected void resizeSplitter ()
    {
        if (_splitter != null)
            _splitter.setDividerLocation(0.5);
    }

    private Component createComparisonPane (
            final JarEntriesTableModel leftModel, final JarEntriesTableModel rightModel)
    {
        final Dimension                d=getSize();
        final int                    maxWidth=(int) (d.getWidth() / 2);
        final JarComparisonPane[]    cmpPanes={
                new JarComparisonPane(LEFT_JAR_OWNER_ID, leftModel, maxWidth),
                new JarComparisonPane(RIGHT_JAR_OWNER_ID, rightModel, maxWidth)
            };

        _diffPanes = new TreeMap<String,JarComparisonPane>();
        for (final JarComparisonPane pane : cmpPanes)
        {
            final String    paneName=pane.getName();
            pane.addFileSelectionAction(new ActionListener() {
                    /*
                     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                     */
                    @SuppressWarnings("synthetic-access")
                    @Override
                    public void actionPerformed (ActionEvent e)
                    {
                        loadFile(paneName, getLoadDialogElement());
                    }
                });
            _diffPanes.put(paneName, pane);
        }

        final JScrollPane    leftScroll=cmpPanes[0].getScroller(),
                            rightScroll=cmpPanes[1].getScroller();
        final JScrollBar    leftBar=leftScroll.getHorizontalScrollBar(),
                            rightBar=rightScroll.getHorizontalScrollBar();
        leftBar.setModel(rightBar.getModel());    // make both scrollers work in unison

        if (_splitter == null)
        {
            _splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cmpPanes[0], cmpPanes[1]);
            _splitter.setDividerLocation(0.5);
        }

        return _splitter;
    }

    private boolean haveComparisonDetails ()
    {
        final Collection<? extends JarComparisonPane>    panes=
            (_diffPanes == null) ? null : _diffPanes.values();
        if ((panes == null) || panes.isEmpty())
            return false;

        for (final JarComparisonPane pane : panes)
        {
            final String    filePath=pane.getText();
            if ((filePath == null) || (filePath.length() <= 0))
                return false;

            final File    f=new File(filePath);
            if (f.exists() && f.isFile() && f.canRead())
                continue;

            return false;
        }

        return true;
    }

    void update (Collection<? extends JarEntriesMatchRow> rows)
    {
        final int    numRows=(rows == null) ? 0 : rows.size();
        if (numRows <= 0)
            return;

        for (final JarEntriesMatchRow row : rows)
        {
            final String                ownerId=(row == null) ? null : row.getOwnerId();
            final JarComparisonPane        pane=
                ((_diffPanes == null) || (ownerId == null) || (ownerId.length() <= 0)) ? null : _diffPanes.get(ownerId);
            final JarEntriesTableModel    model=(pane == null) ? null : pane.getModel();
            final int                    mIndex=(model == null) ? (-1) : model.indexOf(row);
            if (mIndex < 0)
                continue;

            // TODO fire a "row-updated" event + add renderers
        }
    }

    private JarPaneComparator    _comparator;
    void doneComparing (JarPaneComparator comparator)
    {
        if (_comparator != comparator)
        {
            if (_comparator != null)
                throw new IllegalStateException("Mismatched comparator instances");
        }
        else
            _comparator = null;
        updateRunningState(false);
    }

    private void compare ()
    {
        final Collection<? extends JarComparisonPane>    panes=
            (_diffPanes == null) ? null : _diffPanes.values();
        _comparator = new JarPaneComparator(this, panes);
        updateRunningState(true);
        _comparator.execute();
    }

    public boolean isComparisonAllowed ()
    {
        if (_comparator != null)
            return false;

        return haveComparisonDetails();
    }

    void populate (Collection<? extends JarEntriesMatchRow> rows)
    {
        final int    numRows=(rows == null) ? 0 : rows.size();
        if (numRows <= 0)
            return;

        for (final JarEntriesMatchRow row : rows)
        {
            final String                ownerId=(row == null) ? null : row.getOwnerId();
            final JarComparisonPane        pane=
                ((_diffPanes == null) || (ownerId == null) || (ownerId.length() <= 0)) ? null : _diffPanes.get(ownerId);
            final JarEntriesTableModel    model=(pane == null) ? null : pane.getModel();
            if (model == null)
                continue;
            if (!model.add(row))
                continue;    // debug breakpoint
        }
    }

    private JarPanePopulator    _populator;
    void donePopulating (JarPanePopulator populator)
    {
        if (_populator != populator)
        {
            if (_populator != null)
                throw new IllegalStateException("Mismatched populator instances");
        }
        else
            _populator = null;

        if ((populator != null) && (!populator.isCancelled()) && isComparisonAllowed())
            compare();
        else
            updateRunningState(false);
    }

    public boolean isPopulateAllowed ()
    {
        if (_populator != null)
            return false;

        return haveComparisonDetails();
    }

    private void populate ()
    {
        final Collection<? extends JarComparisonPane>    panes=
            (_diffPanes == null) ? null : _diffPanes.values();
        _populator = new JarPanePopulator(this, panes);
        updateRunningState(true);
        _populator.execute();
    }

    protected void refresh ()
    {
        if (isPopulateAllowed() && isComparisonAllowed())
            populate();
    }

    protected void stop ()
    {
        if (_populator != null)
            _populator.cancel(false);
        if (_comparator != null)
            _comparator.cancel(false);
    }

    private JCheckBox    _showDiffs, _showIdentical, _checkContents;
    public boolean isCheckContents ()
    {
        return (_checkContents != null) && _checkContents.isSelected();
    }

    public void setCheckContents (boolean enabled)
    {
        if (_checkContents != null)
            _checkContents.setSelected(enabled);
    }

    private JCheckBox createChoiceElement (final JCheckBox prevValue, final String name, final Element elem)
    {
        if (prevValue != null)
            throw new IllegalStateException("createChoiceElement(" + name + ") already processed");

        try
        {
            return new BaseCheckBox(elem);
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e, true);
        }
    }

    private JarEntriesTableModel    _leftModel, _rightModel;
    /*
     * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void loadFile (File f, String cmd, Element dlgElement)
    {
        final String    filePath=(f == null) ? null : f.getAbsolutePath();
        if ((filePath == null) || (filePath.length() <= 0))
            return;

        final JarComparisonPane    cmpPane=
            (_diffPanes == null) ? null : _diffPanes.get(cmd);
        if (cmpPane == null)
            return;
        cmpPane.setText(filePath);
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutSection(java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void layoutSection (String name, Element elem) throws RuntimeException
    {
        if ("jar-entries-table".equalsIgnoreCase(name))
        {
            if ((_leftModel != null) || (_rightModel != null))
                throw new IllegalStateException("layoutSection(" + name + ") already processed");

            try
            {
                _leftModel = new JarEntriesTableModel().fromXml(elem);
                _rightModel = new JarEntriesTableModel().fromXml(elem);
            }
            catch(Exception e)
            {
                throw ExceptionUtil.toRuntimeException(e, true);
            }
        }
        else if ("show-diffs-choice".equalsIgnoreCase(name))
        {
            _showDiffs = createChoiceElement(_showDiffs, name, elem);
        }
        else if ("show-ident-choice".equalsIgnoreCase(name))
        {
            _showIdentical = createChoiceElement(_showIdentical, name, elem);
        }
        else if ("check-data-choice".equalsIgnoreCase(name))
        {
            _checkContents = createChoiceElement(_checkContents, name, elem);
        }
        else
            super.layoutSection(name, elem);
    }

    private AbstractButton    _runBtn, _stopBtn, _stopMenuItem, _runMenuItem;
    protected void updateRunButtonState ()
    {
        AttrUtils.setComponentEnabledState(isPopulateAllowed() && isComparisonAllowed(), _runBtn, _runMenuItem);
    }

    private void updateRunningState (boolean running)
    {
        AttrUtils.setComponentEnabledState(!running, _runBtn, _runMenuItem, _showDiffs, _showIdentical, _checkContents);
        AttrUtils.setComponentEnabledState(!running, (_diffPanes == null) ? null : _diffPanes.values());
        AttrUtils.setComponentEnabledState(running, _stopBtn, _stopMenuItem);
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#setMainMenuItemsActionHandlers(net.community.chest.swing.component.menu.MenuItemExplorer)
     */
    @Override
    protected Map<String,JMenuItem> setMainMenuItemsActionHandlers (MenuItemExplorer ie)
    {
        final Map<String,JMenuItem>    im=super.setMainMenuItemsActionHandlers(ie);
        _stopMenuItem = (null == im) ? null : im.get(STOP_CMD);
        _runMenuItem = (null == im) ? null : im.get(REFRESH_CMD);
        return im;
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final Container    ctPane=getContentPane();
        final JPanel    northPanel=new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        try
        {
            final JToolBar                                b=getMainToolBar();
            final Map<String,? extends AbstractButton>    hm=setToolBarHandlers(b);
            if ((hm != null) && (hm.size() > 0))
            {
                _runBtn = hm.get(REFRESH_CMD);
                _stopBtn = hm.get(STOP_CMD);
            }

            northPanel.add(b);
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }

        {
            final Container        optsPanel=new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            AWTUtils.addComponents(optsPanel, _showDiffs, _showIdentical, _checkContents);
            northPanel.add(optsPanel);
        }
        ctPane.add(northPanel, BorderLayout.NORTH);

        {
            final Component    centerPane=createComparisonPane(_leftModel, _rightModel);
            if (centerPane != null)
                ctPane.add(centerPane, BorderLayout.CENTER);
        }

        addComponentListener(new ComponentAdapter() {
                /*
                 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
                 */
                @Override
                public void componentResized (ComponentEvent e)
                {
                    resizeSplitter();
                }
            });
    }

    MainFrame () throws Exception
    {
        super();
    }
}

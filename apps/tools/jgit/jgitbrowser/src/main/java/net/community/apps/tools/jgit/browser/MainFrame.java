/*
 *
 */
package net.community.apps.tools.jgit.browser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.community.apps.common.BaseMainFrame;
import net.community.apps.tools.jgit.browser.resources.ResourcesAnchor;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.dom.DOMUtils;
import net.community.chest.git.lib.GitlibUtils;
import net.community.chest.git.lib.ref.ByPathComponentsComparator;
import net.community.chest.git.lib.ref.RefUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.text.BaseTextField;
import net.community.chest.swing.component.tree.BaseDefaultTreeModel;
import net.community.chest.swing.component.tree.BaseTree;
import net.community.chest.swing.component.tree.DefaultTreeScroll;
import net.community.chest.swing.component.tree.SelectionModelType;
import net.community.chest.swing.component.tree.TreeUtil;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.helpers.panel.PresetBorderLayoutPanel;
import net.community.chest.ui.helpers.tree.TypedTreeNode;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.Tree;
import org.eclipse.jgit.lib.TreeEntry;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 16, 2011 9:03:15 AM
 */
public final class MainFrame extends BaseMainFrame<ResourcesAnchor> {
    /**
     *
     */
    private static final long serialVersionUID = 5246484499444948402L;
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

    private static final String    REFRESH_CMD="refresh", CLOSE_CMD="close", PULL_CMD="pull", FETCH_CMD="fetch";
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
        lm.put(LOAD_CMD, getLoadFileListener());
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
        lm.put(CLOSE_CMD, new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    if (e != null)
                        closeFile();
                }
            });
        lm.put(PULL_CMD, new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    if (e != null)
                        updateFromRemote(true);
                }
            });
        lm.put(FETCH_CMD, new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    if (e != null)
                        updateFromRemote(false);
                }
            });

        setActionListenersMap(lm);
        return lm;
    }

    private Repository    _repo;
    public final Repository getRepository ()
    {
        return _repo;
    }

    public final boolean isOpen ()
    {
        return (getRepository() != null);
    }
    // returns closed instance - null if no currently active instance
    public Repository closeFile ()
    {
        final Repository    instance=_repo;
        if (instance != null)
        {
            _repo.close();
            _repo = null;
        }

        return instance;
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void loadFile (File f, String cmd, Element dlgElement)
    {
        loadFile(f, true);
    }

    void loadFile (File f, boolean autoSelectHead)
    {
        if (null == f)
            return;

        if (!GitlibUtils.isGitControlledFolder(f))
        {
            JOptionPane.showMessageDialog(this, "Selected folder does not appear to be GIT controlled", "Not a GIT repository", JOptionPane.ERROR_MESSAGE);
            return;
        }

        closeFile();
        try
        {
            _repo = populateReferencesTree(new Repository(null, f));
            updateStatusBar(f.getAbsolutePath());

            if (autoSelectHead)
                setSelectedReference(_repo.getRef(Constants.HEAD), true);
        }
        catch(Exception e)
        {
            BaseOptionPane.showMessageDialog(this, e);
        }
    }

    private BaseTree    _refsTree;
    private BaseTree createReferencesTree ()
    {
        if (_refsTree != null)
            return _refsTree;

        _refsTree = new BaseTree((TreeModel) null);
        _refsTree.setRootVisible(false);
        _refsTree.setShowsRootHandles(true);
        SelectionModelType.SINGLE.setSelectionMode(_refsTree);

        _refsTree.addTreeSelectionListener(new TreeSelectionListener() {
                /*
                 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
                 */
                @Override
                public void valueChanged (final TreeSelectionEvent e)
                {
                    final TreePath    selPath=(null == e) /* should not happen */ ? null : e.getNewLeadSelectionPath();
                    final Object    selNode=(null == selPath) ? null : selPath.getLastPathComponent();
                    if (!(selNode instanceof RefNode))
                        return;
                    setSelectedReference(((RefNode) selNode).getRef(), false);
                }
            });
        if (_nodePopupMenu != null)
        {
            try
            {
                _refsTree.addMouseListener(new RefsTreeMouseAdapter(this, _refsTree, _nodePopupMenu));
            }
            catch(Exception e)
            {
                getLogger().error("Failed (" + e.getClass().getName() + ") to add refs mouse adapter: " + e.getMessage(), e);
            }
        }
        setDropTarget(new DropTarget(_refsTree, this));

        return _refsTree;
    }

    Ref getSelectedReference ()
    {
        final TreePath    selPath=(_refsTree == null) ? null : _refsTree.getSelectionPath();
        final Object    selNode=(null == selPath) ? null : selPath.getLastPathComponent();
        return (selNode instanceof RefNode) ? ((RefNode) selNode).getRef() : null;
    }

    private Repository populateReferencesTree (final Repository repo)
    {
        final Map<String,Ref>            refsMap=(repo == null) ? null : repo.getAllRefs();
        final List<Ref>                    refsList=
            ((refsMap == null) || refsMap.isEmpty()) ? null : new ArrayList<Ref>(refsMap.values());
        final int                        numRefs=(refsList == null) ? 0 : refsList.size();
        final DefaultMutableTreeNode    root=new DefaultMutableTreeNode();
        if (numRefs > 0)
        {
            Collections.sort(refsList, ByPathComponentsComparator.ASCENDING);

            final Map<String,DefaultMutableTreeNode>    nodesMap=new TreeMap<String,DefaultMutableTreeNode>();
            for (final Ref ref : refsList)
                attachRefNode(root, nodesMap, ref);
        }

        final BaseDefaultTreeModel    model=new BaseDefaultTreeModel(root, true);
        _refsTree.setModel(model);
        _refsTree.setCellRenderer(RefNode.RENDERER);
        TreeUtil.setNodesExpansionState(_refsTree, true);
        return repo;
    }

    private RefNode attachRefNode (final DefaultMutableTreeNode root, final Map<String,DefaultMutableTreeNode> nodesMap, final Ref ref)
    {
        final List<String>    comps=
            (root == null) ? null : RefUtils.getRefPathComponents(ref);
        final int            numComps=(comps == null) ? 0 : comps.size();
        if (numComps <= 0)
            return null;

        DefaultMutableTreeNode     parent=root;
        final StringBuilder        sb=new StringBuilder(numComps * 32);
        for (int    cIndex=0; cIndex < (numComps - 1); cIndex++)
        {
            final String cValue=comps.get(cIndex);
            if ((cValue == null) || (cValue.length() <= 0))
                continue;

            if (sb.length() > 0)
                sb.append(GitlibUtils.GITPATH_SEPCHAR);
            sb.append(cValue);

            final String            curPath=sb.toString();
            DefaultMutableTreeNode    mapValue=nodesMap.get(curPath);
            if (mapValue == null)
            {
                mapValue = new DefaultMutableTreeNode(cValue, true);
                parent.add(mapValue);
                nodesMap.put(curPath, mapValue);
            }

            parent = mapValue;
        }

        final RefNode    refNode=new RefNode(ref);
        parent.add(refNode);
        return refNode;
    }

    private static final String REFS_TITLE="References";
    Ref setSelectedReference (final Ref ref, final boolean refreshModel)
    {
        setTreePanelStatus(REFS_TITLE, (ref == null) ? null : ref.getName());

        if ((ref == null) || RefUtils.isTagReference(ref))
            return ref;

        try
        {
            final Repository    repo=getRepository();
            final ObjectId        id=ref.getObjectId();
            setCurrentTree(((id == null) || (repo == null)) ? null : repo.mapTree(id));
        }
        catch(Exception e)
        {
            BaseOptionPane.showMessageDialog(this, e);
        }

        if (refreshModel)
        {
            final DefaultTreeModel    model=(DefaultTreeModel) _refsTree.getModel();
            final RefNode            node=TypedTreeNode.findNode(model, RefNode.class, ref, null);
            final TreePath            path=TreeUtil.setSelectedNode(_refsTree, node);

            if (path == null)
                return ref;    // debug breakpoint
        }

        return ref;
    }

    Ref setSelectedReference (final String name, final boolean refreshModel)
    {
        final Repository    repo=
            ((name == null) || (name.length() <= 0)) ? null : getRepository();
        try
        {
            return (repo == null) ? null : setSelectedReference(repo.getRef(name), refreshModel);
        }
        catch(Exception e)
        {
            BaseOptionPane.showMessageDialog(this, e);
            return null;
        }
    }

    private BaseTree    _filesTree;
    private BaseTree createFilesTree ()
    {
        if (_filesTree != null)
            return _filesTree;

        _filesTree = new BaseTree((TreeModel) null);
        _filesTree.setRootVisible(false);
        _filesTree.setShowsRootHandles(true);
        SelectionModelType.SINGLE.setSelectionMode(_filesTree);

        _filesTree.addTreeSelectionListener(new TreeSelectionListener() {
                /*
                 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
                 */
                @Override
                public void valueChanged (final TreeSelectionEvent e)
                {
                    final TreePath    selPath=(null == e) /* should not happen */ ? null : e.getNewLeadSelectionPath();
                    final Object    selNode=(null == selPath) ? null : selPath.getLastPathComponent();
                    if (!(selNode instanceof TreeEntryNode))
                        return;

                    handleSelectedTreeEntry(((TreeEntryNode) selNode).getTreeEntry());
                }
            });
        _filesTree.addTreeWillExpandListener(new TreeWillExpandListener() {
                /*
                 * @see javax.swing.event.TreeWillExpandListener#treeWillExpand(javax.swing.event.TreeExpansionEvent)
                 */
                @Override
                public void treeWillExpand (final TreeExpansionEvent e) throws ExpandVetoException
                {
                    final TreePath        path=(e == null) ? null : e.getPath();
                    final Object        node=(null == path) ? null : path.getLastPathComponent();
                    expandTreeEntryNode((node instanceof DefaultMutableTreeNode) ? (DefaultMutableTreeNode) node : null);
                }
                /*
                 * @see javax.swing.event.TreeWillExpandListener#treeWillCollapse(javax.swing.event.TreeExpansionEvent)
                 */
                @Override
                public void treeWillCollapse (TreeExpansionEvent e) throws ExpandVetoException
                {
                    // ignored
                }
            });
        try
        {
            _filesTree.addMouseListener(new FilesTreeMouseAdapter(this, _filesTree, _nodePopupMenu));
        }
        catch(Exception e)
        {
            getLogger().error("Failed (" + e.getClass().getName() + ") to add files mouse adapter: " + e.getMessage(), e);
        }

        return _filesTree;
    }

    private static final String    ENTRIES_TITLE="Entries";
    protected void handleSelectedTreeEntry (final TreeEntry entry)
    {
        final File    location=GitlibUtils.getTreeEntryLocation(entry);
        setTreePanelStatus(ENTRIES_TITLE, (location == null) ? null : location.getAbsolutePath());
    }

    void expandTreeEntryNode (final DefaultMutableTreeNode node)
    {
        try
        {
            final TreeEntry        entry=(node instanceof TreeEntryNode) ? ((TreeEntryNode) node).getTreeEntry() : null;
            final TreeEntry[]    members=(entry instanceof Tree) ? ((Tree) entry).members() : null;
            final int            numChildren=(node == null) ? 0 : node.getChildCount(),
                                numMembers=(members == null) ? 0 : members.length;
            if (numChildren == numMembers)
                return;

            expandTreeEntryNode(node, members);
        }
        catch(Exception e)
        {
            getLogger().error("lazyExpandTreeEntryNode(" + node + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    private void expandTreeEntryNode (final DefaultMutableTreeNode parent, final TreeEntry ... members)
    {
        if ((members == null) || (members.length <= 0))
            return;

        for (final TreeEntry m : members)
        {
            final TreeEntryNode    mNode=(m == null) ? null : new TreeEntryNode(m);
            if (mNode == null)
                continue;
            parent.add(mNode);
        }
    }

    void setCurrentTree (final Tree tree)
    {
        if (tree == null)
            return;

        final DefaultMutableTreeNode    root=new DefaultMutableTreeNode();
        try
        {
            expandTreeEntryNode(root, tree.members());
        }
        catch(Exception e)
        {
            BaseOptionPane.showMessageDialog(this, e);
        }

        final BaseDefaultTreeModel    model=new BaseDefaultTreeModel(root, true);
        _filesTree.setModel(model);
        _filesTree.setCellRenderer(TreeEntryNode.RENDERER);
    }

    private JLabel    _statusBar    /* =null */;
    public void updateStatusBar (final String text)
    {
        if (_statusBar != null)
            _statusBar.setText((null == text) ? "" : text);
    }

    protected Ref refresh ()
    {
        final Repository    repo=getRepository();
        final File            workDir=(repo == null) ? null : repo.getWorkDir();
        final Ref            ref=getSelectedReference();
        final String        refName=(ref == null) ? null : ref.getName();
        loadFile(workDir, (refName == null) || (refName.length() <= 0));
        return setSelectedReference(refName, true);
    }

    protected void updateFromRemote (boolean mergeAfterFetch)
    {
        // TODO
    }

    private Element    _nodePopupMenu;
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutSection(java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void layoutSection (String name, Element elem) throws RuntimeException
    {
        if ("node-popup-menu".equalsIgnoreCase(name))
        {
            if (_nodePopupMenu != null)
                throw new IllegalStateException("layoutSection(" + name + ") re-specified: " + DOMUtils.toString(elem));
            _nodePopupMenu = elem;
        }
        else
            super.layoutSection(name, elem);
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final Container    ctPane=getContentPane();
        try
        {
            final JToolBar    b=getMainToolBar();
            /* final Map<String,? extends AbstractButton>    hm= */ setToolBarHandlers(b);
            ctPane.add(b, BorderLayout.NORTH);
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }

        {
            final Component    refsTree=createTreeComponent(createReferencesTree(), REFS_TITLE),
                            filesTree=createTreeComponent(createFilesTree(), ENTRIES_TITLE);
            final Component    center;
            if (refsTree == null)
                center = filesTree;
            else if (filesTree == null)
                center = refsTree;
            else
            {
                final JSplitPane    divider=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, refsTree, filesTree);
                divider.setDividerLocation(0.3);
                center = divider;
            }

            if (center != null)
                ctPane.add(center, BorderLayout.CENTER);
        }

        if (_statusBar == null)
        {
            _statusBar = new JLabel("Ready");
            ctPane.add(_statusBar, BorderLayout.SOUTH);
        }
    }

    private Map<String,Textable>    _treeStatusMap;
    private Textable setTreePanelStatus (final String title, final String text)
    {
        final Textable    c=(_treeStatusMap == null) ? null : _treeStatusMap.get(title);
        if (c == null)
            return null;

        c.setText((text == null) ? "" : text);
        return c;
    }

    private Component createTreeComponent (final JTree tree, final String title)
    {
        final JComponent    comp=(tree == null) ? null : new DefaultTreeScroll(tree);
        if ((comp == null) || (title == null) || (title.length() <= 0))
            return comp;

        comp.setBorder(BorderFactory.createTitledBorder(title));

        final Container    treePanel=new PresetBorderLayoutPanel(5, 5);
        treePanel.add(comp, BorderLayout.CENTER);

        final BaseTextField    statusLabel=new BaseTextField("");
        statusLabel.setEditable(false);

        if (_treeStatusMap == null)
            _treeStatusMap = new TreeMap<String,Textable>(String.CASE_INSENSITIVE_ORDER);
        _treeStatusMap.put(title, statusLabel);
        treePanel.add(statusLabel, BorderLayout.SOUTH);

        return treePanel;
    }

    MainFrame () throws Exception
    {
        super();
    }

}

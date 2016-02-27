package net.community.apps.apache.ant.antrunner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.community.apps.apache.ant.antrunner.resources.ResourcesAnchor;
import net.community.apps.common.BaseMainFrame;
import net.community.chest.apache.ant.LogLevels;
import net.community.chest.apache.ant.build.BuildEventInfo;
import net.community.chest.apache.ant.build.BuildEventTypeEnum;
import net.community.chest.apache.ant.build.BuildEventsHandler;
import net.community.chest.apache.ant.helpers.BaseExecutableElement;
import net.community.chest.apache.ant.helpers.SkeletonBuildProject;
import net.community.chest.apache.ant.helpers.SkeletonBuildTarget;
import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.swing.component.menu.MenuExplorer;
import net.community.chest.swing.component.menu.MenuItemExplorer;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.component.tree.BaseDefaultTreeModel;
import net.community.chest.swing.component.tree.BaseTree;
import net.community.chest.swing.component.tree.DefaultTreeScroll;
import net.community.chest.swing.component.tree.TreeUtil;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.components.input.panel.img.BgImagePanel;
import net.community.chest.ui.components.logging.LogMessagesArea;
import net.community.chest.ui.helpers.tree.TypedTreeNode;
import net.community.chest.util.compare.AbstractComparator;
import net.community.chest.util.logging.LogLevelWrapper;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;
import net.community.chest.util.map.MapEntryImpl;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Element;

final class MainFrame extends BaseMainFrame<ResourcesAnchor> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7602417407480201811L;
	private static final LoggerWrapper	_logger=WrapperFactoryManager.getLogger(MainFrame.class);
	/*
	 * @see net.community.apps.common.BaseMainFrame#getLogger()
	 */
	@Override
	protected LoggerWrapper getLogger ()
	{
		return _logger;
	}
	/**
	 * Minimum priority of messages logged
	 */
	private LogLevels	_priority=LogLevels.INFO;
	public LogLevels getDisplayedMsgPriority ()
    {
        return _priority;
    }

	private static final Collection<Map.Entry<LogLevels,JMenuItem>> getLogPriorityItems (final MenuItemExplorer ie)
	{
		final Map<String,? extends JMenuItem>	itemsMap=(null == ie) ? null : ie.getItemsMap();
		if ((null == itemsMap) || (itemsMap.size() <= 0))
			return null;

		Collection<Map.Entry<LogLevels,JMenuItem>>	ll=null;
		for (final LogLevels l : LogLevels.VALUES)
		{
			final JMenuItem	i=itemsMap.get(l.toString());
			if (null == i)
				continue;

			if (null == ll)
				ll = new LinkedList<Map.Entry<LogLevels,JMenuItem>>();
			ll.add(new MapEntryImpl<LogLevels,JMenuItem>(l,i));
		}

		return ll;
	}

	private Collection<? extends Map.Entry<LogLevels,? extends JMenuItem>>	_logPriItemsList	/* =null */;
	public void setDisplayedMsgPriority (final LogLevels l)
	{
		if ((l != null) && (l.getLevel()  >= Project.MSG_INFO))
		{
			_priority = l;

			if ((_logPriItemsList != null) && (_logPriItemsList.size() > 0))
			{
				for (final Map.Entry<LogLevels,? extends JMenuItem> le : _logPriItemsList)
				{
					final LogLevels	ll=(null == le) ? null : le.getKey();
					final JMenuItem	i=(null == le) ? null : le.getValue();
					if (i != null)
						i.setSelected(l.equals(ll));
				}
			}
		}
	}

	private BuildRunner	_runner	/* =null */;
	protected void signalBuildAborted ()
	{
		if (_runner != null)
		{
			if (_runner.isCancelled())
				getLogger().warn("signalBuildAborted() already canceled");

			_runner.cancel(false);
			getLogger().info("signalBuildAborted() signalled");
		}
		else
			getLogger().warn("signalBuildAborted() no runner");
	}

	void signalBuildDone (BuildRunner r)
	{
		if (r != null)
		{
			if (_runner != r)
				getLogger().warn("signalBuildDone() mismatched instances");
			_runner = null;
		}

   		setRunningMode(false);
	}

	public static final BuildEventsHandler getBuildEventsHandler ()
	{
		final MainFrame	f=MainFrame.class.cast(getMainFrameInstance());
		if (null == f)
			return null;
		return f._runner;
	}

	private LogMessagesArea	_logsArea;
	protected void clearLogMessagesArea ()
	{
		if (_logsArea != null)
			_logsArea.setText("");
	}

	public static final LogLevelWrapper antLevel2LogLevel (final LogLevels ll)
	{
		if (null == ll)
			return null;

		switch(ll)
		{
			case DEBUG		: return LogLevelWrapper.DEBUG;
			case ERROR		: return LogLevelWrapper.ERROR;
			case INFO		: return LogLevelWrapper.INFO;
			case VERBOSE	: return LogLevelWrapper.VERBOSE;
			case WARN		: return LogLevelWrapper.WARNING;
			default			:
				return null;
		}
	}

	void logEvent (final BuildEventInfo eventInfo) throws BadLocationException
	{
		if (null == _logsArea)	// should not happen
			return;

		final BuildEvent	event=(null == eventInfo) ? null : eventInfo.getEvent();
        final Throwable		exc=(null == event) ? null : event.getException();
        if (exc != null)
        {
        	final String	msg=exc.getClass().getName() + ": " + exc.getMessage();
        	_logsArea.log(LogLevelWrapper.ERROR, msg);
        	_logger.error(msg, exc);
            return;
        }

        BuildEventTypeEnum	type=(null == eventInfo) ? null : eventInfo.getType();
        if (null == type)
        	type = BuildEventTypeEnum.MSG_LOGGED;
        switch(type)
        {
            case BUILD_STARTED:
            	_logsArea.log(LogLevelWrapper.INFO, "Starting build....");
                break;

            case BUILD_FINISHED:
            	_logsArea.log(LogLevelWrapper.INFO, "Build ended.");
                break;

            case  MSG_LOGGED :
	            {
	                // skip if no event or message not has enough priority
	                final String	msg=(null == event) ? null : event.getMessage();
	                final int		pri=(null == event) ? Integer.MAX_VALUE : event.getPriority();
	                final LogLevels	l=getDisplayedMsgPriority();
	                if ((null == msg) || (msg.length() <= 0) || (null == l) || (pri > l.getLevel()))
	                   break;
	
	                final Task				t=(null == event) ? null : event.getTask();
	                final String			tName=(null == t) ? null : t.getTaskName(),
	                						logMsg="[" + tName + "]\t" + msg;
	                final LogLevels			ll=LogLevels.fromLevel(pri);
	                final LogLevelWrapper	lw=antLevel2LogLevel(ll);
	                _logsArea.log(lw, logMsg);
	                _logger.log(lw, msg);
	            }
	            break;

            default:    // not interested in other events
        }
	}
	/**
	 * <P>Copyright 2007 as per GPLv2</P>
	 *
	 * <P>Serves as base class for the various tree nodes</P>
	 * 
	 * @author Lyor G.
	 * @since Aug 8, 2007 9:23:47 AM
	 */
	private static class BaseExecutableNode<V extends BaseExecutableElement> extends TypedTreeNode<V> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4671176139155265241L;

		protected BaseExecutableNode (Class<V> elemClass, V elem)
		{
			super(elemClass, elem, (null == elem) ? null : elem.getName());
		}

		@SuppressWarnings("unchecked")
		protected BaseExecutableNode (V elem)
		{
			this((null == elem) ? null : (Class<V>) elem.getClass(), elem);
		}

		public final /* no cheating */ V getExecutableElement ()
		{
			return getUserObject();
		}
	}
	/**
	 * <P>Copyright 2007 as per GPLv2</P>
	 *
	 * <P>Represents the project (root) node</P>
	 * 
	 * @author Lyor G.
	 * @since Aug 8, 2007 9:23:20 AM
	 */
	private static final class ProjectRootNode extends BaseExecutableNode<SkeletonBuildProject> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3393574816896334234L;

		public final /* no cheating */ SkeletonBuildProject getProject ()
		{
			return getExecutableElement();
		}

		protected ProjectRootNode (final SkeletonBuildProject project)
		{
			super(project);
		}
	}
	/**
	 * <P>Copyright 2007 as per GPLv2</P>
	 *
	 * <P>Represents a target as a tree node</P>
	 * 
	 * @author Lyor G.
	 * @since Aug 8, 2007 9:03:15 AM
	 */
	private static final class TargetNode extends BaseExecutableNode<SkeletonBuildTarget> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7521428584064801440L;
		public final /* no cheating */ SkeletonBuildTarget getTarget ()
		{
			return getExecutableElement();
		}

		private final boolean	_default;
		public final /* no cheating */ boolean isDefaultTarget ()
		{
			return _default;
		}

		protected TargetNode (final SkeletonBuildTarget target, final boolean isDefault)
		{
			super(target);

			_default = isDefault;
		}
	}
	/**
	 * <P>Copyright 2007 as per GPLv2</P>
	 *
	 * <P>Used for more fine-grained control over the displayed tree nodes
	 * display properties</P>
	 * 
	 * @author Lyor G.
	 * @since Aug 8, 2007 9:11:57 AM
	 */
	private final class NodeRenderer extends DefaultTreeCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7317099781171654578L;
		protected NodeRenderer ()
		{
			super();
		}
		/*
		 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
		 */
		@Override
		public Component getTreeCellRendererComponent (JTree tree,
				Object value, boolean sel, boolean expanded, boolean leaf,
				int row, boolean isFocused)
		{
			final Component	c=super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, isFocused);
			if (c != this)
				return getLogger().errorObject("getTreeCellRendererComponent(" + value + ") mismatched instances - got a " + ((null == c) ? null : c.getClass().getName()), c);

			if (!(value instanceof BaseExecutableNode<?>))	// should not happen
				return getLogger().errorObject("getTreeCellRendererComponent(" + value + ") unknown value class: " + ((null == value) ? null : value.getClass().getName()), null);

			final BaseExecutableElement	elem=((BaseExecutableNode<?>) value).getExecutableElement();
			if (null == elem)	// should not happen
				return getLogger().errorObject("getTreeCellRendererComponent(" + value + ") no executable element", c);

			setText(elem.getName());

			final String	desc=elem.getDescription();
			if ((desc != null) && (desc.length() > 0))
				setToolTipText(elem.getDescription());

			final Font	elemFont;
			if ((elem instanceof SkeletonBuildProject)
			 || ((value instanceof TargetNode) && ((TargetNode) value).isDefaultTarget()))
				elemFont = getMainFont("default-target-font");
			else
				elemFont = getMainFont(elem.isHiddenComponent() ? "private-targets-font" : "public-targets-font"); 	

			if (elemFont != null)
				setFont(elemFont);

			return c;
		}
	}

	private boolean	_showHiddenTargets	/* =false */;
	public boolean isShowHiddenTargets ()
	{
		return _showHiddenTargets;
	}

	private JMenuItem	_toggleViewMenuItem;
	public void loadProject (final SkeletonBuildProject proj)
	{
		final ProjectRootNode							root=new ProjectRootNode(proj);
		final Collection<? extends SkeletonBuildTarget>	targets=proj.getTargets();
		final boolean									showHidden=isShowHiddenTargets();
		if ((targets != null) && (targets.size() > 0))
		{
			final String	defTarget=proj.getDefaultTarget();
			for (final SkeletonBuildTarget t : targets)
			{
				if (null == t)	// should not happen
					continue;

				final boolean	isDefTgt=(0 == StringUtil.compareDataStrings(defTarget, t.getName(), true));
				if ((!isDefTgt) && (!showHidden) && t.isHiddenComponent())
					continue;

				final TargetNode	n=new TargetNode(t, isDefTgt);
				root.add(n);

				if (getLogger().isDebugEnabled())
					getLogger().debug("loadProject(" + proj.getName() + ")[" + t.getName() + "]");
			}
		}

		final BaseDefaultTreeModel	model=new BaseDefaultTreeModel(root, true);
		_targetsTree.setModel(model);
		_targetsTree.setCellRenderer(new NodeRenderer());
		TreeUtil.setNodesExpansionState(_targetsTree, true);

		if ((_toggleViewMenuItem != null) && (_toggleViewMenuItem.isSelected() != showHidden))
			_toggleViewMenuItem.setSelected(showHidden);
	}

	public void setShowHiddenTargets (final boolean v, final boolean refreshView)
	{
		if (v != _showHiddenTargets)
			_showHiddenTargets = v;

		if (refreshView)
			refreshTargets();
	}

	public void setShowHiddenTargets (final boolean v)
	{
		setShowHiddenTargets(v, true);
	}

	protected void toggleTargetsView ()
	{
		setShowHiddenTargets(!isShowHiddenTargets());
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#getResourcesAnchor()
	 */
	@Override
	public ResourcesAnchor getResourcesAnchor ()
	{
		return ResourcesAnchor.getInstance();
	}

	private File	_filePath	/* =null */;
	public File getFilePath ()
	{
		return _filePath;
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void loadFile (File f, String cmd, Element dlgElement)
	{
		if (null == f)
			return;

		try
		{
			loadProject(new SkeletonBuildProject(f));
			getLogger().info("loadFile(" + f + ") loaded");
			_filePath = f;
		}
		catch(Exception e)
		{
			getLogger().error("loadFile(" + f + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
			BaseOptionPane.showMessageDialog(this, e);
		}
	}

	protected void refreshTargets ()
	{
		loadFile(getFilePath(), LOAD_CMD, null);
	}

	private boolean	_running	/* =false */;
	protected boolean isRunningMode ()
	{
		return _running;
	}

	private JMenuItem		_loadMenuItem, _refreshMenuItem;
	private AbstractButton	_loadBtn, _runBtn, _stopBtn, _refreshBtn;
	protected void updateRunButton (final boolean enable)
	{
		if ((_runBtn != null) && (!isRunningMode()) && (_runBtn.isEnabled() != enable))
			_runBtn.setEnabled(enable);
	}
	/**
	 * The tree representing the project and its targets
	 */
	private BaseTree	_targetsTree;
	protected void setRunningMode (boolean running)
	{
		if (_running != running)
		{
			AttrUtils.setComponentEnabledState(!running,
					_loadMenuItem, _refreshMenuItem,
					_loadBtn, _refreshBtn,
					_targetsTree );

			final Cursor	c=running
					? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
					: Cursor.getDefaultCursor()
					;
			if (c != null)
				setCursor(c);

			if (_stopBtn != null)
			{
				_stopBtn.setEnabled(running);
				_stopBtn.setVisible(running);
			}

			if (_runBtn != null)
			{
				_runBtn.setEnabled(!running);
				_runBtn.setVisible(!running);
			}

			_running = running;
		}
	}

	protected final String getTargetName (final TreeNode selNode)
	{
		if (null == selNode)
			return null;
		else if (selNode instanceof ProjectRootNode)
        {
        	final ProjectRootNode		projNode=(ProjectRootNode) selNode;
        	final SkeletonBuildProject	proj=projNode.getProject();
        	return (null == proj) /* should not happen */ ? null : proj.getDefaultTarget();
        }
        else if (selNode instanceof TargetNode)
        {
        	final TargetNode			tgtNode=(TargetNode) selNode;
        	final SkeletonBuildTarget	tgtBuild=tgtNode.getTarget();
        	return (null == tgtBuild) /* should not happen */ ? null : tgtBuild.getName();
        }
        else
        	return getLogger().errorObject("executeSelectedTarget() unknown selected node class: " + selNode.getClass().getName(), null);
	}

	private void executeSelectedTarget (final String tgtName)
	{
        if ((null == tgtName) || (tgtName.length() <= 0))
        	return;	// should not happen

        if (_runner != null)
        {
        	JOptionPane.showMessageDialog(this, "Stop current build before starting another", "Build in progress", JOptionPane.ERROR_MESSAGE);
        	return;
        }

        clearLogMessagesArea();

        _runner = new BuildRunner(this, tgtName);
   		setRunningMode(true);
        _runner.execute();
	}
	// Note: may be called from the mouse listener, so silently ignore 
	public void executeSelectedTarget ()
	{
        final TreePath 	selPath=
        	(null == _targetsTree) ? null : _targetsTree.getSelectionPath();
        final TreeNode	selNode=
        	(null == selPath) /* OK if nothing chosen */ ? null : (TreeNode) selPath.getLastPathComponent() ;
        executeSelectedTarget(getTargetName(selNode));
	}

	private static final String	RUN_CMD="run", STOP_CMD="stop", REFRESH_CMD="refresh",
								TOGGLE_VIEW_CMD="toggle-targets-view";
	/*
	 * @see net.community.apps.common.BaseMainFrame#getActionListenersMap(boolean)
	 */
	@Override
	protected Map<String,? extends ActionListener> getActionListenersMap (boolean createIfNotExist)
	{
		final Map<String,? extends ActionListener>	org=super.getActionListenersMap(createIfNotExist);
		if (((org != null) && (org.size() > 0)) || (!createIfNotExist))
			return org;

		final Map<String,ActionListener>	lm=new TreeMap<String,ActionListener>(String.CASE_INSENSITIVE_ORDER);
		lm.put(LOAD_CMD, getLoadFileListener());
		lm.put(EXIT_CMD, getExitActionListener());
		lm.put(ABOUT_CMD, getShowManifestActionListener());
		lm.put(TOGGLE_VIEW_CMD, new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent e)
				{
					toggleTargetsView();
				}
			});
		lm.put(RUN_CMD, new ActionListener() {
					/*
					 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
					 */
					@Override
					public void actionPerformed (ActionEvent e)
					{
						if (e != null)
							executeSelectedTarget();
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
							signalBuildAborted();
					}
				});
		lm.put(REFRESH_CMD, new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent e)
				{
					if (e != null)
						refreshTargets();
				}
			});
		lm.put("ant-site", new ActionListener() {
					/*
					 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
					 */
					@Override
					public void actionPerformed (final ActionEvent event)
					{
						final Desktop	d=Desktop.getDesktop();
						try
						{
							d.browse(new URI("http://ant.apache.org/"));
						}
						catch(Exception e)
						{
							getLogger().error("actionPerformed(" + event.getActionCommand() + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
							BaseOptionPane.showMessageDialog(getMainFrame(), e);
						}
					}
				});
		lm.put("clear", new ActionListener() {
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
		lm.put("logPriority", new ActionListener() {
					/*
					 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
					 */
					@Override
					public void actionPerformed (final ActionEvent event)
					{
						final String	cmd=
							(null == event) ? null : event.getActionCommand();
						final LogLevels	l=LogLevels.fromString(cmd);
						setDisplayedMsgPriority(l);
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
		final Map<String,JMenuItem>	im=super.setMainMenuItemsActionHandlers(ie);
		_loadMenuItem = (null == im) ? null : im.get(LOAD_CMD);
		_refreshMenuItem = (null == im) ? null : im.get(REFRESH_CMD);
		_toggleViewMenuItem = (null == im) ? null : im.get(TOGGLE_VIEW_CMD);
		return im;
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#setMainMenuActionHandlers(net.community.chest.swing.component.menu.MenuItemExplorer, net.community.chest.swing.component.menu.MenuExplorer)
	 */
	@Override
	protected void setMainMenuActionHandlers (final MenuItemExplorer ie, final MenuExplorer me)
	{
		super.setMainMenuActionHandlers(ie, me);

		_logPriItemsList = getLogPriorityItems(ie);
	}

	private JLabel	_statusBar	/* =null */;
	public void updateStatusBar (final String text)
	{
		if (_statusBar != null)
			_statusBar.setText((null == text) ? "" : text);
	}

	protected void updateStatusBar (final BaseExecutableElement elem)
	{
		final String	desc=(null == elem) ? null : elem.getDescription();
		if ((null == desc) || (desc.length() <= 0))
			updateStatusBar(elem.getName());
		else
			updateStatusBar(desc);
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutSection(java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void layoutSection (String name, Element elem) throws RuntimeException
	{
		if ("log-msgs-area".equalsIgnoreCase(name))
		{
			if (_logsArea != null)
				throw new IllegalStateException("layoutSection(" + DOMUtils.toString(elem) + ") re-specified log messages area configuration");

			_logsArea = new LogMessagesArea(getMainFont("log-msgs-font"), elem);
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
			final int	nRes=JOptionPane.showConfirmDialog(this, "Abort running build ?", "Build in progress", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (nRes != JOptionPane.YES_OPTION)
				return;
		}

		super.exitApplication();
	}
	// change the divider of the split pane according to the window state
	protected void setResizeWeight (final JSplitPane sp, final int oldState, final int newState)
	{
		if ((sp == null)
		 || ((oldState != Frame.NORMAL) && (oldState != Frame.MAXIMIZED_BOTH))
		 || ((newState != Frame.NORMAL) && (newState != Frame.MAXIMIZED_BOTH))
		 || (oldState == newState))
			return;

		if (Frame.NORMAL == newState)
			sp.setResizeWeight(0.33);
		else
			sp.setResizeWeight(0.15);
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		final Container	ctPane=getContentPane();	
		try
		{
			final JToolBar								b=getMainToolBar();
			final Map<String,? extends AbstractButton>	hm=setToolBarHandlers(b);
			if ((hm != null) && (hm.size() > 0))
			{
				_loadBtn = hm.get(LOAD_CMD);
				_runBtn = hm.get(RUN_CMD);
				_stopBtn = hm.get(STOP_CMD);
				_refreshBtn = hm.get(REFRESH_CMD);
			}

			ctPane.add(b, BorderLayout.NORTH);
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}

		_targetsTree = new BaseTree((TreeModel) null);
		_targetsTree.setRootVisible(true);
		// update status bar with hint whenever a node selection changes
		_targetsTree.addTreeSelectionListener(new TreeSelectionListener() {
				/*
				 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
				 */
				@Override
				public void valueChanged (final TreeSelectionEvent e)
				{
					final TreePath	selPath=(null == e) /* should not happen */ ? null : e.getNewLeadSelectionPath();
	                final Object	selNode=(null == selPath) ? null : selPath.getLastPathComponent();
	                if (selNode instanceof ProjectRootNode)
	                {
	                	updateStatusBar(((ProjectRootNode) selNode).getProject());
	                	updateRunButton(true);
	                }
	                else if (selNode instanceof TargetNode)
	                {
	                	updateStatusBar(((TargetNode) selNode).getTarget());
	                	updateRunButton(true);
	                }
				}
			});
		// interpret double-click on a node as execution command
		_targetsTree.addMouseListener(new MouseAdapter() {
			 	/*
			 	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 	 */
			 	@Override
		        public void mouseClicked (MouseEvent e)
			 	{
			 		// ignore double click if already running
			 		if ((null == e) || (e.getClickCount() <= 1) || isRunningMode())
			 			return;
		             
			 		if (!SwingUtilities.isLeftMouseButton(e))
			 			return;

			 		executeSelectedTarget();
			 	}
			});
		setDropTarget(new DropTarget(_targetsTree, this));

		final Component targetsScroll=new DefaultTreeScroll(_targetsTree);
		targetsScroll.addMouseListener(new MouseAdapter() {
				/*
				 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
				 */
				@Override
				public void mouseClicked (final MouseEvent e)
				{
					// executes the double clicked target
					if ((e != null) && SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() >= 2))
						executeSelectedTarget();
				}
			}
		);

		Container 	logsScroll=null;
		if (_logsArea != null)
		{
			logsScroll = new ScrolledComponent<JTextPane>(JTextPane.class, _logsArea);
			final Image	bgImg=getIconImage();
			if (bgImg != null)
			{
				final Container	bgPanel=new BgImagePanel(bgImg, new BorderLayout(0, 0));
				bgPanel.add(logsScroll, BorderLayout.CENTER);
				bgPanel.setBackground(Color.white);
				logsScroll = bgPanel;
			}
		}

		final JSplitPane	sp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, targetsScroll, logsScroll);
		sp.setResizeWeight(0.33);
		ctPane.add(sp, BorderLayout.CENTER);

		_statusBar = new JLabel("Ready");
		ctPane.add(_statusBar, BorderLayout.SOUTH);

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
				/*
				 * @see java.awt.event.WindowAdapter#windowStateChanged(java.awt.event.WindowEvent)
				 */
				@Override
				public void windowStateChanged (WindowEvent e)
				{
					setResizeWeight(sp, (e == null) ? Integer.MIN_VALUE : e.getOldState(),(e == null) ? Integer.MIN_VALUE : e.getNewState());
				}
 			});
		// change the divider of the split pane according to the window state
		addWindowStateListener(new WindowStateListener() {
				/*
				 * @see java.awt.event.WindowStateListener#windowStateChanged(java.awt.event.WindowEvent)
				 */
				@Override
				public void windowStateChanged (WindowEvent e)
				{
					setResizeWeight(sp, (e == null) ? Integer.MIN_VALUE : e.getOldState(),(e == null) ? Integer.MIN_VALUE : e.getNewState());
				}
			});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	private void processPropertyDefinition (final String dName, final String dValue)
	{
		if (_logger.isDebugEnabled())
			_logger.debug("processPropertyDefinition(" + dName + ")[" + dValue + "]");
	}
	/**
     * @param args initial arguments as received by the (@link #main(String[]))
     * @return actual arguments after filtering out some unusable options
     * and/or extracting the build file name and/or target - may be null/empty
     * @throws IllegalStateException if illegal option(s) encountered
     */
    private Collection<String> processMainArgs (final String... args) throws IllegalStateException
    {
    	final int			numArgs=(null == args) ? 0 : args.length;
    	Collection<String>	effArgs=null;
    	File				buildFile=null;
    	String				tgtName=null;
    	for (int aIndex=0; aIndex < numArgs; aIndex++)
    	{
    		final String	arg=args[aIndex];
    		final int		argLen=(null == arg) ? 0 : arg.length();
    		// ignore options that have no meaning for us
            if ("-help".equals(arg) || "-h".equals(arg)
             || "-version".equals(arg)
             || "-diagnostics".equals(arg))
            {
            	continue;
            }
            else if ("-showhidden".equals(arg))
            {
            	_showHiddenTargets = true;
            }
            else if ("-buildfile".equals(arg) || "-file".equals(arg) || "-f".equals(arg))
            {
            	aIndex++;

            	if (aIndex >= numArgs)
            		throw new IllegalStateException("Missing " + arg + " option value");

            	if (buildFile != null)
            		throw new IllegalStateException(arg + " option value re-specified");

            	final String	filePath=args[aIndex];
            	if ((null == filePath) || (filePath.length() <= 0))
            		throw new IllegalStateException("Missing/empty " + arg + " argument");

            	buildFile = new File(filePath);
            }
            else if ("-quiet".equals(arg) || "-q".equals(arg))
            {
            	setDisplayedMsgPriority(LogLevels.WARN);
            }
            else if ("-verbose".equals(arg) || "-v".equals(arg))
            {
            	setDisplayedMsgPriority(LogLevels.VERBOSE);
            }
            else if ("-debug".equals(arg) || "-d".equals(arg))
            {
            	setDisplayedMsgPriority(LogLevels.DEBUG);
            }
            // the following options are NOT allowed
            else if ("-listener".equals(arg)
            	  || "-logger".equals(arg)
            	  || "-inputhandler".equals(arg)
            	  || "-emacs".equals(arg) || "-e".equals(arg)
            	  || "-projecthelp".equals(arg) || "-p".equals(arg)
            	  || "-find".equals(arg) || "-s".equals(arg))
            {
            	throw new IllegalStateException(arg + " option N/A");
            }
            else if ((argLen > 1) && arg.startsWith("-"))
            {
            	if (null == effArgs)
            		effArgs = new LinkedList<String>();
            	effArgs.add(arg);

            	// handle options that have extra parameter
                if (arg.equals("-logfile") || arg.equals("-l")
                 || arg.startsWith("-propertyfile")
                 || arg.equals("-nice"))
                {
                	aIndex++;

                	if (aIndex >= numArgs)
                		throw new IllegalStateException("Missing extra parameter for " + arg + " option");

                	effArgs.add(args[aIndex]);
                }
                else if ((argLen > 4) && ('D' == arg.charAt(1)))
                {
                	final int	sPos=arg.indexOf('=', 2);
                	if ((sPos > 2) && (sPos < (argLen-1)))
                	{
                		final String	dName=arg.substring(2, sPos),
                						dValue=arg.substring(sPos+1);
                		processPropertyDefinition(dName, dValue);
                	}
                }
            }
            else	// argument does not start with "-" assume it is a target name
            {
            	// target may be LAST argument only
            	if (aIndex < (numArgs-1))
            		throw new IllegalStateException("Target name (" + arg + ") must be LAST argument");

            	if ((tgtName != null) && (tgtName.length() > 0))
            		throw new IllegalStateException(arg + " option value re-specified");
            	if (((tgtName=args[aIndex]) == null) || (tgtName.length() <= 0))
            		throw new IllegalStateException("Missing/empty " + arg + " argument");
            }
    	}

    	m_args = null;	// nullify the arguments to prevent re-execution with same ones

    	if (buildFile != null)
    		loadFile(buildFile, LOAD_CMD, null);
    	if ((tgtName != null) && (tgtName.length() > 0))
    		executeSelectedTarget(tgtName);

    	return effArgs;
    }

    public void setArgsLine (final String args) throws IllegalStateException
    {
        final File    				prevFile=_filePath;
        final Collection<String>	al=
        	((null == args) || (args.length() > 0)) ? null : processMainArgs(args.split("\\s"));
   		m_args = ((null == al) || (al.size() <= 0)) ? null : new ArrayList<String>(al);

    	// check if filename changed - if so then ignore + warning
        if (AbstractComparator.compareComparables(prevFile, _filePath) != 0)
            throw  new IllegalStateException("to load a new file please use the File->Open menu");
        // TODO if target name specified then ???
    }
	/**
	 * @param args original arguments as received by <I>main</I> entry point
	 * @throws Exception if unable to start main frame and application
	 */
	MainFrame (final String ... args) throws Exception
	{
		super(args);
		processMainArgs(args);
	}
}
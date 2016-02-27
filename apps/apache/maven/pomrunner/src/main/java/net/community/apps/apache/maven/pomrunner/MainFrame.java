package net.community.apps.apache.maven.pomrunner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;

import net.community.apps.apache.maven.pomrunner.resources.ResourcesAnchor;
import net.community.apps.common.BaseMainFrame;
import net.community.chest.apache.maven.helpers.BaseTargetDetails;
import net.community.chest.apache.maven.helpers.BuildProject;
import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.dom.DOMUtils;
import net.community.chest.swing.component.menu.MenuItemExplorer;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.components.input.panel.img.BgImagePanel;
import net.community.chest.ui.components.logging.LogMessagesArea;
import net.community.chest.ui.components.panel.LRLabeledTextFieldsPanel;
import net.community.chest.ui.helpers.panel.input.AbstractInputTextPanel;
import net.community.chest.util.logging.LogLevelWrapper;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 8, 2007 10:35:07 AM
 */
final class MainFrame extends BaseMainFrame<ResourcesAnchor> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4562961353767045164L;
	protected static final LoggerWrapper	_logger=WrapperFactoryManager.getLogger(MainFrame.class);
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
	public ResourcesAnchor getResourcesAnchor ()
	{
		return ResourcesAnchor.getInstance();
	}

	private String	_filePath	/* =null */;
	public String getCurrentFilePath ()
	{
		return _filePath;
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void loadFile (final File f, final String cmd, final Element dlgElement)
	{
		File	pomFile=f;
		if ((pomFile != null) && pomFile.exists() && pomFile.isDirectory())
			pomFile = new File(pomFile, BuildProject.DEFAULT_POM_FILE_NAME);

		if ((pomFile != null)
		 && ((!pomFile.exists()) || (!pomFile.isFile())))
		{
			if (LOAD_CMD.equalsIgnoreCase(cmd))
				JOptionPane.showMessageDialog(this, pomFile.getAbsolutePath(), "Bad/Illegal file selected", JOptionPane.WARNING_MESSAGE);
			return;
		}

		final String filePath=(null == pomFile) ? null : pomFile.getAbsolutePath();
		if ((null == filePath) || (filePath.length() <= 0))
			return;

		try
		{
			loadProject(filePath, new BuildProject(filePath));
			getLogger().info("loadFile(" + filePath + ") loaded");
			_filePath = filePath;

			final File	workDir=pomFile.getParentFile();
			setWorkingDirectory(workDir.getAbsolutePath());
		}
		catch(Exception e)
		{
			getLogger().error("loadFile(" + filePath + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
			BaseOptionPane.showMessageDialog(this, e);
		}
	}

	private String	_mavenHome;
	public String getMavenHome ()
	{
		if (_mavenHome == null)
			_mavenHome = System.getenv("M2_HOME");
		return _mavenHome;
	}

	public void setMavenHome (String mavenHome)
	{
		_mavenHome = mavenHome;
	}

	private String	_mavenCommand;
	public String getMavenCommand ()
	{
		if (_mavenCommand == null)
		{
			final String	osType=System.getProperty("os.name", "<unknown>").toLowerCase();
			if ((osType == null) || (osType.length() <= 0))
				return null;

			if (osType.contains("windows"))
				_mavenCommand = "mvn.bat";
			else
				_mavenCommand = "mvn";
		}

		return _mavenCommand;
	}

	public void setMavenCommand (String cmd)
	{
		_mavenCommand = cmd;
	}

	public static final String	RUN_CMD="run", STOP_CMD="stop";
	private JMenuItem		_loadMenuItem, _runMenuItem, _stopMenuItem;
	/*
	 * @see net.community.apps.common.BaseMainFrame#setMainMenuItemsActionHandlers(net.community.chest.swing.component.menu.MenuItemExplorer)
	 */
	@Override
	protected Map<String,JMenuItem> setMainMenuItemsActionHandlers (MenuItemExplorer ie)
	{
		final Map<String,JMenuItem>	im=super.setMainMenuItemsActionHandlers(ie);
		_loadMenuItem = (null == im) ? null : im.get(LOAD_CMD);
		_runMenuItem = (null == im) ? null : im.get(RUN_CMD);
		_stopMenuItem = (null == im) ? null : im.get(STOP_CMD);
		return im;
	}

	private AbstractButton	_loadBtn, _runBtn, _stopBtn;
	protected void updateActionsState (final boolean running)
	{
		AttrUtils.setComponentEnabledState(!running,
				_loadMenuItem, _loadBtn,
				_runMenuItem, _runBtn,
				_workDir, _extraArgs, _targets);
		AttrUtils.setComponentEnabledState(running,
				_stopMenuItem, _stopBtn);
	}

	private PomRunner	_runner;
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

			stopCurrentRun();
		}

		super.exitApplication();
	}

	void done (PomRunner runner)
	{
		if (runner == _runner)
		{
			_runner = null;
			updateActionsState(isRunning());
		}
		else
			getLogger().warn("done() - Unrecognized runner instance");
	}

	void process (Collection<? extends OutputEntry> chunks)
	{
		if ((chunks == null) || chunks.isEmpty() || (_logsArea == null))
			return;

		for (final OutputEntry c : chunks)
		{
			final LogLevelWrapper	level=(c == null) ? null : c.getLevel();
			final String			msg=(c == null) ? null : c.getMessage();
			if ((level == null) || (msg == null) || (msg.length() <= 0))
				continue;
			try
			{
				_logsArea.log(level, msg);
			}
			catch (BadLocationException e)
			{
				getLogger().warn("Failed (" + e.getClass().getSimpleName() + ") to write log message: " + e.getMessage(), e);
			}
		}
	}

	public boolean isRunning ()
	{
		return (_runner != null);
	}

	protected void runLoadedFile ()
	{
		final String	filePath=getCurrentFilePath();
		if (isRunning() || (filePath == null) || (filePath.length() <= 0))
			return;

		_runner = new PomRunner(this);
		updateActionsState(true);
		_runner.execute();
	}

	protected void stopCurrentRun ()
	{
		if (_runner != null)
			_runner.stop();
	}
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
		// TODO disable button(s) if build in progress
		lm.put("load", getLoadFileListener());
		lm.put("exit", getExitActionListener());
		lm.put("about", getShowManifestActionListener());
		lm.put(RUN_CMD, new ActionListener () {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent event)
				{
					runLoadedFile();
				}
			});
		lm.put(STOP_CMD, new ActionListener () {
			/*
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed (ActionEvent event)
			{
				stopCurrentRun();
			}
		});

		setActionListenersMap(lm);
		return lm;
	}

	private JLabel	_statusBar	/* =null */;
	public void updateStatusBar (final String text)
	{
		if (_statusBar != null)
			_statusBar.setText((null == text) ? "" : text);
	}

	private LogMessagesArea	_logsArea;
	protected void clearLogMessagesArea ()
	{
		if (_logsArea != null)
			_logsArea.setText("");
	}

	private LRLabeledTextFieldsPanel	_infoPanel;
	protected void loadProject (final String path, final BuildProject proj)
	{
		final String[]	info={
				BaseTargetDetails.GROUPID_ELEM_NAME,	proj.getGroupId(), 
				BaseTargetDetails.ARTIFACTID_ELEM_NAME,	proj.getArtifactId(),
				BaseTargetDetails.VERSION_ELEM_NAME, proj.getVersion() 
			};
		for (int	i=0; i < info.length; i += 2)
		{
			final String	name=info[i], value=info[i+1];
			final Textable	fld=(_infoPanel == null) ? null : _infoPanel.getTextField(name);
			if (fld == null)
				continue;
			fld.setText((value == null) ? "" : value.trim());
		}

		updateStatusBar(path);
		updateActionsState(isRunning());
	}

	private AbstractInputTextPanel	_targets;
	public String getTargets ()
	{
		return (_targets == null) ? null : _targets.getText();
	}

	public void setTargets (String targets)
	{
		if (_targets != null)
			_targets.setText((targets == null) ? "" : targets);
	}

	private AbstractInputTextPanel	_workDir;
	public String getWorkingDirectory ()
	{
		return (_workDir == null) ? null : _workDir.getText();
	}

	public void setWorkingDirectory (String dir)
	{
		if (_workDir != null)
			_workDir.setText((dir == null) ? "" : dir);
	}

	private AbstractInputTextPanel	_extraArgs;
	public String getExtraArguments ()
	{
		return (_extraArgs == null) ? null : _extraArgs.getText();
	}

	public void setExtraArguments (String args)
	{
		if (_extraArgs != null)
			_extraArgs.setText((args == null) ? "" : args);
	}

	private static final String	WORKDIR_FIELD_NAME="workdir",
								ARGUMENTS_FIELD_NAME="arguments",
								TARGETS_FIELD_NAME="targets";
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutSection(java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void layoutSection (String name, Element elem) throws RuntimeException
	{
		if ("pom-info".equalsIgnoreCase(name))
		{
			if (_infoPanel != null)
				throw new IllegalStateException("layoutSection(" + DOMUtils.toString(elem) + ") re-specified POM info panel");

			_infoPanel = new LRLabeledTextFieldsPanel(elem);
			_workDir = _infoPanel.getTextField(WORKDIR_FIELD_NAME);
			_extraArgs = _infoPanel.getTextField(ARGUMENTS_FIELD_NAME);
			_targets = _infoPanel.getTextField(TARGETS_FIELD_NAME);
		}
		else if ("log-msgs-area".equalsIgnoreCase(name))
		{
			if (_logsArea != null)
				throw new IllegalStateException("layoutSection(" + DOMUtils.toString(elem) + ") re-specified log messages area configuration");

			_logsArea = new LogMessagesArea(getMainFont("log-msgs-font"), elem);
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

		final Container	ctPane=getContentPane();
		final JPanel	northPanel=new JPanel(new GridLayout(1, 3, 0, 0));
		if (_infoPanel != null)
			northPanel.add(_infoPanel);

		{
			final JToolBar	b=getMainToolBar();
			final Map<String,? extends AbstractButton>	hm=setToolBarHandlers(b);
			if ((hm != null) && (hm.size() > 0))
			{
				_loadBtn = hm.get(LOAD_CMD);
				_runBtn = hm.get(RUN_CMD);
				_stopBtn = hm.get(STOP_CMD);
			}

			if (b != null)
				northPanel.add(b);
		}

		ctPane.add(northPanel, BorderLayout.NORTH);
		{
			final Component	logsScroll=new ScrolledComponent<Component>(_logsArea);
			final Image	bgImg=getIconImage();
			if (bgImg != null)
			{
				final Container	bgPanel=new BgImagePanel(bgImg, new BorderLayout(0, 0));
				bgPanel.add(logsScroll, BorderLayout.CENTER);
				bgPanel.setBackground(Color.white);
				ctPane.add(bgPanel, BorderLayout.CENTER);
			}
			else
				ctPane.add(logsScroll, BorderLayout.CENTER);
		}

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
 			});

		setDropTarget(new DropTarget(_logsArea, this));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	MainFrame () throws Exception
	{
		super();
	}
}

/*
 * 
 */
package net.community.apps.apache.maven.conv2maven;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
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
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import net.community.apps.apache.maven.conv2maven.resources.ResourcesAnchor;
import net.community.apps.common.BaseMainFrame;
import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.swing.component.menu.MenuItemExplorer;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.ui.components.logging.LogMessagesArea;
import net.community.chest.ui.components.text.FolderAutoCompleter;
import net.community.chest.ui.helpers.button.HelperCheckBox;
import net.community.chest.ui.helpers.panel.input.LRFieldWithButtonPanel;
import net.community.chest.ui.helpers.text.InputTextField;
import net.community.chest.util.logging.LogLevelWrapper;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 7, 2011 9:09:58 AM
 *
 */
final class MainFrame extends BaseMainFrame<ResourcesAnchor> implements Runnable {
	private static final long serialVersionUID = -6329514955688913319L;
	protected static final LoggerWrapper	_logger=WrapperFactoryManager.getLogger(MainFrame.class);

	MainFrame () throws Exception
	{
		super();
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#getLogger()
	 */
	@Override
	protected LoggerWrapper getLogger ()
	{
		return _logger;
	}

	private LogMessagesArea	_logsArea;
	public void log (LogLevelWrapper l, String msg)
	{
		if ((_logsArea == null) || (l == null)
		 || (msg == null) || (msg.length() <= 0))
			return;

		try
		{
			_logsArea.log(l, msg);
		}
		catch (BadLocationException e)
		{
			_logger.error("log(" + l + ")[" + msg + "] failed: " + e.getMessage());
		}
	}

	protected void clearLogMessagesArea ()
	{
		if (_logsArea != null)
			_logsArea.setText("");
	}

	private HelperCheckBox	_scanRecursive;
	public boolean isRecursiveScanning ()
	{
		return (_scanRecursive == null) || _scanRecursive.isSelected();
	}

	public void setRecursiveScanning (boolean enabled)
	{
		if ((_scanRecursive == null) || (_scanRecursive.isSelected() == enabled))
			return;

		_scanRecursive.setSelected(enabled);
	}

	private final KeyListener _runOptionKeyListener=new KeyAdapter() {
		/*
		 * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
		 */
		@Override
		public void keyReleased (KeyEvent e)
		{
			if (e == null)
				return;

			updateOkToRun();
		}
	};

	private LRFieldWithButtonPanel	_rootSelector;
	private FolderAutoCompleter<JTextComponent> _rootCompleter;
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutSection(java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void layoutSection (String name, Element elem) throws RuntimeException
	{
		if (_logger.isDebugEnabled())
			_logger.debug("layoutSection(" + name + ")[" + DOMUtils.toString(elem) + "]");

		if ("root-selector".equalsIgnoreCase(name))
		{
			if (_rootSelector != null)
				throw new IllegalStateException("layoutSection(" + name + ") already initialized for " + DOMUtils.toString(elem));

			// delay initialization
			_rootSelector = new LRFieldWithButtonPanel(elem, false);
			_rootSelector.setTextField(new InputTextField());
			_rootSelector.layoutComponent();
			_rootSelector.addActionListener(getLoadFileListener());
			_rootSelector.addTextFieldKeyListener(_runOptionKeyListener);
			_rootCompleter = new FolderAutoCompleter<JTextComponent>(_rootSelector.getTextField());
		}
		else if ("log-msgs-area".equalsIgnoreCase(name))
		{
			if (_logsArea != null)
				throw new IllegalStateException("layoutSection(" + name + ") already initialized for " + DOMUtils.toString(elem));

			_logsArea = new LogMessagesArea(Font.getFont(Font.DIALOG), elem);
		}
		else if ("scan-recursive".equalsIgnoreCase(name))
		{
			if (_scanRecursive != null)
				throw new IllegalStateException("layoutSection(" + name + ") already initialized for " + DOMUtils.toString(elem));
			
			_scanRecursive = new HelperCheckBox(elem);
		}
		else
			super.layoutSection(name, elem);
	}

	public final String getRootFolder ()
	{
		return (_rootCompleter == null) ? null : _rootCompleter.getText();
	}

	private void setRootFolder (File rootFolder)
	{
		try
		{
			if (rootFolder == null)
				return;

			if (!rootFolder.isDirectory())
			{
				_logger.error("Referenced file is not a folder: " + rootFolder.getAbsolutePath());
				return;
			}

			if (_rootCompleter == null)
				return;
	
			final String	rootPath=rootFolder.getAbsolutePath(),
							prev=_rootCompleter.getText();
			if (0 == StringUtil.compareDataStrings(prev, rootPath, true))
				return;
			_rootCompleter.setText(rootPath);
		}
		finally
		{
			updateOkToRun();
		}
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void loadFile (final File f, final String cmd, final Element dlgElement)
	{
		setRootFolder(f);
	}
	/*
	 * @see net.community.apps.common.MainComponent#getResourcesAnchor()
	 */
	@Override
	public ResourcesAnchor getResourcesAnchor ()
	{
		return ResourcesAnchor.getInstance();
	}

	protected AbstractButton	_runBtn, _stopBtn;
	private boolean	_running	/* =false */;
	protected boolean isOkToRun ()
	{
        final String	rootFolder=getRootFolder();
        if ((rootFolder == null) || (rootFolder.length() <= 0))
        	return false;
        if (_runner != null)
        	return false;

        return !_running;
	}

	protected boolean updateOkToRun ()
	{
		if ((_runBtn == null) || (_runMenuItem == null))
			return false;

		final boolean	okToRun=isOkToRun();
		_runBtn.setEnabled(okToRun);
		_runMenuItem.setEnabled(okToRun);
		return okToRun;
	}

	protected JMenuItem	_runMenuItem, _loadMenuItem, _stopMenuItem;
	protected void setRunningMode (boolean running)
	{
		if (_running != running)
		{
			AttrUtils.setComponentEnabledState(!running,
					_rootSelector, _runBtn, _scanRecursive,
					_runMenuItem, _loadMenuItem);

			final Cursor	c=running
					? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
					: Cursor.getDefaultCursor()
					;
			if ((c != null) && (_logsArea != null))
				_logsArea.setCursor(c);

			AttrUtils.setComponentEnabledState(running, _stopMenuItem, _stopBtn);

			_running = running;
		}
	}

	private ProjectConverter	_runner;
	/*
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run ()
	{
        if (_runner != null)
        {
        	JOptionPane.showMessageDialog(this, "Stop current conversion before starting another", "Conversion in progress", JOptionPane.ERROR_MESSAGE);
        	return;
        }

        final String	rootFolder=getRootFolder();
        if ((rootFolder == null) || (rootFolder.length() <= 0))
        {
        	JOptionPane.showMessageDialog(this, "Missing root folder", "Incomplete arguments", JOptionPane.ERROR_MESSAGE);
        	return;
        }

		clearLogMessagesArea();

		_runner = new ProjectConverter(this);
   		setRunningMode(true);
        _runner.execute();
	}

	void signalConversionDone (final ProjectConverter r)
	{
		if (r != null)
		{
			if (_runner != r)
				_logger.warn("signalConversionDone() mismatched instances");
			_runner = null;
		}

		setRunningMode(false);
	}

	protected void stop ()
	{
		if ((_runner == null) || _runner.isDone() || _runner.isCancelled())
			return;

		_runner.cancel(false);
		_logger.info("Canceled by user request");
	}

	private static final String	RUN_CMD="run", CLEAR_CMD="clear", STOP_CMD="stop";
	/*
	 * @see net.community.apps.common.FilesLoadMainFrame#setMainMenuItemsActionHandlers(net.community.chest.swing.component.menu.MenuItemExplorer)
	 */
	@Override
	protected Map<String,JMenuItem> setMainMenuItemsActionHandlers (MenuItemExplorer ie)
	{
		final Map<String,JMenuItem>	im=super.setMainMenuItemsActionHandlers(ie);
		_loadMenuItem = (null == im) ? null : im.get(LOAD_CMD);
		_stopMenuItem = (null == im) ? null : im.get(STOP_CMD);
		_runMenuItem = (null == im) ? null : im.get(RUN_CMD);
		return im;
	}
	/*
	 * @see net.community.apps.common.FilesLoadMainFrame#getActionListenersMap(boolean)
	 */
	@Override
	protected Map<String,? extends ActionListener> getActionListenersMap (boolean createIfNotExist)
	{
		final Map<String,? extends ActionListener>	org=super.getActionListenersMap(createIfNotExist);
		if (((org != null) && (org.size() > 0)) || (!createIfNotExist))
			return org;

		final Map<String,ActionListener>	lm=new TreeMap<String,ActionListener>(String.CASE_INSENSITIVE_ORDER);
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

		setActionListenersMap(lm);
		return lm;
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		final JPanel	northPanel=new JPanel(new GridLayout(0, 1));
		try
		{
			final JToolBar								b=getMainToolBar();
			final Map<String,? extends AbstractButton>	hm=setToolBarHandlers(b);
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

		if (_rootSelector != null)
			northPanel.add(_rootSelector);
		if (_scanRecursive != null)
			northPanel.add(_scanRecursive);

		final Container	ctPane=getContentPane();
		ctPane.add(northPanel, BorderLayout.NORTH);
		
		if (_logsArea != null)
			ctPane.add(new ScrolledComponent<JTextPane>(JTextPane.class, _logsArea), BorderLayout.CENTER);

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
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
}

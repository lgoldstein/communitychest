/*
 * 
 */
package net.community.apps.common;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.SysPropsEnum;
import net.community.chest.swing.component.menu.MenuExplorer;
import net.community.chest.swing.component.menu.MenuItemExplorer;
import net.community.chest.swing.component.menu.MenuUtil;
import net.community.chest.ui.components.dialog.manifest.ManifestDialog;
import net.community.chest.ui.helpers.filechooser.HelperFileChooser;
import net.community.chest.ui.helpers.frame.HelperFrame;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 11:08:52 AM
 */
public abstract class FilesLoadMainFrame extends HelperFrame implements BaseMainComponent {
	private static final long serialVersionUID = 9039034035615278148L;

	protected FilesLoadMainFrame (final boolean autoInit)
	{
		super(autoInit);
	}
	/*
	 * @see net.community.apps.common.BaseMainComponent#getMainFrame()
	 */
	@Override
	@CoVariantReturn
	public FilesLoadMainFrame getMainFrame ()
	{
		return this;
	}
    /*
     * @see net.community.apps.common.BaseMainComponent#exitApplication()
     */
    @Override
	public void exitApplication ()
	{
        // TODO stopWhenYouCan();
        System.exit(0);
	}

    public static final String	EXIT_CMD="exit";
    private ActionListener	_exitListener	/* =null */;
    protected ActionListener getExitActionListener ()
    {
    	if (null == _exitListener)
    		_exitListener = new ActionListener() {
					/*
					 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
					 */
					@Override
					public void actionPerformed (ActionEvent e)
					{
						exitApplication();
					}
				};

		return _exitListener;
    }

    public void loadFiles (final String cmd, final Element dlgElement, final List<? extends File> fl)
    {
    	final int	numSelected=(null == fl) ? 0 : fl.size();
    	if (numSelected > 0)
    	{
    		if (1 == numSelected)
    			loadFile(fl.get(0), cmd, dlgElement);
    		else
    			throw new UnsupportedOperationException("loadFiles(" + cmd + " - " + numSelected + " files)[" + DOMUtils.toString(dlgElement) + "] N/A");
    	}
    }

    private File	_lastLoadDir=new File(SysPropsEnum.USERDIR.getPropertyValue());
    protected File getInitialFileChooserFolder ()
    {
    	return _lastLoadDir;
    }

    // returns previous value
    protected File setInitialFileChooserFolder (final File f, final Boolean isSaveDialog)
    {
    	final File	prev=_lastLoadDir;
    	if ((null == f) || (null == isSaveDialog))
    		return prev;	// just so compiler does not complain about un-referenced parameters
 
    	if (prev != f)
    		_lastLoadDir = f.isDirectory() ? f : f.getParentFile();
    	return prev;
    }

    protected File getInitialFileChooserFolder (
    		final Element dlgElement, final String cmd, final Boolean isSaveDialog)
    {
    	if ((null == dlgElement) || (null == isSaveDialog)
    	 || ((cmd != null) && (cmd.length() >= Short.MAX_VALUE)))
    		return _lastLoadDir;	// just so compiler does not complain about un-referenced parameters

    	return _lastLoadDir;
     }

    protected JFileChooser getFileChooser (
    		final Element dlgElement, final String cmd, final Boolean isSaveDialog)
    {
    	if (null == isSaveDialog)
    		return null;

    	final JFileChooser	fc;
    	try
    	{
    		fc = new HelperFileChooser(dlgElement);
    	}
    	catch(Exception e)
    	{
    		throw ExceptionUtil.toRuntimeException(e);
    	}

    	final File	ld=getInitialFileChooserFolder(dlgElement, cmd, isSaveDialog);
    	if (ld != null)
    		fc.setCurrentDirectory(ld);

    	return fc;
    }

    protected List<File> getChosenFiles (
    		final Element dlgElement, final String cmd, final boolean isSaveDialog)
    {
    	final JFileChooser	fc=
    		getFileChooser(dlgElement, cmd, Boolean.valueOf(isSaveDialog));
    	if (null == fc)
    		return null;

    	final int	nRes=isSaveDialog ? fc.showSaveDialog(this) : fc.showOpenDialog(this);
		if (nRes != JFileChooser.APPROVE_OPTION)
			return null;

		final List<File>	selFiles;
		if (fc.isMultiSelectionEnabled())
		{
			final File[]	fa=fc.getSelectedFiles();
			if ((null == fa) || (fa.length <= 0))
				return null;

			selFiles = Arrays.asList(fa);
		}
		else
		{
			final File	f=fc.getSelectedFile();
			if (null == f)
				return null;

			selFiles = Arrays.asList(f);
		}

		// assume all files selected from same folder
    	if ((selFiles != null) && (selFiles.size() > 0))
		{
    		final File	lastSel=selFiles.get(0);
    		if (null == lastSel)
				return null;

    		_lastLoadDir = lastSel.getParentFile();
		}

    	return selFiles;
    }

    protected void loadFile (final String cmd, final Element dlgElement)
    {
    	loadFiles(cmd, dlgElement, getChosenFiles(dlgElement, cmd, false));
    }

    protected void loadFileByCommand (final String cmd)
	{
		if ((null == cmd) || (cmd.length() <= 0))
			return;

		loadFile(cmd, getLoadDialogElement());
	}
	/*
	 * @see net.community.apps.common.BaseMainComponent#getLoadDialogElement()
	 */
	@Override
	public Element getLoadDialogElement ()
	{
		return getSection(LOAD_FILE_SECTION_NAME);
	}
	/**
	 * Default command used by {@link #loadFile()} in call to {@link #loadFileByCommand(String)}
	 */
	public static final String	LOAD_CMD="load";
	/*
	 * @see net.community.apps.common.BaseMainComponent#loadFile()
	 */
	@Override
	public void loadFile ()
	{
		loadFileByCommand(LOAD_CMD);
	}

	private ActionListener	_loadFileListener	/* =null */;
    protected ActionListener getLoadFileListener ()
    {
    	if (null == _loadFileListener)
    		_loadFileListener = new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent event)
				{
					loadFileByCommand((null == event) ? null : event.getActionCommand());
				}
			};

		return _loadFileListener;
    }

    public void saveFiles (final Element dlgElement, final List<? extends File> fl)
    {
    	final int	numFiles=(null == fl) ? 0 : fl.size();
    	if (numFiles > 0)
    	{
    		if (numFiles != 1)
    			throw new UnsupportedOperationException("saveFiles(" + numFiles + " files)[" + DOMUtils.toString(dlgElement) + "] N/A");
   
    		saveFile(fl.get(0), dlgElement);	
    	}
    }

    protected void saveFile (final Element dlgElement)
    {
    	saveFiles(dlgElement, getChosenFiles(dlgElement, SAVE_CMD, true));
    }
	/*
	 * @see net.community.apps.common.BaseMainComponent#getSaveDialogElement()
	 */
	@Override
	public Element getSaveDialogElement ()
	{
		return getSection(SAVE_FILE_SECTION_NAME);
	}
	/*
	 * @see net.community.apps.common.MainComponent#saveFile()
	 */
	@Override
	public void saveFile ()
	{
		saveFile(getSaveDialogElement());
	}

	protected void saveFileByCommand (final String cmd)
	{
		if ((null == cmd) || (cmd.length() <= 0))
			return;

		saveFile();
	}

	public static final String	SAVE_CMD="save";
	private ActionListener	_saveFileListener	/* =null */;
	protected ActionListener getSaveFileListener ()
	{
		if (null == _saveFileListener)
			_saveFileListener = new ActionListener() {
					/*
					 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
					 */
					@Override
					public void actionPerformed (ActionEvent event)
					{
						saveFileByCommand((null == event) ? null : event.getActionCommand());
					}
				};

		return _saveFileListener;
	}

	@Override
	public void showManifest (Object anchor, Element dlgElem) throws Exception
	{
		// NOTE: columns must be added BEFORE model is attached to a table - which is what the XML element is assumed to contain
		final ManifestDialog	dlg=new ManifestDialog(this, anchor.getClass(), dlgElem);
		final Dimension			dim=getSize();
		dlg.setSize((int) (dim.getWidth() / 2), (int) (dim.getHeight() / 2));
		dlg.setVisible(true);
	}
	/*
	 * @see net.community.apps.common.BaseMainComponent#getManifestDialogElement()
	 */
	@Override
	public Element getManifestDialogElement ()
	{
		return getSection(MANIFEST_SECTION_NAME);
	}
	/*
	 * @see net.community.apps.common.BaseMainComponent#showManifest()
	 */
	@Override
	public void showManifest () throws Exception
	{
		showManifest(this, getManifestDialogElement());
	}

	protected void showManifestByCommand (final String cmd)
	{
		if ((null == cmd) || (cmd.length() <= 0))
			return;

		try
		{
			showManifest();
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}

	public static final String	ABOUT_CMD="about";
	private ActionListener	_manifestListener	/* =null */;
	protected ActionListener getShowManifestActionListener ()
	{
		if (null == _manifestListener)
			_manifestListener = new ActionListener () {
					/*
					 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
					 */
					@Override
					public void actionPerformed (ActionEvent e)
					{
						showManifestByCommand((null == e) ? null : e.getActionCommand());
					}
				};

		return _manifestListener;
	}

	private Map<String,? extends ActionListener>	_lm;
	protected Map<String,? extends ActionListener> getActionListenersMap (final boolean createIfNotExist)
	{
		if ((null == _lm) && createIfNotExist)
			return null;

		return _lm;
	}

	public Map<String,? extends ActionListener> getActionListenersMap ()
	{
		return getActionListenersMap(false);
	}

	public void setActionListenersMap (Map<String,? extends ActionListener> lm)
	{
		_lm = lm;
	}

	protected Map<String,JMenuItem> setMainMenuItemsActionHandlers (final MenuItemExplorer ie)
	{
		return MenuUtil.setMenuItemsHandlers(ie, getActionListenersMap(true));
	}

	protected Map<String,JMenu> setMainMenuActionHandlers (final MenuExplorer ie)
	{
		return MenuUtil.setMenuActionHandlers(ie, getActionListenersMap(true));
	}

	protected void setMainMenuActionHandlers (final MenuItemExplorer ie, final MenuExplorer me)
	{
		if (ie != null)
			setMainMenuItemsActionHandlers(ie);
		if (me != null)
			setMainMenuActionHandlers(me);
	}
	
	protected <B extends JMenuBar> B setMainMenuActionHandlers (final B bar)
	{
    	final MenuItemExplorer	ie=MenuUtil.resolveMenuItemExplorer(bar);
    	final MenuExplorer 		me=MenuUtil.resolveMenuExplorer(bar);
    	setMainMenuActionHandlers(ie, me);
    	return bar;
	}
}

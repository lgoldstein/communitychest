/*
 * 
 */
package net.community.apps.tools.svn.wc;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.community.apps.tools.svn.SVNBaseMain;
import net.community.apps.tools.svn.SVNBaseMainFrame;
import net.community.apps.tools.svn.resources.DefaultResourcesAnchor;
import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.lang.SysPropsEnum;
import net.community.chest.svnkit.SVNFoldersFilter;
import net.community.chest.svnkit.core.CoreUtils;
import net.community.chest.svnkit.core.io.SVNRepositoryFactoryType;
import net.community.chest.svnkit.core.wc.SVNLocalCopyData;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.component.table.JTableReflectiveProxy;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.helpers.panel.input.LRFieldWithButtonPanel;
import net.community.chest.ui.helpers.panel.input.LRFieldWithLabelInput;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 9:57:45 AM
 */
public class WCMainFrame extends SVNBaseMainFrame<DefaultResourcesAnchor> implements ISVNStatusHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1229705577006724625L;
	private static final LoggerWrapper	_logger=WrapperFactoryManager.getLogger(WCMainFrame.class);

	public WCMainFrame () throws Exception
    {
		super(true);

		String	wcLoc=getWCLocation();
		if ((wcLoc != null) && (wcLoc.length() > 0))
			return;

		setWCLocation(null, SysPropsEnum.USERDIR.getPropertyValue(), true);
    }
	/*
	 * @see net.community.apps.common.MainComponent#getResourcesAnchor()
	 */
	@Override
	public DefaultResourcesAnchor getResourcesAnchor ()
	{
		return DefaultResourcesAnchor.getInstance();
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
	 * @see net.community.apps.common.BaseMainComponent#getMainFrame()
	 */
	@Override
	@CoVariantReturn
	public WCMainFrame getMainFrame ()
	{
		return this;
	}
	/*
	 * @see net.community.apps.common.FilesLoadMainFrame#showManifest()
	 */
	@Override
	public void showManifest ()
	{
		try
		{
			super.showManifest();
		}
		catch(Exception e)
		{
			BaseOptionPane.showMessageDialog(this, e);
		}
	}

	private LRFieldWithLabelInput	_repoInfo;
	public String getRepositoryRoot ()
	{
		return (null == _repoInfo) ? null : _repoInfo.getText();
	}

	public void setRepositoryRoot (String t)
	{
		if (_repoInfo != null)
			_repoInfo.setText((null == t) ? "" : t);
	}
	
	public void setRepositoryRoot (SVNURL u)
	{
		setRepositoryRoot((null == u) ? null : u.toString());
	}
    /*
     * @see net.community.apps.common.FilesLoadMainFrame#getFileChooser(org.w3c.dom.Element, java.lang.String, java.lang.Boolean)
     */
    @Override
	protected JFileChooser getFileChooser (
			final Element dlgElement, final String cmd, final Boolean isSaveDialog)
    {
    	final JFileChooser	fc=super.getFileChooser(dlgElement, cmd, isSaveDialog);
    	if (fc != null)
    		fc.setFileFilter(SVNFoldersFilter.DEFAULT);
    	return fc;
    }
    /*
     * @see net.community.apps.common.BaseMainComponent#saveFile(java.io.File, org.w3c.dom.Element)
     */
    @Override
	public void saveFile (final File f, final Element dlgElement)
    {
		if (f != null)
	    	JOptionPane.showMessageDialog(this, DOMUtils.toString(dlgElement), "Unexpected call", JOptionPane.ERROR_MESSAGE);
    }

    private static Collection<? extends SVNErrorCode>	_ignoredErrors=
    	Arrays.asList(SVNErrorCode.WC_NOT_DIRECTORY,
    				  SVNErrorCode.WC_NOT_FILE,
    				  SVNErrorCode.ENTRY_NOT_FOUND);

    private static boolean showSVNException (final SVNException e)
    {
    	return !CoreUtils.isContainedSVNException(e, _ignoredErrors);
    }

    private LRFieldWithButtonPanel	_wcLocal;
	/*
	 * @see net.community.apps.tools.svn.SVNBaseMainFrame#getWCLocation()
	 */
	@Override
	public String getWCLocation ()
	{
		return (null == _wcLocal) ? null : _wcLocal.getText();
	}

	private WCLocalFilesManager	_filesMgr;
	public WCLocalFilesManager getFilesManager ()
	{
		return _filesMgr;
	}
	/*
	 * @see org.tmatesoft.svn.core.wc.ISVNStatusHandler#handleStatus(org.tmatesoft.svn.core.wc.SVNStatus)
	 */
	@Override
	public void handleStatus (SVNStatus status) throws SVNException
	{
		final WCLocalFilesManager	mgr=getFilesManager();
		final WCLocalFilesModel		mdl=(null == mgr) ? null : mgr.getTypedModel();
		final SVNLocalCopyData		lclData=(null == mdl) ? null : mdl.handleStatus(status, true);
		if ((lclData != null) && _logger.isDebugEnabled())
			_logger.debug("handleStatus(" + lclData + ") updated");
	}

	private WCLocalFilesStatusUpdater startUpdating (File f)
	{
		if (!WCLocationFileInputVerifier.DEFAULT.verifyFile(f))
			return null;

		final WCLocalFilesStatusUpdater	u=new WCLocalFilesStatusUpdater(
				this, SVNBaseMain.getSVNClientManager(true), f);
		return u;
	}

	private WCLocalFilesStatusUpdater	_updater;
	void stopUpdating (WCLocalFilesStatusUpdater u, boolean stopIfRunning)
	{
		if (null == u)
			return;

		if (_updater == u)
		{
			if (stopIfRunning)
				u.cancel(true);

			try
			{
				final Long	l=u.get(15L, TimeUnit.SECONDS);
				if (_logger.isDebugEnabled())
					_logger.debug("stopUpdating(" + getWCLocation() + ") version=" + l);
			}
			catch(Exception e)
			{
				BaseOptionPane.showMessageDialog(this, e);
			}

			_updater = null;
		}
	}
	/*
	 * @see net.community.apps.tools.svn.SVNBaseMainFrame#setWCLocation(java.io.File, java.lang.String, boolean)
	 */
	@Override
	public boolean setWCLocation (
			final File orgFile, final String orgPath, final boolean forceRefresh)
	{
		if (null == _wcLocal)
			return false;

		final String	filePath;
		if ((null == orgPath) || (orgPath.length() <= 0))
			filePath = (null == orgFile) ? "" : orgFile.getAbsolutePath();
		else
			filePath = orgPath;

		final String	prev=_wcLocal.getText();
		if ((!forceRefresh) && (0 == StringUtil.compareDataStrings(prev, filePath, true)))
			return false;

		_wcLocal.setText(filePath);
		setTitle(filePath);

		if ((null == filePath) || (filePath.length() <= 0))
			return false;	// can occur for the top-level volume root(s)

		final File	f=(null == orgFile) ? new File(filePath) : orgFile;
		try
		{
			if (!WCLocationFileInputVerifier.DEFAULT.verifyFile(f))
				throw new IllegalStateException("Invalid WC location");

			final SVNClientManager	mgr=SVNBaseMain.getSVNClientManager(true);
			final SVNWCClient		wcc=mgr.getWCClient();
			final SVNURL			url=
				wcc.getReposRoot(f, null, SVNRevision.WORKING, null, null);
			setRepositoryRoot(url);

			final SVNRepositoryFactoryType	t=SVNRepositoryFactoryType.setup(url);
			if (t != null)
				_logger.info("setWCLocation(" + filePath + ") Initialized " + t + " repository");
			if (_filesMgr != null)
				_filesMgr.setParentFolder(f);

			stopUpdating(_updater, true);
			if ((_updater=startUpdating(f)) != null)
				_updater.execute();
			return true;
		}
		catch(Exception e)
		{
			if ((e instanceof SVNException) && showSVNException((SVNException) e))
				BaseOptionPane.showMessageDialog(this, e);
			setRepositoryRoot(SVNStatusType.UNKNOWN.toString());
			stopUpdating(_updater, true);
			return false;
		}
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void loadFile (File f, String cmd, Element dlgElement)
	{
		if ((null == f) || (!f.isDirectory()))
			return;

		setWCLocation(f, f.getAbsolutePath(), false);
	}
	/*
	 * @see net.community.chest.ui.helpers.frame.HelperFrame#layoutSection(java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void layoutSection (String name, Element elem) throws RuntimeException
	{
		if (_logger.isDebugEnabled())
			_logger.debug("layoutSection(" + name + ")[" + DOMUtils.toString(elem) + "]");

		if ("repo-info".equalsIgnoreCase(name))
		{
			if (_repoInfo != null)
				throw new IllegalStateException("layoutSection(" + name + ") already initialized for " + DOMUtils.toString(elem));

			_repoInfo = new LRFieldWithLabelInput(elem);
		}
		else if ("wc-local".equalsIgnoreCase(name))
		{
			if (_wcLocal != null)
				throw new IllegalStateException("layoutSection(" + name + ") already initialized for " + DOMUtils.toString(elem));

			// delay auto-layout till after setting the text field
			_wcLocal = new LRFieldWithButtonPanel(elem, false);
			_wcLocal.setTextField(new WCLocationInputTextField(this));
			_wcLocal.layoutComponent();
			_wcLocal.addActionListener(getLoadFileListener());
		}
		else if ("files-mgr-model".equalsIgnoreCase(name))
		{
			if (_filesMgr != null)
				throw new IllegalStateException("layoutSection(" + name + ") already initialized for " + DOMUtils.toString(elem));

			try
			{
				final WCLocalFilesModel	m=new WCLocalFilesModel(this);
				if (m.fromXml(elem) != m)
					throw new IllegalStateException("layoutSection(" + name + ") mismatched initialization for " + DOMUtils.toString(elem));

				_filesMgr = new WCLocalFilesManager(m);
				_filesMgr.setRowSorter(new WCLocalFilesSorter(m));

				final WCFilesManagerPopupMenu		pm=
					new WCFilesManagerPopupMenu(getSection("files-mgr-popup-menu"));
				final WCFilesManagerMouseAdapter	ma=
					new WCFilesManagerMouseAdapter(_filesMgr, pm);
				_filesMgr.addMouseListener(ma);

				final Element	tblElem=
					applyDefinitionElement("files-mgr-tbl", _filesMgr, JTableReflectiveProxy.TBL);
				if ((tblElem != null) && _logger.isDebugEnabled())
					_logger.debug("layoutSection(" + name + ")[" + DOMUtils.toString(tblElem) + "]");
			}
			catch(Exception e)
			{
				throw ExceptionUtil.toRuntimeException(e);
			}
		}
	}

	public void refresh ()
	{
		setWCLocation(null, getWCLocation(), true);
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
		lm.put(LOAD_CMD, getLoadFileListener());
		lm.put(EXIT_CMD, getExitActionListener());
		lm.put(ABOUT_CMD, getShowManifestActionListener());
		lm.put("refresh", new ActionListener() {
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

		setActionListenersMap(lm);
		return lm;
	}
	/*
	 * @see net.community.chest.ui.helpers.frame.HelperFrame#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		final Container	ctPane=getContentPane();
		{
			final JPanel	northPanel=new JPanel(new GridLayout(0, 1));
			if (_repoInfo != null)
				northPanel.add(_repoInfo);
			if (_wcLocal != null)
				northPanel.add(_wcLocal);
			ctPane.add(northPanel, BorderLayout.NORTH);
		}

		if (_filesMgr != null)
			ctPane.add(new ScrolledComponent<WCLocalFilesManager>(WCLocalFilesManager.class, _filesMgr), BorderLayout.CENTER);
	}
}

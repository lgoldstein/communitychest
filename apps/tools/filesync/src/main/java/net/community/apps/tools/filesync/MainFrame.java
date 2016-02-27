/*
 * 
 */
package net.community.apps.tools.filesync;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.community.apps.common.BaseMainFrame;
import net.community.apps.tools.filesync.resources.ResourcesAnchor;
import net.community.chest.awt.attributes.Iconable;
import net.community.chest.dom.DOMUtils;
import net.community.chest.io.FileUtil;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.menu.MenuItemExplorer;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.swing.resources.UIAnchoredResourceAccessor;
import net.community.chest.util.compare.InstancesComparator;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;
import net.community.chest.util.map.MapEntryImpl;
import net.community.chest.util.set.SetsUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 15, 2009 2:58:16 PM
 */
final class MainFrame extends BaseMainFrame<ResourcesAnchor> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7470264927687659344L;
	private static final LoggerWrapper	_logger=WrapperFactoryManager.getLogger(MainFrame.class);
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

	public static final MainFrame getContainerFrameInstance ()
	{
		return MainFrame.class.cast(getMainFrameInstance());
	}

	private FilesList	_pairsList;
	void addFilePair (final FilePair	fp)
	{
		if (null == fp)
			throw new IllegalArgumentException("Invalid pair to add");

		fp.setIcon(getSeparatorIcon());
		_pairsList.add(fp);
		_pairsList.updateUI();
	}

	protected void addFilePair (final File srcFolder, final File dstFolder)
	{
		if (null == srcFolder)
			throw new IllegalStateException("No source folder specified");
		else if (null == dstFolder)
			throw new IllegalStateException("No destination folder specified");
		if (null == _pairsList)
			throw new IllegalStateException("No pairs list");

		if (srcFolder.exists() && (!srcFolder.isDirectory()))
			throw new IllegalStateException("Source=" + srcFolder + " is not a directory");
		if (dstFolder.exists() && (!dstFolder.isDirectory()))
			throw new IllegalStateException("Destination=" + dstFolder + " is not a directory");

		addFilePair(new FilePair(srcFolder, dstFolder));
	}

	public void loadFilePair ()
	{
		final FilePair	p=new FilePair();
		new FilePairLoader(this, p, getSection("pair-load-dialog")).setVisible(true);
		if ((p.getSrcFolder() != null) && (p.getDstFolder() != null))
			addFilePair(p);
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#loadFileByCommand(java.lang.String)
	 */
	@Override
	protected void loadFileByCommand (String cmd)
	{
		if ("load".equalsIgnoreCase(cmd))
			loadFilePair();
		else
			super.loadFileByCommand(cmd);
	}

	public static final String	SRC_ATTR="src", DST_ATTR="dst";
	private static Document createExportDocument (final Collection<? extends Map.Entry<? extends File,? extends File>> pl)
		throws ParserConfigurationException
	{
		if ((null == pl) || (pl.size() <= 0))
			return null;

		final Document	doc=DOMUtils.createDefaultDocument();
		final Element	root=doc.createElement("pairs");
		for (final Map.Entry<? extends File,? extends File>	p : pl)
		{
			final File	srcFile=(null == p) ? null : p.getKey(),
						dstFile=(null == p) ? null : p.getValue();
			if ((null == srcFile) || (null == dstFile))
				continue;

			final Element	elem=doc.createElement("pair");
			elem.setAttribute(SRC_ATTR, srcFile.getAbsolutePath());
			elem.setAttribute(DST_ATTR, dstFile.getAbsolutePath());
			root.appendChild(elem);
		}

		doc.appendChild(root);
		return doc;
	}

	protected Document exportPairs (final File f)
	{
		if (f.exists())
		{
			final int	nRes=JOptionPane.showConfirmDialog(this, "File already exists - overwrite ?", "Overwrite confirmation", JOptionPane.YES_NO_OPTION);
			if (nRes != JOptionPane.YES_OPTION)
				return null;
		}

		final Collection<? extends Map.Entry<? extends File,? extends File>>	pl=
			(null == _pairsList) ? null : _pairsList.getFilePairs(false);
		if ((null == pl) || (pl.size() <= 0))
		{
			JOptionPane.showMessageDialog(this, "No pair(s) available", "No pair(s) available", JOptionPane.WARNING_MESSAGE);
			return null;
		}

		try
		{
			final Document		doc=createExportDocument(pl);
			final Source		src=new DOMSource(doc);
			final Transformer	t=DOMUtils.getDefaultXmlTransformer();
			OutputStream		out=null;
			try
			{
				out = new FileOutputStream(f);
			
				t.transform(src, new StreamResult(out));
				return doc;
			}
			finally
			{
				FileUtil.closeAll(out);
			}
		}
		catch(Exception e)
		{
			_logger.error("exportPairs(" + f + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
			BaseOptionPane.showMessageDialog(this, e);
			return null;
		}
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#saveFile(java.io.File, org.w3c.dom.Element)
	 */
	@Override
	public void saveFile (File f, Element dlgElement)
	{
		exportPairs(f);
	}

	private static final Collection<? extends Map.Entry<? extends File,? extends File>> createPairsList (final Document doc)
	{
		final Element						root=
			(null == doc) ? null : doc.getDocumentElement();
		final Collection<? extends Element>	el=
			DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
		final int							numElems=(null == el) ? 0 : el.size();
		if (numElems <= 0)
			return null;

		Collection<Map.Entry<File,File>>	ret=null;
		for (final Element elem : el)
		{
			final String	srcFile=(null == elem) ? null : elem.getAttribute(SRC_ATTR),
							dstFile=(null == elem) ? null : elem.getAttribute(DST_ATTR);
			if ((null == srcFile) || (srcFile.length() <= 0)
			 || (null == dstFile) || (dstFile.length() <= 0))
			{
				_logger.warn("createPairsList(" + DOMUtils.toString(elem) + ") missing data");
				continue;
			}

			if (null == ret)
				ret = new ArrayList<Map.Entry<File,File>>(numElems);
			ret.add(new MapEntryImpl<File,File>(new File(srcFile), new File(dstFile)));
 		}

		return ret;
	}

	protected Document importPairs (final File f)
	{
		try
		{
			final Document															doc=
				DOMUtils.loadDocument(f);
			final Collection<? extends Map.Entry<? extends File,? extends File>>	pl=
				createPairsList(doc);
			if ((null == pl) || (pl.size() <= 0))
			{
				JOptionPane.showMessageDialog(this, "No pair(s) available", "No pair(s) available", JOptionPane.WARNING_MESSAGE);
				return null;
			}

			for (final Map.Entry<? extends File,? extends File>	p : pl)
			{
				final File	srcFile=(null == p) ? null : p.getKey(),
							dstFile=(null == p) ? null : p.getValue();
				if ((null == srcFile) || (null == dstFile))
					continue;

				try
				{
					addFilePair(srcFile, dstFile);
					_logger.info("importPairs(" + f + ")[" + srcFile + " => " + dstFile + "]");
				}
				catch(Exception e)
				{
					_logger.error("importPairs(" + f + ")[" + srcFile + " => " + dstFile + "] " + e.getClass().getName() + ": " + e.getMessage(), e);
					BaseOptionPane.showMessageDialog(this, e);
				}
			}

			return doc;
		}
		catch(Exception e)
		{
			_logger.error("importPairs(" + f + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
			BaseOptionPane.showMessageDialog(this, e);
			return null;
		}
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void loadFile (File f, String cmd, Element dlgElement)
	{
		importPairs(f);
	}

	private Collection<AbstractButton>	_btns;
	private void updateTrackedButtons (final boolean enabled)
	{
		if ((null == _btns) || (_btns.size() <= 0))
			return;

		for (final AbstractButton b : _btns)
		{
			if ((null == b) || (b.isEnabled() == enabled))
				continue;
			b.setEnabled(enabled);
		}
	}

	private boolean addTrackedButton (final AbstractButton b)
	{
		if (null == b)
			return false;

		if (null == _btns)
		{
			_btns = SetsUtils.setOf(new InstancesComparator<AbstractButton>(AbstractButton.class), b);
			return true;
		}

		return _btns.add(b);
	}

	private Collection<AbstractButton> addTrackedButtons (final Map<String,? extends AbstractButton> m, final Collection<String> keys)
	{
		if ((null == m) || (m.size() <= 0)
		 || (null == keys) || (keys.size() <= 0))
			return null;

		Collection<AbstractButton>	ret=null;
		for (final String	k : keys)
		{
			final AbstractButton	b=
				((null == k) || (k.length() <= 0)) ? null : m.get(k);
			if (null == b)
				continue;

			if (!addTrackedButton(b))
				continue;

			if (null == ret)
				ret = SetsUtils.setOf(new InstancesComparator<AbstractButton>(AbstractButton.class), b);
			else
				ret.add(b);
		}

		return ret;
	}

	private Collection<AbstractButton> addTrackedButtons (final Map<String,? extends AbstractButton> m, final String ... keys)
	{
		return addTrackedButtons(m, ((null == m) || (m.size() <= 0) || (null == keys) || (keys.length <= 0)) ? null : SetsUtils.setOf(String.CASE_INSENSITIVE_ORDER, keys));
	}

	private FilesSynchronizer	_sync;
	void doneFilesSynchronizer (FilesSynchronizer sync)
	{
		JOptionPane.showMessageDialog(this, "Synchronization complete", "Done", JOptionPane.INFORMATION_MESSAGE);
		if (_sync != sync)
			_logger.warn("Mismatched " + FilesSynchronizer.class.getSimpleName() + " instances");
		_sync = null;
		updateTrackedButtons(true);
	}

	protected void executeSync (final FileCmpOptions opts)
	{
		if (_sync != null)
		{
			JOptionPane.showMessageDialog(this, "Synchronizer still running", "Synchronizer still running", JOptionPane.WARNING_MESSAGE);
			return;
		}

		final Collection<? extends Map.Entry<? extends File,? extends File>>	pl=
			(null == _pairsList) ? null : _pairsList.getFilePairs(true);
		if ((null == pl) || (pl.size() <= 0))
		{
			JOptionPane.showMessageDialog(this, "No pair(s) selected", "No pair(s) selected", JOptionPane.WARNING_MESSAGE);
			return;
		}

		_sync = new FilesSynchronizer(this, pl, opts);
		updateTrackedButtons(false);
		_sync.execute();
	}

	private final FileCmpOptions	_opts=new FileCmpOptions();
	public final FileCmpOptions getComparisonOptions ()
	{
		return _opts;
	}

	private ActionListener	_runListener;
	private ActionListener getRunActionListener ()
	{
		if (null == _runListener)
			_runListener = new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent e)
				{
					executeSync(getComparisonOptions());
				}
			};
		return _runListener;
	}

	protected void showOptions (final FileCmpOptions opts)
	{
		new FileCmpOptionsDialog(this, opts, getOptionsDialogElement(), true).setVisible(true);
	}

	private ActionListener	_optsListener;
	private ActionListener getOptionsListener ()
	{
		if (null == _optsListener)
			_optsListener = new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent e)
				{
					showOptions(getComparisonOptions());
				}
			};
		return _optsListener;
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#setToolBarHandlers(javax.swing.JToolBar)
	 */
	@Override
	protected Map<String,AbstractButton> setToolBarHandlers (final JToolBar b)
	{
		final Map<String,AbstractButton>	ret=super.setToolBarHandlers(b);
		addTrackedButtons(ret, "run");
		return ret;
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#setMainMenuItemsActionHandlers(net.community.chest.swing.component.menu.MenuItemExplorer)
	 */
	@Override
	protected Map<String,JMenuItem> setMainMenuItemsActionHandlers (final MenuItemExplorer ie)
	{
		final Map<String,JMenuItem>	ret=super.setMainMenuItemsActionHandlers(ie);
		addTrackedButtons(ret, "run");
		return ret;
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
		lm.put("load", getLoadFileListener());
		lm.put("import", getLoadFileListener());
		lm.put("export", getSaveFileListener());
		lm.put("run", getRunActionListener());
		lm.put("exit", getExitActionListener());
		lm.put("about", getShowManifestActionListener());
		lm.put("options", getOptionsListener());

		setActionListenersMap(lm);
		return lm;
	}

	private JLabel	_statusBar	/* =null */;
	public void updateStatusBar (final String text)
	{
		if (_statusBar != null)
			_statusBar.setText((null == text) ? "" : text);
	}

	private Element	_optsDlgElem	/* =null */;
	private Element getOptionsDialogElement ()
	{
		return _optsDlgElem;
	}

	private Icon	_sepIcon	/* =null */;
	private Icon getSeparatorIcon ()
	{
		return _sepIcon;
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutSection(java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void layoutSection (String name, Element elem) throws RuntimeException
	{
		if ("separator-icon".equalsIgnoreCase(name))
		{
			final String						iconName=
				elem.getAttribute(Iconable.ATTR_NAME.toLowerCase());
			final UIAnchoredResourceAccessor	ra=getResourcesAnchor();
			try
			{
				_sepIcon = ra.getIcon(iconName);
				return;
			}
			catch(Exception e)
			{
				throw ExceptionUtil.toRuntimeException(e);
			}
		}
		else if ("cmp-options-dialog".equalsIgnoreCase(name))
		{
			if (_optsDlgElem != null)
				throw new IllegalStateException("layoutSection(" + name + ") already set");
			if (null == (_optsDlgElem=elem))
				throw new IllegalStateException("layoutSection(" + name + ") no data");
			return;
		}

		super.layoutSection(name, elem);
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		final Container		ctPane=getContentPane();
		try
		{
			final JToolBar	b=getMainToolBar();
			setToolBarHandlers(b);
			ctPane.add(b, BorderLayout.NORTH);
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}

		if (null == _pairsList)
		{
			_pairsList = new FilesList();
			setDropTarget(new DropTarget(_pairsList, this));
			ctPane.add(_pairsList, BorderLayout.CENTER);
		}

		if (null == _statusBar)
		{
			_statusBar = new JLabel("");
			ctPane.add(_statusBar, BorderLayout.SOUTH);
		}
	}

	private void processMainArguments (final String ... args) throws Exception
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		File		srcFolder=null, dstFolder=null;
		for (int	aIndex=0; aIndex < numArgs; aIndex++)
		{
			final String	av=args[aIndex];
			if ((null == av) || (av.length() <= 0))
				continue;

			if ("-s".equalsIgnoreCase(av))
			{
				if (srcFolder != null)
					throw new IllegalStateException(av + " option re-specified");

				aIndex++;
				srcFolder = new File(args[aIndex]);
			}
			else if ("-d".equalsIgnoreCase(av))
			{
				if (dstFolder != null)
					throw new IllegalStateException(av + " option re-specified");

				aIndex++;
				dstFolder = new File(args[aIndex]);
			}
			else if ("-i".equalsIgnoreCase(av))
			{
				aIndex++;

				importPairs(new File(args[aIndex]));
			}
			else if ("-t".equalsIgnoreCase(av))
			{
				final FileCmpOptions	opts=getComparisonOptions();
				if (opts.isTestOnly())	// default is FALSE
					throw new IllegalStateException(av + " option re-specified");
				opts.setTestOnly(true);
			}
			else if ("-c".equalsIgnoreCase(av))
			{
				final FileCmpOptions	opts=getComparisonOptions();
				if (opts.isCompareFileContents())	// default is FALSE
					throw new IllegalStateException(av + " option re-specified");
				opts.setCompareFileContents(true);
			}
			else if ("-k".equalsIgnoreCase(av))
			{
				final FileCmpOptions	opts=getComparisonOptions();
				if (opts.isIgnoreCorruptedFiles())	// default is FALSE
					throw new IllegalStateException(av + " option re-specified");
				opts.setIgnoreCorruptedFiles(true);
			}
			else
				throw new IllegalArgumentException("Unknown option: " + av);
		}

		if ((srcFolder != null) || (dstFolder != null))
			addFilePair(srcFolder, dstFolder);
	}
	/**
	 * @param args original arguments as received by <I>main</I> entry point
	 * @throws Exception if unable to start main frame and application
	 */
	MainFrame (final String ... args) throws Exception
	{
		super(args);

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

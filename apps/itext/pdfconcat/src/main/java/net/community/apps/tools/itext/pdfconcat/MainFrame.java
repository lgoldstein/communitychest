/*
 * 
 */
package net.community.apps.tools.itext.pdfconcat;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import net.community.apps.common.BaseMainFrame;
import net.community.apps.tools.itext.pdfconcat.resources.ResourcesAnchor;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.menu.MenuItemExplorer;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.components.table.file.FilePathCellRenderer;
import net.community.chest.ui.helpers.panel.input.LRFieldWithLabelPanel;
import net.community.chest.ui.helpers.table.TypedTableModel;
import net.community.chest.util.compare.InstancesComparator;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;
import net.community.chest.util.set.SetsUtils;

import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 30, 2009 12:02:36 PM
 */
public final class MainFrame extends BaseMainFrame<ResourcesAnchor>
		implements TableModelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7849360024922547647L;
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
	 * @see net.community.apps.common.MainComponent#getResourcesAnchor()
	 */
	@Override
	public ResourcesAnchor getResourcesAnchor ()
	{
		return ResourcesAnchor.getInstance();
	}

	private InputFilesTable	_tbl;
	protected void loadInputFile (String f)
	{
		if ((f != null) && (f.length() > 0) && (_tbl != null))
			_tbl.addValues(new File(f));
	}
	/*
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	@Override
	public void tableChanged (TableModelEvent e)
	{
		final Object	src=(null == e) ? null : e.getSource();
		if (src instanceof TableModel)
		{
			final int	numRows=((TableModel) src).getRowCount();
			updateTrackedButtons("run", numRows > 0);
			updateTrackedButtons("remove", numRows > 0);
		}
	}

	protected Collection<File> removeSelectedInputFiles ()
	{
		final int[]	idxs=(null == _tbl) ? null : _tbl.getSelectedRows();
		final int	numSel=(null == idxs) ? 0 : idxs.length;
		if (numSel <= 0)
			return null;

		final TypedTableModel<? extends File>	fl=_tbl.getTypedModel();
		final int								numFiles=(null == fl) ? 0 : fl.size();
		Collection<File>						ret=null;
		for (final int i : idxs)
		{
			final int	fIndex=_tbl.convertRowIndexToModel(i);
			final File	f=((fIndex < 0) || (fIndex >= numFiles)) ? null : fl.get(fIndex);
			if (null == f)
				continue;
			if (null == ret)
				ret = SetsUtils.uniqueSetOf(f);
			else
				ret.add(f);
		}

		if ((null == ret) || (ret.size() <= 0))
			return ret;

		for (int	rIndex=0; (fl != null) && (rIndex < fl.size()); rIndex++)
		{
			final File	f=fl.get(rIndex);
			if (!ret.contains(f))
				continue;

			final File	rf=fl.remove(rIndex);
			if (rf != f)	// debug breakpoint
				continue;

			_logger.info("removeSelectedInputFiles(" + f + ")[index=" + rIndex + "]");
			rIndex--;	// compensate for the remove
		}

		if (fl instanceof AbstractTableModel)
			((AbstractTableModel) fl).fireTableDataChanged();
		return ret;
	}

	private ActionListener	_delListener;
	private ActionListener getRemoveActionListener ()
	{
		if (null == _delListener)
			_delListener = new ActionListener () {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent e)
				{
					removeSelectedInputFiles();
				}
			};
		return _delListener;
	}
	/*
	 * @see net.community.apps.common.FilesLoadMainFrame#loadFiles(java.lang.String, org.w3c.dom.Element, java.util.List)
	 */
	@Override
	public void loadFiles (String cmd, Element dlgElement, List<? extends File> fl)
	{
		if ((null == fl) || (fl.size() <= 0) || (null == _tbl))
			return;

		_tbl.addValues(fl);
	}

	private LRFieldWithLabelPanel	_outPanel;
	public String getOutputFile ()
	{
		return (null == _outPanel) ? null : _outPanel.getText();
	}

	public void setOutputFile (String f)
	{
		if ((f != null) && (f.length() > 0) && (_outPanel != null))
			_outPanel.setText(f);
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#saveFile(java.io.File, org.w3c.dom.Element)
	 */
	@Override
	public void saveFile (File f, Element dlgElement)
	{
		setOutputFile((null == f) ? null : f.getAbsolutePath());
	}

	private JLabel	_statusBar	/* =null */;
	public void updateStatusBar (final String text)
	{
		if (_statusBar != null)
			_statusBar.setText((null == text) ? "" : text);
	}

	private static void updateTrackedButtons (final Collection<? extends AbstractButton> bl, final boolean enabled)
	{
		if ((null == bl) || (bl.size() <= 0))
			return;

		for (final AbstractButton b : bl)
		{
			if ((null == b) || (b.isEnabled() == enabled))
				continue;
			b.setEnabled(enabled);
		}
	}

	private Map<String,Collection<AbstractButton>>	_btns;
	private void updateTrackedButtons (final String cmd /* null == all */, final boolean enabled)
	{
		if ((null == _btns) || (_btns.size() <= 0))
			return;

		if ((null == cmd) || (cmd.length() <= 0))
		{
			final Collection<? extends Collection<? extends AbstractButton>>	vl=_btns.values();
			if ((null == vl) || (vl.size() <= 0))
				return;

			for (final Collection<? extends AbstractButton> bl : vl)
				updateTrackedButtons(bl, enabled);
		}
		else
			updateTrackedButtons(_btns.get(cmd), enabled);
	}

	private boolean addTrackedButton (final AbstractButton b)
	{
		final String	cmd=(null == b) ? null : b.getActionCommand();
		if ((null == cmd) || (cmd.length() <= 0) || "exit".equalsIgnoreCase(cmd))
			return false;

		Collection<AbstractButton>	bl=null;
		if (null == _btns)
			_btns = new TreeMap<String,Collection<AbstractButton>>(String.CASE_INSENSITIVE_ORDER);
		else
			bl =_btns.get(cmd);

		if (null == bl)
		{
			bl = SetsUtils.setOf(new InstancesComparator<AbstractButton>(AbstractButton.class), b);
			_btns.put(cmd, bl);
			return true;
		}

		return bl.add(b);
	}

	private void addTrackedButtons (final Map<String,? extends AbstractButton> m)
	{
		final Collection<? extends AbstractButton>	bl=
			((null == m) || (m.size() <= 0)) ? null : m.values();
		if ((null == bl) || (bl.size() <= 0))
			return;

		for (final AbstractButton b : bl)
			addTrackedButton(b);
	}

	private PDFConcatenator	_worker;
	void done (PDFConcatenator w)
	{
		if (w != _worker)
			_logger.warn("Mismatched worker instances");
		if (null == _worker)
			_logger.warn("No current worker instance");
		else
			_worker = null;
		updateTrackedButtons("", true);
		updateStatusBar("Ready.");

		JOptionPane.showMessageDialog(this, "Concatenation complete", "Done", JOptionPane.INFORMATION_MESSAGE);
	}

	protected void executeConcatenation ()
	{
		try
		{
			if (_worker != null)
				throw new IllegalStateException("Already running");

			final String	outFile=getOutputFile();
			final File		oFile=
				((null == outFile) || (outFile.length() <= 0)) ? null : new File(outFile);
			if ((oFile != null) && oFile.exists())
			{
				final int	nRes=JOptionPane.showConfirmDialog(this, "File already exists - overwrite ?", "Overwrite confirmation", JOptionPane.YES_NO_OPTION);
				if (nRes != JOptionPane.YES_OPTION)
					return;
			}

			_worker = new PDFConcatenator(this, (null == _tbl) ? null : _tbl.getInputFiles(), outFile);
			updateTrackedButtons("", false);
			_worker.execute();
		}
		catch(Exception e)
		{
			_logger.error("executeConcatenation()" + e.getClass().getName() + ": " + e.getMessage(), e);
			BaseOptionPane.showMessageDialog(this, e);
		}
	}

	private ActionListener	_runListener;
	private ActionListener getRunActionListener ()
	{
		if (null == _runListener)
			_runListener = new ActionListener () {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent e)
				{
					executeConcatenation();
				}
			};
		return _runListener;
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#setToolBarHandlers(javax.swing.JToolBar)
	 */
	@Override
	protected Map<String,AbstractButton> setToolBarHandlers (final JToolBar b)
	{
		final Map<String,AbstractButton>	ret=super.setToolBarHandlers(b);
		addTrackedButtons(ret);
		return ret;
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#setMainMenuItemsActionHandlers(net.community.chest.swing.component.menu.MenuItemExplorer)
	 */
	@Override
	protected Map<String,JMenuItem> setMainMenuItemsActionHandlers (final MenuItemExplorer ie)
	{
		final Map<String,JMenuItem>	ret=super.setMainMenuItemsActionHandlers(ie);
		addTrackedButtons(ret);
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
		lm.put(LOAD_CMD, getLoadFileListener());
		lm.put(SAVE_CMD, getSaveFileListener());
		lm.put("run", getRunActionListener());
		lm.put("remove", getRemoveActionListener());
		lm.put(EXIT_CMD, getExitActionListener());
		lm.put(ABOUT_CMD, getShowManifestActionListener());

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

		if (null == _tbl)
		{
			_tbl = new InputFilesTable();
			_tbl.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			_tbl.setDefaultRenderer(File.class, new FilePathCellRenderer());
			_tbl.addTableModelListener(this);
			ctPane.add(new ScrolledComponent<JTable>(_tbl), BorderLayout.CENTER);

			setDropTarget(new DropTarget(_tbl, this));
		}

		if (null == _outPanel)
		{
			_outPanel = new LRFieldWithLabelPanel();
			_outPanel.setTitle("Output file:");
		}

		if (null == _statusBar)
		{
			_statusBar = new JLabel("Ready.");
			_statusBar.setHorizontalAlignment(SwingConstants.LEFT);
			_statusBar.setHorizontalTextPosition(SwingConstants.LEFT);
		}

		final JPanel	southPanel=new JPanel(new GridLayout(0, 1, 0, 5));
		southPanel.add(_outPanel);
		southPanel.add(_statusBar);
		ctPane.add(southPanel, BorderLayout.SOUTH);
	}

	private void processMainArguments (final String ... args) throws Exception
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		String		outFile=null;
		for (int	aIndex=0; aIndex < numArgs; aIndex++)
		{
			final String	av=args[aIndex];
			if ((null == av) || (av.length() <= 0))
				continue;

			if ("-i".equalsIgnoreCase(av))
			{
				aIndex++;
				loadInputFile(args[aIndex]);
			}
			else if ("-o".equalsIgnoreCase(av))
			{
				if ((outFile != null) && (outFile.length() > 0))
					throw new IllegalStateException(av + " option re-specified");

				aIndex++;
				outFile = args[aIndex];
				setOutputFile(outFile);
			}
			else
				throw new IllegalArgumentException("Unknown option: " + av);
		}
	}

	public MainFrame (String... args) throws Exception
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

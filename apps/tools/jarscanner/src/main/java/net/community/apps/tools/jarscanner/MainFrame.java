package net.community.apps.tools.jarscanner;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import net.community.chest.awt.attributes.Textable;
import net.community.chest.lang.SysPropsEnum;
import net.community.chest.regexp.RegexpUtils;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.component.table.DefaultTableScroll;
import net.community.chest.ui.components.text.AutoCompleter;
import net.community.chest.ui.components.text.FolderAutoCompleter;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 21, 2007 7:50:20 AM
 */
final class MainFrame extends JFrame implements Textable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1053988419867320215L;

	private static final LoggerWrapper	_logger=WrapperFactoryManager.getLogger(MainFrame.class);

    public static final Dimension DEFAULT_INITIAL_SIZE=new Dimension(400, 500);
    protected Dimension getInitialSize ()
    {
    	return DEFAULT_INITIAL_SIZE;
    }

    public static final Insets	COMMON_INSETS=new Insets(5,5,5,5);
    private static JPanel createDataPanel (final Component lbl, final Component txt)
    {
    	final JPanel				pnl=new JPanel(new GridBagLayout());
    	final GridBagConstraints	gbc=new GridBagConstraints();

    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.insets = COMMON_INSETS;
    	gbc.anchor = GridBagConstraints.LINE_START;
    	pnl.add(lbl, gbc);

    	gbc.gridx = 1;
    	gbc.anchor = GridBagConstraints.LINE_START;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.gridwidth = GridBagConstraints.REMAINDER;
    	gbc.weightx = 1.0;
    	pnl.add(txt, gbc);

    	return pnl;
    }

    private AutoCompleter<?>	_jarsFolder	/* =null */;
    public File getJarsFolder ()
    {
    	if (null == _jarsFolder)
    		return null;

    	final String	fPath=_jarsFolder.getText();
    	if ((null == fPath) || (fPath.length() <= 0))
    		return null;

    	try
    	{
    		final File	f=new File(fPath);
    		if (f.exists() && f.isDirectory())
    			return f;
    	}
    	catch(RuntimeException e)
    	{
    		_logger.warn(e.getClass().getName() + " while checking path=" + fPath + ": " + e.getMessage());
    	}

    	return null;
    }

    public void setJarsFolder (final File f)
    {
    	if ((null == f) || (!f.exists()) || (!f.isDirectory()) || (null == _jarsFolder))
    		return;
    	_jarsFolder.setText(f.getAbsolutePath());
    }

    private static Collection<Pattern> getPatternsList (final JTextComponent tc)
    {
    	final String	scanPattern=(tc == null) ? null : tc.getText();
    	try
    	{
    		return (null == scanPattern) ? null : RegexpUtils.getPatternsList(scanPattern, ',');
    	}
    	catch(RuntimeException e)
    	{
    		return _logger.warnObject(e.getClass().getName() + " while checking pattern=" + scanPattern + ": " + e.getMessage(), e, null);
    	}
    }

    private static final void setPatternsList (final JTextComponent tc, final String p)
    {
    	if ((null == p) || (p.length() <= 0) || (null == tc))
    		return;
    	tc.setText(p);
    }

    private JTextField	_incScanPattern	/* =null */;
    public Collection<Pattern> getIncludedScanPattern ()
    {
    	return getPatternsList(_incScanPattern);
    }

    public void setIncludedScanPattern (final String p)
    {
    	setPatternsList(_incScanPattern, p);
    }

    private JTextField	_excScanPattern	/* =null */;
    public Collection<Pattern> getExcludedScanPattern ()
    {
    	return getPatternsList(_excScanPattern);
    }

    public void setExcludedScanPattern (final String p)
    {
    	setPatternsList(_excScanPattern, p);
    }

    private JTextField	_excJarsPattern;
    public Collection<Pattern> getJarsExcludePattern ()
    {
    	return getPatternsList(_excJarsPattern);
    }

    public void setJarsExcludePattern (final String p)
    {
    	setPatternsList(_excJarsPattern, p);
    }

    private JTextField	_incJarsPattern;
    public Collection<Pattern> getJarsIncludePattern ()
    {
    	return getPatternsList(_incJarsPattern);
    }

    public void setJarsIncludePattern (final String p)
    {
    	setPatternsList(_incJarsPattern, p);
    }

    private JTextField	_excDirsPattern;
    public Collection<Pattern> getDirExcludePattern ()
    {
    	return getPatternsList(_excDirsPattern);
    }

    public void setDirExcludePattern (final String p)
    {
    	setPatternsList(_excDirsPattern, p);
    }

    private JTextField	_incDirsPattern;
    public Collection<Pattern> getDirIncludePattern ()
    {
    	return getPatternsList(_incDirsPattern);
    }

    public void setDirIncludePattern (final String p)
    {
    	setPatternsList(_incDirsPattern, p);
    }

    private static final Map.Entry<JPanel,JTextField> createPatternsPanel (
			final JTextField curField, final String title)
	{
		if (curField != null)
			throw new IllegalStateException("createPatternsPanel(" + title + ") field already initialized");

		final JTextField	pf=new JTextField("");
		final JPanel		p=createDataPanel(new JLabel(title), pf);
		return new MapEntryImpl<JPanel,JTextField>(p, pf);
	}

    private final JPanel getIncludedScanPatternPanel ()
    {
    	final Map.Entry<JPanel,JTextField>	pp=
    		createPatternsPanel(_incScanPattern, "Included entry patterns");
    	if (null == (_incScanPattern=(null == pp) ? null : pp.getValue()))
    		throw new IllegalStateException("getClassPatternPanel() no text field generated");

    	_incScanPattern.addKeyListener(new KeyAdapter() {
			/*
			 * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
			 */
			@Override
			public void keyTyped (KeyEvent e)
			{
				if (updateScanButtonState()
				 && (e != null)
				 && (KeyEvent.VK_ENTER == e.getKeyChar()))
					scanJARs();
			}
    	});

    	return pp.getKey();
    }

    private final JPanel getExcludedScanPatternPanel ()
    {
    	final Map.Entry<JPanel,JTextField>	pp=
    		createPatternsPanel(_excScanPattern, "Excluded entry patterns");
    	if (null == (_excScanPattern=(null == pp) ? null : pp.getValue()))
    		throw new IllegalStateException("getExcludedScanPatternPanel() no text field generated");

    	return pp.getKey();
    }

    private final JPanel getJarsExcludePatternPanel ()
    {
    	final Map.Entry<JPanel,JTextField>	pp=
    		createPatternsPanel(_excJarsPattern, "Jar(s) exclude patterns");
    	if (null == (_excJarsPattern=(null == pp) ? null : pp.getValue()))
    		throw new IllegalStateException("getJarsExcludePatternPanel() no text field generated");

    	return pp.getKey();
    }

    private final JPanel getJarsIncludePatternPanel ()
    {
    	final Map.Entry<JPanel,JTextField>	pp=
    		createPatternsPanel(_incJarsPattern, "Jar(s) include patterns");
    	if (null == (_incJarsPattern=(null == pp) ? null : pp.getValue()))
    		throw new IllegalStateException("getJarsIncludePatternPanel() no text field generated");

    	return pp.getKey();
    }

    private final JPanel getDirsExcludePatternPanel ()
    {
    	final Map.Entry<JPanel,JTextField>	pp=
    		createPatternsPanel(_excDirsPattern, "Folder(s) exclude patterns");
    	if (null == (_excDirsPattern=(null == pp) ? null : pp.getValue()))
    		throw new IllegalStateException("getDirsExcludePatternPanel() no text field generated");
    	_excDirsPattern.setText(".svn");

    	return pp.getKey();
    }

    private final JPanel getDirsIncludePatternPanel ()
    {
    	final Map.Entry<JPanel,JTextField>	pp=
    		createPatternsPanel(_incDirsPattern, "Folder(s) include patterns");
    	if (null == (_incDirsPattern=(null == pp) ? null : pp.getValue()))
    		throw new IllegalStateException("getDirsIncludePatternPanel() no text field generated");

    	return pp.getKey();
    }

    private JarEntriesTableModel	_tblModel	/* =null */;
    private ScrolledComponent<? extends JTable> getResultsPanel ()
    {
    	if (_tblModel != null)
    		throw new IllegalStateException("getResultsPanel() already initialized");
 
    	_tblModel = new JarEntriesTableModel();

    	final JarEntriesTableColInfo[]	cols={
    			new JarEntriesTableColInfo(JarEntriesTableColumns.JAR_PATH, "File", 30),
    			new JarEntriesTableColInfo(JarEntriesTableColumns.ENTRY_PATH, "Path", 45),
    			new JarEntriesTableColInfo(JarEntriesTableColumns.ENTRY_NAME, "Name", 25)
    	};
		// NOTE: columns must be added BEFORE model is attached to a table
    	for (final JarEntriesTableColInfo c : cols)
    	{
    		if (null == c)
    			continue;

    		_tblModel.addColumn(c);
    	}

    	final Dimension	d=getInitialSize();
    	final int		maxWidth=(int) d.getWidth();
    	_tblModel.adjustRelativeColWidths(maxWidth);

    	final JTable	tbl=new JTable(_tblModel);
    	tbl.setAutoCreateRowSorter(true);

		return new DefaultTableScroll(tbl);
    }

    private JButton	_scanButton	/* =null */;
    protected boolean updateScanButtonState ()
    {
    	if (null == _scanButton)
    		return false;
  
    	final boolean	isOKPattern=
    		   (getExcludedScanPattern() != null)
    		|| (getIncludedScanPattern() != null)
    		|| (getJarsExcludePattern() != null)
    		|| (getJarsIncludePattern() != null)
    		|| (getDirExcludePattern() != null)
    		|| (getDirIncludePattern() != null)
    		;
    	_scanButton.setEnabled(isOKPattern);
    	return isOKPattern;
    }

    private JButton	_stopButton	/* =null */;
    private final JPanel getButtonsPanel ()
    {
    	_scanButton = new JButton("Scan JAR(s)");
		_scanButton.addActionListener(new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent e)
				{
					scanJARs();
				}
			});
		updateScanButtonState();

		_stopButton = new JButton("Stop");
		_stopButton.setEnabled(false);
		_stopButton.addActionListener(new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent e)
				{
					stopScanning();
				}
			});
		
    	final JPanel	p=new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    	p.add(_scanButton);
    	p.add(_stopButton);
    	return p;
    }

    private void updateRunningState (final boolean isRunning)
    {
    	final JComponent[]	ca={
    			_incScanPattern,
    			_excScanPattern,
    			_excJarsPattern,
    			_incJarsPattern,
    			_excDirsPattern,
    			_incDirsPattern,
    			_scanButton,
    			_selFolderButton,
    			_resTable
    		};
    	for (final JComponent jc : ca)
    	{
    		if (null == jc)
    			continue;
    		jc.setEnabled(!isRunning);
    	}

    	if (_jarsFolder != null)
    		_jarsFolder.setEnabled(!isRunning);
    	if (_stopButton != null)
    		_stopButton.setEnabled(isRunning);
    }

    private JarEntriesTablePopulator	_popl	/* =null */;
    protected void stopScanning ()
    {
    	if (_popl != null)
    	{
    		if (_popl.cancel(false))
    			_logger.info("signalled populator stop");
    		else
    			_logger.warn("failed to signal populator stop");
    	}
    }

	void signalSearchDone (JarEntriesTablePopulator p)
	{
		if (_popl == p)
		{
			_popl = null;
			updateRunningState(false);
			setText("Ready");
		}
		else
			_logger.error("signalSearchDone() " + " mismatched " + JarEntriesTablePopulator.class.getSimpleName() + " instance");
	}

	protected void scanJARs ()
    {
		try
		{
			if (_popl != null)
				throw new IllegalStateException("Scan already in progress");
			_tblModel.clear();

			final File	fldr=getJarsFolder();
			_popl = new JarEntriesTablePopulator(this);
			_popl.setScanDir(fldr);
			_popl.setModel(_tblModel);
			_popl.setIncludedScanPattern(getIncludedScanPattern());
			_popl.setExcludedScanPattern(getExcludedScanPattern());
			_popl.setJarsIncludePattern(getJarsIncludePattern());
			_popl.setJarsExcludePattern(getJarsExcludePattern());
			_popl.setDirExcludePattern(getDirExcludePattern());
			_popl.setDirIncludePattern(getDirIncludePattern());

			updateRunningState(true);
			_popl.execute();
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
		}
    }

    private JButton	_selFolderButton	/* =null */;
    protected void selectJARsFolder ()
    {
    	final File			jarsFolder=getJarsFolder();
    	final JFileChooser	fc=(null == jarsFolder) ? new JFileChooser() : new JFileChooser(jarsFolder);
    	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(this))
    	{
    		final File	selFolder=fc.getSelectedFile();	// can be null if user did not select anything
    		if ((selFolder != null) && selFolder.exists() && selFolder.isDirectory())
    		{
    			final String	selPath=selFolder.getAbsolutePath();
    			_jarsFolder.setText(selPath);
    			updateScanButtonState();
    		}
    		else
    			_logger.error("Selected path(" + selFolder + ") does not exist or is not a directory");
    	}
    }

    private final JPanel getFolderSelectionPanel ()
    {
    	if ((_jarsFolder != null) || (_selFolderButton != null))
    		throw new IllegalStateException("Folder selection panel already initialized");

    	_jarsFolder = new FolderAutoCompleter<JTextField>(new JTextField());
    	final String	curDir=SysPropsEnum.USERDIR.getPropertyValue();
    	if ((curDir != null) && (curDir.length() > 0))
    		_jarsFolder.setText(curDir);

    	_selFolderButton = new JButton("Folder");
    	_selFolderButton.addActionListener(new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent e)
				{
					selectJARsFolder();
				}
    		});

    	return createDataPanel(_selFolderButton, _jarsFolder.getTextComponent());
    }
    /**
     * Default title of the application
     */
    public static final String	DEFAULT_TITLE="JAR(s) Scanner";

    private final JLabel	_statusBar=new JLabel("");
    /*
	 * @see net.community.chest.awt.attributes.Textable#getText()
	 */
	@Override
	public String getText ()
	{
		return (null == _statusBar) ? null : _statusBar.getText();
	}
	/*
	 * @see net.community.chest.awt.attributes.Textable#setText(java.lang.String)
	 */
	@Override
	public void setText (String t)
	{
    	if (_statusBar != null)
    		_statusBar.setText((null == t) ? "" : t);
	}

	private ScrolledComponent<? extends JTable>	_resTable;
    MainFrame ()
	{
		super(DEFAULT_TITLE);

		final Container	ctPane=getContentPane();	
		ctPane.setLayout(new BorderLayout(5,5));
		// close the application if frame closed
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		// make the frame appear in mid-screen by default
		setLocationRelativeTo(null);

		// give some initial size information
		final Dimension	dim=getInitialSize();
		setPreferredSize(dim);
		setSize(dim);

		{
			final JPanel	northPanel=new JPanel(new GridLayout(0, 1, 0, 0));
			northPanel.add(getFolderSelectionPanel());
			northPanel.add(getDirsIncludePatternPanel());
			northPanel.add(getDirsExcludePatternPanel());
			northPanel.add(getJarsIncludePatternPanel());
			northPanel.add(getJarsExcludePatternPanel());
			northPanel.add(getIncludedScanPatternPanel());
			northPanel.add(getExcludedScanPatternPanel());
			
			northPanel.add(getButtonsPanel());
			ctPane.add(northPanel, BorderLayout.NORTH);
		}

		if ((_resTable=getResultsPanel()) != null)
			ctPane.add(_resTable, BorderLayout.CENTER);

		ctPane.add(_statusBar, BorderLayout.SOUTH);
	}
}

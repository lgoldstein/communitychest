package net.community.apps.eclipse.cparrange;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URL;
import java.util.MissingResourceException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.community.chest.awt.attributes.Enabled;
import net.community.chest.lang.SysPropsEnum;
import net.community.chest.swing.component.button.BaseButton;
import net.community.chest.swing.component.filechooser.FileSelectionMode;
import net.community.chest.swing.component.frame.BaseFrame;
import net.community.chest.swing.component.text.BaseTextField;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Nov 22, 2007 2:11:48 PM
 */
final class MainFrame extends BaseFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6812409172357653538L;

	private static final LoggerWrapper	_logger=WrapperFactoryManager.getLogger(MainFrame.class);

	public static final Dimension DEFAULT_INITIAL_SIZE=new Dimension(480, 150);
    protected Dimension getInitialSize ()
    {
    	return DEFAULT_INITIAL_SIZE;
    }

    private static final File extractFilePath (final JTextField txField)
    {
    	final String	filePath=(null == txField) ? null : txField.getText();
    	if ((null == filePath) || (filePath.length() <= 0))
    		return null;

    	return new File(filePath);
    }

    private static final File extractFilePath (final JTextField txField, final boolean checkDirectory)
    {
    	final File	f=extractFilePath(txField);
    	if (null == f)
    		return null;

    	try
    	{
    		if (f.exists())
    		{
    			if (f.isFile())
    				return f;
    			else
    				return null;
    		}
  
    		if (checkDirectory)
    		{
    			final File	d=f.getParentFile();
    			if ((d != null) && d.exists() && d.isDirectory())
    				return d;
    		}
    	}
    	catch(Exception e)
    	{	
    		// ignored
    	}

    	return null;
    }

    private static MainFrame	_instance;
    public static final synchronized MainFrame getMainFrameInstance ()
    {
    	return _instance;
    }

    private TransformerWorker	_worker	/* =null */;
    private TransformerWorker getTransformerWorker ()
    {
    	return _worker;
    }

    private BaseTextField	_srcFileField	/* =null */, _dstFileField	/* =null */;
    private void updateRunningState (final boolean running)
    {
    	final Enabled[]	comps={ _srcFileField, _srcLoadBtn, _dstFileField, _dstLoadBtn	};
    	for (final Enabled c : comps)
    	{
    		if (c != null)
    			c.setEnabled(!running);
    	}
    }
    // used to inform when done
    void setTransformerWorker (TransformerWorker w)
    {
    	if (w == _worker)
    		_worker = null;
    	else
    		_worker = w;

    	updateRunningState(_worker != null);
    }
    
    protected void transformFiles ()
    {
    	TransformerWorker	w=getTransformerWorker();
    	if (w != null)
    	{
    		JOptionPane.showMessageDialog(this, "Stop current transformation first.", "Transformation in progress", JOptionPane.ERROR_MESSAGE);
    		return;
    	}

    	try
    	{
        	final boolean	recScan=isRecursiveScan();
        	final File		srcFile=recScan ? extractFilePath(_srcFileField) : extractFilePath(_srcFileField, false);
        	final String	dstPath=_dstFileField.getText();
       	    // TODO allow for REGEX as filename
        	w = new TransformerWorker(this, srcFile, recScan ? ".classpath" : dstPath, recScan, recScan ? _dstFileField : null);
        	setTransformerWorker(w);
        	w.execute();
    	}
    	catch(Exception e)
    	{
			BaseOptionPane.showMessageDialog(this, e);
    	}
    }

    private JButton	_execButton	/* =null */;
    private final JButton getExecutionButton ()
    {
    	_execButton = new JButton("Execute");
    	_execButton.addActionListener(new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent e)
				{
					transformFiles();
				}
    		});

    	return _execButton;
    }

    private final JPanel getExecutionPanel ()
    {
    	final JPanel	pnl=new JPanel(new BorderLayout(5, 0));

    	final JButton	exeBtn=getExecutionButton();
    	pnl.add(exeBtn, BorderLayout.EAST);

    	final JCheckBox	cbMode=getRecursionOption();
    	pnl.add(cbMode, BorderLayout.WEST);

    	return pnl;
    }

    private JCheckBox	_cbRecursive	/* =null */;
    public boolean isRecursiveScan ()
    {
    	return (_cbRecursive != null) && _cbRecursive.isSelected();
    }

    protected boolean updateExecutionButtonState ()
    {
    	if (null == _execButton)
    		return false;

    	final boolean	enabled;
    	if (isRecursiveScan())
    	{
    		final File	f=extractFilePath(_srcFileField);
    		enabled = (f != null) && f.exists() && f.isDirectory();
    	}
    	else
    		enabled = (extractFilePath(_srcFileField, false) != null)
    			   && (extractFilePath(_dstFileField, true) != null)
    			   ;

    	_execButton.setEnabled(enabled);
    	return enabled;
    }

    public static final Insets	COMMON_INSETS=new Insets(5,5,5,5);
    private static JPanel createDataPanel (final JTextField txt, final JButton btn)
    {
    	final JPanel				pnl=new JPanel(new GridBagLayout());
    	final GridBagConstraints	gbc=new GridBagConstraints();

    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.insets = COMMON_INSETS;
    	gbc.anchor = GridBagConstraints.LINE_START;
    	pnl.add(btn, gbc);

    	gbc.gridx = 1;
    	gbc.anchor = GridBagConstraints.LINE_START;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.gridwidth = GridBagConstraints.REMAINDER;
    	gbc.weightx = 1.0;
    	pnl.add(txt, gbc);

    	return pnl;
    }

    private File loadFile (final JTextField txField, final boolean checkDirectory)
    {
    	File dirLoad=extractFilePath(txField, checkDirectory);
    	if (null == dirLoad)
    		dirLoad = new File(SysPropsEnum.USERDIR.getPropertyValue());
    	if (!dirLoad.isDirectory())
    		dirLoad = dirLoad.getParentFile();

    	final JFileChooser	fc=new JFileChooser(dirLoad);
    	fc.setDialogTitle("Choose '.classpath' file");
    	fc.setFileSelectionMode(FileSelectionMode.FILES.getModeValue());

    	final int	nRes=fc.showOpenDialog(this);
    	final File	file;
		if (JFileChooser.APPROVE_OPTION == nRes)
		{
			if ((file=fc.getSelectedFile()) != null)	// can happen if user did not select anything
				txField.setText(file.getAbsolutePath());
		}
		else	// just so we have a debug breakpoint
			file = null;

		updateExecutionButtonState();
		return file;
    }

    protected void loadDestinationFile ()
    {
    	loadFile(_dstFileField, true);
    }

    private class FileFieldKeyListener implements KeyListener {
    	protected FileFieldKeyListener ()
    	{
    		super();
    	}
		/*
		 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
		 */
		@Override
		public void keyPressed (KeyEvent e)
		{
			updateExecutionButtonState();
		}
		/*
		 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
		 */
		@Override
		public void keyReleased (KeyEvent e)
		{
			updateExecutionButtonState();
		}
		/*
		 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
		 */
		@Override
		public void keyTyped (KeyEvent e)
		{
			updateExecutionButtonState();
		}
    }

    private KeyListener	_ffl	/* =null */;
    private final KeyListener getFileFieldKeyListener ()
    {
    	if (null == _ffl)
    		_ffl = new FileFieldKeyListener();
    	return _ffl;
    }

    private BaseButton	_dstLoadBtn	/* =null */;
    private final JPanel getDstFilePanel ()
    {
    	final ActionListener	ldListener=new ActionListener() {
			/*
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed (ActionEvent e)
			{
				loadDestinationFile();
			}
		};

		_dstFileField = new BaseTextField();
		_dstFileField.addActionListener(ldListener);
		_dstFileField.addKeyListener(getFileFieldKeyListener());

		_dstLoadBtn = new BaseButton("Load destination file");
		_dstLoadBtn.addActionListener(ldListener);

    	return createDataPanel(_dstFileField, _dstLoadBtn);
    }

    private final File loadDirectory (final JTextField txField)
    {
    	File dirLoad=extractFilePath(txField);
    	if (null == dirLoad)
    		dirLoad = new File(SysPropsEnum.USERDIR.getPropertyValue());
    	if (!dirLoad.isDirectory())
    		dirLoad = dirLoad.getParentFile();

    	final JFileChooser	fc=new JFileChooser(dirLoad);
    	fc.setDialogTitle("Choose root folder to scan");
    	fc.setFileSelectionMode(FileSelectionMode.FOLDERS.getModeValue());

    	final int	nRes=fc.showOpenDialog(this);
    	final File	file;
		if (JFileChooser.APPROVE_OPTION == nRes)
		{
			if ((file=fc.getSelectedFile()) != null)	// can happen if user did not select anything
				txField.setText(file.getAbsolutePath());
		}
		else	// just so we have a debug breakpoint
			file = null;

		updateExecutionButtonState();
		return file;
    }

    protected void loadSourceFile ()
    {
    	if (isRecursiveScan())
    	{
    		loadDirectory(_srcFileField);
    	}
    	else
    	{
	    	final File	srcFile=loadFile(_srcFileField, false);
	    	if ((srcFile != null) && srcFile.exists() && srcFile.isFile() && (_dstFileField != null))
	    	{
	    		_dstFileField.setText(srcFile.getAbsolutePath());
	    		updateExecutionButtonState();
	    	}
    	}
    }

    protected void updateWorkingMode ()
    {
    	final boolean	dstAllowed=(!isRecursiveScan());
    	_dstLoadBtn.setEnabled(dstAllowed);
    	_dstFileField.setEnabled(dstAllowed);

    	updateExecutionButtonState();
    }

    private final JCheckBox getRecursionOption ()
    {
    	_cbRecursive = new JCheckBox("Recursive scan");
    	_cbRecursive.addChangeListener(new ChangeListener() {
    		/*
    		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
    		 */
    		@Override
			public void stateChanged (final ChangeEvent event)
    		{
    			final Object	src=(null == event) ? null : event.getSource();
    			if (src instanceof JCheckBox)
    				updateWorkingMode();
    		}
    	});

    	return _cbRecursive;
    }

    private BaseButton	_srcLoadBtn	/* =null */;
    private final JPanel getSrcFilePanel ()
    {
    	final ActionListener	ldListener=new ActionListener() {
			/*
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed (ActionEvent e)
			{
				loadSourceFile();
			}
		};

		_srcFileField = new BaseTextField();
		_srcFileField.addActionListener(ldListener);
		_srcFileField.addKeyListener(getFileFieldKeyListener());

    	_srcLoadBtn = new BaseButton("Load source file");
    	_srcLoadBtn.addActionListener(ldListener);

    	return createDataPanel(_srcFileField, _srcLoadBtn);
    }

    private void processArguments (final String ... args)
    {
    	final int		numArgs=(null == args) ? 0 : args.length;
    	final String	srcPath=(numArgs > 0) ? args[0] : null,
    					dstPath=(numArgs > 1) ? args[1] : srcPath;
    	if ((srcPath != null) && (srcPath.length() > 0))
    	{
    		_srcFileField.setText(srcPath);
    		_dstFileField.setText(srcPath);	// default
    	}

    	if ((dstPath != null) && (dstPath.length() > 0))
    		_dstFileField.setText(dstPath);
    }
    /**
     * Default title of the application
     */
    public static final String	DEFAULT_TITLE="Eclipse classpath transformer";

    MainFrame (final String ... args)
	{
		super(DEFAULT_TITLE);

		URL	url=null;
		try
		{
			if (null == (url=Arranger.ARRANGER.getDefaultIcon()))
				throw new MissingResourceException("no icon", getClass().getName(), Icon.class.getSimpleName());

			final ImageIcon	icon=new ImageIcon(url);
			setIconImage(icon.getImage());
		}
		catch(Exception e)
		{
			_logger.error(e.getClass().getName() + " while load icon=" + url + ": " + e.getMessage(), e);
		}

		final Container	ctPane=getContentPane();
		ctPane.setLayout(new GridLayout(3, 1, 5, 5));
		// close the application if frame closed
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		// make the frame appear in mid-screen by default
		setLocationRelativeTo(null);

		// give some initial size information
		final Dimension	dim=getInitialSize();
		setPreferredSize(dim);
		setSize(dim);

		ctPane.add(getSrcFilePanel());
		ctPane.add(getDstFilePanel());
		ctPane.add(getExecutionPanel());
		setFocusTraversalPolicy(_srcFileField, _dstFileField, _cbRecursive, _execButton);

		processArguments(args);
		updateExecutionButtonState();

		synchronized(MainFrame.class)
		{
			if (_instance != null)
				throw new IllegalStateException("Multiple MainFrame(s) registered");
			_instance = this;
		}
	}
}

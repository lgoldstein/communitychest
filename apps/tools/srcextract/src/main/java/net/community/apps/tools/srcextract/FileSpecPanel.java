package net.community.apps.tools.srcextract;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.community.chest.lang.SysPropsEnum;
import net.community.chest.swing.component.filechooser.FileSelectionMode;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Nov 25, 2007 11:14:23 AM
 */
class FileSpecPanel extends JPanel implements ActionListener, KeyListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4616647765595007533L;
	public static final Insets	COMMON_INSETS=new Insets(5,5,5,5);
    private static JPanel createDataPanel (final JTextField txt, final JComponent comp)
    {
    	final JPanel				pnl=new JPanel(new GridBagLayout());
    	final GridBagConstraints	gbc=new GridBagConstraints();

    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.insets = COMMON_INSETS;
    	gbc.anchor = GridBagConstraints.LINE_START;
    	pnl.add(comp, gbc);

    	gbc.gridx = 1;
    	gbc.anchor = GridBagConstraints.LINE_START;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.gridwidth = GridBagConstraints.REMAINDER;
    	gbc.weightx = 1.0;
    	pnl.add(txt, gbc);

    	return pnl;
    }

    private final Collection<FileSpecChangeListener>	_clList=new LinkedList<FileSpecChangeListener>();
    public void addChangeListener (FileSpecChangeListener l)
    {
    	if (l != null)
    		_clList.add(l);
    }

    public boolean removeChangeListener (FileSpecChangeListener l)
    {
    	if (l != null)
    		return _clList.remove(l);
    	else
    		return false;
    }

    protected void signalSelectionChanged ()
    {
    	if ((null == _clList) || (_clList.size() <= 0))
    		return;

    	for (final FileSpecChangeListener l : _clList)
    	{
    		if (l != null)
    			l.handleSelectionChanged(this);
    	}
    }

    private final JTextField	_fileNameField, _pkgNameField;
    public String getFileName ()
    {
    	return (null == _fileNameField) ? null : _fileNameField.getText();
    }

    public void setFileName (String name)
    {
    	_fileNameField.setText((null == name) ? "" : name);
    }
    /*
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed (ActionEvent e)
	{
		final String	curName=getFileName();
		File			curFile=((null == curName) || (curName.length() <= 0)) ? null : new File(curName);
		if (curFile != null)
		{
			if (curFile.exists())
			{
				if (curFile.isFile())
					curFile = curFile.getParentFile();
			}
			else
				curFile = curFile.getParentFile();
		}

		if ((null == curFile) || (!curFile.exists()) || (!curFile.isDirectory()))
			curFile = new File(SysPropsEnum.USERDIR.getPropertyValue());

    	final JFileChooser	fc=new JFileChooser(curFile);
    	fc.setDialogTitle("Choose '.jar' file");
    	fc.setFileSelectionMode(FileSelectionMode.FILES.getModeValue());
    	fc.setFileFilter(JarFilesFilter.DEFAULT);

    	final int	nRes=fc.showOpenDialog(this);
    	if (nRes != JFileChooser.APPROVE_OPTION)
    		return;

    	final File		jf=fc.getSelectedFile();	// can be null if user did not select anything
    	final String	jfPath=(null == jf) ? null : jf.getAbsolutePath();
    	if ((jfPath != null) && (jfPath.length() > 0))	// should not be otherwise
    	{
    		_fileNameField.setText(jfPath);
    		signalSelectionChanged();
    	}
	}

	public String getPackageName ()
    {
    	return (null == _pkgNameField) ? null : _pkgNameField.getText();
    }

	public void setPackageName (String name)
	{
		_pkgNameField.setText((null == name) ? "" : name);
	}
	/*
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyPressed (KeyEvent e)
	{
		signalSelectionChanged();
	}
	/*
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyReleased (KeyEvent e)
	{
		signalSelectionChanged();
	}
	/*
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyTyped (KeyEvent e)
	{
		signalSelectionChanged();
	}

	FileSpecPanel (final String btnName, final String rootName)
	{
		super(new GridLayout(2, 1, 5, 5));

		_fileNameField = new JTextField();
//		_filenameField.addActionListener(this);
		_fileNameField.addKeyListener(this);

		final JButton	ldFileBtn=new JButton(btnName);
		ldFileBtn.addActionListener(this);
		add(createDataPanel(_fileNameField, ldFileBtn));

		_pkgNameField = new JTextField();
		_pkgNameField.addKeyListener(this);
		add(createDataPanel(_pkgNameField, new JLabel(rootName)));
	}
	/*
	 * @see java.awt.Component#toString()
	 */
	@Override
	public String toString ()
	{
		return getFileName() + "[" + getPackageName() + "]";
	}
}

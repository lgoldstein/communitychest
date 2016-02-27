/*
 * 
 */
package net.community.chest.ui.helpers.panel.input;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeListener;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.swing.event.ChangeListenerSet;
import net.community.chest.ui.helpers.filechooser.HelperFileChooser;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>The default action for the button is to select a file for <U>loading</U>
 * and once selected, update the text filed with its path</P>
 * 
 * @author Lyor G.
 * @since Dec 30, 2008 12:51:04 PM
 */
public class FileInputTextPanel extends LRFieldWithButtonPanel
		implements ActionListener, TypedComponentAssignment<File> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8454738809918260017L;
	public FileInputTextPanel (Element elem, boolean autoLayout)
	{
		super(elem, autoLayout);
	}

	public FileInputTextPanel (boolean autoLayout)
	{
		this(null, autoLayout);
	}

	public FileInputTextPanel ()
	{
		this(true);
	}

	public FileInputTextPanel (Element elem) throws Exception
	{
		this(elem, true);
	}
	/*
	 * @see net.community.chest.ui.helpers.panel.input.LRFieldWithButtonPanel#getPanelConverter(org.w3c.dom.Element)
	 */
	@Override
	protected XmlProxyConvertible<?> getPanelConverter (Element elem)
	{
		return (null == elem) ? null : FileInputTextPanelReflectiveProxy.FILEINPTXTPNL;
	}
	/*
	 * @see net.community.chest.ui.helpers.panel.input.LRFieldWithButtonPanel#layoutComponent(javax.swing.JButton, javax.swing.JTextField)
	 */
	@Override
	protected void layoutComponent (final JButton b, final JTextField f)
	{
		super.layoutComponent(b, f);

		final JButton	selBtn=getButton();
		if (null == selBtn)
			return;

		selBtn.addActionListener(this);
	}

	private JFileChooser	_fc	/* =null */;
	protected JFileChooser getFileChooser (final boolean createIfNotExist)
	{
		if ((null == _fc) && createIfNotExist)
			_fc = new HelperFileChooser();
		return _fc;
	}

	public JFileChooser getFileChooser ()
	{
		return getFileChooser(false);
	}

	public void setFileChooser (JFileChooser fc)
	{
		if (_fc != fc)
			_fc = fc;
	}

	private File	_accFolder; 
	public File getCurrentAccessFolder ()
	{
		return _accFolder;
	}

	public void setCurrentAccessFolder (File f)
	{
		if (_accFolder != f)
		{
			if ((f != null) && (_accFolder != null) && f.equals(_accFolder))
				return;	// ignore if same value set

			_accFolder = f;
		}
	}

	public File getSelectedFile ()
	{
		final String	t=getText();
		if ((t != null) && (t.length() > 0))
		{
			try
			{
				return new File(t);
			}
			catch(RuntimeException e)
			{
				// ignored
			}
		}

		return null;
	}

	protected File resolveCurrentAccessFolder ()
	{
		final File	f=getSelectedFile(), p=(null == f) ? null : f.getParentFile();
		if ((p != null) && p.exists() && p.isDirectory())
			return f.isDirectory() ? f : p;

		return getCurrentAccessFolder();
	}


	private Collection<ChangeListener>	_cl;
	protected int fireFileSelectionChangeEvent (final File f)
	{
		return ChangeListenerSet.fireChangeEventForSource(f, _cl, true);
	}

	public boolean addFileSelectionChangeListener (ChangeListener l)
	{
		if (null == l)
			return false;

		synchronized(this)
		{
			if (null == _cl)
				_cl = new ChangeListenerSet();
		}

		synchronized(_cl)
		{
			return _cl.add(l);
		}
	}

	public boolean removeFileSelectionChangeListener (ChangeListener l)
	{
		if (null == l)
			return false;

		synchronized(this)
		{
			if ((null == _cl) || (_cl.size() <= 0))
				return false;
		}

		synchronized(_cl)
		{
			return _cl.remove(l);
		}
	}

	public int setSelectedFile (File f, boolean fireChanges)
	{
		if (null == f)
			return 0;

		final File	lastSel=f.getParentFile();
    	if (lastSel != null)
    		setCurrentAccessFolder(lastSel);

    	setText(f.getAbsolutePath());
   
    	if (fireChanges)
    		return fireFileSelectionChangeEvent(f);
    	else
    		return 0;
	}

	public void setSelectedFile (File f)
	{
		setSelectedFile(f, true);
	}
	/*
	 * @see net.community.chest.awt.TypedComponentAssignment#getAssignedValue()
	 */
	@Override
	public File getAssignedValue ()
	{
		return getSelectedFile();
	}
	/*
	 * @see net.community.chest.awt.TypedComponentAssignment#setAssignedValue(java.lang.Object)
	 */
	@Override
	public void setAssignedValue (File value)
	{
		setSelectedFile(value);
	}

	private boolean _saveFileDialog	/* =false */;
	public boolean isSaveFileDialog ()
	{
		return _saveFileDialog;
	}

	public void setSaveFileDialog (boolean saveFileDialog)
	{
		_saveFileDialog = saveFileDialog;
	}

	protected class FileSelectorWorker extends SwingWorker<Void,File> {
		private final Component	_parent;
		public final Component getParent ()
		{
			return _parent;
		}

		protected FileSelectorWorker (final Component parent)
		{
			if (null == (_parent=parent))
				throw new IllegalArgumentException("No parent provided");
		}
		/*
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Void doInBackground () throws Exception
		{
			final JFileChooser	fc=getFileChooser(true);
			if (null == fc)
				return null;

			if (fc.isMultiSelectionEnabled())
				throw new IllegalStateException("Multiple files N/A"); 

			final File	ld=resolveCurrentAccessFolder();
			if (ld != null)
				fc.setCurrentDirectory(ld);

			final Component	p=getParent();
			final int		nRes=isSaveFileDialog() ? fc.showSaveDialog(p) : fc.showOpenDialog(p);
			if (nRes != JFileChooser.APPROVE_OPTION)
				return null;

			final File	f=fc.getSelectedFile();
			if (null == f)	// can happen even if APPROVE(d) if user did not select anything
				return null;

			publish(f);
			return null;
		}
		/*
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		@Override
		protected void process (List<File> chunks)
		{
			final int	numFiles=(null == chunks) ? 0 : chunks.size();
			if (numFiles != 1)
				throw new IllegalStateException("Bad #files published: " + numFiles);

			setSelectedFile(chunks.get(0));
		}
	}
	/*
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed (final ActionEvent event)
	{
		final Object	src=(null == event) ? null : event.getSource(),
						btn=getButton();
		if (src != btn)
			return;
		
		new FileSelectorWorker(this).execute();
	}
}

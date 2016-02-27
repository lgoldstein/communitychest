/*
 * 
 */
package net.community.apps.common.test;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.helpers.panel.input.LRFieldWithButtonPanel;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 31, 2008 10:00:58 AM
 */
public class TestFilesViewIconFrame extends TestMainFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4336740610646332L;
	public TestFilesViewIconFrame (String... args) throws Exception
	{
		super(args);
	}

	private LRFieldWithButtonPanel	_imgLabel;
    /*
     * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
	public void loadFile (File f, String cmd, Element dlgElement)
	{
		final String filePath=(null == f) ? null : f.getAbsolutePath();
		if ((null == filePath) || (filePath.length() <= 0))
			return;

		try
		{
			final FileSystemView	v=FileSystemView.getFileSystemView();
			_imgLabel.setText(f.getAbsolutePath());

			final Icon	icon=v.getSystemIcon(f);
			if (icon != null)
				_imgLabel.setIcon(icon);
		}
		catch(Exception e)
		{
			BaseOptionPane.showMessageDialog(this, e);
		}
	}

	protected void loadIcon ()
	{
		final String	path=(null == _imgLabel) ? null : _imgLabel.getText();
		if ((null == path) || (path.length() <= 0))
			return;

		try
		{
			final File	f=new File(path);
			if (f.isFile() && f.exists() && (f.length() > 0))
			{
				loadFile(f, LOAD_CMD, null);
				return;
			}

			final FileSystemView	v=FileSystemView.getFileSystemView();
			final Icon				icon=v.getSystemIcon(f);
			if (icon != null)
				_imgLabel.setIcon(icon);
		}
		catch(RuntimeException e)
		{
			BaseOptionPane.showMessageDialog(this, e);
		}
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
    		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    	return fc;
    }
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		if (null == _imgLabel)
		{
			_imgLabel = new LRFieldWithButtonPanel();
			_imgLabel.setTitle("Load");
			_imgLabel.addActionListener(new ActionListener() {
					/*
					 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
					 */
					@Override
					public void actionPerformed (ActionEvent e)
					{
						if (e != null)
							loadIcon();
					}
				});

			final Container	ctPane=getContentPane();
			ctPane.add(_imgLabel, BorderLayout.NORTH);
		}
	}
}

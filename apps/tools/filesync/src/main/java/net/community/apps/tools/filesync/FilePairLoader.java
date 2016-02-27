/*
 * 
 */
package net.community.apps.tools.filesync;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.io.File;
import java.util.concurrent.Callable;

import javax.swing.JPanel;

import net.community.chest.ui.helpers.SettableComponent;
import net.community.chest.ui.helpers.button.WindowDisposeButton;
import net.community.chest.ui.helpers.dialog.ButtonsPanel;
import net.community.chest.ui.helpers.dialog.FormDialog;
import net.community.chest.ui.helpers.panel.input.FileInputTextPanel;

import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 2, 2009 2:18:47 PM
 */
public class FilePairLoader extends FormDialog implements SettableComponent<FilePair>, Callable<Boolean> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7188717376027344238L;
	public FilePairLoader (Frame parent, FilePair p, Element elem)
	{
		super(parent, elem);

		if (p != null)
			setContent(p);
	}

	public FilePairLoader (Frame parent, Element elem)
	{
		this(parent, null, elem);
	}

	public FilePairLoader (Frame parent, FilePair p)
	{
		this(parent, p, null);
	}

	public FilePairLoader (Frame parent)
	{
		this(parent, null, null);
	}

	private FilePair	_curPair	/* =null */;
	public FilePair getContent ()
	{
		return _curPair;
	}

	private static final boolean updateFileContent (final FilePair p, final FileInputTextPanel t, final boolean srcPart)
	{
		final File	f=(null == t) ? null : t.getSelectedFile();
		if ((null == f) || (!f.isDirectory()))
			return false;

		if (srcPart)
			p.setSrcFolder(f);
		else
			p.setDstFolder(f);
		return true;
	}

	private FilePairInputTextPanel	_srcDir	/* =null */, _dstDir	/* =null */;
	/*
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call () throws Exception
	{
		final FilePair	p=getContent();
		if (null == p)
			return null;

		final boolean	okToExit=updateFileContent(p, _srcDir, true)
							  && updateFileContent(p, _dstDir, false)
							  ;
		if (!okToExit)
			return null;

		return Boolean.TRUE;
	}
	/*
	 * @see net.community.chest.ui.helpers.dialog.FormDialog#getButtonsPanel()
	 */
	@Override
	public ButtonsPanel getButtonsPanel ()
	{
		ButtonsPanel	bp=super.getButtonsPanel();
		if (null == bp)
		{
			bp = new ButtonsPanel();
			bp.add(new WindowDisposeButton(this, this, "OK"));
			setButtonsPanel(bp);
		}

		return bp;
	}
	/*
	 * @see net.community.chest.ui.helpers.dialog.FormDialog#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		if (null == _srcDir)
			_srcDir = new FilePairInputTextPanel("Source");
		if (null == _dstDir)
			_dstDir = new FilePairInputTextPanel("Destination");

		final Container	viewPanel=new JPanel(new GridLayout(0, 1, 5, 5));
		viewPanel.add(_srcDir);
		viewPanel.add(_dstDir);

		final Container	ctPane=getContentPane();
		ctPane.add(viewPanel, BorderLayout.CENTER);
	}
	/*
	 * @see net.community.chest.ui.helpers.dialog.SettableDialog#setContent(java.lang.Object)
	 */
	@Override
	public void setContent (FilePair value)
	{
		final File[]				fa={
			(null == value) ? null : value.getSrcFolder(),
			(null == value) ? null : value.getDstFolder()
							};
		final FileInputTextPanel[]	pa={ _srcDir, _dstDir };
		for (int	i=0; i < fa.length; i++)
		{
			final FileInputTextPanel	p=pa[i];
			final File					f=fa[i];
			if (null == p)
				continue;
			if (f != null)
				p.setSelectedFile(f, false /* no fire of change events */);
			else
				p.setText("");
		}

		_curPair = value;
	}
	/*
	 * @see net.community.chest.ui.helpers.dialog.SettableDialog#refreshContent(java.lang.Object)
	 */
	@Override
	public void refreshContent (FilePair value)
	{
		setContent(value);
	}
	/*
	 * @see net.community.chest.ui.helpers.dialog.SettableDialog#clearContent()
	 */
	@Override
	public void clearContent ()
	{
		setContent(null);
	}
}

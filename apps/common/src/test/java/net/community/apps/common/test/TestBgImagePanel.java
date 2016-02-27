/*
 * 
 */
package net.community.apps.common.test;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.community.chest.awt.image.AbstractImageReader;
import net.community.chest.awt.image.BMPReader;
import net.community.chest.awt.image.DefaultImageReader;
import net.community.chest.awt.image.ICOReader;
import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.io.EOLStyle;
import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.io.encode.hex.Hex;
import net.community.chest.swing.component.text.DefaultTextAreaScroll;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.components.input.panel.img.BgImagePanel;
import net.community.chest.ui.helpers.combobox.EnumComboBox;

import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 10, 2009 10:42:46 AM
 *
 */
public class TestBgImagePanel extends TestMainFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -803555459212999329L;

	private BgImagePanel	_pnl;
	protected void updateImagePosition (BorderLayoutPosition p)
	{
		if ((null == p) || (null == _pnl))
			return;

		_pnl.setBgImagePosition(p);
		_pnl.repaint();
	}

	private void setBgImage (Image img)
	{
		if ((null == img) || (null == _pnl))
			return;

		_pnl.setBgImage(img);
		_pnl.repaint();
	}

	protected static class BorderLayoutPositionChoice extends EnumComboBox<BorderLayoutPosition> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1984769589089027082L;

		public BorderLayoutPositionChoice ()
		{
			super(BorderLayoutPosition.class);
			setEnumValues(BorderLayoutPosition.VALUES);
			populate();
		}
	}
	private BorderLayoutPositionChoice	_posChoice;

	private byte[]	_workBuf;
	private byte[] getWorkBuf ()
	{
		if (null == _workBuf)
			_workBuf = new byte[IOCopier.DEFAULT_COPY_SIZE];
		return _workBuf;
	}

	private static final int	VALS_PER_LINE=16;

	private JTextArea	_area;
	private void setFgText (final String filePath) throws IOException
	{
		if (null == _area)
			return;

		_area.setText("");

		final byte[]	data=getWorkBuf();
		final int		rLen;
		InputStream		in=null;
		try
		{
			in = new FileInputStream(filePath);

			if ((rLen=in.read(data)) <= 0)
				throw new EOFException("No data read");
		}
		finally
		{
			FileUtil.closeAll(in);
		}

		final StringBuilder	sb=new StringBuilder(VALS_PER_LINE * (Hex.MAX_HEX_DIGITS_PER_BYTE + 1) + 4);
		for (int	rIndex=0; rIndex < rLen; rIndex += VALS_PER_LINE)
		{
			if (sb.length() > 0)
				sb.setLength(0);

			for (int	dIndex=rIndex, maxIndex=Math.min(rLen, rIndex + VALS_PER_LINE); dIndex < maxIndex; dIndex++)
			{
				if (sb.length() > 0)
					sb.append(' ');
				Hex.appendHex(sb, data[dIndex], true);
			}

			EOLStyle.LF.appendEOL(sb);
			_area.append(sb.toString());
			_area.setCaretPosition(_area.getDocument().getLength());
		}
	}
    /*
     * @see net.community.apps.common.FileLoadComponent#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
	public void loadFile (File f, String cmd, Element dlgElement)
	{
		final String filePath=(null == f) ? null : f.getAbsolutePath();
		if ((null == filePath) || (filePath.length() <= 0))
			return;

		try
		{
			final AbstractImageReader	r;
			if (ICOReader.isIconFile(filePath))
				r = ICOReader.DEFAULT;
			else if (BMPReader.isBitmapFile(filePath))
				r = new BMPReader();
			else
				r = DefaultImageReader.DEFAULT;

			final List<? extends Image>	il=r.readImages(filePath);
			final int					numImages=(null == il) ? 0 : il.size();
			final Image					img=(numImages <= 0) ? null : il.get(0);
			if (null == img)
				throw new IllegalStateException("No image extracted");
			setTitle(filePath);
			setBgImage(img);
			setFgText(filePath);
		}
		catch(Exception e)
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
    		fc.setFileFilter(new FileNameExtensionFilter("Image files", AbstractImageReader.GIF_SUFFIX, AbstractImageReader.JPG_SUFFIX, AbstractImageReader.PNG_SUFFIX, BMPReader.BMP_SUFFIX));
    	return fc;
    }
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		final Container	ctPane=getContentPane();
		if (_posChoice == null)
			_posChoice = new BorderLayoutPositionChoice();
		_posChoice.addActionListener(new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent e)
				{
					final Object	src=(null == e) ? null : e.getSource();
					if (src instanceof BorderLayoutPositionChoice)
						updateImagePosition(((BorderLayoutPositionChoice) src).getSelectedValue());
				}
				
			});
		ctPane.add(_posChoice, BorderLayout.NORTH);

		if (null == _pnl)
			_pnl = new BgImagePanel(getIconImage(), new BorderLayout(0, 0));

		if (null == _area)
			_area = new JTextArea();
		_pnl.add(new DefaultTextAreaScroll(_area), BorderLayout.CENTER);

		ctPane.add(_pnl, BorderLayout.CENTER);
	}

	public TestBgImagePanel (String... args) throws Exception
	{
		super(args);

		if ((args != null) && (args.length == 1) && (args[0] != null))
			loadFile(new File(args[0]), LOAD_CMD, null);
	}
}

package net.community.chest.ui.components.dialog.manifest;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Window;
import java.util.List;
import java.util.jar.Manifest;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.swing.WindowCloseOptions;
import net.community.chest.swing.component.table.DefaultTableScroll;
import net.community.chest.ui.helpers.dialog.HelperDialog;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2007 2:17:48 PM
 */
public class ManifestDialog extends HelperDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8173142072434782934L;

	protected void initDialogData (final Window owner, final ManifestTableModel model) throws HeadlessException
	{
		if (null == model)
			throw new HeadlessException(ClassUtil.getExceptionLocation(getClass(), "initDialogData") + " no " + Manifest.class.getName() + " instance");

		// copy the image used by the owner (if any
		if (owner != null)
		{
			setLocationRelativeTo(owner);
			final List<? extends Image>	imgs=owner.getIconImages();
			if ((imgs != null) && (imgs.size() > 0))
			{
				for (final Image img : imgs)
				{
					if (null == img)
						continue;

					setIconImage(img);
					break;
				}
			}
		}

		final Container	ctPane=getContentPane();
		ctPane.setLayout(new BorderLayout());

		final ManifestTable	tbl=new ManifestTable(model);
		tbl.setAutoCreateRowSorter(true);

		ctPane.add(new DefaultTableScroll(tbl), BorderLayout.CENTER);
		setDefaultCloseOperationAction(WindowCloseOptions.DISPOSE);
	}

	public ManifestDialog (Frame owner, ManifestTableModel model, String title, boolean modal, GraphicsConfiguration gc) throws HeadlessException
	{
		super(owner, title, modal, gc);
		initDialogData(owner, model);
	}

	public ManifestDialog (Dialog owner, ManifestTableModel model, String title, boolean modal, GraphicsConfiguration gc) throws HeadlessException
	{
		super(owner, title, modal, gc);
		initDialogData(owner, model);
	}

	public ManifestDialog (Frame owner, ManifestTableModel model, String title, boolean modal) throws HeadlessException
	{
		this(owner, model, title, modal, null);
	}

	public ManifestDialog (Frame owner, ManifestTableModel model, boolean modal) throws HeadlessException
	{
		this(owner, model, null, modal);
	}

	public ManifestDialog (Frame owner, ManifestTableModel model) throws HeadlessException
	{
		this(owner, model, false);
	}

	public ManifestDialog (ManifestTableModel model) throws HeadlessException
	{
		this((Frame) null, model);
	}

	public ManifestDialog (Dialog owner, ManifestTableModel model, boolean modal) throws HeadlessException
	{
		this(owner, model, null, modal);
	}

	public ManifestDialog (Dialog owner, ManifestTableModel model) throws HeadlessException
	{
		this(owner, model, true);
	}

	public ManifestDialog (Frame owner, ManifestTableModel model, String title) throws HeadlessException
	{
		this(owner, model, title, true);
	}

	public ManifestDialog (Dialog owner, ManifestTableModel model, String title, boolean modal) throws HeadlessException
	{
		this(owner, model, title, modal, null);
	}

	public ManifestDialog (Dialog owner, ManifestTableModel model, String title) throws HeadlessException
	{
		this(owner, model, title, true);
	}
	/*
	 * @see net.community.chest.ui.helpers.dialog.HelperDialog#getDialogConverter(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	protected XmlProxyConvertible<? extends ManifestDialog> getDialogConverter (final Element elem)
	{
		return (null == elem) ? null : ManifestDialogReflectiveProxy.MANIFEST;
	}

	public ManifestDialog (Frame owner, Class<?> anchor, Element elem) throws Exception
	{
		super(owner, elem);
		initDialogData(owner, new ManifestTableModel(elem, anchor));
	}

	public ManifestDialog (Class<?> anchor, Element elem) throws Exception
	{
		this((Frame) null, anchor, elem);
	}

	public ManifestDialog (Dialog owner, Class<?> anchor, Element elem) throws Exception
	{
		super(owner, elem);
		initDialogData(owner, new ManifestTableModel(elem, anchor));
	}
}

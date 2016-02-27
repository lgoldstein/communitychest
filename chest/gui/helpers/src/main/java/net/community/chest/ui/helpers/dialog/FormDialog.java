/*
 * 
 */
package net.community.chest.ui.helpers.dialog;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;

import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.dom.proxy.XmlProxyConvertible;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>This dialog assumes some kind of content that requires a buttons
 * panel which by default is placed at the bottom</P>
 * @author Lyor G.
 * @since Jan 6, 2009 2:06:16 PM
 */
public class FormDialog extends HelperDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3075176168271934873L;
	public FormDialog (boolean autoInit)
	{
		super(autoInit);
	}

	public FormDialog (Frame owner, boolean modal, boolean autoInit)
	{
		super(owner, modal, autoInit);
	}

	public FormDialog (Dialog owner, boolean modal, boolean autoInit)
	{
		super(owner, modal, autoInit);
	}

	public FormDialog (Frame owner, Element elem, boolean autoInit)
	{
		super(owner, elem, autoInit);
	}

	public FormDialog (Element elem, boolean autoInit)
	{
		super(elem, autoInit);
	}

	public FormDialog (Dialog owner, Element elem, boolean autoInit)
	{
		super(owner, elem, autoInit);
	}

	public FormDialog (Document doc, boolean autoInit)
	{
		super(doc, autoInit);
	}

	public FormDialog (Frame owner, Document doc, boolean autoInit)
	{
		super(owner, doc, autoInit);
	}

	public FormDialog (Dialog owner, Document doc, boolean autoInit)
	{
		super(owner, doc, autoInit);
	}

	public FormDialog (Frame owner, String title, boolean modal, boolean autoInit)
	{
		super(owner, title, modal, autoInit);
	}

	public FormDialog (Dialog owner, String title, boolean modal, boolean autoInit)
	{
		super(owner, title, modal, autoInit);
	}

	public FormDialog (Frame owner, String title, boolean modal, GraphicsConfiguration gc, boolean autoInit)
	{
		super(owner, title, modal, gc, autoInit);
	}

	public FormDialog (Dialog owner, String title, boolean modal, GraphicsConfiguration gc, boolean autoInit)
	{
		super(owner, title, modal, gc, autoInit);
	}


	public FormDialog ()
	{
		this(true);
	}

	public FormDialog (Frame owner)
	{
		super(owner);
	}

	public FormDialog (Dialog owner)
	{
		super(owner);
	}

	public FormDialog (Frame owner, boolean modal)
	{
		super(owner, modal);
	}

	public FormDialog (Frame owner, String title)
	{
		super(owner, title);
	}

	public FormDialog (Dialog owner, boolean modal)
	{
		super(owner, modal);
	}

	public FormDialog (Dialog owner, String title)
	{
		super(owner, title);
	}

	public FormDialog (Frame owner, Element elem)
	{
		super(owner, elem);
	}

	public FormDialog (Element elem)
	{
		this(elem, true);
	}

	public FormDialog (Dialog owner, Element elem)
	{
		this(owner, elem, true);
	}

	public FormDialog (Document doc)
	{
		this(doc, true);
	}

	public FormDialog (Frame owner, Document doc)
	{
		this(owner, doc, true);
	}

	public FormDialog (Dialog owner, Document doc)
	{
		this(owner, doc, true);
	}

	public FormDialog (Frame owner, String title, boolean modal)
	{
		this(owner, title, modal, true);
	}

	public FormDialog (Dialog owner, String title, boolean modal)
	{
		this(owner, title, modal, true);
	}

	public FormDialog (Frame owner, String title, boolean modal, GraphicsConfiguration gc)
	{
		super(owner, title, modal, gc);
	}

	public FormDialog (Dialog owner, String title, boolean modal, GraphicsConfiguration gc)
	{
		this(owner, title, modal, gc, true);
	}

	public FormDialog (Window owner, boolean autoInit)
	{
		super(owner, autoInit);
	}

	public FormDialog (Window owner, Element elem, boolean autoInit)
	{
		super(owner, elem, autoInit);
	}

	public FormDialog (Window owner, Document doc, boolean autoInit)
	{
		super(owner, doc, autoInit);
	}

	public FormDialog (Window owner, Document doc)
	{
		this(owner, doc, true);
	}

	public FormDialog (Window owner, ModalityType modalityType, boolean autoInit)
	{
		super(owner, modalityType, autoInit);
	}

	public FormDialog (Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc, boolean autoInit)
	{
		super(owner, title, modalityType, gc, autoInit);
	}

	public FormDialog (Window owner, String title, ModalityType modalityType, boolean autoInit)
	{
		super(owner, title, modalityType, autoInit);
	}

	public FormDialog (Window owner, String title, boolean autoInit)
	{
		super(owner, title, autoInit);
	}
	public FormDialog (Window owner, Element elem)
	{
		this(owner, elem, true);
	}

	public FormDialog (Window owner, String title, ModalityType modalityType)
	{
		super(owner, title, modalityType);
	}

	public FormDialog (Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc)
	{
		super(owner, title, modalityType, gc);
	}

	public FormDialog (Window owner, ModalityType modalityType)
	{
		super(owner, modalityType);
	}

	public FormDialog (Window owner, String title)
	{
		super(owner, title);
	}

	public FormDialog (Window owner)
	{
		this(owner, true);
	}

	private BorderLayoutPosition	_btnsPos	/* =null */;
	public BorderLayoutPosition getButtonsPanelLocation ()
	{
		return _btnsPos;
	}

	public void setButtonsPanelLocation (BorderLayoutPosition pos)
	{
		_btnsPos = pos;
	}

	private ButtonsPanel	_btnsPnl	/* =null */;
	public ButtonsPanel getButtonsPanel ()
	{
		return _btnsPnl;
	}
	// CAVEAT EMPTOR if called after layoutButtons...
	public void setButtonsPanel (ButtonsPanel p)
	{
		_btnsPnl = p;
	}
	/**
	 * <P>Called by {@link #layoutComponent()} <U>after</U> laying out
	 * all the other sections in order to enable initializing the buttons
	 * (which are usually used in a dialog). The reason it is not called by
	 * default is that if we add the buttons panel before the other components
	 * then the UI becomes corrupted.</P></BR> 
	 * <P>The default base class implementation checks if a {@link ButtonsPanel}
	 * is available, and if so it adds it according to the content pane
	 * according to the {@link #getButtonsPanelLocation()} value (default=SOUTH).
	 * @throws RuntimeException If failed to initialize the buttons
	 */
	public void layoutButtons () throws RuntimeException
	{
		final ButtonsPanel	p=getButtonsPanel();
		if (p != null)
		{
			final Container				ct=getContentPane();
			final BorderLayoutPosition	pos=getButtonsPanelLocation();
			ct.add(p, (null == pos) ? BorderLayoutPosition.SOUTH.getPosition() : pos.getPosition());
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.dialog.HelperDialog#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();
		setLocationRelativeTo(getParent());	// by default
		layoutButtons();	// must be called last
	}
	/*
	 * @see net.community.chest.ui.helpers.dialog.HelperDialog#getDialogConverter(org.w3c.dom.Element)
	 */
	@Override
	protected XmlProxyConvertible<?> getDialogConverter (Element elem)
	{
		return (null == elem) ? null : FormDialogReflectiveProxy.FRMDLG;
	}
}

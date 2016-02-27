/*
 * 
 */
package net.community.chest.ui.helpers.panel.input;

import java.awt.LayoutManager;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

import net.community.chest.awt.attributes.Editable;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.ui.helpers.panel.HelperPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>A {@link javax.swing.JPanel} with a {@link JTextField} for input
 * @author Lyor G.
 * @since Aug 20, 2008 2:19:08 PM
 */
public abstract class AbstractInputTextPanel extends HelperPanel
		implements Textable, Editable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 846829434515042111L;
	private JTextField	_textField;
	protected JTextField getTextField (boolean createIfNotExist)
	{
		if ((null == _textField) && createIfNotExist)
			_textField = new JTextField();
		return _textField;
	}

	public JTextField getTextField ()
	{
		return getTextField(false);
	}

	public void setTextField (JTextField textField)
	{
		_textField = textField;
	}
	/*
	 * @see net.community.chest.awt.attributes.Textable#getText()
	 */
	@Override
	public String getText ()
	{
		final JTextField	f=getTextField();
		return (null == f) ? null : f.getText();
	}

	public void addTextFieldKeyListener (KeyListener l)
	{
		if (l == null)
			return;
		
		final JTextField	f=getTextField();
		if (f == null)
			return;

		f.addKeyListener(l);
	}
	/*
	 * @see net.community.chest.awt.attributes.Textable#setText(java.lang.String)
	 */
	@Override
	public void setText (String text)
	{
		final JTextField	f=getTextField(true);
		if (f != null)
			f.setText((null == text) ? "" : text);
	}
	/*
	 * @see net.community.chest.awt.attributes.Editable#isEditable()
	 */
	@Override
	public boolean isEditable ()
	{
		final JTextField	f=getTextField();
		return (f != null) && f.isEditable();
	}
	/*
	 * @see net.community.chest.awt.attributes.Editable#setEditable(boolean)
	 */
	@Override
	public void setEditable (boolean b)
	{
		final JTextField	f=getTextField(true);
		if (f != null)
			f.setEditable(b);
	}
	/*
	 * @see javax.swing.JComponent#setEnabled(boolean)
	 */
	@Override
	public void setEnabled (boolean enabled)
	{
		final JTextField	f=getTextField();
		if (f != null)
			f.setEnabled(enabled);
		super.setEnabled(enabled);
	}
	/**
	 * Called by default implementation of {@link #layoutComponent()} after
	 * successfully instantiating the text field
	 * @param f Set/created {@link JTextField} instance
	 */
	protected abstract void layoutComponent (final JTextField f);
	/* Force overriding the inherited method
	 * @see net.community.chest.swing.component.panel.BasePanel#getPanelConverter(org.w3c.dom.Element)
	 */
	@Override
	protected XmlProxyConvertible<?> getPanelConverter (final Element elem)
	{
		return (null == elem) ? null : InputTextPanelReflectiveProxy.ITEXTPNL;
	}
	/*
	 * @see net.community.chest.ui.helpers.panel.HelperPanel#layoutComponent(org.w3c.dom.Element)
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();
		layoutComponent(getTextField(true));
	}

	protected AbstractInputTextPanel (LayoutManager layout, Document doc, boolean autoLayout)
	{
		super(layout, doc, autoLayout);
	}

	protected AbstractInputTextPanel (Document doc, boolean autoLayout)
	{
		this(null, doc, autoLayout);
	}

	protected AbstractInputTextPanel (boolean autoLayout)
	{
		this((Document) null, autoLayout);
	}

	protected AbstractInputTextPanel (Element elem, boolean autoLayout)
	{
		this((null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	protected AbstractInputTextPanel ()
	{
		this(true);
	}

	protected AbstractInputTextPanel (Element elem)
	{
		this(elem, true);
	}

	protected AbstractInputTextPanel (LayoutManager layout, boolean autoLayout)
	{
		this(layout, (Document) null, autoLayout);
	}

	protected AbstractInputTextPanel (LayoutManager layout)
	{
		this(layout, true);
	}
}

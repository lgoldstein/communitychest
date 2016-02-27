/*
 * 
 */
package net.community.chest.ui.helpers.panel.input;

import java.awt.LayoutManager;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTextField;

import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.attributes.Titled;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.dom.proxy.XmlProxyConvertible;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Also has a {@link JButton} that can do something related to the text field</P>
 * @author Lyor G.
 * @since Aug 20, 2008 2:45:08 PM
 */
public abstract class AbstractFieldWithButtonPanel extends AbstractInputTextPanel implements Iconable, Titled {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1743304344105772248L;
	private JButton	_btn;
	protected JButton getButton (boolean createIfNotExist)
	{
		if ((null == _btn) && createIfNotExist)
			_btn = new JButton();
		return _btn;
	}

	public JButton getButton ()
	{
		return getButton(false);
	}

	public void setButton (JButton b)
	{
		_btn = b;
	}
	/*
	 * @see net.community.chest.awt.attributes.Iconable#getIcon()
	 */
	@Override
	public Icon getIcon ()
	{
		final JButton	b=getButton();
		return (null == b) ? null : b.getIcon();
	}
	/*
	 * @see net.community.chest.awt.attributes.Iconable#setIcon(javax.swing.Icon)
	 */
	@Override
	public void setIcon (Icon i)
	{
		final JButton	b=getButton(true);
		if (b != null)
			b.setIcon(i);
	}
	/*
	 * @see net.community.chest.awt.attributes.Titled#getTitle()
	 */
	@Override
	public String getTitle ()
	{
		final JButton	b=getButton();
		return (null == b) ? null : b.getText();
	}
	/*
	 * @see net.community.chest.awt.attributes.Titled#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle (String t)
	{
		final JButton	b=getButton(true);
		if (b != null)
			b.setText((null == t) ? "" : t);
	}
	/*
	 * @see net.community.chest.ui.helpers.panel.input.AbstractInputTextPanel#setEnabled(boolean)
	 */
	@Override
	public void setEnabled (boolean enabled)
	{
		final JButton	b=getButton();
		if (b != null)
			b.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	public void addActionListener (final ActionListener l)
	{
		if (null == l)
			return;

		final JButton	b=getButton();
		if (b != null)
			b.addActionListener(l);
	}

	public void removeActionListener (final ActionListener l)
	{
		if (null == l)
			return;

		final JButton	b=getButton();
		if (b != null)
			b.removeActionListener(l);
	}

	protected AbstractFieldWithButtonPanel (LayoutManager layout, Document doc, boolean autoLayout)
	{
		super(layout, doc, autoLayout);
	}

	protected AbstractFieldWithButtonPanel (Document doc, boolean autoLayout)
	{
		this(null, doc, autoLayout);
	}

	protected AbstractFieldWithButtonPanel (Document doc)
	{
		this(doc, true);
	}

	protected AbstractFieldWithButtonPanel (LayoutManager layout, boolean autoLayout)
	{
		this(layout, (Document) null, autoLayout);
	}

	protected AbstractFieldWithButtonPanel (boolean autoLayout)
	{
		this((Document) null, autoLayout);
	}

	protected AbstractFieldWithButtonPanel ()
	{
		this(true);
	}

	protected AbstractFieldWithButtonPanel (Element elem, boolean autoLayout)
	{
		this(null, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	protected AbstractFieldWithButtonPanel (Element elem)
	{
		this(elem, true);
	}

	protected AbstractFieldWithButtonPanel (LayoutManager layout)
	{
		this(layout, true);
	}
	/*
	 * @see net.community.chest.swing.component.panel.input.AbstractInputTextPanel#getPanelConverter(org.w3c.dom.Element)
	 */
	@Override
	protected XmlProxyConvertible<?> getPanelConverter (final Element elem)
	{
		return (null == elem) ? null : FieldWithButtonPanelReflectiveProxy.FLDWITHBTNPNL;
	}
	/**
	 * Called by default {@link #layoutComponent(JTextField)} implementation
	 * after successfully retrieving/creating a {@link JButton} instance
	 * @param b The {@link JButton} instance
	 * @param f The {@link JTextField} instance
	 */
	protected abstract void layoutComponent (final JButton b, final JTextField f);
	/*
	 * @see net.community.chest.swing.component.panel.input.AbstractInputTextPanel#layoutComponent(javax.swing.JTextField)
	 */
	@Override
	protected void layoutComponent (final JTextField f)
	{
		layoutComponent(getButton(true), f);
	}
}

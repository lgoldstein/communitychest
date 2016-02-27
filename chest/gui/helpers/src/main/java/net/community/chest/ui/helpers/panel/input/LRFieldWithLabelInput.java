/*
 * 
 */
package net.community.chest.ui.helpers.panel.input;

import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.ui.helpers.input.InputFieldValidator;
import net.community.chest.ui.helpers.text.InputTextField;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 13, 2009 11:00:05 AM
 */
public class LRFieldWithLabelInput extends LRFieldWithLabelPanel implements InputFieldValidator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3371884064286743674L;
	public LRFieldWithLabelInput (boolean lblRightPos, Document doc, boolean autoLayout)
	{
		super(lblRightPos, doc, autoLayout);
	}

	public LRFieldWithLabelInput (Document doc, boolean autoLayout)
	{
		this(false, doc, autoLayout);
	}

	public LRFieldWithLabelInput (boolean lblRightPos, Element elem, boolean autoLayout)
	{
		this(lblRightPos, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public LRFieldWithLabelInput (boolean autoLayout)
	{
		this((Document) null, autoLayout);
	}

	public LRFieldWithLabelInput ()
	{
		this(true);
	}

	public LRFieldWithLabelInput (Element elem, boolean autoLayout)
	{
		this(false, elem, autoLayout);
	}

	public LRFieldWithLabelInput (Element elem)
	{
		this(elem, true);
	}
	/*
	 * @see net.community.chest.ui.helpers.panel.input.AbstractInputTextPanel#getTextField(boolean)
	 */
	@Override
	protected JTextField getTextField (boolean createIfNotExist)
	{
		JTextField	f=super.getTextField(false);
		if ((null == f) && createIfNotExist)
		{
			f = new InputTextField();
			setTextField(f);
		}

		return f;
	}
	/*
	 * @see net.community.chest.ui.helpers.panel.input.LRFieldWithLabelPanel#getPanelConverter(org.w3c.dom.Element)
	 */
	@Override
	protected XmlProxyConvertible<?> getPanelConverter (Element elem)
	{
		return (null == elem) ? null : LRFieldWithLabelInputReflectiveProxy.LRFLBLINP;
	}

	public InputTextField getInputTextField ()
	{
		final JTextField	f=getTextField();
		return (f instanceof InputTextField) ? (InputTextField) f : null;
	}

	public void setInputTextField (InputTextField f)
	{
		setTextField(f);
	}
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#addDataChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public boolean addDataChangeListener (ChangeListener l)
	{
		final InputTextField	f=getInputTextField();
		return (null == f) ? false : f.addDataChangeListener(l);
	}
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#getErrFieldColor()
	 */
	@Override
	public Color getErrFieldColor ()
	{
		final InputTextField	f=getInputTextField();
		return (null == f) ? null : f.getErrFieldColor();
	}
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#getOkFieldColor()
	 */
	@Override
	public Color getOkFieldColor ()
	{
		final InputTextField	f=getInputTextField();
		return (null == f) ? null : f.getOkFieldColor();
	}
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#isValidData()
	 */
	@Override
	public boolean isValidData ()
	{
		final InputTextField	f=getInputTextField();
		return (null == f) ? false : f.isValidData();
	}
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#removeDataChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public boolean removeDataChangeListener (ChangeListener l)
	{
		final InputTextField	f=getInputTextField();
		return (null == f) ? false : f.removeDataChangeListener(l);
	}
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#setErrFieldColor(java.awt.Color)
	 */
	@Override
	public void setErrFieldColor (Color errColor)
	{
		final InputTextField	f=getInputTextField();
		if (f != null)
			f.setErrFieldColor(errColor);
	}
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#setOkFieldColor(java.awt.Color)
	 */
	@Override
	public void setOkFieldColor (Color okColor)
	{
		final InputTextField	f=getInputTextField();
		if (f != null)
			f.setOkFieldColor(okColor);
	}
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#signalDataChanged(boolean)
	 */
	@Override
	public int signalDataChanged (boolean fireEvent)
	{
		final InputTextField	f=getInputTextField();
		if (f != null)
			return f.signalDataChanged(fireEvent);
		else
			return 0;
	}
}

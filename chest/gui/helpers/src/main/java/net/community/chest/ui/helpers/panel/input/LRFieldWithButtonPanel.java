/*
 * 
 */
package net.community.chest.ui.helpers.panel.input;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JTextField;

import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.dom.proxy.XmlProxyConvertible;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Places the button to the left/right of the text field and makes the
 * text field take the rest of the width</P>
 * @author Lyor G.
 * @since Aug 20, 2008 3:05:23 PM
 */
public class LRFieldWithButtonPanel extends AbstractFieldWithButtonPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5683481745124861067L;
	// if leftPosButton is TRUE then auto-layout is delayed until AFTER the value has been updated
	public LRFieldWithButtonPanel (boolean leftPosButton, Document doc, boolean autoLayout)
	{
		super(new GridBagLayout(), doc, autoLayout && (!leftPosButton));

		if (leftPosButton)
		{
			_buttonLeftPos = leftPosButton;

			if (autoLayout)
				layoutComponent();
		}
	}

	public LRFieldWithButtonPanel (Document doc, boolean autoLayout)
	{
		this(false, doc, autoLayout);
	}

	public LRFieldWithButtonPanel (Document doc)
	{
		this(doc, true);
	}

	public LRFieldWithButtonPanel (Element elem, boolean autoLayout)
	{
		this((null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public LRFieldWithButtonPanel (boolean leftPosButton, boolean autoLayout)
	{
		this(leftPosButton, (Document) null, autoLayout);
	}

	public LRFieldWithButtonPanel (boolean autoLayout)
	{
		this(false, autoLayout);
	}

	public LRFieldWithButtonPanel ()
	{
		this(true);
	}

	public LRFieldWithButtonPanel (Element elem)
	{
		this(elem, true);
	}
	/*
	 * @see net.community.chest.swing.component.panel.input.AbstractFieldWithButtonPanel#getPanelConverter(org.w3c.dom.Element)
	 */
	@Override
	protected XmlProxyConvertible<?> getPanelConverter (Element elem)
	{
		return (null == elem) ? null : LRFieldWithButtonReflectiveProxy.LRFLDBTNPNL;
	}

	private boolean	_buttonLeftPos	/* =false */;
 	public boolean isButtonLeftPos ()
	{
		return _buttonLeftPos;
	}

 	public void setButtonLeftPos (final boolean buttonLeftPos)
	{
		if (buttonLeftPos != isButtonLeftPos())
			_buttonLeftPos = buttonLeftPos;
	}
    /**
     * Default components {@link Insets}
     * @see #getComponentsInsets()
     */
    public static final Insets	COMMON_INSETS=new Insets(5,5,5,5);

    private Insets	_insets;
    public Insets getComponentsInsets ()
    {
    	if (null == _insets)
    		_insets = COMMON_INSETS;
    	return _insets;
    }

	public void setComponentsInsets (final Insets i)
	{
		if ((i != null) && (!i.equals(getComponentsInsets())))
			_insets = i;
	}
	/* Allow only GridBagLayout
	 * @see java.awt.Container#setLayout(java.awt.LayoutManager)
	 */
	@Override
	public void setLayout (final LayoutManager mgr)
	{
		if ((mgr != null) && (!(mgr instanceof GridBagLayout)))
			throw new IllegalArgumentException("setLayout(" + mgr.getClass().getName() + ") non-" + GridBagLayout.class.getSimpleName() + " N/A");

		super.setLayout(mgr);
	}
	/*
	 * @see net.community.chest.swing.component.panel.input.AbstractFieldWithButtonPanel#layoutComponent(javax.swing.JButton, javax.swing.JTextField)
	 */
	@Override
	protected void layoutComponent (final JButton b, final JTextField f)
	{
		if ((null == b) && (null == f))
			return;	// should not happen

		final GridBagConstraints	gbc=new GridBagConstraints();
		final boolean				leftPos=isButtonLeftPos();
    	// initialize common settings
    	gbc.gridy = 0;
    	gbc.insets = getComponentsInsets();

		if (b != null)
		{
			gbc.gridx = leftPos ? 0 : 1;
			gbc.anchor = leftPos ? GridBagConstraints.LINE_START : GridBagConstraints.LINE_END;
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridwidth = 1;
			gbc.weightx = 0.0;

			add(b, gbc);
		}

		if (f != null)
		{
			gbc.gridx = leftPos ? 1 : 0;
			gbc.anchor = leftPos ? GridBagConstraints.LINE_END : GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			gbc.weightx = 1.0;

			add(f, gbc);
		}
	}
}

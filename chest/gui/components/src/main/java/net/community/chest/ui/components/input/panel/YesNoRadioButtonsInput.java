/*
 * 
 */
package net.community.chest.ui.components.input.panel;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.util.Enumeration;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.awt.layout.BoxLayoutAxis;
import net.community.chest.awt.layout.FlowLayoutAlignment;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.swing.event.ChangeListenerSet;
import net.community.chest.ui.helpers.button.TypedRadioButton;
import net.community.chest.ui.helpers.button.group.RadioButtonsGroup;
import net.community.chest.ui.helpers.input.InputFieldValidator;
import net.community.chest.ui.helpers.input.ValidatorUtils;
import net.community.chest.ui.helpers.panel.HelperPanel;
import net.community.chest.util.map.BooleansMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 15, 2009 12:36:30 PM
 */
public class YesNoRadioButtonsInput extends HelperPanel
		implements TypedComponentAssignment<Boolean>, InputFieldValidator {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8416283939512561645L;
	/* Allow only the preset layout type
	 * @see java.awt.Container#setLayout(java.awt.LayoutManager)
	 */
	@Override
	public void setLayout (final LayoutManager mgr)
	{
		// NOTE !!! 1st call comes from the constructor BEFORE _lmClass is initialized
		final Class<?>	mgrClass=(null == mgr) ? null : mgr.getClass();
		if (mgrClass != null)
		{
			if (FlowLayout.class.isAssignableFrom(mgrClass)
			 || BoxLayout.class.isAssignableFrom(mgrClass))
				super.setLayout(mgr);

			throw new UnsupportedOperationException("setLayout(" + mgrClass.getName() + ") N/A");
		}

		super.setLayout(mgr);
	}

	public static final BoxLayoutAxis	DEFAULT_AXIS=BoxLayoutAxis.LINE;
	public YesNoRadioButtonsInput (BoxLayoutAxis axis, Document doc, boolean autoLayout)
	{
		super(null, doc, false /* delay component init till layout set */);

		setLayout(new BoxLayout(this, ((null == axis) ? DEFAULT_AXIS : axis).getAxis()));
		if (autoLayout)
			layoutComponent();
	}

	public YesNoRadioButtonsInput (BoxLayoutAxis axis, Document doc)
	{
		this(axis, doc, true);
	}

	public YesNoRadioButtonsInput (BoxLayoutAxis axis, boolean autoLayout)
	{
		this(axis, (Document) null, autoLayout);
	}

	public YesNoRadioButtonsInput (BoxLayoutAxis axis, Element elem, boolean autoLayout)
	{
		this(axis, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public YesNoRadioButtonsInput (BoxLayoutAxis axis, Element elem)
	{
		this(axis, elem, true);
	}

	public YesNoRadioButtonsInput (FlowLayout l, Document doc, boolean autoLayout)
	{
		super(l, doc, autoLayout);
	}

	public YesNoRadioButtonsInput (FlowLayout l, Document doc)
	{
		this(l, doc, true);
	}

	public YesNoRadioButtonsInput (FlowLayout l, boolean autoLayout)
	{
		this(l, (Document) null, autoLayout);
	}

	public YesNoRadioButtonsInput (FlowLayout l, Element elem, boolean autoLayout)
	{
		this(l, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public YesNoRadioButtonsInput (FlowLayout l, Element elem)
	{
		this(l, elem, true);
	}

	public YesNoRadioButtonsInput (FlowLayout l)
	{
		this(l, true);
	}

	public static final FlowLayoutAlignment	DEFAULT_ALIGNMENT=FlowLayoutAlignment.LEFT;
	public static final int					DEFAULT_HGAP=0, DEFAULT_VGAP=0;
	public YesNoRadioButtonsInput (FlowLayoutAlignment align, int hGap, int vGap, boolean autoLayout)
	{
		this(new FlowLayout(((null == align) ? DEFAULT_ALIGNMENT : align).getAlignment(), hGap, vGap), (Document) null, autoLayout);
	}

	public YesNoRadioButtonsInput (FlowLayoutAlignment align, boolean autoLayout)
	{
		this(align, DEFAULT_HGAP, DEFAULT_VGAP, autoLayout);
	}

	public YesNoRadioButtonsInput (FlowLayoutAlignment align, int hGap, int vGap)
	{
		this(align, hGap, vGap, true);
	}

	public YesNoRadioButtonsInput (FlowLayoutAlignment align)
	{
		this(align, true);
	}

	public YesNoRadioButtonsInput (int hGap, int vGap, boolean autoLayout)
	{
		this(DEFAULT_ALIGNMENT, hGap, vGap, autoLayout);
	}

	public YesNoRadioButtonsInput (int hGap, int vGap)
	{
		this(hGap, vGap, true);
	}

	public YesNoRadioButtonsInput (boolean autoLayout)
	{
		this(DEFAULT_ALIGNMENT, autoLayout);
	}

	public YesNoRadioButtonsInput ()
	{
		this(true);
	}

	private ButtonGroup	_btg;
	public ButtonGroup getButtonGroup ()
	{
		return _btg;
	}
	// NOTE !!! might have no effect if called AFTER layoutComponent
	public void setButtonGroup (ButtonGroup btg)
	{
		_btg = btg;
	}

	protected ButtonGroup createButtonGroup ()
	{
		return new RadioButtonsGroup.JRadioButtonGroup();
	}

	private BooleansMap<AbstractButton>	_btnsMap;
	// NOTE !!! might have no effect if called AFTER layoutComponent
	public AbstractButton addButton (boolean optVal, AbstractButton btn)
	{
		if (null == btn)
			return null;

		if (null == _btnsMap)
			_btnsMap = new BooleansMap<AbstractButton>(AbstractButton.class, false);
		return _btnsMap.put(optVal, btn);
	}
	// NOTE !!! might have no effect if called AFTER layoutComponent
	public AbstractButton getButton (boolean optVal)
	{
		return (null == _btnsMap) ? null : _btnsMap.get(optVal);
	}
	// NOTE !!! might have no effect if called AFTER layoutComponent
	public AbstractButton removeButton (boolean optVal)
	{
		return (null == _btnsMap) ? null : _btnsMap.remove(optVal);
	}

	protected AbstractButton createButton (boolean optVal)
	{
		final AbstractButton	b=new TypedRadioButton<Boolean>(Boolean.class, Boolean.valueOf(optVal));
		b.setText(String.valueOf(optVal));
		b.setSelected(optVal);
		return b;
	}

	protected ButtonGroup layoutOptionButton (final ButtonGroup orgBtg, final boolean optVal, final AbstractButton btn)
	{
		if (null == btn)
			return orgBtg;

		add(btn);
		addButton(optVal, btn);

		ButtonGroup			btg=orgBtg;
		if (null == btg)
			btg = createButtonGroup();
		if (btg != null)
			btg.add(btn);
		return btg;
	}

	private Boolean	_initialValue	/* =null */;
	public Boolean getInitialValue ()
	{
		return _initialValue;
	}
	// NOTE !!! might have no effect if called after layoutButtons
	public void setInitialValue (Boolean initialValue)
	{
		_initialValue = initialValue;
	}

	protected void layoutButtons (final AbstractButton yesBtn, final AbstractButton noBtn)
	{
		final AbstractButton[]	ba={
				(null == yesBtn) ? createButton(true) : yesBtn,
				(null == noBtn) ? createButton(false) : noBtn
			};
		final ButtonGroup	orgBtg=getButtonGroup();
		ButtonGroup			btg=orgBtg;
		ChangeListener		cl=null;
		for (int	bIndex=0; bIndex < ba.length; bIndex++)
		{
			final boolean			optVal=(0 == bIndex);
			final AbstractButton	btn=ba[bIndex];
			if (btn != null)
			{
				if (null == cl)
					cl = new ChangeListener() {
							/*
							 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
							 */
							@Override
							public void stateChanged (ChangeEvent e)
							{
								signalDataChanged(true);
							}
						};
				btn.addChangeListener(cl);
			}
			btg = layoutOptionButton(btg, optVal, btn);
		}

		if (btg != orgBtg)
			setButtonGroup(btg);

		final Boolean	curVal=getInitialValue();
		if (curVal != null)
			setAssignedValue(curVal);
	}
	/*
	 * @see net.community.chest.ui.helpers.panel.HelperPanel#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();
		layoutButtons(getButton(true), getButton(false));
	}

	protected Boolean getAssignedValue (AbstractButton b)
	{
		if ((null == b) || (!b.isSelected()) || (!b.isEnabled()))
			return null;
		
		if (!(b instanceof TypedComponentAssignment<?>))
			return null;

		final Object	bv=((TypedComponentAssignment<?>) b).getAssignedValue();
		if (!(bv instanceof Boolean))
			return null;

		return (Boolean) bv;
	}

	private static final boolean[]	OPTVALS={ true, false };
	/*
	 * @see net.community.chest.ui.helpers.TypedComponentAssignment#getAssignedValue()
	 */
	@Override
	public Boolean getAssignedValue ()
	{
		final ButtonGroup	btg=getButtonGroup();
		if ((null == btg) || (btg.getButtonCount() <= 0))
		{
			for (final boolean optVal : OPTVALS)
			{
				final Boolean	v=getAssignedValue(getButton(optVal));
				if (v != null)
					return v;
			}
		}
		else
		{
			for (final Enumeration<? extends AbstractButton>	bte=btg.getElements();
					(bte != null) && bte.hasMoreElements(); )
			{
				final Boolean	v=getAssignedValue(bte.nextElement());
				if (v != null)
					return v;
			}
		}

		return null;
	}

	protected void setAssignedValue (AbstractButton b, Boolean value)
	{
		if (null == b)
			return;

		if (null == value)
		{
			b.setEnabled(false);
			return;
		}

		if (!(b instanceof TypedComponentAssignment<?>))
			return;

		final Object	bv=((TypedComponentAssignment<?>) b).getAssignedValue();
		if (!(bv instanceof Boolean))
			return;

		b.setSelected(value.equals(bv));
	}
	/* null means disabled
	 * @see net.community.chest.ui.helpers.TypedComponentAssignment#setAssignedValue(java.lang.Object)
	 */
	@Override
	public void setAssignedValue (final Boolean value)
	{
		final ButtonGroup	btg=getButtonGroup();
		if ((null == btg) || (btg.getButtonCount() <= 0))
		{
			setAssignedValue(getButton(true), value);
			setAssignedValue(getButton(false), value);
		}
		else
		{
			for (final Enumeration<? extends AbstractButton>	bte=btg.getElements();
				(bte != null) && bte.hasMoreElements(); )
				setAssignedValue(bte.nextElement(), value);
		}
	}

	public static final int DEFAULT_LINE_THICKNESS=2;
	protected Border createStateBorder (Color c)
	{
		return (null == c) ? null : BorderFactory.createLineBorder(c, DEFAULT_LINE_THICKNESS);
	}

	private Color	_errColor;
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#getErrFieldColor()
	 */
	@Override
	public Color getErrFieldColor ()
	{
		if (null == _errColor)
			_errColor = DEFAULT_ERR_COLOR;
		return _errColor;
	}

	private Border	_errBorder	/* =null */;
	public Border getErrBorder ()
	{
		if (null == _errBorder)
			_errBorder = createStateBorder(getErrFieldColor());
		return _errBorder;
	}

	public void setErrBorder (Border b)
	{
		_errBorder = b;
	}
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#setErrFieldColor(java.awt.Color)
	 */
	@Override
	public void setErrFieldColor (Color errColor)
	{
		if ((errColor != null) && (!errColor.equals(_errColor)))
		{
			_errColor = errColor;
			// force re-creation on next update
			setErrBorder(null);
		}
	}

	private Color	_okColor;
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#getOkFieldColor()
	 */
	@Override
	public Color getOkFieldColor ()
	{
		if (null == _okColor)
			_okColor = DEFAULT_OK_COLOR;
		return _okColor;
	}

	private Border	_okBorder	/* =null */;
	public Border getOkBorder ()
	{
		// NOTE: OK border is empty by default whereas ERR border is auto-created
		return _okBorder;
	}

	public void setOkBorder (Border b)
	{
		if (_okBorder != b)
			_okBorder = b;
	}
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#setOkFieldColor(java.awt.Color)
	 */
	@Override
	public void setOkFieldColor (Color okColor)
	{
		if ((okColor != null) && (!okColor.equals(_okColor)))
		{
			_okColor = okColor;
			// force re-creation on next update
			setOkBorder(null);
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#isValidData()
	 */
	@Override
	public boolean isValidData ()
	{
		return (getAssignedValue() != null);
	}
	/*
	 * @see javax.swing.JComponent#setBorder(javax.swing.border.Border)
	 */
	@Override
	public void setBorder (Border b)
	{
		super.setBorder(b);
		// force re-creation on next update
		setErrBorder(null);
		setOkBorder(null);
	}

	protected Border updateBorderColor (final boolean validData)
	{
		final Border	b=ValidatorUtils.resolveValidatorBorder(this, validData, getOkBorder(), getErrBorder());
		super.setBorder(b);	// do not disturb the current OK/ERR borders
		return b;
	}

	private Set<ChangeListener>	_cl;
	protected int fireChangeEvent ()
	{
		return ChangeListenerSet.fireChangeEventForSource(this, _cl, true);
	}
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#addDataChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public boolean addDataChangeListener (ChangeListener l)
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
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#removeDataChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public boolean removeDataChangeListener (ChangeListener l)
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
	/*
	 * @see net.community.chest.ui.helpers.input.InputFieldValidator#signalDataChanged(boolean)
	 */
	@Override
	public int signalDataChanged (boolean fireEvent)
	{
		updateBorderColor(isValidData());

		if (fireEvent)
			return fireChangeEvent();
		else
			return 0;
	}
}

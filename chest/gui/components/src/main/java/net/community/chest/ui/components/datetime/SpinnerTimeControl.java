/*
 * 
 */
package net.community.chest.ui.components.datetime;

import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import net.community.chest.awt.layout.FlowLayoutAlignment;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.util.datetime.CalendarFieldType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 18, 2010 7:17:43 AM
 */
public class SpinnerTimeControl extends AbstractSpinnerDateTimeControl {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2145222156952820599L;
	public SpinnerTimeControl (FlowLayout l, Document doc, boolean autoLayout)
	{
		super(l, doc, autoLayout);
	}

	public SpinnerTimeControl (FlowLayout l, Element elem, boolean autoLayout)
	{
		this(l, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public SpinnerTimeControl (FlowLayout l, Element elem)
	{
		this(l, elem, true);
	}

	public SpinnerTimeControl (FlowLayout l, Document doc)
	{
		this(l, doc, true);
	}

	public SpinnerTimeControl (FlowLayout l, boolean autoLayout)
	{
		this(l, (Document) null, autoLayout);
	}

	public SpinnerTimeControl (FlowLayout l)
	{
		this(l, true);
	}

	public SpinnerTimeControl (FlowLayoutAlignment align, int hGap, int vGap, boolean autoLayout)
	{
		this(new FlowLayout(((null == align) ? DEFAULT_ALIGNMENT : align).getAlignment(), hGap, vGap), (Document) null, autoLayout);
	}

	public SpinnerTimeControl (FlowLayoutAlignment align, int hGap, int vGap)
	{
		this(align, hGap, vGap, true);
	}

	public SpinnerTimeControl (int hGap, int vGap, boolean autoLayout)
	{
		this(DEFAULT_ALIGNMENT, hGap, vGap, autoLayout);
	}

	public SpinnerTimeControl (int hGap, int vGap)
	{
		this(hGap, vGap, true);
	}

	public SpinnerTimeControl (FlowLayoutAlignment align, boolean autoLayout)
	{
		this(align, DEFAULT_HGAP, DEFAULT_VGAP, autoLayout);
	}

	public SpinnerTimeControl (FlowLayoutAlignment align)
	{
		this(align, true);
	}

	public SpinnerTimeControl (boolean autoLayout)
	{
		this(DEFAULT_ALIGNMENT, autoLayout);
	}

	public SpinnerTimeControl ()
	{
		this(true);
	}
	/*
	 * @see net.community.chest.ui.components.datetime.AbstractSpinnerDateTimeControl#createSpinnerModel(net.community.chest.util.datetime.CalendarFieldType)
	 */
	@Override
	protected SpinnerModel createSpinnerModel (final CalendarFieldType calField)
	{
		if (null == calField)
			return null;

		final Calendar	c=Calendar.getInstance();
		final int		calValue=calField.getFieldValue(c);
		switch(calField)
		{
			case HOUR	:
				return new SpinnerNumberModel(calValue, 0, 23, 1);
			case H1224	:
				return new SpinnerNumberModel(calValue, 1, 12, 1);
			case MINUTE	:
				return new SpinnerNumberModel(calValue, 0, 59, 1);
			case SECOND	:
				return new SpinnerNumberModel(calValue, 0, 59, 1);
			case MSEC	:
				return new SpinnerNumberModel(calValue, 0, 999, 1);
			default		:
				throw new UnsupportedOperationException("createSpinnerModel(" + calField + ") N/A");
		}
	}
	/*
	 * @see net.community.chest.ui.components.datetime.AbstractSpinnerDateTimeControl#setSpinnerEditor(net.community.chest.util.datetime.CalendarFieldType, javax.swing.JSpinner)
	 */
	@Override
	protected JComponent setSpinnerEditor (CalendarFieldType calField, JSpinner s)
	{
		if ((null == calField) || (null == s))
			return null;

		switch(calField)
		{
			case HOUR	:
			case H1224	:
			case MINUTE	:
			case SECOND	:
			case MSEC	:
				{
					final JComponent	e=new JSpinner.NumberEditor(s);
					s.setEditor(e);
					return e;
				}

			default		:
				throw new UnsupportedOperationException("setSpinnerEditor(" + calField + ") N/A");
		}
	}

	public static final List<CalendarFieldType>	DEFAULT_FIELDS_ORDER=
		Arrays.asList(CalendarFieldType.HOUR, CalendarFieldType.MINUTE, CalendarFieldType.SECOND);
	/*
	 * @see net.community.chest.ui.components.datetime.AbstractSpinnerDateTimeControl#getFieldsOrder()
	 */
	@Override
	public List<CalendarFieldType> getFieldsOrder ()
	{
		return DEFAULT_FIELDS_ORDER;
	}

	public JSpinner getHourSpinner ()
	{
		return getSpinner(CalendarFieldType.HOUR);
	}

	public JSpinner getHour1224Spinner ()
	{
		return getSpinner(CalendarFieldType.H1224);
	}

	public JSpinner getMinuteSpinner ()
	{
		return getSpinner(CalendarFieldType.MINUTE);
	}

	public JSpinner getSecondSpinner ()
	{
		return getSpinner(CalendarFieldType.SECOND);
	}

	public JSpinner getMsecSpinner ()
	{
		return getSpinner(CalendarFieldType.MSEC);
	}
}

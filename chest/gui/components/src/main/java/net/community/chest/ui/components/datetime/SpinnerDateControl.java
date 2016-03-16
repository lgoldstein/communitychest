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
import net.community.chest.ui.helpers.spinner.EnumSpinnerListModel;
import net.community.chest.util.datetime.CalendarFieldType;
import net.community.chest.util.datetime.DateUtil;
import net.community.chest.util.datetime.MonthsValues;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since May 11, 2010 2:51:46 PM
 */
public class SpinnerDateControl extends AbstractSpinnerDateTimeControl {
    /**
     *
     */
    private static final long serialVersionUID = -765725658128895990L;
    public SpinnerDateControl (FlowLayout l, Document doc, boolean autoLayout)
    {
        super(l, doc, autoLayout);
    }

    public SpinnerDateControl (FlowLayout l, Element elem, boolean autoLayout)
    {
        this(l, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
    }

    public SpinnerDateControl (FlowLayout l, Element elem)
    {
        this(l, elem, true);
    }

    public SpinnerDateControl (FlowLayout l, Document doc)
    {
        this(l, doc, true);
    }

    public SpinnerDateControl (FlowLayout l, boolean autoLayout)
    {
        this(l, (Document) null, autoLayout);
    }

    public SpinnerDateControl (FlowLayout l)
    {
        this(l, true);
    }

    public SpinnerDateControl (FlowLayoutAlignment align, int hGap, int vGap, boolean autoLayout)
    {
        this(new FlowLayout(((null == align) ? DEFAULT_ALIGNMENT : align).getAlignment(), hGap, vGap), (Document) null, autoLayout);
    }

    public SpinnerDateControl (FlowLayoutAlignment align, int hGap, int vGap)
    {
        this(align, hGap, vGap, true);
    }

    public SpinnerDateControl (int hGap, int vGap, boolean autoLayout)
    {
        this(DEFAULT_ALIGNMENT, hGap, vGap, autoLayout);
    }

    public SpinnerDateControl (int hGap, int vGap)
    {
        this(hGap, vGap, true);
    }

    public SpinnerDateControl (FlowLayoutAlignment align, boolean autoLayout)
    {
        this(align, DEFAULT_HGAP, DEFAULT_VGAP, autoLayout);
    }

    public SpinnerDateControl (FlowLayoutAlignment align)
    {
        this(align, true);
    }

    public SpinnerDateControl (boolean autoLayout)
    {
        this(DEFAULT_ALIGNMENT, autoLayout);
    }

    public SpinnerDateControl ()
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

        final Calendar    c=Calendar.getInstance();
        final int        calValue=calField.getFieldValue(c);
        switch(calField)
        {
            case DAY    :
                return new SpinnerNumberModel(calValue, 1, DateUtil.MAX_DAYS_PER_MONTH, 1);

            case MONTH    :
                {
                    final EnumSpinnerListModel<MonthsValues> model=
                        new EnumSpinnerListModel<MonthsValues>(MonthsValues.class, true);
                    model.addAllValues();

                    final MonthsValues    mValue=MonthsValues.fromCalendarValue(calValue);
                    model.setValue(mValue);
                    return model;
                }

            case YEAR    :
                return new SpinnerNumberModel(calValue, 1970, 2500, 1);

            default        :
                throw new UnsupportedOperationException("createSpinnerModel(" + calField + ") N/A");
        }
    }
    /*
     * @see net.community.chest.ui.components.datetime.AbstractSpinnerDateTimeControl#setSpinnerEditor(net.community.chest.util.datetime.CalendarFieldType, javax.swing.JSpinner)
     */
    @Override
    protected JComponent setSpinnerEditor (final CalendarFieldType calField, final JSpinner s)
    {
        if ((null == calField) || (null == s))
            return null;

        switch(calField)
        {
            case DAY    :
            case YEAR    :
                {
                    final JComponent    e=new JSpinner.NumberEditor(s);
                    s.setEditor(e);
                    return e;
                }

            case MONTH    :
                return null;

            default        :
                throw new UnsupportedOperationException("setSpinnerEditor(" + calField + ") N/A");
        }
    }

    public static final List<CalendarFieldType>    DEFAULT_FIELDS_ORDER=
        Arrays.asList(CalendarFieldType.DAY, CalendarFieldType.MONTH, CalendarFieldType.YEAR);
    /*
     * @see net.community.chest.ui.components.datetime.AbstractSpinnerDateTimeControl#getFieldsOrder()
     */
    @Override
    public List<CalendarFieldType> getFieldsOrder ()
    {
        return DEFAULT_FIELDS_ORDER;
    }

    public JSpinner getDaysSpinner ()
    {
        return getSpinner(CalendarFieldType.DAY);
    }

    public void setDaysSpinner (JSpinner daysSpinner)
    {
        setSpinner(CalendarFieldType.DAY, daysSpinner);
    }

    public JSpinner getMonthsSpinner ()
    {
        return getSpinner(CalendarFieldType.MONTH);
    }

    public void setMonthsSpinner (JSpinner monthsSpinner)
    {
        setSpinner(CalendarFieldType.MONTH, monthsSpinner);
    }

    public JSpinner getYearsSpinner ()
    {
        return getSpinner(CalendarFieldType.YEAR);
    }

    public void setYearsSpinner (JSpinner yearsSpinner)
    {
        setSpinner(CalendarFieldType.YEAR, yearsSpinner);
    }

    /*
     * @see net.community.chest.ui.components.datetime.AbstractSpinnerDateTimeControl#getCalendarFieldValue(net.community.chest.util.datetime.CalendarFieldType, java.lang.Object)
     */
    @Override
    protected Number getCalendarFieldValue (CalendarFieldType calField, Object v)
    {
        if (CalendarFieldType.MONTH.equals(calField))
            return Integer.valueOf(((MonthsValues) v).getFieldValue());
        else
            return super.getCalendarFieldValue(calField, v);
    }

    public Calendar getValue (boolean asEndOfDay)
    {
        final Calendar    cal=super.getValue();
        if (cal == null)
            return null;

        cal.set(Calendar.HOUR_OF_DAY, asEndOfDay ? 23 : 0);
        cal.set(Calendar.MINUTE, asEndOfDay ? 59 : 0);
        cal.set(Calendar.SECOND, asEndOfDay ? 59 : 0);

        return cal;
    }
    /*
     * @see net.community.chest.ui.components.datetime.AbstractSpinnerDateTimeControl#getValue()
     */
    @Override
    public Calendar getValue ()
    {
        return getValue(false);
    }
    /*
     * @see net.community.chest.ui.components.datetime.AbstractSpinnerDateTimeControl#getCalendarFieldValue(net.community.chest.util.datetime.CalendarFieldType, java.util.Calendar)
     */
    @Override
    protected Object getCalendarFieldValue (final CalendarFieldType calField, final Calendar c)
    {
        final Object    v=super.getCalendarFieldValue(calField, c);
        if (CalendarFieldType.MONTH.equals(calField))
            return MonthsValues.fromCalendarValue(((Number) v).intValue());
        else
            return v;
    }
}

/*
 *
 */
package net.community.chest.ui.components.datetime;

import java.awt.FlowLayout;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.awt.layout.FlowLayoutAlignment;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.ui.helpers.panel.PresetFlowLayoutPanel;
import net.community.chest.util.datetime.CalendarFieldType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 18, 2010 7:22:42 AM
 */
public abstract class AbstractSpinnerDateTimeControl extends PresetFlowLayoutPanel implements TypedComponentAssignment<Calendar> {
    /**
     *
     */
    private static final long serialVersionUID = -1932668676512423487L;
    protected AbstractSpinnerDateTimeControl (FlowLayout l, Document doc, boolean autoLayout)
    {
        super(l, doc, autoLayout);
    }

    protected AbstractSpinnerDateTimeControl (FlowLayout l, Element elem, boolean autoLayout)
    {
        this(l, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
    }

    protected AbstractSpinnerDateTimeControl (FlowLayout l, Element elem)
    {
        this(l, elem, true);
    }

    protected AbstractSpinnerDateTimeControl (FlowLayout l, Document doc)
    {
        this(l, doc, true);
    }

    protected AbstractSpinnerDateTimeControl (FlowLayout l, boolean autoLayout)
    {
        this(l, (Document) null, autoLayout);
    }

    protected AbstractSpinnerDateTimeControl (FlowLayout l)
    {
        this(l, true);
    }

    protected AbstractSpinnerDateTimeControl (FlowLayoutAlignment align, int hGap, int vGap, boolean autoLayout)
    {
        this(new FlowLayout(((null == align) ? DEFAULT_ALIGNMENT : align).getAlignment(), hGap, vGap), (Document) null, autoLayout);
    }

    protected AbstractSpinnerDateTimeControl (FlowLayoutAlignment align, int hGap, int vGap)
    {
        this(align, hGap, vGap, true);
    }

    protected AbstractSpinnerDateTimeControl (int hGap, int vGap, boolean autoLayout)
    {
        this(DEFAULT_ALIGNMENT, hGap, vGap, autoLayout);
    }

    protected AbstractSpinnerDateTimeControl (int hGap, int vGap)
    {
        this(hGap, vGap, true);
    }

    protected AbstractSpinnerDateTimeControl (FlowLayoutAlignment align, boolean autoLayout)
    {
        this(align, DEFAULT_HGAP, DEFAULT_VGAP, autoLayout);
    }

    protected AbstractSpinnerDateTimeControl (FlowLayoutAlignment align)
    {
        this(align, true);
    }

    protected AbstractSpinnerDateTimeControl (boolean autoLayout)
    {
        this(DEFAULT_ALIGNMENT, autoLayout);
    }

    protected AbstractSpinnerDateTimeControl ()
    {
        this(true);
    }

    protected abstract SpinnerModel createSpinnerModel (final CalendarFieldType calField);
    protected abstract JComponent setSpinnerEditor (final CalendarFieldType calField, final JSpinner s);

    protected JSpinner createSpinner (final CalendarFieldType calField)
    {
        final SpinnerModel    model=createSpinnerModel(calField);
        if (null == model)
            return null;

        final JSpinner    s=new JSpinner(model);
        setSpinnerEditor(calField, s);
        return s;
    }

    private Map<CalendarFieldType,JSpinner>    _spinnersMap;
    protected Map<CalendarFieldType,JSpinner> getSpinnersMap ()
    {
        if (null == _spinnersMap)
            _spinnersMap = new EnumMap<CalendarFieldType,JSpinner>(CalendarFieldType.class);
        return _spinnersMap;
    }

    protected JSpinner getSpinner (CalendarFieldType calField, boolean createIfNotExist)
    {
        if (null == calField)
            return null;

        final Map<CalendarFieldType,JSpinner>    sMap=getSpinnersMap();
        JSpinner                                s=sMap.get(calField);
        if ((null == s) && createIfNotExist)
        {
            if (null == (s=createSpinner(calField)))
                return null;
            sMap.put(calField, s);
        }

        return s;
    }

    public JSpinner getSpinner (CalendarFieldType calField)
    {
        return getSpinner(calField, false);
    }

    protected void setSpinner (CalendarFieldType calField, JSpinner s)
    {
        if (null == calField)
            return;

        final Map<CalendarFieldType,JSpinner>    sMap=getSpinnersMap();
        final JSpinner    prev=
            (null == s) ? sMap.remove(calField) : sMap.put(calField, s);
        if (prev != null)
            return;    // debug breakpoint
    }

    public abstract List<CalendarFieldType> getFieldsOrder ();

    protected Number getCalendarFieldValue (final CalendarFieldType calField, final Object v)
    {
        if ((calField == null) || (!(v instanceof Number)))
            return null;

        return (Number) v;
    }

    public Calendar getValue ()
    {
        final Collection<CalendarFieldType>    fields=getFieldsOrder();
        if ((fields == null) || fields.isEmpty())
            return null;

        final Calendar    cal=Calendar.getInstance();
        for (final CalendarFieldType calField : fields)
        {
            final JSpinner    s=getSpinner(calField);
            if (null == s)
                return null;

            Object    v=s.getValue();
            if (v instanceof Map.Entry<?,?>)
                v = ((Map.Entry<?,?>) v).getValue();

            final Number    n=getCalendarFieldValue(calField, v);
            if (n == null)
                continue;

            calField.setFieldValue(cal, n.intValue());
        }

        return cal;
    }
    /*
     * @see net.community.chest.awt.TypedComponentAssignment#getAssignedValue()
     */
    @Override
    public Calendar getAssignedValue ()
    {
        return getValue();
    }

    protected Object getCalendarFieldValue (final CalendarFieldType calField, final Calendar c)
    {
        if ((calField == null) || (c == null))
            return null;
        else
            return Integer.valueOf(calField.getFieldValue(c));
    }

    public void setValue (Calendar c)
    {
        final Collection<CalendarFieldType>    fields=(c == null) ? null : getFieldsOrder();
        if ((fields == null) || fields.isEmpty())
            return;

        for (final CalendarFieldType calField : fields)
        {
            final JSpinner        s=getSpinner(calField);
            final SpinnerModel    m=(null == s) ? null : s.getModel();
            if (null == m)
                continue;

            final Object    v=getCalendarFieldValue(calField, c);
            if (v == null)
                continue;

            m.setValue(v);
        }
    }
    /*
     * @see net.community.chest.awt.TypedComponentAssignment#setAssignedValue(java.lang.Object)
     */
    @Override
    public void setAssignedValue (Calendar value)
    {
        setValue(value);
    }
    /*
     * @see net.community.chest.ui.helpers.panel.HelperPanel#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final Collection<CalendarFieldType>    fields=getFieldsOrder();
        if ((fields == null) || fields.isEmpty())
            return;

        for (final CalendarFieldType calField : fields)
        {
            final JSpinner    s=getSpinner(calField, true);
            if (null == s)
                continue;

            add(s);
        }
    }
}

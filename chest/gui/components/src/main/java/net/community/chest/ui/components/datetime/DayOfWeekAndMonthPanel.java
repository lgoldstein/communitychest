/*
 *
 */
package net.community.chest.ui.components.datetime;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.dom.DOMUtils;
import net.community.chest.ui.helpers.combobox.TypedComboBox;
import net.community.chest.ui.helpers.combobox.TypedComboBoxActionListener;
import net.community.chest.ui.helpers.panel.PresetBorderLayoutPanel;
import net.community.chest.ui.helpers.spinner.TypedListSpinner;
import net.community.chest.util.datetime.MonthsValues;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 16, 2008 11:02:09 AM
 */
public class DayOfWeekAndMonthPanel extends PresetBorderLayoutPanel {
    /**
     *
     */
    private static final long serialVersionUID = -6719159069537378072L;
    private DateWithDayOfWeekPanel    _dowPanel    /* =null */;
    public DateWithDayOfWeekPanel getDateWithDayOfWeekPanel ()
    {
        return _dowPanel;
    }
    // CAVEAT EMPTOR
    public void setDateWithDayOfWeekPanel (DateWithDayOfWeekPanel p)
    {
        if (_dowPanel != p)
            _dowPanel = p;
    }

    protected DateWithDayOfWeekPanel createDateWithDayOfWeekPanel (Element elem, boolean useElement)
    {
        if ((null == elem) || (!useElement))
            return new DateWithDayOfWeekPanel();

        return new DateWithDayOfWeekPanel(elem);
    }

    private JComponent    _selMonth    /* =null */;
    public JComponent getMonthSelectorComponent ()
    {
        return _selMonth;
    }
    // CAVEAT EMPTOR
    public void setMonthSelectorComponent (JComponent c)
    {
        if (_selMonth != null)
            _selMonth = c;
    }

    protected class MonthSelectionListener
                extends TypedComboBoxActionListener<MonthsValues,MonthOfYearComboBox>
                implements ChangeListener {
        private final JComponent    _selComp;
        public final JComponent getMonthSelectionComponent ()
        {
            return _selComp;
        }

        protected MonthSelectionListener (final JComponent selComp)
        {
            if (null == (_selComp=selComp))
                throw new IllegalArgumentException("No selection component provided");
        }

        protected void setSelectedMonth (final MonthsValues value)
        {
            final DateWithDayOfWeekPanel    ddp=(null == value) ? null : getDateWithDayOfWeekPanel();
            final DayOfMonthValuePanel        dvp=(null == ddp) ? null : ddp.getDayOfMonthValuePanel();
            if (dvp != null)
            {
                final int    dayValue=dvp.getDay();
                // ugly hack to preserve the same selected day
                dvp.setComponentValue((dayValue <= 28) ? dayValue : 1, value, dvp.getYear());
            }
        }
        /*
         * @see net.community.chest.ui.helpers.combobox.TypedComboBoxActionListener#handleSelectedItem(java.awt.event.ActionEvent, net.community.chest.ui.helpers.combobox.TypedComboBox, java.lang.String, java.lang.Object)
         */
        @Override
        public void handleSelectedItem (ActionEvent e, MonthOfYearComboBox selMonth, String text, MonthsValues value)
        {
            setSelectedMonth(value);
        }
        /*
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        @Override
        public void stateChanged (ChangeEvent e)
        {
            final Object                src=(null == e) ? null : e.getSource();
            final TypedListSpinner<?>    m=(src instanceof TypedListSpinner<?>) ? (TypedListSpinner<?>) src : null;
            final Map.Entry<String,?>    vp=(null == m) ? null : m.getValue();
            final Object                sv=(null == vp) ? null : vp.getValue();
            if (sv instanceof MonthsValues)
                setSelectedMonth((MonthsValues) sv);
        }
    }

    protected JComboBox createComboBoxMonthSelector (
            @SuppressWarnings("unused") Element elem,
            @SuppressWarnings("unused") boolean useElement)
    {
        final MonthOfYearComboBox    cb=new MonthOfYearComboBox(getLocale());
        cb.addActionListener(new MonthSelectionListener(cb));
        return cb;
    }

    protected JSpinner createSpinnerMonthSelector (
            @SuppressWarnings("unused") Element elem,
            @SuppressWarnings("unused") boolean useElement)
    {
        final MonthOfYearSpinner    sp=new MonthOfYearSpinner(getLocale());
        final JComponent            ec=sp.getEditor();
        final JTextField            f=(ec instanceof JSpinner.DefaultEditor) ? ((JSpinner.DefaultEditor) ec).getTextField() : null;
        if (f != null)
            f.setHorizontalAlignment(SwingConstants.LEFT);
        sp.addChangeListener(new MonthSelectionListener(sp));

        return sp;
    }

    protected JComponent createMonthSelectorComponent (Element elem, boolean useElement)
    {
//        return createComboBoxMonthSelector(elem, useElement);
        return createSpinnerMonthSelector(elem, useElement);
    }

    public BorderLayoutPosition getMonthSelectorPosition ()
    {
        return BorderLayoutPosition.NORTH;
    }

    protected JComponent layoutMonthSelectorComponent (Element elem, boolean useElement)
    {
        JComponent    c=getMonthSelectorComponent();
        if (null == c)
        {
            if ((c=createMonthSelectorComponent(elem, useElement)) != null)
                setMonthSelectorComponent(c);
        }

        if (c != null)
        {
            final BorderLayoutPosition    pos=getMonthSelectorPosition();
            if ((null == pos) || BorderLayoutPosition.CENTER.equals(pos))
                throw new IllegalStateException("layoutMonthSelectorComponent(" + DOMUtils.toString(elem) + ") illegal position: " + pos);
            add(c, pos);
        }

        return c;
    }

    protected JComponent layoutDayOfMonthComponent (Element elem, boolean useElement)
    {
        DateWithDayOfWeekPanel    p=getDateWithDayOfWeekPanel();
        if (null == p)
        {
            if ((p=createDateWithDayOfWeekPanel(elem, useElement)) != null)
                setDateWithDayOfWeekPanel(p);
        }

        if (p != null)
            add(p, BorderLayout.CENTER);

        return p;
    }
    /*
      * @see net.community.chest.ui.helpers.panel.HelperPanel#layoutComponent(org.w3c.dom.Element)
     */
    @Override
    public void layoutComponent (Element elem) throws RuntimeException
    {
        super.layoutComponent(elem);

        final JComponent    selMonth=layoutMonthSelectorComponent(elem, false),
                            dowComp=layoutDayOfMonthComponent(elem, false);
        // set the current selected month according to the panel
        if (dowComp instanceof DateWithDayOfWeekPanel)
        {
            final DayOfMonthValuePanel    dvp=((DateWithDayOfWeekPanel) dowComp).getDayOfMonthValuePanel();
            final MonthsValues            mth=(null == dvp) ? null : dvp.getMonth();
            if (selMonth instanceof TypedComboBox<?>)
            {
                @SuppressWarnings("unchecked")
                final TypedComboBox<MonthsValues>    cb=(TypedComboBox<MonthsValues>) selMonth;
                final MonthsValues                    cbv=cb.getSelectedValue();
                if ((mth != null) && (!mth.equals(cbv)))
                {
                    final int    selIndex=cb.setSelectedValue(mth);
                    if (selIndex < 0)    // debug breakpoint
                        return;
                }
            }
            else if (selMonth instanceof TypedListSpinner<?>)
            {
                @SuppressWarnings("unchecked")
                final TypedListSpinner<MonthsValues>    sp=(TypedListSpinner<MonthsValues>) selMonth;
                final Map.Entry<String,MonthsValues>    item=sp.getValue();
                final MonthsValues                        cbv=(null == item) ? null : item.getValue();
                if ((mth != null) && (!mth.equals(cbv)))
                    sp.setValue(mth);
            }
        }
    }
    public DayOfWeekAndMonthPanel (boolean autoLayout)
    {
        super(autoLayout);
    }

    public DayOfWeekAndMonthPanel ()
    {
        this(true);
    }

    public DayOfWeekAndMonthPanel (Document doc)
    {
        super(doc);
    }

    public DayOfWeekAndMonthPanel (Element elem)
    {
        super(elem);
    }

    public DayOfWeekAndMonthPanel (int hgap, int vgap, boolean autoLayout)
    {
        super(hgap, vgap, autoLayout);
    }

    public DayOfWeekAndMonthPanel (int hgap, int vgap)
    {
        this(hgap, vgap, true);
    }
}

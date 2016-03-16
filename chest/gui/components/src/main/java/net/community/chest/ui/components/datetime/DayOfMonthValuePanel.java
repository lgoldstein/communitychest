/*
 *
 */
package net.community.chest.ui.components.datetime;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.community.chest.awt.dom.converter.InsetsValueInstantiator;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.swing.event.ChangeListenerSet;
import net.community.chest.ui.helpers.panel.PresetGridLayoutPanel;
import net.community.chest.util.datetime.DateUtil;
import net.community.chest.util.datetime.DaysValues;
import net.community.chest.util.datetime.MonthsValues;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides a panel populated with the days of the month buttons
 * along with ability to monitor selection changes</P>
 *
 * @author Lyor G.
 * @since Dec 14, 2008 2:20:11 PM
 */
public class DayOfMonthValuePanel extends PresetGridLayoutPanel implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -7289165807393951464L;
    private int    _year    /* =0 */;
    public int getYear ()
    {
        return _year;
    }

    public void setYear (int year)
    {
        if (_year != year)
            _year = year;
    }

    private MonthsValues    _month    /* =null */;
    public MonthsValues getMonth ()
    {
        return _month;
    }

    public void setMonth (MonthsValues month)
    {
        if (_month != month)    // debug breakpoint
            _month = month;
    }

    public MonthsValues setMonth (final int mthValue)
    {
        final MonthsValues    m=MonthsValues.fromCalendarValue(mthValue);
        if (m != null)
            setMonth(m);
        return m;
    }

    private int    _day    /* =0 */;
    public int getDay ()
    {
        return _day;
    }

    public void setDay (int day)
    {
        if (_day != day)    // debug breakpoint
            _day = day;
    }

    public boolean isValidSelectedDate ()
    {
        final MonthsValues    m=getMonth();
        final int            startDay=getDay(),
                            startYear=getYear(),
                            startMonth=(null == m) ? (-1) : m.getCalendarFieldId(),
                            numDays=DateUtil.getDaysPerMonth(startMonth, startYear);
        if ((startDay <= 0) || (startYear <= 0) || (startDay > numDays)
         || (startMonth < Calendar.JANUARY) || (startMonth > Calendar.DECEMBER))
            return false;

        return true;
    }
    // returns input parameter if successfully updated, null otherwise
    public Calendar updateSelectedDate (final Calendar cal)
    {
        if (null == cal)
            return cal;

        final MonthsValues    m=getMonth();
        final int            startDay=getDay(),
                            startYear=getYear(),
                            startMonth=(null == m) ? (-1) : m.getCalendarFieldId(),
                            numDays=DateUtil.getDaysPerMonth(startMonth, startYear);
        if ((startDay <= 0) || (startYear <= 0) || (startDay > numDays)
         || (startMonth < Calendar.JANUARY) || (startMonth > Calendar.DECEMBER))
            return null;

        cal.clear();
        cal.set(startYear, startMonth, startDay, 0, 0, 0);

        final long    v=cal.getTimeInMillis();    // force re-calculation
        if (v <= 0L)    // debug breakpoint
            return cal;

        return cal;
    }

    public Calendar getSelectedDate ()
    {
        return isValidSelectedDate() ? updateSelectedDate(Calendar.getInstance()) : null;
    }

    public void setSelectedDate (final int startDay, final int startMonth, final int startYear)
    {
        setDay(startDay);
        setMonth(startMonth);
        setYear(startYear);
    }

    public void setSelectedDate (final Calendar c)
    {
        final Calendar    calStart=getSelectionValue(c);
        final int        startDay=calStart.get(Calendar.DAY_OF_MONTH),
                        startMonth=calStart.get(Calendar.MONTH),
                        startYear=calStart.get(Calendar.YEAR);
        setSelectedDate(startDay, startMonth, startYear);
    }

    protected static Calendar getSelectionValue (Calendar c)
    {
        if (null == c)
            return Calendar.getInstance();
        else
            return c;
    }
    // defaults for various options
    public static final Color    DEFAULT_SELECTED_DAY_FOREGROUND=Color.BLUE,
                                DEFAULT_SELECTED_DAY_BACKROUND=Color.WHITE,
                                DEFAULT_NORMAL_DAY_FOREGROUND=Color.BLACK,
                                DEFAULT_NORMAL_DAY_BACKGROUND=Color.WHITE;
    private static final Color resolveColor (final Color curColor, final Color defColor)
    {
        return (null == curColor) ? defColor : curColor;
    }

    private Color    _selDayFg;
    public Color getSelectedDayForeground ()
    {
        return resolveColor(_selDayFg, DEFAULT_SELECTED_DAY_FOREGROUND);
    }

    public void setSelectedDayForeground (final Color c)
    {
        if (c != null)
            _selDayFg = c;
    }

    private Color    _selDayBg;
    public Color getSelectedDayBackground ()
    {
        return resolveColor(_selDayBg, DEFAULT_SELECTED_DAY_BACKROUND);
    }

    public void setSelectedDayBackground (final Color c)
    {
        if (c != null)
            _selDayBg = c;
    }

    private Color    _nrmDayFg;
    public Color getNormalDayForeground ()
    {
        return resolveColor(_nrmDayFg, DEFAULT_NORMAL_DAY_FOREGROUND);
    }

    public void setNormalDayForeground (final Color c)
    {
        if (c != null)
            _nrmDayFg = c;
    }

    private Color    _nrmDayBg;
    public Color getNormalDayBackground ()
    {
        return resolveColor(_nrmDayBg, DEFAULT_NORMAL_DAY_BACKGROUND);
    }

    public void setNormalDayBackground (final Color c)
    {
        if (c != null)
            _nrmDayBg = c;
    }

    protected static class DayButton extends JButton {
        /**
         *
         */
        private static final long serialVersionUID = -560847711241126527L;
        private final int    _dayIndex;
        public final int getDayIndex ()
        {
            return _dayIndex;
        }

        public DayButton (int dayIndex, boolean selected)
        {
            if (((_dayIndex=dayIndex) <= 0) || (dayIndex > DateUtil.MAX_DAYS_PER_MONTH))
                throw new IllegalArgumentException("Bad day index: " + dayIndex);
            setText(String.valueOf(dayIndex));
            setSelected(selected);
        }

        public DayButton (int dayIndex)
        {
            this(dayIndex, false);
        }
    }

    private ChangeEvent    _selEvent    /* =null */;
    protected synchronized ChangeEvent getSelectionChangeEvent ()
    {
        if (null == _selEvent)
            _selEvent = new ChangeEvent(this);
        return _selEvent;
    }

    private Set<ChangeListener>    _cl    /* =null */;
    // returns Collection of informed listeners
    protected List<ChangeListener> fireSelectionChangedListeners ()
    {
        final List<ChangeListener>    ll;
        // we use a copy to avoid concurrent modifications
        synchronized(this)
        {
            if ((null == _cl) || (_cl.size() <= 0))
                return null;

            ll = new ArrayList<ChangeListener>(_cl);
        }

        final ChangeEvent    ce=getSelectionChangeEvent();
        for (final ChangeListener l : ll)
        {
            if (l != null)    // should not be otherwise
                l.stateChanged(ce);
        }

        return ll;
    }
    /**
     * Can be used to register for whenever the user makes a selection
     * @param l The {@link ChangeListener} instance to be fired when selected
     * day changes - ignored if <code>null</code> or already registered. The
     * provided {@link ChangeEvent} contains this panel as the source object
     * @return <code>true</code> if listener successfully registered
     */
    public synchronized boolean addChangeListener (final ChangeListener l)
    {
        if (null == l)
            return false;
        else if (null == _cl)
            _cl = new ChangeListenerSet();
        else if (_cl.contains(l))
            return false;

        return _cl.add(l);
    }
    /**
     * Can be used to un-register a selection listener
     * @param l The {@link ChangeListener} instance to be un-registered
     * ignored if <code>null</code> or  not registered
     * @return <code>true</code> if listener successfully un-registered
     */
    public synchronized boolean removeChangeListener (final ChangeListener l)
    {
        if ((null == l) || (null == _cl))
            return false;

        return _cl.remove(l);
    }

    protected boolean isPlaceholderComponent (final Object o)
    {
        return (!(o instanceof DayButton));
    }

    protected void updateDeselectedDayComponent (final Component c)
    {
        if (c instanceof JButton)
        {
            final JButton    b=(JButton) c;
            b.setSelected(false);
            b.setBackground(getNormalDayBackground());
            b.setForeground(getNormalDayForeground());
        }
    }

    protected void updateSelectedDayComponent (final Component c)
    {
        if (c instanceof JButton)
        {
            final JButton    b=(JButton) c;
            b.setSelected(true);
            b.setBackground(getSelectedDayBackground());
            b.setForeground(getSelectedDayForeground());
        }
    }
    /*
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed (ActionEvent event)
    {
        final Object    src=(null == event) ? null : event.getSource();
        if (isPlaceholderComponent(src))
            return;    // debug breakpoint

        final DayButton    dbtn=(DayButton) src;
        if (dbtn.isSelected())
            return;    // nothing to do if already selected

        final Component[]    ca=getComponents();
        if ((ca != null) && (ca.length > 0))
        {
            for (final Component c : ca)
            {
                if (isPlaceholderComponent(c))
                    continue;

                // we could have stopped at first selected button...
                final JButton    b=(JButton) c;
                if ((null == b) || (!b.isSelected()))
                    continue;

                updateDeselectedDayComponent(c);
            }
        }

        updateSelectedDayComponent(dbtn);
        setDay(dbtn.getDayIndex());
        fireSelectionChangedListeners();
    }

    protected JComponent getDayValuePlaceHolder (final DaysValues dv)
    {
        if (null == dv)
            return null;

        final JButton    dbtn=new JButton("");
        dbtn.setFocusPainted(false);
        dbtn.setMargin(InsetsValueInstantiator.NO_INSETS);
        dbtn.setBorder(BorderFactory.createEmptyBorder());
        dbtn.setOpaque(true);
        dbtn.setVisible(false);
        return dbtn;
    }

    protected JComponent getDayValueComponent (
            final DaysValues dv, final int dIndex, final boolean selected)
    {
        if ((null == dv) || (dIndex <= 0) || (dIndex > DateUtil.MAX_DAYS_PER_MONTH))
            return null;

        final DayButton    dbtn=new DayButton(dIndex);
        dbtn.setFocusPainted(false);
        dbtn.setMargin(InsetsValueInstantiator.NO_INSETS);
        dbtn.setBorder(BorderFactory.createEmptyBorder());
        dbtn.setOpaque(true);
        dbtn.setVisible(true);
        dbtn.setSelected(selected);
        dbtn.setBackground(selected ? getSelectedDayBackground() : getNormalDayBackground());
        dbtn.setForeground(selected ? getSelectedDayForeground() : getNormalDayForeground());
        dbtn.addActionListener(this);
        return dbtn;
    }
    // NOTE !!! does not change the selected value
    public void setComponentValue (final int startDay, final MonthsValues m, final int startYear)
    {
        final int    numComps=getComponentCount();
        if (numComps > 0)    // debug breakpoint
            removeAll();

        final int    startMonth=(null == m) ? (-1) : m.getCalendarFieldId(),
                    startDOW=DateUtil.getDayOfWeekForDate(1, startMonth, startYear),
                    numDays=DateUtil.getDaysPerMonth(startMonth, startYear);
        if ((startMonth < Calendar.JANUARY)
         || (startMonth > Calendar.DECEMBER)
         || (numDays <= 0)
         || (startDOW < Calendar.SUNDAY)
         || (startDOW > Calendar.SATURDAY))
            return;    // debug breakpoint

        final DaysValues        startDayValue=DaysValues.fromCalendarValue(startDOW);
        for (int dIndex=1; dIndex <= numDays; )
        {
            for (int    dwi=0; (dwi < DateUtil.DAYS_PER_WEEK) && (dIndex <= numDays); dwi++)
            {
                final DaysValues    dv=DaysValues.VALUES.get(dwi);
                boolean                useDayValue=true;
                // fill with spaces till day-of-week of 1st day of month reached
                if (1 == dIndex)
                {
                    if (!startDayValue.equals(dv))
                        useDayValue = false;
                }
                // fill with spaces till end of last week
                else if (dIndex > numDays)
                    useDayValue = false;

                final JComponent    c=useDayValue
                    ? getDayValueComponent(dv, dIndex, (startDay == dIndex))
                    : getDayValuePlaceHolder(dv)
                    ;

                if (c != null)
                    add(c);

                if (useDayValue)
                    dIndex++;
            }
        }

        setSelectedDate(startDay, startMonth, startYear);
        updateUI();
        fireSelectionChangedListeners();
    }

    public void setComponentValue (final int startDay, final int startMonth, final int startYear)
    {
        setComponentValue(startDay, MonthsValues.fromCalendarValue(startMonth), startYear);
    }

    public void setComponentDayValue (final int startDay)
    {
        setComponentValue(startDay, getMonth(), getYear());
    }

    public void setComponentMonthValue (final MonthsValues m)
    {
        setComponentValue(getDay(), m, getYear());
    }

    public void setComponentMonthValue (final int startMonth)
    {
        setComponentMonthValue(MonthsValues.fromCalendarValue(startMonth));
    }

    public void setComponentYearValue (final int startYear)
    {
        setComponentValue(getDay(), getMonth(), startYear);
    }

    // NOTE !!! updates the selected value(s)
    public Calendar setComponentValue (final Calendar c)
    {
        final Calendar            calStart=getSelectionValue(c);
        final int                startDay=calStart.get(Calendar.DAY_OF_MONTH),
                                startMonth=calStart.get(Calendar.MONTH),
                                startYear=calStart.get(Calendar.YEAR);
        setComponentValue(startDay, startMonth, startYear);
        return c;
    }
    /*
     * @see net.community.chest.ui.helpers.panel.HelperPanel#layoutComponent(org.w3c.dom.Element)
     */
    @Override
    public void layoutComponent (Element elem) throws RuntimeException
    {
        super.layoutComponent(elem);

        final Calendar    curDate=getSelectedDate();
        setComponentValue(curDate);
    }

    public DayOfMonthValuePanel (Document doc, boolean autoLayout)
    {
        super(0, DateUtil.DAYS_PER_WEEK, 5, 5, doc, autoLayout);
    }

    public DayOfMonthValuePanel (Document doc)
    {
        this(doc, true);
    }

    public DayOfMonthValuePanel (Element elem, boolean autoLayout)
    {
        this((null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
    }

    public DayOfMonthValuePanel (Element elem)
    {
        this(elem, true);
    }

    public DayOfMonthValuePanel (boolean autoLayout)
    {
        this((Document) null, autoLayout);
    }

    public DayOfMonthValuePanel ()
    {
        this(true);
    }

}

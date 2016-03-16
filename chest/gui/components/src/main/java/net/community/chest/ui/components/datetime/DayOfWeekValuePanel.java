/*
 *
 */
package net.community.chest.ui.components.datetime;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.ui.helpers.label.TypedLabel;
import net.community.chest.ui.helpers.panel.PresetGridLayoutPanel;
import net.community.chest.util.datetime.DateUtil;
import net.community.chest.util.datetime.DaysValues;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides a panel with the days of the week one after the other in
 * a row as labels</P>
 *
 * @author Lyor G.
 * @since Dec 15, 2008 3:32:21 PM
 */
public class DayOfWeekValuePanel extends PresetGridLayoutPanel {
    /**
     *
     */
    private static final long serialVersionUID = 5590741094708256818L;
    private DateFormatSymbols    _dfs;
    protected synchronized DateFormatSymbols getDateFormatSymbols ()
    {
        if (null == _dfs)
        {
            final Locale    l=getDisplayLocale();
            _dfs = DateUtil.getDateFormatSymbols(l);
        }

        return _dfs;
    }

    protected synchronized void setDateFormatSymbols (DateFormatSymbols dfs)
    {
        if (_dfs != dfs)    // debug breakpoint
            _dfs = dfs;
    }

    private Map<DaysValues,String>    _namesMap    /* =null */;
    protected synchronized Map<DaysValues,String> getWeekdayNamesMap ()
    {
        if (null == _namesMap)
            _namesMap = DaysValues.getWeekdayNamesMap(getDateFormatSymbols(), true);
        return _namesMap;
    }

    protected synchronized void setWeekdayNamesMap (Map<DaysValues,String> nm)
    {
        if (_namesMap != nm)    // debug breakpoint
            _namesMap = nm;
    }

    protected String getDayDisplayText (final DaysValues d)
    {
        final Map<DaysValues,String>    nm=
            (null == d) ? null : getWeekdayNamesMap();
        final String                    n=
            ((null == nm) || (nm.size() <= 0)) ? null : nm.get(d);
        if ((null == n) || (n.length() <= 0))
            return (null == d) ? null : d.toString();

        return n;
    }

    protected JComponent getDayComponent (final DaysValues d)
    {
        final String    n=getDayDisplayText(d);
        if ((null == n) || (n.length() <= 0))
            return null;

        return new TypedLabel<DaysValues>(DaysValues.class, d, n, SwingConstants.CENTER);
    }

    public void layoutComponent (final Collection<? extends DaysValues> days)
    {
        final int    numComps=getComponentCount();
        if (numComps > 0)    // debug breakpoint
            removeAll();

        final int    numDays=(null == days) ? 0 : days.size();
        if (numDays <= 0)
            return;

        for (final DaysValues d : days)
        {
            final JComponent    jd=getDayComponent(d);
            if (jd != null)
                add(jd);
        }
    }

    public void layoutComponent (final DaysValues ... days)
    {
        layoutComponent(((null == days) || (days.length <= 0)) ? null : Arrays.asList(days));
    }
    /*
     * @see net.community.chest.ui.helpers.panel.HelperPanel#layoutComponent(org.w3c.dom.Element)
     */
    @Override
    public void layoutComponent (Element elem) throws RuntimeException
    {
        super.layoutComponent(elem);
        layoutComponent(DaysValues.VALUES);
    }

    public DayOfWeekValuePanel (int maxDays, Document doc, boolean autoLayout)
    {
        super(1, maxDays, 5, 0, doc, autoLayout);
    }

    public DayOfWeekValuePanel (Document doc, boolean autoLayout)
    {
        this(DateUtil.DAYS_PER_WEEK, doc, autoLayout);
    }

    public DayOfWeekValuePanel (Element elem, boolean autoLayout)
    {
        this((null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
    }

    public DayOfWeekValuePanel (boolean autoLayout)
    {
        this((Document) null, autoLayout);
    }

    public DayOfWeekValuePanel ()
    {
        this(true);
    }

    public DayOfWeekValuePanel (Element elem)
    {
        this(elem, true);
    }

    public DayOfWeekValuePanel (Document doc)
    {
        this(doc, true);
    }
}

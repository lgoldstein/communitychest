/*
 *
 */
package net.community.chest.ui.components.datetime;

import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.Map;

import net.community.chest.ui.helpers.combobox.EnumComboBox;
import net.community.chest.util.datetime.DateUtil;
import net.community.chest.util.datetime.DaysValues;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 16, 2008 9:43:32 AM
 */
public class DayOfWeekComboBox extends EnumComboBox<DaysValues> {
    /**
     *
     */
    private static final long serialVersionUID = 2719088708576951195L;
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

    private boolean    _useShortNames    /* =false */;
    public boolean isShortNames ()
    {
        return _useShortNames;
    }

    public void setShortNames (boolean f)
    {
        if (_useShortNames != f)
            _useShortNames = f;
    }

    private Map<DaysValues,String>    _namesMap    /* =null */;
    protected synchronized Map<DaysValues,String> getWeekdayNamesMap ()
    {
        if (null == _namesMap)
            _namesMap = DaysValues.getWeekdayNamesMap(getDateFormatSymbols(), isShortNames());
        return _namesMap;
    }

    protected synchronized void setWeekdayNamesMap (Map<DaysValues,String> nm)
    {
        if (_namesMap != nm)    // debug breakpoint
            _namesMap = nm;
    }
    /*
     * @see net.community.chest.ui.helpers.combobox.TypedComboBox#getValueDisplayText(java.lang.Object)
     */
    @Override
    public String getValueDisplayText (final DaysValues d)
    {
        final Map<DaysValues,String>    nm=
            (null == d) ? null : getWeekdayNamesMap();
        final String                    n=
            ((null == nm) || (nm.size() <= 0)) ? null : nm.get(d);
        if ((null == n) || (n.length() <= 0))
            return super.getValueDisplayText(d);

        return n;
    }

    public DayOfWeekComboBox (Locale l, boolean autoPopulate)
    {
        // delay population till after enum values & locale initialization
        super(DaysValues.class, false);

        if (l != null)
            setDisplayLocale(l);
        setEnumValues(DaysValues.VALUES);

        if (autoPopulate)
            populate();
    }

    public DayOfWeekComboBox (Locale l)
    {
        this(l, true);
    }

    public DayOfWeekComboBox (boolean autoPopulate)
    {
        this(Locale.getDefault(), autoPopulate);
    }

    public DayOfWeekComboBox ()
    {
        this(true);
    }
}

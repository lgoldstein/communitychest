/*
 *
 */
package net.community.chest.ui.components.datetime;

import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.Map;

import net.community.chest.ui.helpers.spinner.EnumListSpinner;
import net.community.chest.util.datetime.DateUtil;
import net.community.chest.util.datetime.MonthsValues;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 16, 2008 2:33:55 PM
 */
public class MonthOfYearSpinner extends EnumListSpinner<MonthsValues> {
    /**
     *
     */
    private static final long serialVersionUID = 2086025178044436613L;
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

    private Map<MonthsValues,String>    _namesMap    /* =null */;
    protected synchronized Map<MonthsValues,String> getMonthNamesMap ()
    {
        if (null == _namesMap)
            _namesMap = MonthsValues.getMonthNamesMap(getDateFormatSymbols(), isShortNames());
        return _namesMap;
    }

    protected synchronized void setMonthNamesMap (Map<MonthsValues,String> nm)
    {
        if (_namesMap != nm)    // debug breakpoint
            _namesMap = nm;
    }
    /*
     * @see net.community.chest.ui.helpers.combobox.TypedComboBox#getValueDisplayText(java.lang.Object)
     */
    @Override
    public String getValueDisplayText (final MonthsValues m)
    {
        final Map<MonthsValues,String>    nm=
            (null == m) ? null : getMonthNamesMap();
        final String                    n=
            ((null == nm) || (nm.size() <= 0)) ? null : nm.get(m);
        if ((null == n) || (n.length() <= 0))
            return super.getValueDisplayText(m);

        return n;
    }

    public MonthOfYearSpinner (Locale l, boolean autoPopulate)
    {
        // delay population till after enum values & locale initialization
        super(MonthsValues.class, false);

        if (l != null)
            setDisplayLocale(l);
        setEnumValues(MonthsValues.VALUES);

        if (autoPopulate)
            populate();
    }

    public MonthOfYearSpinner (Locale l)
    {
        this(l, true);
    }

    public MonthOfYearSpinner (boolean autoPopulate)
    {
        this(Locale.getDefault(), autoPopulate);
    }

    public MonthOfYearSpinner ()
    {
        this(true);
    }
}

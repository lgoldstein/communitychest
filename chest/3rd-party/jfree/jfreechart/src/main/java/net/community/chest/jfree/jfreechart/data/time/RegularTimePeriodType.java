/*
 *
 */
package net.community.chest.jfree.jfreechart.data.time;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.community.chest.lang.ExceptionUtil;
import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulates the derived {@link RegularTimePeriod} classes except for
 * the {@link org.jfree.data.time.FixedMillisecond}</P>
 * @author Lyor G.
 * @since May 6, 2009 9:00:24 AM
 */
public enum RegularTimePeriodType {
    MSEC(Millisecond.class) {
            /* order is year,month,day,hour,minute,second,msec
             * @see net.community.chest.jfree.jfreechart.data.time.RegularTimePeriodType#getAttributesValues(org.jfree.data.time.RegularTimePeriod)
             */
            @Override
            protected List<Integer> getAttributesValues (final RegularTimePeriod inst)
            {
                final Millisecond    v=(Millisecond) inst;
                final List<Integer>    dl=SECOND.getAttributesValues(v.getSecond()),
                                    vl=new ArrayList<Integer>(dl.size() + 1);
                vl.addAll(dl);
                vl.add(Integer.valueOf((int) v.getMillisecond()));
                return vl;
            }
        },
    SECOND(Second.class) {
            /* order is year,month,day,hour,minute,second,msec
             * @see net.community.chest.jfree.jfreechart.data.time.RegularTimePeriodType#getAttributesValues(org.jfree.data.time.RegularTimePeriod)
             */
            @Override
            protected List<Integer> getAttributesValues (final RegularTimePeriod inst)
            {
                final Second        v=(Second) inst;
                final List<Integer>    dl=MINUTE.getAttributesValues(v.getMinute()),
                                    vl=new ArrayList<Integer>(dl.size() + 1);
                vl.addAll(dl);
                vl.add(Integer.valueOf(v.getSecond()));
                return vl;
            }
        },
    MINUTE(Minute.class) {
            /* order is year,month,day,hour,minute,second,msec
             * @see net.community.chest.jfree.jfreechart.data.time.RegularTimePeriodType#getAttributesValues(org.jfree.data.time.RegularTimePeriod)
             */
            @Override
            protected List<Integer> getAttributesValues (final RegularTimePeriod inst)
            {
                final Minute        v=(Minute) inst;
                final List<Integer>    dl=HOUR.getAttributesValues(v.getHour()),
                                    vl=new ArrayList<Integer>(dl.size() + 1);
                vl.addAll(dl);
                vl.add(Integer.valueOf(v.getMinute()));
                return vl;
            }
        },
    HOUR(Hour.class) {
            /* order is year,month,day,hour,minute,second,msec
             * @see net.community.chest.jfree.jfreechart.data.time.RegularTimePeriodType#getAttributesValues(org.jfree.data.time.RegularTimePeriod)
             */
            @Override
            protected List<Integer> getAttributesValues (final RegularTimePeriod inst)
            {
                final Hour    v=(Hour) inst;
                return Arrays.asList(Integer.valueOf(v.getYear()),
                                     Integer.valueOf(v.getMonth()),
                                     Integer.valueOf(v.getDayOfMonth()),
                                     Integer.valueOf(v.getHour()));
            }
        },
    DAY(Day.class) {
            /* order is year,month,day,hour,minute,second,msec
             * @see net.community.chest.jfree.jfreechart.data.time.RegularTimePeriodType#getAttributesValues(org.jfree.data.time.RegularTimePeriod)
             */
            @Override
            protected List<Integer> getAttributesValues (final RegularTimePeriod inst)
            {
                final Day    v=(Day) inst;
                return Arrays.asList(Integer.valueOf(v.getYear()),
                                     Integer.valueOf(v.getMonth()),
                                     Integer.valueOf(v.getDayOfMonth()));
            }
        },
    WEEK(Week.class) {
            /* order is year,month,day,hour,minute,second,msec
             * @see net.community.chest.jfree.jfreechart.data.time.RegularTimePeriodType#getAttributesValues(org.jfree.data.time.RegularTimePeriod)
             */
            @Override
            protected List<Integer> getAttributesValues (final RegularTimePeriod inst)
            {
                final Week    v=(Week) inst;
                return Arrays.asList(Integer.valueOf(v.getYearValue()),
                                     Integer.valueOf(v.getWeek()));
            }
        },
    MONTH(Month.class) {
            /* order is year,month,day,hour,minute,second,msec
             * @see net.community.chest.jfree.jfreechart.data.time.RegularTimePeriodType#getAttributesValues(org.jfree.data.time.RegularTimePeriod)
             */
            @Override
            protected List<Integer> getAttributesValues (final RegularTimePeriod inst)
            {
                final Month    v=(Month) inst;
                return Arrays.asList(Integer.valueOf(v.getYearValue()),
                                     Integer.valueOf(v.getMonth()));
            }
        },
    QUARTER(Quarter.class) {
            /* order is year,month,day,hour,minute,second,msec
             * @see net.community.chest.jfree.jfreechart.data.time.RegularTimePeriodType#getAttributesValues(org.jfree.data.time.RegularTimePeriod)
             */
            @Override
            protected List<Integer> getAttributesValues (final RegularTimePeriod inst)
            {
                final Quarter    v=(Quarter) inst;
                return Arrays.asList(Integer.valueOf(v.getYearValue()),
                                     Integer.valueOf(v.getQuarter()));
            }
        },
    YEAR(Year.class) {
            /* order is year,month,day,hour,minute,second,msec
             * @see net.community.chest.jfree.jfreechart.data.time.RegularTimePeriodType#getAttributesValues(org.jfree.data.time.RegularTimePeriod)
             */
            @Override
            protected List<Integer> getAttributesValues (final RegularTimePeriod inst)
            {
                return Arrays.asList(Integer.valueOf(((Year) inst).getYear()));
            }
        };

    private Constructor<?>    _ctor;
    private synchronized Constructor<?> getIntsConstructor ()
    {
        if (null == _ctor)
        {
            final Class<?>            c=getPeriodType();
            final Constructor<?>[]    ca=c.getConstructors();
            for (final Constructor<?> cc : ca)
            {
                final Class<?>[]    pa=cc.getParameterTypes();
                if ((null == pa) || (pa.length <= 0))
                    continue;

                boolean    allInts=true;
                for (final Class<?> pc : pa)
                {
                    if (Integer.TYPE.isAssignableFrom(pc)
                     || Integer.class.isAssignableFrom(pc))
                        continue;

                    allInts = false;
                    break;
                }

                if (allInts)
                {
                    _ctor = cc;
                    break;
                }
            }
        }

        return _ctor;
    }
    // order is year,month,day,hour,minute,second,msec
    protected abstract List<Integer> getAttributesValues (final RegularTimePeriod inst);

    public int getNumArguments ()
    {
        final Constructor<?>    cc=getIntsConstructor();
        final Class<?>[]        pa=(null == cc) ? null : cc.getParameterTypes();
        return (null == pa) ? 0 : pa.length;
    }

    // order is year,month,day,hour,minute,second,msec
    public <A extends Appendable> A appendArguments (final A sb, final RegularTimePeriod inst) throws IOException
    {
        final Class<?>    tc=getPeriodType(), ic=(null == inst) ? null : inst.getClass();
        if (null == ic)
            return sb;
        if (!tc.isAssignableFrom(ic))
            throw new ClassCastException("appendArguments() - expected=" + tc.getName() + "/got=" + ic.getName());

        final Collection<?>    vl=getAttributesValues(inst);
        if ((null == vl) || (vl.size() <= 0))
            return sb;

        boolean    isFirst=true;
        for (final Object v : vl)
        {
            if (!isFirst)
                sb.append(',');
            sb.append(String.valueOf(v));
            isFirst = false;
        }

        return sb;
    }
    // order is year,month,day,hour,minute,second,msec
    public RegularTimePeriod fromNumberArgs (final List<? extends Number> vl)
    {
        final int    numVals=(null == vl) ? 0 : vl.size();
        if (numVals <= 0)
            return null;

        final Constructor<?>    cc=getIntsConstructor();
        final Class<?>[]        pa=(null == cc) ? null : cc.getParameterTypes();
        final int                numParams=(null == pa) ? 0 : pa.length;
        if (numVals < numParams)
            throw new IllegalArgumentException("Missing values for " + name() + " - expected=" + numParams + "/got=" + numVals);

        try
        {
            if (numParams <= 0)    // should not happen
                return getPeriodType().newInstance();
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }

        /* populate the constructor arguments in reverse since the order we
         * impose is the opposite of the constructor order
         */
        final Integer[]    aa=new Integer[numParams];
        for (int    pIndex=0, nIndex=numParams - 1; pIndex < numParams; pIndex++, nIndex--)
        {
            final Number    n=vl.get(nIndex);
            if (n instanceof Integer)
                aa[pIndex] = (Integer) n;
            else
                aa[pIndex] = Integer.valueOf(n.intValue());
        }

        try
        {
            return getPeriodType().cast(cc.newInstance((Object[]) aa));
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }
    // order is year,month,day,hour,minute,second,msec
    public RegularTimePeriod fromNumberArgs (Number ... vl)
    {
        return fromNumberArgs(((null == vl) || (vl.length <= 0)) ? null : Arrays.asList(vl));
    }
    // order is year,month,day,hour,minute,second,msec
    public RegularTimePeriod fromIntArgs (int ... vl)
    {
        if ((null == vl) || (vl.length <= 0))
            return null;

        final Integer[]    na=new Integer[vl.length];
        for (int vIndex=0; vIndex < vl.length; vIndex++)
            na[vIndex] = Integer.valueOf(vl[vIndex]);

        return fromNumberArgs(na);
    }
    // order is year,month,day,hour,minute,second,msec
    public RegularTimePeriod fromStringArgs (List<String> vl) throws NumberFormatException
    {
        final int    vNum=(null == vl) ? 0 : vl.size();
        if (vNum <= 0)
            return null;

        final Integer[]    iVals=new Integer[vNum];
        for (int    vIndex=0; vIndex < vNum; vIndex++)
        {
            final String    vs=vl.get(vIndex);
            iVals[vIndex] = Integer.valueOf(vs);
        }

        return fromNumberArgs(iVals);
    }
    // order is year,month,day,hour,minute,second,msec
    public RegularTimePeriod fromStringArgs (String ... vl) throws NumberFormatException
    {
        return fromStringArgs(((null == vl) || (vl.length <= 0)) ? null : Arrays.asList(vl));
    }

    private final Class<? extends RegularTimePeriod>    _type;
    public final Class<? extends RegularTimePeriod> getPeriodType ()
    {
        return _type;
    }

    RegularTimePeriodType (Class<? extends RegularTimePeriod> t)
    {
        _type = t;
    }

    public static final List<RegularTimePeriodType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final RegularTimePeriodType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final RegularTimePeriodType fromType (final Class<?> c)
    {
        if ((null == c) || (!RegularTimePeriod.class.isAssignableFrom(c)))
            return null;

        for (final RegularTimePeriodType v : VALUES)
        {
            final Class<?>    vc=(null == v) ? null : v.getPeriodType();
            if ((vc != null) && vc.isAssignableFrom(c))
                return v;
        }

        return null;
    }

    public static final RegularTimePeriodType fromObject (final Object o)
    {
        return (null == o) ? null : fromType(o.getClass());
    }
}

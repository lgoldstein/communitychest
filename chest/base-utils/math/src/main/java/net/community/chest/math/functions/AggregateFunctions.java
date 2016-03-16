/*
 *
 */
package net.community.chest.math.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.community.chest.lang.math.DoublesComparator;
import net.community.chest.lang.math.LongsComparator;
import net.community.chest.math.NumbersFunction;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 27, 2009 11:57:42 AM
 */
public enum AggregateFunctions implements NumbersFunction {
    /**
     * Minimum member
     */
    MIN {
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#accumulate(Collection)
             */
            @Override
            public List<Number> accumulate (Collection<? extends Number> args)
            {
                final int    numArgs=(null == args) ? 0 : args.size();
                if (numArgs <= 0)
                    return null;

                final List<Number>    mins=new ArrayList<Number>(numArgs);
                Number                ret=null;
                for (final Number    n : args)
                {
                    if (null == n)
                        continue;

                    if (ret != null)
                    {
                        final int    cRes=compare(ret, n);
                        if (cRes > 0)
                            ret = n;
                    }
                    else
                        ret = n;

                    if (!mins.add(ret))
                        continue;
                }

                return mins;
            }
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#aggregate(java.util.Collection)
             */
            @Override
            public Number aggregate (final Collection<? extends Number> args)
            {
                if ((null == args) || (args.size() <= 0))
                    return null;

                Number    ret=null;
                for (final Number    n : args)
                {
                    if (null == n)
                        continue;

                    if (ret != null)
                    {
                        final int    cRes=compare(ret, n);
                        if (cRes < 0)
                            continue;
                    }

                    ret = n;
                }

                return ret;
            }
        },
    /**
     * Maximum member
     */
    MAX {
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#accumulate(Collection)
             */
            @Override
            public List<Number> accumulate (Collection<? extends Number> args)
            {
                final int    numArgs=(null == args) ? 0 : args.size();
                if (numArgs <= 0)
                    return null;

                final List<Number>    maxs=new ArrayList<Number>(numArgs);
                Number                ret=null;
                for (final Number    n : args)
                {
                    if (null == n)
                        continue;

                    if (ret != null)
                    {
                        final int    cRes=compare(ret, n);
                        if (cRes < 0)
                            ret = n;
                    }
                    else
                        ret = n;

                    if (!maxs.add(ret))
                        continue;
                }

                return maxs;
            }
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#aggregate(java.util.Collection)
             */
            @Override
            public Number aggregate (final Collection<? extends Number> args)
            {
                if ((null == args) || (args.size() <= 0))
                    return null;

                Number    ret=null;
                for (final Number    n : args)
                {
                    if (null == n)
                        continue;

                    if (ret != null)
                    {
                        final int    cRes=compare(ret, n);
                        if (cRes > 0)
                            continue;
                    }

                    ret = n;
                }

                return ret;
            }
        },
    /**
     * Sum
     */
    SUM {
            public Number calculateValue (final double retDouble, final long retLong)
            {
                if (0L == retLong)
                    return Double.valueOf(retDouble);

                if (retDouble != 0.0d)
                    return Double.valueOf(retDouble + retLong);

                return Long.valueOf(retLong);
            }
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#aggregate(java.util.Collection)
             */
            @Override
            public Number aggregate (final Collection<? extends Number> args)
            {
                if ((null == args) || (args.size() <= 0))
                    return null;

                double    retDouble=0.0d;
                long    retLong=0L;
                for (final Number    n : args)
                {
                    if (null == n)
                        continue;

                    if (MathFunctions.isFloatingPoint(n))
                        retDouble += n.doubleValue();
                    else
                        retLong += n.longValue();
                }

                return calculateValue(retDouble, retLong);
            }
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#accumulate(Collection)
             */
            @Override
            public List<Number> accumulate (Collection<? extends Number> args)
            {
                final int    numArgs=(null == args) ? 0 : args.size();
                if (numArgs <= 0)
                    return null;

                final List<Number>    sums=new ArrayList<Number>(numArgs);
                double                retDouble=0.0d;
                long                retLong=0L;
                for (final Number    n : args)
                {
                    if (null == n)
                        continue;

                    if (MathFunctions.isFloatingPoint(n))
                        retDouble += n.doubleValue();
                    else
                        retLong += n.longValue();

                    final Number    s=calculateValue(retDouble, retLong);
                    if (null == s)
                        throw new IllegalStateException("No " + name() + " value calculated "
                                                      + "for D=" + retDouble + "/L=" + retLong);
                    if (!sums.add(s))
                        continue;    // debug breakpoint
                }

                return sums;
            }
        },
        /**
         * Average
         */
        AVG {
                public Number calculateValue (final Number n, final int numArgs)
                {
                    if ((null == n) || (numArgs <= 1))
                        return n;

                    if (MathFunctions.isFloatingPoint(n))
                        return Double.valueOf(n.doubleValue() / numArgs);
                    else
                        return Double.valueOf(n.longValue() / (double) numArgs);
                }
                /*
                 * @see net.community.chest.math.functions.AggregateFunctions#aggregate(java.util.Collection)
                 */
                @Override
                public Number aggregate (final Collection<? extends Number> args)
                {
                    final Number    n=SUM.aggregate(args);
                    final int        numArgs=(null == args) ? 0 : args.size();
                    return calculateValue(n, numArgs);
                }
                /*
                 * @see net.community.chest.math.functions.AggregateFunctions#accumulate(Collection)
                 */
                @Override
                public List<Number> accumulate (Collection<? extends Number> args)
                {
                    final List<? extends Number>    sums=SUM.accumulate(args);
                    final int                        numSums=(null == sums) ? 0 : sums.size();
                    if (numSums <= 0)
                        return null;

                    final List<Number>    avgs=new ArrayList<Number>(numSums);
                    for (int    sIndex=0; sIndex < numSums; sIndex++)
                    {
                        final Number    s=sums.get(sIndex),
                                        v=calculateValue(s, sIndex+1);
                        if (null == v)
                            throw new IllegalStateException("No " + name() + " value calculated "
                                                          + "after " + sIndex + " values processed "
                                                          + "for sum=" + s);
                        if (!avgs.add(v))
                            continue;    // debug breakpoint
                    }

                    return avgs;
                }
            },
    /**
     * Product
     */
    PROD {
            public Number calculateValue (final double retDouble, final long retLong)
            {
                if ((0L == retLong) || (0.0d == retDouble))
                    return Long.valueOf(0L);

                if (1L == retLong)
                    return Double.valueOf(retDouble);

                if (retDouble != 1.0d)
                    return Double.valueOf(retDouble * retLong);

                return Long.valueOf(retLong);
            }
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#accumulate(Collection)
             */
            @Override
            public List<Number> accumulate (Collection<? extends Number> args)
            {
                final int    numArgs=(null == args) ? 0 : args.size();
                if (numArgs <= 0)
                    return null;

                final List<Number>    prods=new ArrayList<Number>(numArgs);
                double                retDouble=1.0d;
                long                retLong=1L;
                for (final Number    n : args)
                {
                    if (null == n)
                        continue;

                    final Number    p;
                    if ((0L == retLong) || (0.0d == retDouble))
                    {
                        p = Long.valueOf(0L);
                    }
                    else
                    {
                        if (MathFunctions.isFloatingPoint(n))
                            retDouble *= n.doubleValue();
                        else
                            retLong *= n.longValue();

                        if (null == (p=calculateValue(retDouble, retLong)))
                            throw new IllegalStateException("No " + name() + " calculated "
                                                          + "on L=" + retLong + "/D=" + retDouble);
                    }

                    if (!prods.add(p))
                        continue;
                }

                return prods;
            }
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#aggregate(java.util.Collection)
             */
            @Override
            public Number aggregate (final Collection<? extends Number> args)
            {
                if ((null == args) || (args.size() <= 0))
                    return null;

                double    retDouble=1.0d;
                long    retLong=1L;
                for (final Number    n : args)
                {
                    if (null == n)
                        continue;

                    if (MathFunctions.isFloatingPoint(n))
                        retDouble *= n.doubleValue();
                    else
                        retLong *= n.longValue();

                    if ((0L == retLong) || (0.0d == retDouble))
                        return Long.valueOf(0L);
                }

                return calculateValue(retDouble, retLong);
            }
        },
    /**
     * number of arguments
     */
    CNT {
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#accumulate(Collection)
             */
            @Override
            public List<Number> accumulate (Collection<? extends Number> args)
            {
                final int    numArgs=(null == args) ? 0 : args.size();
                if (numArgs <= 0)
                    return null;

                final List<Number>    cnts=new ArrayList<Number>(numArgs);
                for (int aIndex=1; aIndex <= numArgs; aIndex++)
                {
                    if (!cnts.add(Integer.valueOf(aIndex)))
                        continue;
                }

                return cnts;
            }
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#aggregate(java.util.Collection)
             */
            @Override
            public Number aggregate (final Collection<? extends Number> args)
            {
                return Integer.valueOf((null == args) ? 0 : args.size());
            }
        },
    /**
     * (G)reatest (C)ommon (D)ivisor
     */
    GCD {
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#getFloatingPointExecutionState()
             */
            @Override
            public Boolean getFloatingPointExecutionState ()
            {
                return Boolean.FALSE;
            }

            public Number aggregate (final Number[] args, final int offset, final int len)
            {
                if (len <= 0)
                    return null;

                Number    lastVal=null;
                for (int    aIndex=offset; aIndex < (offset + len); aIndex++)
                {
                    final Number n=args[aIndex];
                    if (null == n)
                        continue;

                    if (null == lastVal)
                    {
                        if (n.longValue() != 0L)
                            lastVal = n;    // GCD(0,x) == x anyway...
                        continue;
                    }

                    final long    lVal=lastVal.longValue(),
                                gVal=MathFunctions.binaryGcd(lVal, n.longValue());
                    if (gVal != lVal)
                        lastVal = Long.valueOf(gVal);
                    if (gVal <= 1L)    // if reached 1 (or zero) then
                        break;
                }

                return lastVal;

            }
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#accumulate(Number[])
             */
            @Override
            public List<Number> accumulate (final Number ... args)
            {
                final int    numArgs=(null == args) ? 0 : args.length;
                if (numArgs <= 0)
                    return null;

                final List<Number>    gcds=new ArrayList<Number>(numArgs);
                for (int aIndex=1; aIndex < numArgs; aIndex++)
                {
                    final Number    v=aggregate(args, 0, aIndex);
                    if (null == v)
                        continue;

                    if (!gcds.add(v))
                        continue;
                }

                return gcds;
            }
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#accumulate(Collection)
             */
            @Override
            public List<Number> accumulate (Collection<? extends Number> args)
            {
                final int    numArgs=(null == args) ? 0 : args.size();
                if (numArgs <= 0)
                    return null;

                return accumulate(args.toArray(new Number[numArgs]));
            }
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#aggregate(Number[])
             */
            @Override
            public Number aggregate (final Number ... args)
            {
                return aggregate(args, 0, (null == args) ? 0 : args.length);
            }
            /*
             * @see net.community.chest.math.functions.AggregateFunctions#aggregate(java.util.Collection)
             */
            @Override
            public Number aggregate (final Collection<? extends Number> args)
            {
                final int    numArgs=(null == args) ? 0 : args.size();
                if (numArgs <= 0)
                    return null;

                return aggregate(args.toArray(new Number[numArgs]));
            }
        };
    /*
     * @see net.community.chest.math.NumbersFunction#getFloatingPointExecutionState()
     */
    @Override
    public Boolean getFloatingPointExecutionState ()
    {
        return null;
    }
    /*
     * @see net.community.chest.math.FunctionInterface#getName()
     */
    @Override
    public final String getName ()
    {
        return name();
    }
    /*
     * @see net.community.chest.math.FunctionInterface#getNumArguments()
     */
    @Override
    public final int getNumArguments ()
    {
        return (-1);
    }
    /*
     * @see net.community.chest.math.FunctionInterface#getSymbol()
     */
    @Override
    public final String getSymbol ()
    {
        return getName();
    }

    protected int compare (final Number curVal, final Number newVal)
    {
        if ((null == curVal) || (null == newVal))
            return 0;

        if (MathFunctions.isFloatingPoint(curVal) || MathFunctions.isFloatingPoint(newVal))
            return DoublesComparator.compare(curVal.doubleValue(), newVal.doubleValue());
        else
            return LongsComparator.compare(curVal.longValue(), newVal.longValue());
    }
    /**
     * @param args The {@link Collection} of {@link Number}-s to apply the function to
     * @return The {@link Number} result of applying the function to the
     * provided number - may be <code>null</code> if no arguments provided
     */
    public abstract Number aggregate (Collection<? extends Number> args);
    /**
     * @param args The array of {@link Number}-s to apply the function to
     * @return The {@link Number} result of applying the function to the
     * provided number - may be <code>null</code> if no arguments provided
     */
    public Number aggregate (Number ... args)
    {
        return ((null == args) || (args.length <= 0)) ? null : aggregate(Arrays.asList(args));
    }
    /*
     * @see net.community.chest.math.NumbersFunction#invoke(java.util.List)
     */
    @Override
    public Number invoke (List<? extends Number> args)
    {
        return aggregate(args);
    }
    /*
     * @see net.community.chest.math.NumbersFunction#invoke(java.lang.Number[])
     */
    @Override
    public Number invoke (Number... args)
        throws IllegalArgumentException, ClassCastException
    {
        return ((null == args) || (args.length <= 0)) ? null : invoke(Arrays.asList(args));
    }
    /**
     * @param args The array of {@link Number}-s to apply the function to
     * @return A {@link List} of &quot;intermediate&quot; results where the
     * {@link Number} at position <code>N</code> shows the result of the
     * {@link #aggregate(Collection)} function if it were applied only to the
     * <U>first</U> <code>N</code> values out of all of the provided ones
     */
    public abstract List<Number> accumulate (Collection<? extends Number> args);
    /**
     * @param args The array of {@link Number}-s to apply the function to
     * @return A {@link List} of &quot;intermediate&quot; results where the
     * {@link Number} at position <code>N</code> shows the result of the
     * {@link #aggregate(Number...)} function if it were applied only to the
     * <U>first</U> <code>N</code> values out of all of the provided ones
     */
    public List<Number> accumulate (Number ... args)
    {
        return ((null == args) || (args.length <= 0)) ? null : accumulate(Arrays.asList(args));
    }

    public static final List<AggregateFunctions>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final AggregateFunctions fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final AggregateFunctions fromSymbol (final String sym)
    {
        return MathFunctions.fromSymbol(VALUES, sym, false);
    }
}

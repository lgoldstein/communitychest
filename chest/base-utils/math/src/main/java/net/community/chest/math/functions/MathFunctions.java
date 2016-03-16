/*
 *
 */
package net.community.chest.math.functions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.community.chest.lang.StringUtil;
import net.community.chest.math.FunctionInterface;
import net.community.chest.math.NumberType;
import net.community.chest.math.NumbersFunction;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 27, 2009 9:53:12 AM
 */
public enum MathFunctions implements NumbersFunction, SingleArgumentCalculator {
    ABS(null) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.abs(v);
            }
            /*
             * @see net.community.chest.math.functions.MathFunctions#execute(long)
             */
            @Override
            public long execute (long v)
            {
                return Math.abs(v);
            }
        },
    CBRT(true) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.cbrt(v);
            }
        },
    CEIL(true) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.ceil(v);
            }
        },
    EXP(true) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.exp(v);
            }
        },
    EXPM1(true) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.expm1(v);
            }
        },
    FLOOR(true) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.floor(v);
            }
        },
    LOG(true) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.log(v);
            }
        },
    LOG10(true) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.log10(v);
            }
        },
    LOG1P(true) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.log1p(v);
            }
        },
    NEXTUP(true) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.nextUp(v);
            }
        },
    RINT(true) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.rint(v);
            }
        },
    ROUND(true) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.round(v);
            }
        },
    SIGNUM(true) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.signum(v);
            }
        },
    SQRT(true) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.sqrt(v);
            }
        },
    ULP(true) {
            /*
             * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(double)
             */
            @Override
            public double execute (double v)
            {
                return Math.ulp(v);
            }
        };

    private final Boolean    _fpState;
    /*
     * @see net.community.chest.math.NumbersFunction#getFloatingPointExecutionState()
     */
    @Override
    public final Boolean getFloatingPointExecutionState ()
    {
        return _fpState;
    }

    MathFunctions (Boolean fpState)
    {
        _fpState = fpState;
    }

    MathFunctions (boolean fpState)
    {
        this(Boolean.valueOf(fpState));
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
     * @see net.community.chest.math.FunctionInterface#getSymbol()
     */
    @Override
    public final String getSymbol ()
    {
        return getName();
    }
    /*
     * @see net.community.chest.math.FunctionInterface#getNumArguments()
     */
    @Override
    public final int getNumArguments ()
    {
        return 1;
    }
    /*
     * @see net.community.chest.math.functions.SingleArgumentCalculator#execute(long)
     */
    @Override
    public long execute (long v)
    {
        return (long) execute((double) v);
    }
    /*
     * @see net.community.chest.math.NumbersFunction#invoke(java.util.List)
     */
    @Override
    public Number invoke (List<? extends Number> args)
            throws IllegalArgumentException, ClassCastException
    {
        return invokeSingle(this, args);
    }
    /*
     * @see net.community.chest.math.NumbersFunction#invoke(java.lang.Number[])
     */
    @Override
    public Number invoke (Number... args) throws IllegalArgumentException, ClassCastException
    {
        return invokeSingle(this, args);
    }
    /**
     * Calculates the (G)reatest (C)ommon (D)ivisor using
     * <A href="http://en.wikipedia.org/wiki/Binary_GCD_algorithm">Stein's algorithm</A>
     * @param uu 1st value
     * @param vv 2nd value
     * @return GCD value (Note: <code>gcd(x,0)=x</code>, and also <code>gcd(0,0)=0/undefined</code>)
     */
    public static final int binaryGcd (int uu, int vv)
    {
         int u=Math.abs(uu), v=Math.abs(vv), shift=0;

         /* GCD(0,x) := x */
         if ((u == 0) || (v == 0))
           return u | v;

         /* Let shift := lg K, where K is the greatest power of 2 dividing both u and v. */
         for (shift = 0; ((u | v) & 1) == 0; ++shift)
         {
             u >>= 1;
             v >>= 1;
         }

         while ((u & 1) == 0)
           u >>= 1;

         /* From here on, u is always odd. */
         do
         {
             while ((v & 1) == 0)  /* Loop X */
               v >>= 1;

             /* Now u and v are both odd, so diff(u, v) is even.
                Let u = min(u, v), v = diff(u, v)/2. */
             if (u < v)
             {
                 v -= u;
             }
             else
             {
                 final int diff=u - v;
                 u = v;
                 v = diff;
             }

             v >>= 1;
         } while (v != 0);

         return u << shift;
     }

    public static final long binaryGcd (long uu, long vv)
    {
         long u=Math.abs(uu), v=Math.abs(vv);
         int shift=0;

         /* GCD(0,x) := x */
         if ((u == 0L) || (v == 0L))
           return u | v;

         /* Let shift := lg K, where K is the greatest power of 2 dividing both u and v. */
         for (shift = 0; ((u | v) & 1L) == 0L; ++shift)
         {
             u >>= 1;
             v >>= 1;
         }

         while ((u & 1L) == 0)
           u >>= 1;

         /* From here on, u is always odd. */
         do
         {
             while ((v & 1L) == 0)  /* Loop X */
               v >>= 1;

             /* Now u and v are both odd, so diff(u, v) is even.
                Let u = min(u, v), v = diff(u, v)/2. */
             if (u < v)
             {
                 v -= u;
             }
             else
             {
                 final long diff=u - v;
                 u = v;
                 v = diff;
             }

             v >>= 1;
         } while (v != 0L);

         return u << shift;
    }

    public static final <F extends NumbersFunction & SingleArgumentCalculator> Number invokeSingleValue (
            final F func, final Number val)
        throws IllegalArgumentException, ClassCastException
    {
        if ((null == func) || (null == val))
            return null;

        Boolean        fpState=func.getFloatingPointExecutionState();
        if (fpState == null)
            fpState = Boolean.valueOf(MathFunctions.isFloatingPoint(val));

        if (fpState.booleanValue())
            return Double.valueOf(func.execute(val.doubleValue()));
        else
            return Double.valueOf(func.execute(val.doubleValue()));
    }

    public static final <F extends NumbersFunction & SingleArgumentCalculator> Number invokeSingle (
            final F func, final Number ... args)
        throws IllegalArgumentException, ClassCastException
    {
        return ((null == args) || (args.length <= 0)) ? null : invokeSingleValue(func, args[0]);
    }

    public static final <F extends NumbersFunction & SingleArgumentCalculator> Number invokeSingle (
            final F func, final List<? extends Number> args)
        throws IllegalArgumentException, ClassCastException
    {
        return ((null == args) || (args.size() <= 0)) ? null : invokeSingleValue(func, args.get(0));
    }

    public static final <F extends NumbersFunction & DualArgumentsCalculator> Number invokeDualValues (
            final F func, final Number val1, Number val2)
        throws IllegalArgumentException, ClassCastException
    {
        if ((null == val1) || (null == val2))
            return null;

        Boolean        fpState=func.getFloatingPointExecutionState();
        if (fpState == null)
            fpState = Boolean.valueOf(MathFunctions.isFloatingPoint(val1) || MathFunctions.isFloatingPoint(val2));

        if (fpState.booleanValue())
            return Double.valueOf(func.execute(val1.doubleValue(), val2.doubleValue()));
        else
            return Long.valueOf(func.execute(val1.longValue(), val2.longValue()));
    }

    public static final <F extends NumbersFunction & DualArgumentsCalculator> Number invokeDual (
            final F func, final List<? extends Number> args) throws IllegalArgumentException, ClassCastException
    {
        return ((null == args) || (args.size() <= 1)) ? null : invokeDualValues(func, args.get(0), args.get(1));
    }

    public static final <F extends NumbersFunction & DualArgumentsCalculator> Number invokeDual (
            final F func, final Number ... args) throws IllegalArgumentException, ClassCastException
    {
        return ((null == args) || (args.length <= 1)) ? null : invokeDualValues(func, args[0], args[1]);
    }

    public static final <F extends FunctionInterface> F fromSymbol (final Collection<? extends F> funcs, final String sym, final boolean caseSensitive)
    {
        if ((null == sym) || (sym.length() <= 0)
         || (null == funcs) || (funcs.size() <= 0))
            return null;

        for (final F f : funcs)
        {
            final String    fs=(null == f) ? null : f.getSymbol();
            if (0 == StringUtil.compareDataStrings(fs, sym, caseSensitive))
                return f;
        }

        return null;
    }

    public static final <F extends Enum<F> & FunctionInterface> F fromSymbol (final Class<F> fc, final String sym, final boolean caseSensitive)
    {
        return ((null == fc) || (null == sym) || (sym.length() <= 0)) ? null : fromSymbol(Arrays.asList(fc.getEnumConstants()), sym, caseSensitive);
    }

    public static final boolean isFloatingPoint (final Number n)
    {
        return NumberType.isFloatingPoint(n);
    }

    public static final List<MathFunctions>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final MathFunctions fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final MathFunctions fromSymbol (final String sym)
    {
        return fromSymbol(VALUES, sym, false);
    }
}

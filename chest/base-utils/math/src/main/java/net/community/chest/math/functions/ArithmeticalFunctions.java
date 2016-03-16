/*
 *
 */
package net.community.chest.math.functions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.math.NumbersFunction;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 27, 2009 9:54:03 AM
 */
public enum ArithmeticalFunctions implements NumbersFunction, DualArgumentsCalculator {
    ADD("+") {
            /*
             * @see net.community.chest.math.functions.DualArgumentsCalculator#execute(double, double)
             */
            @Override
            public double execute (double v1, double v2)
            {
                return (v1 + v2);
            }
            /*
             * @see net.community.chest.math.functions.DualArgumentsCalculator#execute(long, long)
             */
            @Override
            public long execute (long v1, long v2)
            {
                return (v1 + v2);
            }
        },
    SUB("-") {
            /*
             * @see net.community.chest.math.functions.DualArgumentsCalculator#execute(double, double)
             */
            @Override
            public double execute (double v1, double v2)
            {
                return (v1 - v2);
            }
            /*
             * @see net.community.chest.math.functions.DualArgumentsCalculator#execute(long, long)
             */
            @Override
            public long execute (long v1, long v2)
            {
                return (v1 - v2);
            }
        },
    MUL("*") {
            /*
             * @see net.community.chest.math.functions.DualArgumentsCalculator#execute(double, double)
             */
            @Override
            public double execute (double v1, double v2)
            {
                return (v1 * v2);
            }
            /*
             * @see net.community.chest.math.functions.DualArgumentsCalculator#execute(long, long)
             */
            @Override
            public long execute (long v1, long v2)
            {
                return (v1 * v2);
            }
        },
    DIV("/") {
            /*
             * @see net.community.chest.math.functions.DualArgumentsCalculator#execute(double, double)
             */
            @Override
            public double execute (double v1, double v2)
            {
                if (v2 == 0.0d)
                    throw new IllegalArgumentException("Division by zero attempted");
                return (v1 / v2);
            }
            /*
             * @see net.community.chest.math.functions.DualArgumentsCalculator#execute(long, long)
             */
            @Override
            public long execute (long v1, long v2)
            {
                if (v2 == 0L)
                    throw new IllegalArgumentException("Division by zero attempted");
                return (v1 / v2);
            }
        },
    REM("%") {
            /*
             * @see net.community.chest.math.functions.DualArgumentsCalculator#execute(double, double)
             */
            @Override
            public double execute (double v1, double v2)
            {
                if (v2 == 0.0d)
                    throw new IllegalArgumentException("Remainder by zero attempted");
                return (v1 % v2);
            }
            /*
             * @see net.community.chest.math.functions.DualArgumentsCalculator#execute(long, long)
             */
            @Override
            public long execute (long v1, long v2)
            {
                if (v2 == 0L)
                    throw new IllegalArgumentException("Remainder by zero attempted");
                return (v1 % v2);
            }
        },
    POW("^") {
            /*
             * @see net.community.chest.math.functions.DualArgumentsCalculator#execute(double, double)
             */
            @Override
            public double execute (double v1, double v2)
            {
                return Math.pow(v1, v2);
            }
            /*
             * @see net.community.chest.math.functions.DualArgumentsCalculator#execute(long, long)
             */
            @Override
            public long execute (long v1, long v2)
            {
                return (long) Math.pow(v1, v2);
            }
            /*
             * @see net.community.chest.math.functions.ArithmeticalFunctions#getFloatingPointExecutionState()
             */
            @Override
            public Boolean getFloatingPointExecutionState ()
            {
                return Boolean.TRUE;
            }
        };
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
        return 2;
    }
    /*
     * @see net.community.chest.math.NumbersFunction#getFloatingPointExecutionState()
     */
    @Override
    public Boolean getFloatingPointExecutionState ()
    {
        return null;
    }

    private final String    _symbol;
    /*
     * @see net.community.chest.math.FunctionInterface#getSymbol()
     */
    @Override
    public final String getSymbol ()
    {
        return _symbol;
    }

    ArithmeticalFunctions (String sym)
    {
        _symbol = sym;
    }
    /*
     * @see net.community.chest.math.NumbersFunction#invoke(java.util.List)
     */
    @Override
    public Number invoke (final List<? extends Number> args) throws IllegalArgumentException, ClassCastException
    {
        return MathFunctions.invokeDual(this, args);
    }
    /*
     * @see net.community.chest.math.NumbersFunction#invoke(java.lang.Number[])
     */
    @Override
    public Number invoke (Number... args) throws IllegalArgumentException, ClassCastException
    {
        return MathFunctions.invokeDual(this, args);
    }

    public static final List<ArithmeticalFunctions>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final ArithmeticalFunctions fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final ArithmeticalFunctions fromSymbol (final String sym)
    {
        return MathFunctions.fromSymbol(VALUES, sym, false);
    }

    public static final ArithmeticalFunctions fromSymbol (final char c)
    {
        if ((c <= '\0') || (c >= '\u007e'))
            return null;

        for (final ArithmeticalFunctions v : VALUES)
        {
            final CharSequence    s=(null == v) ? null : v.getSymbol();
            if ((null == s) || (s.length() != 1))
                continue;
            if (s.charAt(0) == c)
                return v;
        }

        return null;
    }
}

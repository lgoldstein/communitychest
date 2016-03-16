/*
 *
 */
package net.community.chest.math.compare;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import net.community.chest.math.NumbersFunction;
import net.community.chest.math.functions.DualArgumentsCalculator;
import net.community.chest.math.functions.MathFunctions;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>An {@link Enum}-eration that provides comparison operators for
 * {@link Comparable} objects</P>
 * @author Lyor G.
 * @since Apr 13, 2009 10:32:00 AM
 */
public enum ComparableOperator implements ComparisonExecutor, NumbersFunction, DualArgumentsCalculator {
    EQ("==") {
            /*
             * @see net.community.chest.ComparisonExecutor#getComparisonResult(int)
             */
            @Override
            public Boolean getComparisonResult (final int nRes)
            {
                return Boolean.valueOf(nRes == 0);
            }
        },
    NE("<>") {
            /*
             * @see net.community.chest.ComparisonExecutor#getComparisonResult(int)
             */
            @Override
            public Boolean getComparisonResult (final int nRes)
            {
                return Boolean.valueOf(nRes != 0);
            }
        },
    LT("<") {
            /*
             * @see net.community.chest.ComparisonExecutor#getComparisonResult(int)
             */
            @Override
            public Boolean getComparisonResult (final int nRes)
            {
                return Boolean.valueOf(nRes < 0);
            }
        },
    LE("<=") {
            /*
             * @see net.community.chest.ComparisonExecutor#getComparisonResult(int)
             */
            @Override
            public Boolean getComparisonResult (final int nRes)
            {
                return Boolean.valueOf(nRes <= 0);
            }
        },
    GT(">") {
            /*
             * @see net.community.chest.ComparisonExecutor#getComparisonResult(int)
             */
            @Override
            public Boolean getComparisonResult (final int nRes)
            {
                return Boolean.valueOf(nRes > 0);
            }
        },
    GE(">=") {
            /*
             * @see net.community.chest.ComparisonExecutor#getComparisonResult(int)
             */
            @Override
            public Boolean getComparisonResult (final int nRes)
            {
                return Boolean.valueOf(nRes >= 0);
            }
        };
    /*
     * @see net.community.chest.ComparisonExecutor#invoke(java.lang.Comparable, java.lang.Comparable)
     */
    @Override
    public <V extends Comparable<V>> Boolean invoke (V o1, V o2)
    {
        return getComparisonResult(AbstractComparator.compareComparables(o1, o2));
    }
    /*
     * @see net.community.chest.ComparisonExecutor#invoke(java.util.Comparator, java.lang.Object, java.lang.Object)
     */
    @Override
    public <V> Boolean invoke (Comparator<? super V> c, V o1, V o2)
    {
        if (null == c)
            return null;

        return getComparisonResult(c.compare(o1, o2));
    }
    /*
     * @see net.community.chest.math.functions.DualArgumentsCalculator#execute(double, double)
     */
    @Override
    public final double execute (double v1, double v2)
    {
        final Boolean    res=invoke(Double.valueOf(v1), Double.valueOf(v2));
        if (null == res)
            throw new IllegalArgumentException("Failed to compare " + v1 + " and " + v2);
        return res.booleanValue() ? 1.0d : 0.0d;
    }
    /*
     * @see net.community.chest.math.functions.DualArgumentsCalculator#execute(long, long)
     */
    @Override
    public final long execute (long v1, long v2)
    {
        final Boolean    res=invoke(Long.valueOf(v1), Long.valueOf(v2));
        if (null == res)
            throw new IllegalArgumentException("Failed to compare " + v1 + " and " + v2);

        return res.booleanValue() ? 1L : 0L;
    }
    /*
     * @see net.community.chest.math.NumbersFunction#getFloatingPointExecutionState()
     */
    @Override
    public final Boolean getFloatingPointExecutionState ()
    {
        return null;
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

    private final String    _symbol;
    /*
     * @see net.community.chest.math.FunctionInterface#getSymbol()
     */
    @Override
    public final String getSymbol ()
    {
        return _symbol;
    }

    ComparableOperator (String sym)
    {
        _symbol = sym;
    }

    public static final List<ComparableOperator>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final ComparableOperator fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final ComparableOperator fromSymbol (final String sym)
    {
        return MathFunctions.fromSymbol(VALUES, sym, false);
    }

    public static final ComparableOperator inverse (final ComparableOperator op)
    {
        if (null == op)
            return null;

        switch(op)
        {
            case EQ    : return NE;
            case GE    : return LT;
            case GT    : return LE;
            case LE    : return GT;
            case LT    : return GE;
            case NE    : return EQ;
            default    :    // should not happen
                throw new NoSuchElementException("inverse(" + op + ") unknown operator");
        }
    }
    // negates whatever Boolean invocation result is received from the real comparator
    public static final ComparisonExecutor negate (final ComparisonExecutor c)
    {
        // some "shortcuts"
        if (c instanceof ComparableOperator)
            return inverse((ComparableOperator) c);

        return ComparatorNegator.negate(ComparisonExecutor.class, c);
    }
}

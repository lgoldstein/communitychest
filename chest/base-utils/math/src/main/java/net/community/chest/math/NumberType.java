/*
 *
 */
package net.community.chest.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.CoVariantReturn;
import net.community.chest.convert.DoubleValueStringConstructor;
import net.community.chest.convert.FloatValueStringConstructor;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.math.compare.ComparableOperator;
import net.community.chest.math.compare.RangeOperator;
import net.community.chest.math.functions.AggregateFunctions;
import net.community.chest.math.functions.ArithmeticalFunctions;
import net.community.chest.math.functions.ConversionFunctions;
import net.community.chest.math.functions.MathFunctions;
import net.community.chest.math.functions.TrigonometryFunctions;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.map.MapEntryImpl;
import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>An {@link Enum} used to &quot;translate&quot; existing built-in Java
 * types for {@link Number}-s into their &quot;type&quot;</P>
 * @author Lyor G.
 * @since Oct 15, 2008 3:32:05 PM
 */
public enum NumberType {
    BYTE('B', Byte.class, Byte.TYPE, Byte.SIZE, false) {
            /*
             * @see net.community.chest.lang.math.NumberType#fromNumber(java.lang.Number)
             */
            @Override
            @CoVariantReturn
            public Byte fromNumber (final Number n)
            {
                if (null == n)
                    return null;
                else if (n instanceof Byte)
                    return (Byte) n;
                else
                    return Byte.valueOf(n.byteValue());
            }
            /*
             * @see net.community.chest.lang.math.NumberType#decode(java.lang.String)
             */
            @Override
            @CoVariantReturn
            public Byte decode (final String s) throws NumberFormatException
            {
                return ((null == s) || (s.length() <= 0)) ? null : Byte.decode(s);
            }
        },
    SHORT('S', Short.class, Short.TYPE, Short.SIZE, false) {
            /*
             * @see net.community.chest.lang.math.NumberType#fromNumber(java.lang.Number)
             */
            @Override
            @CoVariantReturn
            public Short fromNumber (final Number n)
            {
                if (null == n)
                    return null;
                else if (n instanceof Short)
                    return (Short) n;
                else
                    return Short.valueOf(n.shortValue());
            }
            /*
             * @see net.community.chest.lang.math.NumberType#decode(java.lang.String)
             */
            @Override
            @CoVariantReturn
            public Short decode (final String s) throws NumberFormatException
            {
                return ((null == s) || (s.length() <= 0)) ? null : Short.decode(s);
            }
        },
    INTEGER('I', Integer.class, Integer.TYPE, Integer.SIZE, false) {
            /*
             * @see net.community.chest.lang.math.NumberType#fromNumber(java.lang.Number)
             */
            @Override
            @CoVariantReturn
            public Integer fromNumber (final Number n)
            {
                if (null == n)
                    return null;
                else if (n instanceof Integer)
                    return (Integer) n;
                else
                    return Integer.valueOf(n.intValue());
            }
            /*
             * @see net.community.chest.lang.math.NumberType#decode(java.lang.String)
             */
            @Override
            @CoVariantReturn
            public Integer decode (final String s) throws NumberFormatException
            {
                return ((null == s) || (s.length() <= 0)) ? null : Integer.decode(s);
            }
        },
    LONG('L', Long.class, Long.TYPE, Long.SIZE, false) {
            /*
             * @see net.community.chest.lang.math.NumberType#fromNumber(java.lang.Number)
             */
            @Override
            @CoVariantReturn
            public Long fromNumber (final Number n)
            {
                if (null == n)
                    return null;
                else if (n instanceof Long)
                    return (Long) n;
                else
                    return Long.valueOf(n.longValue());
            }
            /*
             * @see net.community.chest.lang.math.NumberType#decode(java.lang.String)
             */
            @Override
            @CoVariantReturn
            public Long decode (final String s) throws NumberFormatException
            {
                return ((null == s) || (s.length() <= 0)) ? null : Long.decode(s);
            }
        },
    FLOAT('F', Float.class, Float.TYPE, Float.SIZE, true) {
            /*
             * @see net.community.chest.lang.math.NumberType#fromNumber(java.lang.Number)
             */
            @Override
            @CoVariantReturn
            public Float fromNumber (final Number n)
            {
                if (null == n)
                    return null;
                else if (n instanceof Float)
                    return (Float) n;
                else
                    return Float.valueOf(n.floatValue());
            }
            /*
             * @see net.community.chest.lang.math.NumberType#decode(java.lang.String)
             */
            @Override
            @CoVariantReturn
            public Float decode (final String s) throws NumberFormatException
            {
                if ((null == s) || (s.length() <= 0))
                    return null;

                final Float    sv=FloatValueStringConstructor.convertSpecialInstance(s);
                if (null == sv)
                    return Float.valueOf(s);
                return sv;
            }
        },
    DOUBLE('D', Double.class, Double.TYPE, Double.SIZE, true) {
            /*
             * @see net.community.chest.lang.math.NumberType#fromNumber(java.lang.Number)
             */
            @Override
            @CoVariantReturn
            public Double fromNumber (final Number n)
            {
                if (null == n)
                    return null;
                else if (n instanceof Double)
                    return (Double) n;
                else
                    return Double.valueOf(n.doubleValue());
            }
            /*
             * @see net.community.chest.lang.math.NumberType#decode(java.lang.String)
             */
            @Override
            @CoVariantReturn
            public Double decode (final String s) throws NumberFormatException
            {
                if ((null == s) || (s.length() <= 0))
                    return null;

                final Double    sv=DoubleValueStringConstructor.convertSpecialInstance(s);
                if (null == sv)
                    return Double.valueOf(s);
                return sv;
            }
        };
    /**
     * @param n Original {@link Number}
     * @return A {@link Number} representing as closely as possible the
     * original one cast to the actual type - may be <code>null</code> if
     * original null argument
     */
    public abstract Number fromNumber (final Number n);
    /**
     * @param s Input {@link String}
     * @return The represented {@link Number} according to the type - may be
     * <code>null</code> if null/empty input to begin with
     * @throws NumberFormatException If input string format does not match
     * expected/allowed format for the number
     * @see Byte#decode(String)
     * @see Short#decode(String)
     * @see Integer#decode(String)
     * @see Long#decode(String)
     * @see Float#valueOf(String)
     * @see Double#valueOf(String)
     */
    public abstract Number decode (final String s) throws NumberFormatException;
    /**
     * A type character used to enforce a specific interpretation
     */
    private final char    _typeChar;
    public final char getTypeChar ()
    {
        return _typeChar;
    }

    private final Class<? extends Number>    _typeClass;
    public final Class<? extends Number> getTypeClass ()
    {
        return _typeClass;
    }
    /**
     * The equivalent primitive type class
     */
    private final Class<? extends Number>    _primClass;
    public final Class<? extends Number> getPrimitiveClass ()
    {
        return _primClass;
    }
    /**
     * Number of bits used to represent the value of this type
     */
    private final int    _precision;
    public final int getPrecision ()
    {
        return _precision;
    }
    /**
     * Compare the precision with another {@link NumberType} instance
     * @param t The {@link NumberType} instance to compare with
     * @return Positive if this precision is greater than the compared
     * type's one, negative if less and zero if same
     */
    public final int comparePrecision (final NumberType t)
    {
        if (t == this)
            return 0;

        final int    p1=getPrecision(), p2=(null == t) ? 0 : t.getPrecision();
        return (p1 - p2);
    }

    private final boolean    _fp;
    public final boolean isFloatingPoint ()
    {
        return _fp;
    }

    public static final boolean isFloatingPoint (final Class<?> c)
    {
        if (null == c)
            return false;

        if (Double.class.isAssignableFrom(c) || Double.TYPE.isAssignableFrom(c))
            return true;
        if (Float.class.isAssignableFrom(c) || Float.TYPE.isAssignableFrom(c))
            return true;

        return false;
    }

    public static final boolean isFloatingPoint (final Object o)
    {
        return (null == o) ? false : isFloatingPoint(o.getClass());
    }

    public final boolean compareFloatingPoint (final NumberType t)
    {
        if (null == t)
            return false;
        else if (this == t)
            return true;
        else
            return (isFloatingPoint() == t.isFloatingPoint());
    }

    NumberType (char c, Class<? extends Number> tc, Class<? extends Number> pc, int precision, boolean fp)
    {
        _typeChar = c;
        _typeClass = tc;
        _primClass = pc;
        _precision = precision;
        _fp = fp;
    }

    public Number parseValue (final String s) throws Exception
    {
        final int    sLen=(null == s) ? 0 : s.length();
        if (sLen <= 0)
            return null;

        final ValueStringInstantiator<? extends Number> vsi=
            ClassUtil.getAtomicStringInstantiator(getTypeClass());
        return vsi.newInstance(s);
    }

    public static final List<NumberType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final NumberType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final NumberType fromClass (final Class<?> c)
    {
        if ((null == c) || (!Number.class.isAssignableFrom(c)))
            return null;

        for (final NumberType v : VALUES)
        {
            final Class<?>    tc=(null == v) ? null : v.getTypeClass(),
                            pc=(null == v) ? null : v.getPrimitiveClass();
            if (((pc != null) && pc.isAssignableFrom(c))
             || ((tc != null) && tc.isAssignableFrom(c)))
                return v;
        }

        return null;
    }

    public static final NumberType fromObject (final Object o)
    {
        return (null == o) ? null : fromClass(o.getClass());
    }

    public static final NumberType fromTypeChar (final char c)
    {
        // convert to uppercase
        final char    ec=((c < 'A') || (c > 'Z')) ? Character.toUpperCase(c) : c;
        for (final NumberType v : VALUES)
        {
            if ((v != null) && (v.getTypeChar() == ec))
                return v;
        }

        return null;
    }
    /**
     * @param <C> The used {@link CharSequence} value type
     * @param v A {@link CharSequence} containing the value and optionally
     * the type character
     * @param defType The default type to be used if no type character found
     * @return A &quot;pair&quot; representing the type and the &quot;pure&quot;
     * value (which could be same as input). <code>null</code> if have a type
     * character but cannot determine the type
     */
    public static final <C extends CharSequence> Map.Entry<NumberType,C> fromValue (final C v, final NumberType defType)
    {
        final int    vLen=(null == v) ? 0 : v.length();
        if (vLen <= 0)
            return null;
        if (vLen <= 1)    // must have at least 1 digit
            return new MapEntryImpl<NumberType,C>(defType, v);

        final char    c=v.charAt(vLen - 1);
        if ((c >= '0') && (c <= '9')) // if last character is digit return default
            return new MapEntryImpl<NumberType,C>(defType, v);

        final NumberType    t=fromTypeChar(c);
        if (null == t)
            return null;

        @SuppressWarnings("unchecked")
        final Map.Entry<NumberType,C> ret=new MapEntryImpl<NumberType,C>(t, (C) v.subSequence(1, vLen));
        return ret;
    }

    public static final Map.Entry<NumberType,Number> fromString (final CharSequence v, final NumberType defType) throws Exception
    {
        if ((null == v) || (v.length() <= 0))
            return null;

        final Map.Entry<NumberType,CharSequence>    tp=fromValue(v, defType);
        if (null == tp)
            throw new IllegalArgumentException("fromString(" + v + ")[" + defType + "] failed to resolve type");

        final NumberType    t=tp.getKey();
        final CharSequence    s=tp.getValue();
        if ((null == s) || (s.length() <= 0))
            return null;

        final Number    n=t.parseValue(s.toString());
        return new MapEntryImpl<NumberType,Number>(t, n);
    }

    public static final NumberType findByPrecisionAndFloatingPoint (final int precision, final boolean fp)
    {
        if (precision <= 0)
            return null;

        for (final NumberType v : VALUES)
        {
            if ((v != null) && (v.getPrecision() == precision) && (v.isFloatingPoint() == fp))
                return v;
        }

        return null;
    }

    public static final NumberType    MAX_PRECISION_TYPE=DOUBLE;
    /**
     * <P>Attempts to find the highest possible precision for all the numbers
     * using the following algorithm:</P></BR>
     * <UL>
     *         <LI>the higher the number of precision bits the better</LI>
     *         <LI>floating point takes precedence over non-floating point</LI>
     * </UL>
     * @param defType Default precision to use if unable to determine (may be
     * <code>null</code>
     * @param nums The {@link Number}-s to check - ignored if null/empty, and
     * any null member is also ignored.
     * @return Best possible precision type - default if none could be
     * determined
     */
    public static final NumberType getBestPrecision (final NumberType defType, final Collection<? extends Number> nums)
    {
        if ((null == nums) || (nums.size() <= 0))
            return defType;

        NumberType    ret=null;
        for (final Number n : nums)
        {
            final NumberType    t=fromObject(n);
            if (null == t)
                continue;
            if (MAX_PRECISION_TYPE.equals(t))
                return t;    // already at max. floating point precision

            if (ret != null)
            {
                // check if have a floating point transition
                if (!ret.compareFloatingPoint(t))
                {
                    if (ret.isFloatingPoint())
                    {
                        // if precision greater than required, fine
                        if (ret.comparePrecision(t) > 0)
                            continue;

                        return MAX_PRECISION_TYPE;
                    }
                    else
                    {
                        if (FLOAT.comparePrecision(t) <= 0)
                            return MAX_PRECISION_TYPE;

                        ret = FLOAT;
                        continue;
                    }
                }
                else if (ret.comparePrecision(t) >= 0)
                    continue;    // skip if already using higher precision
            }

            ret = t;

        }

        if (null == ret)
            ret = defType;
        return ret;
    }

    public static final NumberType getBestPrecision (final NumberType defType, final Number ... nums)
    {
        return ((null == nums) || (nums.length <= 0)) ? defType : getBestPrecision(defType, SetsUtils.uniqueSetOf(nums));
    }

    public static final List<Number> convertToBestPrecision (final NumberType defType, final Collection<? extends Number> nums)
    {
        final int    numNumbers=(null == nums) ? 0 : nums.size();
        if (numNumbers <= 0)
            return null;

        final NumberType    t=getBestPrecision(defType, nums);
        if (null == t)
            return null;

        final List<Number>    ret=new ArrayList<Number>(numNumbers);
        for (final Number n : nums)
        {
            final Number    cn=(null == n) ? null : t.fromNumber(n);
            if (null == cn)
                continue;
            ret.add(cn);
        }

        return ret;
    }

    public static final List<Number> convertToBestPrecision (final NumberType defType, final Number ... nums)
    {
        return ((null == nums) || (nums.length <= 0)) ? null : convertToBestPrecision(defType, Arrays.asList(nums));
    }

    private static final <F extends Enum<F> & NumbersFunction>
        Map<String,NumbersFunction> updateFunctionSymbolsMap (Class<F> fc, Map<String,NumbersFunction> org)
    {
        final F[]    fa=(null == fc) ? null : fc.getEnumConstants();
        if ((null == fa) || (fa.length <= 0))
            return org;

        Map<String,NumbersFunction>    ret=org;
        for (final F f : fa)
        {
            final String    fs=(null == f) ? null : f.getSymbol();
            if ((null == fs) || (fs.length() <= 0))
                continue;

            if (null == ret)
                ret = new TreeMap<String,NumbersFunction>(String.CASE_INSENSITIVE_ORDER);

            final FunctionInterface    prev=ret.put(fs, f);
            if ((prev != null) && (!prev.equals(f)))
                throw new IllegalStateException("Ambiguous functions for symbol=" + fs + ": " + f + "/" + prev);
        }

        return ret;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final Map<String,NumbersFunction> createFunctionSymbolsMap ()
    {
        final Collection<? extends Class<?>>    cl=SetsUtils.uniqueSetOf(
                MathConstants.class,
                ComparableOperator.class,
                RangeOperator.class,
                AggregateFunctions.class,
                ArithmeticalFunctions.class,
                MathFunctions.class,
                TrigonometryFunctions.class,
                ConversionFunctions.class
            );

        Map<String,NumbersFunction>    ret=null;
        for (final Class<?> c : cl)
            ret = updateFunctionSymbolsMap((Class) c, ret);
        return ret;
    }

    private static Map<String,NumbersFunction>    _funcsMap    /* =null */;
    public static final synchronized Map<String,NumbersFunction> getFunctionSymbolsMap ()
    {
        if (null == _funcsMap)
            _funcsMap = createFunctionSymbolsMap();
        return _funcsMap;
    }

    public static final NumbersFunction getNumbersFunctionBySymbol (final String sym)
    {
        final Map<String,? extends NumbersFunction>    fm=
            ((null == sym) || (sym.length() <= 0)) ? null : getFunctionSymbolsMap();
        if ((null == fm) || (fm.size() <= 0))
            return null;

        return fm.get(sym);
    }
}

/*
 *
 */
package net.community.chest.lang;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.community.chest.AbstractTestSupport;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 11, 2011 9:28:51 AM
 */
public abstract class AbstractEnumTestSupport extends AbstractTestSupport {
    protected AbstractEnumTestSupport ()
    {
        // TODO Auto-generated constructor stub
    }
    /**
     * Checks that the <code><U>static</U> <U>public<U> <code>fromString</code>
     * method exists and it yields the correct result when invoked on all the
     * define {@link Enum} values
     * @param eClass The {@link Enum} derived class
     * @throws Exception if failed to use reflection API
     */
    protected <E extends Enum<E>> void assertFromStringValidity (final Class<E> eClass) throws Exception
    {
        assertFromMethodValidity(eClass, "fromString", new EnumTestValueAccessor<E,String>() {
                /*
                 * @see net.community.chest.lang.EnumUtilTest.EnumTestValueAccessor#getTestArgumentType()
                 */
                @Override
                public Class<String> getTestArgumentType ()
                {
                    return String.class;
                }
                /*
                 * @see net.community.chest.lang.EnumUtilTest.EnumTestValueAccessor#getTestValue(java.lang.Enum)
                 */
                @Override
                public String getTestValue (E value)
                {
                    return value.toString();
                }
            });
    }
    /**
     * Checks that the <code><U>static</U> <U>public<U> <code>fromName</code>
     * method exists and it yields the correct result when invoked on all the
     * define {@link Enum} values
     * @param eClass The {@link Enum} derived class
     * @throws Exception if failed to use reflection API
     */
    protected <E extends Enum<E>> void assertFromNameValidity (final Class<E> eClass) throws Exception
    {
        assertFromMethodValidity(eClass, "fromName", new EnumTestValueAccessor<E,String>() {
                /*
                 * @see net.community.chest.lang.EnumUtilTest.EnumTestValueAccessor#getTestArgumentType()
                 */
                @Override
                public Class<String> getTestArgumentType ()
                {
                    return String.class;
                }
                /*
                 * @see net.community.chest.lang.EnumUtilTest.EnumTestValueAccessor#getTestValue(java.lang.Enum)
                 */
                @Override
                public String getTestValue (E value)
                {
                    return value.name();
                }
            });
    }
    /**
     * Checks that the <code><U>static</U> <U>public<U></code> specified
     * {@link Method} exists and it yields the correct result when invoked on
     * all the {@link Enum} values
     * @param eClass The {@link Enum} derived class
     * @param methodName The method name
     * @param accessor The {@link EnumTestValueAccessor} used to test the values
     * @throws Exception if failed to use reflection API
     */
    protected <E extends Enum<E>> void assertFromMethodValidity (
            final Class<E> eClass, final String methodName, final EnumTestValueAccessor<E,?> accessor)
        throws Exception
    {
        final Class<?>    invokeArgType=accessor.getTestArgumentType();
        final String    methodSignature=eClass.getName() + "#" + methodName + "(" + invokeArgType.getName() + ")";
        final Method    fromMethod=eClass.getDeclaredMethod(methodName, invokeArgType);
        assertTrue(methodSignature + " not static", Modifier.isStatic(fromMethod.getModifiers()));
        assertTrue(methodSignature + " not public", Modifier.isPublic(fromMethod.getModifiers()));

        final E[]    VALUES=eClass.getEnumConstants();
        for (final E v : VALUES)
        {
            final Object    fromArg=accessor.getTestValue(v),
                            xlateResult=fromMethod.invoke(null, fromArg);
            assertSame(methodSignature + "[Mismatched translation for value=" + v.name() + " with argument=" + fromArg,
                       v, xlateResult);
        }
    }
    /**
     * <P>Copyright as per GPLv2</P>
     * Interface used to test {@link Enum} value(s) conversion from other types
     * @author Lyor G.
     * @since Aug 11, 2011 9:20:35 AM
     * @param <E> Type of {@link Enum} being encapsulated
     * @param <V> Type of argument from which the {@link Enum} is translated
     */
    public static interface EnumTestValueAccessor<E extends Enum<E>,V> {
        /**
         * @return Type of argument being used to convert from to {@link Enum}
         */
        Class<V> getTestArgumentType ();
        /**
         * @param value The {@link Enum}
         * @return The value to provide to the conversion method
         */
        V getTestValue (E value);
    }
}

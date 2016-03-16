package net.community.chest.reflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Represents a chain of <U>getter(s)</U> {@link Method}-s to be applied
 * <U>successively</U> starting from an object having the class specified by
 * the {@link #getValuesClass()} method</P>. The invocation stops at the
 * <U>first <B>null</B></U> result</P>.
 *
 * @author Lyor G.
 * @since May 29, 2008 11:00:31 AM
 */
public class AttributeGettersChain extends ArrayList<Method> {
    /**
     *
     */
    private static final long serialVersionUID = -2082772346637597469L;
    private final Class<?>    _valsClass;
    public final Class<?> getValuesClass ()
    {
        return _valsClass;
    }

    public AttributeGettersChain (final Class<?> valsClass)
    {
        if (null == (_valsClass=valsClass))
            throw new IllegalArgumentException("No values class specified");
    }

    public AttributeGettersChain (final Class<?> valsClass, final Collection<? extends Method> c)
    {
        super(c);

        if (null == (_valsClass=valsClass))
            throw new IllegalArgumentException("No values class specified");
    }

    public AttributeGettersChain (final Class<?> valsClass, final int initialCapacity)
    {
        super(initialCapacity);

        if (null == (_valsClass=valsClass))
            throw new IllegalArgumentException("No values class specified");
    }
    /**
     * Invokes <U>successively</U> the current {@link Method}-s in the
     * {@link List} until all exhausted or a <I>null</I> value received
     * from the {@link Method#invoke(Object, Object...)} call.
     * @param value The initial value to start from (for generic types reasons
     * it is defined as {@link Object})
     * @return Invocation value - null if original value is null, no
     * {@link Method}-s in the {@link List} or null received from some
     * intermediate invocation
     * @throws Exception If failed to invoke
     */
    public Object invoke (final Object value) throws Exception
    {
        if ((null == value) || (size() <= 0))
            return null;

        Object    invObj=value;
        for (final Method m : this)
        {
            if (null == (invObj=m.invoke(invObj, AttributeAccessor.EMPTY_OBJECTS_ARRAY)))
                return null;
        }

        return invObj;
    }
    /**
     * @param attrsList A {@link Collection} of attributes names to be used
     * to build the chain (starting from the current values class). The name
     * of the attribute <U>may</U> contain the &quot;is/get&quot; as a hint.
     * If not, then it will be automatically detected (slight performance hit).
     * @return A {@link List} of the invocation chain (<U>not</U>
     * <code>this</code> instance). Null/empty if no chain built (e.g.,
     * null/empty attributes list)
     * @throws Exception If failed to build the chain.
     */
    public List<Method> setInvocationChain (final Collection<String> attrsList) throws Exception
    {
        clear();

        final int            numAttrs=(null == attrsList) ? 0 :  attrsList.size();
        final List<Method>    ml=(numAttrs <= 0) ? null : new ArrayList<Method>(numAttrs);
        if (numAttrs > 0)
        {
            Class<?>    curClass=getValuesClass();
            for (final String n : attrsList)
            {
                final String    aName=AttributeMethodType.getCleanGetterName(n);
                if ((null == aName) || (aName.length() <= 0))
                    throw new IllegalArgumentException("No attribute name specified for class=" + curClass.getName());

                // check if the attribute name contains an "is/get" hint
                AttributeMethodType    aType=AttributeMethodType.classifyAttributeName(aName);
                final Method        aMethod;
                if (AttributeMethodType.OPERATION.equals(aType))
                {
                    final Map.Entry<AttributeMethodType,Method>    aec=
                        AttributeMethodType.classifyAttributeGetterName(curClass, aName);
                    if (null == aec)
                        throw new NoSuchMethodException("Cannot classify " + curClass.getName() + "[" + aName + "] getter");

                    if ((null == (aType=aec.getKey())) || (null == (aMethod=aec.getValue())))
                        throw new NoSuchMethodException("No classification results for " + curClass.getName() + "[" + aName + "] getter");
                }
                else    // got a hint
                {
                    if (null == (aMethod=curClass.getMethod(aName)))    // should not happen
                        throw new NoSuchMethodException("No method lookup result for " + curClass.getName() + "[" + aName + "] getter");
                }

                if ((null == aType) || ((!aType.isGetter()) && (!aType.isPredicate())))
                    throw new IllegalStateException("Bad classification (" + aType + ") for " + curClass.getName() + "[" + aName + "] getter");

                ml.add(aMethod);
                curClass = aMethod.getReturnType();
            }

            addAll(ml);
        }

        return ml;
    }

    public Class<?> getAttributeType (final int index)
    {
        final Method    m=get(index);
        return m.getReturnType();
    }

    public Map.Entry<AttributeMethodType,Method> getAttributeAccess (final int index)
    {
        final Method                m=get(index);
        final AttributeMethodType    aType=AttributeMethodType.classifyAttributeMethod(m);
        if ((null == aType) || ((!aType.isGetter()) && (!aType.isPredicate())))
            throw new IllegalStateException("Bad classification (" + aType + ") for " + m + " getter");

        return new MapEntryImpl<AttributeMethodType,Method>(aType, m);
    }

    public String getAttributeName (final int index)
    {
        final Map.Entry<AttributeMethodType,Method>    aec=getAttributeAccess(index);
        final AttributeMethodType                    aType=aec.getKey();
        final Method                                m=aec.getValue();
        return aType.getPureAttributeName(m);
    }
    /**
     * Sets the invocation chain from a {@link String} - <B>Note:</B> any
     * existing {@link Method}-s are <U>removed</U> from the chain
     * @param invChain A {@link String} representing the chain using the
     * provided separation character to separate the attributes.
     * @param sepChar The character used to separate the attributes names.
     * @return A {@link List} of the invocation chain (<U>not</U>
     * <code>this</code> instance). Null/empty if no chain built (e.g.,
     * null/empty attributes list)
     * @throws Exception If failed to build the chain.
     */
    public List<Method> setInvocationChain (final String invChain, final char sepChar) throws Exception
    {
        return setInvocationChain(StringUtil.splitString(invChain, sepChar));
    }
}

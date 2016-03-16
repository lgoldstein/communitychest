package net.community.chest.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import net.community.chest.CoVariantReturn;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Holds {@link Method}-s that can be used to get/set an attribute</P>
 * @author Lyor G.
 * @since Aug 14, 2007 12:28:56 PM
 */
public class AttributeAccessor extends AttributeDescriptor {
    private Method    _getter    /* =null */, _setter /* =null */;
    public Method getGetter ()
    {
        return _getter;
    }

    public void setGetter (Method getter)
    {
        _getter = getter;
    }

    public Method getSetter ()
    {
        return _setter;
    }

    public void setSetter (Method setter)
    {
        _setter = setter;
    }
    /**
     *         Empty objects array to be used in {@link Method#invoke(Object, Object...)}
     * call of the getter in order to avoid automatic creation of such an
     * array for the <code>varargs</code> call
     */
    public static final Object[]    EMPTY_OBJECTS_ARRAY=new Object[0];
    public static final Class<?> resolveType (final Method gm, final Method sm)
    {
        if (gm != null)
            return gm.getReturnType();

        final Class<?>[]    pa=(null == sm) ? null : sm.getParameterTypes();
        if ((pa != null) && (1 == pa.length))
            return pa[0];

        return null;
    }
    /**
     * Attempts to see if there is an available getter - if so, then reports
     * the return type as the attribute type. Otherwise, the 1st (and only)
     * parameter type of the setter (if any)
     * @return Attribute type - null if neither setter nor getter available
     * @see net.community.chest.reflect.AttributeDescriptor#getType()
     */
    @Override
    public Class<?> getType ()
    {
        final Class<?>    t=super.getType();
        if (t != null)
            return t;

        return resolveType(getGetter(), getSetter());
    }
    /**
     * @param <A> The {@link Annotation} generic type
     * @param aClass An {@link Annotation} {@link Class} to look for
     * @return A &quot;pair&quot; represented as a {@link java.util.Map.Entry} whose
     * key=the {@link Annotation} instance, value=the <U>first</U> {@link Method}
     * annotated with this annotation. Null if no match found (or null/empty
     * class/methods to begin with). <B>Note:</B> first looks for the annotation
     * in the <U>getter</U> (if any)
     * @see MethodUtil#findAnnotation(Class, Method...)
     */
    public <A extends Annotation> Map.Entry<A,Method> findAnnotation (final Class<A> aClass)
    {
        return MethodUtil.findAnnotation(aClass, getGetter(), getSetter());
    }
    /**
     * @param <A> The {@link Annotation} generic type
     * @param aClass An {@link Annotation} {@link Class} to look for
     * @return A &quot;pair&quot; represented as a {@link java.util.Map.Entry} whose
     * key=the {@link Annotation} instance, value=the <U>first</U> {@link Method}
     * annotated with this annotation. Null if no match found (or null/empty
     * class/methods to begin with). <B>Note:</B> first looks for the annotation
     * in the <U>getter</U> (if any)
     */
    public <A extends Annotation> Map.Entry<A,Method> findClosestAnnotation (final Class<A> aClass)
    {
        if (null == aClass)
            return null;

        final Method[]    ma={ getGetter(), getSetter() };
        for (final Method m : ma)
        {
            if (null == m)
                continue;

            final Map.Entry<A,Method>    ap=MethodUtil.findClosestAnnotation(aClass, m);
            if (ap != null)
                return ap;
        }

        return null;
    }
    /**
     * Invokes a series of <I>getter</I> {@link Method}-s (i.e., ones with no
     * invocation arguments) and updates the provided {@link Map} with their
     * values (if non-<code>null</code>).
     * @param org Original {@link Map} - key=attribute name, value=matching
     * {@link Method#invoke(Object, Object...)} result. If original map is
     * null, then one will be created using <U>case insensitive</U> key(s)
     * @param ml A {@link Collection} of method "pairs" to invoke represented
     * as {@link java.util.Map.Entry}_ies - key=attribute name to map the result,
     * value=the {@link Method} to invoke
     * @param inst The {@link Object} instance on which to invoke the method(s)
     * @param errIfDuplicate TRUE=throws an {@link IllegalStateException} if
     * the same attribute name already has a value
     * @return Updated {@link Map} or new one if no original one provided and
     * non-null values retrieved
     * @throws Exception If failed to invoke
     */
    public static final Map<String,Object> updateGetterValues (
                final Map<String,Object>                                         org,
                final Collection<? extends Map.Entry<String,? extends Method>>    ml,
                final Object                                                    inst,
                final boolean                                                    errIfDuplicate)
        throws Exception
    {
        if ((null == ml) || (ml.size() <= 0))
            return org;

        Map<String,Object>    ret=org;
        for (final Map.Entry<String,? extends Method> me : ml)
        {
            final String    aName=(null == me) ? null : me.getKey();
            final Method    aMethod=(null == me) ? null : me.getValue();
            final Object    aValue=(aMethod == null) ? null : aMethod.invoke(inst, EMPTY_OBJECTS_ARRAY);
            if (null == aValue)
                continue;

            if (null == ret)
                ret = new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);
            final Object    prev=ret.put(aName, aValue);
            if ((prev != null) && errIfDuplicate)
                throw new IllegalStateException("updateGetterValues(" + inst + ")[" + aName + "] multiple values: old=" + prev + "/new=" + aValue);
        }

        return ret;
    }
    /**
     * Invokes a series of <I>getter</I> {@link Method}-s (i.e., ones with no
     * invocation arguments) and updates the provided {@link Map} with their
     * values (if non-<code>null</code>).
     * @param org Original {@link Map} - key=attribute name, value=matching
     * {@link Method#invoke(Object, Object...)} result. If original map is
     * null, then one will be created using <U>case insensitive</U> key(s)
     * @param mMap The {@link Map} of methods to invoke - key=attribute name
     * to map the result, value=the {@link Method} to invoke
     * @param inst The {@link Object} instance on which to invoke the method(s)
     * @param errIfDuplicate TRUE=throws an {@link IllegalStateException} if
     * the same attribute name already has a value
     * @return Updated {@link Map} or new one if no original one provided and
     * non-null values retrieved
     * @throws Exception If failed to invoke
     */
    public static final Map<String,Object> updateGetterValues (
                final Map<String,Object>             org,
                final Map<String,? extends Method>    mMap,
                final Object                        inst,
                final boolean                        errIfDuplicate)
        throws Exception
    {
        return ((null == mMap) || (mMap.size() <= 0)) ? org : updateGetterValues(org, mMap.entrySet(), inst, errIfDuplicate);
    }

    public static final Map<String,Method> getAttributeMethodsMap (
            final Map<String,? extends AttributeAccessor>    aMap,
            final boolean                                    useGetters)
    {
        final Collection<? extends Map.Entry<String,? extends AttributeAccessor>>    aal=
            ((null == aMap) || (aMap.size() <= 0)) ? null : aMap.entrySet();
        if ((null == aal) || (aal.size() <= 0))
            return null;

        Map<String,Method>    ret=null;
        for (final Map.Entry<String,? extends AttributeAccessor> ae : aal)
        {
            final AttributeAccessor    aa=(null == ae) ? null : ae.getValue();
            if (null == aa)
                continue;

            final Method    m=useGetters ? aa.getGetter() : aa.getSetter();
            if (null == m)
                continue;

            final String    key=(null == ae) ? null : ae.getKey();
            if (null == ret)
                ret = new TreeMap<String,Method>(String.CASE_INSENSITIVE_ORDER);

            final Method    p=ret.put(key, m);
            if (p != null)
                throw new IllegalStateException("getAttributeMethodsMap(" + key + ") multiple methods: prev=" + p + "/new=" + m);
        }

        return ret;
    }

    public static final Map<String,Object> updateAttributesGetterValues (
            final Map<String,Object>                         org,
            final Map<String,? extends AttributeAccessor>    aMap,
            final Object                                    inst,
            final boolean                                    errIfDuplicate)
        throws Exception
    {
        return updateGetterValues(org, getAttributeMethodsMap(aMap, true), inst, errIfDuplicate);
    }
    /**
     * Invokes a series of <I>getter</I> {@link Method}-s (i.e., ones with no
     * invocation arguments) and returns a {@link Map} with their values (if
     * non-<code>null</code>).
     * @param ml A {@link Collection} of method "pairs" to invoke represented
     * as {@link java.util.Map.Entry}-ies - key=attribute name to map the result,
     * value=the {@link Method} to invoke
     * @param inst The {@link Object} instance on which to invoke the method(s)
     * @param errIfDuplicate TRUE=throws an {@link IllegalStateException} if
     * the same attribute name (<U>case insensitive</U> ) already has a value
     * @return Retrieved non-null objects {@link Map} - key=attribute name,
     * value=matching {@link Method#invoke(Object, Object...)} result.
     * @throws Exception If failed to invoke
     */
    public static final Map<String,Object> retrieveGetterValues (
            final Collection<? extends Map.Entry<String,? extends Method>>    ml,
            final Object                                                    inst,
            final boolean                                                    errIfDuplicate)
        throws Exception
    {
        return updateGetterValues(null, ml, inst, errIfDuplicate);
    }
    /**
     * Invokes a series of <I>getter</I> {@link Method}-s (i.e., ones with no
     * invocation arguments) and returns a {@link Map} with their values (if
     * non-<code>null</code>).
     * @param mMap The {@link Map} of methods to invoke - key=attribute name
     * to map the result, value=the {@link Method} to invoke
     * @param inst The {@link Object} instance on which to invoke the method(s)
     * @param errIfDuplicate TRUE=throws an {@link IllegalStateException} if
     * the same attribute name (<U>case insensitive</U> ) already has a value
     * @return Retrieved non-null objects {@link Map} - key=attribute name,
     * value=matching {@link Method#invoke(Object, Object...)} result.
     * @throws Exception If failed to invoke
     */
    public static final Map<String,Object> retrieveGetterValues (
                final Map<String,? extends Method>    mMap,
                final Object                        inst,
                final boolean                        errIfDuplicate)
        throws Exception
    {
        return ((null == mMap) || (mMap.size() <= 0)) ? null : retrieveGetterValues(mMap.entrySet(), inst, errIfDuplicate);
    }
    /**
     * Invokes a series of <I>setters</I> on an object
     * @param ml A {@link Collection} of method "pairs" to invoke represented
     * as {@link java.util.Map.Entry}-ies - key=attribute name to lookup the value,
     * value=the {@link Method} to invoke
     * @param vMap The value {@link Map} to use to lookup the attribute value
     * to be used for the setter's invocation
     * @param inst The {@link Object} instance on which to invoke the method(s)
     * @param errIfMissing TRUE=throws an {@link IllegalStateException} if
     * a method has no mapped value to be used for invocation (otherwise the
     * method is ignored)
     * @throws Exception If failed to invoke
     */
    public static final void updateSetterValues (
            final Collection<? extends Map.Entry<String,? extends Method>>    ml,
            final Map<String,?>                                                vMap,
            final Object                                                    inst,
            final boolean                                                    errIfMissing)
        throws Exception
    {
        if ((null == ml) || (ml.size() <= 0))
            return;

        if ((null == vMap) || (vMap.size() <= 0))
        {
            if (errIfMissing)
                throw new IllegalStateException("updateSetterValues(" + inst + ") no values provided");
            return;
        }

        for (final Map.Entry<String,? extends Method> me : ml)
        {
            final String    aName=(null == me) ? null : me.getKey();
            if (!vMap.containsKey(aName))
            {
                if (errIfMissing)
                    throw new IllegalStateException("updateSetterValues(" + inst + ")[" + aName + "] no value provided");
            }

            final Method    aMethod=(null == me) ? null : me.getValue();
            final Object    aValue=vMap.get(aName);
            if (aMethod == null)
                throw new IllegalStateException("updateSetterValues(" + inst + ")[" + aName + "] no method");
            aMethod.invoke(inst, aValue);
        }
    }
    /**
     * Invokes a series of <I>setters</I> on an object
     * @param mMap The {@link Map} of methods to invoke - key=attribute name
     * to map the result, value=the {@link Method} to invoke
     * @param vMap The value {@link Map} to use to lookup the attribute value
     * to be used for the setter's invocation
     * @param inst The {@link Object} instance on which to invoke the method(s)
     * @param errIfMissing TRUE=throws an {@link IllegalStateException} if
     * a method has no mapped value to be used for invocation (otherwise the
     * method is ignored)
     * @throws Exception If failed to invoke
     */
    public static final void updateSetterValues (
            final Map<String,? extends Method>    mMap,
            final Map<String,?>                    vMap,
            final Object                        inst,
            final boolean                        errIfMissing)
        throws Exception
    {
        if ((null == mMap) || (mMap.size() <= 0))
            return;

        updateSetterValues(mMap.entrySet(), vMap, inst, errIfMissing);
    }

    public static final void updateAttributesSetterValues (
            final Map<String,? extends AttributeAccessor>    aMap,
            final Map<String,?>                                vMap,
            final Object                                    inst,
            final boolean                                    errIfMissing)
        throws Exception
    {
        updateSetterValues(getAttributeMethodsMap(aMap, false), vMap, inst, errIfMissing);
    }

    public AttributeAccessor (String name, Method getter, Method setter)
    {
        super(name);

        _getter = getter;
        _setter = setter;
    }

    public AttributeAccessor (AttributeAccessor other)
    {
        this((null == other) ? null : other.getName(),
             (null == other) ? null : other.getGetter(),
             (null == other) ? null : other.getSetter());
    }

    public AttributeAccessor (String name, Method getter)
    {
        this(name, getter, null);
    }

    public AttributeAccessor (String name)
    {
        super(name);
    }

    public AttributeAccessor ()
    {
        super();
    }
    /*
     * @see net.community.chest.reflect.AttributeDescriptor#clone()
     */
    @Override
    @CoVariantReturn
    public AttributeAccessor clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see net.community.chest.reflect.AttributeDescriptor#toString()
     */
    @Override
    public String toString ()
    {
        return super.toString()
            + "{"
            + ((null == getGetter()) ? "" : "R")
            + ((null == getSetter()) ? "" : "W")
            + "}"
            ;
    }
    /*
     * @see net.community.chest.reflect.AttributeDescriptor#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        final Class<?>    oc=(obj == null) ? null : obj.getClass();
        if (oc != getClass())
            return false;
        if (this == obj)
            return true;

        final AttributeAccessor    a=(AttributeAccessor) obj;
        if (!isSameDescriptor(a))
            return false;

        return AbstractComparator.compareObjects(getGetter(), a.getGetter())
            && AbstractComparator.compareObjects(getSetter(), a.getSetter())
            ;
    }
    /*
     * @see net.community.chest.reflect.AttributeDescriptor#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return super.hashCode()
             + ClassUtil.getObjectHashCode(getGetter())
             + ClassUtil.getObjectHashCode(getSetter())
             ;
    }

    public static final <I> I getAttributeAccessorInterface (
            final Object o, final AttributeAccessor aa, final Class<I> ifc) throws RuntimeException
    {
        final Class<?>    oc=(null == o) ? null : o.getClass();
        if (null == oc)
            return null;

        if (null == aa)
            throw new NoSuchElementException("getAttributeAccessorInterface() no accessor");

        if ((null == ifc) || (!ifc.isInterface()))
            throw new IllegalArgumentException("getAttributeAccessorInterface(" + aa.getName() + ") bad interface class: " + ((null == ifc) ? null : ifc.getName()));

        if (ifc.isAssignableFrom(oc))
            return ifc.cast(o);

        final Thread        t=Thread.currentThread();
        final ClassLoader    cl=t.getContextClassLoader();
        final Class<?>[]    ia={ ifc };
        return ifc.cast(Proxy.newProxyInstance(cl, ia, new AttributeAccessorInvocationHandler(o, aa)));
    }
}

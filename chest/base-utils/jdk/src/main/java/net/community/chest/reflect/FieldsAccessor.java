package net.community.chest.reflect;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.lang.TypedValuesContainer;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to optimize access to fields of a given class. This class uses
 * a <U>lazy</U> lookup mechanism by searching the field from the 'top-level'
 * class up to the supplied 'base' class (and no further)</P>
 *
 * @param <V> The accessed generic type
 * @author Lyor G.
 * @since Jan 22, 2008 8:36:08 AM
 */
public class FieldsAccessor<V> extends TreeMap<String,Field> implements TypedValuesContainer<V> {
    /**
     *
     */
    private static final long serialVersionUID = -418760848225731965L;
    private final Class<V>    _valsClass;
    /*
     * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
     */
    @Override
    public final Class<V> getValuesClass ()
    {
        return _valsClass;
    }

    private final Class<?>    _baseClass;
    public final Class<?> getBaseClass ()
    {
        return _baseClass;
    }
    /**
     * @param valsClass The top-level class to be used in order to lookup
     * fields by name
     * @param baseClass The bottom class (inclusive) in which it is still
     * OK to lookup for the field by name
     * @throws IllegalArgumentException if null values/base class
     */
    public FieldsAccessor (Class<V> valsClass, Class<?> baseClass) throws IllegalArgumentException
    {
        if (null == (_valsClass=valsClass))
            throw new IllegalArgumentException("No values class provided");
        if (null == (_baseClass=baseClass))
            throw new IllegalArgumentException("No base class provided");
    }

    public FieldsAccessor (Class<V> valsClass)
    {
        this(valsClass, Object.class);
    }

    public static final Field findFieldByName (final String name, final Field ... fields)
    {
        if ((null == name) || (name.length() <= 0)
         || (null == fields) || (fields.length <= 0))
            return null;

        for (final Field f : fields)
        {
            final String    fName=(null == f) ? null : f.getName();
            if ((fName != null) && (fName.length() > 0) && name.equals(fName))
                return f;
        }

        return null;
    }

    public static final Map<String,Field> updateFieldsMap (final Map<String,Field>                 fm,
                                                           final Collection<? extends Field>    fields,
                                                           final boolean                         ignoreDuplicates)
        throws IllegalStateException
    {
        if ((null == fields) || (fields.size() <= 0) || (null == fm))
            return fm;

        for (final Field f : fields)
        {
            final String    name=(null == f) /* should not happen */ ? null : f.getName();
            if ((null == name) || (name.length() <= 0))
                continue;    // should not happen

            final boolean    exists=fm.containsKey(name);
            if (exists)
            {
                if (ignoreDuplicates)
                    continue;
                throw new IllegalStateException("updateFieldsMap() duplicate field name in hierarchy: " + name);
            }

            fm.put(name, f);
        }

        return fm;
    }
    /**
     * @param c Top-level {@link Class} to start investigation from
     * @param base Basement class to stop investigation - if null, then
     * stopping when reached the {@link Object} class
     * @param caseSensitive TRUE=check for duplicates using case sensitive
     * key (FALSE=case insensitive)
     * @param ignoreDuplicates TRUE=if found same field name in more then
     * one class then preserve the 1st instance (the most top-level one).
     * Otherwise, <code>throw</code> an {@link IllegalStateException}
     * @return A {@link Map} whose key=field name (case in/sensitive as per
     * the <code>caseSensitive</code> parameter specification. May be
     * null/empty if no fields found (or null class to begin with)
     * @throws IllegalStateException if found duplicates and not allowed to
     * ignore them
     */
    public static final Map<String,Field> getDeclaredFieldsMap (final Class<?>     c,
                                                                final Class<?>     base,
                                                                final boolean    caseSensitive,
                                                                final boolean    ignoreDuplicates)
        throws IllegalStateException
    {
        final Field[]    fields=(null == c) ? null : c.getDeclaredFields();
        if ((null == fields) || (fields.length <= 0))
            return null;

        final Map<String,Field>    fm=caseSensitive ? new TreeMap<String,Field>() : new TreeMap<String,Field>(String.CASE_INSENSITIVE_ORDER);
        updateFieldsMap(fm, Arrays.asList(fields), ignoreDuplicates);

        // if no parent or "below" the base then stop
        final Class<?>    par=c.getSuperclass();
        if (null == par)
            return fm;
        if ((base != null) && (!base.isAssignableFrom(par)))
            return fm;

        final Map<String,Field>    bm=getDeclaredFieldsMap(par, base, caseSensitive, ignoreDuplicates);
        final Collection<? extends Field>    subFields=((null == bm) || (bm.size() <= 0)) ? null : bm.values();
        return updateFieldsMap(fm, subFields, ignoreDuplicates);
    }

    public static final Field findFieldByName (final String name, final Class<?> c, final Class<?> base)
    {
        if ((null == name) || (name.length() <= 0) || (null == c))
            return null;

        try
        {
            final Field    f=c.getDeclaredField(name);
            if (f != null)
                return f;
        }
        catch(NoSuchFieldException e)
        {
            // ignored - fall through
        }

        // if not found field in this class, then try the superclass
        if ((null == base) || (base == c))    // if no recursion bottom specified or loopback then stop
            return null;

        final Class<?>    par=c.getSuperclass();
        if ((null == par) || (!base.isAssignableFrom(par)))
            return null;    // if no parent or "below" the base then stop

        return findFieldByName(name, par, base);
    }

    public static final Field findFieldByName (final String name, final Class<?> c, final boolean recursive)
    {
        return findFieldByName(name, c, recursive ? Object.class : null);
    }

    public static final Object getStaticFieldValue (final String name, final Class<?> c, final boolean recursive)
        throws IllegalArgumentException, IllegalAccessException
    {
        final Field    f=findFieldByName(name, c, recursive);
        if (f == null)
            return null;

        if (!f.isAccessible())
            f.setAccessible(true);

        return f.get(null);
    }
    /*
     * @see java.util.TreeMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public synchronized /* to avoid race condition with 'get' */ Field put (String key, Field value)
    {
        return super.put(key, value);
    }
    /*
     * @see java.util.TreeMap#get(java.lang.Object)
     */
    @Override
    public Field get (Object key)
    {
        if (!(key instanceof String))
            return null;

        final String    name=String.class.cast(key);
        if ((null == name) || (name.length() <= 0))
            return null;

        synchronized(this)
        {
            Field    f=super.get(name);
            if (null == f)
            {
                if ((f=findFieldByName(name, getValuesClass(), getBaseClass())) != null)
                    put(name, f);
            }

            return f;
        }
    }
    /**
     * @param name {@link Field} name to find
     * @return The required {@link Field} (null if not found), with the
     * {@link Field#isAccessible()} set to TRUE (if it was not so)
     */
    public Field getAccessible (String name)
    {
        final Field    f=get(name);
        if ((f != null) && (!f.isAccessible()))
            f.setAccessible(true);

        return f;
    }

    public Object getFieldValue (Object o, String name)
        throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        final Field    f=getAccessible(name);
        if (null == f)    // should not happen
            throw new NoSuchFieldException("getFieldValue(" + name + ")");

        return f.get(o);
    }

    public <T> T getCastFieldValue (Object o, String name, Class<T> objClass)
        throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ClassCastException
    {
        return objClass.cast(getFieldValue(o, name));
    }

    public Object getStaticFieldValue (String name)
        throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        return getFieldValue(null, name);
    }

    public <T> T getCastStaticFieldValue (String name, Class<T> objClass)
        throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ClassCastException
    {
        return objClass.cast(getStaticFieldValue(name));
    }

    public void setFieldValue (V o, String name, Object v)
        throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException
    {
        final Field    f=getAccessible(name);
        if (null == f)    // should not happen
            throw new NoSuchFieldException("setFieldValue(" + name + ")[" + v + "]");

        f.set(o, v);
    }

    public void setStaticFieldValue (String name, Object v)
        throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException
    {
        setFieldValue(null, name, v);
    }
}

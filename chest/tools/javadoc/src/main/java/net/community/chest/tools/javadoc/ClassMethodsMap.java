package net.community.chest.tools.javadoc;

import java.util.Map;
import java.util.TreeMap;

import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Helper class that maps efficiently methods to their matching {@link MethodDoc}
 * object - taking into account the method's signature (so as to resolve
 * overloaded names)</P>
 *
 * @author Lyor G.
 * @since Aug 16, 2007 11:17:02 AM
 */
public class ClassMethodsMap extends TreeMap<String,MethodDoc> {
    /**
     *
     */
    private static final long serialVersionUID = 1640816890649247914L;
    /**
     * @param name method name - may NOT be null/empty
     * @param params parameters (fully-qualified) type names - may be
     * null/empty and even contain null/empty elements (ignored)
     * @return "unique" key - null/empty if error
     */
    public static final String getMethodKey (final String name, final String[] params)
    {
        final int    nLen=(null == name) ? 0 : name.length(),
                    numParams=(null == params) /* OK */ ? 0 : params.length;
        if (nLen <= 0)
            return null;

        final StringBuilder    sb=new StringBuilder(nLen + 2 + Math.max(numParams, 0) * 72);
        sb.append(name);
        sb.append('(');

        for (int    pIndex=0, eIndex=0; pIndex < numParams; pIndex++)
        {
            final String    t=params[pIndex];
            if ((null == t) || (t.length() <= 0))
                continue;    // should not happen

            if (eIndex > 0)
                sb.append(',');
            sb.append(t);
        }

        sb.append(')');
        return sb.toString();
    }
    /**
     * @param name method name - may NOT be null/empty
     * @param params parameters (fully-qualified) type names - may be
     * null/empty and even contain null/empty elements (ignored)
     * @return "unique" key - null/empty if error
     */
    public static final String getMethodKey (final String name, final Parameter[] params)
    {
        if ((null == name) || (name.length() <= 0))
            return null;

        final int        numParams=(null == params) /* OK */ ? 0 : params.length;
        final String[]    pars=(numParams <= 0) ? null : new String[numParams];
        for (int    pIndex=0; pIndex < numParams; pIndex++)
        {
            final Parameter    p=params[pIndex];
            final Type        pType=(null == p) /* should not happen */ ? null : p.type();
            pars[pIndex] = (null == pType) /* should not happen */ ? null : pType.qualifiedTypeName();
        }

        return getMethodKey(name, pars);
    }
    /**
     * @param md method whose key is required - may NOT be null
     * @return "unique" key - null/empty if error
     */
    public static final String getMethodKey (final MethodDoc md)
    {
        return getMethodKey((null == md) /* should not happen */ ? null : md.name(),
                            (null == md) /* should not happen */ ? null : md.parameters());
    }
    /*
     * @see java.util.TreeMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public MethodDoc put (final String mKey, final MethodDoc o) throws IllegalArgumentException
    {
        if ((null == mKey) || (mKey.length() <= 0) || (null == o))
            throw new IllegalArgumentException("put(" + mKey + ") bad/illegal key/object to map");

        return super.put(mKey, o);
    }
    /**
     * @param m method to be mapped - may NOT be null
     * @param o object to be mapped - may NOT be null
     * @return previous mapping - null if none
     * @throws IllegalArgumentException if null method/object
     */
    public MethodDoc put (final MethodDoc m, final MethodDoc o) throws IllegalArgumentException
    {
        if (null == o)
            throw new IllegalArgumentException("bad/illegal object to map");

        return put(getMethodKey(m), o);
    }
    /**
     * @param m method to be mapped to <U>itself</I> - may NOT be null
     * @return previous mapping - null if none
     * @throws IllegalArgumentException if null method/object
     */
    public MethodDoc put (final MethodDoc m) throws IllegalArgumentException
    {
        return put(m, m);
    }
    /**
     * @param name method name - may NOT be null/empty
     * @param params parameters (fully-qualified) type names - may be
     * null/empty and even contain null/empty elements (ignored)
     * @param o object to be mapped - may NOT be null
     * @return previous mapping - null if none
     * @throws IllegalArgumentException if null method/object
     */
    public MethodDoc put (final String name, final Parameter[] params, final MethodDoc o) throws IllegalArgumentException
    {
        if (null == o)
            throw new IllegalArgumentException("bad/illegal object to map");

        return put(getMethodKey(name, params), o);
    }
    /**
     * @param name method name - may NOT be null/empty
     * @param params parameters (fully-qualified) type names - may be
     * null/empty and even contain null/empty elements (ignored)
     * @param o object to be mapped - may NOT be null
     * @return previous mapping - null if none
     * @throws IllegalArgumentException if null method/object
     */
    public MethodDoc put (final String name, final String[] params, final MethodDoc o) throws IllegalArgumentException
    {
        if (null == o)
            throw new IllegalArgumentException("bad/illegal object to map");

        return put(getMethodKey(name, params), o);
    }
    /**
     * @param mKey method key generated by one of the <I>getMethodKey</I>
     * overloads (<B>Caveat emptor:</B> not validated) - if null, then
     * same as if lookup failed
     * @return mapped object - null if none found
     */
    public MethodDoc get (final String mKey)
    {
        if ((null == mKey) || (mKey.length() <= 0))
            return null;
        else
            return super.get(mKey);
    }
    /**
     * @param m method to be mapped - if null, then nothing is looked up
     * @return previous mapping - null if none
     */
    public MethodDoc get (final MethodDoc m)
    {
        return get(getMethodKey(m));
    }
    /*
     * @see java.util.TreeMap#get(java.lang.Object)
     */
    @Override
    public MethodDoc get (Object key)
    {
        if (null == key)
            return null;
        else if (key instanceof String)
            return get((String) key);
        else if (key instanceof MethodDoc)
            return get((MethodDoc) key);
        else
            return null;
    }
    /**
     * @param name method name - may NOT be null/empty
     * @param params parameters (fully-qualified) type names - may be
     * null/empty and even contain null/empty elements (ignored)
     * @return previous mapping - null if none
     * @throws IllegalArgumentException if null method/object
     */
    public MethodDoc get (final String name, final Parameter[] params)
    {
        return get(getMethodKey(name, params));
    }
    /**
     * @param name method name - may NOT be null/empty
     * @param params parameters (fully-qualified) type names - may be
     * null/empty and even contain null/empty elements (ignored)
     * @return previous mapping - null if none
     * @throws IllegalArgumentException if null method/object
     */
    public MethodDoc get (final String name, final String[] params)
    {
        return get(getMethodKey(name, params));
    }
    /*
     * @see java.util.Map#containsKey(java.lang.Object)
     *
     */
    @Override
    public boolean containsKey (Object key)
    {
        if (null == key)
            return false;
        else if (key instanceof String)
            return (get((String) key) != null);
        else if (key instanceof MethodDoc)
            return (get((MethodDoc) key) != null);
        else
            return false;
    }
    /*
     * @see java.util.TreeMap#putAll(java.util.Map)
     */
    @Override
    public void putAll (Map<? extends String, ? extends MethodDoc> map)
    {
        throw new UnsupportedOperationException(getClass().getName() + "#putAll() N/A");
    }
    /**
     * @param mKey method key generated by one of the <I>getMethodKey</I>
     * overloads (<B>Caveat emptor:</B> not validated) - if null, then
     * same as if lookup failed
     * @return removed associated value
     */
    public MethodDoc remove (final String mKey)
    {
        if ((null == mKey) || (mKey.length() <= 0))
            return null;
        else
            return super.remove(mKey);
    }
    /**
     * @param m method to be mapped - if null, then nothing is looked up
     * @return previous mapping - null if none
     */
    public MethodDoc remove (final MethodDoc m)
    {
        return remove(getMethodKey(m));
    }
    /*
     * @see java.util.TreeMap#remove(java.lang.Object)
     */
    @Override
    public MethodDoc remove (Object key)
    {
        if (null == key)
            return null;
        else if (key instanceof String)
            return remove((String) key);
        else if (key instanceof MethodDoc)
            return remove((MethodDoc) key);
        else
            return null;
    }
    /**
     * @param name method name - may NOT be null/empty
     * @param params parameters (fully-qualified) type names - may be
     * null/empty and even contain null/empty elements (ignored)
     * @return previous mapping - null if none
     * @throws IllegalArgumentException if null method/object
     */
    public MethodDoc remove (final String name, final Parameter[] params)
    {
        return get(getMethodKey(name, params));
    }
    /**
     * @param name method name - may NOT be null/empty
     * @param params parameters (fully-qualified) type names - may be
     * null/empty and even contain null/empty elements (ignored)
     * @return previous mapping - null if none
     * @throws IllegalArgumentException if null method/object
     */
    public MethodDoc remove (final String name, final String[] params)
    {
        return get(getMethodKey(name, params));
    }
}

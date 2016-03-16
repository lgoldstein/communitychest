/*
 *
 */
package net.community.chest.util.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.community.chest.lang.EnumUtil;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 17, 2009 11:50:50 AM
 */
public final class MapsUtils {
    private MapsUtils ()
    {
        throw new UnsupportedOperationException("No instance");
    }
    /**
     * Reverses the keys and values from one {@link Map} to another - i.e.,
     * the source map values become the keys, and its keys become the values
     * @param src The source map
     * @param dst The destination map
     * @return Same instance as the destination map
     */
    public static <K,V,M extends Map<? super V,? super K>> M flip (Map<? extends K,? extends V> src, M dst) {
        if (size(src) <= 0) {
            return dst;
        }

        for (Map.Entry<? extends K,? extends V> se : src.entrySet()) {
            K    key=se.getKey();
            V    value=se.getValue();
            dst.put(value, key);
        }

        return dst;
    }

    public static boolean isEmpty (Map<?,?> m) {
        return size(m) <= 0;
    }

    public static int size (Map<?,?> m) {
        return (m == null) ? 0 : m.size();
    }

    public static boolean compareMaps (Map<?,?> m1, Map<?,?> m2) {
        if (m1 == m2)
            return true;
        if ((m1 == null) || (m2 == null))
            return false;
        if (m1.size() != m2.size())
            return false;

        return containsAll(m1, m2) && containsAll(m2, m1);
    }

    public static boolean containsAll (Map<?,?> m, Map<?,?> subMap) {
        if (m == subMap)
            return true;
        if ((subMap == null) || subMap.isEmpty())
            return true;
        if ((m == null) || m.isEmpty())
            return false;

        for (Map.Entry<?,?> subEntry : subMap.entrySet()) {
            Object  subKey=subEntry.getKey(), subValue=subEntry.getValue();
            /*
             * If the associated value is null we need to distinguish between
             * it and the fact that the key does not exist in the main map
             */
            if (subValue == null) {
                if (!m.containsKey(subKey)) {
                    return false;
                }

                if (m.get(subKey) != null) {
                    return false;
                }
            } else {
                Object  value=m.get(subKey);
                if (!subValue.equals(value)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @param map The {@link Map} to put the value into
     * @param key The mapping key
     * @param value The value
     * @return <code>true</code> if the value is non-<code>null</code> and has been
     * put in the map. <code>false</code> if the value is <code>null</code>, in which
     * case the value is <U>not</U> put in the map.
     */
    public static <K,V> boolean putIfNonNull (Map<K,V> map, K key, V value) {
        if (value == null) {
            return false;
        }

        map.put(key, value);
        return true;
    }

    /**
     * Creates a {@link java.util.Map.Entry} with the given key/value pair
     * @param key The key
     * @param value The associated value
     * @return {@link java.util.Map.Entry} whose {@link java.util.Map.Entry#getKey()} returns the
     * key and {@link java.util.Map.Entry#getValue()} returns the value. <B>Note:</B>
     * any attempt to call {@link java.util.Map.Entry#setValue(Object)} throws an
     * {@link UnsupportedOperationException}
     */
    public static <K,V> Map.Entry<K,V> createMapEntry (final K key, final V value) {
        return new Map.Entry<K,V>() {
            @Override
            public K getKey() {
                return key;
            }

            @Override
            public V getValue() {
                return value;
            }

            @Override
            public V setValue(V v) {
                throw new UnsupportedOperationException("setValue(" + v + ") N/A");
            }

            @Override
            public int hashCode() {
                return ClassUtil.getObjectHashCode(key) + ClassUtil.getObjectHashCode(value);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null)
                    return false;
                if (this == obj)
                    return true;
                if (!(obj instanceof Map.Entry<?,?>))
                    return false;

                Map.Entry<?,?>  other=(Map.Entry<?,?>) obj;
                return ClassUtil.typedEquals(key, other.getKey())
                    && ClassUtil.typedEquals(value, other.getValue())
                    ;
            }

            @Override
            public String toString() {
                return key + "=" + value;
            }
        };
    }

    public static final <K,V> Map<K,V> putAll (final Map<K,V> m, final Collection<? extends Map.Entry<? extends K,? extends V>> el)
    {
        if ((null == m) || (null == el) || (el.size() <= 0))
            return m;

        for (final Map.Entry<? extends K,? extends V> ee : el)
        {
            final K    k=(null == ee) ? null : ee.getKey();
            final V    v=(null == ee) ? null : ee.getValue();
            if (null == k)    // avoid null keys
                continue;

            m.put(k, v);
        }

        return m;
    }

    // returns the list of pairs that have been removed
    public static final <K,V> List<Map.Entry<K,V>> removeAll (final Map<K,V> m, final Collection<? extends K> keys)
    {
        final int    numKeys=(keys == null) ? 0 : keys.size();
        if ((m == null) || (m.size() <= 0) ||  (numKeys <= 0))
            return null;

        List<Map.Entry<K,V>>    remList=null;
        for (final K k : keys)
        {
            final V    v=(k == null) ? null : m.remove(k);
            if (v == null)
                continue;

            final Map.Entry<K,V>    e=new MapEntryImpl<K,V>(k, v);
            if (remList == null)
                remList = new ArrayList<Map.Entry<K,V>>(Math.max(numKeys, 5));
            if (!remList.add(e))
                continue;    // debug breakpoint
        }

        return remList;
    }

    public static final <E extends Enum<E>,V> Map<E,V> toEnumNamesMap (
                                final Map<String,? extends V>     vm,
                                final Class<E>                    ec)
        throws NoSuchElementException, IllegalStateException
    {
        final Collection<? extends Map.Entry<String,? extends V>>    vl=
            ((null == vm) || (vm.size() <= 0) || (null == ec)) ? null : vm.entrySet();
        if ((null == vl) || (vl.size() <= 0))
            return null;

        final Collection<E>    vals=Arrays.asList(ec.getEnumConstants());
        Map<E,V>            ret=null;
        for (final Map.Entry<String,? extends V> ve : vl)
        {
            final String    vn=(null == ve) ? null : ve.getKey();
            final V            vv=(null == ve) ? null : ve.getValue();
            if ((null == vn) || (vn.length() <= 0) || (null == vv))
                continue;

            final E    ev=EnumUtil.fromName(vals, vn, false);
            if (null == ev)
                throw new NoSuchElementException("toEnumNamesMap(" + ec.getSimpleName() + ")[" + vn + "] no match found");

            if (null == ret)
                ret = new EnumMap<E,V>(ec);

            final V    prev=ret.put(ev, vv);
            if (prev != null)
                throw new IllegalStateException("toEnumNamesMap(" + ec.getSimpleName() + ")[" + vn + "] multiple mappings: prev=" + prev + "/new=" + vv);
        }

        return ret;
    }

    public static final <E extends Enum<E>,V> Map<E,V> toEnumStringMap (
                                final Map<String,? extends V>     vm,
                                final Class<E>                    ec)
        throws NoSuchElementException, IllegalStateException
    {
        final Collection<? extends Map.Entry<String,? extends V>>    vl=
            ((null == vm) || (vm.size() <= 0) || (null == ec)) ? null : vm.entrySet();
        if ((null == vl) || (vl.size() <= 0))
            return null;

        final Collection<E>    vals=Arrays.asList(ec.getEnumConstants());
        Map<E,V>            ret=null;
        for (final Map.Entry<String,? extends V> ve : vl)
        {
            final String    vn=(null == ve) ? null : ve.getKey();
            final V            vv=(null == ve) ? null : ve.getValue();
            if ((null == vn) || (vn.length() <= 0) || (null == vv))
                continue;

            final E    ev=CollectionsUtils.fromString(vals, vn, false);
            if (null == ev)
                throw new NoSuchElementException("toEnumNamesMap(" + ec.getSimpleName() + ")[" + vn + "] no match found");

            if (null == ret)
                ret = new EnumMap<E,V>(ec);

            final V    prev=ret.put(ev, vv);
            if (prev != null)
                throw new IllegalStateException("toEnumNamesMap(" + ec.getSimpleName() + ")[" + vn + "] multiple mappings: prev=" + prev + "/new=" + vv);
        }

        return ret;
    }
}

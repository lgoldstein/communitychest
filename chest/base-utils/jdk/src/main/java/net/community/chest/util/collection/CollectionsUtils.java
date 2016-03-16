package net.community.chest.util.collection;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * Copyright 2007 as per GPLv2
 *
 * Useful {@link Collection} related utilities
 *
 * @author Lyor G.
 * @since Jul 19, 2007 1:33:46 PM
 */
public final class CollectionsUtils {
    private CollectionsUtils ()
    {
        throw new UnsupportedOperationException("No instance");
    }

    /**
     * @param c The {@link Collection} to be accessed
     * @return The 1st member in the collection - if not empty, or <code>null</code> otherwise
     */
    public static <T> T getFirstMember (Collection<? extends T> c) {
        if (size(c) <= 0) {
            return null;
        }

        if (c instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<T>    list=(List<T>) c;
            return list.get(0);
        }

        Iterator<? extends T>    iter=c.iterator();
        if ((iter != null) && iter.hasNext()) {
            return iter.next();
        }

        return null;
    }

    public static boolean isEmpty (Collection<?> c) {
        return (size(c) <= 0);
    }

    public static int size (Collection<?> c) {
        return (c == null) ? 0 : c.size();
    }
    /**
     * Adds all <U>non-null</U> values from the source {@link Collection} to
     * the destination one
     * @param <V> The type of value in the {@link Collection}-s
     * @param <C1> Source argument generic {@link Collection} type
     * @param <C2> Destination argument generic {@link Collection} type
     * @param src source {@link Collection} - if null/empty then nothing is
     * added to the destination one
     * @param dst destination {@link Collection} - may not be null if objects
     * are to be added to it
     * @return updated destination {@link Collection}
     */
    public static <V,C1 extends Collection<V>,C2 extends Collection<V>> C2 duplicateCollection (C1 src, C2 dst)
    {
        if ((src != null) && (src.size() > 0))
        {
            for (final V v : src)
            {
                if (v != null)
                    dst.add(v);
            }
        }

        return dst;
    }
    /**
     * Checks if 2 {@link Collection} have the same size
     * @param c1 First {@link Collection}
     * @param c2 Second {@link Collection}
     * @return <code>true</code> if both have the same &quot;size&quot -
     * <B>Note:</B> for the purpose of this method a <code>null</code>
     * {@link Collection} is considered to have size=0
     */
    public static final boolean isSameCardinality (final Collection<?> c1, final Collection<?> c2)
    {
        // first check the obvious
        if (c1 == c2)
            return true;

        if ((null == c1) || (c1.size() <= 0))
            return (null == c2) || (c2.size() <= 0);
        else if ((null == c2) || (c2.size() <= 0))
            return (c1.size() <= 0);

        return (c1.size() == c2.size());
    }
    /**
     * Checks if 2 {@link Collection}-s contain exactly the same members
     * @param <V> Expected element(s) value type
     * @param <C1> First type of {@link Collection} being checked
     * @param <C2> Second type of {@link Collection} being checked
     * @param c1 source {@link Collection}
     * @param c2 destination {@link Collection}
     * @return TRUE if either both are null/empty or contain the same members
     */
    public static <V,C1 extends Collection<? extends V>,C2 extends Collection<? extends V>> boolean isSameMembers (final C1 c1, final C2 c2)
    {
        if (!isSameCardinality(c1, c2))
            return false;

        if ((null == c1) || (c1.size() <= 0))
            return true;    // since same cardinality

        return c1.containsAll(c2)
            && c2.containsAll(c1);
    }
    /**
     * @param c {@link Collection} whose <U>cumulative</U> members hash code
     * is required - may be null/empty
     * @return <U>cumulative</U> (non-null) members hash code(s) - zero if
     * null/empty collection to begin with
     */
    public static int getMembersHashCode (final Collection<?> c)
    {
        int    hashVal=0;
        if ((c != null) && (c.size() > 0))
        {
            for (final Object o : c)
                hashVal += ((null == o) ? 0 : o.hashCode());
        }

        return hashVal;
    }
    /**
     * Adds a (non-null) member to a {@link Collection} creating one if
     * necessary.
     * @param <V> Expected element(s) value type
     * @param <C> The {@link Collection} generic type
     * @param org original {@link Collection} if null and non-null value
     * member to be added then one will be created. Otherwise the member
     * value will be added to the original
     * @param val member to be added - ignored if null
     * @param instClass {@link Class} to use to create a new collection if
     * the original one is null and need to add a non-null member
     * @return updated collection - may be same as input, and even null/empty
     * if original collection was null/empty and null member supplied
     * @throws Exception if unable to generate new collection instance
     */
    public static <V,C extends Collection<V>> C addMember (
            final C org, final V val, final Class<? extends C> instClass)
        throws Exception
    {
        C    ret=org;
        if (val != null)
        {
            if (null == ret)
                ret = instClass.newInstance();
            ret.add(val);
        }

        return ret;
    }
    /**
     * Adds a (non-null) member to a {@link Collection} creating one if
     * necessary.
     * @param <V> Expected element(s) value type
     * @param <C> The {@link Collection} generic type
     * @param org original {@link Collection} if null and non-null value
     * member to be added then one will be created. Otherwise the member
     * value will be added to the original
     * @param val member to be added - ignored if null
     * @param instCtor {@link Constructor} to use to create a new collection if
     * the original one is null and need to add a non-null member
     * @return updated collection - may be same as input, and even null/empty
     * if original collection was null/empty and null member supplied
     * @throws Exception if unable to generate new collection instance
     */
    public static <V,C extends Collection<V>> C addMember (
            final C org, final V val, final Constructor<? extends C> instCtor)
        throws Exception
    {
        C    ret=org;
        if (val != null)
        {
            if (null == ret)
                ret = instCtor.newInstance();
            ret.add(val);
        }

        return ret;
    }
    /**
     * Adds a non-null/empty string to a {@link Collection} of such - creating
     * it if necessary
     * @param <C> The {@link Collection} generic type
     * @param org original {@link Collection} if null and non-null value
     * member to be added then one will be created. Otherwise the member
     * value will be added to the original
     * @param val member to be added - ignored if null/empty
     * @param instClass {@link Class} to use to create a new collection if
     * the original one is null and need to add a non-null/empty member
     * @return updated collection - may be same as input, and even null/empty
     * if original collection was null/empty and null/empty member supplied
     * @throws Exception if unable to generate new collection instance
     */
    public static <C extends Collection<String>> C addStringMember (
            final C org, final String val, final Class<? extends C> instClass)
        throws Exception
    {
        if ((null == val) || (val.length() <= 0))
            return org;
        else
            return addMember(org, val, instClass);
    }
    /**
     * Adds a non-null/empty string to a {@link Collection} of such - creating
     * it if necessary
     * @param <C> The {@link Collection} generic type
     * @param org original {@link Collection} if null and non-null value
     * member to be added then one will be created. Otherwise the member
     * value will be added to the original
     * @param val member to be added - ignored if null/empty
     * @param instCtor {@link Constructor} to use to create a new collection if
     * the original one is null and need to add a non-null/empty member
     * @return updated collection - may be same as input, and even null/empty
     * if original collection was null/empty and null/empty member supplied
     * @throws Exception if unable to generate new collection instance
     */
    public static <C extends Collection<String>> C addStringMember (
            final C org, final String val, final Constructor<? extends C> instCtor)
        throws Exception
    {
        if ((null == val) || (val.length() <= 0))
            return org;
        else
            return addMember(org, val, instCtor);
    }
    /**
     * Locates an element in a {@link List} of elements
     * @param <T> Type of {@link List} elements
     * @param v Element to be located - if null, then failure result returned
     * @param c The {@link Comparator} to use to check if elements are equal
     * to the sought value. If null, then only reference equality and the
     * {@link Object#equals(Object)} method are used.
     * @param startIndex Index to start looking for the element (inclusive)
     * @param vals The {@link List} values - <B>Note:</B> null elements are ignored
     * @return Index of element in the {@link List} - negative if not found
     */
    public static final <T> int findElementIndex (
            final T v, final Comparator<? super T> c, final int startIndex, final List<? extends T> vals)
    {
        final int    numVals=(null == vals) ? 0 : vals.size();
        if ((null == v) || (numVals <= 0) || (startIndex >= numVals))
            return (-1);

        for (int    vIndex=Math.max(0,startIndex); vIndex < numVals; vIndex++)
        {
            final T    cv=vals.get(vIndex);
            if (null == cv)
                continue;

            if (v == cv)    // check the simplest first
                return vIndex;

            if (c != null)
            {
                if (0 == c.compare(cv, v))
                    return vIndex;
            }
            else
            {
                 if (v.equals(cv))
                     return vIndex;
            }
        }

        return (-1);
    }
    /**
     * Locates an element in a {@link List} of elements
     * @param <T> Type of {@link List} elements
     * @param v Element to be located - if null, then failure result returned
     * @param c The {@link Comparator} to use to check if elements are equal
     * to the sought value. If null, then only reference equality and the
     * {@link Object#equals(Object)} method are used.
     * @param vals The {@link List} values - <B>Note:</B> null elements are ignored
     * @return Index of element in the {@link List} - negative if not found
     */
    public static final <T> int findElementIndex (
            final T v, final Comparator<? super T> c, final List<? extends T> vals)
    {
        return findElementIndex(v, c, 0, vals);
    }
    /**
     * Locates an element in a {@link List} of elements
     * @param <T> Type of elements in the {@link List}
     * @param v Element to be located using {@link Object#equals(Object)} - <code>if null</code>, then failure result returned
     * @param vals The {@link List} values - <B>Note:</B> null elements are ignored
     * @return Index of element in the {@link List} - negative if not found
     */
    public static final <T> int findElementIndex (final T v, final List<? extends T> vals)
    {
        if ((null == vals) || (null == v) || (vals.size() <= 0))
            return (-1);
        else
            return vals.indexOf(v);
    }
    /**
     * @param <T> Type of object being looked for
     * @param l A {@link Collection} of objects to scan
     * @param v A value to compare the collection objects against
     * @param c A {@link Comparator} to compare the collection objects and
     * the parameter one
     * @return The matching collection object - <code>null</code> if no match
     * found (or if null/empty collection/comparison value/comparator)
     */
    public static final <T> T findElement (
            final Collection<? extends T> l, final T v, final Comparator<? super T> c)
    {
        if ((null == l) || (l.size() <= 0) || (null == v) || (null == c))
            return null;

        for (final T cv : l)
        {
            if (0 == c.compare(v, cv))
                return cv;
        }

        return null;
    }
    /**
     * @param <T> The type of {@link Comparable} object being looked for
     * @param l A {@link Collection} of objects to scan
     * @param v A value to compare the collection objects against
     * @return The matching collection object - <code>null</code> if no match
     * found (or if null/empty collection/comparison value)
     */
    public static final <T extends Comparable<T>> T findElement (
            final Collection<? extends T> l, final T v)
    {
        if ((null == l) || (l.size() <= 0) || (null == v))
            return null;

        for (final T cv : l)
        {
            if (0 == v.compareTo(cv))
                return cv;
        }

        return null;
    }
    /**
     * @param <T> Type of contained/compared objects
     * @param l A {@link Collection} of objects to be checked - if null/empty
     * then <code>false</code> is returned
     * @param v The value to be checked - if <code>null</code> then
     * <code>false</code> is returned
     * @param c A {@link Comparator} to be used for equality checking - if
     * <code>null</code> then {@link Collection#contains(Object)} is called
     * @return <code>true</code> if element is contained in the collection
     */
    public static final <T> boolean containsElement (
            final Collection<? extends T> l, final T v, final Comparator<? super T> c)
    {
        if ((null == l) || (l.size() <= 0) || (null == v))
            return false;

        if (null == c)
            return l.contains(v);

        for (final T cv : l)
        {
            if (0 == c.compare(v, cv))
                return true;
        }

        return false;
    }

    public static <T> boolean compareCollections (Collection<? extends T> c1, Collection<? extends T> c2) {
        if (c1 == c2)
            return true;
        if ((c1 == null) || (c2 == null))
            return false;
        if (c1.size() != c2.size())
            return false;

        return c1.containsAll(c2) && c2.containsAll(c1);
    }
    /**
     * @param <V> Type of object being checked
     * @param <C1> First type of {@link Collection} being checked
     * @param <C2> Second type of {@link Collection} being checked
     * @param main The &quot;main&quot; {@link Collection} that is checked
     * if it contains the other
     * @param sub The {@link Collection} whose members are checked if
     * contained by the quot;main&quot; one
     * @param c The {@link Comparator} to use - if <code>null</code> then
     * the built-in {@link Collection#containsAll(Collection)} is called on
     * the &quot;main&quot; {@link Collection}
     * @return <code>true</code> if all members from the &quot;sub&quot;
     * {@link Collection} are contained by the &quot;main&quot; one
     */
    public static final <V,C1 extends Collection<? extends V>,C2 extends Collection<? extends V>> boolean containsAll (
            final C1 main, final C2 sub, final Comparator<? super V> c)
    {
        if ((null == sub) || (sub.size() <= 0))
            return true;    // empty is contained in everything

        if ((null == main) || (main.size() <= 0))
            return false;    // empty does not contain anything

        if (null == c)
            return main.containsAll(sub);

        for (final V v : sub)
        {
            if (!containsElement(main, v, c))
                return false;
        }

        return true;
    }
    /**
     * Checks if 2 {@link Collection}-s contain exactly the same members
     * @param <V> Expected element(s) value type
     * @param <C1> First type of {@link Collection} being checked
     * @param <C2> Second type of {@link Collection} being checked
     * @param c1 source {@link Collection}
     * @param c2 destination {@link Collection}
     * @param c The {@link Comparator} to check for containment - if <code>null</code>
     * then {@link #isSameMembers(Collection, Collection)} is called
     * @return TRUE if either both are null/empty or have same cardinality and
     * contain the same members
     */
    public static <V,C1 extends Collection<? extends V>,C2 extends Collection<? extends V>> boolean isSameMembers (
                final C1 c1, final C2 c2, final Comparator<? super V> c)
    {
        if (!isSameCardinality(c1, c2))
            return false;

        if ((null == c1) || (c1.size() <= 0))
            return true;    // since same cardinality

        return containsAll(c1, c2, c) && containsAll(c2, c1, c);
    }
    /**
     * Removes all the elements that match according to the supplied {@link Comparator}
     * @param <T> Type of elements being manipulated
     * @param l A {@link Collection} of objects to be manipulated - if null/empty
     * then nothing is done
     * @param v The value to be compared - if <code>null</code> then nothing
     * is done
     * @param c A {@link Comparator} to be used for equality checking - if
     * <code>null</code> then {@link Collection#remove(Object)} is called
     * @return A {@link Collection} of the <U>deleted</U> objects - null/empty
     * if no matches found
     */
    public static final <T> Collection<T> removeMatchingElements (
            final Collection<? extends T> l, final T v, final Comparator<? super T> c)
    {
        if ((null == l) || (l.size() <= 0) || (null == v))
            return null;

        Collection<T>    dl=null;
        if (null == c)
        {
            if (l.contains(v))
            {
                dl = new LinkedList<T>();
                dl.add(v);
            }
        }
        else
        {
            for (final T cv : l)
            {
                if (c.compare(v, cv) != 0)
                    continue;

                if (null == dl)
                    dl = new LinkedList<T>();
                dl.add(cv);
            }
        }

        if ((dl != null) && (dl.size() > 0))
            l.removeAll(dl);

        return dl;
    }
    // returns same as input
    public static <T extends Comparable<? super T>> List<T> sort (final List<T> list)
    {
        if ((list != null) && (list.size() > 1))
            Collections.sort(list);
        return list;
    }
    // returns same as input
    public static <T> List<T> sort (final List<T> list, final Comparator<? super T> c)
    {
        if ((list != null) && (list.size() > 1) && (c != null))
            Collections.sort(list, c);
        return list;
    }
    /**
     * @param <N> Type of array component being compared
     * @param vals Values being compared
     * @return The value being {@link Comparable#compareTo(Object)} higher
     * than all other - excluding <code>null</code>-s. <B>Note:</B> may be
     * <code>null</code> if null/empty array or all values are null.
     */
    public static final <N extends Comparable<N>> N getMaxValue (final Collection<? extends N> vals)
    {
        if ((null == vals) || (vals.size() <= 0))
            return null;

        N    ret=null;
        for (final N t : vals)
        {
            if (null == t)
                continue;    // ignore null values
            if ((ret != null) && (ret.compareTo(t) > 0))
                continue;    // skip if already have greater value

            ret = t;
        }

        return ret;
    }
    /**
     * @param <N> Type of component being compared
     * @param vals Values being compared
     * @return The value being {@link Comparable#compareTo(Object)} lower
     * than all other - excluding <code>null</code>-s. <B>Note:</B> may be
     * <code>null</code> if null/empty {@link Collection} or all values are null.
     */
    public static final <N extends Comparable<N>> N getMinValue (final Collection<? extends N> vals)
    {
        if ((null == vals) || (vals.size() <= 0))
            return null;

        N    ret=null;
        for (final N t : vals)
        {
            if (null == t)
                continue;    // ignore null values
            if ((ret != null) && (ret.compareTo(t) < 0))
                continue;    // skip if already have lower value

            ret = t;
        }

        return ret;
    }
    /**
     * @param <N> Type of array component being compared
     * @param vals Values being compared
     * @param c The {@link Comparator} to use - ignored if <code>null</code>
     * @return The value being {@link Comparable#compareTo(Object)} higher
     * than all other - excluding <code>null</code>-s. <B>Note:</B> may be
     * <code>null</code> if null/empty array or all values are null.
     */
    public static final <N> N getMaxValue (final Collection<? extends N> vals, final Comparator<? super N> c)
    {
        if ((null == vals) || (vals.size() <= 0) || (null == c))
            return null;

        N    ret=null;
        for (final N t : vals)
        {
            if (null == t)
                continue;    // ignore null values
            if ((ret != null) && (c.compare(ret, t) > 0))
                continue;    // skip if already have greater value

            ret = t;
        }

        return ret;
    }
    /**
     * @param <N> Type of array component being compared
     * @param vals Values being compared
     * @param c The {@link Comparator} to use - ignored if <code>null</code>
     * @return The value being {@link Comparable#compareTo(Object)} lower
     * than all other - excluding <code>null</code>-s. <B>Note:</B> may be
     * <code>null</code> if null/empty array or all values are null.
     */
    public static final <N> N getMinValue (final Collection<? extends N> vals, final Comparator<? super N> c)
    {
        if ((null == vals) || (vals.size() <= 0) || (null == c))
            return null;

        N    ret=null;
        for (final N t : vals)
        {
            if (null == t)
                continue;    // ignore null values
            if ((ret != null) && (c.compare(ret, t) < 0))
                continue;    // skip if already have lower value

            ret = t;
        }

        return ret;
    }
    /**
     * @param <V> Type of value being instantiated
     * @param vals A {@link Collection} of {@link String}-s to be used for
     * instantiating each member
     * @param vsi The {@link ValueStringInstantiator} to be used
     * @return A {@link List} of instantiated values. May be null/empty if
     * no values, or no instantiator or no non-null/empty strings/values
     * instantiated
     * @throws Exception If failed to instantiate a specific value
     */
    public static final <V> List<V> instantiateObjects (
            final Collection<String>                    vals,
            final ValueStringInstantiator<? extends V>    vsi)
        throws Exception
    {
        final int    numVals=(null == vals) ? 0 : vals.size();
        if ((numVals <= 0) || (null == vsi))
            return null;

        List<V>    ret=null;
        for (final String v : vals)
        {
            final V    o=((null == v) || (v.length() <= 0)) ? null : vsi.newInstance(v);
            if (null == o)
                continue;

            if (null == ret)
                ret = new ArrayList<V>(numVals);
            if (!ret.add(o))
                continue;    // debug breakpoint
        }

        return ret;
    }
    /**
     * @param <E> The type of elements
     * @param values {@link Collection} of values to check
     * @param name specified name
     * @param caseSensitive TRUE if comparison with {@link Object#toString()} should
     * be made case sensitive
     * @return found matching value - <code>null</code> if no match found (or null/empty name/values)
     */
    public static <E> E fromString (final Collection<? extends E> values, final String name, final boolean caseSensitive)
    {
        if ((null == name) || (name.length() <= 0)
         || (null == values) || (values.size() <= 0))
            return null;

        for (final E v : values)
        {
            final String    vName=(null == v) /* should not happen */ ? null : v.toString();
            if ((null == vName) || (vName.length() <= 0))
                continue;    // should not happen

            final int    vDiff=StringUtil.compareDataStrings(vName, name, caseSensitive);
            if (0 == vDiff)
                return v;
        }

        return null;    // no match found
    }

    public static final <E> List<E> reverse (List<E> org)
    {
        if ((org == null) || (org.size() <= 0))
            return org;

        Collections.reverse(org);
        return org;
    }

    private static final IgnoringCollection<?>    IGNORING_INSTANCE=new IgnoringCollection<Object>();
    @SuppressWarnings("unchecked")
    public static final <T> Collection<T> ignoringCollection ()
    {
        return (Collection<T>) IGNORING_INSTANCE;
    }
    /**
     * Makes sure that the destination {@link Collection} contains only the
     * objects from the source. This is done by removing objects that do not
     * appear in the source and adding those that do not appear in the destination.
     * @param src The source {@link Collection}
     * @param dst The destination {@link Collection}
     * @return TRUE if anything was changed in the destination
     * @see #calculateSyncActions(Collection, Collection)
     */
    public static final <E> boolean syncContents (Collection<? extends E> src, Collection<E> dst) {
        CollectionSyncResult<E>    result=calculateSyncActions(src, dst);
        return result.executeActions(dst);
    }
    /**
     * Calculates the necessary actions in order to ensure that the destination
     * {@link Collection} contains only the objects from the source. This is done
     * by removing objects that do not appear in the source and adding those that
     * do not appear in the destination.
     * @param src The source {@link Collection}
     * @param dst The destination {@link Collection}
     * @return The required actions to achieve synchronization
     * @see CollectionSyncResult#executeActions(Collection)
     */
    public static final <E> CollectionSyncResult<E> calculateSyncActions (Collection<? extends E> src, Collection<? extends E> dst) {
        if (size(src) <= 0)  {
            if (size(dst) <= 0)     {     // both empty - nothing to do
                return new CollectionSyncResult<E>(Collections.<E>emptyList(), Collections.<E>emptyList());
            } else {    // need to remove all entries from destination to make it empty as well
                return new CollectionSyncResult<E>(Collections.<E>emptyList(), new ArrayList<E>(dst));
            }
        }

        if (size(dst) <= 0) {    // need to add all values from source (and delete none)
            return new CollectionSyncResult<E>(new ArrayList<E>(src), Collections.<E>emptyList());
        }

        Collection<E> addValues=subtract(src, dst), delValues=subtract(dst, src);
        return new CollectionSyncResult<E>(addValues, delValues);
    }

    /**
     * @param c1 1st {@link Collection}
     * @param c2 2nd {@link Collection}
     * @return A {@link List} containing all the members from both parameters
     */
    public static <E> List<E> unionToList (Collection<? extends E> c1, Collection<? extends E> c2) {
        int    s1=size(c1), s2=size(c2);
        return unionToTarget(((s1 > 0) || (s2 > 0)) ? new ArrayList<E>(s1 + s2) : Collections.<E>emptyList(), c1, c2);
    }

    /**
     * @param c1 1st {@link Collection}
     * @param c2 2nd {@link Collection}
     * @return A {@link Set} containing all the members from both parameters
     */
    public static <E> Set<E> unionToSet (Collection<? extends E> c1, Collection<? extends E> c2) {
        int    s1=size(c1), s2=size(c2);
        return unionToTarget(((s1 > 0) || (s2 > 0)) ? new HashSet<E>(s1 + s2) : Collections.<E>emptySet(), c1, c2);
    }

    public static <E, C extends Collection<E>> C unionToTarget (C target, Collection<? extends E> c1, Collection<? extends E> c2) {
        if (target == null) {
            throw new IllegalArgumentException("No target");
        }

        if (size(c1) > 0) {
            target.addAll(c1);
        }

        if (size(c2) > 0) {
            target.addAll(c2);
        }

        return target;
    }
    /**
     * @param a Source {@link Collection}
     * @param b Items to remove
     * @return The result of removing all items from <code>a</code> that are mentioned in <code>b</code>
     */
    public static <E> List<E> subtract(Collection<? extends E> a, Collection<? extends E> b) {
        if (a == b) {
            return Collections.emptyList();
        }

        List<E> list = new ArrayList<E>(a);
        if (size(b) <= 0) {
            return list;
        }

        if (!list.removeAll(b)) {
            return list;    // debug breakpoint
        }

        return list;
    }

    /**
     * Compares the elements of 2 {@link List}-s at the given index range
     * using {@link AbstractComparator#compareObjects(Object, Object)}
     * @param l1 1st list
     * @param l2 2nd list
     * @return The index of the 1st non-matching index - negative if all
     * elements in the specified range match. <B>Note:</B> if one list is &quot;prefix&quot;
     * of the other within the compared range, and all elements in the shorter
     * list match the ones in the longer one, then the length of
     * the <U>shorter<U> list is returned as the first non-matching index
     * @see #findFirstNonMatchingIndex(List, List, Comparator)
     */
    public static final <T> int findFirstNonMatchingIndex (List<? extends T> l1, List<? extends T> l2) {
        return findFirstNonMatchingIndex(l1, l2, null);
    }

    /**
     * Compares the elements of 2 {@link List}-s at the given index range
     * @param l1 1st list
     * @param l2 2nd list
     * @param cmp The {@link Comparator} to use for equality - if
     * <code>null</code> then uses {@link AbstractComparator#compareObjects(Object, Object)}
     * @return The index of the 1st non-matching index - negative if all
     * elements in the specified range match. <B>Note:</B> if one list is &quot;prefix&quot;
     * of the other within the compared range, and all elements in the shorter
     * list match the ones in the longer one, then the length of
     * the <U>shorter<U> list is returned as the first non-matching index
     * @see #findFirstNonMatchingIndex(List, List, int, Comparator)
     */
    public static final <T> int findFirstNonMatchingIndex (List<? extends T> l1, List<? extends T> l2, Comparator<? super T> cmp) {
        int    n1=size(l1), n2=size(l2);
        return findFirstNonMatchingIndex(l1, l2, Math.max(n1, n2), cmp);
    }

    /**
     * Compares the elements of 2 {@link List}-s at the given index range
     * using {@link AbstractComparator#compareObjects(Object, Object)}
     * @param l1 1st list
     * @param l2 2nd list
     * @param numMembers Max. number of elements to compare
     * @return The index of the 1st non-matching index - negative if all
     * elements in the specified range match. <B>Note:</B> if one list is &quot;prefix&quot;
     * of the other within the compared range, and all elements in the shorter
     * list match the ones in the longer one, then the length of
     * the <U>shorter<U> list is returned as the first non-matching index
     * @see #findFirstNonMatchingIndex(List, List, int, Comparator)
     */
    public static final <T> int findFirstNonMatchingIndex (List<? extends T> l1, List<? extends T> l2, int numMembers) {
        return findFirstNonMatchingIndex(l1, l2, numMembers, null);
    }

    /**
     * Compares the elements of 2 {@link List}-s at the given index range
     * @param l1 1st list
     * @param l2 2nd list
     * @param numMembers Max. number of elements to compare
     * @param cmp The {@link Comparator} to use for equality - if
     * <code>null</code> then uses {@link AbstractComparator#compareObjects(Object, Object)}
     * @return The index of the 1st non-matching index - negative if all
     * elements in the specified range match. <B>Note:</B> if one list is &quot;prefix&quot;
     * of the other within the compared range, and all elements in the shorter
     * list match the ones in the longer one, then the length of
     * the <U>shorter<U> list is returned as the first non-matching index
     * @throws IllegalArgumentException if negative max. members specified
     * @throws IndexOutOfBoundsException if not enough members to compare
     */
    public static final <T> int findFirstNonMatchingIndex (List<? extends T> l1, List<? extends T> l2, int numMembers, Comparator<? super T> cmp) {
        if (numMembers < 0) {
            throw new IllegalArgumentException("findFirstNonMatchingIndex - negative max. members: " + numMembers);
        }
        int    n1=size(l1), n2=size(l2), maxSize=Math.max(n1, n2);
        // make sure requested number of members does not exceed max. available
        if (maxSize < numMembers) {
            throw new IndexOutOfBoundsException("findFirstNonMatchingIndex -"
                                              + " num. required (" + numMembers + ")"
                                              + " exceeds available (" + maxSize + ")");
        }

        int    maxCommon=Math.min(n1, n2), maxCompared=Math.min(maxCommon, numMembers);
        for (int    index=0; index < maxCompared; index++) {
            T    e1=l1.get(index), e2=l2.get(index);
            if (cmp == null) {
                if (!AbstractComparator.compareObjects(e1, e2)) {
                    return index;
                }
            } else {
                int    nRes=cmp.compare(e1, e2);
                if (nRes != 0) {
                    return index;
                }
            }
        }

        // check if compared what was required
        if (maxCompared == numMembers) {
            return (-1);    // all elements match
        }

        // at this point, either 1st or 2nd list is "shorter" than the other
        if (n1 < n2) {
            return n1;
        } else {
            return n2;
        }
    }
    /**
     * Randomly pick a list of items out of a population.
     * Original code: http://www.javamex.com/tutorials/random_numbers/random_sample.shtml
     * @param population The {@link List} of values to pick from
     * @param sampleSize Requested sample size
     * @param r A {@link Random}-izer to use for picking randomly
     * @return A {@link List} of samples of the requested size
     */
    public static <T> List<T> pickSample(final List<T> population, final int sampleSize, final Random r) {
        int samplesNeeded=sampleSize, nLeft=size(population);
        if (samplesNeeded > nLeft) {
            throw new IllegalArgumentException("pickSample(" + samplesNeeded + ") Requested more samples than in the population: " + nLeft);
        }

        List<T> res = new ArrayList<T>(samplesNeeded);
        int i = 0;
        while (samplesNeeded > 0) {
            int rand = r.nextInt(nLeft);
            if (rand < samplesNeeded) {
                res.add(population.get(i));
                samplesNeeded--;
            }

            nLeft--;
            if (nLeft <= 0) {
                nLeft = population.size();
            }

            i++;
            if (i >= population.size()) {
                i = 0;
            }
        }
        return res;
    }
}

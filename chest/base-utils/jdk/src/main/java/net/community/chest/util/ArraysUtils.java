/**
 *
 */
package net.community.chest.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 30, 2008 9:33:33 AM
 */
public final class ArraysUtils {
    private ArraysUtils ()
    {
        throw new UnsupportedOperationException("Construction N/A");
    }
    /**
     * Locates an element in an array of elements
     * @param <T> Type of array elements
     * @param v Element to be located - if null, then failure result returned
     * @param c The {@link Comparator} to use to check if elements are equal
     * to the sought value. If null, then only reference equality and the
     * {@link Object#equals(Object)} method are used.
     * @param startIndex Index to start looking for the element (inclusive)
     * @param vals The array values - <B>Note:</B> null elements are ignored
     * @return Index of element in array - negative if not found
     */
    @SafeVarargs
    public static final <T> int findElement (final T v, final Comparator<? super T> c, final int startIndex, final T ... vals)
    {
        final int    numVals=(null == vals) ? 0 : vals.length;
        if ((null == v) || (numVals <= 0) || (startIndex >= numVals))
            return (-1);

        for (int    vIndex=Math.max(0,startIndex); vIndex < numVals; vIndex++)
        {
            final T    cv=vals[vIndex];
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
     * Locates an element in an array of elements
     * @param <T> Type of array elements
     * @param v Element to be located - if null, then failure result returned
     * @param c The {@link Comparator} to use to check if elements are equal
     * to the sought value. If null, then only reference equality and the
     * {@link Object#equals(Object)} method are used.
     * @param vals The array values - <B>Note:</B> null elements are ignored
     * @return Index of element in array - negative if not found
     * @see #findElement(Object, Comparator, int, Object...) for searching
     * from offset index in array
     */
    @SafeVarargs
    public static final <T> int findElement (final T v, final Comparator<? super T> c, final T ... vals)
    {
        return findElement(v, c, 0, vals);
    }
    /**
     * Locates an element in an array of elements using only reference
     * equality and the {@link Object#equals(Object)} method
     * @param <T> Type of array elements
     * @param v Element to be located - if null, then failure result returned
     * @param startIndex Index to start looking for the element (inclusive)
     * @param vals The array values - <B>Note:</B> null elements are ignored
     * @return Index of element in array - negative if not found
     * @see #findElement(Object, Comparator, int, Object...) for searching
     * from offset index in array
     */
    @SafeVarargs
    public static final <T> int findElement (final T v, final int startIndex, final T ... vals)
    {
        return findElement(v, null, startIndex, vals);
    }
    /**
     * Locates an element in an array of elements using only reference
     * equality and the {@link Object#equals(Object)} method
     * @param <T> Type of array elements
     * @param v Element to be located - if null, then failure result returned
     * @param vals The array values - <B>Note:</B> null elements are ignored
     * @return Index of element in array - negative if not found
     * @see #findElement(Object, Comparator, int, Object...) for searching
     * from offset index in array
     */
    @SafeVarargs
    public static final <T> int findElement (final T v, final T ... vals)
    {
        return findElement(v, 0, vals);
    }
    /**
     * Looks for a string in an array of strings
     * @param s The {@link String} to look for - if null/empty then failure
     * result is returned
     * @param caseSensitive TRUE if comparison is to be made case sensitive
     * @param startIndex Index to start looking
     * @param strings Array of strings to look into - <B>Note:</B> null/empty
     * elements are ignored
     * @return String index in array - negative if not found
     */
    public static final int getStringIndex (final String s, final boolean caseSensitive, final int startIndex, final String ... strings)
    {
        if ((null == s) || (s.length() <= 0))
            return (-1);

        return findElement(s, caseSensitive ? null : String.CASE_INSENSITIVE_ORDER, startIndex, strings);
    }
    /**
     * Looks for a string in an array of strings
     * @param s The {@link String} to look for - if null/empty then failure
     * result is returned
     * @param caseSensitive TRUE if comparison is to be made case sensitive
     * @param strings Array of strings to look into - <B>Note:</B> null/empty
     * elements are ignored
     * @return String index in array - negative if not found
     */
    public static final int getStringIndex (final String s, final boolean caseSensitive, final String ... strings)
    {
        return getStringIndex(s, caseSensitive, 0, strings);
    }

    public static final byte[] cloneArray (byte ... ar)
    {
        return ((null == ar) || (ar.length <= 0)) ? ar : ar.clone();
    }

    public static final short[] cloneArray (short ... ar)
    {
        return ((null == ar) || (ar.length <= 0)) ? ar : ar.clone();
    }

    public static final int[] cloneArray (int ... ar)
    {
        return ((null == ar) || (ar.length <= 0)) ? ar : ar.clone();
    }

    public static final long[] cloneArray (long ... ar)
    {
        return ((null == ar) || (ar.length <= 0)) ? ar : ar.clone();
    }

    public static final float[] cloneArray (float ... ar)
    {
        return ((null == ar) || (ar.length <= 0)) ? ar : ar.clone();
    }

    public static final double[] cloneArray (double ... ar)
    {
        return ((null == ar) || (ar.length <= 0)) ? ar : ar.clone();
    }
    // returns same as input
    public static <T> T[] sort(T[] a, Comparator<? super T> c)
    {
        if ((a != null) && (a.length > 1) && (c != null))
            Arrays.sort(a, c);
        return a;
    }
    // returns same as input
    public static <T> T[] sort (T[] a, int fromIndex, int toIndex, Comparator<? super T> c)
    {
        if ((a != null) && (a.length > 1) && (c != null))
            Arrays.sort(a, fromIndex, toIndex, c);
        return a;
    }
    /**
     * @param <N> Type of component being compared
     * @param vals Values being compared
     * @return The value being {@link Comparable#compareTo(Object)} higher
     * than all other - excluding <code>null</code>-s. <B>Note:</B> may be
     * <code>null</code> if null/empty array or all values are null.
     */
    @SafeVarargs
    public static final <N extends Comparable<N>> N getMaxValue (final N ... vals)
    {
        if ((null == vals) || (vals.length <= 0))
            return null;

        return CollectionsUtils.getMaxValue(Arrays.asList(vals));
    }
    /**
     * @param <N> Type of component being compared
     * @param vals Values being compared
     * @return The value being {@link Comparable#compareTo(Object)} lower
     * than all other - excluding <code>null</code>-s. <B>Note:</B> may be
     * <code>null</code> if null/empty array or all values are null.
     */
    @SafeVarargs
    public static final <N extends Comparable<N>> N getMinValue (final N ... vals)
    {
        if ((null == vals) || (vals.length <= 0))
            return null;

        return CollectionsUtils.getMinValue(Arrays.asList(vals));
    }
    /**
     * @param <N> Type of component being compared
     * @param c The {@link Comparator} to use - ignored if <code>null</code>
     * @param vals Values being compared
     * @return The value being {@link Comparable#compareTo(Object)} higher
     * than all other - excluding <code>null</code>-s. <B>Note:</B> may be
     * <code>null</code> if null/empty array or all values are null.
     */
    @SafeVarargs
    public static final <N> N getMaxValue (final Comparator<? super N> c, final N ... vals)
    {
        if ((null == vals) || (vals.length <= 0) || (null == c))
            return null;

        return CollectionsUtils.getMaxValue(Arrays.asList(vals), c);
    }
    /**
     * @param <N> Type of component being compared
     * @param c The {@link Comparator} to use - ignored if <code>null</code>
     * @param vals Values being compared
     * @return The value being {@link Comparable#compareTo(Object)} lower
     * than all other - excluding <code>null</code>-s. <B>Note:</B> may be
     * <code>null</code> if null/empty array or all values are null.
     */
    @SafeVarargs
    public static final <N> N getMinValue (final Comparator<? super N> c, final N ... vals)
    {
        if ((null == vals) || (vals.length <= 0) || (null == c))
            return null;

        return CollectionsUtils.getMinValue(Arrays.asList(vals), c);
    }
    /**
     * @param <V> Type of value being instantiated
     * @param vsi The {@link ValueStringInstantiator} to be used
     * @param vals The values to be instantiated
     * @return A {@link List} of instantiated values. May be null/empty if
     * no values, or no instantiator or no non-null/empty strings/values
     * instantiated
     * @throws Exception If failed to instantiate a specific value
     */
    public static final <V> List<V> instantiateObjects (
            final ValueStringInstantiator<? extends V>    vsi, final String ... vals)
        throws Exception
    {
        return ((null == vals) || (vals.length <= 0)) ? null : CollectionsUtils.instantiateObjects(Arrays.asList(vals), vsi);
    }
    /**
     * Converts the strings array to an array of char array(s) - each char
     * array matches the string at the same index
     * @param strs strings array to be converted - may be null/zero-length.
     * <B>Note:</B> if a string element is null/empty then the matching
     * char array is set to <I>null</I>
     * @return An array of characters array - may be zero-length
     */
    public static final char[][] initCharsFromStrings (final String ... strs)
    {
        return initCharsFromStrings(((strs == null) || (strs.length <= 0)) ? null : Arrays.asList(strs));
    }
    /**
     * Converts the {@link Collection} of {@link String}ss to an array of char
     * array(s) - each char array matches the string at the same index
     * @param strs strings array to be converted - may be null/zero-length.
     * <B>Note:</B> if a string element is null/empty then the matching
     * char array is set to <I>null</I>
     * @return An array of characters array - may be zero-length
     */
    public static final char[][] initCharsFromStrings (final Collection<String> strs)
    {
        final int        numStrs=(null == strs) ? 0 : strs.size();
        final char[][]    resChars=new char[numStrs][];
        if (numStrs <= 0)
            return resChars;

        int    sIndex=0;
        for (final String    sVal : strs)
        {
            if ((null == sVal) || (sVal.length() <= 0))
                resChars[sIndex] = null;
            else
                resChars[sIndex] = sVal.toCharArray();
            sIndex++;
        }

        return resChars;
    }
    /**
     * Builds an array of strings from the array of char-array(s)
     * @param vals values array(s) - may be null/zero-length. <B>Note:</B>
     * if a null/zero-length array is found then a null string is set
     * @return array of strings - each string element matches the char-array
     * element at the same index
     */
    public static final String[] initStringsFromChars (final char[][] vals)
    {
        final int        numVals=(null == vals) ? 0 : vals.length;
        final String[]    strs=new String[numVals];
        for (int    vIndex=0; vIndex < numVals; vIndex++)
        {
            final char[]    v=vals[vIndex];
            if ((null == v) || (v.length <= 0))
                strs[vIndex] = null;
            else
                strs[vIndex] = new String(v);
        }

        return strs;
    }

    @SafeVarargs
    public static <T> boolean addAll (Collection<? super T> coll, T ... values) {
        if (length(values) <= 0) {
            return false;
        }

        boolean result=false;
        for (T v : values) {
            if (coll.add(v)) {
                result = true;
            }
        }

        return result;
    }

    public static <T> int length (T[] array)
    {
        return( array != null) ? array.length : 0;
    }

    public static int length (int[] array)
    {
        return (array != null) ? array.length : 0;
    }

    public static int length (long[] array)
    {
        return (array != null) ? array.length : 0;
    }

    public static int length (short[] array)
    {
        return (array != null) ? array.length : 0;
    }

    public static int length (double[] array)
    {
        return (array != null) ? array.length : 0;
    }

    public static int length (float[] array)
    {
        return (array != null) ? array.length : 0;
    }

    public static int length (byte[] array)
    {
        return (array != null) ? array.length : 0;
    }

    public static int length (char[] array)
    {
        return array != null ? array.length : 0;
    }

    public static int length (boolean[] array)
    {
        return array != null ? array.length : 0;
    }
}

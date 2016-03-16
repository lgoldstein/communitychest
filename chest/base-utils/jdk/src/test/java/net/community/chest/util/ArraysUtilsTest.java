/*
 *
 */
package net.community.chest.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import net.community.chest.AbstractTestSupport;

import org.junit.Test;

/**
 * <P>
 * Copyright as per GPLv2
 * </P>
 *
 * @author Lyor G.
 * @since Jun 13, 2012 8:09:18 AM
 */
public class ArraysUtilsTest extends AbstractTestSupport {
    public ArraysUtilsTest ()
    {
        super();
    }

    @Test
    public void testLengthObjectArray ()
    {
        String[] arr1 = new String[3];
        Object[] arr2 = null;
        Integer[] arr3 = new Integer[0];

        assertEquals("Mismatched same array length", arr1.length, ArraysUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ArraysUtils.length(arr2));
        assertEquals("Mismatched zero array length", arr3.length, ArraysUtils.length(arr3));
    }

    @Test
    public void testLengthIntArray ()
    {
        int[] arr1 = new int[3];
        int[] arr2 = null;
        int[] arr3 = new int[0];

        assertEquals("Mismatched same array length", arr1.length, ArraysUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ArraysUtils.length(arr2));
        assertEquals("Mismatched zero array length", arr3.length, ArraysUtils.length(arr3));
    }

    @Test
    public void testLengthLongArray ()
    {
        long[] arr1 = new long[3];
        long[] arr2 = null;
        long[] arr3 = new long[0];

        assertEquals("Mismatched same array length", arr1.length, ArraysUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ArraysUtils.length(arr2));
        assertEquals("Mismatched zero array length", arr3.length, ArraysUtils.length(arr3));
    }

    @Test
    public void testLengthShortArray ()
    {
        short[] arr1 = new short[3];
        short[] arr2 = null;
        short[] arr3 = new short[0];

        assertEquals("Mismatched same array length", arr1.length, ArraysUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ArraysUtils.length(arr2));
        assertEquals("Mismatched zero array length", arr3.length, ArraysUtils.length(arr3));
    }

    @Test
    public void testLengthDoubleArray ()
    {
        double[] arr1 = new double[3];
        double[] arr2 = null;
        double[] arr3 = new double[0];

        assertEquals("Mismatched same array length", arr1.length, ArraysUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ArraysUtils.length(arr2));
        assertEquals("Mismatched zero array length", arr3.length, ArraysUtils.length(arr3));
    }

    @Test
    public void testLengthFloatArray ()
    {
        float[] arr1 = new float[3];
        float[] arr2 = null;
        float[] arr3 = new float[0];

        assertEquals("Mismatched same array length", arr1.length, ArraysUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ArraysUtils.length(arr2));
        assertEquals("Mismatched zero array length", arr3.length, ArraysUtils.length(arr3));
    }

    @Test
    public void testLengthByteArray ()
    {
        byte[] arr1 = new byte[3];
        byte[] arr2 = null;
        byte[] arr3 = new byte[0];

        assertEquals("Mismatched same array length", arr1.length, ArraysUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ArraysUtils.length(arr2));
        assertEquals("Mismatched zero array length", arr3.length, ArraysUtils.length(arr3));
    }

    @Test
    public void testLengthCharArray ()
    {
        char[] arr1 = new char[3];
        char[] arr2 = null;
        char[] arr3 = new char[0];

        assertEquals("Mismatched same array length", arr1.length, ArraysUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ArraysUtils.length(arr2));
        assertEquals("Mismatched zero array length", arr3.length, ArraysUtils.length(arr3));
    }

    @Test
    public void testLengthBooleanArray ()
    {
        boolean[] arr1 = new boolean[3];
        boolean[] arr2 = null;
        boolean[] arr3 = new boolean[0];

        assertEquals("Mismatched same array length", arr1.length, ArraysUtils.length(arr1));
        assertEquals("Mismatched null array length", 0, ArraysUtils.length(arr2));
        assertEquals("Mismatched zero array length", arr3.length, ArraysUtils.length(arr3));
    }

    @Test
    public void testAddAllNullOrEmpty ()
    {
        Collection<String> coll = new LinkedList<String>();
        for (String[] values : new String[][] { null, new String[] {} })
        {
            String testValues = Arrays.toString(values);
            assertFalse("Unexpected change result for " + testValues, ArraysUtils.addAll(coll, values));
            assertTrue("Test collection changed for " + testValues, coll.isEmpty());
        }
    }

    @Test
    public void testAddAllToSet ()
    {
        Set<String> coll = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        String[] values = { getClass().getSimpleName(), "time=" + String.valueOf(System.nanoTime()), "rand=" + String.valueOf(Math.random()) };
        assertTrue("No changes in test collection", ArraysUtils.addAll(coll, values));
        assertEquals("Mismatched collection contents", values.length, coll.size());
        for (String v : values)
        {
            assertTrue("Value not added: " + v, coll.contains(v.toUpperCase()));
        }

        for (int index = 0; index < values.length; index++)
        {
            String v = values[index];
            values[index] = v.toLowerCase();
        }

        assertFalse("Unexpected secondary changes in test collection", ArraysUtils.addAll(coll, values));
        assertEquals("Mismatched collection contents after re-insert", values.length, coll.size());
        for (String v : values)
        {
            assertTrue("Value removed: " + v, coll.contains(v.toUpperCase()));
        }
    }
}

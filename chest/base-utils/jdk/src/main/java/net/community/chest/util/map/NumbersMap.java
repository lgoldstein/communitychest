package net.community.chest.util.map;

import java.io.Serializable;
import java.util.Comparator;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Abstract class that can be used for keys that are {@link Number}-s</P>
 *
 * @param <K> Generic key type
 * @param <V> Generic value type
 * @author Lyor G.
 * @since Oct 23, 2007 11:43:36 AM
 */
public abstract class NumbersMap<K extends Number,V> extends AbstractSortedMap<K,V> implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 7775811815988064564L;
    /**
     * current objects array - empty places are null
     */
    private V[]    _objVals    /* =null */;
    protected V[] getObjects ()
    {
        return _objVals;
    }

    protected void setObjects (V[] objVals)
    {
        _objVals = objVals;
    }
    /**
     * Grows the objects array to specified size
     * @param newSize new size of objects array
     * @return newly allocated objects array
     * @throws IllegalStateException if new size less-or-equals to current array size
     */
    protected V[] growObjects (final int newSize) throws IllegalStateException
    {
        final V[]    o=getObjects();
        final int    numObjects=(null == o) ? 0 : o.length, curSize=size();

        if (newSize <= numObjects)
            throw new IllegalStateException(getArgumentsExceptionLocation("growObjects", Integer.valueOf(numObjects), Integer.valueOf(newSize)) + " bad new size");

        _objVals = allocateValuesArray(newSize);
        if (curSize > 0)
            System.arraycopy(o, 0, _objVals, 0, curSize);
        return _objVals;
    }
    /**
     * Constructor
     * @param kClass class of key(s)
     * @param objClass class of object to be used (required for creating
     * arrays) - may NOT be null
     * @param initialSize allocated room initially - may be zero provided grow
     * size is non-zero. May NOT be negative
     * @param growSize how much to make room automatically if needed - may be
     * zero provided initial size is non-zero (in which case, any attempt to
     * grow beyond the limits will cause an exception - unless subsequent
     * call(s) to {@link #setGrowSize(int)} or {@link #grow(int)}
     * @throws IllegalArgumentException if no class, negative values or both zero
     */
    protected NumbersMap (final Class<K> kClass, final Class<V> objClass, final int initialSize, final int growSize) throws IllegalArgumentException
        /* IllegalStateException due to {@link #markEmptySpots()} - should never happen */
    {
        super(kClass, objClass);

        if (initialSize < 0)
            throw new IllegalArgumentException(getConstructorExceptionLocation() + " initial size=" + initialSize + " not allowed to be negative");

        if (initialSize > 0)
        {
            _objVals = allocateValuesArray(initialSize);
        }
        else    // zero initial size => make sure grow size is non-zero
        {
            if (0 == growSize)
                throw new IllegalArgumentException(getConstructorExceptionLocation() + " must specify either initial size or grow size as non-zero");
        }

        setGrowSize(growSize);
    }
    /**
     * Marks the keys/objects array(s) as "empty" (<I>null</I> for objects and
     * {@link Integer#MAX_VALUE} for keys) starting from specified index
     * @param fromIndex index to start from (inclusive)
     * @throws IllegalStateException if keys/objects array(s) length(s) mismatch
     * @throws IllegalArgumentException if index negative or greater than length
     * of keys/objects array(s)
     */
    protected abstract void markEmptySpots (final int fromIndex)
        throws IllegalStateException, IllegalArgumentException;

    private int    _growSize    /* =0 */;
    /**
     * @return currently set grow size in case need to extend the map. If ZERO
     * then attempting to {@link #put(Object, Object)} a new entry will throw
     * an {@link IllegalStateException}
     * @see #setGrowSize(int)
     */
    public int getGrowSize ()
    {
        return _growSize;
    }
    /**
     * @param growSize how much to grow every time the current entries
     * array is full and need to grow it. If ZERO then attempting to
     * put a new entry will throw an {@link IllegalStateException}
     * @throws IllegalArgumentException if negative size
     */
    public void setGrowSize (final int growSize) throws IllegalArgumentException
    {
        if (growSize < 0)
            throw new IllegalArgumentException(getExceptionLocation("setGrowSize") + "(" + growSize + ") not allowed negative values");

        _growSize = growSize;
    }
    /**
     * Useful string for {@link #grow(int)} exceptions text
     * @param growSize how much more room to make
     * @return {@link #getExceptionLocation(String)} + "(" + growSize + ")"
     */
    protected String getGrowExceptionLocation (int growSize)
    {
        return getExceptionLocation("grow") + "(" + growSize + ")";
    }

    private int    _size        /* =0 */;
    protected int updateSize (final int updVal)
    {
        _size += updVal;
        return _size;
    }
    /*
     * @see java.util.Map#size()
     */
    @Override
    public int size ()
    {
        return _size;
    }
    /**
     * Marks the all keys/objects array(s) from current {@link #size()} as "empty"
     * @throws IllegalStateException if keys/objects array(s) length(s) mismatch
     * or {@link #size()} negative or greater than length of keys/objects array(s)
     * (which should not happen...)
     */
    protected void markEmptySpots () throws IllegalStateException
    {
        final int    nSize=size();
        try
        {
            markEmptySpots(nSize);
        }
        // thrown if size > keys/objects array(s) length(s) - which should not happen
        catch(IllegalArgumentException e)
        {
            throw new IllegalStateException(getExceptionLocation("markEmptySpots") + "() internal size (" + nSize + ") mismatch: " + e.getMessage());
        }
    }
    /*
     * @see java.util.Map#clear()
     * @throws IllegalStateException if mismatched internal arrays lengths
     * (which should never happen)
     */
    @Override
    public void clear () throws IllegalStateException
    {
        markEmptySpots(0);
        _size = 0;
    }
    /**
     * Attempts to make more room according to the value specified in parameter
     * @param growSize how much more room to make - may be zero (but not negative)
     * @throws IllegalArgumentException if negative grow size requested
     * @throws IllegalStateException if unable to replicate internal objects array
     */
    public abstract void grow (final int growSize) throws IllegalArgumentException, IllegalStateException;
    /**
     * Attempts to make more room according to the value last specified in
     * the constructor or via {@link #setGrowSize(int)}.
     * @throws IllegalStateException if built-in grow size is non-positive
     */
    public void grow () throws IllegalStateException
    {
        final int    gSize=getGrowSize();
        if (gSize <= 0)
            throw new IllegalStateException(getExceptionLocation("grow") + "(" + gSize + ") zero/negative auto-grow size");

        grow(gSize);
    }
    /**
     * Compares 2 {@link java.util.Map.Entry} based on their {@link Number} key via the
     * {@link #compareNumbers(Number, Number)} abstract method
     *
     * @author Lyor G.
     * @since Jul 19, 2010 1:28:01 PM
     */
    protected static abstract class NumberEntriesComparator
            implements Comparator<Entry<? extends Number,?>>, Serializable {
        /**
         *
         */
        private static final long serialVersionUID = -6766312113506346860L;
        protected NumberEntriesComparator ()
        {
            super();
        }

        protected abstract int compareNumbers (Number n1, Number n2);
        /*
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare (final Entry<? extends Number,?> o1, final Entry<? extends Number,?> o2)
        {
            final Number    n1=(null == o1) ? null : o1.getKey(),
                            n2=(null == o2) ? null : o2.getKey();
            return compareNumbers(n1, n2);
        }
    }

    public abstract Comparator<Entry<? extends Number,?>> getEntryComparator ();
}

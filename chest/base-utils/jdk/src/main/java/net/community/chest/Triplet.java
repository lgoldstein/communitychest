/*
 *
 */
package net.community.chest;

import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V1> The 1st value type
 * @param <V2> The 2nd value type
 * @param <V3> The 3rd value type
 * @author Lyor G.
 * @since Dec 25, 2008 5:07:50 PM
 */
public class Triplet<V1,V2,V3> implements Cloneable {
    private V1 _v1;
    private V2 _v2;
    private V3 _v3;

    public Triplet (V1 v1, V2 v2, V3 v3)
    {
        _v1 = v1;
        _v2 = v2;
        _v3 = v3;
    }

    public Triplet (V1 v1, V2 v2)
    {
        this(v1, v2, null);
    }

    public Triplet (V1 v1)
    {
        this(v1, null);
    }

    public Triplet ()
    {
        this(null);
    }

    public V1 getV1 ()
    {
        return _v1;
    }

    public void setV1 (V1 v1)
    {
        _v1 = v1;
    }

    public V2 getV2 ()
    {
        return _v2;
    }

    public void setV2 (V2 v2)
    {
        _v2 = v2;
    }

    public V3 getV3 ()
    {
        return _v3;
    }

    public void setV3 (V3 v3)
    {
        _v3 = v3;
    }
    /**
     * @return TRUE if all values are <code>null</code>
     */
    public boolean isEmpty ()
    {
        if ((null == getV1()) && (null == getV2()) && (null == getV3()))
            return true;

        return false;
    }
    /**
     * Sets all values to <code>null</code>
     * @see #isEmpty()
     */
    public void clear ()
    {
        setV1(null);
        setV2(null);
        setV3(null);
    }
    /*
     * @see java.lang.Object#clone()
     */
    @SuppressWarnings("unchecked")
    @Override
    @CoVariantReturn
    public Triplet<V1,V2,V3> clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    public boolean equals (Object v1, Object v2, Object v3)
    {
        final Object[]    oa={
                getV1(), v1,
                getV2(), v2,
                getV3(), v3
            };
        for (int    oIndex=0; oIndex < oa.length; oIndex += 2)
        {
            final Object    tv=oa[oIndex], ov=oa[oIndex+1];
            if (!AbstractComparator.compareObjects(tv, ov))
                return false;
        }

        return true;
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        final Class<?>    oc=(obj == null) ? null : obj.getClass();
        if (oc != getClass())
            return false;
        if (this == obj)
            return true;

        final Triplet<?,?,?>    other=(Triplet<?,?,?>) obj;
        return equals(other.getV1(), other.getV2(), other.getV3());
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return ClassUtil.getObjectHashCode(getV1())
             + ClassUtil.getObjectHashCode(getV2())
             + ClassUtil.getObjectHashCode(getV3())
             ;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return "V1=" + getV1()
             + "/V2=" + getV2()
             + "/V3=" + getV3()
             ;
    }
}

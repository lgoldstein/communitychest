package net.community.chest.net.address;

import java.net.InetAddress;

import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 * @param <A> The compared {@link InetAddress} type
 * @author Lyor G.
 * @since Jul 4, 2007 9:00:09 AM
 */
public class InetAddressComparator<A extends InetAddress> extends AbstractComparator<A> {
    /**
     *
     */
    private static final long serialVersionUID = -7596332430888627626L;
    public InetAddressComparator (Class<A> aClass, boolean ascending)
    {
        super(aClass, !ascending);
    }
    /**
     * Compares 2 addresses represented as byte-arrays in network order (i.e., most important first)
     * @param a1 1st address to be compared
     * @param a2 2nd address to be compared
     * @return negative if 1st address is &quot;smaller&quot;, positive if the 2nd, zero
     * if equal
     */
    public static final int compareAddresses (final byte[]    a1, final byte[] a2)
    {
        if (a1 == a2)    // obviously
            return 0;

        final int    l1=(null == a1) ? 0 : a1.length,
                    l2=(null == a2) ? 0 : a2.length,
                    l=Math.min(l1, l2);
        for (int    aIndex=0; aIndex < l; aIndex++)
        {
            final byte    v1=a1[aIndex], v2=a2[aIndex];
            final int    vDiff=v1-v2;
            if (vDiff != 0)
                return vDiff;
        }

        // at this point we know they have the same "prefix" -> shorter comes first
        return (l1 - l2);
    }
    /**
     * Compares 2 {@link InetAddress}-es by using their {@link InetAddress#getAddress()}
     * value(s). The values are compared along their longest common length. If same prefix,
     * then the shortest address comes first
     * @param o1 1st address to be compared
     * @param o2 2nd address to be compared
     * @return negative if 1st address is &quot;smaller&quot;, positive if the 2nd, zero
     * if equal
     */
    public static final int compareAddresses (final InetAddress o1, final InetAddress o2)
    {
        if (o1 == o2)    // obviously
            return 0;

        // null comes last
        if (null == o1)
            return (null == o2) ? 0 : (+1);
        else if (null == o2)
            return (-1);

        if (o1.equals(o2))
            return 0;

        return compareAddresses(o1.getAddress(), o2.getAddress());
    }
    /*
     * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (A o1, A o2)
    {
        return compareAddresses(o1, o2);
    }
    /**
     * A default instance of a {@link InetAddressComparator}
     */
    public static final InetAddressComparator<InetAddress>    ASCENDING=
                new InetAddressComparator<InetAddress>(InetAddress.class, true),
                                                            DESCENDING=
                new InetAddressComparator<InetAddress>(InetAddress.class, false);
}


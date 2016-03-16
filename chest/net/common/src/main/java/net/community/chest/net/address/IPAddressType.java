/*
 *
 */
package net.community.chest.net.address;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 17, 2010 9:04:01 AM
 */
public enum IPAddressType {
    IPv4(Inet4Address.class, IPv4Address.ADDRESS_NUM_BITS, IPv4Address.ADDRESS_LENGTH, IPv4Address.ADDRESS_SEP_CHAR),
    IPv6(Inet6Address.class, IPv6Address.ADDRESS_NUM_BITS, IPv6Address.ADDRESS_LENGTH, IPv6Address.ADDRESS_SEP_CHAR);

    private final Class<? extends InetAddress>    _inetAddressType;
    public Class<? extends InetAddress> getInetAddressType ()
    {
        return _inetAddressType;
    }

    private final int    _numBits, _numBytes;
    public final int getNumBits ()
    {
        return _numBits;
    }

    public final int getNumBytes ()
    {
        return _numBytes;
    }

    private final char    _sepChar;
    public final char getSeparatorChar ()
    {
        return _sepChar;
    }

    public List<InetAddress> filterByFamily (final InetAddress ... addrs)
    {
        if ((addrs == null) || (addrs.length <= 0))
            return Collections.emptyList();
        else
            return filterByFamily(Arrays.asList(addrs));
    }

    public List<InetAddress> filterByFamily (final Collection<? extends InetAddress> addrs)
    {
        if ((addrs == null) || addrs.isEmpty())
            return Collections.emptyList();

        List<InetAddress>    result=null;
        for (final InetAddress a : addrs)
        {
            final IPAddressType    family=fromInetAddress(a);
            if (!this.equals(family))
                continue;

            if (result == null)
                result = new ArrayList<InetAddress>(addrs.size());
            result.add(a);
        }

        return result;
    }

    IPAddressType (final Class<? extends InetAddress>    inetType,
                   final int                             numBits,
                   final int                             numBytes,
                   final char                             sepChar)
    {
        _inetAddressType = inetType;
        _numBits = numBits;
        _numBytes = numBytes;
        _sepChar = sepChar;
    }

    public static final List<IPAddressType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final IPAddressType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final IPAddressType fromNumBytes (final int numBytes)
    {
        if (numBytes <= 0)
            return null;

        for (final IPAddressType v : VALUES)
        {
            if ((v != null) && (v.getNumBytes() == numBytes))
                return v;
        }

        return null;
    }

    public static final IPAddressType fromNumBytes (final byte ... bytes)
    {
        return (bytes == null) ? null : fromNumBytes(bytes.length);
    }

    public static final IPAddressType fromNumBits (final int numBits)
    {
        if (numBits <= 0)
            return null;

        for (final IPAddressType v : VALUES)
        {
            if ((v != null) && (v.getNumBits() == numBits))
                return v;
        }

        return null;
    }
    /**
     * @param clazz A {@link Class} assumed to be derived from {@link InetAddress}
     * @return The matching {@link IPAddressType} constant - or <code>null</code>
     * if no match found or class is not derived from {@link InetAddress}
     */
    public static final IPAddressType fromInetAddressType (final Class<?> clazz)
    {
        if ((clazz == null) || (!InetAddress.class.isAssignableFrom(clazz)))
            return null;

        for (final IPAddressType    family : VALUES)
        {
            if (family.getInetAddressType().isAssignableFrom(clazz))
                return family;
        }

        return null;    // no match found
    }
    /**
     * @param addr An {@link InetAddress} instance
     * @return The matching {@link IPAddressType} constant - or <code>null</code>
     * if <code>null</code> instance parameter
     */
    public static final IPAddressType fromInetAddress (final InetAddress addr)
    {
        return (addr == null) ? null : fromInetAddressType(addr.getClass());
    }
}

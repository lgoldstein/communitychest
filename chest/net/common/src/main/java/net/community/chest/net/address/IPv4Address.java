package net.community.chest.net.address;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.Inet4Address;

import net.community.chest.CoVariantReturn;

/**
 * Copyright 2007 as per GPLv2
 *
 * Represents an IPv4 address
 *
 * @author Lyor G.
 * @since Jun 28, 2007 2:16:06 PM
 */
public class IPv4Address extends AbstractIPAddress {
    /**
     *
     */
    private static final long serialVersionUID = -5191191811609263069L;
    /**
     * Default (empty) constructor
     */
    public IPv4Address ()
    {
        super(IPAddressType.IPv4);
    }
    /**
     * Length of IPv4 address (in bytes)
     */
    public static final int    ADDRESS_LENGTH=4;
    /**
     * Number of bits for representing an IPv4 address
     */
    public static final int ADDRESS_NUM_BITS=ADDRESS_LENGTH * Byte.SIZE;
    // NOTE: must have at least ADDRESS_LENGTH available length
    public static final byte[] fromLongValue (final long aVal, final byte[] av, final int off)
    {
        long    curVal=aVal;
        for (int  aIndex=off + ADDRESS_LENGTH;
             aIndex > off;
             aIndex--, curVal >>= Byte.SIZE)
            av[aIndex - 1] = (byte) (curVal & 0x00FF);
        return av;
    }

    public static final byte[] fromLongValue (final long aVal, final byte[] av)
    {
        return fromLongValue(aVal, av, 0);
    }
    // NOTE: must have at least IPv4_ADDRESS_LENGTH available length
    public static final byte[] fromLongValue (final long aVal)
    {
        return fromLongValue(aVal, new byte[ADDRESS_LENGTH]);
    }
    /**
     * Initializes the current contents from the 32-bit value
     * @param aVal 32-bit value to initialize from
     */
    public void fromLong (long aVal)
    {
        fromLongValue(aVal, getAddress());
    }
    /**
     * @param aVal initial 32-bit value
     * @see #fromLong(long)
     */
    public IPv4Address (long aVal)
    {
        super(IPAddressType.IPv4);
        fromLong(aVal);
    }
    /*
     * @see net.community.chest.net.address.AbstractIPAddress#clone()
     */
    @Override
    @CoVariantReturn
    public IPv4Address clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    /**
     * Automatic mask applied to all <code>long</long> values
     * to ensure only low 32 bits are used
     */
    public static final long    IPv4ADRESS_VALUE_MASK=0x00FFFFFFFFL;
    public static final long toLong (final byte ... av) throws NumberFormatException
    {
        final int    avLen=(null == av) ? 0 : av.length;
        if (avLen < ADDRESS_LENGTH)
            throw new NumberFormatException("Invalid number of IP components: got=" + avLen + "/expected=" + ADDRESS_LENGTH);

        long    longVal=0L;
        for (int    aIndex=0; aIndex < ADDRESS_LENGTH; aIndex++)
        {
            final int    cVal=av[aIndex] & 0x00FF;
            longVal <<= Byte.SIZE;
            longVal |= cVal;
        }

        return longVal & IPv4ADRESS_VALUE_MASK;
    }
    /**
     * @return current content as a 32-bit value
     */
    public long toLong ()
    {
        return toLong(getAddress());
    }

    /**
     * Default separator used between address components
     */
    public static final char ADDRESS_SEP_CHAR='.';
    public static final <A extends Appendable> A appendAddress (
            final A sb, final byte[] a, final int offset, final int len)
        throws IOException
    {
        if (sb == null)
            throw new EOFException("No " + Appendable.class.getSimpleName() + " instance provided");

        final int    aLen=(null == a) ? 0 : a.length, maxOffset=offset + ADDRESS_LENGTH;
        if ((aLen < ADDRESS_LENGTH)
         || (offset < 0)
         || (len < ADDRESS_LENGTH)
         || (maxOffset > aLen)
         || ((offset + len) > aLen))
            throw new StreamCorruptedException("appendAddress() bad/illegal data");

        for (int    curOffset=offset; curOffset < maxOffset; curOffset++)
        {
            sb.append(String.valueOf(a[curOffset] & 0x00FF));
            if (curOffset < (maxOffset - 1))
                sb.append(ADDRESS_SEP_CHAR);
        }

        return sb;
    }

    public static final <A extends Appendable> A appendAddress (final A sb, final byte ... a) throws IOException
    {
        return appendAddress(sb, a, 0, (null == a) ? 0 : a.length);
    }
    /**
     * @param a array to be converted to dot notation.
     * @param offset offset in array to start conversion
     * @param len number of bytes available for conversion. <B>Note:</B> if
     * greater than {@link #ADDRESS_LENGTH}, then only the first bytes
     * are converted
     * @return string representation (null if problem(s) encountered)
     */
    public static final String toString (final byte[] a, final int offset, final int len)
    {
        if (len <= 0)
            return null;

        try
        {
            return appendAddress(new StringBuilder(ADDRESS_LENGTH * 4 + 2), a, offset, len).toString();
        }
        catch(IOException e)    // can happen if bad arguments
        {
            throw new RuntimeException(e);
        }
    }
    /**
     * @param a array to be converted to dot notation. <B>Note:</B> if length
     * greater than {@link #ADDRESS_LENGTH} then only the first bytes
     * are converted
     * @return string representation (null if problem(s) encountered)
     */
    public static final String toString (final byte ... a)
    {
        return toString(a, 0, (null == a) ? 0 : a.length);
    }

    public static final <A extends Appendable> A appendAddress (final A sb, final long a) throws IOException
    {
        if (null == sb)
            throw new IOException("No " + Appendable.class.getSimpleName() + " instance provided");

        for (int    aIndex=0, sSize=24; aIndex < ADDRESS_LENGTH; aIndex++, sSize -= Byte.SIZE)
        {
            final long    v=(a >> sSize) & 0x00FF;
            if (aIndex > 0)
                sb.append(ADDRESS_SEP_CHAR);
            sb.append(String.valueOf(v));
        }

        return sb;
    }

    public static final String toString (final long a)
    {
        try
        {
            return appendAddress(new StringBuilder(ADDRESS_LENGTH * 4), a).toString();
        }
        catch(IOException e)    // unexpected
        {
            throw new RuntimeException(e);
        }
    }
    /*
     * @see net.community.chest.net.address.AbstractIPAddress#toString()
     */
    @Override
    public String toString ()
    {
        return toString(getAddress());
    }
    /**
     * @param sv string in "a.b.c.d" notation
     * @return 32-bit integer value represented by the notation
     * @throws NumberFormatException bad/illegal format in string
     */
    public static final long toLong (final String sv) throws NumberFormatException
    {
        final int    svLen=(null == sv) ? 0 : sv.length();
        int            numElements=0;
        long        longVal=0L;
        for (int curPos=0; curPos < svLen; )
        {
            final int        nextPos=sv.indexOf(ADDRESS_SEP_CHAR, curPos);
            final boolean    done=((nextPos <= curPos) || (nextPos >= svLen));
            final String    dotStr=done ? sv.substring(curPos) : sv.substring(curPos, nextPos);
            final int        dotVal=Integer.parseInt(dotStr);
            if ((dotVal < 0) || (dotVal > 255))
                throw new NumberFormatException("Address component (" + dotStr + ") not in range for address=" + sv);

            longVal <<= Byte.SIZE;
            longVal |= dotVal & 0x00FF;
            numElements++;

            if (done)
                break;

            if ((curPos=nextPos + 1) >= svLen)
                throw new NumberFormatException("Bad/Illegal dot position after component=" + dotStr + " in address=" + sv);
        }

        if (numElements != ADDRESS_LENGTH)
            throw new NumberFormatException("Bad/Illegal IPv4 string format: " + sv);

        return longVal & IPv4ADRESS_VALUE_MASK;
    }
    /**
     * Initializes contents of address bytes array with the values from the
     * parsed string.
     * @param sv string to be converted - may NOT be null/empty, and MUST be a
     * valid dot notation of an IP address
     * @param av bytes array into which to place the result - may NOT be null
     * @param offset offset in bytes array to place the result
     * @param len number of available bytes - if more than {@link #ADDRESS_LENGTH}
     * bytes available, then only the first ones are used
     * @return number of bytes used (should be <U>exactly</U> IPv4_ADDRESS_LENGTH)
     * @throws NumberFormatException bad/illegal format in string
     */
    public static final int fromString (final String sv, final byte[] av, final int offset, final int len)
                                throws NumberFormatException
    {
        final int    svLen=(null == sv) ? 0 : sv.length(),
                    avLen=(null == av) ? 0 : av.length;
        if ((svLen <= 0) || (len < ADDRESS_LENGTH) || (offset < 0) || ((offset + len) > avLen))
            throw new NumberFormatException("Bad/Illegal string/address bytes array specification");

        int    curLen=0, curPos=0;
        for (int    curOffset=offset; curLen < len; curOffset++)
        {
            final int        nextPos=sv.indexOf(ADDRESS_SEP_CHAR, curPos);
            final boolean    done=((nextPos <= curPos) || (nextPos >= svLen));
            final String    dotStr=done ? sv.substring(curPos) : sv.substring(curPos, nextPos);
            final char        ch0=(dotStr.length() <= 0) ? '?' : dotStr.charAt(0);
            if ((ch0 < '0') || (ch0 > '9'))
                throw new NumberFormatException("Address component (" + dotStr + ") does not start with digit in address=" + sv);

            final short        dotVal=Short.parseShort(dotStr);
            if ((dotVal < 0) || (dotVal > 255))
                throw new NumberFormatException("Address component (" + dotStr + ") not in range for address=" + sv);

            av[curOffset] = (byte) dotVal;
            curLen++;

            if (done)
            {
                curPos = svLen;
                break;
            }

            if ((curPos=nextPos + 1) >= svLen)
                throw new NumberFormatException("Bad/Illegal dot position after component=" + dotStr + " in address=" + sv);
        }

        // make sure exhausted ALL dotted values
        if (curPos < svLen)
            throw new NumberFormatException("Not all input string exhausted for addr=" + sv);

        if (curLen != ADDRESS_LENGTH)
            throw new NumberFormatException("Bad/incomplete address (len=" + curLen + "): " + sv);

        return curLen;
    }
    /**
     * Initializes contents of address bytes array with the values from the
     * parsed string.
     * @param sv string to be converted - may NOT be null/empty, and MUST be a
     * valid dot notation of an IP address
     * @param av bytes array into which to place the result - may NOT be null,
     * and if more than {@link #ADDRESS_LENGTH} bytes available, then
     * only the first ones are used
     * @return number of bytes used (should be <U>exactly</U> IPv4_ADDRESS_LENGTH)
     * @throws NumberFormatException bad/illegal format in string
     * @throws IllegalArgumentException bad/illegal string/bytes array
     */
    public static final int fromString (String sv, byte[] av)
        throws NumberFormatException, IllegalArgumentException
    {
        return fromString(sv, av, 0, (null == av) ? 0 : av.length);
    }
    /**
     * @param cs {@link CharSequence} to be checked if IPv4 address
     * @param startPos start position (inclusive) to check
     * @param len number of characters to check
     * @return TRUE if {@link CharSequence} represents an IPv4 address (within
     * the specified range)
     */
    public static final boolean isIPv4Address (final CharSequence cs, final int startPos, final int len)
    {
        final int    maxPos=startPos + len;
        if ((null == cs) || (startPos < 0)
         || (len < 7) /* min. address is "0.0.0.0" */ || (maxPos > cs.length()))
            return false;

        int    dotCount=0, lastDotPos=(-1);
        for (int    curPos=startPos; curPos < maxPos; curPos++)
        {
            final char    c=cs.charAt(curPos);
            if (ADDRESS_SEP_CHAR == c)
            {
                dotCount++;
                // cannot have more than 3 dots
                if (dotCount > 3)
                    return false;

                final int    cLen;
                if (lastDotPos >= startPos)
                {
                    cLen = curPos - lastDotPos;
                }
                else    // first time we encounter dot
                {
                    cLen = 1 + (curPos - startPos);
                }
                // at least one digit and no more than 3
                if ((cLen <= 1) || (cLen > 4))
                    return false;

                // make sure the value between 2 successive dots is a valid IP component
                final CharSequence    ns=cs.subSequence((curPos - cLen) + 1, curPos);
                final int            nVal=Integer.parseInt(ns.toString());
                if ((nVal < 0) || (nVal > 255))
                    return false;

                lastDotPos = curPos;
            }
            else if ((c < '0') || (c > '9'))
                return false;
        }

        return true;
    }
    /**
     * @param cs {@link CharSequence} to be checked if IPv4 address
     * @return TRUE if (entire) {@link CharSequence} represents an IPv4 address
     * @see #isIPv4Address(CharSequence, int, int)
     */
    public static final boolean isIPv4Address (final CharSequence cs)
    {
        return (null == cs) ? false : isIPv4Address(cs, 0, cs.length());
    }

    public static final String    DEFAULT_LOOPBACK_ADDRESS="127.0.0.1";
    public static final Long    DEFAULT_LOOPBACK_IP=Long.valueOf(toLong(DEFAULT_LOOPBACK_ADDRESS));
    // see http://en.wikipedia.org/wiki/Loopback - any 127.x.x.x is a loopback
    public static final boolean isLoopbackAddress (final CharSequence addr)
    {
        if (!isIPv4Address(addr))
            return false;    // debug breakpoint

        for (int    cIndex=1; cIndex < addr.length(); cIndex++)
        {
            // find 1st component
            if (addr.charAt(cIndex) != ADDRESS_SEP_CHAR)
                continue;

            final CharSequence    vSeq=addr.subSequence(0, cIndex);
            final int            vVal=Integer.parseInt(vSeq.toString());
            if (vVal == 127)
                return true;    // debug breakpoint

            break;
        }

        return false;    // debug breakpoint
    }
    // see http://en.wikipedia.org/wiki/Loopback - any 127.x.x.x is a loopback
    public static final boolean isLoopbackAddress (final long addr)
    {
        if (((addr >> 24) & 0x00FF) != 127)
            return false;    // debug breakpoint
        else
            return true;
    }
    // see http://en.wikipedia.org/wiki/Loopback - any 127.x.x.x is a loopback
    public static final boolean isLoopbackAddress (final byte[] addr, final int offset, final int len)
    {
        if ((null == addr) || (len != ADDRESS_LENGTH) || (offset < 0) || ((offset+len) > addr.length))
            return false;    // debug breakpoint
        if (addr[offset] != 127)
            return false;    // debug breakpoint

        return true;
    }
    public static final boolean isLoopbackAddress (final byte... addr)
    {
        return isLoopbackAddress(addr, 0, (addr == null) ? 0 : addr.length);
    }
    /*
     * @see net.community.chest.net.address.AbstractIPAddress#isLoopbackAddress()
     */
    @Override
    public boolean isLoopbackAddress ()
    {
        return isLoopbackAddress(getAddress());
    }
    /*
     * @see net.community.chest.net.address.AbstractIPAddress#fromString(java.lang.String)
     */
    @Override
    public void fromString (String s) throws NumberFormatException
    {
        final int    nRes=fromString(s, getAddress());
        if (nRes != ADDRESS_LENGTH)    // should not happen
            throw new NumberFormatException("Bad/Illegal conversion result length: " + nRes);
    }
    /**
     * Initializes contents from specified string
     * @param s string to be used - may NOT be null/empty, and MUST be a valid
     * dot notation of an IP address
     * @throws NumberFormatException bad/illegal format in string
     * @see #fromString(String)
     */
    public IPv4Address (String s) throws NumberFormatException
    {
        super(IPAddressType.IPv4);
        fromString(s);
    }
    /**
     * Initializes using the given array data
     * @param av data array to copy from
     * @param offset offset in array to start copying
     * @param len number of available bytes - if more than {@link #ADDRESS_LENGTH}
     * available, then only the first ones are used
     * @throws IllegalArgumentException if bad/illegal array
     * @see #fromBytes(byte[], int, int)
     */
    public IPv4Address (final byte[] av, final int offset, final int len) throws IllegalArgumentException
    {
        super(IPAddressType.IPv4);
        fromBytes(av, offset, len);
    }
    /**
     * Initializes using the given array data
     * @param av data array - if more than {@link #ADDRESS_LENGTH} bytes available, then
     * only the first ones are used
     * @throws IllegalArgumentException if bad/illegal array
     * @see #fromBytes(byte[])
     */
    public IPv4Address (final byte ... av) throws IllegalArgumentException
    {
        this(av, 0, (av == null) ? 0 : av.length);
    }
    /**
     * @param a The {@link Inet4Address} instance to be used for initialization
     * @throws IllegalArgumentException If null/bad address value
     */
    public IPv4Address (final Inet4Address a) throws IllegalArgumentException
    {
        this((a == null) ? null : a.getAddress());
    }
    /**
     * Copies from original address
     * @param a object to copy from
     * @throws IllegalArgumentException if null/illegal source object
     */
    public void fromAddress (IPv4Address a) throws IllegalArgumentException
    {
        final byte[]    aVal=(null == a) ? null : a.getAddress(), curVal=getAddress();
        if ((null == aVal) || (aVal.length != curVal.length))
            throw new IllegalArgumentException("Bad/illegal initial object to copy from");

        System.arraycopy(aVal, 0, curVal, 0, aVal.length);
    }
    /**
     * Copy constructor
     * @param a object to copy from
     * @throws IllegalArgumentException if null/illegal source object
     * @see #fromAddress(IPv4Address)
     */
    public IPv4Address (IPv4Address a) throws IllegalArgumentException
    {
        super(IPAddressType.IPv4);
        fromAddress(a);
    }
    /**
     * Generates a mask with the given number of high bits set - e.g., if
     * <code>numBits == 24</code> then resulting address is 255.255.255.0
     * @param numBits number of bits to be set
     * @throws IllegalArgumentException if number of bits not within 0-32
     * range (<B>Note:</B> a value of zero causes the 0.0.0.0 mask to be
     * generated
     */
    public void fromMask (byte numBits) throws IllegalArgumentException
    {
        if ((numBits < 0) || (numBits > ADDRESS_NUM_BITS))
            throw new IllegalArgumentException("Illegal number of mask bits: " + numBits);

        if (numBits > 0)
        {
            int    aVal=~0;
            // shift left (adding zeros) to 32-bits complement
            for (byte    bIndex=(byte) (ADDRESS_NUM_BITS - numBits); bIndex > 0; bIndex--)
                aVal <<= 1;

            fromLong(aVal);
        }
        else    // zero length == 0.0.0.0
            reset();
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return (int) toLong();
    }
}

package net.community.chest.net.address;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.List;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 17, 2010 9:12:08 AM
 */
public class IPv6Address extends AbstractIPAddress {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7393071877030506058L;

	public IPv6Address ()
	{
		super(IPAddressType.IPv6);
	}
	/**
	 * Length of IPv6 address (in bytes)
	 */
	public static final int	ADDRESS_LENGTH=16;
	/**
	 * Number of bits for representing an IPv6 address
	 */
	public static final int ADDRESS_NUM_BITS=ADDRESS_LENGTH * Byte.SIZE;

	public static final long toLong (final byte[] aBytes, final int startOffset, final int numBits)
		throws NumberFormatException
	{
		int	maxLen=numBits / Byte.SIZE;
		if ((numBits & 0x07) != 0)
			maxLen++;

		final int	maxPos=startOffset + maxLen;
		if ((aBytes == null) || (aBytes.length < maxPos)
		 || (startOffset < 0) || (maxPos > aBytes.length)
		 || (numBits <= 0) || (numBits > Long.SIZE))
			throw new NumberFormatException("Illegal conversion arguments");

		long	retVal=0L;
		int		curPos=startOffset, remBits=numBits;
		for ( ; (curPos < maxPos) && (remBits > 0); curPos++)
		{
			int	bValue=aBytes[curPos] & 0x00FF;
			if (remBits < Byte.SIZE)
			{
				bValue = (bValue >>> (Byte.SIZE - remBits));
				retVal = (retVal << remBits) | bValue;
				remBits = 0;
			}
			else
			{
				retVal = (retVal << Byte.SIZE) | bValue;
				remBits -= Byte.SIZE;
			}
		}

		if (curPos < maxPos)
			throw new NumberFormatException("Incomplete usage of the data bytes: " + (curPos - startOffset) + " out of " + maxLen);
		if (remBits != 0)
			throw new NumberFormatException("Not all bits extracted: remaining=" + remBits);
		return retVal;
	}
	// return number of used bytes
	public static final int fromLong (final long value, final byte[] aBytes, final int startOffset, final int numBits)
		throws NumberFormatException
	{
		int	maxLen=numBits / Byte.SIZE;
		if ((numBits & 7) != 0)
			maxLen++;

		final int	maxPos=startOffset + maxLen;
		if ((aBytes == null) || (aBytes.length < maxPos)
		 || (startOffset < 0) || (maxPos > aBytes.length)
		 || (numBits <= 0) || (numBits > Long.SIZE))
			throw new NumberFormatException("Illegal conversion arguments");
		
		int		curPos=startOffset, remBits=numBits;
		for ( ; (curPos < maxPos) && (remBits > 0); curPos++)
		{
			if (remBits >= Byte.SIZE)
			{
				aBytes[curPos] = (byte) ((value >>> (remBits - Byte.SIZE)) & 0x00FF);
				remBits -= Byte.SIZE;
			}
			else
			{
				final byte	rawValue=(byte) ((value << (Byte.SIZE - remBits)) & 0x00FF);
				aBytes[curPos] |= rawValue;
				remBits = 0;
			}
		}

		return curPos - startOffset;
	}
	/**
	 * @return The high 64 bits of the address
	 */
	public long getNetworkPrefix ()
	{
		return toLong(getAddress(), 0, 64);
	}

	public void setNetworkPrefix (final long value)
	{
		fromLong(value, getAddress(), 0, 64);
	}
	/**
	 * @return The low 64 bits of the address
	 */
	public long getInterfaceIdentifier ()
	{
		return toLong(getAddress(), ADDRESS_LENGTH / 2, 64);
	}

	public void setInterfaceIdentifier (final long value)
	{
		fromLong(value, getAddress(), ADDRESS_LENGTH / 2, 64);
	}

	public static final long	LINK_LOCAL_NET_PREFIX=0xFE80000000000000L;
	public boolean isLinkLocalAddress ()
	{
		final long	netPrefix=getNetworkPrefix();
		if (netPrefix != LINK_LOCAL_NET_PREFIX)
			return false;	// debug breakpoint
		else
			return true;
	}
	public static final boolean isUnspecifiedAddress (final byte[] addr, final int offset, final int len)
	{
		final int	maxOffset=offset + len;
		if ((null == addr) || (len != ADDRESS_LENGTH) || (offset < len) || (maxOffset > addr.length))
			return false;	// debug breakpoint

		for (int	aIndex=offset; aIndex < maxOffset; aIndex++)
		{
			if (addr[aIndex] != 0)
				return false;
		}

		return true;
	}

	public static final boolean isUnspecifiedAddress (final byte... addr)
	{
		return isUnspecifiedAddress(addr, 0, (addr == null) ? 0 : addr.length);
	}

	public boolean isUnspecifiedAddress ()
	{
		return isUnspecifiedAddress(getAddress());
	}

	public static final boolean isLoopbackAddress (final byte[] addr, final int offset, final int len)
	{
		final int	maxOffset=offset + len;
		if ((null == addr) || (len != ADDRESS_LENGTH) || (offset < len) || (maxOffset > addr.length))
			return false;	// debug breakpoint

		if (addr[maxOffset - 1] != 1)
			return false;

		for (int	aIndex=maxOffset - 2; aIndex >= offset; aIndex--)
		{
			if (addr[aIndex] != 0)
				return false;
		}

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
	/**
	 * Default separator used between address components
	 */
	public static final char	ADDRESS_SEP_CHAR=':';
	/**
	 * @param <A> The type {@link Appendable} instance being used
	 * @param sb The {@link Appendable} instance to append to
	 * @param useZeroContraction If <code>true</code> then uses the zero-contraction form 
	 * @param a The address bytes
	 * @param useDottedQuadNotation If <code>true</code> then the last 4 bytes are displayed as IPv4 address
	 * @param offset Offset in the address bytes array to start from
	 * @param len Max. available length - if more than {@link #ADDRESS_LENGTH} than only
	 * {@link #ADDRESS_LENGTH} bytes will be used
	 * @return The updated {@link Appendable} instance (same as input)
	 * @throws IOException If failed to append the address value in any way
	 */
	public static final <A extends Appendable> A appendAddress (
			final A sb, final boolean useZeroContraction, final boolean useDottedQuadNotation, final byte[] a, final int offset, final int len)
		throws IOException
	{
		if (sb == null)
			throw new EOFException("No " + Appendable.class.getSimpleName() + " instance provided");

		final int	aLen=(null == a) ? 0 : a.length, maxOffset=offset + ADDRESS_LENGTH;
		if ((aLen < ADDRESS_LENGTH)
		 || (offset < 0)
		 || (len < ADDRESS_LENGTH)
		 || (maxOffset > aLen)
		 || ((offset + len) > aLen))
			throw new StreamCorruptedException("appendAddress() bad/illegal data");

		// if contraction not allowed then mark it as if it has been used since it can be used only once
		boolean 	contractionUsed=!useZeroContraction;
		final int	maxAppendOffset=useDottedQuadNotation ? maxOffset - IPv4Address.ADDRESS_LENGTH : maxOffset;
		int			curOffset=offset, cIndex=0;
		for ( ; curOffset < maxAppendOffset; cIndex++, curOffset += 2 /* each component is 16 bits */)
		{
			if (cIndex > 0)	// append separator from previous component
				sb.append(ADDRESS_SEP_CHAR);

			final byte	hiVal=a[curOffset];
			// check if have a series of zeros and have not used yet the contraction
			if ((hiVal == 0) && (!contractionUsed))
			{
				int	nextOffset=curOffset+1;
				// find where the zeros end
				for ( ; nextOffset < maxAppendOffset; nextOffset++)
				{
					if (a[nextOffset] != 0)
						break;
				}

				// check how many 16 bit components we can accommodate 
				final int	diffOffset=nextOffset - curOffset,
							numComps=diffOffset >> 1;	// we need 2 bytes per component
				if (numComps > 0)
				{
					contractionUsed = true;

					// if leading zeros then add the initial separator
					if (0 == cIndex)
						sb.append(ADDRESS_SEP_CHAR);

					// if all zeroes till end then need to add a separator
					if ((curOffset += (numComps << 1)) < maxAppendOffset)
					{
						curOffset -= 2; // compensate for the automatic +=2
						continue;
					}

					sb.append(ADDRESS_SEP_CHAR);
					break;
				}
			}

			final byte	loVal=a[curOffset + 1];
			final int	compVal=((hiVal << Byte.SIZE) & 0x0FF00) | (loVal & 0x00FF);
			sb.append(Integer.toHexString(compVal));
		}

		if (useDottedQuadNotation)
		{
			if (cIndex > 0)
				sb.append(ADDRESS_SEP_CHAR);

			final int	usedOffset=curOffset - offset;
			return IPv4Address.appendAddress(sb, a, curOffset, len - usedOffset);
		}

		return sb;
	}
	
	public static final <A extends Appendable> A appendAddress (
			final A sb, final boolean useZeroContraction, final boolean useDottedQuadNotation, final byte ... bytes)
		throws IOException
	{
		return appendAddress(sb, useZeroContraction, useDottedQuadNotation, bytes, 0, (bytes == null) ? 0 : bytes.length);
	}
	/**
	 * @param <A> The type {@link Appendable} instance being used
	 * @param sb The {@link Appendable} instance to append to
	 * @param useZeroContraction If <code>true</code> then uses the zero-contraction form 
	 * @param a The address bytes
	 * @param offset Offset in the address bytes array to start from
	 * @param len Max. available length - if more than {@link #ADDRESS_LENGTH} than only
	 * {@link #ADDRESS_LENGTH} bytes will be used
	 * @return The updated {@link Appendable} instance (same as input)
	 * @throws IOException If failed to append the address value in any way
	 */
	public static final <A extends Appendable> A appendAddress (
			final A sb, final boolean useZeroContraction, final byte[] a, final int offset, final int len)
		throws IOException
	{
		return appendAddress(sb, useZeroContraction, false, a, offset, len);
	}
	// uses the contracted form by default
	public static final <A extends Appendable> A appendAddress (
			final A sb, final byte[] a, final int offset, final int len)
		throws IOException
	{
		return appendAddress(sb, true, a, offset, len);
	}
	
	public static final <A extends Appendable> A appendAddress (
			final A sb, final boolean useZeroContraction, final byte ... addr)
		throws IOException
	{
		return appendAddress(sb, useZeroContraction, addr, 0, (addr == null) ? 0 : addr.length);
	}
	// uses the contracted form by default
	public static final <A extends Appendable> A appendAddress (
			final A sb, final byte ... addr)
		throws IOException
	{
		return appendAddress(sb, true, addr); 
	}

	public static final String	UNSPECIFIED_ADDRESS="::", LOOPBACK_ADDRESS="::1";
	private static final List<String> adjustDottedQuadAddressComponentsList (final List<String> addrComps)
	{
		// if dotted notation then replace the dot notation with HEX values
		final int	numComps=(addrComps == null) ? 0 : addrComps.size();
		if (numComps <= 0)
			return addrComps;

		final String		lastComp=addrComps.get(numComps - 1);
		final long			addrVal=IPv4Address.toLong(lastComp);
		final int			hiValue=(int) (addrVal >>> 16) & 0x00FFFF,
							loValue=(int) addrVal & 0x00FFFF;
		final List<String>	adjComps=new ArrayList<String>(numComps + 1);
		adjComps.addAll(addrComps);
		adjComps.set(numComps - 1, Integer.toHexString(hiValue));
		adjComps.add(Integer.toHexString(loValue));
		return adjComps;
	}
	/**
	 * Max. number of components in an address string delimited by the {@link #ADDRESS_SEP_CHAR}
	 */
	public static final int	MAX_ADDRESS_COMPONENTS=8;
	private static final List<String> padLastAddressComponents (final List<String> addrComps)
	{
		final int	numComps=(addrComps == null) ? 0 : addrComps.size();
		if ((numComps <= 0) || (numComps >= MAX_ADDRESS_COMPONENTS))
			return addrComps;

		final List<String>	adjComps=new ArrayList<String>(MAX_ADDRESS_COMPONENTS);
		adjComps.addAll(addrComps);
		for (int	cIndex=numComps; cIndex < MAX_ADDRESS_COMPONENTS; cIndex++)
			adjComps.add("0");

		return adjComps;
	}

	private static final List<String> adjustAddressComponentsList (final String s)
	{
		final int			sLen=(s == null) ? 0 : s.length();
		final List<String>	addrComps=(sLen <= 0) ? null : StringUtil.splitString(s, ADDRESS_SEP_CHAR);
		final int			numComps=(addrComps == null) ? 0 : addrComps.size();
		if (numComps <= 0)
			return addrComps;

		// check if dotted-quad notation - can be only last element
		final String	lastComp=addrComps.get(numComps-1);
		final int		cLen=(lastComp == null) ? 0 : lastComp.length();
		final int		dotPos=(cLen <= 0) ? (-1) : lastComp.indexOf(IPv4Address.ADDRESS_SEP_CHAR);
		if ((dotPos > 0) && (dotPos < (cLen - 1)))
			return adjustDottedQuadAddressComponentsList(addrComps);

		// check if ending in "::" and if so, then "pad" with zeroes
		if ((sLen > 2) && (ADDRESS_SEP_CHAR == s.charAt(sLen-1)) && (ADDRESS_SEP_CHAR == s.charAt(sLen-2))
		 && (lastComp != null) && (lastComp.length() > 0))
			return padLastAddressComponents(addrComps);

		return addrComps;
	}
	/**
	 * Separator used for scope ID (see rfc4007) 
	 */
	public static final char	SCOPE_ID_SEP_CHAR='%';
	// removes any special extra address characters - e.g., scope ID
	public static final String getPureAddressString (final String s)
	{
		final int	sLen=(s == null) ? 0 : s.length();
		if (sLen <= 0)
			return s;

		final int	scopePos=s.lastIndexOf(SCOPE_ID_SEP_CHAR);
		if ((scopePos > 0) && (scopePos < (sLen-1)))
			return s.substring(0, scopePos);

		return s;
	}
	// returns number of filled bytes (which is always ADDRESS_LENGTH)
	public static final int fromString (final String org, final byte[] addr, final int startOffset, final int maxLen)
		throws NumberFormatException
	{
		final String	s=getPureAddressString(org);
		final int		sLen=(s == null) ? 0 : s.length(),
						addrLen=(addr == null) ? 0 : addr.length,
						maxOffset=startOffset + ADDRESS_LENGTH;
		if ((sLen <= 0)
		 || (addrLen < maxLen)
		 || (maxLen < ADDRESS_LENGTH)
		 || (startOffset >= maxOffset)
		 || (maxOffset > addrLen))
			throw new NumberFormatException("Bad/Illegal conversion parameters");

		// the all zeros contraction is not handled correctly by the default code
		if (UNSPECIFIED_ADDRESS.equals(s))
		{
			for (int	curOffset=startOffset; curOffset < maxOffset; curOffset++)
				addr[curOffset] = 0;
			return ADDRESS_LENGTH;
		} 

		final List<String>	addrComps=adjustAddressComponentsList(s);
		final int			numComps=(addrComps == null) ? 0 : addrComps.size();
		if (numComps < 3)	// at least "::1" for "0:0:0:0:0:0:0:1" contraction
			throw new NumberFormatException("Not enough components in " + s);
		if (numComps > MAX_ADDRESS_COMPONENTS)
			throw new NumberFormatException("Too many components in " + s);

		boolean	contractionUsed=false;
		int		curOffset=startOffset, cIndex=0;
		for ( ; (cIndex < numComps) && (curOffset < maxOffset); cIndex++)
		{
			final String	compString=addrComps.get(cIndex);
			final int		cLen=(compString == null) ? 0 : compString.length();
			// means that the "::" zero(s) contraction has been used
			if (cLen <= 0)
			{
				// zero contraction may be used only once
				if (contractionUsed)
					throw new NumberFormatException("Zero contraction re-used in " + s);
				contractionUsed = true;

				/* Contracted string cannot be last - only the following contraction formats can occur:
				 * 
				 * 		a::b 	- some contraction between 2 non-zero components
				 * 		a::		- all zeros at the end
				 * 		::b		- starting with all zeros
				 * 		::		- only zeros
				 * 
				 * In any case the empty split component must be followed by at least one other
				 * component (that may also be null/empty)
				 */
				if ((cIndex + 1) >= numComps)
					throw new NumberFormatException("Zero contraction re-used in " + s);

				final String	nextComp=addrComps.get(cIndex+1);
				// check if "a::b" - if not, then skip the empty component
				if ((nextComp == null) || (nextComp.length() <= 0))
					cIndex++;

				// calculate where we have to continue the filling of the address bytes
				final int	remComps=numComps - cIndex - 1,
				// the next offset to start from relative to the END of the address buffer
							nextOffset=ADDRESS_LENGTH - (remComps * 2 /* each component takes 2 bytes */);
				for ( ; curOffset < nextOffset; curOffset++)
					addr[curOffset] = 0;
				continue;
			}

			int	compVal=Integer.parseInt(compString, 16);
			if ((compVal < 0) || (compVal > 0x0FFFF))
				throw new NumberFormatException("Component=" + compString + " exceeds 16 bits in " + s);

			addr[curOffset] = (byte) ((compVal >>> Byte.SIZE) & 0x0FF);
			addr[curOffset+1] = (byte) (compVal & 0x00FF);
			curOffset += 2;
		}

		if (cIndex < numComps)
			throw new NumberFormatException("Not exhausted all address components in " + s);

		if (curOffset < maxOffset)
			throw new NumberFormatException("Not exhausted all address bytes in " + s);

		return ADDRESS_LENGTH;
	}

	public static final int fromString (final String s, final byte ... addr)
		throws NumberFormatException
	{
		return fromString(s, addr, 0, (addr == null) ? 0 : addr.length);
	}

	@Override
	public void fromString (String s) throws NumberFormatException
	{
		final int	nRes=fromString(s, getAddress());
		if (nRes != ADDRESS_LENGTH)	// should not happen
			throw new NumberFormatException("Bad/Illegal conversion result length: " + nRes);
	}

	public IPv6Address (String s) throws NumberFormatException
	{
		super(IPAddressType.IPv6);
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
	public IPv6Address (final byte[] av, final int offset, final int len) throws IllegalArgumentException
	{
		super(IPAddressType.IPv6);
		fromBytes(av, offset, len);
	}
	/**
	 * Initializes using the given array data
	 * @param av data array - if more than {@link #ADDRESS_LENGTH} bytes available, then
	 * only the first ones are used
	 * @throws IllegalArgumentException if bad/illegal array
	 * @see #fromBytes(byte[])
	 */
	public IPv6Address (final byte ... av) throws IllegalArgumentException
	{
		this(av, 0, (av == null) ? 0 : av.length);
	}
	/**
	 * @return The last 4 bytes as an {@link IPv4Address}
	 */
	public IPv4Address toIPv4Address ()
	{
		return new IPv4Address(getAddress(), ADDRESS_LENGTH - IPv4Address.ADDRESS_LENGTH, IPv4Address.ADDRESS_LENGTH);
	}

	public void fromIPv4Address (IPv4Address a /* null=0.0.0.0 */)
	{
		final byte[]	v6Addr=getAddress(), v4Addr=(a == null) ? null : a.getAddress();
		for (int	aIndex=0; aIndex < ADDRESS_LENGTH - IPv4Address.ADDRESS_LENGTH; aIndex++)
			v6Addr[aIndex] = 0;

		for (int	v6Index=ADDRESS_LENGTH-1, v4Index=IPv4Address.ADDRESS_LENGTH-1; v4Index >= 0; v6Index--, v4Index--)
			v6Addr[v6Index] = (v4Addr == null) ? 0 : v4Addr[v4Index];
	}

	public IPv6Address (IPv4Address a /* null=0.0.0.0 */)
	{
		super(IPAddressType.IPv6);
		fromIPv4Address(a);
	}
	
	public IPv6Address (Inet6Address a) throws IllegalArgumentException
	{
		this((a == null) ? null : a.getAddress());
	}
	/*
	 * @see net.community.chest.net.address.AbstractIPAddress#clone()
	 */
	@Override
	@CoVariantReturn
	public IPv6Address clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}

	public static final String toString (final boolean useContraction, final boolean useDotQuadNotation,
										 final byte[] a, final int offset, final int len)
	{
		if (len <= 0)
			return null;

		try
		{
			return appendAddress(new StringBuilder(ADDRESS_LENGTH * 6 + 2), useContraction, useDotQuadNotation, a, offset, len).toString();
		}
		catch(IOException e)	// can happen if bad arguments
		{
			throw new RuntimeException(e);
		}
	}

	public static final String toString (final boolean useContraction, final byte[] a, final int offset, final int len)
	{
		return toString(useContraction, false, a, offset, len);
	}

	public static final String toString (final byte[] a, final int offset, final int len)
	{
		return toString(true, a, offset, len);
	}

	public static final String toString (final byte ... a)
	{
		return toString(true, a);
	}

	public static final String toString (final boolean useContraction, final boolean useDotQuadNotation, final byte ... a)
	{
		return toString(useContraction, useDotQuadNotation, a, 0, (null == a) ? 0 : a.length);
	}

	public static final String toString (final boolean useContraction, final byte ... a)
	{
		return toString(useContraction, false, a);
	}

	public String toString (boolean useContraction, boolean useDotQuadNotation)
	{
		return toString(useContraction, useDotQuadNotation, getAddress());
	}
	
	public String toString (boolean useContraction)
	{
		return toString(useContraction, false);
	}
	/*
	 * @see net.community.chest.net.address.AbstractIPAddress#toString()
	 */
	@Override
	public String toString ()
	{
		return toString(true);
	}
}

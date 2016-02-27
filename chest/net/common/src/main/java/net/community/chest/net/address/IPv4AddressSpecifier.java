package net.community.chest.net.address;

import java.io.Serializable;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.math.LongsComparator;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 28, 2007 2:18:23 PM
 */
public class IPv4AddressSpecifier implements Comparable<IPv4AddressSpecifier>, PubliclyCloneable<IPv4AddressSpecifier>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8446147527044814723L;
	private IPv4Address	_address	/* =null */, _netmask	/* =null */;
	/**
	 * Default (empty) constructor
	 */
	public IPv4AddressSpecifier ()
	{
		super();
	}
	/**
	 * @return the address part (null if none set)
	 */
	public IPv4Address getAddress ()
	{
		return _address;
	}

	public void setAddress (IPv4Address address)
	{
		_address = address;
	}
	/**
	 * @return the netmask part (null if none set)
	 */
	public IPv4Address getNetmask ()
	{
		return _netmask;
	}

	public void setNetmask (IPv4Address netmask)
	{
		_netmask = netmask;
	}
	/**
	 * Initialized constructor
	 * @param address address part - may be null (not checked)
	 * @param netmask netmask part - may be null (not checked)
	 */
	public IPv4AddressSpecifier (IPv4Address address, IPv4Address netmask)
	{
		setAddress(address);
		setNetmask(netmask);
	}
	/**
	 * No-netmask constructor
	 * @param address address part - may be null (not checked)
	 */
	public IPv4AddressSpecifier (IPv4Address address)
	{
		setAddress(address);
	}
	/**
	 * Converts the address + netmask part(s) to a 32-bit integer
	 * @param aPart address part - if null, then ZERO is returned
	 * @param mPart netmask part - if null then address part value is returned
	 * @return address + netmask applied to it as a 32-bit integer
	 */
	public static final long toLong (IPv4Address aPart, IPv4Address mPart)
	{
		if (null == aPart)
			return 0;

		final long	aVal=aPart.toLong();
		if (null == mPart)	// if not netmask, then return original address
			return aVal;

		final long	mVal=mPart.toLong();
		return (aVal & mVal) & IPv4Address.IPv4ADRESS_VALUE_MASK;
	}
	/**
	 * Resulting 32-bit integer representation if the current netmask is
	 * applied to the supplied address part
	 * @param aPart address part - if null, then ZERO is returned
	 * @return address with netmask (if any) applied to it - if no netmask
	 * set then address part value is used
	 */
	public long toLong (IPv4Address aPart)
	{
		return toLong(aPart, getNetmask());
	}
	/**
	 * @return final representation of the address - taking into account the
	 * netmask (if any). If no netmask, then the original address is returned.
	 */
	public long toLong ()
	{
		return toLong(getAddress());
	}
	/**
	 * @return final address (after applying the netmask) as an {@link IPv4Address} instance
	 */
	public IPv4Address toAddress ()
	{
		return new IPv4Address(toLong());
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public IPv4AddressSpecifier clone () throws CloneNotSupportedException
	{
		final IPv4AddressSpecifier	as=getClass().cast(super.clone());
		final IPv4Address			a=getAddress(), m=getNetmask();
		if (a != null)
			as.setAddress(a.clone());
		if (m != null)
			as.setNetmask(m.clone());

		return as;
	}
	/**
	 * @param a address part to be checked if same as this one after applying
	 * the netmask to both.
	 * @return TRUE if same (after netmask - if any - applied). <B>Note:</B>
	 * returns FALSE if address part is null
	 */
	public boolean isSameAddress (IPv4Address a)
	{
		if (null == a)
			return false;

		return toLong() == toLong(a);
	}
	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (IPv4AddressSpecifier o)
	{
		if (null == o)
			return (-1);
		if (this == o)
			return 0;

		return LongsComparator.compare(toLong(), o.toLong());
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (obj instanceof IPv4AddressSpecifier)
			return (0 == compareTo((IPv4AddressSpecifier) obj));
		else
			return false;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return (int) toLong();
	}
	/**
	 * Delimiter used in address specifications to separate between the
	 * address and the netmask - e.g. "192.168.1.3/24" 
	 */
	public static final char NETMASK_SEPARATOR_CHAR='/';
	public static final String NETMASK_SEPARATOR=String.valueOf(NETMASK_SEPARATOR_CHAR);
	/**
	 * Initializes contents from string - format is "address/netmask", where
	 * each part is optional - e.g., "127.0.0.1" (address only), "10.3.3.5/24",
	 * "194.32.4.3/255.255.0.0" (address + netmask), "/255.0.0.0" (netmask only,
	 * in which case the address is set/reset to "0.0.0.0")
	 * @param sv string value - may NOT be null/empty
	 * @throws IllegalArgumentException bad/illegal string
	 * @throws NumberFormatException bad/illegal address/netmask format
	 */
	public void fromString (String sv) throws IllegalArgumentException, NumberFormatException
	{
		final int	svLen=(null == sv) ? 0 : sv.length();
		if (svLen <= 0)
			throw new IllegalArgumentException("Null/empty string value");

		final int		sepPos=sv.indexOf(NETMASK_SEPARATOR_CHAR);
		final String	aPart=(sepPos >= 0) ? sv.substring(0, sepPos) : sv,
						mPart=(sepPos >= 0) ? sv.substring(sepPos + 1) : null;

		if ((aPart != null) && (aPart.length() >= 0))
			setAddress(new IPv4Address(aPart));
		else	// no address part
			setAddress(null);

		if ((mPart != null) && (mPart.length() >= 0))
		{
			final IPv4Address	m=new IPv4Address();
			// check if this is a simple number
			if (mPart.indexOf('.') > 0)
				m.fromString(mPart);
			else	// assume a mask length
				m.fromMask(Byte.parseByte(mPart));

			setNetmask(m);
		}
		else	// no netmask
			setNetmask(null);
	}
	// @see #fromString()
	public IPv4AddressSpecifier (String sv) throws IllegalArgumentException, NumberFormatException
	{
		fromString(sv);
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final IPv4Address	a=getAddress(), m=getNetmask();
		final String		as=(null == a) ? null : a.toString(),
							ms=(null == m) ? null : m.toString();
		final boolean		ha=(as != null) && (as.length() > 0),
							hm=(ms != null) && (ms.length() > 0);

		if (ha)
		{
			if (hm)
				return as + NETMASK_SEPARATOR + ms;
			else
				return as;
		}
		else	// no address - maybe just netmask
		{
			if (hm)
				return NETMASK_SEPARATOR + ms;
			else
				return "";
		}
	}
}

/*
 * 
 */
package net.community.chest.net.address;

import java.io.Serializable;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.PubliclyCloneable;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 17, 2010 9:03:00 AM
 */
public abstract class AbstractIPAddress implements Serializable, PubliclyCloneable<AbstractIPAddress>, Comparable<AbstractIPAddress> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7937556434108903695L;
	private final IPAddressType	_addrType;
	public final IPAddressType getAddressType ()
	{
		return _addrType;
	}
	/**
	 * The internal address bytes
	 */
	private byte[]	_address;
	/**
	 * @return Address bytes
	 */
	public final byte[] getAddress ()
	{
		return _address;
	}

	protected AbstractIPAddress (final IPAddressType addrType)
	{
		if ((_addrType=addrType) == null)
			throw new IllegalStateException("No address type specified");
		_address = new byte[addrType.getNumBytes()];
	}
	/**
	 * Zero-es the current contents 
	 */
	public void reset ()
	{
		final byte[]	av=getAddress();
		final int		avLen=(null == av) ? 0 : av.length;
		for (int	aIndex=0; aIndex < avLen; aIndex++)
			av[aIndex] = 0;
	}
	/**
	 * Initializes using the given array data
	 * @param av data array to copy from
	 * @param offset offset in array to start copying
	 * @param len number of available bytes - if more than required number of bytes
	 * available, then only the first ones are used 
	 * @throws IllegalArgumentException if bad/illegal array
	 */
	public void fromBytes (final byte[] av, final int offset, final int len) throws IllegalArgumentException
	{
		final byte[]	da=getAddress();
		final int		avLen=(null == av) ? 0 : av.length;
		if ((avLen <= 0)
		 || (offset < 0)
		 || ((offset + len) > avLen)
		 || (null == da) || (da.length <= 0)
		 || (len < da.length))
			throw new IllegalArgumentException("Bad/Illegal address bytes array specification");

		System.arraycopy(av, 0, da, 0, da.length);
	}
	/**
	 * Initializes using the given array data
	 * @param av data array - if more than required address bytes available, then
	 * only the first ones are used
	 * @throws IllegalArgumentException if bad/illegal array
	 */
	public void fromBytes (final byte ... av) throws IllegalArgumentException
	{
		fromBytes(av, 0, (null == av) ? 0 : av.length);
	}
	/**
	 * Initializes contents from specified string
	 * @param s string to be used - may NOT be null/empty, and MUST be a valid
	 * representation of an IP address
	 * @throws NumberFormatException bad/illegal format in string
	 */
	public abstract void fromString (String s) throws NumberFormatException;
	/**
	 * @return <code>true</code> if this address represents a loopbacl
	 */
	public abstract boolean isLoopbackAddress ();
	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (AbstractIPAddress o)
	{
		if (this == o)
			return 0;

		final IPAddressType	tType=getAddressType(), oType=(o == null) ? null : o.getAddressType();
		if (oType == null)	// push null(s) to end
			return (-1);
		else if (tType == null)	// unlikely
			return (+1);

		int	nRes=tType.compareTo(oType);
		if (nRes != 0)
			return nRes;

		if ((nRes=InetAddressComparator.compareAddresses(getAddress(), o.getAddress())) != 0)
			return nRes;	// debug breakpoint

		return 0;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		final IPAddressType	tType=getAddressType();
		return tType.hashCode();
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof AbstractIPAddress))
			return false;
		if (this == obj)
			return true;

		if (compareTo((AbstractIPAddress) obj) != 0)
			return false;	// debug breakpoint

		return true;
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public AbstractIPAddress clone () throws CloneNotSupportedException
	{
		final AbstractIPAddress	ret=getClass().cast(super.clone());
		final byte[]			av=getAddress();
		if (av != null)
			ret._address = av.clone();

		return ret;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return String.valueOf(getAddressType());
	}
}

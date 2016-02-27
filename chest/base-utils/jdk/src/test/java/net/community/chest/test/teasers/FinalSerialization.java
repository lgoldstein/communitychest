/*
 * 
 */
package net.community.chest.test.teasers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * <P>Copyright as per GPLv2</P>
 * 
 * <P>Check if <B><U>final</U></B> values can be serialized</P>
 * @author Lyor G.
 * @since Nov 10, 2010 11:18:48 AM
 *
 */
public class FinalSerialization implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5999797295915767101L;
	private final long	_value;
	public final long getValue ()
	{
		return _value;
	}

	public FinalSerialization ()
	{
		_value = System.currentTimeMillis();
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return (int) getValue();
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof FinalSerialization))
			return false;
		if (this == obj)
			return true;

		final FinalSerialization	oo=(FinalSerialization) obj;
		if (getValue() != oo.getValue())
			return false;

		return true;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return String.valueOf(getValue());
	}

	public static void main (String[] args)
	{
		try
		{
			final FinalSerialization	org=new FinalSerialization();
			final ByteArrayOutputStream	byteOutStream=new ByteArrayOutputStream(256);
			final ObjectOutputStream	objOutStream=new ObjectOutputStream(byteOutStream);
			objOutStream.writeObject(org);
			objOutStream.close();

			final byte[]				orgData=byteOutStream.toByteArray();
			final ByteArrayInputStream	byteInStream=new ByteArrayInputStream(orgData);
			final ObjectInputStream		objInStream=new ObjectInputStream(byteInStream);
			final Object				serObject=objInStream.readObject();
			objInStream.close();

			if (!org.equals(serObject))
				throw new IllegalStateException("De-serialized object (" + serObject + ") not same as original (" + org + ")");
			System.out.append("Org=").append(org.toString())
					  .append(";Deser=").append(serObject.toString())
				.println()
				;
		}
		catch(Exception e)
		{
			System.err.append(e.getClass().getName())
					  .append(": ")
					  .append(e.getMessage())
				.println();
		}
	}

}

/*
 * 
 */
package net.community.chest.jinterop.core;

import net.community.chest.lang.EnumUtil;

import org.jinterop.dcom.core.JIFlags;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulate the {@link JIFlags} values into an {@link Enum}
 * @author Lyor G.
 * @since May 19, 2009 10:51:49 AM
 */
public enum JIFlagType {
	/**
	 * Flag representing nothing. Use this if no other flag is to be set.
	 */
	NULL(JIFlags.FLAG_NULL),
	/**
	 * Flag representing a <code>BSTR</code> string .
	 **/
	BSTR(JIFlags.FLAG_REPRESENTATION_STRING_BSTR),
	/**
	 * Flag representing a normal String. 
	 */
	LPCTSTR(JIFlags.FLAG_REPRESENTATION_STRING_LPCTSTR),
	/**
	 * Flag representing a Wide Char (16 bit characters)
	 */
	LPWSTR(JIFlags.FLAG_REPRESENTATION_STRING_LPWSTR),
	/**
	 * Flag representing an array
	 */
	ARRAY(JIFlags.FLAG_REPRESENTATION_ARRAY),
	/**
	 * Flag representing that this is a IDispatch invoke call
	 */
	IDISPATCH(JIFlags.FLAG_REPRESENTATION_IDISPATCH_INVOKE), 
	/**
	 * Flag representing unsigned byte.
	 */
	UBYTE(JIFlags.FLAG_REPRESENTATION_UNSIGNED_BYTE),
	/**
	 * Flag representing unsigned short.
	 */
	USHORT(JIFlags.FLAG_REPRESENTATION_UNSIGNED_SHORT),
	/**
	 * Flag representing unsigned integer.
	 */
	UINT(JIFlags.FLAG_REPRESENTATION_UNSIGNED_INT),
	/**
	 * Flag representing integer of the type VT_INT.
	 */
	VTINT(JIFlags.FLAG_REPRESENTATION_VT_INT),
	/**
	 * Flag representing (unsigned) integer of the type VT_UINT.
	 */
	VTUINT(JIFlags.FLAG_REPRESENTATION_VT_UINT),
	/**
	 * Flag representing <code>VARIANT_BOOL</code>, a <code>boolean</code> is 
	 * 2 bytes for a <code>VARIANT</code> and 1 byte for normal calls.
	 * Use this when setting array of <code>boolean</code>s within <code>VARIANT</code>s.
	 */
	VTBOOL(JIFlags.FLAG_REPRESENTATION_VARIANT_BOOL);

	private final int	_flagValue;
	public final int getFlagValue ()
	{
		return _flagValue;
	}

	JIFlagType (int v)
	{
		_flagValue = v;
	}

	private static JIFlagType[]	_values;
	public static final synchronized JIFlagType[] getValues ()
	{
		if (null == _values)
			_values = values();
		return _values;
	}

	public static final JIFlagType fromString (final String s)
	{
		return EnumUtil.fromString(getValues(), s, false);
	}

	public static final JIFlagType fromFlagValue (final int f)
	{
		if (f < 0)
			return null;

		final JIFlagType[]	vals=getValues();
		if ((null == vals) || (vals.length <= 0))
			return null;

		for (final JIFlagType v : vals)
		{
			if ((v != null) && (f == v.getFlagValue()))
				return v;
		}

		return null;
	}
}

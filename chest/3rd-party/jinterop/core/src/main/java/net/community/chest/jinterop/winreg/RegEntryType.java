/*
 * 
 */
package net.community.chest.jinterop.winreg;

import net.community.chest.lang.EnumUtil;

import org.jinterop.winreg.IJIWinReg;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulate registry entry type(s) as {@link Enum}-s</P>
 * @author Lyor G.
 * @since May 19, 2009 12:24:38 PM
 */
public enum RegEntryType {
	/**
	 * Type specifying empty type
	 */
	NONE(IJIWinReg.REG_NONE),
	/**
	 * Type specifying String
	 */
	SZ(IJIWinReg.REG_SZ),
	/**
	 * Type specifying Binary
	 */
	BINARY(IJIWinReg.REG_BINARY),
	/**
	 * Type specifying DWORD
	 */
	DWORD(IJIWinReg.REG_DWORD),
	/**
	 * Type specifying environment string
	 */
	SZEXP(IJIWinReg.REG_EXPAND_SZ),
	/**
	 * Type specifying multiple strings (array)
	 */
	SZMULTI(IJIWinReg.REG_MULTI_SZ);

	private final int	_eType;
	public final int getEntryType ()
	{
		return _eType;
	}
	
	RegEntryType (int t)
	{
		_eType = t;
	}
	
	private static RegEntryType[]	_values;
	public static final synchronized RegEntryType[] getValues ()
	{
		if (null == _values)
			_values = values();
		return _values;
	}

	public static final RegEntryType fromString (final String s)
	{
		return EnumUtil.fromString(getValues(), s, false);
	}

	public static final RegEntryType fromTypeValue (final int t)
	{
		if (t < 0)
			return null;

		final RegEntryType[]	vals=getValues();
		if ((null == vals) || (vals.length <= 0))
			return null;

		for (final RegEntryType v : vals)
		{
			if ((v != null) && (v.getEntryType() == t))
				return v;
		}

		return null;
	}
}

/*
 * 
 */
package net.community.chest.jinterop.winreg;

import java.util.Collection;

import net.community.chest.lang.EnumUtil;
import net.community.chest.util.set.SetsUtils;

import org.jinterop.winreg.IJIWinReg;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulate the registry access types as {@link Enum}-s
 * @author Lyor G.
 * @since May 19, 2009 12:30:03 PM
 */
public enum RegAccessType {
	ALL(IJIWinReg.KEY_ALL_ACCESS),
	CRLINK(IJIWinReg.KEY_CREATE_LINK),
	CRSUBKEY(IJIWinReg.KEY_CREATE_SUB_KEY),
	ENMSUBKEYS(IJIWinReg.KEY_ENUMERATE_SUB_KEYS),
	EXEC(IJIWinReg.KEY_EXECUTE),
	NOTIFY(IJIWinReg.KEY_NOTIFY),
	VALQUERY(IJIWinReg.KEY_QUERY_VALUE),
	READKEY(IJIWinReg.KEY_READ),
	SETVALUE(IJIWinReg.KEY_SET_VALUE),
	WRITEKEY(IJIWinReg.KEY_WRITE);

	private final int	_accVal;
	public final int getAccessMask ()
	{
		return _accVal;
	}
	
	RegAccessType (int m)
	{
		_accVal = m;
	}

	private static RegAccessType[]	_values;
	public static final synchronized RegAccessType[] getValues ()
	{
		if (null == _values)
			_values = values();
		return _values;
	}

	public static final RegAccessType fromString (final String s)
	{
		return EnumUtil.fromString(getValues(), s, false);
	}

	public static final int fromAccessMask (final Collection<RegAccessType> al)
	{
		if ((null == al) || (al.size() <= 0))
			return 0;

		int	retMask=0;
		for (final RegAccessType t : al)
		{
			final int	m=(null == t) ? 0 : t.getAccessMask();
			retMask |= m;
		}

		return retMask;
	}
	
	public static final int fromAccessMask (final RegAccessType ... al)
	{
		return fromAccessMask(SetsUtils.setOf(al));
	}
}

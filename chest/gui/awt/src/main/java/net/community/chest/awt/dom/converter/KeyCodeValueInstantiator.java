package net.community.chest.awt.dom.converter;

import java.awt.event.KeyEvent;
import java.util.NoSuchElementException;

import net.community.chest.convert.NumberValueStringInstantiator;
import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Converts a string into a {@link KeyEvent} key code.
 * @author Lyor G.
 * @since Jul 24, 2007 2:30:34 PM
 */
public class KeyCodeValueInstantiator extends AbstractXmlValueStringInstantiator<Integer>
		implements NumberValueStringInstantiator<Integer> {
	public KeyCodeValueInstantiator ()
	{
		super(Integer.class);
	}
	/*
	 * @see net.community.chest.reflect.NumberValueStringInstantiator#getPrimitiveValuesClass()
	 */
	@Override
	public Class<Integer> getPrimitiveValuesClass ()
	{
		return getValuesClass();
	}
	/**
	 * Standard prefix of all "VK_xxx" values
	 */
	public static final String	VK_PREFIX="VK_";
	/**
	 * Appends {@link #VK_PREFIX} to base name - if not null/empty
	 * @param baseName base name - if null/empty then nothing done
	 * @return appended name (null/empty if original null/empty base name)
	 */
	public static final String createVKKeyName (final String baseName)
	{
		if ((null == baseName) || (baseName.length() <= 0))
			return null;
		return VK_PREFIX + baseName;
	}
	/**
	 * @param baseName base string to be added before the offset - may not
	 * be null/empty
	 * @param offset offset to be appended - must be in [0-127] range
	 * @return {@link #VK_PREFIX} + baseName + offset
	 */
	public static final String createVKKeyName (final String baseName, final int offset)
	{
		if ((null == baseName) || (baseName.length() <= 0)
		 || (offset < 0) || (offset > Byte.MAX_VALUE))
			return null;

		return createVKKeyName(baseName + String.valueOf(offset));
	}
	/**
	 * @param offset offset from base character - must be in [0-127] range
	 * @param baseChar base character - must be in [1-127] range
	 * @return {@link #VK_PREFIX} + (baseChar + offset) - null/empty if bad arguments
	 */
	public static final String createVKKeyName (final int offset, final char baseChar)
	{
		if ((baseChar <= '\0') || (baseChar > 0x007E)
		 || (offset < 0) || (offset > Byte.MAX_VALUE))
			return null;

		final char	vkChar=(char) (baseChar + offset);
		return createVKKeyName(String.valueOf(vkChar));
	}
	// some "known" VK_xxx base name(s)
	public static final String	NUMPAD_BASENAME="NUMPAD",
								FUNC_BASENAME="F";
	public static final String toString (final int keyCode) throws IllegalArgumentException
	{
		if (KeyEvent.CHAR_UNDEFINED >= keyCode)
			throw new IllegalArgumentException("fromInteger(" + keyCode + ") invalid value");

		if ((KeyEvent.VK_A <= keyCode) && (keyCode <= KeyEvent.VK_Z))
			return createVKKeyName(keyCode - KeyEvent.VK_A, 'A');
		else if ((KeyEvent.VK_0 <= keyCode) && (keyCode <= KeyEvent.VK_9))
			return createVKKeyName(keyCode - KeyEvent.VK_0, '0');
		else if ((KeyEvent.VK_NUMPAD0 <= keyCode) && (keyCode <= KeyEvent.VK_NUMPAD9))
			return createVKKeyName(NUMPAD_BASENAME, keyCode - KeyEvent.VK_NUMPAD0);
		else if ((KeyEvent.VK_F1 <= keyCode) && (keyCode <= KeyEvent.VK_F12))
			return createVKKeyName(FUNC_BASENAME, 1 + (keyCode - KeyEvent.VK_F1));
		else if ((KeyEvent.VK_F13 <= keyCode) && (keyCode <= KeyEvent.VK_F24))
			return createVKKeyName(FUNC_BASENAME, 1 + (keyCode - KeyEvent.VK_F13));
		else	// TODO add some more key codes if necessary
			return null;
	}
	/*
	 * @see net.community.chest.reflect.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (final Integer inst) throws Exception
	{
		if (null == inst)
			return null;

		final String	ret=toString(inst.intValue());
		if ((null == ret) || (ret.length() <= 0))
			throw new NoSuchElementException(getArgumentsExceptionLocation("convertInstance", inst) + " invalid conversion result");

		return ret;
	}

	public static final Integer fromString (final String s)
	{
		final String	ts=StringUtil.getCleanStringValue(s),
						val=(null == ts) ? null : ts.toUpperCase();
		final int		vLen=(null == val) ? 0 : val.length();
		if (vLen <= 0)
			return null;

		// TODO allow non-VK values as well
		if (!val.startsWith(VK_PREFIX))
			return null;
		if (vLen <= VK_PREFIX.length())
			return null;

		final String	remVal=val.substring(VK_PREFIX.length());
		final int		rLen=(null == remVal) ? 0 : remVal.length();
		if (1 == rLen)
		{
			final char	opChar=remVal.charAt(0);
			if (('A' <= opChar) && (opChar <= 'Z'))
				return Integer.valueOf(KeyEvent.VK_A + (opChar - 'A'));
			else if (('0' <= opChar) && (opChar <= '0'))
				return Integer.valueOf(KeyEvent.VK_0 + (opChar - '0'));
		// else fall through to null return value
		}
		else if (remVal.startsWith(NUMPAD_BASENAME))
		{
			if (rLen <= NUMPAD_BASENAME.length())
				return null;

			final String	padVal=remVal.substring(NUMPAD_BASENAME.length());
			if (padVal.length() != 1)
				return null;

			final char	padChar=padVal.charAt(0);
			if (('0' <= padChar) && (padChar <= '0'))
				return Integer.valueOf(KeyEvent.VK_NUMPAD0 + (padChar - '0'));
			// else fall through to null return value
		}
		else if (remVal.startsWith(FUNC_BASENAME))
		{
			if (rLen <= FUNC_BASENAME.length())
				return null;

			final String	funcName=remVal.substring(FUNC_BASENAME.length());
			final int		funcVal=Integer.parseInt(funcName);
			if ((1 <= funcVal) && (funcVal <= 12))
				return Integer.valueOf(KeyEvent.VK_F1 + (funcVal - 1));
			else if ((13 <= funcVal) && (funcVal <= 24))
				return Integer.valueOf(KeyEvent.VK_F13 + (funcVal - 13));
			// else fall through to null return value
		}
		// TODO allow for more code keys
		return null;
	}
	/*
	 * @see net.community.chest.reflect.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public Integer newInstance (final String s) throws Exception
	{
		final String	val=StringUtil.getCleanStringValue(s);
		if ((null == val) || (val.length() <= 0))
			return null;

		final Integer	v=fromString(val);
		if (null == v)
			throw new NoSuchElementException("newInstance(" + s + ") unknown value");

		return v;
	}
	/*
	 * @see net.community.chest.reflect.ValueNumberInstantiator#getInstanceNumber(java.lang.Object)
	 */
	@Override
	public Integer getInstanceNumber (Integer inst) throws Exception
	{
		return inst;
	}
	/*
	 * @see net.community.chest.reflect.ValueNumberInstantiator#newInstance(java.lang.Number)
	 */
	@Override
	public Integer newInstance (Integer num) throws Exception
	{
		return num;
	}

	public static final KeyCodeValueInstantiator	DEFAULT=new KeyCodeValueInstantiator();
}

package net.community.chest.convert;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Converts TRUE/FALSE into pre-defined strings</P>
 * 
 * @author Lyor G.
 * @since Nov 12, 2007 1:55:49 PM
 */
public class BooleanStateFormatter extends Format {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6667090736306969967L;
	/**
	 * String to show for TRUE value(s)
	 */
	private String	_trueString	/* =null */;
	public String getTrueString ()
	{
		return _trueString;
	}

	public void setTrueString (String trueString)
	{
		_trueString = trueString;
	}
	/**
	 * String to show for false value(s)
	 */
	private String	 _falseString	/* =null */;
	public String getFalseString ()
	{
		return _falseString;
	}

	public void setFalseString (String falseString)
	{
		_falseString = falseString;
	}

	public BooleanStateFormatter ()
	{
		_trueString = String.valueOf(true);
		_falseString = String.valueOf(false);
	}

	public String getDisplayString (boolean val)
	{
		return val ? getTrueString() : getFalseString();
	}

	public void setDisplayString (boolean val, String s)
	{
		if (val)
			setTrueString(s);
		else
			setFalseString(s);
	}

	public BooleanStateFormatter (final String trueString, final String falseString) throws IllegalArgumentException
	{
		if ((null == (_trueString=trueString)) || (trueString.length() <= 0)
		 || (null == (_falseString=falseString)) || (falseString.length() <= 0))
			throw new IllegalArgumentException("<cinit>(" + trueString + "/" + falseString + ") incomplete specification");
	}

	public static final char	VALUES_SEP='/';
	// Format: TRUE value/FALSE value 
	public BooleanStateFormatter (final String vals) throws IllegalArgumentException
	{
		final int	vLen=(null == vals) ? 0 : vals.length(),
					sPos=(vLen <= 0) ? (-1) : vals.lastIndexOf(VALUES_SEP);

		_trueString = (sPos > 0) ? vals.substring(0, sPos) : null;
		_falseString = ((sPos > 0) && (sPos < (vLen-1))) ? vals.substring(sPos + 1) : null;

		if ((null == _trueString) || (_trueString.length() <= 0)
		 || (null == _falseString) || (_falseString.length() <= 0))
			throw new IllegalArgumentException("<cinit>(" + vals + ") incomplete argument(s)");
	}
	/*
	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
	 */
	@Override
	public StringBuffer format (Object obj, StringBuffer toAppendTo, FieldPosition pos)
	{
		if ((obj != null) && (obj instanceof Boolean))
		{
			final String	s=getDisplayString(((Boolean) obj).booleanValue());
			if ((s != null) && (s.length() > 0))
				return toAppendTo.append(s);
		}

		return toAppendTo;
	}
	/*
	 * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
	 */
	@Override
	public Object parseObject (String source, ParsePosition pos)
	{
		throw new UnsupportedOperationException("parseObject(" + source + ") N/A");
	}
}

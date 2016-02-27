/*
 * 
 */
package net.community.chest.db.sql;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.io.FileUtil;
import net.community.chest.math.compare.ComparableOperator;
import net.community.chest.math.strings.StringComparison;

import org.w3c.dom.Document;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 1, 2009 1:37:58 PM
 */
public final class SQLUtils {
	private SQLUtils ()
	{
		// no instance
	}

	public static final Document loadDocument (final Blob b) throws Exception
	{
		InputStream	in=null;
		try
		{
			in = b.getBinaryStream();
			return DOMUtils.loadDocument(in);
		}
		finally
		{
			FileUtil.closeAll(in);
		}
	}

	public static <A extends Appendable> A appendNullCondition (final A sb, final boolean isNull) throws IOException
	{
		if (null == sb)
			throw new IOException("appendNullCondition(" + isNull + ") no " + Appendable.class.getSimpleName() + " instance");

		sb.append(' ').append(SQLKeyword.IS.getKeyword());
		if (!isNull)
			sb.append(' ').append(SQLKeyword.NOT.getKeyword());
		sb.append(' ').append(SQLKeyword.NULL.getKeyword());
		return sb;
	}

	public static <A extends Appendable> A appendNullCondition (final A sb, final StringComparison cmpOp) throws IOException
	{
		if (StringComparison.MATCHES.equals(cmpOp))
			return appendNullCondition(sb, true);
		else if (StringComparison.DIFFS.equals(cmpOp))
			return appendNullCondition(sb, false);
		else
			throw new IOException("appendNullCondition(" + cmpOp + ") unknwon operator");
	}

	public static <A extends Appendable> A appendNullCondition (final A sb, final ComparableOperator cmpOp) throws IOException
	{
		if (ComparableOperator.EQ.equals(cmpOp))
			return SQLUtils.appendNullCondition(sb, true);
		else if (ComparableOperator.NE.equals(cmpOp))
			return SQLUtils.appendNullCondition(sb, false);
		else
			throw new IOException("appendNullCondition(" + cmpOp + ") unknwon operator");
	}

	public static <A extends Appendable> A appendNullCondition (final A sb, final String prefix, final boolean isNull) throws IOException
	{
		if ((null == prefix) || (prefix.length() <= 0) || (null == sb))
			throw new IOException("appendNullCondition(" + prefix + "=" + isNull + ") no " + Appendable.class.getSimpleName() + "/prefix expression provided");

		sb.append(prefix);
		return appendNullCondition(sb, isNull);
	}

	public static final char	TEXT_ESCAPE_CHAR_SDELIM='[',
								TEXT_ESCAPE_CHAR_EDELIM=']',
								TEXT_LITERAL_DELIM='\'',
								TEXT_WIDLCARD_CHAR='%';

	public static final char[]	ESCAPED_STRING_LITERAL_CHARS={
			TEXT_LITERAL_DELIM, TEXT_WIDLCARD_CHAR, TEXT_ESCAPE_CHAR_SDELIM
		};
	public static final String	ESCAPED_STRING_LITERAL_VALS=
						new String(ESCAPED_STRING_LITERAL_CHARS);

	public static final boolean isEscapedTextLiteral (final char c)
	{
		return (ESCAPED_STRING_LITERAL_VALS.indexOf(c) >= 0);
	}

	public static final String adjustQueryTextLiteral (final String s)
	{
		final int		sLen=(null == s) ? 0 : s.length();
		StringBuilder	sb=null;
		int				curPos=0;
		for (int	nextPos=curPos; nextPos < sLen; nextPos++)
		{
			final char	ch=s.charAt(nextPos);
			if (!isEscapedTextLiteral(ch))
				continue;

			if (null == sb)
				sb = new StringBuilder(sLen + 4);

			if (nextPos > curPos)
			{
				final String	cs=s.substring(curPos, nextPos);
				sb.append(cs);
			}

			if (ch == TEXT_LITERAL_DELIM)
			{
				sb.append(ch)
				  .append(ch)
				  ;
			}
			else
				sb.append(TEXT_ESCAPE_CHAR_SDELIM)
				  .append(ch)
				  .append(TEXT_ESCAPE_CHAR_EDELIM)
				  ;
			curPos = nextPos + 1;
		}

		if (null == sb)	// OK if nothing replaced
			return s;

		if (curPos < sLen)
		{
			final String	cs=s.substring(curPos);
			sb.append(cs);
		}

		return sb.toString();
	}

	public static final <A extends Appendable> A appendStringLiteral (final A sb, final CharSequence cs) throws IOException
	{
		if (null == sb)
			throw new IOException("appendStringLiteral(" + cs + ") no " + Appendable.class.getSimpleName() + " instance");

		final int	csLen=(null == cs) ? 0 : cs.length();
		for (int curPos=0; curPos < csLen; curPos++)
		{
			final char	ch=cs.charAt(curPos);
			if (isEscapedTextLiteral(ch))
			{
				if (ch == TEXT_LITERAL_DELIM)
					sb.append(ch).append(ch);
				else
					sb.append(TEXT_ESCAPE_CHAR_SDELIM).append(ch).append(TEXT_ESCAPE_CHAR_EDELIM);
			}
			else
				sb.append(ch);
		}

		return sb;
	}

	public static final <A extends Appendable> A appendStringCondition (final A sb, final StringComparison cmpOp, final String val) throws IOException
	{
		if ((null == sb) || (null == cmpOp))
			throw new IOException("appendStringCondition(" + cmpOp + "[" + val + "]) no " + Appendable.class.getSimpleName() + "/comparison instance");
		
		sb.append(' ');
		switch(cmpOp)
		{
			case CONTAINS	:
				sb.append(SQLKeyword.LIKE.getKeyword())
				  .append(' ')
				  .append(TEXT_LITERAL_DELIM)
				  .append(TEXT_WIDLCARD_CHAR)
				  ;
				appendStringLiteral(sb, val);
				sb.append(TEXT_WIDLCARD_CHAR);
				break;

			case DIFFS		:
				sb.append("<>")
				  .append(' ')
				  .append(TEXT_LITERAL_DELIM);
				appendStringLiteral(sb, val);
				break;

			case ENDS		:
				sb.append(SQLKeyword.LIKE.getKeyword())
				  .append(' ')
				  .append(TEXT_LITERAL_DELIM)
				  .append(TEXT_WIDLCARD_CHAR)
				  ;
				appendStringLiteral(sb, val);
				break;

			case MATCHES	:
				sb.append('=')
				  .append(' ')
				  .append(TEXT_LITERAL_DELIM);
				appendStringLiteral(sb, val);
				break;

			case STARTS		:
				sb.append(SQLKeyword.LIKE.getKeyword())
				  .append(' ')
				  .append(TEXT_LITERAL_DELIM)
				  ;
				appendStringLiteral(sb, val);
				sb.append(TEXT_WIDLCARD_CHAR);
				break;

			default			:
				throw new IOException("appendStringCondition(" + cmpOp + "[" + val + "]) unknown operator");
		}

		sb.append(TEXT_LITERAL_DELIM);
		return sb;
	}

	public static final <A extends Appendable> A appendStringCondition (final A sb, final String prefix, final StringComparison cmpOp, final String val) throws IOException
	{
		if ((null == sb) || (null == prefix) || (prefix.length() <= 0) || (null == cmpOp))
			throw new IOException("appendStringCondition(" + prefix + cmpOp + "[" + val + "]) no " + Appendable.class.getSimpleName() + "/comparison instance");

		sb.append(prefix);
		return appendStringCondition(sb, cmpOp, val);
	}

	public static final <A extends Appendable,C extends Comparable<C>> A appendComparableCriterion (
			final A sb, final ComparableOperator op, final C obj) throws IOException
	{
		final String	val=(null == obj) ? null : obj.toString();
		if ((null == op) || (null == val) || (val.length() <= 0) || (null == sb))
			throw new IOException("appendComparableCriterion(" + op + "-" + val + ") incomplete specification");

		sb.append(' ');
		switch(op)
		{
			case EQ	: sb.append('='); 	break;
			case NE	: sb.append("<>"); 	break;
			case LT	: sb.append('<');	break;
			case LE	: sb.append("<=");	break;
			case GT	: sb.append('>');	break;
			case GE	: sb.append(">=");	break;
			default	:
				throw new IOException("appendComparableCriterion(" + op + "-" + val + ") invalid opcode");
		}

		sb.append(' ').append(val);
		return sb;
	}

	public static final <A extends Appendable,C extends Comparable<C>> A 
		appendComparableCriterion (final A sb, final String name, final ComparableOperator op, final C obj) throws IOException
	{
		if ((null == op) || (null == name) || (name.length() <= 0) || (null == sb))
			throw new IOException("appendComparableCriterion(" + name + "-" + op + "-" + obj + ") incomplete specification");

		sb.append(name);
		return appendComparableCriterion(sb, op, obj);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final <A extends Appendable> A appendNumberCriterion (
			final A sb, final ComparableOperator op, final Number obj) throws IOException
	{
		if (obj instanceof Comparable)
		{
			appendComparableCriterion(sb, op, (Comparable) obj);
			return sb;
		}

		throw new StreamCorruptedException("appendNumberCriterion(" + op + "-" + obj + ") not comparable (class=" + ((null == obj) ? null : obj.getClass().getName()) + ")");
	}
	
	public static final <A extends Appendable> A appendNumberCriterion (
			final A sb, final String name, final ComparableOperator op, final Number obj) throws IOException
	{
		if ((null == op) || (null == name) || (name.length() <= 0) || (null == sb))
			throw new IOException("appendNumberCriterion(" + name + "-" + op + "-" + obj + ") incomplete specification");

		sb.append(name);
		return appendNumberCriterion(sb, op, obj);
	}
	// open-ended - either end can be null (but not both)
	public static final <A extends Appendable> A appendNumberRangeCriterion (
			final A sb, final Number lo, final Number hi) throws IOException
	{
		if ((null == sb) || ((null == lo) && (null == hi)))
			throw new IOException("appendNumberRangeCriterion(" + lo + "-" + hi + ") incomplete specification");

		if (null == hi)
			return appendNumberCriterion(sb, ComparableOperator.GE, lo);
		else if (null == lo)
			return appendNumberCriterion(sb, ComparableOperator.LE, hi);

		sb.append(' ')
		  .append(SQLKeyword.BETWEEN.getKeyword())
		  .append(' ')
		  .append(lo.toString())
		  .append(' ')
		  .append(SQLKeyword.AND.getKeyword())
		  .append(' ')
		  .append(hi.toString())
		  ;
		return sb;
	}

	// open-ended - either end can be null (but not both)
	public static final <A extends Appendable> A appendNumberRangeCriterion (
			final A sb, final String prefix, final Number lo, final Number hi) throws IOException
	{
		if ((null == sb) || (null == prefix) || (prefix.length() <= 0))
			throw new IOException("appendNumberRangeCriterion(" + prefix + "[" + lo + "-" + hi + "]) incomplete specification");

		sb.append(prefix);
		return appendNumberRangeCriterion(sb, lo, hi);
	}

	public static final <A extends Appendable,E extends Enum<E>> A appendEnumValue (
			final A sb, final boolean matchEquals, final E val) throws IOException
	{
		return appendStringCondition(sb, matchEquals ? StringComparison.MATCHES : StringComparison.DIFFS, (null == val) ? null : val.toString());
	}

	public static final <A extends Appendable,E extends Enum<E>> A appendEnumValue (
			final A sb, final String prefix, final boolean matchEquals, final E val) throws IOException
	{
		if ((null == sb) || (null == prefix) || (prefix.length() <= 0) || (null == val))
			throw new IOException("appendEnumValue(" + prefix + "/" + val + ") incomplete specification");

		sb.append(prefix);
		return appendEnumValue(sb, matchEquals, val);
	}
	/**
	 * @param <V> Type of object being extracted
	 * @param rs The {@link ResultSet} to use
	 * @param columnIndex The column index for extracting the values 
	 * @param valsClass The expected object type {@link Class}
	 * @return A {@link List} of all non-null the extracted values from the
	 * specified column of the result set - may be null/empty if no results
	 * or no non-null values
	 * @throws SQLException If DB error
	 */
	public static final <V> List<V> extractSingleColumnValuesList (
			final ResultSet rs, final int columnIndex, final Class<V> valsClass)
		throws SQLException
	{
		if (null == rs)
			throw new SQLException("No " + ResultSet.class.getSimpleName() + " instance");
		if (null == valsClass)
			throw new SQLException("No values class provided");

		final ResultSetColumnDataType	rse=
			ResultSetColumnDataType.DEFAULT_MAP.get(valsClass);
		if (null == rse)
			throw new SQLException("No " + ResultSetColumnDataType.class.getSimpleName() + " for value type=" + valsClass.getName());

		final int	iSize=rs.getFetchSize();
    	List<V>		result=null;
    	while (rs.next())
    	{
    		final Object	o=rse.getData(rs, columnIndex);
    		if (null == o)
    			continue;
    		
    		if (null == result)
    			result = new ArrayList<V>(Math.max(iSize, 10));

    		if (!result.add(valsClass.cast(o)))
    			continue;	// debug breakpoint
    	}

    	return result;
	}
	/**
	 * @param <V> Type of object being extracted
	 * @param rs The {@link ResultSet} to use
	 * @param valsClass The expected object type {@link Class}
	 * @return A {@link List} of all non-null the extracted values from the
	 * <U>first</U> column of the result set - may be null/empty if no results
	 * or no non-null values
	 * @throws SQLException If DB error
	 * @see #extractSingleColumnValuesList(ResultSet, int, Class)
	 */
	public static final <V> List<V> extractSingleValuesList (
			final ResultSet rs, final Class<V> valsClass)
		throws SQLException
	{
		return extractSingleColumnValuesList(rs, 1, valsClass);
	}
	/**
	 * @param <V> Type of object being extracted
	 * @param rs The {@link ResultSet} to use
	 * @param columnIndex The column index for extracting the values 
	 * @param vsi The {@link ValueStringInstantiator} to be used to convert
	 * the {@link String} column value(s) to objects
	 * @return A {@link List} of all non-null the extracted values from the
	 * specified column of the result set - may be null/empty if no results
	 * or no non-null values
	 * @throws SQLException If DB error
	 * @see ResultSet#getString(int)
	 * @see ValueStringInstantiator#newInstance(String)
	 */
	public static final <V> List<V> extractSingleColumnValuesList (
			final ResultSet rs, final int columnIndex, final ValueStringInstantiator<? extends V> vsi)
		throws SQLException
	{
		if (null == rs)
			throw new SQLException("No " + ResultSet.class.getSimpleName() + " instance");
		if (null == vsi)
			throw new SQLException("No " + ValueStringInstantiator.class.getSimpleName() + " instance");

		final int	iSize=rs.getFetchSize();
    	List<V>		result=null;
    	while (rs.next())
    	{
    		final String	s=rs.getString(columnIndex);
    		final V			o;
    		try
    		{
    			if (null == (o=vsi.newInstance(s)))
    				continue;
    		}
    		catch(Exception e)
    		{
    			throw new SQLException(e.getClass().getName() + " while convert value=" + s + ": " + e.getMessage(), e);
    		}

    		if (null == result)
    			result = new ArrayList<V>(Math.max(iSize, 10));

    		if (!result.add(o))
    			continue;	// debug breakpoint
    	}

    	return result;
	}
	/**
	 * @param <V> Type of object being extracted
	 * @param rs The {@link ResultSet} to use
	 * @param vsi The {@link ValueStringInstantiator} to be used to convert
	 * the {@link String} column value(s) to objects
	 * @return A {@link List} of all non-null the extracted values from the
	 * <U>first</U> column of the result set - may be null/empty if no results
	 * or no non-null values
	 * @throws SQLException If DB error
	 * @see #extractSingleColumnValuesList(ResultSet, int, ValueStringInstantiator)
	 */
	public static final <V> List<V> extractSingleValuesList (
			final ResultSet rs, final ValueStringInstantiator<? extends V> vsi)
		throws SQLException
	{
		return extractSingleColumnValuesList(rs, 1, vsi);
	}
	/**
	 * @param <K> Key type
	 * @param <V> Value type
	 * @param rs The {@link ResultSet} to use
	 * @param keyColIndex Key column index
	 * @param keyClass The expected key type {@link Class}
	 * @param valColIndex Value column index
	 * @param valsClass The expected object type {@link Class}
	 * @param ignoreDuplicates FALSE=throw {@link SQLException} if same key repeated
	 * @return A {@link Map} of all "pairs" in the result set extracted from the
	 * specified key/value column index
	 * @throws SQLException If DB error
	 */
	public static final <K,V> Map<K,V> extractMappedColumnsPairs (
												final ResultSet 	rs,
												final int			keyColIndex,
												final Class<K>		keyClass,
												final int			valColIndex,
												final Class<V>		valsClass,
												final boolean		ignoreDuplicates)
		throws SQLException
	{
		if (null == rs)
			throw new SQLException("No " + ResultSet.class.getSimpleName() + " instance");
		if (null == keyClass)
			throw new SQLException("No key class provided");
		if (null == valsClass)
			throw new SQLException("No values class provided");

		final ResultSetColumnDataType	rseKey=
			ResultSetColumnDataType.DEFAULT_MAP.get(keyClass);
		if (null == rseKey)
			throw new SQLException("No " + ResultSetColumnDataType.class.getSimpleName() + " for key type=" + keyClass.getName());

		final ResultSetColumnDataType	rseValue=
			ResultSetColumnDataType.DEFAULT_MAP.get(valsClass);
		if (null == rseValue)
			throw new SQLException("No " + ResultSetColumnDataType.class.getSimpleName() + " for value type=" + valsClass.getName());

		Map<K,V>	ret=null;
		while (rs.next())
		{
			final Object	key=rseKey.getData(rs, keyColIndex),
							value=rseValue.getData(rs, valColIndex);
			if (null == key)
			{
				if (value != null)
					throw new SQLException("Non-null value for null key: " + value);
				continue;
			}
			
			if (null == value)
				continue;

			if (null == ret)
			{
				if (!Comparable.class.isAssignableFrom(keyClass))
				{
					final int	iSize=rs.getFetchSize();
					ret = new HashMap<K,V>(Math.max(iSize, 50), 1.0f);
				}
				else
					ret = new TreeMap<K,V>();
			}

			final V	prev=ret.put(keyClass.cast(key), valsClass.cast(value));
			if (prev != null)
			{
				if (ignoreDuplicates)
					continue;

				throw new SQLException("Multiple values found for key=" + key + ": " + prev + "/" + value);
			}
		}

		return ret;

	}
	/**
	 * @param <K> Key type
	 * @param <V> Value type
	 * @param rs The {@link ResultSet} to use
	 * @param keyClass The expected key type {@link Class}
	 * @param valsClass The expected object type {@link Class}
	 * @param ignoreDuplicates FALSE=throw {@link SQLException} if same key repeated
	 * @return A {@link Map} of all "pairs" in the result set extracted from the
	 * specified key/value columns 1/2 respectively
	 * @throws SQLException If DB error
	 * @see #extractMappedColumnsPairs(ResultSet, int, Class, int, Class, boolean)
	 */
	public static final <K,V> Map<K,V> extractMappedPairs (
			final ResultSet 	rs,
			final Class<K>		keyClass,
			final Class<V>		valsClass,
			final boolean		ignoreDuplicates)
		throws SQLException
	{
		return extractMappedColumnsPairs(rs, 1, keyClass, 2, valsClass, ignoreDuplicates);
	}
}
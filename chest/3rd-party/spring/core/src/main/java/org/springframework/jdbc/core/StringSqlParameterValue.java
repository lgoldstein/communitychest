/*
 * 
 */
package org.springframework.jdbc.core;

import java.sql.Types;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * @author Lyor G.
 * @since Dec 18, 2011 9:18:01 AM
 */
public class StringSqlParameterValue extends CommonSqlParameterValue {
	public StringSqlParameterValue (Enum<?> value)
	{
		this((value != null) ? value.name() : null);
	}

	public StringSqlParameterValue (Object value)
	{
		this((value != null) ? value.toString() : null);
	}

	public StringSqlParameterValue (String value)
	{
		super(Types.VARCHAR, value);
	}

	public static final StringSqlParameterValue addOptionalParameter (
			final Map<String,Object>	valsMap,
			final String				name,
			final Object				value)
	{
		Assert.notNull(valsMap, "No values map provided");
		if (value == null)
			return null;

		final StringSqlParameterValue	result;
		if (value instanceof String)
			result = new StringSqlParameterValue((String) value);
		else if (value instanceof Enum<?>)
			result = new StringSqlParameterValue((Enum<?>) value);
		else
			result = new StringSqlParameterValue(value);

		if (valsMap.put(name, result) != null)
			throw new IllegalStateException("Duplicate optional parameter: " + name);
		return result;
	}
}

/*
 * 
 */
package net.community.chest.spring.jdbc.core.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.db.sql.ResultSetColumnDataType;

import org.springframework.jdbc.core.RowMapper;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * A simple {@link RowMapper} implementation that assumes all rows contain
 * a value of the same type as their 1st (and only) column
 * 
 * @param <V> Type of value being mapped
 * @author Lyor G.
 * @since Apr 27, 2010 9:14:06 AM
 */
public class TypedSingleValueRowMapper<V>
			extends BaseTypedValuesContainer<V>
			implements RowMapper<V> {

	private final ResultSetColumnDataType	_dataType;
	public final ResultSetColumnDataType getColumnDataType ()
	{
		return _dataType;
	}

	public TypedSingleValueRowMapper (Class<V> valType, ResultSetColumnDataType colType)
	{
		super(valType);
		
		if (null == (_dataType=colType))
			throw new IllegalArgumentException("No " + ResultSetColumnDataType.class.getSimpleName() + " provided");

		final Class<?>	dtClass=colType.getValuesClass();
		if (!valType.isAssignableFrom(dtClass))
			throw new ClassCastException("Mismatched value (" + valType.getName() + ") and column type (" + dtClass.getName() + ")");
	}
	/*
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	@Override
	public V mapRow (final ResultSet rs, final int rowNum) throws SQLException
	{
		if (null == rs)
			throw new SQLException("No " + ResultSet.class.getSimpleName() + " instance for row=" + rowNum);

		final ResultSetColumnDataType	colType=getColumnDataType();
		final Object					result=colType.getData(rs, 1);
		if (null == result)
			return null;	// debug breakpoint

		final Class<V>	vClass=getValuesClass();
		return vClass.cast(result);
	}
}

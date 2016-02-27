/*
 * 
 */
package net.community.chest.spring.jdbc.core.mapper;

import net.community.chest.db.sql.ResultSetColumnDataType;

import org.springframework.jdbc.core.RowMapper;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * <P>A simple {@link RowMapper} implementation for queries that
 * return a single {@link Long} value (like id) per-row</P>
 * 
 * @author Lyor G.
 * @since Apr 27, 2010 9:00:58 AM
 */
public class SingleLongRowMapper extends TypedSingleValueRowMapper<Long> {
	public SingleLongRowMapper ()
	{
		super(Long.class, ResultSetColumnDataType.LONG);
	}
	/**
	 * A re-entrant default instance to be used instead of allocating a new one
	 */
	public static final SingleLongRowMapper	DEFAULT=new SingleLongRowMapper();
}

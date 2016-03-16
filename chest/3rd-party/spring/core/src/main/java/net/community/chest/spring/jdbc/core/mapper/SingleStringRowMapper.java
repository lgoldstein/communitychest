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
 * return a single {@link String} value per-row</P>
 *
 * @author Lyor G.
 * @since Apr 27, 2010 9:11:50 AM
 */
public class SingleStringRowMapper extends TypedSingleValueRowMapper<String> {
    public SingleStringRowMapper ()
    {
        super(String.class, ResultSetColumnDataType.STRING);
    }

    public static final SingleStringRowMapper    DEFAULT=new SingleStringRowMapper();
}

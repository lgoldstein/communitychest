/*
 * 
 */
package net.community.chest.spring.jdbc.core.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * <P>Assumes that the provided {@link ResultSet} contains a {@link Long}
 * as its 1st column. <B>Note:</B> returns <code>null</code> if {@link ResultSet#next()}
 * returns <code>false</code>.</P>
 * 
 * @author Lyor G.
 * @since Apr 27, 2010 9:03:39 AM
 */
public class SingleLongResultExtractor implements ResultSetExtractor<Long> {
	public SingleLongResultExtractor ()
	{
		super();
	}
	/*
	 * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
	 */
	@Override
	public Long extractData (final ResultSet rs)
		throws SQLException, DataAccessException
	{
		if (null == rs)
			throw new SQLException("No " + ResultSet.class.getSimpleName() + " instance");

		if (rs.next())
			return Long.valueOf(rs.getLong(1));				
		else
			return null;
	}

	public static final SingleLongResultExtractor	DEFAULT=new SingleLongResultExtractor();
}

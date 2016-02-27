/*
 * 
 */
package net.community.chest.spring.jdbc.core.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import net.community.chest.db.sql.SQLUtils;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 * 
 * Assumes the {@link ResultSet} contains &quot;pairs&quot; of {@link Long}
 * values (e.g., entity ID + associated count). The result is a {@link Map}
 * whose key=the 1st column value and value=the 2nd column value. <B>Note:</B>
 * a {@link DataRetrievalFailureException} is thrown if a key is repeated.
 * 
 * @author Lyor G.
 * @since Apr 27, 2010 9:08:49 AM
 */
public class LongPairsMapResultExtractor implements ResultSetExtractor<Map<Long,Long>> {
	public LongPairsMapResultExtractor ()
	{
		super();
	}
	/*
	 * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
	 */
	@Override
	public Map<Long,Long> extractData (final ResultSet rs)
		throws SQLException, DataAccessException
	{
		return SQLUtils.extractMappedPairs(rs, Long.class, Long.class, false);
	}

	public static final LongPairsMapResultExtractor	DEFAULT=new LongPairsMapResultExtractor();
}

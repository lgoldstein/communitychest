/*
 *
 */
package net.community.chest.spring.jdbc.core.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import net.community.chest.db.sql.SQLUtils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * <P>Assumes that the {@link ResultSet} is a {@link List} of {@link String}-s</P>
 *
 * @author Lyor G.
 * @since Apr 27, 2010 9:05:46 AM
 */
public class StringListResultExtractor implements ResultSetExtractor<List<String>> {
    public StringListResultExtractor ()
    {
        super();
    }
    /*
     * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
     */
    @Override
    public List<String> extractData (final ResultSet rs)
        throws SQLException, DataAccessException
    {
        return SQLUtils.extractSingleValuesList(rs, String.class);
    }

    public static final StringListResultExtractor    DEFAULT=new StringListResultExtractor();
}

/*
 *
 */
package net.community.chest.spring.jdbc.core.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.db.sql.SQLUtils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * @param <V> Type of object being extracted
 * @author Lyor G.
 * @since Jul 19, 2010 2:04:08 PM
 */
public class ValueListStringInstantiatorResultExtractor<V>
        extends BaseTypedValuesContainer<V>
        implements ResultSetExtractor<List<V>> {
    private ValueStringInstantiator<? extends V> _vsi;
    public final ValueStringInstantiator<? extends V> getInstantiator ()
    {
        return _vsi;
    }

    @SuppressWarnings("unchecked")
    public ValueListStringInstantiatorResultExtractor (ValueStringInstantiator<? extends V> vsi)
            throws IllegalArgumentException
    {
        super((null == vsi) ? null : (Class<V>) vsi.getValuesClass());

        if (null == (_vsi=vsi))
            throw new IllegalArgumentException("No instantiator specified");
    }
    /*
     * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
     */
    @Override
    public List<V> extractData (ResultSet rs) throws SQLException, DataAccessException
    {
        return SQLUtils.extractSingleValuesList(rs, getInstantiator());
    }
}

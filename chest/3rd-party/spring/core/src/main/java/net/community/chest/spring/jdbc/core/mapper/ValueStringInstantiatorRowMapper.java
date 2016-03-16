/*
 *
 */
package net.community.chest.spring.jdbc.core.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.convert.ValueStringInstantiator;

import org.springframework.jdbc.core.RowMapper;

/**
 * @param <V> Type of value being mapped
 * @author Lyor G.
 * @since Jul 19, 2010 1:51:10 PM
 */
public class ValueStringInstantiatorRowMapper<V>
            extends BaseTypedValuesContainer<V>
            implements RowMapper<V> {

    private ValueStringInstantiator<? extends V> _vsi;
    public final ValueStringInstantiator<? extends V> getInstantiator ()
    {
        return _vsi;
    }

    @SuppressWarnings("unchecked")
    public ValueStringInstantiatorRowMapper (ValueStringInstantiator<? extends V> vsi)
    {
        super((null == vsi) ? null : (Class<V>) vsi.getValuesClass());

        if (null == (_vsi=vsi))
            throw new IllegalArgumentException("No instantiator specified");
    }
    /*
     * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
     */
    @Override
    public V mapRow (ResultSet rs, int rowNum) throws SQLException
    {
        if (null == rs)
            throw new SQLException("No " + ResultSet.class.getSimpleName() + " instance for row=" + rowNum);

        final ValueStringInstantiator<? extends V>    vsi=getInstantiator();
        if (null == vsi)
            throw new SQLException("No " + ValueStringInstantiator.class.getSimpleName() + " instance for row=" + rowNum);

        final String    s=rs.getString(1);
        try
        {
            return vsi.newInstance(s);
        }
        catch(Exception e)
        {
            throw new SQLException(e.getClass().getName() + " while convert row=" + rowNum + " value=" + s + ": " + e.getMessage(), e);
        }
    }
}

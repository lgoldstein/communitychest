/*
 *
 */
package org.springframework.jdbc.core;

/**
 * @author Lyor G.
 * @since Nov 27, 2011 12:07:26 PM
 */
public abstract class CommonSqlParameterValue extends SqlParameterValue {
    protected CommonSqlParameterValue (int sqlType, Object value)
    {
        super(sqlType, value);
    }

    protected CommonSqlParameterValue (SqlParameter declaredParam, Object value)
    {
        super(declaredParam, value);
    }

    protected CommonSqlParameterValue (int sqlType, String typeName, Object value)
    {
        super(sqlType, typeName, value);
    }

    protected CommonSqlParameterValue (int sqlType, int scale, Object value)
    {
        super(sqlType, scale, value);
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return getClass().getSimpleName() + "[" + getValue() + "]";
    }

}

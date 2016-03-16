/*
 *
 */
package org.springframework.jdbc.core;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * @author Lyor G.
 * @since Nov 13, 2011 11:24:35 AM
 */
public class TimestampSqlParameterValue extends CommonSqlParameterValue {
    public TimestampSqlParameterValue (Date date)
    {
        this(date.getTime());
    }

    public TimestampSqlParameterValue (long value)
    {
        super(Types.TIMESTAMP, new Timestamp(value));
    }

    public static final TimestampSqlParameterValue addOptionalParameter (
            final Map<String,Object>    valsMap,
            final String                name,
            final Date                    value)
    {
        Assert.notNull(valsMap, "No values map provided");
        if (value == null)
            return null;

        final TimestampSqlParameterValue    result=new TimestampSqlParameterValue(value);
        if (valsMap.put(name, result) != null)
            throw new IllegalStateException("Duplicate optional parameter: " + name);
        return result;
    }
}

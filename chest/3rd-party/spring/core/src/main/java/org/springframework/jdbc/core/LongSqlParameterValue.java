/*
 *
 */
package org.springframework.jdbc.core;

import java.sql.Types;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * @author Lyor G.
 * @since Nov 13, 2011 11:28:48 AM
 */
public class LongSqlParameterValue extends CommonSqlParameterValue {
    public LongSqlParameterValue (long value)
    {
        this(Long.valueOf(value));
    }

    public LongSqlParameterValue (Long value)
    {
        super(Types.BIGINT, value);
    }

    public static final LongSqlParameterValue addOptionalParameter (
            final Map<String,Object>    valsMap,
            final String                name,
            final Long                    value)
    {
        Assert.notNull(valsMap, "No values map provided");
        if (value == null)
            return null;

        final LongSqlParameterValue    result=new LongSqlParameterValue(value);
        if (valsMap.put(name, result) != null)
            throw new IllegalStateException("Duplicate optional parameter: " + name);
        return result;
    }
}

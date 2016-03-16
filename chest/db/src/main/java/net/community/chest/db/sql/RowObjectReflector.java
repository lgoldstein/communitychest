package net.community.chest.db.sql;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Uses reflection API in order to update an object by using the result
 * row data column name to be same as the attribute (default).</P>
 *
 * @param <V> The expected row object class
 * @author Lyor G.
 * @since Apr 30, 2008 3:01:56 PM
 */
public class RowObjectReflector<V> extends BaseTypedValuesContainer<V> {
    public RowObjectReflector (Class<V> objClass) throws IllegalArgumentException
    {
        super(objClass);
    }

    private Map<String,AttributeAccessor>    _attrsMap    /* =null */;
    public synchronized Map<String,AttributeAccessor> getObjectAttributes ()
    {
        if (null == _attrsMap)
            _attrsMap = AttributeMethodType.getAllAccessibleAttributes(getValuesClass());
        return _attrsMap;
    }
    /**
     * @param colName DB column name
     * @return The {@link Method} to use for setting the object value.
     * <B>Note:</B> may be null if no match found (instead of exception)
     * Default=find a setter with same 'pure' name (case <U>insensitive</U>)
     * @throws Exception if failed to use internal reflection API(s)
     */
    public Method getColumnSetter (final String colName) throws Exception
    {
        final Map<String,? extends AttributeAccessor>    attrsMap=
            ((null == colName) || (colName.length() <= 0)) ? null : getObjectAttributes();
        final AttributeAccessor                            aa=
            ((null == attrsMap) || (attrsMap.size() <= 0)) ? null : attrsMap.get(colName);
        return (null == aa) ? null : aa.getSetter();
    }

    public Object getColumnValue (V obj, String colName, Method m, ResultSetRowColumnData rsd) throws Exception
    {
        if ((null == obj) || (null == m))
            throw new IllegalArgumentException("getColumnValue(" + colName + ") incomplete arguments");

        return (null == rsd) ? null : rsd.getColumnValue();
    }

    public V fromResultSetRowData (V obj, Collection<? extends ResultSetRowColumnData>    cl) throws Exception
    {
        if ((null == cl) || (cl.size() <= 0))
            return obj;

        for (final ResultSetRowColumnData rsd : cl)
        {
            if (null == rsd)    // should not happen
                continue;

            final String    colName=rsd.getColumnName();
            final Method    m=getColumnSetter(colName);
            final Object    arg=getColumnValue(obj, colName, m, rsd);
            m.invoke(obj, arg);
        }

        return obj;
    }

    public V getRowInstance (@SuppressWarnings("unused") Collection<? extends ResultSetRowColumnData> cl) throws Exception
    {
        return getValuesClass().newInstance();
    }

    public V fromResultSetRowData (Collection<? extends ResultSetRowColumnData>    cl) throws Exception
    {
        final V    v=getRowInstance(cl);
        return fromResultSetRowData(v, cl);
    }
}

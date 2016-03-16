/*
 *
 */
package net.community.chest.util;

import java.util.Map;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>Provides a {@link ParametersHolder} implementation using a {@link Map} as its
 * backing holder. <B>Note:</B> the implementation of {@link #getParameter(String, String)}
 * retrieves the value's {@link Object#toString()} as the parameter's string representation</P>
 * @param <K> The mapped key type
 * @param <V> The mapped value type
 * @param <M> The used {@link Map} type
 * @author Lyor G.
 * @since Nov 24, 2010 9:11:53 AM
 */
public class MappedParametersHolder<K,V, M extends Map<K,V>> extends AbstractParametersHolder {
    private Map<K,V>    _paramsMap;
    public Map<K,V> getParametersMap ()
    {
        return _paramsMap;
    }

    public void setParametersMap (Map<K,V> m)
    {
        _paramsMap = m;
    }

    public MappedParametersHolder (Map<K,V> m)
    {
        _paramsMap = m;
    }

    public MappedParametersHolder ()
    {
        this(null);
    }
    /*
     * @see net.community.chest.util.ParametersHolder#getParameter(java.lang.String, java.lang.String)
     */
    @Override
    public String getParameter (String paramName, String defValue)
    {
        final Map<?,?>    m=getParametersMap();
        if ((m == null) || m.isEmpty()
         || (paramName == null) || (paramName.length() <= 0))
            return defValue;

        final Object    o=m.get(paramName);
        final String    s=(o == null) ? null : o.toString();
        if ((s == null) || (s.length() <= 0))
            return defValue;

        return s;
    }
}

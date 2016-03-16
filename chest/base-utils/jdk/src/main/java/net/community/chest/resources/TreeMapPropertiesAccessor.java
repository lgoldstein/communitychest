/*
 *
 */
package net.community.chest.resources;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Implements {@link PropertyAccessor} using a {@link TreeMap}</P>
 *
 * @param <K> Map key type
 * @param <V> Map value type
 * @author Lyor G.
 * @since Jul 9, 2009 1:31:38 PM
 */
public class TreeMapPropertiesAccessor<K,V> extends TreeMap<K,V> implements PropertyAccessor<K,V> {
    /**
     *
     */
    private static final long serialVersionUID = 3699500841875396882L;
    public TreeMapPropertiesAccessor ()
    {
        super();
    }

    public TreeMapPropertiesAccessor (Comparator<? super K> comparator)
    {
        super(comparator);
    }

    public TreeMapPropertiesAccessor (Map<? extends K,? extends V> m)
    {
        super(m);
    }

    public TreeMapPropertiesAccessor (SortedMap<K,? extends V> m)
    {
        super(m);
    }
    /*
     * @see net.community.chest.resources.PropertyAccessor#getProperty(java.lang.Object)
     */
    @Override
    public V getProperty (K key)
    {
        if (null == key)
            return null;
        return get(key);
    }
}

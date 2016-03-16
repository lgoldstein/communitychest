/*
 *
 */
package net.community.chest.util.set;

import java.util.Collection;
import java.util.TreeSet;

import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.util.compare.InstancesComparator;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <V> Type of object being stored in the set
 * @author Lyor G.
 * @since Apr 13, 2009 10:03:00 AM
 */
public class UniqueInstanceSet<V> extends TreeSet<V> implements TypedValuesContainer<V> {
    /**
     *
     */
    private static final long serialVersionUID = 1669420982472753776L;
    private final Class<V>    _valsClass;
    /*
     * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
     */
    @Override
    public final Class<V> getValuesClass ()
    {
        return _valsClass;
    }

    @SuppressWarnings("unchecked")
    public UniqueInstanceSet (final InstancesComparator<? super V> c) throws IllegalArgumentException
    {
        super(c);

        if (null == (_valsClass=(null == c) ? null : (Class<V>) c.getValuesClass()))
            throw new IllegalArgumentException("No comparator/vaues class provided");
    }

    public UniqueInstanceSet (Class<V> vc, Collection<? extends V> c) throws IllegalArgumentException
    {
        this(new InstancesComparator<V>(vc));

        if ((c != null) && (c.size() > 0))
            addAll(c);
    }

    public UniqueInstanceSet (Class<V> vc) throws IllegalArgumentException
    {
        this(vc, null);
    }
}

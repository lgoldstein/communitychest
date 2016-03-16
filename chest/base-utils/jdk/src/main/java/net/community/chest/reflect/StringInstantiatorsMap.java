package net.community.chest.reflect;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.util.map.ClassNameMap;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>A {@link java.util.Map} implementation where key={@link Class} name
 * (case <U>insensitive</U>), value=associated {@link ValueStringInstantiator}
 * for that class</P>
 *
 * @author Lyor G.
 * @since Jul 11, 2007 10:02:20 AM
 */
public class StringInstantiatorsMap extends ClassNameMap<ValueStringInstantiator<?>> {
    /**
     *
     */
    private static final long serialVersionUID = -3659625983998703978L;
    public StringInstantiatorsMap ()
    {
        super(String.CASE_INSENSITIVE_ORDER);
    }

    private static final StringInstantiatorsMap    _default=new StringInstantiatorsMap();
    /**
     * @return a singleton default instance where all newly created
     * instantiators can be added. <B>Note:</B> all access to this {@link java.util.Map}
     * must be <U>synchronized</U>.
     */
    public static final StringInstantiatorsMap getDefaultInstance ()
    {
        return _default;
    }
}

/*
 *
 */
package net.community.chest.math.functions.eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.community.chest.resources.PropertyAccessor;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 27, 2009 3:30:39 PM
 */
public class PositionalArgumentPropertyAccessor
        extends ArrayList<Number>
        implements PropertyAccessor<String,Number> {
    /**
     *
     */
    private static final long serialVersionUID = -8479003120539033189L;
    public PositionalArgumentPropertyAccessor ()
    {
        super();
    }

    public PositionalArgumentPropertyAccessor (Collection<? extends Number> c)
    {
        super(c);
    }

    public PositionalArgumentPropertyAccessor (int initialCapacity)
    {
        super(initialCapacity);
    }

    public PositionalArgumentPropertyAccessor (Number ... nums)
    {
        this(((null == nums) || (nums.length <= 0)) ? 10 : nums.length);

        if ((nums != null) && (nums.length > 0))
            addAll(Arrays.asList(nums));
    }
    /*
     * @see net.community.chest.resources.PropertyAccessor#getProperty(java.lang.Object)
     */
    @Override
    public Number getProperty (String key)
    {
        if ((null == key) || (key.length() <= 0))
            return null;

        final Integer    nIndex=Integer.decode(key);
        return get(nIndex.intValue());
    }
}

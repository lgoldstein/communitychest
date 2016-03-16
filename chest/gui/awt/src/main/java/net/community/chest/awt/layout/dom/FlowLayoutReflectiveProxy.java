/*
 *
 */
package net.community.chest.awt.layout.dom;

import java.awt.FlowLayout;

import net.community.chest.awt.layout.FlowLayoutAlignmentValueStringInstantiator;
import net.community.chest.convert.ValueStringInstantiator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <L> The {@link FlowLayout} being reflected
 * @author Lyor G.
 * @since Aug 20, 2008 1:20:59 PM
 */
public class FlowLayoutReflectiveProxy<L extends FlowLayout> extends AbstractLayoutManagerReflectiveProxy<L> {
    public FlowLayoutReflectiveProxy (Class<L> objClass) throws IllegalArgumentException
    {
        super(objClass);
    }
    // special handling
    public static final String    ALIGNMENT_ATTR="alignment";
    /*
     * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
    {
        if (ALIGNMENT_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<C>) FlowLayoutAlignmentValueStringInstantiator.DEFAULT;

        return super.resolveAttributeInstantiator(name, type);
    }

    public static final  FlowLayoutReflectiveProxy<FlowLayout>    FLOW=
                new FlowLayoutReflectiveProxy<FlowLayout>(FlowLayout.class);
}

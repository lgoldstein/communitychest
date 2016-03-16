package net.community.chest.awt.frame;

import java.awt.Frame;

import net.community.chest.awt.dom.proxy.WindowReflectiveProxy;
import net.community.chest.convert.ValueStringInstantiator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The reflected {@link Frame} type
 * @author Lyor G.
 * @since Mar 20, 2008 10:12:45 AM
 */
public class FrameReflectiveProxy<F extends Frame> extends WindowReflectiveProxy<F> {
    public FrameReflectiveProxy (Class<F> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected FrameReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
            throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    // some attributes of interest
    public static final String    STATE_ATTR="State",
                                EXTENDED_STATE_ATTR="ExtendedState";
    /*
     * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
    {
        if (STATE_ATTR.equalsIgnoreCase(name)
          || EXTENDED_STATE_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<C>) FrameStateValueStringInstantiator.DEFAULT;

        return super.resolveAttributeInstantiator(name, type);
    }
}

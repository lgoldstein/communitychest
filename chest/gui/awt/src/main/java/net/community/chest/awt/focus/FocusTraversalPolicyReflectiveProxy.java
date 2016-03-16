/*
 *
 */
package net.community.chest.awt.focus;

import java.awt.FocusTraversalPolicy;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @param <P> Type of {@link FocusTraversalPolicy} being reflected
 * @author Lyor G.
 * @since Apr 4, 2010 12:09:58 PM
 */
public abstract class FocusTraversalPolicyReflectiveProxy<P extends FocusTraversalPolicy>
        extends UIReflectiveAttributesProxy<P> {
    protected FocusTraversalPolicyReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    protected FocusTraversalPolicyReflectiveProxy (Class<P> objClass)
    {
        this(objClass, true);
    }

    public static final FocusTraversalPolicyReflectiveProxy<FocusTraversalPolicy>    BY_NAME_POLICY=
            new FocusTraversalPolicyReflectiveProxy<FocusTraversalPolicy>(FocusTraversalPolicy.class, false) {
                /*
                 * @see net.community.chest.dom.proxy.AbstractXmlProxyConverter#createInstance(org.w3c.dom.Element)
                 */
                @Override
                public FocusTraversalPolicy createInstance (Element elem)
                        throws Exception
                {
                    if (null == elem)
                        return null;
                    return new ByNameFocusTraversalPolicy();
                }

        };
}

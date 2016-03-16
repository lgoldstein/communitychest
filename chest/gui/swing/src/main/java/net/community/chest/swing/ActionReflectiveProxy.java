/*
 *
 */
package net.community.chest.swing;

import javax.swing.Action;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <A> The reflected {@link Action}
 * @author Lyor G.
 * @since Sep 24, 2008 11:50:45 AM
 */
public abstract class ActionReflectiveProxy<A extends Action> extends UIReflectiveAttributesProxy<A> {
    protected ActionReflectiveProxy (Class<A> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected ActionReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
            throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    /**
     * Default XML element name used for actions.
     */
    public static final String    ACTION_ELEMNAME="action";
}

/*
 *
 */
package net.community.chest.swing.component.list;

import javax.swing.JList;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.swing.component.JComponentReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <O> The {@link JList} model
 * @param <L> The reflected {@link JList} instance
 * @author Lyor G.
 * @since Oct 7, 2008 8:38:19 AM
 */
public class JListReflectiveProxy<O,L extends JList<O>> extends JComponentReflectiveProxy<L> {
    public JListReflectiveProxy (Class<L> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected JListReflectiveProxy (Class<L> objClass, boolean registerAsDefault)
            throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    // some special attributes of interest
    public static final String    LAYOUT_ORIENTATION_ATTR="layoutOrientation",
                                SELECTION_MODE_ATTR="selectionMode";
    /*
     * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
    {
        if (LAYOUT_ORIENTATION_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<C>) ListLayoutOrientationValueStringInstantiator.DEFAULT;
        else if (SELECTION_MODE_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<C>) ListSelectionModeValueStringInstantiator.DEFAULT;

        return super.resolveAttributeInstantiator(name, type);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final JListReflectiveProxy    LIST=new JListReflectiveProxy(JList.class, true);
}

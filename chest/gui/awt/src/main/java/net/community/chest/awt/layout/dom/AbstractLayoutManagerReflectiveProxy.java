/*
 *
 */
package net.community.chest.awt.layout.dom;

import java.awt.Container;
import java.awt.LayoutManager;

import javax.swing.GroupLayout;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;
import net.community.chest.awt.layout.BaseBoxLayout;
import net.community.chest.awt.layout.border.BorderLayoutReflectiveProxy;
import net.community.chest.awt.layout.gridbag.GridBagLayoutReflectiveProxy;
import net.community.chest.dom.proxy.ReflectiveAttributesProxy;
import net.community.chest.dom.transform.XmlValueInstantiator;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <L> The {@link LayoutManager} being reflected
 * @author Lyor G.
 * @since Aug 20, 2008 1:05:38 PM
 */
public abstract class AbstractLayoutManagerReflectiveProxy<L extends LayoutManager> extends UIReflectiveAttributesProxy<L> {
    protected AbstractLayoutManagerReflectiveProxy (Class<L> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    protected AbstractLayoutManagerReflectiveProxy (Class<L> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }
    /**
     * Default element name for layouts
     */
    public static final String LAYOUT_ELEMNAME="layout";

    // available default values
    public static final String    BORDER_LAYOUT_VALUE="border",
                                FLOW_LAYOUT_VALUE="flow",
                                GRID_LAYOUT_VALUE="grid",
                                GRIDBAG_LAYOUT_VALUE="gridbag",
                                CARD_LAYOUT_VALUE="card",
                                /* NOTE !!! not implemented as an XML proxy */
                                BOX_LAYOUT_VALUE="box",
                                GROUP_LAYOUT_VALUE="group";

    public static XmlValueInstantiator<? extends LayoutManager> getLayoutConverter (final String lt)
    {
        if ((null == lt) || (lt.length() <= 0))
            return null;

        if (BORDER_LAYOUT_VALUE.equalsIgnoreCase(lt))
            return BorderLayoutReflectiveProxy.BORDER;
        else if (FLOW_LAYOUT_VALUE.equalsIgnoreCase(lt))
            return FlowLayoutReflectiveProxy.FLOW;
        else if (GRID_LAYOUT_VALUE.equalsIgnoreCase(lt))
            return GridLayoutReflectiveProxy.GRID;
        else if (GRIDBAG_LAYOUT_VALUE.equalsIgnoreCase(lt))
            return GridBagLayoutReflectiveProxy.GRIDBAG;
        else if (CARD_LAYOUT_VALUE.equalsIgnoreCase(lt))
            return CardLayoutReflectiveProxy.CARD;
        else if (GROUP_LAYOUT_VALUE.equalsIgnoreCase(lt))
            return GroupLayoutReflectiveProxy.GROUP;

        return null;
    }

    public static XmlValueInstantiator<? extends LayoutManager> getLayoutConverter (final Element elem)
    {
        final String    lt=
            (null == elem) ? null : elem.getAttribute(ReflectiveAttributesProxy.CLASS_ATTR);
        return getLayoutConverter(lt);
    }
    // special support for BoxLayout, GroupLayout and others
    public static final LayoutManager createLayoutManager (final Container c, final Element elem) throws Exception
    {
        final String    lt=(null == elem) ? null : elem.getAttribute(ReflectiveAttributesProxy.CLASS_ATTR);
        if (BOX_LAYOUT_VALUE.equalsIgnoreCase(lt))
            return new BaseBoxLayout(c, elem);
        else if (GROUP_LAYOUT_VALUE.equalsIgnoreCase(lt))
            return new GroupLayout(c);
        else
        {
            final XmlValueInstantiator<? extends LayoutManager>    proxy=getLayoutConverter(elem);
            return proxy.fromXml(elem);
        }
    }
}

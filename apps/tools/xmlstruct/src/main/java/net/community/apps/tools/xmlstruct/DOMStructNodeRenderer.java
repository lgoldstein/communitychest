/*
 *
 */
package net.community.apps.tools.xmlstruct;

import java.awt.Component;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.dom.NodeTypeEnum;
import net.community.chest.lang.EnumUtil;

import org.w3c.dom.Node;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 6, 2009 4:46:46 PM
 */
public class DOMStructNodeRenderer extends DefaultTreeCellRenderer {
    /**
     *
     */
    private static final long serialVersionUID = 2880814897307880986L;
    public DOMStructNodeRenderer ()
    {
        super();
    }
    /*
     * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    @Override
    public Component getTreeCellRendererComponent (JTree tree,
            Object value, boolean sel, boolean expanded, boolean leaf,
            int row, boolean isFocused)
    {
        final Component    c=super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, isFocused);
        if (value instanceof DocStructNode<?>)
        {
            @SuppressWarnings("unchecked")
            final Node            n=((DocStructNode<? extends Node>) value).getAssignedValue();
            final NodeTypeEnum    t=NodeTypeEnum.fromNode(n);
            if (t != null)
            {
                if (AttrUtils.isIconableComponent(c))
                {
                    final MainFrame                            f=MainFrame.getContainerFrameInstance();
                    final Map<NodeTypeEnum,? extends Icon>    im=(null == f) ? null : f.getIconsMap();
                    final Icon                                i=((null == im) || (im.size() <= 0)) ? null : im.get(t);
                    if (i != null)
                        AttrUtils.setComponentIcon(c, i);
                }

                if (AttrUtils.isTooltipedComponent(c))
                {
                    final String    tt=EnumUtil.toAttributeName(t);
                    if ((tt != null) && (tt.length() > 0))
                        AttrUtils.setComponentToolTipText(c, tt);
                }
            }
        }

        return c;
    }
}

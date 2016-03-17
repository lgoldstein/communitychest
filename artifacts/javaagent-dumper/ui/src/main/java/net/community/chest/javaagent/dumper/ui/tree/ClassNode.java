/*
 *
 */
package net.community.chest.javaagent.dumper.ui.tree;

import java.util.ArrayList;
import java.util.Collection;

import net.community.chest.javaagent.dumper.ui.data.SelectibleClassInfo;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 14, 2011 2:57:39 PM
 *
 */
public class ClassNode extends AbstractInfoNode<SelectibleClassInfo> {
    private static final long serialVersionUID = 6804536071532121317L;

    public ClassNode (SelectibleClassInfo info)
    {
        this(info, true);
    }

    public ClassNode (SelectibleClassInfo info, boolean withChildren)
    {
        super(SelectibleClassInfo.class, info, info.getSimpleName(), withChildren);
    }

    public Collection<MethodNode> getSelectedNodes ()
    {
        final int                        childCount=getChildCount();
        final Collection<MethodNode>    nodes=
            (childCount > 0) ? new ArrayList<MethodNode>(childCount) : null;
        for (int    cIndex=0; cIndex < childCount; cIndex++)
        {
            final MethodNode    childNode=(MethodNode) getChildAt(cIndex);
            if (!childNode.isSelected())
                continue;
            nodes.add(childNode);
        }

        return nodes;
    }
}

/*
 *
 */
package net.community.chest.javaagent.dumper.ui.tree;

import net.community.chest.javaagent.dumper.ui.data.SelectibleMethodInfo;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 14, 2011 2:55:48 PM
 */
public class MethodNode extends AbstractInfoNode<SelectibleMethodInfo> {
    private static final long serialVersionUID = -6597435605654541611L;

    public MethodNode (SelectibleMethodInfo info)
    {
        this(info, true);
    }

    public MethodNode (SelectibleMethodInfo info, boolean withChildren)
    {
        super(SelectibleMethodInfo.class, info, info.getName(), withChildren);
    }
}

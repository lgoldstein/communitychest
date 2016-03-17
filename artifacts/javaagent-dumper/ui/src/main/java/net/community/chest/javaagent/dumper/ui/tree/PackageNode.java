/*
 *
 */
package net.community.chest.javaagent.dumper.ui.tree;

import net.community.chest.javaagent.dumper.ui.data.SelectiblePackageInfo;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 14, 2011 1:13:56 PM
 */
public class PackageNode extends AbstractInfoNode<SelectiblePackageInfo> {
    private static final long serialVersionUID = -7512566366222245598L;

    public PackageNode (SelectiblePackageInfo info)
    {
        this(info, true);
    }

    public PackageNode (SelectiblePackageInfo info, boolean withChildren)
    {
        super(SelectiblePackageInfo.class, info, info.getName(), withChildren);
    }
}

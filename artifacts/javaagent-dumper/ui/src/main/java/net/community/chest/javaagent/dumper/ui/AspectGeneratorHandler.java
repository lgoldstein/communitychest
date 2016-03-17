/*
 *
 */
package net.community.chest.javaagent.dumper.ui;

import net.community.chest.javaagent.dumper.ui.tree.AbstractInfoNode;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 21, 2011 12:35:26 PM
 */
interface AspectGeneratorHandler {
    void handleProcessedNode (AbstractInfoNode<?> node);
    void doneGenerating (AspectGeneratorThread instance);
}

/*
 * 
 */
package net.community.chest.groovy.apps.mvntree

import java.awt.BorderLayout;

import groovy.swing.SwingBuilder;

import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Dec 8, 2010 1:13:15 PM
 *
 */
class MvnDependencyDisplay {

	static void showDependencies (MvnDependencyNode	rootNode) {
		def swingBuilder=new SwingBuilder()
		def JTree depsTree
		def frame=swingBuilder.frame(title: "Groovy Maven Dependencies Tree",
									 size: [450, 300],
									 locationRelativeTo: null,
									 defaultCloseOperation: WindowConstants.EXIT_ON_CLOSE) {
			borderLayout()
			lookAndFeel("system")
			menuBar() {
				menu(text: "File", mnemonic: 'F') {
					menuItem(text: "Exit", mnemonic: 'X', actionPerformed: {dispose() })
				}
			}
			scrollPane(constraints: BorderLayout.CENTER,
					   verticalScrollBarPolicy: ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					   horizontalScrollBarPolicy: ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
				depsTree = tree(rootVisible: false)
			}
		}

	    depsTree.model.root.removeAllChildren()
		populateTreeModel(depsTree.model, rootNode)
		depsTree.model.reload(depsTree.model.root)
		expandAll(depsTree)

		frame.show()
	}
	
	static void expandAll (tree) {
		expandAll(tree, new TreePath(tree.model.root))
	}
	
	static void expandAll (tree, parent) {
		def node=parent.getLastPathComponent()
		if (node.getChildCount() >= 0) {
			for (def e=node.children(); e.hasMoreElements(); ) {
				def n=e.nextElement()
				def path=parent.pathByAddingChild(n);
				expandAll(tree, path);
			}
		}

		tree.expandPath(parent);
	}

	static TreeNode populateTreeModel (TreeModel model, MvnDependencyNode rootNode) {
		def treeRoot=new DefaultMutableTreeNode(rootNode.toString())
		model.root.add treeRoot
		populateChildren(treeRoot, rootNode)
		return treeRoot
	}
	
	static void populateChildren (final TreeNode parentNode, final MvnDependencyNode depNode) {
		depNode.children.each { childNode ->
			def treeChild=new DefaultMutableTreeNode(childNode.toString())
			parentNode.add treeChild
			populateChildren(treeChild, childNode)
		}
	}
}

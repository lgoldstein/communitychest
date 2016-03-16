package net.community.chest.swing.component.tree;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Tooltiped;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides some extra useful functionality for {@link JTree}</P>
 *
 * @author Lyor G.
 * @since Jul 29, 2007 9:47:50 AM
 */
public class BaseTree extends JTree
        implements XmlConvertible<BaseTree>, Tooltiped, Foregrounded, Backgrounded, Enabled {
    /**
     *
     */
    private static final long serialVersionUID = 888758066818651972L;
    public BaseTree ()
    {
        super();
    }

    public BaseTree (Object[] value)
    {
        super(value);
    }

    public BaseTree (Vector<?> value)
    {
        super(value);
    }

    public BaseTree (Hashtable<?, ?> value)
    {
        super(value);
    }

    public BaseTree (TreeNode root)
    {
        super(root);
    }

    public BaseTree (TreeModel newModel)
    {
        super(newModel);
    }

    public BaseTree (TreeNode root, boolean asksAllowsChildren)
    {
        super(root, asksAllowsChildren);
    }
    /**
     * @param <N> The expected {@link TreeNode} type
     * @param nodeClass expected node class - may NOT be null
     * @return underlying model root node cast as a {@link TreeNode} derived
     * object instance - may be null if no model or no current root
     * @throws ClassCastException unable to cast root to required class
     */
    public <N extends TreeNode> N getRoot (final Class<N> nodeClass) throws ClassCastException
    {
        final TreeModel    model=getModel();
        if (null == model)
            return null;

        return BaseDefaultTreeModel.getNode(nodeClass, model.getRoot());
    }
    /**
     * @return underlying model root node cast as a {@link TreeNode} - may be
     * null if null model or no root
     * @throws ClassCastException if cannot cast root object to {@link TreeNode}
     * @see #getRoot(Class) for an extended casting option
     */
    public TreeNode getRoot () throws ClassCastException
    {
        return getRoot(TreeNode.class);
    }

    protected XmlProxyConvertible<?> getTreeConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : JTreeReflectiveProxy.TREE;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseTree fromXml (final Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getTreeConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException(ClassUtil.getArgumentsExceptionLocation(getClass(), "fromXml", DOMUtils.toString(elem)) + " mismatched initialization instances");

        return this;
    }

    public BaseTree (final Element elem) throws Exception
    {
        final JTree    p=fromXml(elem);
        if (p != this)    // not allowed
            throw new IllegalStateException(ClassUtil.getConstructorArgumentsExceptionLocation(getClass(), DOMUtils.toString(elem)) + " mismatched restored " + JPanel.class.getName() + " instances");
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        // TODO implement toXml
        throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
    }
}

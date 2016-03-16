/*
 *
 */
package net.community.chest.ui.components.tree.document;

import javax.swing.tree.TreeModel;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.swing.component.tree.BaseDefaultTreeModel;
import net.community.chest.swing.component.tree.BaseTree;
import net.community.chest.swing.component.tree.TreeUtil;
import net.community.chest.ui.helpers.SettableComponent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Displays a {@link Document} hierarchy
 * @author Lyor G.
 * @since Aug 21, 2008 12:58:21 PM
 */
public class DocumentTree extends BaseTree
        implements SettableComponent<Document>,
                   TypedComponentAssignment<Document> {
    /**
     *
     */
    private static final long serialVersionUID = 8302370898959833278L;
    public DocumentTree ()
    {
        super((TreeModel) null);
        setRootVisible(true);
    }

    private Document    _doc    /* =null */;
    /*
     * @see net.community.chest.ui.helpers.TypedComponentAssignment#getAssignedValue()
     */
    @Override
    public Document getAssignedValue ()
    {
        return _doc;
    }
    /*
     * @see net.community.chest.ui.helpers.TypedComponentAssignment#setAssignedValue(java.lang.Object)
     */
    @Override
    public void setAssignedValue (Document doc)
    {
        if (_doc != doc)
            _doc = doc;
    }

    public Document getDocument ()
    {
        return getAssignedValue();
    }

    protected void showDocument (final Document doc)
    {
        final Element    docElem=(null == doc) ? null : doc.getDocumentElement();
        if (null == docElem)
        {
            setModel(null);
            return;
        }

        final ElementNode    root=new ElementNode(docElem);
        setModel(new BaseDefaultTreeModel(root, true));
        setCellRenderer(new ElementNodeRenderer());
        TreeUtil.setNodesExpansionState(this, true);
    }

    public void setDocument (Document doc, boolean updateTree)
    {
        setAssignedValue(doc);

        if (updateTree)
            showDocument(doc);
    }

    public void setDocument (Document doc)
    {
        setDocument(doc, true);
    }
    /*
     * @see net.community.chest.ui.helpers.SettableComponent#setContent(java.lang.Object)
     */
    @Override
    public void setContent (Document doc)
    {
        setDocument(doc, true);
    }
    /*
     * @see net.community.chest.ui.helpers.SettableComponent#clearContent()
     */
    @Override
    public void clearContent ()
    {
        setContent(null);
    }
    /*
     * @see net.community.chest.ui.helpers.SettableComponent#refreshContent(java.lang.Object)
     */
    @Override
    public void refreshContent (Document value)
    {
        setContent(value);
    }
}

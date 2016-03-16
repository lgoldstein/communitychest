/*
 *
 */
package net.community.chest.ui.components.tree.document;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.dom.DOMUtils;
import net.community.chest.swing.component.tree.DefaultTreeScroll;
import net.community.chest.ui.helpers.SettableComponent;
import net.community.chest.ui.helpers.panel.PresetBorderLayoutPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Contains a {@link DocumentTree} as its CENTER component, an optional
 * file path component at its NORTH position and an optional content component
 * at its SOUTH position
 *
 * @author Lyor G.
 * @since Dec 10, 2008 12:58:03 PM
 */
public class BaseDocumentPanel extends PresetBorderLayoutPanel
        implements SettableComponent<Document>, TypedComponentAssignment<Document> {
    /**
     *
     */
    private static final long serialVersionUID = -6348213924289700422L;
    private DocumentTree    _docTree;
    public DocumentTree getDocumentTree ()
    {
        return _docTree;
    }
    // CAVEAT EMPTOR !!! should not be called after "layoutComponent"
    public void setDocumentTree (DocumentTree t)
    {
        _docTree = t;
    }

    protected DocumentTree createDocumentTree ()
    {
        return new DocumentTree();
    }

    private JComponent    _filePath;
    public JComponent getFilePathComponent ()
    {
        return _filePath;
    }
    // CAVEAT EMPTOR !!! should not be called after "layoutComponent"
    public void setFilePathComponent (JComponent fpc)
    {
        _filePath = fpc;
    }

    protected JComponent createFilePathComponent ()
    {
        return new JLabel("");
    }

    public String getFilePath ()
    {
        return AttrUtils.getComponentText(getFilePathComponent());
    }

    public void setFilePath (String filePath)
    {
        AttrUtils.setComponentText(getFilePathComponent(), (null == filePath) ? "" : filePath);
    }

    private JComponent    _elemData;
    public JComponent getElementDataComponent ()
    {
        return _elemData;
    }
    // CAVEAT EMPTOR !!! should not be called after "layoutComponent"
    public void setElementDataComponent (JComponent ed)
    {
        _elemData = ed;
    }

    protected JComponent createElementDataComponent ()
    {
        return new JLabel("");
    }

    protected String setSelectedElement (final Element elem)
    {
        final String    s=DOMUtils.toString(elem);
        AttrUtils.setComponentText(getElementDataComponent(), s);
        return s;
    }
    /*
     * @see net.community.chest.ui.helpers.panel.DelayedLayoutPanel#layoutComponent()
     */
    @Override
    public void layoutComponent ()
    {
        super.layoutComponent();

        if ((_filePath=createFilePathComponent()) != null)
            add(_filePath, BorderLayout.NORTH);

        if ((_docTree=createDocumentTree()) != null)
        {
            _docTree.addTreeSelectionListener(new TreeSelectionListener() {
                /*
                 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
                 */
                @Override
                public void valueChanged (final TreeSelectionEvent e)
                {
                    final TreePath    selPath=(null == e) /* should not happen */ ? null : e.getNewLeadSelectionPath();
                    final Object    selNode=(null == selPath) ? null : selPath.getLastPathComponent();
                    if (selNode instanceof ElementNode)
                        setSelectedElement(((ElementNode) selNode).getElement());
                }
            });
            add(new DefaultTreeScroll(_docTree), BorderLayout.CENTER);
        }

        if ((_elemData=createElementDataComponent()) != null)
            add(_elemData, BorderLayout.SOUTH);
    }

    public BaseDocumentPanel (boolean autoLayout)
    {
        super(autoLayout);
    }

    public BaseDocumentPanel ()
    {
        this(true);
    }

    public Document getDocument ()
    {
        final DocumentTree    t=getDocumentTree();
        return (null == t) ? null : t.getDocument();
    }

    public void setDocument (final Document doc, final boolean updateTree)
    {
        final DocumentTree    t=getDocumentTree();
        if (t != null)
            t.setDocument(doc, updateTree);
    }

    public void setDocument (final Document doc)
    {
        setDocument(doc, true);
    }
    /*
     * @see net.community.chest.ui.helpers.SettableComponent#clearContent()
     */
    @Override
    public void clearContent ()
    {
        setDocument(null);
        setFilePath(null);
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
     * @see net.community.chest.ui.helpers.SettableComponent#refreshContent(java.lang.Object)
     */
    @Override
    public void refreshContent (Document doc)
    {
        setContent(doc);
    }
    /*
     * @see net.community.chest.ui.helpers.TypedComponentAssignment#getAssignedValue()
     */
    @Override
    public Document getAssignedValue ()
    {
        return getDocument();
    }
    /* NOTE: does NOT refresh the UI - use "setContent" for this purpose
     * @see net.community.chest.ui.helpers.TypedComponentAssignment#setAssignedValue(java.lang.Object)
     */
    @Override
    public void setAssignedValue (Document doc)
    {
        setDocument(doc, false);
    }
}

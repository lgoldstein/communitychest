/*
 *
 */
package net.community.apps.tools.xmlstruct;

import net.community.chest.CoVariantReturn;
import net.community.chest.ui.components.tree.document.BaseDocumentPanel;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 6, 2009 4:30:57 PM
 */
public class DocStructPanel extends BaseDocumentPanel {
    /**
     *
     */
    private static final long serialVersionUID = -5033348104749825219L;
    public DocStructPanel (boolean autoLayout)
    {
        super(autoLayout);
    }

    public DocStructPanel ()
    {
        this(true);
    }
    /*
     * @see net.community.chest.ui.components.tree.BaseDocumentPanel#createDocumentTree()
     */
    @Override
    @CoVariantReturn
    protected DocStructTree createDocumentTree ()
    {
        return new DocStructTree();
    }
}

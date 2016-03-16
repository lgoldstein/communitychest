/*
 *
 */
package net.community.apps.apache.http.xmlinjct;

import net.community.chest.dom.DOMUtils;

import org.w3c.dom.Document;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 21, 2008 1:08:12 PM
 */
public class ReqDocumentPanel extends DocumentPanel {
    /**
     *
     */
    private static final long serialVersionUID = -4075837707516430145L;

    public ReqDocumentPanel ()
    {
        super();
    }

    public Document setDocument (final String filePath) throws Exception
    {
        final Document    doc=DOMUtils.loadDocument(filePath);
        setFilePath(filePath);
        setDocument(doc);
        return doc;
    }
}

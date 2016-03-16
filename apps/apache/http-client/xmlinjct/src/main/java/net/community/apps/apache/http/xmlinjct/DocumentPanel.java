/*
 *
 */
package net.community.apps.apache.http.xmlinjct;

import java.util.Arrays;
import java.util.Collection;

import net.community.chest.ui.components.tree.document.BaseDocumentPanel;

import org.apache.commons.httpclient.Header;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 21, 2008 1:02:37 PM
 */
public class DocumentPanel extends BaseDocumentPanel {
    /**
     *
     */
    private static final long serialVersionUID = -1287885915343143950L;
    public DocumentPanel ()
    {
        super();
    }

    private Collection<Header>    _hdrs    /* =null */;
    public Collection<Header> getHeaders ()
    {
        return _hdrs;
    }

    public void setHeaders (Collection<Header> hl)
    {
        _hdrs = hl;
    }

    public void setHeaders (Header ...headers)
    {
        setHeaders(((null == headers) || (headers.length <= 0)) ? null : Arrays.asList(headers));
    }
}

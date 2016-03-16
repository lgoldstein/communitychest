/*
 *
 */
package net.community.chest.swing.text;

import javax.swing.text.Document;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;

/**
 * <P>Copyright as per GPLv2</P>
 * @param <D> Type of {@link Document} being reflected
 * @author Lyor G.
 * @since Nov 11, 2010 2:23:36 PM
 */
public abstract class DocumentReflectiveProxy<D extends Document> extends UIReflectiveAttributesProxy<D> {
    protected DocumentReflectiveProxy (Class<D> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final DocumentReflectiveProxy<? extends Document> getDocumentReflectiveProxy (final String type)
    {
        if ("html".equalsIgnoreCase(type))
            return HTMLDocumentReflectiveProxy.HTMLDOC;
        else if ("plain".equalsIgnoreCase(type))
            return PlainDocumentReflectiveProxy.PLAINDOC;
        else if ("styled".equalsIgnoreCase(type))
            return DefaultStyledDocumentReflectiveProxy.DEFSTYLEDDOC;
        else
            return null;
    }
}

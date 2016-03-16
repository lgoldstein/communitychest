/*
 *
 */
package net.community.chest.swing.text;

import java.net.URL;

import javax.swing.text.html.HTMLDocument;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.io.URLStringInstantiator;

/**
 * <P>Copyright as per GPLv2</P>
 * @param <D> Type of {@link HTMLDocument} being reflected
 * @author Lyor G.
 * @since Nov 11, 2010 2:42:41 PM
 */
public class HTMLDocumentReflectiveProxy<D extends HTMLDocument> extends DefaultStyledDocumentReflectiveProxy<D> {
    protected HTMLDocumentReflectiveProxy (Class<D> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public HTMLDocumentReflectiveProxy (Class<D> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final String    BASE_URL_PROP_NAME="base";
    /*
     * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
    {
        if (BASE_URL_PROP_NAME.equalsIgnoreCase(name) && URL.class.isAssignableFrom(type))
            return (ValueStringInstantiator<C>) URLStringInstantiator.DEFAULT;

        return super.resolveAttributeInstantiator(name, type);
    }

    public static final HTMLDocumentReflectiveProxy<HTMLDocument>    HTMLDOC=
        new HTMLDocumentReflectiveProxy<HTMLDocument>(HTMLDocument.class, true);
}

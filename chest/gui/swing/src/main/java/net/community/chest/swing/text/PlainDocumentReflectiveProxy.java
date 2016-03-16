/*
 *
 */
package net.community.chest.swing.text;

import javax.swing.text.PlainDocument;

/**
 * <P>Copyright as per GPLv2</P>
 * @param <D> Type of {@link PlainDocument} being reflected
 * @author Lyor G.
 * @since Nov 11, 2010 2:40:32 PM
 */
public class PlainDocumentReflectiveProxy<D extends PlainDocument> extends AbstractDocumentReflectiveProxy<D> {
    protected PlainDocumentReflectiveProxy (Class<D> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public PlainDocumentReflectiveProxy (Class<D> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final PlainDocumentReflectiveProxy<PlainDocument>    PLAINDOC=
        new PlainDocumentReflectiveProxy<PlainDocument>(PlainDocument.class, true);
}

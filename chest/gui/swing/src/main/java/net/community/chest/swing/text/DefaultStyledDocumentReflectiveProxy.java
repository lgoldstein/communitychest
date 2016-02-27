/*
 * 
 */
package net.community.chest.swing.text;

import javax.swing.text.DefaultStyledDocument;

/**
 * <P>Copyright as per GPLv2</P>
 * @param <D> Type of {@link DefaultStyledDocument} being reflected
 * @author Lyor G.
 * @since Nov 11, 2010 2:36:30 PM
 */
public class DefaultStyledDocumentReflectiveProxy<D extends DefaultStyledDocument> extends AbstractDocumentReflectiveProxy<D> {
	protected DefaultStyledDocumentReflectiveProxy (Class<D> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public DefaultStyledDocumentReflectiveProxy (Class<D> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final DefaultStyledDocumentReflectiveProxy<DefaultStyledDocument>	DEFSTYLEDDOC=
		new DefaultStyledDocumentReflectiveProxy<DefaultStyledDocument>(DefaultStyledDocument.class, true);
}

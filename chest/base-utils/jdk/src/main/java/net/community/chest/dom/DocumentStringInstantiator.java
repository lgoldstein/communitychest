/*
 * 
 */
package net.community.chest.dom;

import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.convert.ValueStringInstantiator;

import org.w3c.dom.Document;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 13, 2009 1:55:51 PM
 */
public class DocumentStringInstantiator extends BaseTypedValuesContainer<Document>
		implements ValueStringInstantiator<Document> {
	public DocumentStringInstantiator () throws IllegalArgumentException
	{
		super(Document.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (Document inst) throws Exception
	{
		if (null == inst)
			return null;

		final Transformer	t=DOMUtils.getDefaultXmlTransformer();
		final Source		xmlSource=new DOMSource(inst);
		final Writer		sw=new StringWriter(256);
		final Result		outputTarget=new StreamResult(sw);
		t.transform(xmlSource, outputTarget);
		return sw.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public Document newInstance (String s) throws Exception
	{
		return DOMUtils.loadDocumentFromString(s);
	}

	public static final DocumentStringInstantiator	DEFAULT=new DocumentStringInstantiator();
}

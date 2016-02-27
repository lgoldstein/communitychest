/*
 * 
 */
package net.community.chest.ui.components.dialog.load.xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.community.chest.convert.StringValueInstantiator;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.io.FileStringInstantiator;
import net.community.chest.io.URLStringInstantiator;
import net.community.chest.util.collection.CollectionsUtils;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Represents the possible sources for importing XML data</P>
 * 
 * @author Lyor G.
 * @since Mar 31, 2009 12:21:36 PM
 */
public enum XmlImportSource {
	FILE(File.class, FileStringInstantiator.DEFAULT) {
			/*
			 * @see net.community.chest.ui.components.dialog.load.xml.XmlImportSource#doLoad(java.lang.Object)
			 */
			@Override
			protected Document doLoad (Object o)
				throws ParserConfigurationException, SAXException, IOException
			{

				return (null == o) ? null : DOMUtils.loadDocument((File) o);
			}
		},
	URL(URL.class, URLStringInstantiator.DEFAULT) {
			/*
			 * @see net.community.chest.ui.components.dialog.load.xml.XmlImportSource#doLoad(java.lang.Object)
			 */
			@Override
			protected Document doLoad (Object o)
				throws ParserConfigurationException, SAXException, IOException
			{
				return (null == o) ? null : DOMUtils.loadDocument((URL) o);
			}
		},
	TEXT(String.class, StringValueInstantiator.DEFAULT) {
			/*
			 * @see net.community.chest.ui.components.dialog.load.xml.XmlImportSource#doLoad(java.lang.Object)
			 */
			@Override
			protected Document doLoad (Object o)
				throws ParserConfigurationException, SAXException, IOException
			{
				return (null == o) ? null : DOMUtils.loadDocumentFromString((String) o);
			}
		};

	private final Class<?>	_ic;
	public final Class<?> getInputClass ()
	{
		return _ic;
	}

	private final ValueStringInstantiator<?>	_vsi;
	public final ValueStringInstantiator<?> getStringValueInstantiator ()
	{
		return _vsi;
	}

	protected abstract Document doLoad (Object o)
		throws ParserConfigurationException, SAXException, IOException;

	public Document loadDocument (Object o)
		throws ParserConfigurationException, SAXException, IOException
	{
		final Class<?>	oc=(null == o) ? null : o.getClass(), ic=getInputClass();
		if (null == oc)
			return null;

		if (!ic.isAssignableFrom(oc))
			throw new ParserConfigurationException("loadDocument(" + o + ") not a " + ic.getName() + " but rather a " + oc.getName()); 

		return doLoad(o);
	}

	public Document loadDocumentFromString (String s) throws Exception
	{
		if ((null == s) || (s.length() <= 0))
			return null;

		final ValueStringInstantiator<?>	vsi=getStringValueInstantiator();
		final Object						o=vsi.newInstance(s);
		return loadDocument(o);
	}

	XmlImportSource (Class<?> ic, ValueStringInstantiator<?> vsi)
	{
		_ic = ic;
		_vsi = vsi;
	}

	public static final List<XmlImportSource>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final XmlImportSource fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final XmlImportSource fromInputClass (final Class<?> c)
	{
		if (null == c)
			return null;

		for (final XmlImportSource v : VALUES)
		{
			final Class<?>	vc=(null == v) ? null : v.getInputClass();
			if ((vc != null) && vc.isAssignableFrom(c))
				return v;
		}

		return null;
	}
}

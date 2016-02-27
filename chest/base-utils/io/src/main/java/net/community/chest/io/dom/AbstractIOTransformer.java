package net.community.chest.io.dom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StreamCorruptedException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.community.chest.dom.AttrDataComparator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.io.FileUtil;
import net.community.chest.io.output.OutputStreamEmbedder;
import net.community.chest.io.xml.BaseIOTransformer;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides some default implementations for some of the {@link javax.xml.transform.Transformer}
 * abstract methods</P>
 * 
 * @author Lyor G.
 * @since Nov 22, 2007 9:49:21 AM
 */
public abstract class AbstractIOTransformer extends BaseIOTransformer {
	protected AbstractIOTransformer ()
	{
		super();
	}
	/**
	 * @return The {@link Attr}-ibutes {@link Comparator} to use when writing
	 * an XML {@link Element} attributes - <code>null</code> means no special
	 * sorting - default={@link AttrDataComparator#CASE_INSENSITIVE_ATTR_DATA}
	 */
	public Comparator<? super Attr> getAttributesSorter ()
	{
		return AttrDataComparator.CASE_SENSITIVE_ATTR_DATA;
	}

	public Writer writeAttribute (final Attr a, final Writer w) throws IOException
	{
		return DOMUtils.appendAttribute(a, w, true);
	}

	public Writer writeElementData (Element elem, Writer org, boolean followChildren, boolean closeIt, CharSequence indent) throws IOException
	{
		final Comparator<? super Attr>	c;
		if (isSortedElementAttributes())
		{
			if (null == (c=getAttributesSorter()))
				throw new StreamCorruptedException("writeElementData() no " + Comparator.class.getSimpleName() + " for sorting attributes");
		}
		else
			c = null;

		return DOMUtils.appendElementData(elem, org, c, followChildren, closeIt, indent);
	}

	public Writer writeElementData (Element elem, final Writer w, boolean followChildren, boolean closeIt) throws IOException
	{
		return writeElementData(elem, w, followChildren, closeIt, "");
	}
	/**
	 * Called by default implementation of {@link #transform(Document, Writer)} after
	 * taking care of the XML declaration part
	 * @param root The {@link Document#getDocumentElement()} result
	 * @param org The original {@link Writer} instance
	 * @return The {@link Writer} instance to be used - should be same as
	 * input (unless <U><B>very</B> good</U> reason not to)
	 * @throws TransformerException If failed to transform the XML {@link Element}-s
	 * @throws IOException If failed to write the formatted output
	 */
	public abstract Writer transformRootElement (Element root, Writer org) throws TransformerException, IOException;

	protected Writer writeComments (final Node n, final Writer org, final CharSequence indent) throws IOException, DOMException
	{
		final Collection<? extends Comment>	cl=DOMUtils.extractAllNodes(Comment.class, n, Node.COMMENT_NODE);
		if ((null == cl) || (cl.size() <= 0))
			return org;

		Writer			w=org;
		final boolean	useIndent=(indent != null) && (indent.length() > 0);
		for (final Comment c : cl)
		{
			final String	t=(null == c) ? null : c.getData();
			if ((null == t) || (t.length() <= 0))
				continue;

			w = FileUtil.writeln(w);
			if (useIndent)
				w.append(indent);
			w = DOMUtils.appendComment(w, t);
			w = FileUtil.writeln(w);
		}

		return w;
	}

	public Writer transform (Document doc, final Writer org) throws TransformerException, IOException
	{
		Writer	w=org;
		if ((null == doc) || (null == w))
			throw new TransformerException("transform() no " + Document.class.getSimpleName() + "/" + Writer.class.getSimpleName() + " instance(s)");

	    if (!isOmitXmlDeclaration())
		{
			String	enc=getOutputProperty(OutputKeys.ENCODING);
			if ((null == enc) || (enc.length() <= 0))
				enc = "UTF-8";
			w = FileUtil.writeln(w, "<?xml version=\"1.0\" encoding=\"" + enc + "\"?>");
		}

	    if (isShowComments())
	    	w = writeComments(doc, w, "");

	    return transformRootElement(doc.getDocumentElement(), w);
	}

	public OutputStream transform (Document doc, OutputStream out) throws TransformerException, IOException
	{
		if ((null == doc) || (null == out))
			throw new TransformerException("transform() no " + Document.class.getSimpleName() + "/" + OutputStream.class.getSimpleName() + " instance(s)");

		Writer	w=null;
		try
		{
			// we use an embedder so that closing the writer does not close the underlying output stream
			w = new OutputStreamWriter(new OutputStreamEmbedder(out, false));
			transform(doc, w);
			return out;
		}
		finally
		{
			FileUtil.closeAll(w);
		}
	}
	/**
	 * @param doc A {@link Document} assumed to contain the <code>classpath</code>
	 * specification of an Eclipse project
	 * @param f The {@link File} to which to write the transformation result
	 * @return Same as input {@link File} instance
	 * @throws TransformerException if failed to transform
	 * @throws IOException if failed to write output
	 */
	public File transform (Document doc, File f) throws TransformerException, IOException
	{
		if ((null == doc) || (null == f))
			throw new TransformerException("transform(" + f + ") no " + Document.class.getSimpleName() + "/" + File.class.getSimpleName() + " instance(s)");

		OutputStream	out=null;
		try
		{
			out = new FileOutputStream(f);
			transform(doc, out);
			return f;
		}
		finally
		{
			FileUtil.closeAll(out);
		}
	}

	public URL transform (Document doc, URL url)
		throws TransformerException, IOException
	{
		OutputStream	out=null;
		try
		{
			out = openURLForOutput(url);
			transform(doc, out);
			return url;
		}
		finally
		{
			FileUtil.closeAll(out);
		}
	}

	public void transform (DOMSource xmlSource, StreamResult outputTarget)
		throws TransformerException
	{
		final Document	n=getDocument(xmlSource);
		if (null == n)
			throw new TransformerException("Null/non-" + Document.class.getSimpleName() + " " + DOMSource.class.getSimpleName() + " " + Node.class.getSimpleName());

		final OutputStream	o=(null == outputTarget) ? null : outputTarget.getOutputStream();
		if (null == o)	// if no output stream try the Writer
		{
			final Writer	w=outputTarget.getWriter();
			// if no writer, assume the system ID is the file URI
			if (null == w)
			{
				final String	sysId=outputTarget.getSystemId();
				final URI		uri;
				try
				{
					uri = ((null == sysId) || (sysId.length() <= 0)) ? null : new URI(sysId);
				}
				catch(URISyntaxException e)
				{
					throw new TransformerException("transform(URI=" + sysId + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
				}

				try
				{
					final URL	url=(null == uri) ? null : uri.toURL();
					if (null == url)
						throw new TransformerException("No " + OutputStream.class.getSimpleName() + "/" + Writer.class.getSimpleName() + "/" + File.class.getSimpleName() + " provided");

					transform(n, url);
					return;
				}
				catch(IOException e)
				{
					throw new TransformerException("transform()[" + File.class.getSimpleName() + "] " + e.getClass() + ": " + e.getMessage(), e);
				}
			}

			try
			{
				transform(n, w);
			}
			catch(IOException e)
			{
				throw new TransformerException("transform()[" + Writer.class.getSimpleName() + "] " + e.getClass() + ": " + e.getMessage(), e);
			}
		}
		else
		{
			try
			{
				transform(n, o);
			}
			catch(IOException e)
			{
				throw new TransformerException("transform()[" + OutputStream.class.getSimpleName() + "] " + e.getClass() + ": " + e.getMessage(), e);
			}
		}
	}
	/*
	 * @see javax.xml.transform.Transformer#transform(javax.xml.transform.Source, javax.xml.transform.Result)
	 */
	@Override
	public void transform (Source xmlSource, Result outputTarget) throws TransformerException
	{
		if ((null == xmlSource) || (!(xmlSource instanceof DOMSource)))
			throw new TransformerException("Null/non-" + DOMSource.class.getSimpleName() + " XML " + Source.class.getSimpleName());
		if ((null == outputTarget) || (!(outputTarget instanceof StreamResult)))
			throw new TransformerException("Null/non-" + StreamResult.class.getSimpleName() + " output " + Result.class.getSimpleName());

		transform((DOMSource) xmlSource, (StreamResult) outputTarget);
	}
}

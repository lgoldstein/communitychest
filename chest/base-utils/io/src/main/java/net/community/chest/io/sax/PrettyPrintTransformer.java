/*
 * 
 */
package net.community.chest.io.sax;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.TransformerException;

import net.community.chest.io.IOCopier;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since May 12, 2010 9:17:07 AM
 */
public class PrettyPrintTransformer extends AbstractIOTransformer {
	public PrettyPrintTransformer ()
	{
		super();
	}
	/**
	 * Property for {@link BufferedWriter} size to use
	 * @see #getOutputWriterBufferSize()
	 */
	public static final String	OUTPUT_WRITER_BUFFER_SIZE="x-output-writer-buffer-size";
	/**
	 * @return size of {@link BufferedWriter} to use - ignored if zero
	 */
	public int getOutputWriterBufferSize ()
	{
		return getIntProperty(getOutputProperty(OUTPUT_WRITER_BUFFER_SIZE),
							  IOCopier.DEFAULT_COPY_SIZE, 0, Short.MAX_VALUE);
	}
	/*
	 * @see net.community.chest.io.sax.AbstractIOTransformer#transform(org.xml.sax.InputSource, java.lang.Appendable)
	 */
	@Override
	public void transform (InputSource src, Appendable w)
			throws TransformerException, IOException
	{
		if ((null == src) || (null == w))
			throw new TransformerException("No source/output provided");

		try
		{
			final SAXParser			parser=getSAXParser();
			final int				tabLength=isTabsIndentation() ? getTabLength() : 0;
			final DefaultHandler	handler=new PrettyPrintHandler(w, tabLength);
			parser.parse(src, handler);
		}
		catch(ParserConfigurationException e)
		{
			throw new TransformerException(e.getClass().getName() + ": " + e.getMessage(), e);
		}
		catch(SAXException e)
		{
			throw new TransformerException(e.getClass().getName() + ": " + e.getMessage(), e);
		}
	}
	/*
	 * @see net.community.chest.io.sax.AbstractIOTransformer#transform(org.xml.sax.InputSource, java.io.OutputStream)
	 */
	@Override
	public void transform (InputSource src, OutputStream out)
			throws TransformerException, IOException
	{
		Writer	w=((null == src) || (null == out)) ? null : new OutputStreamWriter(out);
		if (w != null)
		{
			final int	bufSize=getOutputWriterBufferSize();
			if (bufSize > Byte.MAX_VALUE)
				w = new BufferedWriter(w, bufSize);
		}

		transform(src, w);
		if (w != null)
			w.flush();	// make sure everything written to the output stream
	}

	public static final PrettyPrintTransformer DEFAULT=new PrettyPrintTransformer();
}

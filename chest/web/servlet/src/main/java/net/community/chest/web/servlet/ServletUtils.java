/**
 * 
 */
package net.community.chest.web.servlet;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.community.chest.dom.DOMUtils;
import net.community.chest.io.FileUtil;

import org.w3c.dom.Document;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 28, 2008 2:23:13 PM
 */
public final class ServletUtils {
	private ServletUtils ()
	{
		// no instance
	}
    /**
     * Extracts all the initialization parameters specified in the {@link ServletContext}
     * @param ctx The {@link ServletContext} - ignored if null
     * @return A {@link Map} with the name/value {@link String}-s - may be
     * null/empty if no initialization parameters found or all parameters have
     * null/empty value
     * @param caseSensitive TRUE=map the parameters using case sensitive match,
     * FALSE=case insensitive
     * @throws IllegalStateException if duplicate parameter found (according
     * to the select comparison case sensitivity)
     */
    public static final Map<String,String> getServletInitializationParameters (final ServletContext ctx, final boolean caseSensitive) throws IllegalStateException
    {
    	Map<String,String>	pMap=null;
    	// actually this is Enumeration<String> but this code is more robust...
    	for (final Enumeration<?>	names=(null == ctx) ? null : ctx.getInitParameterNames();
    		 (names != null) && names.hasMoreElements();
    		 )
    	{
    		final Object	pn=names.nextElement();
    		final String	n=(null == pn) ? null : pn.toString();
    		if ((null == n) || (n.length() <= 0))
    			continue;	// should not happen

    		final String	v=ctx.getInitParameter(n);
    		if ((null == v) || (v.length() <= 0))
    			continue;	// ignore empty values

    		if (null == pMap)
    			pMap = caseSensitive ? new TreeMap<String,String>() : new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
    		else if (pMap.containsKey(n))	// should not happen
    			throw new IllegalStateException("Multiple values for parameter=" + n);
    		pMap.put(n, v);
    	}

    	return pMap;
    }
    /**
     * Loads a {@link Document} from a {@link HttpServletRequest} payload
     * @param req The {@link HttpServletRequest} instance - may NOT be null
     * @return The loaded {@link Document} instance
     * @throws Exception If failed to load the document
     * @see HttpServletRequest#getInputStream()
     * @see DOMUtils#loadDocument(InputStream)
     */
    public static final Document loadDocument (final HttpServletRequest req) throws Exception
    {
		InputStream	in=null;
		try
		{
			in = req.getInputStream();

			return DOMUtils.loadDocument(in);
		}
		finally
		{
			FileUtil.closeAll(in);
		}
    }
    /**
     * Writes the XML {@link Document} using a default {@link Transformer}
     * to the servlet's {@link OutputStream}
     * @param doc The {@link Document} to be dumped (may NOT be null)
     * @param resp The {@link HttpServletResponse} instance whose {@link OutputStream}
     * is to be used
     * @throws Exception if cannot access the servlet or transform the document
     */
    public static final void dumpDocument (final Document doc, final HttpServletResponse resp) throws Exception
	{
    	if ((null == resp) || (null == doc))
    		throw new ServletException("No response/document instance");

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/xml");

		OutputStream	out=null;
		try
		{
			out = resp.getOutputStream();

			final Transformer	t=DOMUtils.getDefaultXmlTransformer();
			final Source		s=new DOMSource(doc);
			final Result		r=new StreamResult(out);
			t.transform(s, r);
		}
		finally
		{
			FileUtil.closeAll(out);
		}
	}
}

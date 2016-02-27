/*
 * 
 */
package net.community.chest.spring.test.listener;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.community.chest.io.sax.PrettyPrintHandler;

import org.xml.sax.ContentHandler;

/**
 * @author Lyor G.
 * @since Jul 26, 2010 12:18:32 PM
 */
public final class EntitiesContext {
	private static final Map<String,JAXBContext>	_ctxMap=
		new TreeMap<String,JAXBContext>();
	public static final JAXBContext getJAXBContext (String pkgName)
		throws JAXBException
	{
		if ((null == pkgName) || (pkgName.length() <= 0))
			return null;

		JAXBContext	ctx=null;
		synchronized(_ctxMap)
		{
			if (null == (ctx=_ctxMap.get(pkgName)))
			{
				if (null == (ctx=JAXBContext.newInstance(pkgName)))
					throw new JAXBException("No context generated for package=" + pkgName);
				_ctxMap.put(pkgName, ctx);
			}
		}

		return ctx;
	}

	public static final JAXBContext getJAXBContext (Package p) throws JAXBException
	{
		return (null == p) ? null : getJAXBContext(p.getName());
	}

	public static final JAXBContext getJAXBContext (Class<?> c) throws JAXBException
	{
		return (null == c) ? null : getJAXBContext(c.getPackage());
	}

	public static final JAXBContext getJAXBContext (Object o) throws JAXBException
	{
		return (null == o) ? null : getJAXBContext(o.getClass());
	}

	public static final <A extends Appendable> A marshalObject (Object o, A out)
		throws IOException
	{
		if ((null == o) || (null == out))
			return out;

		try
		{
			final JAXBContext	ctx=getJAXBContext(o);
			if (null == ctx)
				throw new IOException("No JAXB context for object=" + o);

			final ContentHandler	hndlr=new PrettyPrintHandler(out);
			final Marshaller		m=ctx.createMarshaller();
			m.marshal(o, hndlr);
			return out;
		}
		catch(JAXBException e)
		{
			throw new IOException(e.getClass().getName() + " while marshal object=" + o + ": " + e.getMessage(), e);
		}
	}
}

package net.community.chest.convert;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Collection;

import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.AttributeGettersChain;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.reflect.MethodUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to simply retrieve an attribute via a getter method</P>
 * 
 * @author Lyor G.
 * @since Nov 12, 2007 1:56:45 PM
 */
public class ReflectiveEntityFormatter extends Format {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8876477837675475382L;
	private final AttributeGettersChain	_chain;
	public final AttributeGettersChain getAccessorsChain ()
	{
		return _chain;
	}

	// format: "class#method" - e.g., "Foo.Bar#getName().get..."
	public ReflectiveEntityFormatter (final String methodKey) throws Exception
	{
		final int					sepPos=methodKey.lastIndexOf(MethodUtil.METHOD_SEP_CHAR);
		final String				clsName=methodKey.substring(0, sepPos),
									mthdsList=methodKey.substring(sepPos+1);
		final Class<?>				c=ClassUtil.loadClassByName(clsName);
		final Collection<String>	orgList=StringUtil.splitString(mthdsList, '.');

		_chain = new AttributeGettersChain(c, (null == orgList) ? (-1) : orgList.size());
		_chain.setInvocationChain(orgList);
	}
	/*
	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
	 */
	@Override
	public StringBuffer format (Object obj, StringBuffer toAppendTo, FieldPosition pos)
	{
		if (obj != null)
		{
			try
			{
				final AttributeGettersChain	chain=getAccessorsChain();
				final Object				v=chain.invoke(obj);
				if (v != null)
					return toAppendTo.append(v);
			}
			catch(Exception e)
			{
				throw ExceptionUtil.toRuntimeException(e);
			}
		}

		return toAppendTo;
	}
	/*
	 * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
	 */
	@Override
	public Object parseObject (String source, ParsePosition pos)
	{
		throw new UnsupportedOperationException("parseObject(" + source + ") N/A");
	}
}

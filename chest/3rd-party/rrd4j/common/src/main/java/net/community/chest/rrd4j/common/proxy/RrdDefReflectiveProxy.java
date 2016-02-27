package net.community.chest.rrd4j.common.proxy;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.proxy.ReflectiveAttributesProxy;
import net.community.chest.resources.SystemPropertiesResolver;
import net.community.chest.rrd4j.common.RrdUtils;
import net.community.chest.rrd4j.common.core.RrdDefExt;
import net.community.chest.rrd4j.common.core.util.RrdTimestampValueStringInstantiator;
import net.community.chest.util.datetime.Duration;

import org.rrd4j.core.RrdDef;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <D> Type of reflected {@link RrdDef}
 * @author Lyor G.
 * @since Jan 8, 2008 12:26:18 PM
 */
public class RrdDefReflectiveProxy<D extends RrdDef> extends ReflectiveAttributesProxy<D>{
	protected RrdDefReflectiveProxy (Class<D> valsClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(valsClass, registerAsDefault);
	}

	public RrdDefReflectiveProxy (Class<D> valsClass) throws IllegalArgumentException
	{
		this(valsClass, false);
	}
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#getObjectAttributeValue(java.lang.Object, java.lang.String, java.lang.String, java.lang.Class)
	 */
	@Override
	protected Object getObjectAttributeValue (D src, String name, String value, Class<?> type) throws Exception
	{
		if (RrdDefExt.PATH_ATTR.equalsIgnoreCase(name))
			return SystemPropertiesResolver.SYSTEM.format(value);

		if (RrdDefExt.STEP_ATTR.equalsIgnoreCase(name))
		{
			final long		val=Duration.fromTimespec(value);
			return Long.valueOf(RrdUtils.toRrdTime(val)); 	// since step is in seconds
		}

		if (RrdDefExt.START_TIME_ATTR.equals(name))
			return RrdTimestampValueStringInstantiator.DEFAULT.newInstance(value);

		return super.getObjectAttributeValue(src, name, value, type);
	}
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public D fromXmlChild (final D src, final Element elem) throws Exception
	{
		if (src instanceof RrdDefExt)
		{
			((RrdDefExt) src).addDefinitionElement(elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}

	public static final RrdDefReflectiveProxy<RrdDef>	DEFAULT=
				new RrdDefReflectiveProxy<RrdDef>(RrdDef.class, true) {
			/*
			 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#createInstance(org.w3c.dom.Element)
			 */
			@Override
			@CoVariantReturn
			public RrdDefExt createInstance (Element elem) throws Exception
			{
				return (null == elem) ? null : new RrdDefExt();
			}
		};
}

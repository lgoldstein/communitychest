package net.community.chest.rrd4j.common.proxy;

import java.awt.Paint;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;
import net.community.chest.awt.dom.converter.ColorValueInstantiator;
import net.community.chest.convert.DoubleValueStringConstructor;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.resources.SystemPropertiesResolver;
import net.community.chest.rrd4j.common.RrdUtils;
import net.community.chest.rrd4j.common.core.util.RrdTimestampValueStringInstantiator;
import net.community.chest.rrd4j.common.graph.RrdGraphDefExt;
import net.community.chest.util.datetime.Duration;
import net.community.chest.util.map.MapEntryImpl;

import org.rrd4j.graph.RrdGraphConstants;
import org.rrd4j.graph.RrdGraphDef;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <D> The {@link RrdGraphDef} generic type
 * @author Lyor G.
 * @since Jan 14, 2008 1:40:48 PM
 */
public class RrdGraphDefReflectiveProxy<D extends RrdGraphDef> extends UIReflectiveAttributesProxy<D> {
	public RrdGraphDefReflectiveProxy (Class<D> dClass)
	{
		this(dClass, false);
	}

	protected RrdGraphDefReflectiveProxy (Class<D> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#getObjectAttributeValue(java.lang.Object, java.lang.String, java.lang.String, java.lang.Class)
	 */
	@Override
	protected Object getObjectAttributeValue (D src, String name, String value, Class<?> type) throws Exception
	{
		if (RrdGraphDefExt.STEP_ATTR.equalsIgnoreCase(name))
		{
			final long	val=Duration.fromTimespec(value);
			return Long.valueOf(RrdUtils.toRrdTime(val)); 	// since step is in seconds
		}

		if (RrdGraphDefExt.FILENAME_ATTR.equalsIgnoreCase(name))
		{
			if ((null == value) || (value.length() <= 0) || RrdGraphConstants.IN_MEMORY_IMAGE.equals(value))
				return value;

			return SystemPropertiesResolver.SYSTEM.format(value);
		}

		if (RrdGraphDefExt.START_TIME_ATTR.equalsIgnoreCase(name)
		 || RrdGraphDefExt.END_TIME_ATTR.equalsIgnoreCase(name))
			return RrdTimestampValueStringInstantiator.DEFAULT.newInstance(value);

		return super.getObjectAttributeValue(src, name, value, type);
	}
	/*
	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#extractSettersMap(java.lang.Class)
	 */
	@Override
	protected Map<String,Method> extractSettersMap (Class<D> valsClass)
	{
		final Map<String,Method>	sMap=super.extractSettersMap(valsClass);
		try
		{
			// some special setters
			final Method[]	extraSetters={
				valsClass.getMethod("setValueAxis", Double.TYPE, Integer.TYPE),
				valsClass.getMethod("setColor", String.class, Paint.class)
			};
			for (final Method setter : extraSetters)
			{
				final String	name=setter.getName(),
								aName=AttributeMethodType.SETTER.getPureAttributeName(name);
				if ((null == aName) || (aName.length() <= 0))
					continue;	// should not happen
				sMap.put(aName, setter);
			}

			return sMap;
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}

	public static final <G extends RrdGraphDef> Map.Entry<Double,Integer> setValueAxis (final G src, final String value) throws Exception
	{
		if ((null == value) || (value.length() <= 0))
			return null;

		final List<String>	comps=StringUtil.splitString(value, ',');
		if ((null == comps) || (comps.size() != 2))
			throw new IllegalArgumentException("setValueAxis(" + value + ") bad format (should be gridStep,labelFactor)");

		final Double	gridStep=
			DoubleValueStringConstructor.DEFAULT.newInstance(StringUtil.getCleanStringValue(comps.get(0)));
		final Integer	labelFactor=
			Integer.valueOf(StringUtil.getCleanStringValue(comps.get(1)));
		if ((null == gridStep) || (null == labelFactor))
			throw new IllegalArgumentException("setValueAxis(" + value + ") cannot re-construct values");
		src.setValueAxis(gridStep.doubleValue(), labelFactor.intValue());

		return new MapEntryImpl<Double,Integer>(gridStep, labelFactor);
	}

	public static final <G extends RrdGraphDef> Map.Entry<String,Paint> setColor (G src, String value) throws Exception
	{
		if ((null == value) || (value.length() <= 0))
			return null;

		final List<String>	comps=StringUtil.splitString(value, ',');
		if ((null == comps) || (comps.size() != 2))
			throw new IllegalArgumentException("setColor(" + value + ") bad format (should be colorTag,colorName)");

		final String	t=StringUtil.getCleanStringValue(comps.get(0));
		final Paint		p=ColorValueInstantiator.DEFAULT.newInstance(StringUtil.getCleanStringValue(comps.get(1)));
		if ((null == t) || (t.length() <= 0) || (null == p))
			throw new IllegalArgumentException("setColor(" + value + ") cannot re-construct values");
		src.setColor(t, p);

		return new MapEntryImpl<String,Paint>(t, p);
	}
	/*
	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
	 */
	@Override
	protected D updateObjectAttribute (D src, String name, String value, Method setter) throws Exception
	{
		// special setters handler
		if (RrdGraphDefExt.VALUE_AXIS_ATTR.equalsIgnoreCase(name))
		{
			setValueAxis(src, value);
			return src;
		}
		else if (RrdGraphDefExt.COLOR_ATTR.equalsIgnoreCase(name))
		{
			setColor(src, value);
			return src;
		}

		return super.updateObjectAttribute(src, name, value, setter);
	}
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public D fromXmlChild (final D src, final Element elem) throws Exception
	{
		if (src instanceof RrdGraphDefExt)
		{
			((RrdGraphDefExt) src).addRenderingElement(elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}

	public static final RrdGraphDefReflectiveProxy<RrdGraphDef>	DEFAULT=
		new RrdGraphDefReflectiveProxy<RrdGraphDef>(RrdGraphDef.class, true);
}

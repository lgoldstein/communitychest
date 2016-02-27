package com.vmware.spring.workshop.services.convert.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.model.Identified;
import com.vmware.spring.workshop.services.ExceptionUtils;

/**
 * @author lgoldstein
 */
public abstract class AbstractDTOConverter<MDL, DTO> extends DTOConverterSupport<MDL, DTO> {

	protected static final ValueConverter<Object,Object> IDENTITY_VALUE_CONVERTER=
			new ValueConverter<Object,Object>() {
				@Override
				public Object convertValue(Object srcValue) {
					return srcValue;
				}
			};
	protected static final ValueConverter<Identified,Long>	IDENTIFIED_VALUE_CONVERTER=
			new ValueConverter<Identified, Long>() {
				@Override
				public Long convertValue(Identified srcValue) {
					if (srcValue == null)
						return null;
					else
						return srcValue.getId();
				}
			};

	private final List<Method2MethodConversionInfo>	_mdl2dtoConverters, _dto2mdlConverters;
	public AbstractDTOConverter(Class<MDL> mdlClass, Class<DTO> dtoClass) {
		super(mdlClass, dtoClass);

		final Map<String,PropertyDescriptor>	mdlProps=
				createPropertiesMap(getPropertyDescriptors(mdlClass)),
												dtoProps=
				createPropertiesMap(getPropertyDescriptors(dtoClass));
		_mdl2dtoConverters = Collections.unmodifiableList(
				createConvertersList(mdlClass, mdlProps, dtoClass, dtoProps));
		_dto2mdlConverters = Collections.unmodifiableList(
				createConvertersList(dtoClass, dtoProps, mdlClass, mdlProps));
	}

	@Override
	public DTO toDTO(final MDL data) {
		final DTO	dto=createDTOInstance(data);
		if (dto == null)
			return null;

		applyConverters(data, dto, _mdl2dtoConverters);
		return dto;
	}

	@Override
	public MDL updateFromDTO(DTO dto, MDL data) {
		if ((dto == null) || (data == null))
			return data;

		applyConverters(dto, data, _dto2mdlConverters);
		return data;
	}

	protected void applyConverters (final Object srcObject, final Object dstObject,
									final Collection<Method2MethodConversionInfo> converters) {
		Assert.notNull(srcObject, "No source object");
		Assert.notNull(dstObject, "No destination object");
		Assert.isTrue(!CollectionUtils.isEmpty(converters), "No converters");
		
		for (final Method2MethodConversionInfo c : converters) {
			try {
				c.convert(srcObject, dstObject);
			} catch (Exception t) {
				final RuntimeException	e=ExceptionUtils.toRuntimeException(t);	
				_logger.error("applyConverters(" + c + ")"
							+ " failed (" + e.getClass().getSimpleName() + ")"
							+ " to convert value: " + e.getMessage(), t);
				throw e;
			}
		}
	}

	protected static final String	ID_SUFFIX="Id";
	protected List<Method2MethodConversionInfo> createConvertersList (
						final Class<?> 							srcClass,
						final Map<String,PropertyDescriptor>	srcProps,
						final Class<?>							dstClass,
						final Map<String,PropertyDescriptor>	dstProps) {
		if (MapUtils.isEmpty(srcProps))
			throw new IllegalStateException("No source properties for " + srcClass.getSimpleName());
		if (MapUtils.isEmpty(dstProps))
			throw new IllegalStateException("No destination properties for " + dstClass.getSimpleName());

		final List<Method2MethodConversionInfo>	result=new ArrayList<Method2MethodConversionInfo>(srcProps.size());
		for (final Map.Entry<String,PropertyDescriptor> srcEntry : srcProps.entrySet()) {
			final String				srcName=srcEntry.getKey();
			final PropertyDescriptor	srcProp=srcEntry.getValue();
			final Class<?>				srcType=srcProp.getPropertyType();
			final String				dstName;
			if (Identified.class.isAssignableFrom(srcType))
				dstName = srcName + ID_SUFFIX;
			else if (srcName.endsWith(ID_SUFFIX) && Long.class.isAssignableFrom(srcType))
				dstName = srcName.substring(0, srcName.length() - ID_SUFFIX.length());
			else
				dstName = srcName;

			final PropertyDescriptor	dstProp=dstProps.get(dstName);
			if (dstProp == null) {
				if (_logger.isDebugEnabled())
					_logger.debug("createConvertersMap(" + srcClass.getSimpleName() + "[" + srcName + "]"
								+ " => " + dstClass.getSimpleName() + "[" + dstName + "]"
								+ " - no match found");
				continue;
			}

			try {
				final Method2MethodConversionInfo	convEntry=
						resolvePropertyConverter(srcClass, srcProp.getReadMethod(), srcProp.getWriteMethod(),
												 dstClass, dstProp.getReadMethod(), dstProp.getWriteMethod());
				if (convEntry == null) {
					if (_logger.isDebugEnabled())
						_logger.debug("createConvertersMap(" + srcClass.getSimpleName() + "[" + srcName + "]"
									+ " => " + dstClass.getSimpleName() + "[" + dstName + "]"
									+ " - no converter");
					continue;
				}

				result.add(convEntry);
			} catch(RuntimeException e) {
				throw new IllegalStateException("createConvertersMap(" + srcClass.getSimpleName() + ")[" + srcName + "]"
						  + " => " + dstClass.getSimpleName() + "[" + dstName + "]"
						  + " - " + e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}
		
		return result;
	}

	protected Method2MethodConversionInfo resolvePropertyConverter (
														  final Class<?> 	srcClass,
														  final Method		srcGetter,
														  final Method		srcSetter,
														  final Class<?>	dstClass,
														  final Method		dstGetter,
														  final Method		dstSetter) {
		final ValueConverter<?,?>	converter=
				resolveValueConverter(srcClass, srcGetter, srcSetter, dstClass, dstGetter, dstSetter);
		if (converter == null)
			return null;
		else
			return new Method2MethodConversionInfo(srcGetter, srcSetter, dstGetter, dstSetter, converter); 
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected ValueConverter<?,?> resolveValueConverter (final Class<?> srcClass,
														 final Method	srcGetter,
														 final Method	srcSetter,
														 final Class<?>	dstClass,
														 final Method	dstGetter,
														 final Method	dstSetter) {
		if ((srcGetter == null) || (dstGetter == null)
		 ||	(srcSetter == null) || (dstSetter == null))
			return null;	// ignore non reachable properties

		final Class<?>	srcType=srcGetter.getReturnType(), dstType=dstGetter.getReturnType();
		if (dstType.isAssignableFrom(srcType))
			return IDENTITY_VALUE_CONVERTER;

		if (Enum.class.isAssignableFrom(srcType)) {
			if (Enum.class.isAssignableFrom(dstType)) {
				return new NamedEnumValueConverter(srcType, dstType);
			} else {
				throw new IllegalStateException("resolveValueConverter(" + srcClass.getSimpleName() + ")"
											  + "[" + srcGetter.getName() + "]"
											  + "=> " + dstClass.getSimpleName()
											  + "[" + dstSetter.getName() + "]"
											  + ": target is not enum");
			}
		}

		if (Identified.class.isAssignableFrom(srcType)) {
			if (Long.class.isAssignableFrom(dstType)) {
				return IDENTIFIED_VALUE_CONVERTER;
			} else {
				throw new IllegalStateException("resolveValueConverter(" + srcClass.getSimpleName() + ")"
											  + "[" + srcGetter.getName() + "]"
											  + "=> " + dstClass.getSimpleName()
											  + "[" + dstSetter.getName() + "]"
											  + ": identified target is not an ID type");
			}
		}

		return resolveUnknownPropertyValueConverter(srcClass, srcGetter, srcSetter, dstClass, dstGetter, dstSetter);
	}

	protected ValueConverter<?,?> resolveUnknownPropertyValueConverter (
												final Class<?>	srcClass,
												final Method	srcGetter,
												final Method	srcSetter,
												final Class<?>	dstClass,
												final Method	dstGetter,
												final Method	dstSetter) {
		throw new IllegalStateException("Identified target is not convertible");
	}

	@Override
	public MDL fromDTO (final DTO dto)
	{
		return updateFromDTO(dto, createModelInstance(dto));
	}

	protected MDL createModelInstance (DTO dto)
	{
		if (dto == null)
			return null;

		final Class<MDL>	mdlClass=getModelClass();
		try
		{
			return mdlClass.newInstance();
		}
		catch(Exception e)
		{
			_logger.error("createModelInstance(" + dto + ")"
						+ " failed (" + e.getClass().getName() + ")"
						+ " to create new instance: " + e.getMessage(), e);
			throw ExceptionUtils.toRuntimeException(e);
		}
	}

	protected DTO createDTOInstance (MDL data)
	{
		if (data == null)
			return null;

		final Class<DTO>	dtoClass=getDtoClass();
		try
		{
			return dtoClass.newInstance();
		}
		catch(Exception e)
		{
			_logger.error("createDTOInstance(" + data + ")"
						+ " failed (" + e.getClass().getName() + ")"
						+ " to create new instance: " + e.getMessage(), e);
			throw ExceptionUtils.toRuntimeException(e);
		}
	}

	// returns the 1st Method2MethodConversionInfo where data is not equal - or null if all equal
	Method2MethodConversionInfo compareDto2Model (DTO dto, MDL mdl) {
		return compareInstancesProperties(dto, mdl, _dto2mdlConverters);
	}

	Method2MethodConversionInfo compareModel2Dto (MDL mdl, DTO dto) {
		return compareInstancesProperties(mdl, dto, _mdl2dtoConverters);
	}

	Method2MethodConversionInfo compareInstancesProperties (
				final Object srcObject, final Object dstObject,
				final Collection<Method2MethodConversionInfo> converters) {
		Assert.notNull(srcObject, "No source object");
		Assert.notNull(dstObject, "No destination object");
		Assert.isTrue(!CollectionUtils.isEmpty(converters), "No converters");
		
		for (final Method2MethodConversionInfo	c : converters) {
			try {
				final Object	srcValue=c.getSourceValue(srcObject),
								dstValue=c.getDestinationValue(dstObject),
								cnvValue=c.convertSourceValue(srcValue);
				if (ObjectUtils.equals(cnvValue, dstValue))
					continue;
			} catch(Exception t) {
				final RuntimeException	e=ExceptionUtils.toRuntimeException(t);	
				_logger.error("compareInstancesProperties(" + c + ")"
							+ " failed (" + e.getClass().getSimpleName() + ")"
							+ " to retrieve value(s): " + e.getMessage(), t);
			}

			// this point is reached if either an exception or un-equal values
			return c;
		}

		// this point is reached if all converters exhausted and all values equal
		return null;
	}
			
	@Override
	public String toString ()
	{
		final Component	c=getClass().getAnnotation(Component.class);
		return ((c == null) ? null : c.value())
				+ "[" + getModelClass().getSimpleName()
				+ " => " + getDtoClass().getSimpleName()
				+ "]";
	}

	private static final PropertyDescriptor[] getPropertyDescriptors (final Class<?> clazz) {
		Assert.notNull(clazz, "No class provided");
		try {
			final BeanInfo				info=Introspector.getBeanInfo(clazz);
			final PropertyDescriptor[]	props=info.getPropertyDescriptors();
			if (ArrayUtils.isEmpty(props))
				throw new IntrospectionException("No properties");

			return props;
		} catch(IntrospectionException e) {
			throw new IllegalStateException("getPropertyDescriptors(" + clazz.getSimpleName() + ") failed to introspect: " + e.getMessage());
		}
	}
	
	private static final Map<String,PropertyDescriptor> createPropertiesMap (final PropertyDescriptor ... descriptors) {
		if (ArrayUtils.isEmpty(descriptors))
			return Collections.emptyMap();
		
		final Map<String,PropertyDescriptor>	result=new TreeMap<String,PropertyDescriptor>(String.CASE_INSENSITIVE_ORDER);
		for (final PropertyDescriptor d : descriptors) {
			final String				name=d.getName();
			final PropertyDescriptor	prev=result.put(name, d);
			if (prev != null)
				throw new IllegalStateException("Multiple properties named " + name + " - only case different");
		}

		return result;
	}
	
	protected static class Method2MethodConversionInfo {
		private final Method				_srcGetter, _srcSetter, _dstGetter, _dstSetter;
		private final ValueConverter<?,?>	_converter;
		
		public Method2MethodConversionInfo (Method srcGetter, Method srcSetter,
											Method dstGetter, Method dstSetter,
											ValueConverter<?,?> converter) {
			Assert.state((_srcGetter=srcGetter) != null, "No source getter method provided");
			Assert.state((_srcSetter=srcSetter) != null, "No source setter method provided");
			Assert.state((_dstGetter=dstGetter) != null, "No destination getter method provided");
			Assert.state((_dstSetter=dstSetter) != null, "No destination setter method provided");
			Assert.state((_converter=converter) != null, "No converter provided");
		}

		public void convert (Object srcObject, Object dstObject)
				throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			final Object	srcValue=getSourceValue(srcObject);
			final Object	dstValue=convertSourceValue(srcValue);
			setDestinationValue(dstObject, dstValue);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Object convertSourceValue (Object srcValue) {
			return ((ValueConverter) _converter).convertValue(srcValue);
		}

		public Object getSourceValue (Object srcObject)
				throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			return _srcGetter.invoke(srcObject, ArrayUtils.EMPTY_OBJECT_ARRAY);
		}

		public void setSourceValue (Object srcObject, Object srcValue)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			_srcSetter.invoke(srcObject, srcValue);
		}

		public Object getDestinationValue (Object dstObject)
				throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			return _dstGetter.invoke(dstObject, ArrayUtils.EMPTY_OBJECT_ARRAY);
		}

		public void setDestinationValue (Object dstObject, Object dstValue)
				throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			_dstSetter.invoke(dstObject, dstValue);
		}

		@Override
		public String toString() {
			return _srcGetter.getDeclaringClass().getSimpleName() + "[" + _srcGetter.getName() + "]"
				 + " => "
				 + _dstSetter.getDeclaringClass().getSimpleName() + "[" + _dstSetter.getName() + "]"
				 ;
		}
	}
}

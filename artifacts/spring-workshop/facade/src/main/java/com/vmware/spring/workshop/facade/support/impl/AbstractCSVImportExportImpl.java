package com.vmware.spring.workshop.facade.support.impl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.vmware.spring.workshop.facade.support.CSVImportExport;
import com.vmware.spring.workshop.model.ModelUtils;

/**
 * @author lgoldstein
 */
public abstract class AbstractCSVImportExportImpl<DTO> implements CSVImportExport<DTO> {
	private final Class<DTO>	_dtoClass;
	private final Map<String,PropertyDescriptor>	_propsMap;
	private final List<String> _defaultPropsOrder;
	@Inject private ConversionService	_svcConvert;
	protected final Logger	_logger=LoggerFactory.getLogger(getClass());

	protected AbstractCSVImportExportImpl(final Class<DTO> dtoClass) throws IntrospectionException {
		Assert.state((_dtoClass=dtoClass) != null, "No DTO class provided");
		_propsMap = Collections.unmodifiableMap(ModelUtils.createPropertiesMap(dtoClass));
		_defaultPropsOrder = Collections.unmodifiableList(new ArrayList<String>(_propsMap.keySet()));
	}

	@Override
	public final Class<DTO> getDTOClass() {
		return _dtoClass;
	}

	@Override
	public DTO toDTO(BufferedReader reader, List<String> propsOrder) throws IOException {
		Assert.notNull(reader, "No reader");
		Assert.isTrue(!CollectionUtils.isEmpty(propsOrder), "No properties order specified");

		for (String	lineData=reader.readLine(); lineData != null; lineData=reader.readLine())
		{
			lineData = StringUtils.strip(lineData);
			if (StringUtils.isBlank(lineData))
				continue;	// skip blank lines

			return toDTO(lineData, propsOrder);
		}

		return null;	// reached if exhausted all non empty lines
	}

	@Override
	public <A extends Appendable> A appendTitleLine(final A sb) throws IOException {

		return appendTitleLine(sb, getPropertiesOrder());
	}

	@Override
	public <A extends Appendable> A appendTitleLine(A sb, List<String> propsOrder) throws IOException {
		Assert.notNull(sb, "No appender provided");
		Assert.isTrue(!CollectionUtils.isEmpty(propsOrder), "No title properties order");

		boolean	isFirst=true;
		for (final String name : propsOrder) {
			if (isFirst)
				isFirst = false;
			else
				sb.append(',');
			sb.append(name);
		}

		return sb;
	}

	@Override
	public <A extends Appendable> A appendDTOList(final A sb, final Collection<? extends DTO> dtoList) throws IOException {
		return appendDTOList(sb, getPropertiesOrder(), dtoList);
	}

	@Override
	public <A extends Appendable> A appendDTOList(A sb, List<String> propsOrder, Collection<? extends DTO> dtoList)
			throws IOException {
		if (CollectionUtils.isEmpty(dtoList))
			return sb;

		for (final DTO dto : dtoList)
		{
			sb.append(LINE_SEP);	// separate from previous line
			appendDTO(sb, dto, propsOrder);
		}

		return sb;
	}

	@Override
	public DTO toDTO(final String data, final List<String> propsOrder) throws IOException {
		Assert.isTrue(!CollectionUtils.isEmpty(propsOrder), "No properties order provided");

		final List<String> valsList=breakdownLineData(StringUtils.trimToEmpty(data));
		if (CollectionUtils.isEmpty(valsList))
			throw new StreamCorruptedException("No values extracted from " + data);
		if (valsList.size() != propsOrder.size())
			throw new StreamCorruptedException("Mismatched values size"
											 + " (got=" + valsList.size() + ")"
											 + "/(expected=" + propsOrder.size() + ")"
											 + " for data=" + data);

		final Class<DTO>	dtoClass=getDTOClass();
		final DTO			dto;
		try {
			if ((dto=dtoClass.newInstance()) == null)
				throw new IllegalStateException("Failed to instantiate");
		} catch(Exception e) {
			throw new StreamCorruptedException("Failed to create instance for " + data + ": " + e.getMessage());
		}

		for (int	vIndex=0; vIndex < propsOrder.size(); vIndex++) {
			final String	name=propsOrder.get(vIndex),
							value=valsList.get(vIndex);
			final PropertyDescriptor	desc=_propsMap.get(name);
			if (desc == null)
				throw new IllegalStateException("toDTO(" + data + ") missing descriptor for property=" + name);

			final Object	dtoValue=toDTOValue(name, value, desc.getPropertyType());
			if (dtoValue == null)
				continue;

			final Method	sMethod=desc.getWriteMethod();
			ReflectionUtils.invokeMethod(sMethod, dto, dtoValue);
		}

		return dto;
	}

	@Override
	public <A extends Appendable> A appendDTO(final A sb, final DTO dto) throws IOException {

		return appendDTO(sb, dto, getPropertiesOrder());
	}

	@Override
	public <A extends Appendable> A appendDTO(A sb, DTO dto, List<String> propsOrder) throws IOException {
		Assert.notNull(sb, "No appender provided");
		Assert.notNull(dto, "No DTO provided");
		Assert.isTrue(!CollectionUtils.isEmpty(propsOrder), "No properties order");

		boolean	isFirst=true;
		for (final String name : propsOrder) {
			final PropertyDescriptor	desc=_propsMap.get(name);
			if (desc == null)
				throw new IllegalStateException("No descriptor for property=" + name + " of DTO=" + dto);

			final Method	gMethod=desc.getReadMethod();
			final Object	value=ReflectionUtils.invokeMethod(gMethod, dto, ArrayUtils.EMPTY_OBJECT_ARRAY);
			final String	strValue=toDTOStringValue(name, value);
			if (isFirst)
				isFirst = false;
			else
				sb.append(',');

			if (StringUtils.isBlank(strValue))
				continue;

			if (value instanceof String)	// quote strings to avoid comma issues
				sb.append('\'').append(strValue).append('\'');
			else
				sb.append(strValue);
		}

		return sb;
	}

	protected Object toDTOValue (final String name, final String value, final Class<?> propType)
	{
		Assert.notNull(propType, "No property type");
		if (StringUtils.isBlank(value))
			return null;
		
		if (_svcConvert.canConvert(String.class, propType))
			return _svcConvert.convert(value, propType);
		throw new IllegalStateException("toDTOValue(" + name + ")[" + propType.getSimpleName() + "] cannot convert " + value);
	}

	private static List<String> breakdownLineData (final String lineData) {
		if (StringUtils.isBlank(lineData))
			return Collections.emptyList();

		final List<String>	result=new ArrayList<String>();
		int	lastPos=0;
		boolean	inQuotes=false;
		for (int	curPos=0; curPos < lineData.length(); curPos++) {
			final char	ch=lineData.charAt(curPos);
			switch(ch) {
				case ','	:
					if (!inQuotes) {
						final int		cpyLen=curPos - lastPos;
						final String	cpyValue=(cpyLen <= 0) ? "" : StringUtils.trimToEmpty(lineData.substring(lastPos, curPos));
						result.add(stripDelims(cpyValue));
						lastPos = curPos + 1;
					}
					break;

				case '\'':
					inQuotes = !inQuotes;
					break;
				
				default	:	// do nothing
			}
		}
		
		if (lastPos < lineData.length()) {
			final String	cpyValue=lineData.substring(lastPos);
			result.add(stripDelims(cpyValue));
		}

		return result;
	}

	private static String stripDelims (final String value) {
		if (StringUtils.isBlank(value))
			return "";

		if (value.charAt(0) != '\'')
			return value;

		if ((value.length() <= 1) || (value.charAt(value.length() - 1) != '\''))
			throw new IllegalArgumentException("Imbalanced delimiter in " + value);

		return StringUtils.trimToEmpty(value.substring(1, value.length() - 1));
	}

	protected String toDTOStringValue (final String name, final Object value) {
		final String	strValue=StringUtils.trimToEmpty((value == null) ? null : value.toString());
		if (StringUtils.isBlank(strValue))
			return strValue;
		else
			return strValue.replace('\'', '-');
	}

	@Override
	public String toString ()
	{
		final Class<?>		clazz=getClass();
		final Component		ctrl=clazz.getAnnotation(Component.class);
		final String		name=(ctrl == null) ? null : ctrl.value();
		return StringUtils.isBlank(name) ? clazz.getSimpleName() : name;
	}

	protected List<String> getPropertiesOrder () {
		return _defaultPropsOrder;
	}
}

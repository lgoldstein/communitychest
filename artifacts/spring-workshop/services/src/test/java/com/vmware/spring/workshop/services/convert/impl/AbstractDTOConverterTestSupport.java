package com.vmware.spring.workshop.services.convert.impl;

import org.junit.Assert;

import com.vmware.spring.workshop.services.convert.impl.AbstractDTOConverter.Method2MethodConversionInfo;

/**
 * @author lgoldstein
 */
public abstract class AbstractDTOConverterTestSupport<MDL,DTO> extends Assert {
	protected AbstractDTOConverterTestSupport() {
		super();
	}

	protected DTO checkModel2DTOConversion (MDL data, AbstractDTOConverter<MDL,DTO> converter) {
		final DTO	dto=converter.toDTO(data);
		if (dto == null)
			return null;

		final Method2MethodConversionInfo	mismatch=converter.compareDto2Model(dto, data);
		assertNull("Mismatched MDL=>DTO conversion values: " + mismatch, mismatch);
		return dto;
	}

	protected MDL checkDTO2ModelConversion (DTO dto, AbstractDTOConverter<MDL,DTO> converter) {
		final MDL	data=converter.fromDTO(dto);
		if (data == null)
			return null;

		final Method2MethodConversionInfo	mismatch=converter.compareModel2Dto(data, dto);
		assertNull("Mismatched DTO=>MDL conversion values: " + mismatch, mismatch);
		return data;
	}
}

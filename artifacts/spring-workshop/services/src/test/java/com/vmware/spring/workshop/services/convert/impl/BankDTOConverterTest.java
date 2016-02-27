package com.vmware.spring.workshop.services.convert.impl;

import org.junit.Test;

import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.model.banking.Bank;

/**
 * @author lgoldstein
 */
public class BankDTOConverterTest extends AbstractDTOConverterTestSupport<Bank,BankDTO> {
	private final BankDTOConverterImpl	_converter=new BankDTOConverterImpl();
	public BankDTOConverterTest() {
		super();
	}

	@Test
	public void testModel2DTOConversion () {
		checkModel2DTOConversion(createMockBank(), _converter);
	}

	@Test
	public void testDTO2ModelConversion () {
		final Bank	data=checkDTO2ModelConversion(createMockBankDTO(), _converter);
		assertEquals("Mismatched reconstructed version", 0, data.getVersion());
	}

	static final Bank createMockBank () {
		final Bank	data=new Bank();
		final long	nanoTime=System.nanoTime(), msecTime=System.currentTimeMillis();
		data.setId(Long.valueOf(nanoTime));
		data.setVersion((int) nanoTime);
		data.setBankCode((int) nanoTime);
		data.setName("bank#" + data.getBankCode());
		data.setHqAddress(msecTime + "/" + nanoTime);
		return data;
	}

	static final BankDTO createMockBankDTO () {
		final BankDTO	dto=new BankDTO();
		final long		nanoTime=System.nanoTime(), msecTime=System.currentTimeMillis();
		dto.setId(Long.valueOf(nanoTime));
		dto.setBankCode((int) nanoTime);
		dto.setName("dto#" + dto.getBankCode());
		dto.setHqAddress(msecTime + "/" + nanoTime);
		return dto;
	}
}

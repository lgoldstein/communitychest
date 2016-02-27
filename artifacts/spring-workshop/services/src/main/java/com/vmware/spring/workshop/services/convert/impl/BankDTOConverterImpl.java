package com.vmware.spring.workshop.services.convert.impl;

import org.springframework.stereotype.Component;

import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.model.banking.Bank;
import com.vmware.spring.workshop.services.convert.BankDTOConverter;

/**
 * @author lgoldstein
 */
@Component("bankDTOConverter")
public class BankDTOConverterImpl extends AbstractDTOConverter<Bank,BankDTO> implements BankDTOConverter {
	public BankDTOConverterImpl () {
		super(Bank.class, BankDTO.class);
	}
}

package com.vmware.spring.workshop.services.convert;

import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.model.banking.Bank;

/**
 * @author lgoldstein
 */
public interface BankDTOConverter extends DTOConverter<Bank,BankDTO> {
	// nothing extra
}

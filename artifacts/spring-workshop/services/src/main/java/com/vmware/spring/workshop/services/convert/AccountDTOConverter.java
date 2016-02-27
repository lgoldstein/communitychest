package com.vmware.spring.workshop.services.convert;

import com.vmware.spring.workshop.dto.banking.AccountDTO;
import com.vmware.spring.workshop.model.banking.Account;

/**
 * @author lgoldstein
 */
public interface AccountDTOConverter extends DTOConverter<Account,AccountDTO> {
	// nothing extra
}

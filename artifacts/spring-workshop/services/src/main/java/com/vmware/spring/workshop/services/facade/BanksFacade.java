package com.vmware.spring.workshop.services.facade;

import java.util.List;

import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.model.banking.Bank;

/**
 * @author lgoldstein
 */
public interface BanksFacade extends CommonFacadeActions<BankDTO> {
	BankDTO findBankByName (String name);
	BankDTO findBankByBankCode (int code);

	/**
	 * @param location A sub-string of the location
	 * @return A {@link List} of all matching {@link Bank}-s whose location
	 * contains the specified parameter (case <U>insensitive</U>)
	 */
	List<BankDTO> findByBankLocation (String location);
}

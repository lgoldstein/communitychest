package com.vmware.spring.workshop.dto.banking;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * @author lgoldstein
 */
@XmlRegistry
public final class ObjectFactory {
	public ObjectFactory() {
		super();
	}

	public BankDTO createBankDTO () {
		return new BankDTO();
	}

	public BankDTOList createBankDTOList () {
		return new BankDTOList();
	}

	public BranchDTO createBranchDTO () {
		return new BranchDTO();
	}

	public BranchDTOList createBranchDTOList () {
		return new BranchDTOList();
	}

	public AccountDTO createAccountDTO () {
		return new AccountDTO();
	}
}

package com.vmware.spring.workshop.services.facade.impl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import com.vmware.spring.workshop.dao.api.BankDao;
import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.model.banking.Bank;
import com.vmware.spring.workshop.services.convert.BankDTOConverter;
import com.vmware.spring.workshop.services.facade.BanksFacade;
import com.vmware.spring.workshop.services.facade.Facade;

/**
 * @author lgoldstein
 */
@Facade("banksFacade")
@Transactional
public class BanksFacadeImpl
		extends AbstractCommonFacadeActions<Bank,BankDTO,BankDao,BankDTOConverter>
		implements BanksFacade {

	@Inject
	public BanksFacadeImpl(final BankDao			daoBank,
						   final BankDTOConverter	bnkConverter) {
		super(BankDTO.class, Bank.class, daoBank, bnkConverter);
	}

	@Override
	@Transactional(readOnly=true)
	public BankDTO findBankByName(String name) {
		return _converter.toDTO(_dao.findBankByName(name));
	}

	@Override
	@Transactional(readOnly=true)
	public BankDTO findBankByBankCode(int code) {
		return _converter.toDTO(_dao.findBankByBankCode(code));
	}

	@Override
	@Transactional(readOnly=true)
	public List<BankDTO> findByBankLocation(String location) {
		return _converter.toDTO(_dao.findByBankLocation(location));
	}
}

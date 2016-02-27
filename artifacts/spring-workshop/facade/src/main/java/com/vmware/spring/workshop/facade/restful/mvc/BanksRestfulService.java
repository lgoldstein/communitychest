package com.vmware.spring.workshop.facade.restful.mvc;

import java.util.Collection;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.dto.banking.BankDTOList;
import com.vmware.spring.workshop.services.facade.BanksFacade;

/**
 * @author lgoldstein
 */
@Controller("banksRestfulService")
@RequestMapping(AbstractMVCRestfulService.MVC_ACCESS_ROOT + "/banks")
public class BanksRestfulService extends AbstractMVCRestfulService<BankDTO, BanksFacade> {
	@Inject
	public BanksRestfulService (final BanksFacade facade) {
		super(BankDTO.class, facade);
	}

	@Override
	protected BankDTOList wrapAsDTOList(Collection<? extends BankDTO> dtoList) {
		return new BankDTOList(dtoList);
	}
}

package com.vmware.spring.workshop.facade.restful.mvc;

import java.util.Collection;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.vmware.spring.workshop.dto.banking.BranchDTO;
import com.vmware.spring.workshop.dto.banking.BranchDTOList;
import com.vmware.spring.workshop.services.facade.BranchesFacade;

/**
 * @author lgoldstein
 */
@Controller("branchesRestfulService")
@RequestMapping(AbstractMVCRestfulService.MVC_ACCESS_ROOT + "/branches")
public class BranchesRestfulService
		extends AbstractMVCRestfulService<BranchDTO, BranchesFacade> {
	@Inject
	public BranchesRestfulService (final BranchesFacade facade) {
		super(BranchDTO.class, facade);
	}

	@Override
	protected BranchDTOList wrapAsDTOList(Collection<? extends BranchDTO> dtoList) {
		return new BranchDTOList(dtoList);
	}
}

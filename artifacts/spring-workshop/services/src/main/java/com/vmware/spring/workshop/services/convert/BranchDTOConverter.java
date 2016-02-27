package com.vmware.spring.workshop.services.convert;

import com.vmware.spring.workshop.dto.banking.BranchDTO;
import com.vmware.spring.workshop.model.banking.Branch;

/**
 * @author lgoldstein
 */
public interface BranchDTOConverter extends DTOConverter<Branch,BranchDTO> {
	// nothing extra
}

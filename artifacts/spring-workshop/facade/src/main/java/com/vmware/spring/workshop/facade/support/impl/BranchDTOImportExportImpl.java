package com.vmware.spring.workshop.facade.support.impl;

import java.beans.IntrospectionException;

import org.springframework.stereotype.Component;

import com.vmware.spring.workshop.dto.banking.BranchDTO;
import com.vmware.spring.workshop.facade.support.BranchDTOImportExport;

/**
 * @author lgoldstein
 */
@Component("branchDTOImportExport")
public class BranchDTOImportExportImpl
		extends AbstractCSVImportExportImpl<BranchDTO>
		implements BranchDTOImportExport {
	public BranchDTOImportExportImpl () throws IntrospectionException {
		super(BranchDTO.class);
	}
}

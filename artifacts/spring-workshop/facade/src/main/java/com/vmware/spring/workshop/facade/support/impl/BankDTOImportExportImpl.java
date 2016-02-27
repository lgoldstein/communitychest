package com.vmware.spring.workshop.facade.support.impl;

import java.beans.IntrospectionException;

import org.springframework.stereotype.Component;

import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.facade.support.BankDTOImportExport;

/**
 * @author lgoldstein
 */
@Component("bankDTOImportExport")
public class BankDTOImportExportImpl
		extends AbstractCSVImportExportImpl<BankDTO>
		implements BankDTOImportExport {
	public BankDTOImportExportImpl () throws IntrospectionException {
		super(BankDTO.class);
	}
}

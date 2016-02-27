package com.vmware.spring.workshop.facade.support.views;

import org.springframework.test.context.ContextConfiguration;

import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.dto.banking.BankDTOList;
import com.vmware.spring.workshop.facade.AbstractFacadeTestSupport;

/**
 * @author lgoldstein
 */
@ContextConfiguration(locations={ AbstractFacadeTestSupport.DEFAULT_TEST_CONTEXT })
public class BankDTOMarshallingTest extends AbstractDTOMarshallingTestSupport<BankDTO,BankDTOList> {
	public BankDTOMarshallingTest () throws Exception {
		super(BankDTO.class, BankDTOList.class);
	}
}

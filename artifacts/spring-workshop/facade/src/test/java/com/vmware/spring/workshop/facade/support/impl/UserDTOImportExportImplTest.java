package com.vmware.spring.workshop.facade.support.impl;

import javax.inject.Inject;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import com.vmware.spring.workshop.dto.user.UserDTO;
import com.vmware.spring.workshop.facade.AbstractFacadeTestSupport;
import com.vmware.spring.workshop.facade.support.UserDTOImportExport;

/**
 * @author lgoldstein
 */
@ContextConfiguration(locations={ AbstractFacadeTestSupport.DEFAULT_TEST_CONTEXT })
public class UserDTOImportExportImplTest
		extends AbstractCSVImportExportImplTestSupport<UserDTO,UserDTOImportExport> {
	@Inject	private UserDTOImportExport	_importer;

	public UserDTOImportExportImplTest() {
		super();
	}

	@Test
	public void testImportExport () throws Exception {
		runDTOImportExportTest(_importer);
	}
}

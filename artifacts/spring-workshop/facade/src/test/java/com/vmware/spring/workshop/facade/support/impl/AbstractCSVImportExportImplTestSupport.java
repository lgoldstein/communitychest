package com.vmware.spring.workshop.facade.support.impl;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import org.junit.Assert;

import com.vmware.spring.workshop.facade.AbstractFacadeTestSupport;
import com.vmware.spring.workshop.facade.support.CSVImportExport;

/**
 * @author lgoldstein
 */
public abstract class AbstractCSVImportExportImplTestSupport<DTO, IE extends CSVImportExport<DTO>>
		extends AbstractFacadeTestSupport {
	protected AbstractCSVImportExportImplTestSupport() {
		super();
	}

	protected DTO runDTOImportExportTest (final IE importer) throws Exception {
		final Class<DTO>		dtoClass=importer.getDTOClass();
		final DTO				srcDTO=dtoClass.newInstance();
		final List<String>		propsOrder=initializeDTOValues(srcDTO);
		final StringBuilder		sb=importer.appendDTO(new StringBuilder(256), srcDTO, propsOrder);
		final BufferedReader	rdr=new BufferedReader(new StringReader(sb.toString()));
		final DTO				dstDTO=importer.toDTO(rdr, propsOrder);
		Assert.assertEquals("Mismatched reconstructed values", srcDTO, dstDTO);
		return srcDTO;
	}
}

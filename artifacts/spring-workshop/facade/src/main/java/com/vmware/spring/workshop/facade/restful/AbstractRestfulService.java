package com.vmware.spring.workshop.facade.restful;

import java.util.Collection;
import java.util.List;

import org.springframework.util.Assert;

import com.vmware.spring.workshop.dto.IdentifiedDTO;
import com.vmware.spring.workshop.facade.support.AbstractController;
import com.vmware.spring.workshop.services.facade.CommonFacadeActions;

/**
 * @author lgoldstein
 */
public abstract class AbstractRestfulService<DTO extends IdentifiedDTO, FCD extends CommonFacadeActions<DTO>>
		extends AbstractController {
	public static final String	RESTFUL_ACCESS_ROOT="/restful";

	private final Class<DTO> _dtoClass;
	protected final FCD	_facade;

	protected AbstractRestfulService(Class<DTO> dtoClass, FCD facade) {
		Assert.state((_dtoClass=dtoClass) != null, "No DTO class provided");
		Assert.state((_facade=facade) != null, "No facade provided");
	}

	public final Class<DTO> getDTOClass () {
		return _dtoClass;
	}
	
	protected abstract List<DTO> wrapAsDTOList (final Collection<? extends DTO> dtoList);
}

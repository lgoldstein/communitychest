package com.vmware.spring.workshop.facade.restful.resteasy;

import java.util.Collection;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.springframework.util.Assert;

import com.vmware.spring.workshop.dto.IdentifiedDTO;
import com.vmware.spring.workshop.facade.restful.AbstractRestfulService;
import com.vmware.spring.workshop.services.facade.CommonFacadeActions;

/**
 * @author lgoldstein
 */
public abstract class AbstractResteasyRestfulService<DTO extends IdentifiedDTO, FCD extends CommonFacadeActions<DTO>>
		extends AbstractRestfulService<DTO, FCD> {
	public static final String	RESTEASY_ACCESS_ROOT=RESTFUL_ACCESS_ROOT + "/resteasy";

	protected AbstractResteasyRestfulService(Class<DTO> dtoClass, FCD facade) {
		super(dtoClass, facade);
	}
	
	@GET
	public Collection<? extends DTO> list () {
		return wrapAsDTOList(_facade.findAll());
	}
	
	@GET
	@Path(BY_ID_TEMPLATE)
	public DTO findById (@PathParam(ID_PARAM_NAME) final Long id) {
		return _facade.findById(id);
	}

	@POST
	public Collection<? extends DTO> createInstance (final DTO dto) {
		Assert.notNull(dto, "No DTO object");

		final Long	id=dto.getId();
		if (id != null) {
			dto.setId(null);
			_logger.warn("createInstance(" + dto + ") ignore preset ID=" + id);
		}

		_facade.create(dto);
		return list();

	}

	@DELETE
	@Path(BY_ID_TEMPLATE)
	public DTO deleteById (@PathParam(ID_PARAM_NAME) final Long id) {
		return _facade.deleteById(id);
	}
}

package com.vmware.spring.workshop.services.facade.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.dao.IdentifiedCommonOperationsDao;
import com.vmware.spring.workshop.dto.IdentifiedDTO;
import com.vmware.spring.workshop.model.Identified;
import com.vmware.spring.workshop.services.convert.DTOConverter;
import com.vmware.spring.workshop.services.facade.CommonFacadeActions;

/**
 * @param <DTO> Managed DTO type
 * @author lgoldstein
 */
public abstract class AbstractCommonFacadeActions<MDL extends Identified,
												  DTO extends IdentifiedDTO,
												  DAO extends IdentifiedCommonOperationsDao<MDL>,
												  CNV extends DTOConverter<MDL,DTO>>
				implements CommonFacadeActions<DTO> {
	private final Class<DTO>	_dtoClass;
	private final Class<MDL>	_mdlClass;
	protected final DAO	_dao;
	protected final CNV	_converter;
	protected final Logger	_logger=LoggerFactory.getLogger(getClass());

	protected AbstractCommonFacadeActions(Class<DTO> dtoClass, Class<MDL> mdlClass,
										  DAO dao, CNV converter) {
		Assert.state((_dtoClass=dtoClass) != null, "No DTO class provided");
		Assert.state((_mdlClass=mdlClass) != null, "No model class provided");
		Assert.state((_dao=dao) != null, "No dao provided");
		Assert.state((_converter=converter) != null, "No converter provided");
	}

	@Override
	public final Class<DTO> getDTOClass() {
		return _dtoClass;
	}

	public final Class<MDL> getModelClass () {
		return _mdlClass;
	}

	@Override
	@Transactional(readOnly=true)
	public List<DTO> findAll() {
		return _converter.toDTO(_dao.findAll());
	}

	@Override
	@Transactional(readOnly=true)
	public DTO findById(Long id) {
		return _converter.toDTO(_dao.findOne(id));
	}

	@Override
	public void create(DTO dto) {
		final Long id=createModelInstance(_converter.fromDTO(dto));
		Assert.state(id != null, "No model ID assigned");
		dto.setId(id);
	}

	@Override
	public void update(DTO dto) {
		Assert.notNull(dto, "No DTO");
		_dao.save(_converter.updateFromDTO(dto, _dao.findOne(dto.getId())));
	}

	@Override
	public void delete(DTO dto) {
		Assert.notNull(dto, "No DTO");
		_dao.delete(dto.getId());
	}

	@Override
	public DTO deleteById(Long id) {
		DTO	dto=findById(id);
		if (dto == null)
			return null;
		_dao.delete(id);
		return dto;
	}

	protected Long createModelInstance (MDL mdl) {
		Assert.notNull(mdl, "No model instance");
		_dao.save(mdl);
		return mdl.getId();
	}
}

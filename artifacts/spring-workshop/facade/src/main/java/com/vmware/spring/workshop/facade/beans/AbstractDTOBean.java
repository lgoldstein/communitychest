package com.vmware.spring.workshop.facade.beans;

/**
 * @author lgoldstein
 */
public abstract class AbstractDTOBean<DTO> {
	private DTO	_dtoValue;
	protected AbstractDTOBean() {
		super();
	}

	protected AbstractDTOBean(DTO dtoValue) {
		_dtoValue = dtoValue;
	}

	public DTO getDTOValue() {
		return _dtoValue;
	}
	public void setDTOValue(DTO dtoValue) {
		_dtoValue = dtoValue;
	}

}

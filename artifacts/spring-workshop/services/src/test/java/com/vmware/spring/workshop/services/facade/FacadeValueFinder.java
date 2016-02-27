package com.vmware.spring.workshop.services.facade;

/**
 * @author lgoldstein
 */
public interface FacadeValueFinder<FCD,DTO> {
	DTO findDtoValue (FCD facade, DTO dto);
}

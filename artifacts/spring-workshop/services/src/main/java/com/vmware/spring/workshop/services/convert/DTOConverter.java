package com.vmware.spring.workshop.services.convert;

import java.util.List;

/**
 * @param MDL the model type
 * @param DTO the DTO type
 * @author lgoldstein
 */
public interface DTOConverter<MDL, DTO> {
    Class<MDL> getModelClass ();
    Class<DTO> getDtoClass ();

    DTO toDTO (MDL data);
    List<DTO> toDTO (Iterable<? extends MDL> dataList);

    MDL fromDTO (DTO dto);
    MDL updateFromDTO (DTO dto, MDL data);
    List<MDL> fromDTO (Iterable<? extends DTO> dtoList);
}

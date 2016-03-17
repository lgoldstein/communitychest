package com.vmware.spring.workshop.services.convert.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.services.convert.DTOConverter;

/**
 * @author lgoldstein
 */
public abstract class DTOConverterSupport<MDL, DTO> implements DTOConverter<MDL, DTO> {
    private final Class<MDL>    _mdlClass;
    @Override
    public final Class<MDL> getModelClass ()
    {
        return _mdlClass;
    }

    private final Class<DTO>    _dtoClass;
    @Override
    public final Class<DTO> getDtoClass ()
    {
        return _dtoClass;
    }

    protected final Logger    _logger=LoggerFactory.getLogger(getClass());
    protected DTOConverterSupport (Class<MDL> mdlClass, Class<DTO> dtoClass)
    {
        Assert.state((_mdlClass=mdlClass) != null, "No model class provided");
        Assert.state((_dtoClass=dtoClass) != null, "No DTO class provided");
    }

    @Override
    public List<DTO> toDTO (final Iterable<? extends MDL> dataList)
    {
        if (dataList == null)
            return Collections.emptyList();

        final List<DTO>    dtoList=new ArrayList<DTO>();
        for (final MDL data : dataList)
        {
            final DTO    dto=toDTO(data);
            if (dto == null)
                continue;    // debug breakpoint
            dtoList.add(dto);
        }

        return dtoList;
    }

    @Override
    public List<MDL> fromDTO (final Iterable<? extends DTO> dtoList)
    {
        if (dtoList == null)
            return Collections.emptyList();

        final List<MDL>    dataList=new ArrayList<MDL>();
        for (final DTO dto : dtoList)
        {
            final MDL    data=fromDTO(dto);
            if (data == null)
                continue;    // debug breakpoint
            dataList.add(data);
        }

        return dataList;
    }
}

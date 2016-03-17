package com.vmware.spring.workshop.facade.restful.resteasy;

import java.util.Collection;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Service;

import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.dto.banking.BankDTOList;
import com.vmware.spring.workshop.services.facade.BanksFacade;

/**
 * @author lgoldstein
 */
@Path(AbstractResteasyRestfulService.RESTEASY_ACCESS_ROOT + "/banks")
@Produces({ MediaType.APPLICATION_XML })
@Service("banksResteasyService")
public class BanksResteasyService
        extends AbstractResteasyRestfulService<BankDTO, BanksFacade> {
    @Inject
    public BanksResteasyService (final BanksFacade facade) {
        super(BankDTO.class, facade);
    }

    @Override
    protected BankDTOList wrapAsDTOList(Collection<? extends BankDTO> dtoList) {
        return new BankDTOList(dtoList);
    }
}

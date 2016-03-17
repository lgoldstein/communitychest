package com.vmware.spring.workshop.facade.restful.resteasy;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Service;

import com.vmware.spring.workshop.dto.banking.BranchDTO;
import com.vmware.spring.workshop.dto.banking.BranchDTOList;
import com.vmware.spring.workshop.services.facade.BranchesFacade;

/**
 * @author lgoldstein
 */
@Path(AbstractResteasyRestfulService.RESTEASY_ACCESS_ROOT + "/branches")
@Produces({ MediaType.APPLICATION_XML })
@Service("branchesResteasyService")
public class BranchesResteasyService
        extends AbstractResteasyRestfulService<BranchDTO, BranchesFacade> {

    @Inject
    public BranchesResteasyService (final BranchesFacade facade) {
        super(BranchDTO.class, facade);
    }

    @Override
    protected List<BranchDTO> wrapAsDTOList(Collection<? extends BranchDTO> dtoList) {
        return new BranchDTOList(dtoList);
    }
}

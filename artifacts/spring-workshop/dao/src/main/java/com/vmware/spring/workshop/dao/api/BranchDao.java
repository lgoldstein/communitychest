package com.vmware.spring.workshop.dao.api;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.vmware.spring.workshop.dao.IdentifiedCommonOperationsDao;
import com.vmware.spring.workshop.model.Identified;
import com.vmware.spring.workshop.model.banking.Branch;

/**
 * @author lgoldstein
 */
public interface BranchDao extends IdentifiedCommonOperationsDao<Branch> {
    Branch findByBranchCode (@Param("code") int code);
    Branch findByBranchName (@Param("name") String name);

    List<Branch> findByBankId (@Param(Identified.ID_COL_NAME) Long bankId);
    List<Branch> findByBranchBankCode (@Param("code") int bankCode);

    /**
     * @param location A sub-string of the location
     * @return A {@link List} of all matching {@link Branch}-es whose location
     * contains the specified parameter (case <U>insensitive</U>)
     */
    List<Branch> findByBranchLocation (@Param("location") String location);
}

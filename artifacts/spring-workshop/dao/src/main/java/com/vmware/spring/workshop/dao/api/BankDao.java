package com.vmware.spring.workshop.dao.api;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.vmware.spring.workshop.dao.IdentifiedCommonOperationsDao;
import com.vmware.spring.workshop.model.banking.Bank;

/**
 * @author lgoldstein
 */
public interface BankDao extends IdentifiedCommonOperationsDao<Bank> {
    Bank findBankByName (@Param("name") String name);
    Bank findBankByBankCode (@Param("code") int code);
    /**
     * @param location A sub-string of the location
     * @return A {@link List} of all matching {@link Bank}-s whose location
     * contains the specified parameter (case <U>insensitive</U>)
     */
    List<Bank> findByBankLocation (@Param("location") String location);
}

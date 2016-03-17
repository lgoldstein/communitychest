package com.vmware.spring.workshop.services.convert.impl;

import org.junit.Test;
import org.mockito.Mockito;

import com.vmware.spring.workshop.dao.api.BranchDao;
import com.vmware.spring.workshop.dao.api.UserDao;
import com.vmware.spring.workshop.dto.banking.AccountDTO;
import com.vmware.spring.workshop.model.banking.Account;
import com.vmware.spring.workshop.model.banking.Branch;
import com.vmware.spring.workshop.model.user.User;

/**
 * @author lgoldstein
 */
public class AccountDTOConverterTest
        extends AbstractDTOConverterTestSupport<Account,AccountDTO> {

    public AccountDTOConverterTest() {
        super();
    }

    @Test
    public void testModel2DTOConversion () {
        final Account                    data=createMockAccount();
        final AccountDTOConverterImpl    converter=getConverter(data);
        final AccountDTO                dto=checkModel2DTOConversion(data, converter);
        final Branch                    branch=data.getBranch();
        final User                        owner=data.getOwner();
        assertEquals("Mismatched branch ID", branch.getId(), dto.getBranchId());
        assertEquals("Mismatched owner ID", owner.getId(), dto.getOwnerId());
    }

    @Test
    public void testDto2ModelConversion () {
        final AccountDTO                dto=createMockAccountDTO();
        final AccountDTOConverterImpl    converter=getConverter(dto);
        final Account                    data=checkDTO2ModelConversion(dto, converter);
        final Branch                    branch=data.getBranch();
        final Long                        dataBranch=branch.getId(), dtoBranch=dto.getBranchId();
        final User                        owner=data.getOwner();
        final Long                        dataOwner=owner.getId(), dtoOwner=dto.getOwnerId();
        assertEquals("Mismatched branch ID(s)", dataBranch, dtoBranch);
        assertEquals("Mismatched owner ID(s)", dataOwner, dtoOwner);
    }

    private AccountDTOConverterImpl getConverter (final Account account) {
        return getConverter(account.getBranch(), account.getOwner());
    }

    private AccountDTOConverterImpl getConverter (final AccountDTO account) {
        return getConverter(account.getBranchId(), account.getOwnerId());
    }

    private AccountDTOConverterImpl getConverter (final Long branchId, final Long ownerId) {
        final Branch    branch=BranchDTOConverterTest.createMockBranch();
        branch.setId(branchId);

        final User    owner=UserDTOConverterTest.createMockUser();
        owner.setId(ownerId);

        return getConverter(branch, owner);
    }

    private AccountDTOConverterImpl getConverter (final Branch branch, final User owner) {
        final BranchDao    daoBranch=Mockito.mock(BranchDao.class);
        final Long        idBranch=branch.getId();
        Mockito.when(daoBranch.findOne(idBranch)).thenReturn(branch);

        final UserDao    daoUser=Mockito.mock(UserDao.class);
        final Long        idUser=owner.getId();
        Mockito.when(daoUser.findOne(idUser)).thenReturn(owner);

        return new AccountDTOConverterImpl(daoBranch, daoUser);
    }

    static final Account createMockAccount () {
        final Account    data=new Account();
        final long        nanoTime=System.nanoTime(), msecTime=System.currentTimeMillis();
        data.setId(Long.valueOf(nanoTime));
        data.setVersion((int) (msecTime ^ nanoTime));
        data.setAccountNumber(nanoTime + ":" + msecTime);
        data.setAmount((int) (nanoTime & 0x00FFFFL));
        data.setBranch(BranchDTOConverterTest.createMockBranch());
        data.setOwner(UserDTOConverterTest.createMockUser());
        return data;
    }

    static final AccountDTO createMockAccountDTO () {
        final AccountDTO    dto=new AccountDTO();
        final long            nanoTime=System.nanoTime(), msecTime=System.currentTimeMillis();
        dto.setId(Long.valueOf(nanoTime));
        dto.setAccountNumber(nanoTime + ":" + msecTime);
        dto.setAmount((int) (nanoTime & 0x00FFFFL));
        dto.setBranchId(Long.valueOf(nanoTime + msecTime));
        dto.setOwnerId(Long.valueOf(nanoTime - msecTime));
        return dto;
    }
}

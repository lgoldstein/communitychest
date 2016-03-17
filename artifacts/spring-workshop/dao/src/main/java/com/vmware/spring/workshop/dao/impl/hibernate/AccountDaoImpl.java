package com.vmware.spring.workshop.dao.impl.hibernate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.dao.api.AccountDao;
import com.vmware.spring.workshop.model.Identified;
import com.vmware.spring.workshop.model.banking.Account;
import com.vmware.spring.workshop.model.user.InvestmentData;

/**
 * @author lgoldstein
 */
@Repository("accountDao")
@Transactional
public class AccountDaoImpl
        extends AbstractIdentifiedHibernateDaoImpl<Account>
        implements AccountDao {
    private final NamedParameterJdbcOperations    _namedOperations;
    @Inject
    public AccountDaoImpl (DataSource dataSource) {
        super(Account.class);
        _namedOperations = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Account> findUserAccountsById(Long userId) {
        return getNamedIdDefaultQueryResults("findUserAccountsById", userId);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Account> findBranchAccountsById(Long branchId) {
        return getNamedIdDefaultQueryResults("findBranchAccountsById", branchId);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Account> findBankAccountsById(Long bankId) {
        return getNamedIdDefaultQueryResults("findBankAccountsById", bankId);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Map.Entry<Long, Integer>> findInvestedAmountsByBank(Long userId) {
        Assert.notNull(userId, "No user id");
        return _namedOperations.query("SELECT bnk.id, SUM(acc.amount) \n"
                                    + "FROM Account acc \n"
                                    + "INNER JOIN Branch brh ON brh.id = acc.branchId \n"
                                    + "INNER JOIN Bank bnk ON bnk.id = brh.bankId \n"
                                    + "WHERE acc.ownerId = :" + Identified.ID_COL_NAME + " \n"
                                    + "GROUP BY bnk.id",
                                      Collections.singletonMap(Identified.ID_COL_NAME, userId),
                                      MAPPER);
    }

    private static final RowMapper<Map.Entry<Long,Integer>>    MAPPER=
            new RowMapper<Map.Entry<Long,Integer>>() {
                @Override
                public InvestmentData mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new InvestmentData(rs.getLong(1), rs.getInt(2));
                }
        };
}

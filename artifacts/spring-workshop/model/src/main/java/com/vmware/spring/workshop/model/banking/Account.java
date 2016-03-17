package com.vmware.spring.workshop.model.banking;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.vmware.spring.workshop.model.AbstractIdentified;
import com.vmware.spring.workshop.model.Identified;
import com.vmware.spring.workshop.model.user.User;

/**
 * @author lgoldstein
 */
@Entity(name="Account")
@NamedQueries({
    @NamedQuery(name="Account.findUserAccountsById",query="SELECT a FROM Account a WHERE a.owner." + Identified.ID_COL_NAME + " = :" + Identified.ID_COL_NAME),
    @NamedQuery(name="Account.findBranchAccountsById",query="SELECT a FROM Account a WHERE a.branch." + Identified.ID_COL_NAME + " = :" + Identified.ID_COL_NAME),
    @NamedQuery(name="Account.findBankAccountsById",query="SELECT a FROM Account a WHERE a.branch.bank." + Identified.ID_COL_NAME + " = :" + Identified.ID_COL_NAME),
    @NamedQuery(name="Account.findBankAccountsByNumber",query="SELECT a FROM Account a \n"
                                                    + "WHERE a.branch.bank." + Identified.ID_COL_NAME + " = :" + Identified.ID_COL_NAME
                                                    + "  AND a.accountNumber LIKE :accountNumber"),
    @NamedQuery(name="Account.findBranchAccountsByNumber",query="SELECT a FROM Account a \n"
                                                      + "WHERE a.branch." + Identified.ID_COL_NAME + " = :" + Identified.ID_COL_NAME + "\n"
                                                      + "  AND a.accountNumber LIKE :accountNumber"),
    @NamedQuery(name="Account.findInvestedAmountsByBank", query="SELECT NEW com.vmware.spring.workshop.model.user.InvestmentData(bnk.id, SUM(acc.amount)) \n"
                                                              + "FROM Account acc \n"
                                                              + "INNER JOIN acc.branch brh \n"
                                                              + "INNER JOIN brh.bank bnk \n"
                                                              + "WHERE acc.owner." + Identified.ID_COL_NAME + " = :" + Identified.ID_COL_NAME + " \n"
                                                              + "GROUP BY bnk.id")
})
public class Account extends AbstractIdentified {
    private static final long serialVersionUID = 9173191618286031646L;

    private String    _accountNumber;
    private User    _owner;
    private Branch    _branch;
    private int        _amount;

    public Account() {
        super();
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ownerId",nullable=false,referencedColumnName=ID_COL_NAME)
    public User getOwner() {
        return _owner;
    }

    public void setOwner(User owner) {
        _owner = owner;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="branchId",nullable=false,referencedColumnName=ID_COL_NAME)
    public Branch getBranch() {
        return _branch;
    }

    public void setBranch(Branch branch) {
        _branch = branch;
    }

    public static final int    MAX_ACCOUNT_NUMBER_LENGTH=64;
    @Column(name="accountNumber",nullable=false,unique=false,length=MAX_ACCOUNT_NUMBER_LENGTH)
    public String getAccountNumber() {
        return _accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        _accountNumber = accountNumber;
    }

    @Column(name="amount",nullable=false)
    public int getAmount() {
        return _amount;
    }

    public void setAmount(int amount) {
        _amount = amount;
    }
}

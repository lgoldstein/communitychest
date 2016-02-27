package com.vmware.spring.workshop.dto.banking;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.vmware.spring.workshop.dto.AbstractIdentifiedDTO;

/**
 * @author lgoldstein
 */
@XmlRootElement(name="account")
@XmlType(name="account")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class AccountDTO extends AbstractIdentifiedDTO implements Cloneable {
	private static final long serialVersionUID = -8496018899421781852L;

	private String	_accountNumber;
	private Long	_ownerId;
	private Long	_branchId;
	private int		_amount;

	public AccountDTO() {
		super();
	}

	@XmlAttribute(name="ownerId",required=true)
	public Long getOwnerId() {
		return _ownerId;
	}

	public void setOwnerId(Long ownerId) {
		_ownerId = ownerId;
	}

	@XmlAttribute(name="branchId",required=true)
	public Long getBranchId() {
		return _branchId;
	}

	public void setBranchId(Long branchId) {
		_branchId = branchId;
	}

	@XmlAttribute(name="accountNumber",required=true)
	public String getAccountNumber() {
		return _accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		_accountNumber = accountNumber;
	}

	@XmlAttribute(name="amount",required=true)
	public int getAmount() {
		return _amount;
	}

	public void setAmount(int amount) {
		_amount = amount;
	}

	@Override
	public AccountDTO clone() {
		try {
			return getClass().cast(super.clone());
		} catch(CloneNotSupportedException e) {
			throw new RuntimeException("Failed to clone " + this + ": " + e.getMessage(), e);
		}
	}
}

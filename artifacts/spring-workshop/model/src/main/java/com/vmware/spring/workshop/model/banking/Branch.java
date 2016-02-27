package com.vmware.spring.workshop.model.banking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.apache.commons.lang3.ObjectUtils;

import com.vmware.spring.workshop.model.AbstractNamedIdentified;
import com.vmware.spring.workshop.model.GeoPosition;
import com.vmware.spring.workshop.model.Identified;
import com.vmware.spring.workshop.model.Located;

/**
 * @author lgoldstein
 */
@Entity(name="Branch")
@NamedQueries({
	@NamedQuery(name="Branch.findByBranchCode",query="SELECT b FROM Branch b WHERE b.branchCode = :code"),
	@NamedQuery(name="Branch.findByBranchName",query="SELECT b FROM Branch b WHERE b.name = :name"),
	@NamedQuery(name="Branch.findByBranchLocation",query="SELECT b FROM Branch b WHERE LOWER(b.location) LIKE LOWER(:location)"),
	@NamedQuery(name="Branch.findByBranchBankCode",query="SELECT b FROM Branch b WHERE b.bank.bankCode = :code"),
	@NamedQuery(name="Branch.findByBankId",query="SELECT b FROM Branch b WHERE b.bank." + Identified.ID_COL_NAME + " = :" + Identified.ID_COL_NAME)
})
public class Branch extends AbstractNamedIdentified implements Located {
	private static final long serialVersionUID = 8093376341193340635L;
	private String	_location;
	private int		_branchCode;
	private Bank	_bank;
	private GeoPosition	_position;

	public Branch() {
		super();
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="bankId",nullable=false,referencedColumnName=ID_COL_NAME)
	public Bank getBank() {
		return _bank;
	}

	public void setBank(Bank bank) {
		_bank = bank;
	}

	@Column(name="branchCode",nullable=false)
	public int getBranchCode() {
		return _branchCode;
	}

	public void setBranchCode(int branchCode) {
		_branchCode = branchCode;
	}

	@Override
	@Column(name="location",nullable=false,unique=false,length=MAX_LOCATION_LENGTH)
	public String getLocation() {
		return _location;
	}

	@Override
	public void setLocation(String location) {
		_location = location;
	}

	@Override
	@Embedded
	public GeoPosition getPosition() {
		return _position;
	}

	@Override
	public void setPosition(GeoPosition position) {
		_position = position;
	}

	public static final List<Branch> findByBank (final Bank bank, final Iterable<? extends Branch> branches) {
		if ((bank == null) || (branches == null))
			return Collections.emptyList();

		final List<Branch>	result=new ArrayList<Branch>();
		for (final Branch branch : branches) {
			final Bank	branchOwner=(branch == null) ? null : branch.getBank();
			if (ObjectUtils.equals(bank, branchOwner))
				result.add(branch);
		}

		return result;
	}
	
}

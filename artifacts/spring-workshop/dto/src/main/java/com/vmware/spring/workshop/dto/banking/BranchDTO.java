package com.vmware.spring.workshop.dto.banking;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.vmware.spring.workshop.dto.AbstractNamedIdentifiedDTO;
import com.vmware.spring.workshop.dto.LocatedDTO;

/**
 * @author lgoldstein
 */
@XmlRootElement(name="branch")
@XmlType(name="branch")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class BranchDTO extends AbstractNamedIdentifiedDTO implements LocatedDTO, Cloneable {
    private static final long serialVersionUID = 2940429393498337775L;

    private String    _location;
    private int        _branchCode;
    private Long    _bankId;

    public BranchDTO() {
        super();
    }

    @XmlAttribute(name="bankId",required=true)
    public Long getBankId () {
        return _bankId;
    }

    public void setBankId(Long bankId) {
        _bankId = bankId;
    }

    @XmlAttribute(name="branchCode",required=true)
    public int getBranchCode() {
        return _branchCode;
    }

    public void setBranchCode(int branchCode) {
        _branchCode = branchCode;
    }

    @XmlElement(name="location",required=true,nillable=false)
    public String getLocation() {
        return _location;
    }

    @Override
    public void setLocation(String location) {
        _location = location;
    }

    @Override
    public BranchDTO clone() {
        try {
            return getClass().cast(super.clone());
        } catch(CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone " + this + ": " + e.getMessage(), e);
        }
    }
}

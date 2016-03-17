package com.vmware.spring.workshop.dto.banking;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.vmware.spring.workshop.dto.AbstractNamedIdentifiedDTO;
import com.vmware.spring.workshop.dto.LocatedDTO;

/**
 * @author lgoldstein
 */
@XmlRootElement(name="bank")
@XmlType(name="bank")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class BankDTO extends AbstractNamedIdentifiedDTO implements LocatedDTO, Cloneable {
    private static final long serialVersionUID = 8973059162535607271L;

    private String    _hqAddress;
    private int    _bankCode;

    public BankDTO() {
        super();
    }

    @XmlAttribute(name="bankCode",required=true)
    public int getBankCode() {
        return _bankCode;
    }

    public void setBankCode(int bankCode) {
        _bankCode = bankCode;
    }

    @XmlElement(name="hqAddress",required=true,nillable=false)
    public String getHqAddress() {
        return _hqAddress;
    }

    public void setHqAddress(String hqAddress) {
        _hqAddress = hqAddress;
    }

    @Override
    @XmlTransient
    public String getLocation() {
        return getHqAddress();
    }

    @Override
    public void setLocation(String location) {
        setHqAddress(location);
    }

    @Override
    public BankDTO clone() {
        try {
            return getClass().cast(super.clone());
        } catch(CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone " + this + ": " + e.getMessage(), e);
        }
    }
}

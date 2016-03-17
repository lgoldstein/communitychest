package com.vmware.spring.workshop.model.banking;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import com.vmware.spring.workshop.model.AbstractNamedIdentified;
import com.vmware.spring.workshop.model.GeoPosition;
import com.vmware.spring.workshop.model.Located;

/**
 * @author lgoldstein
 */
@Entity(name="Bank")
@NamedQueries({
    @NamedQuery(name="Bank.findBankByName",query="SELECT b FROM Bank b WHERE b.name = :name"),
    @NamedQuery(name="Bank.findByBankCode",query="SELECT b FROM Bank b WHERE b.bankCode = :code"),
    @NamedQuery(name="Bank.findByBankLocation",query="SELECT b FROM Bank b WHERE LOWER(b.hqAddress) LIKE LOWER(:location)")
})
public class Bank extends AbstractNamedIdentified implements Located {
    private static final long serialVersionUID = -2411126072852051556L;
    private String    _hqAddress;
    private int    _bankCode;
    private GeoPosition    _position;

    public Bank() {
        super();
    }

    @Column(name="bankCode",unique=true,nullable=false)
    public int getBankCode() {
        return _bankCode;
    }

    public void setBankCode(int bankCode) {
        _bankCode = bankCode;
    }

    public static final int    MAX_HQ_ADDRESS_LENGTH=MAX_LOCATION_LENGTH;
    @Column(name="hqAddress",unique=false,nullable=false,length=MAX_HQ_ADDRESS_LENGTH)
    public String getHqAddress() {
        return _hqAddress;
    }

    public void setHqAddress(String hqAddress) {
        _hqAddress = hqAddress;
    }

    @Override
    @Transient
    public String getLocation() {
        return getHqAddress();
    }

    @Override
    public void setLocation(String location) {
        setHqAddress(location);
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
}

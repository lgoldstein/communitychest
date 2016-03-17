package com.vmware.spring.workshop.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * @author lgoldstein
 */
@MappedSuperclass
public class AbstractNamedIdentified extends AbstractIdentified implements Named {
    private static final long serialVersionUID = 4246186579314819874L;

    private String    _name;
    protected AbstractNamedIdentified() {
        super();
    }

    @Column(name=NAME_COL_NAME, nullable=false, unique=true, length=MAX_NAME_LENGTH)
    @Override
    public String getName() {
        return _name;
    }

    @Override
    public void setName(String name) {
        _name = name;
    }


}

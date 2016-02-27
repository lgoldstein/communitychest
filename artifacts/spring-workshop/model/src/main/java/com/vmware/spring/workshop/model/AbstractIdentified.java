package com.vmware.spring.workshop.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

/**
 * @author lgoldstein
 */
@MappedSuperclass
public abstract class AbstractIdentified extends BaseEntity implements Identified {
	private static final long serialVersionUID = 7260999813491347937L;
	private Long	_id;
	private int		_version;

	protected AbstractIdentified() {
		super();
	}

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name=ID_COL_NAME,nullable=false,unique=true)
	@Override
	public Long getId() {
		return _id;
	}

	@Override
	public void setId(Long id) {
		_id = id;
	}

	@Version
	@Column(name="version", nullable=false)
	public int getVersion() {
		return _version;
	}

	public void setVersion (int version) {
		_version = version;
	}

	protected static final String[]	_XCLUDED_FIELDS={ "_version" };
	@Override
	protected String[] excludedFields () {
		return _XCLUDED_FIELDS;
	}
}

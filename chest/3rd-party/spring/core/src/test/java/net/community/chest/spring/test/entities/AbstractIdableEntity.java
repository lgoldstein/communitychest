/*
 * 
 */
package net.community.chest.spring.test.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Lyor G.
 * @since Jul 21, 2010 8:41:43 AM
 */
@MappedSuperclass
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlType(name="AbstractIdableEntity")
public abstract class AbstractIdableEntity implements Serializable, IdableEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3910459642397451803L;
	private Long	_id;
	/*
	 * @see net.community.chest.spring.test.entities.IdableEntity#getId()
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Override
	public Long getId ()
	{
		return _id;
	}
	/*
	 * @see net.community.chest.spring.test.entities.IdableEntity#setId(java.lang.Long)
	 */
	@Override
	public void setId (Long id)
	{
		_id = id;
	}

	private int	_version;
	@Version
	@Column(nullable=false)
	public int getVersion ()
	{
		return _version;
	}

	public void setVersion (int version)
	{
		_version = version;
	}

	protected AbstractIdableEntity (Long id)
	{
		_id = id;
	}
	
	protected AbstractIdableEntity ()
	{
		this(null);
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		final Long	id=getId();
		return (null == id) ? 0 : id.intValue();
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof IdableEntity))
			return false;
		if (this == obj)
			return true;

		final Long	tid=getId(), oid=((IdableEntity) obj).getId();
		if (null == tid)
			return (null == oid);
		else
			return tid.equals(oid);
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return getClass().getSimpleName() + "[id=" + getId() + "]";
	}
}

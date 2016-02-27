/*
 * 
 */
package net.community.chest.spring.test.entities;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import net.community.chest.lang.StringUtil;

/**
 * @author Lyor G.
 * @since Jul 21, 2010 8:45:39 AM
 */
@MappedSuperclass
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlType(name="AbstractBaseEntity")
public abstract class AbstractBaseEntity extends AbstractIdableEntity
		implements NamedEntity, DescribableEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4199475160249436193L;
	private String	_name;
	/*
	 * @see net.community.chest.spring.test.entities.NamedEntity#getName()
	 */
	@Override
	@Column(nullable=false, length=MAX_NAME_LENGTH, unique=true)
	@NotNull
	@Size(min=1, max=MAX_NAME_LENGTH)
	public String getName ()
	{
		return _name;
	}
	/*
	 * @see net.community.chest.spring.test.entities.NamedEntity#setName(java.lang.String)
	 */
	@Override
	public void setName (String n)
	{
		_name = n;
	}

	private String _desc;
	/*
	 * @see net.community.chest.spring.test.entities.DescribableEntity#getDescription()
	 */
	@Override
	@Column(nullable=true, length=MAX_DESCRIPTION_LENGTH)
	@Size(min=0, max=MAX_DESCRIPTION_LENGTH)
	public String getDescription ()
	{
		return _desc;
	}
	/*
	 * @see net.community.chest.spring.test.entities.DescribableEntity#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription (String d)
	{
		_desc = d;
	}

	protected AbstractBaseEntity (Long id, String name, String desc)
	{
		super(id);
		_name = name;
		_desc = desc;
	}
	
	protected AbstractBaseEntity (Long id)
	{
		this(id, null, null);
	}
	
	protected AbstractBaseEntity ()
	{
		this(null);
	}
	/*
	 * @see net.community.chest.spring.test.entities.AbstractIdableEntity#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return super.hashCode()
			+ StringUtil.getDataStringHashCode(getName(), false)
			;
	}
	/*
	 * @see net.community.chest.spring.test.entities.AbstractIdableEntity#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof NamedEntity))
			return false;
		if (!super.equals(obj))
			return false;
		if (this == obj)
			return true;

		final String	tn=getName(), on=((NamedEntity) obj).getName();
		return (0 == StringUtil.compareDataStrings(tn, on, false));
	}
	/*
	 * @see net.community.chest.spring.test.entities.AbstractIdableEntity#toString()
	 */
	@Override
	public String toString ()
	{
		return super.toString()
			+ ";name=" + getName()
			+ ";desc=" + getDescription()
			;
	}
}

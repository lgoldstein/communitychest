/*
 * 
 */
package net.community.chest.aspectj.test;

import javax.persistence.Column;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 31, 2011 8:26:57 AM
 */
public interface Nameable {
	void setName (String name);
	String getName ();
	
	static aspect Impl {
		@Column(nullable=false, unique=true, length=Byte.MAX_VALUE)
		private String Nameable._name;
		public void Nameable.setName (String name)
		{
			this._name = name;
		}

		public String Nameable.getName ()
		{
			return this._name;
		}
	}
}

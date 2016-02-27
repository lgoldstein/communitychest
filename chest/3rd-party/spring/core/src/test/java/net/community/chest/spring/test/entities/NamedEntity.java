/*
 * 
 */
package net.community.chest.spring.test.entities;

/**
 * @author Lyor G.
 * @since Jul 21, 2010 8:46:06 AM
 */
public interface NamedEntity {
	static final int	MAX_NAME_LENGTH=224;

	String getName ();
	void setName (String n);
}

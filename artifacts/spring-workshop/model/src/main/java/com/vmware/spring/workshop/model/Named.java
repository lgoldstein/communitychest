package com.vmware.spring.workshop.model;

/**
 * @author lgoldstein
 */
public interface Named {
	static final String	NAME_COL_NAME="name"; 
	static final int	MAX_NAME_LENGTH=255;

	String getName ();
	void setName (String name);
}

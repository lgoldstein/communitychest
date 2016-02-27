/**
 * 
 */
package com.vmware.spring.workshop.model;

/**
 * @author lgoldstein
 */
public interface Identified {
	static final String	ID_COL_NAME="id";
	Long getId ();
	void setId (Long id);
}

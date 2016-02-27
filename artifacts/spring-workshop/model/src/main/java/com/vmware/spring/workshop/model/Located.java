package com.vmware.spring.workshop.model;

/**
 * @author lgoldstein
 */
public interface Located {
	static final int	MAX_LOCATION_LENGTH=1024;

	String getLocation ();
	void setLocation (String location);

	GeoPosition getPosition ();
	void setPosition(GeoPosition position);
}

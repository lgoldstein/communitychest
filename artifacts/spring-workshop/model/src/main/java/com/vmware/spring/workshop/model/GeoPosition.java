package com.vmware.spring.workshop.model;

import javax.persistence.Embeddable;

/**
 * @author lgoldstein
 */
@Embeddable
public class GeoPosition extends BaseEntity implements Cloneable {
    private static final long serialVersionUID = -5212679011654781685L;
    private    String    _latitude, _longitude, _elevation;
    public GeoPosition() {
        super();
    }

    public String getLatitude() {
        return _latitude;
    }

    public void setLatitude(String latitude) {
        _latitude = latitude;
    }

    public String getLongitude() {
        return _longitude;
    }

    public void setLongitude(String longitude) {
        _longitude = longitude;
    }

    public String getElevation() {
        return _elevation;
    }

    public void setElevation(String elevation) {
        _elevation = elevation;
    }

    @Override
    public GeoPosition clone()  {
        try {
            return getClass().cast(super.clone());
        } catch(CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone: " + toString() + ": " + e, e);
        }
    }
}

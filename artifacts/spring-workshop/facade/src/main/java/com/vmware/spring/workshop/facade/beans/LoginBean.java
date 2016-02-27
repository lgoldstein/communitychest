package com.vmware.spring.workshop.facade.beans;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author lgoldstein
 */
public class LoginBean implements Authentication, Cloneable {
	private static final long serialVersionUID = 4326481843641289222L;

	private String	_username, _password;
	public LoginBean() {
		super();
	}

	public String getUsername() {
		return _username;
	}

	public void setUsername(String username) {
		_username = username;
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String password) {
		_password = password;
	}

	@Override
	public String getName() {
		return getUsername();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.emptyList();
	}

	@Override
	public String getCredentials() {
		return getPassword();
	}

	@Override
	public String getDetails() {
		return getName();
	}

	@Override
	public String getPrincipal() {
		return getUsername();
	}

	@Override
	public boolean isAuthenticated() {
		return false;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated)
			throws IllegalArgumentException {
		if (isAuthenticated)
			throw new IllegalArgumentException("This bean cannot be set as authenticated");
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;

		if (!EqualsBuilder.reflectionEquals(this, obj, false))
			return false;
		else
			return true;
	}

	@Override
	public LoginBean clone() {
		try {
			return getClass().cast(super.clone());
		} catch(CloneNotSupportedException e) {
			throw new RuntimeException("Failed to clone " + this + ": " + e.getMessage(), e);
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

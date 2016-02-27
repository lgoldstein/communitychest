package com.vmware.spring.workshop.services.auth.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.model.user.User;
import com.vmware.spring.workshop.model.user.UserRoleType;
import com.vmware.spring.workshop.services.auth.CleartextAuthenticationProvider;

/**
 * @author lgoldstein
 */
public abstract class AbstractCleartextAuthenticationProvider implements CleartextAuthenticationProvider {
	protected final Logger	_logger=LoggerFactory.getLogger(getClass());
	protected AbstractCleartextAuthenticationProvider() {
		super();
	}

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		Assert.notNull(authentication, "No authentication data provided");
		final String	username=authentication.getName();
		final Object	credentials=authentication.getCredentials();
		if (StringUtils.isBlank(username) || (!(credentials instanceof String)))
			return null;

		final User	user=authenticate(username, (String) credentials);
		if (user == null)
			return null;

		final UserRoleType	role=user.getRole();
		Assert.state(role != null, "No authenticated user role");

		return new UsernamePasswordAuthenticationToken(user, credentials, AuthorityUtils.createAuthorityList(role.name()));
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return (authentication != null) && Authentication.class.isAssignableFrom(authentication);
	}

}

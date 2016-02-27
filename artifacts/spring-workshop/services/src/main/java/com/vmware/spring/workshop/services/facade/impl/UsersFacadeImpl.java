package com.vmware.spring.workshop.services.facade.impl;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.dao.api.UserDao;
import com.vmware.spring.workshop.dto.user.UserDTO;
import com.vmware.spring.workshop.dto.user.UserRoleTypeDTO;
import com.vmware.spring.workshop.model.user.User;
import com.vmware.spring.workshop.services.auth.CleartextAuthenticationProvider;
import com.vmware.spring.workshop.services.convert.UserDTOConverter;
import com.vmware.spring.workshop.services.facade.Facade;
import com.vmware.spring.workshop.services.facade.UsersFacade;

/**
 * @author lgoldstein
 */
@Facade("usersFacade")
@Transactional
public class UsersFacadeImpl
		extends AbstractCommonFacadeActions<User,UserDTO,UserDao,UserDTOConverter>
		implements UsersFacade {
	private final Collection<? extends CleartextAuthenticationProvider>	_authProviders;

	@Inject
	public UsersFacadeImpl(final UserDao 												daoUser,
						   final UserDTOConverter										usrConverter,
						   final Collection<? extends CleartextAuthenticationProvider>	authProviders) {
		super(UserDTO.class, User.class, daoUser, usrConverter);
		
		Assert.state(!CollectionUtils.isEmpty(authProviders), "No authentication providers");
		_authProviders = Collections.unmodifiableCollection(authProviders);
	}

	@Override
	@Transactional(readOnly=true)
	public UserDTO findByLoginName(String loginName) {
		return _converter.toDTO(_dao.findByLoginName(loginName));
	}

	@Override
	public UsernamePasswordAuthenticationToken authenticate(Authentication authentication)
			throws AuthenticationException {
		final String	username=authentication.getName();
		final Object	password=authentication.getCredentials();
		if (StringUtils.isBlank(username) || (!(password instanceof String)))
			throw new BadCredentialsException("Bad credentials");
		for (final CleartextAuthenticationProvider provider : _authProviders) {
			final User		user=provider.authenticate(username, (String) password);
			final UserDTO	dto=_converter.toDTO(user);
			if (dto == null)
				continue;

			final UserRoleTypeDTO	role=dto.getRole();
			Assert.state(role != null, "No authenticated user role");

			final GrantedAuthority				ga=new SimpleGrantedAuthority(role.name());
			final Collection<GrantedAuthority>	gaList=Collections.singletonList(ga);
			return new UsernamePasswordAuthenticationToken(dto, password, gaList);
		}

		// this point is reached if no success
		throw new BadCredentialsException("Bad credentials");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return (authentication != null) && Authentication.class.isAssignableFrom(authentication);
	}

}

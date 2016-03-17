package com.vmware.spring.workshop.services.auth.impl;

import java.util.Collection;

import javax.inject.Inject;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.dao.api.UserDao;
import com.vmware.spring.workshop.model.user.User;
import com.vmware.spring.workshop.model.user.UserRoleType;

/**
 * @author lgoldstein
 */
@Service("internalUserAuthenticator")
public class InternalUserAuthenticator
        extends AbstractCleartextAuthenticationProvider
        implements UserDetailsService {
    private final UserDao    _daoUser;

    @Inject
    public InternalUserAuthenticator(final UserDao daoUser) {
        _daoUser = daoUser;
    }

    @Override
    public User authenticate(String username, String password)
            throws AuthenticationException {
        User    user=_daoUser.findByLoginName(username);
        if (user == null)
            return null;

        if (password.equals(user.getPassword()))
            return user;

        throw new BadCredentialsException("Bad credentials");
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        Assert.hasText(username, "No username");

        final User    user=_daoUser.findByLoginName(username);
        if (user == null)
            throw new UsernameNotFoundException("No such user");

        final UserRoleType                                role=user.getRole();
        final Collection<? extends GrantedAuthority>    auths=AuthorityUtils.createAuthorityList(role.name());
        return new UserDetails() {
            private static final long serialVersionUID = 2335354574366723003L;

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return auths;
            }

            @Override
            public String getPassword() {
                return user.getPassword();
            }

            @Override
            public String getUsername() {
                return user.getLoginName();
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        };
    }

}

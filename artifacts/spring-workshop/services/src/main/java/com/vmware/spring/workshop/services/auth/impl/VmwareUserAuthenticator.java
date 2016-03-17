package com.vmware.spring.workshop.services.auth.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SimpleDirContextAuthenticationStrategy;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.vmware.spring.workshop.model.user.User;
import com.vmware.spring.workshop.model.user.UserRoleType;
import com.vmware.spring.workshop.services.ExceptionUtils;

/**
 * @author lgoldstein
 */
@Service("vmwareUserAuthenticator")
public class VmwareUserAuthenticator extends AbstractCleartextAuthenticationProvider {
    private final String    _ldapAccessURL,    _userSearchFilter;

    @Inject
    public VmwareUserAuthenticator(
            @Value("${vmware.ldap.accessURL}")              final String ldapAccessURL,
            @Value("${vmware.ldap.userSearchFilter}")    final String userSearchFilter){
        Assert.state(StringUtils.hasText(ldapAccessURL), "No LDAP access URL provided");
        Assert.state(StringUtils.hasText(userSearchFilter), "No user search filter provided");

        _ldapAccessURL = ldapAccessURL;
        _userSearchFilter = userSearchFilter;
    }

    @Override
    public User authenticate(final String username, final String password)
            throws AuthenticationException {
        final String            authToken=username + "@vmware.com";
        final LdapContextSource    ctxSource=
                new DefaultSpringSecurityContextSource(_ldapAccessURL);
        ctxSource.setAuthenticationStrategy(new SimpleDirContextAuthenticationStrategy() {
                @Override
                public void setupEnvironment (@SuppressWarnings("rawtypes") Hashtable env, String userDn, String bindPassword)
                {
                    super.setupEnvironment(env, authToken, password);
                }
            });
        ctxSource.setAuthenticationSource(new AuthenticationSource() {
                @Override
                public String getPrincipal ()
                {
                    return authToken;
                }

                @Override
                public String getCredentials ()
                {
                    return password;
                }
            });
        try {
            ctxSource.afterPropertiesSet();
        } catch(Exception t) {
            final RuntimeException e=ExceptionUtils.toRuntimeException(t);
            _logger.error("Failed (" + e.getClass().getSimpleName() + " to initialized LDAP context: " + e.getMessage(), e);
            throw e;
        }

        final LdapUserSearch        userSearch=new FilterBasedLdapUserSearch("", _userSearchFilter, ctxSource);
        final DirContextOperations    ops;
        try {
            if ((ops=userSearch.searchForUser(authToken)) == null)
                throw new UsernameNotFoundException("Null context returned");
        } catch(UsernameNotFoundException e) {
            return null;
        }

        try {
            return createUser(username, ops);
        } catch(NamingException e) {
            throw new AuthenticationServiceException("Failed to extract user attributes", e);
        }
    }

    User createUser (final String username, final DirContextOperations ops) throws NamingException {
        Assert.notNull(ops, "No context provided");
        return createUser(username, ops.getAttributes());
    }

    // attributes used to extract information form the LDAP response
    static final String    ACCOUNT_ATTR="sAMAccountName",
                        DISPNAME_ATTR="displayName",
                        STREETADDR_ATTR="streetAddress",
                        CITY_ATTR="l",
                        COUNTRY_ATTR="co";
    static final Long    DUMMY_USER_ID=Long.valueOf(-1L);
    User createUser (final String username, final Attributes attrs) throws NamingException {
        final User                            user=new User();
        final Map<LocationValues,String>    locVals=new EnumMap<LocationValues,String>(LocationValues.class);
        for (final NamingEnumeration<? extends Attribute>    attrVals=attrs.getAll();
             (attrVals != null) && attrVals.hasMore(); ) {
            final Attribute    a=attrVals.next();
            final String    attrID=a.getID();
            final Object    attrVal=a.get();
            if (_logger.isDebugEnabled())
                _logger.debug("createUser(" + username + "): " + attrID + " = " + attrVal);
            if (attrVal == null)
                continue;

            if (ACCOUNT_ATTR.equalsIgnoreCase(attrID))
                user.setLoginName(attrVal.toString());
            if (DISPNAME_ATTR.equalsIgnoreCase(attrID))
                user.setName(attrVal.toString());
            else if (STREETADDR_ATTR.equalsIgnoreCase(attrID))
                updateStreetAddress(locVals, attrVal.toString());
            else if (CITY_ATTR.equalsIgnoreCase(attrID))
                updateCity(locVals, attrVal.toString());
            else if (COUNTRY_ATTR.equalsIgnoreCase(attrID))
                updateCountry(locVals, attrVal.toString());
        }

        user.setId(DUMMY_USER_ID);
        user.setRole(UserRoleType.GUEST);
        user.setLocation(buildLocation(locVals));

        if (!StringUtils.hasText(user.getLoginName())) {
            _logger.warn("createUser(" + username + ") no login name");
            user.setLoginName(username);
        }

        if (!StringUtils.hasText(user.getName())) {
            _logger.warn("createUser(" + username + ") no display name");
            user.setName(username);
        }

        if (_logger.isDebugEnabled())
            _logger.debug("createUser(" + username + "): name=" + user.getName() + ", location=" + user.getLocation());

        if (!StringUtils.hasText(user.getLocation())) {
            _logger.warn("createUser(" + username + ") no location");
        }

        return user;
    }

    static final String buildLocation (final Map<LocationValues,String> locVals) {
        if (MapUtils.isEmpty(locVals))
            return null;

        final StringBuilder    sb=new StringBuilder(Byte.MAX_VALUE);
        for (final LocationValues locVal : LocationValues.VALUES) {
            final String    locString=locVals.get(locVal);
            if (!StringUtils.hasText(locString))
                continue;
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(locString);
        }

        if (sb.length() <= 0)
            return null;
        else
            return sb.toString();
    }

    static final String updateCountry (final Map<LocationValues,String> locVals, final String country) {
        return updateLocationValue(locVals, LocationValues.COUNTRY, country);
    }

    static final String updateCity (final Map<LocationValues,String> locVals, final String city) {
        return updateLocationValue(locVals, LocationValues.CITY, city);
    }

    static final String updateStreetAddress (final Map<LocationValues,String> locVals, final String streetAddress) {
        if (!StringUtils.hasText(streetAddress))
            return null;

        int    lastPos=streetAddress.indexOf('\r');
        if (lastPos < 0)
            lastPos = streetAddress.indexOf('\n');
        if (lastPos < 0)
            lastPos = streetAddress.length();

        return updateLocationValue(locVals, LocationValues.STREET, streetAddress.substring(0, lastPos));
    }

    static final String updateLocationValue (final Map<LocationValues,String>    locVals,
                                             final LocationValues                locIndex,
                                             final String                        locString) {
        if (StringUtils.hasText(locString))
            return locVals.put(locIndex, locString);
        else
            return locVals.get(locIndex);
    }

    static enum LocationValues {
        STREET,
        CITY,
        COUNTRY;

        public static final List<LocationValues>    VALUES=
                Collections.unmodifiableList(Arrays.asList(values()));
    }
}

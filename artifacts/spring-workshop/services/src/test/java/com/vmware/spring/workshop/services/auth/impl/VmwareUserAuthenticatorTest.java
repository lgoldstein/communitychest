package com.vmware.spring.workshop.services.auth.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StringUtils;

import com.vmware.spring.workshop.model.user.User;
import com.vmware.spring.workshop.model.user.UserRoleType;
import com.vmware.spring.workshop.services.AbstractServicesTestSupport;
import com.vmware.spring.workshop.services.auth.CleartextAuthenticationProvider;

/**
 * @author lgoldstein
 */
@ContextConfiguration(locations={ AbstractServicesTestSupport.DEFAULT_TEST_CONTEXT })
@ActiveProfiles("hibernate")
public class VmwareUserAuthenticatorTest extends AbstractServicesTestSupport {
	@Inject @Named("vmwareUserAuthenticator") private CleartextAuthenticationProvider	_authenticator;
	public VmwareUserAuthenticatorTest() {
		super();
	}

	@Test
	public void testLDAPAuthentication () {
		final String	testUser=System.getProperty("vmware.ldap.test.username"),
						testPassword=System.getProperty("vmware.ldap.test.password");
		if ((!StringUtils.hasText(testUser)) && (!StringUtils.hasText(testPassword))) {
			logger.info("testLDAPAuthentication - skipped - no credentials");
			return;
		}

		Assert.assertNotNull("No authentication data returned", _authenticator.authenticate(testUser, testPassword));
	}

	@Test
	public void testLDAPUserInitialization () throws NamingException {
		final Attributes	TEST_ATTRS=new BasicAttributes(false);
		TEST_ATTRS.put(VmwareUserAuthenticator.ACCOUNT_ATTR, "testLDAPUserInitialization");
		TEST_ATTRS.put(VmwareUserAuthenticator.DISPNAME_ATTR, "test LDAP User Initialization");
		TEST_ATTRS.put(VmwareUserAuthenticator.STREETADDR_ATTR, "Sapir 3\r\nAmpa Bldg.");
		TEST_ATTRS.put(VmwareUserAuthenticator.CITY_ATTR, "Herzliya");
		TEST_ATTRS.put(VmwareUserAuthenticator.COUNTRY_ATTR, "Israel");

		final DirContextOperations	ops=Mockito.mock(DirContextOperations.class);
		Mockito.when(ops.getAttributes()).thenReturn(TEST_ATTRS);

		final VmwareUserAuthenticator	auth=new VmwareUserAuthenticator("dummy", "dummy");
		final User						user=auth.createUser("testUser", ops);
		Assert.assertSame("Mismatched user ID", VmwareUserAuthenticator.DUMMY_USER_ID, user.getId());
		Assert.assertEquals("Mismatched login name", TEST_ATTRS.get(VmwareUserAuthenticator.ACCOUNT_ATTR).get(), user.getLoginName());
		Assert.assertNull("Password not reset", user.getPassword());
		Assert.assertEquals("Mismatched display name", TEST_ATTRS.get(VmwareUserAuthenticator.DISPNAME_ATTR).get(), user.getName());
		Assert.assertSame("Mismatched role", UserRoleType.GUEST, user.getRole());
		Assert.assertEquals("Mismatched location", "Sapir 3, Herzliya, Israel", user.getLocation());
	}
}

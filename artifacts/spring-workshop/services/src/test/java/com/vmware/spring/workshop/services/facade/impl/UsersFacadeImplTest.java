package com.vmware.spring.workshop.services.facade.impl;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.vmware.spring.workshop.dto.user.UserDTO;
import com.vmware.spring.workshop.dto.user.UserRoleTypeDTO;
import com.vmware.spring.workshop.services.AbstractServicesTestSupport;
import com.vmware.spring.workshop.services.facade.AbstractFacadeTestSupport;
import com.vmware.spring.workshop.services.facade.FacadeValueFinder;
import com.vmware.spring.workshop.services.facade.UsersFacade;

/**
 * @author lgoldstein
 */
@ContextConfiguration(locations={ AbstractServicesTestSupport.DEFAULT_TEST_CONTEXT })
@ActiveProfiles("hibernate")
public class UsersFacadeImplTest extends AbstractFacadeTestSupport {
	@Inject private UsersFacade	_usrFacade;

	public UsersFacadeImplTest() {
		super();
	}

	@Test
	public void testFindByLoginName () {
		runFacadeValueFinderTest(new FacadeValueFinder<UsersFacade,UserDTO>() {
				@Override
				public UserDTO findDtoValue(UsersFacade facade, UserDTO dto) {
					return facade.findByLoginName(dto.getLoginName());
				}
			});
	}

	@Test
	public void testFindById () {
		runFacadeValueFinderTest(new FacadeValueFinder<UsersFacade,UserDTO>() {
				@Override
				public UserDTO findDtoValue(UsersFacade facade, UserDTO dto) {
					return facade.findById(dto.getId());
				}
			});
	}

	@Test
	public void testInternalUserAuthentication () {
		final Collection<? extends UserDTO>	dtoList=_usrFacade.findAll();
		Assert.assertFalse("No current users", CollectionUtils.isEmpty(dtoList));
		
		for (final UserDTO dto : dtoList) {
			final Authentication	authData=new UsernamePasswordAuthenticationToken(dto.getLoginName(), dto.getPassword()),
									authResult=_usrFacade.authenticate(authData);
			Assert.assertNotNull("No authentication result for " + dto, authResult);

			final Object	principal=authResult.getPrincipal();
			Assert.assertTrue("Principal not UserDTO for " + dto, principal instanceof UserDTO);
			Assert.assertEquals("Mismatched principal values", dto, principal);

			final Collection<? extends GrantedAuthority>	gaList=authResult.getAuthorities();
			Assert.assertFalse("No authorities for " + dto, CollectionUtils.isEmpty(gaList));
			Assert.assertEquals("Mismatched size of authorities for " + dto, 1, gaList.size());

			final GrantedAuthority	ga=gaList.iterator().next();
			final UserRoleTypeDTO	role=dto.getRole();
			Assert.assertEquals("Mismatched granted authority for " + dto, role.name(), ga.getAuthority());
		} 
	}

	private void runFacadeValueFinderTest (final FacadeValueFinder<UsersFacade,UserDTO> finder) {
		runFacadeValueFinderTest(_usrFacade, _usrFacade.findAll(), finder);
	}
}

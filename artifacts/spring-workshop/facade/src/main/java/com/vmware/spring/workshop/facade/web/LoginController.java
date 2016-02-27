package com.vmware.spring.workshop.facade.web;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.vmware.spring.workshop.dto.user.UserDTO;
import com.vmware.spring.workshop.dto.user.UserRoleTypeDTO;
import com.vmware.spring.workshop.facade.beans.LoginBean;

/**
 * @author lgoldstein
 */
@Controller("loginController")
@RequestMapping(value="/login")
public class LoginController extends AbstractWebController {
	private static final String	LOGIN_BEAN_ATTR_NAME="loginBean", LOGIN_VIEW="login";
	private final AuthenticationManager _securityProvider;

	@Inject
	public LoginController(@Named("usersFacade") final AuthenticationManager securityProvider) {
		_securityProvider = securityProvider;
	}

	@RequestMapping(method=RequestMethod.GET)
	public String createLoginForm (Model model) {
		model.addAttribute(LOGIN_BEAN_ATTR_NAME, new LoginBean());
		return LOGIN_VIEW;
	}

	@RequestMapping(method=RequestMethod.POST)
	public String authenticate (
			@Valid @ModelAttribute(LOGIN_BEAN_ATTR_NAME) final LoginBean 	loginBean,
								final BindingResult 		result,
								final HttpServletRequest	request,
								final Model					model) {
		if (result.hasErrors())
			return LOGIN_VIEW;

		// Authenticate user
		final UserDTO	dto=authenticateUser(loginBean, request, model);
		if (dto == null) {
			model.addAttribute("errorMessage", "Authentication failure. Please try again.");
			return LOGIN_VIEW;
		}

		final UserRoleTypeDTO	role=dto.getRole();
		return "redirect:users/" + role.name().toLowerCase();
	}

	UserDTO authenticateUser (final LoginBean loginBean, final HttpServletRequest request, final Model model) {
		try {
			// generate session if one doesn't exist
	        final HttpSession	session=request.getSession();
	        Assert.state(session != null, "No currently active HTTP session");

	        final Authentication authRes=_securityProvider.authenticate(loginBean);
			// Mark the HTTPSession authenticated 
			SecurityContextHolder.getContext().setAuthentication(authRes);

			final Object	principal=authRes.getPrincipal();
			Assert.state(principal instanceof UserDTO, "Authenticated user principal not a UserDTO");
			return (UserDTO) principal;
		} catch(AuthenticationException e) {
			return null;
		}
	}
}

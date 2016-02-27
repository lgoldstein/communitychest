package com.vmware.spring.workshop.facade.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * @author lgoldstein
 */
@Controller("welcomeController")
public class WelcomeController extends AbstractWebController {
	public WelcomeController() {
		super();
	}

	@RequestMapping(value="/home", method=RequestMethod.GET)
	public String welcome (
			@RequestHeader(value="User-Agent", required=false) final String userAgent,
																final Model model) {
		final UserAgents	agentType=UserAgents.findUserAgents(userAgent);
		model.addAttribute("browserType", agentType.name().toLowerCase());
		model.addAttribute("downloadLocation", agentType.getDownloadLocation());
		model.addAttribute("agentInfo", userAgent);
		return getTopLevelViewPath("welcome");
	}
}

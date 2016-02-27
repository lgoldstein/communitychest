/**
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.samples.rest.webmvc;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.springsource.insight.samples.rest.AbstractRequestHandler;
import com.springsource.insight.samples.rest.model.RestfulDataList;
import com.springsource.insight.samples.rest.model.RestfulService;

/**
 * @author lgoldstein
 */
@Controller
@RequestMapping("/webmvc")
public class RestfulController extends AbstractRequestHandler {
	private final RestfulService	_service;

	@Inject
	public RestfulController(final RestfulService service) {
		_service = service;
	}

	@RequestMapping(method=RequestMethod.GET)
	public String listAll (Model model, @RequestParam(value="delay", defaultValue=DEFAULT_DELAY) int maxDelay) {
		final RestfulDataList	list=_service.findAll();
		model.addAttribute("dataList", list);
		delay(maxDelay);
		return "webmvc/list";
	}
}

package com.vmware.spring.workshop.facade.restful.mvc;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.vmware.spring.workshop.dto.IdentifiedDTO;
import com.vmware.spring.workshop.facade.restful.AbstractRestfulService;
import com.vmware.spring.workshop.facade.support.views.GenericRESTView;
import com.vmware.spring.workshop.services.facade.CommonFacadeActions;

/**
 * @author lgoldstein
 */
public abstract class AbstractMVCRestfulService<DTO extends IdentifiedDTO, FCD extends CommonFacadeActions<DTO>>
			extends AbstractRestfulService<DTO, FCD> {
	public static final String	MVC_ACCESS_ROOT=RESTFUL_ACCESS_ROOT + "/mvc";

	@Inject @Named("genericRESTView") private View	_restView;
	@Inject @Named("genericXMLSchemaView") private View	_schemaView;

	protected AbstractMVCRestfulService(Class<DTO> dtoClass, FCD facade) {
		super(dtoClass, facade);
	}

	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView list () {
		return resolveModelAndView(wrapAsDTOList(_facade.findAll()));
	}

	@RequestMapping(method=RequestMethod.GET, value="/schema")
	public ModelAndView getSchema () {
		return resolveModelAndView(getDTOClass());
	}

	@RequestMapping(method=RequestMethod.GET, value="/" + BY_ID_TEMPLATE)
	public ModelAndView getInstance (@PathVariable(ID_PARAM_NAME) final Long id) {
		return resolveModelAndView(_facade.findById(id));
	}

	@RequestMapping(method=RequestMethod.DELETE, value="/" + BY_ID_TEMPLATE)
	public ModelAndView removeInstance (@PathVariable(ID_PARAM_NAME) final Long id) {
		final DTO	dto=_facade.deleteById(id);
		_logger.info("removeInstance(" + dto + ") removed");
		return resolveModelAndView(dto);
	}

	@RequestMapping(method=RequestMethod.POST)
	public ModelAndView createInstance (@RequestBody final DTO dto) {
		Assert.notNull(dto, "No DTO object");

		final Long	id=dto.getId();
		if (id != null) {
			dto.setId(null);
			_logger.warn("createInstance(" + dto + ") ignore preset ID=" + id);
		}

		_facade.create(dto);
		return list();
	}

	protected ModelAndView resolveModelAndView (final Object result) {
		final View	view=resolveView(result);
		return new ModelAndView(view, Collections.singletonMap(GenericRESTView.MODEL_VALUE_KEY, result));
	}

	protected View resolveView (final Object result) {
		return (result instanceof Class<?>) ? _schemaView : _restView;
	}
}

package org.springframework.ext.config;

import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.ext.resource.ResourceHttpRequestHandler;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.w3c.dom.Element;

public class ResourcesBeanDefinitionParser implements BeanDefinitionParser {
	public ResourcesBeanDefinitionParser ()
	{
		super();
	}

	private static final String HANDLER_ADAPTER_BEAN_NAME=
		org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter.class.getName();
	private static final String HANDLER_MAPPING_BEAN_NAME=
		"org.springframework.web.servlet.config.resourcesHandlerMapping";

	public static final String	RESOURCE_DIRECTORY_ATTR_NAME="directory",
									DEFAULT_RESOURCE_DIRECTORY="/resources/",
								RESOURCE_REQPATH_ATTR_NAME="request-path",
									DEFAULT_RESOURCE_REQPATH="/resources/**";
	/*
	 * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
	 */
	@Override
	public BeanDefinition parse (Element element, ParserContext parserContext)
	{
		Object source = parserContext.extractSource(element);

		registerHandlerAdapterIfNecessary(parserContext, source);
		BeanDefinition handlerMappingDef = registerHandlerMappingIfNecessary(parserContext, source);

		// check if have a resource directory override
		String resourceDirectory = element.getAttribute(RESOURCE_DIRECTORY_ATTR_NAME);
		if ((null == resourceDirectory) || (resourceDirectory.length() <= 0))
			resourceDirectory = DEFAULT_RESOURCE_DIRECTORY;

		RootBeanDefinition resourceHandlerDef = new RootBeanDefinition(ResourceHttpRequestHandler.class);
		resourceHandlerDef.setSource(source);
		resourceHandlerDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		resourceHandlerDef.getConstructorArgumentValues().addIndexedArgumentValue(0, resourceDirectory);
		
		Map<String, BeanDefinition> urlMap = getUrlMap(handlerMappingDef);
		String resourceRequestPath = element.getAttribute(RESOURCE_REQPATH_ATTR_NAME);
		if ((null == resourceRequestPath) || (resourceRequestPath.length() <= 0))
			resourceRequestPath = DEFAULT_RESOURCE_REQPATH;
		urlMap.put(resourceRequestPath, resourceHandlerDef);

		return null;
	}

	private void registerHandlerAdapterIfNecessary(ParserContext parserContext, Object source) {
		if (!parserContext.getRegistry().containsBeanDefinition(HANDLER_ADAPTER_BEAN_NAME)) {
			RootBeanDefinition handlerAdapterDef = new RootBeanDefinition(HttpRequestHandlerAdapter.class);
			handlerAdapterDef.setSource(source);
			handlerAdapterDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			parserContext.getRegistry().registerBeanDefinition(HANDLER_ADAPTER_BEAN_NAME, handlerAdapterDef);
			parserContext.registerComponent(new BeanComponentDefinition(handlerAdapterDef, HANDLER_ADAPTER_BEAN_NAME));
		}
	}
	
	private BeanDefinition registerHandlerMappingIfNecessary(ParserContext parserContext, Object source) {
		if (!parserContext.getRegistry().containsBeanDefinition(HANDLER_MAPPING_BEAN_NAME)) {
			RootBeanDefinition handlerMappingDef = new RootBeanDefinition(SimpleUrlHandlerMapping.class);
			handlerMappingDef.setSource(source);
			handlerMappingDef.getPropertyValues().add("order", "2");
			handlerMappingDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			parserContext.getRegistry().registerBeanDefinition(HANDLER_MAPPING_BEAN_NAME, handlerMappingDef);
			parserContext.registerComponent(new BeanComponentDefinition(handlerMappingDef, HANDLER_MAPPING_BEAN_NAME));
			return handlerMappingDef;
		}
		else {
			return parserContext.getRegistry().getBeanDefinition(HANDLER_MAPPING_BEAN_NAME);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, BeanDefinition> getUrlMap(BeanDefinition handlerMappingDef) {
		Map<String, BeanDefinition> urlMap;
		if (handlerMappingDef.getPropertyValues().contains("urlMap")) {
			urlMap = (Map<String, BeanDefinition>) handlerMappingDef.getPropertyValues().getPropertyValue("urlMap").getValue();
		}
		else {
			urlMap = new ManagedMap<String, BeanDefinition>();
			handlerMappingDef.getPropertyValues().add("urlMap", urlMap);
		}
		return urlMap;
	}

}

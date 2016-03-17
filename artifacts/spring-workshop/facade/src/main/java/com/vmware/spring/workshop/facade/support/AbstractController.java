package com.vmware.spring.workshop.facade.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.Assert;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

/**
 * @author lgoldstein
 */
public abstract class AbstractController implements ApplicationListener<ContextRefreshedEvent> {
    public static final String    ID_PARAM_NAME="id", BY_ID_TEMPLATE="{" + ID_PARAM_NAME + "}";
    private static final Set<ApplicationContext>    INITIALIZED_SETS=
            Collections.synchronizedSet(new HashSet<ApplicationContext>());
    protected final Logger    _logger=LoggerFactory.getLogger(getClass());

    protected AbstractController() {
        super();
    }

    @Override // (very) dirty hack to achieve this setting
    public void onApplicationEvent (final ContextRefreshedEvent event) {
        final ApplicationContext    context=event.getApplicationContext();
        if (!INITIALIZED_SETS.add(context)) {
            _logger.info("Context already initialized: " + context.getDisplayName());
            return;
        }

        final Map<String,? extends AbstractHandlerMapping>    mappings=context.getBeansOfType(AbstractHandlerMapping.class);
        Assert.state(!MapUtils.isEmpty(mappings), "No mappings handlers");

        for (final AbstractHandlerMapping handler : mappings.values()) {
            handler.setAlwaysUseFullPath(true);
        }

        _logger.info("Context mappings initialized: " + context.getDisplayName());
    }
}

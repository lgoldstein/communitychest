package org.springframework.ext.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * {@link org.springframework.beans.factory.xml.NamespaceHandler} for ext configuration namespace:
 * contains extension tags that will eventually be integrated back into Spring.
 *
 * @author Keith Donald
 * @since 3.0
 */
public class ExtNamespaceHandler extends NamespaceHandlerSupport {
    public ExtNamespaceHandler ()
    {
        super();
    }
    /*
     * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
     */
    @Override
    public void init ()
    {
        registerBeanDefinitionParser("resources", new ResourcesBeanDefinitionParser());
    }
}


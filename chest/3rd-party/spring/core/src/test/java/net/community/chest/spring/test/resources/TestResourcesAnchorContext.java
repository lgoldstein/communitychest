/*
 *
 */
package net.community.chest.spring.test.resources;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Serves as anchor to the resource files
 * @author Lyor G.
 * @since Jul 20, 2010 9:28:10 AM
 */
public class TestResourcesAnchorContext extends ClassPathXmlApplicationContext {

    public TestResourcesAnchorContext (String[] configLocations, boolean refresh, ApplicationContext parent)
        throws BeansException
    {
        super(configLocations, refresh, parent);
    }

    public TestResourcesAnchorContext (String[] configLocations, ApplicationContext parent)
        throws BeansException
    {
        this(configLocations, true, parent);
    }

    public TestResourcesAnchorContext (String[] configLocations, boolean refresh)
            throws BeansException
    {
        this(configLocations, refresh, null);
    }

    public TestResourcesAnchorContext (String... configLocations)
        throws BeansException
    {
        this(configLocations, true);
    }

    public TestResourcesAnchorContext (String configLocation)
        throws BeansException
    {
        this(new String[] {configLocation}, true);
    }

    public TestResourcesAnchorContext (String[] paths, Class<?> clazz, ApplicationContext parent)
        throws BeansException
    {
        super(paths, clazz, parent);
    }

    public TestResourcesAnchorContext (String[] paths, Class<?> clazz)
            throws BeansException
    {
        this(paths, clazz, null);
    }

    public TestResourcesAnchorContext (String path, Class<?> clazz)
        throws BeansException
    {
        this(new String[] {path}, clazz);
    }

    public static final String    DEFAULT_CONTEXT_FILE="application-testContext.xml";
    public TestResourcesAnchorContext ()
    {
        this(DEFAULT_CONTEXT_FILE, TestResourcesAnchorContext.class);
    }

    public TestResourcesAnchorContext (ApplicationContext parent)
    {
        this(new String[] { DEFAULT_CONTEXT_FILE }, TestResourcesAnchorContext.class, parent);
    }
}

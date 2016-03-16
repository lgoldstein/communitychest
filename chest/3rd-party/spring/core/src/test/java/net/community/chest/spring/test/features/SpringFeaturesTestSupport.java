/*
 *
 */
package net.community.chest.spring.test.features;

import org.junit.After;
import org.junit.Before;
import org.junit.ExtendedAssert;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 3, 2012 8:10:08 AM
 */
public class SpringFeaturesTestSupport extends ExtendedAssert implements ApplicationContextAware {
    private ApplicationContext _applicationContext;
    public SpringFeaturesTestSupport ()
    {
        super();
    }

    @Before
    public void setUp ()
    {
        closeCurrentContext();
        assertNull("Context not reset", _applicationContext);
    }

    @After
    public void tearDown ()
    {
        closeCurrentContext();
    }
    /**
     * 1. What happens when a bean implements the Initializing/DisposableBean
     * and also annotates the implemented methods with PostConstruct/PreDestroy
     *
     * 2. What happens when a bean implements the Initializing/DisposableBean
     * but it annotates different methods with PostConstruct/PreDestroy
     *
     * 3. What happens when a bean implements the Initializing/DisposableBean
     * but it inverts the PostConstruct/PreDestroy annotations
     */
    @Test
    public void testFullFeatureBean ()
    {
        runSimpleLookupTest(FullFeatureBean.class);
    }
    /**
     * What happens when a bean has both a stereotype annotation and is also
     * explicitly defined in the context
     */
    @Test(expected=NoSuchBeanDefinitionException.class) // the actual message is that there are duplicate matching beans
    public void testDuplicateDefinedBean ()
    {
        runSimpleLookupTest(DuplicateDefinedBean.class);
    }
    /**
     * What happens when the PostConstruct/PreDestroy are on one set of methods
     * but the XML specified other init/destroy-method(s) - both methods are called
     */
    @Test
    public void testDuplicateInitDestroyMethods ()
    {
        runSimpleLookupTest(DuplicateInitDestroyMethods.class);
    }

    private <T> T runSimpleLookupTest (Class<T> beanClass)
    {
        return runSimpleLookupTest(beanClass.getSimpleName() + ".xml", beanClass);
    }

    private <T> T runSimpleLookupTest (String name, Class<T> beanClass)
    {
        final AbstractApplicationContext    context=loadContext(name);
        final T                                beanInstance=context.getBean(beanClass);
        assertNotNull("Bean " + beanClass.getSimpleName() + " not found in " + name);
        return beanInstance;
    }

    private ClassPathXmlApplicationContext loadContext (String name)
    {
        final ClassPathXmlApplicationContext    context=new ClassPathXmlApplicationContext(name, getClass());
        setApplicationContext(context);
        return context;
    }

    @Override
    public void setApplicationContext (ApplicationContext applicationContext) throws BeansException
    {
        _applicationContext = applicationContext;
    }

    private ApplicationContext closeCurrentContext ()
    {
        final ApplicationContext    context=_applicationContext;
        if (context instanceof AbstractApplicationContext)
            ((AbstractApplicationContext) context).stop();
        _applicationContext = null;
        return context;
    }
}

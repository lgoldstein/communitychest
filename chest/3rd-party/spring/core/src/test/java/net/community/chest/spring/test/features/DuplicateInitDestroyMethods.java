/*
 *
 */
package net.community.chest.spring.test.features;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 3, 2012 9:51:49 AM
 */
public class DuplicateInitDestroyMethods implements InitializingBean, DisposableBean {
    public DuplicateInitDestroyMethods ()
    {
        super();
    }

    @Override
    @PostConstruct
    public void afterPropertiesSet () throws Exception
    {
        System.out.append(getClass().getSimpleName()).println("#afterPropertiesSet()");
    }

    public void xmlInit () throws Exception
    {
        System.out.append(getClass().getSimpleName()).println("#xmlInit()");
    }

    @Override
    @PreDestroy
    public void destroy () throws Exception
    {
        System.out.append(getClass().getSimpleName()).println("#destroy()");
    }

    public void xmlDestroy () throws Exception
    {
        System.out.append(getClass().getSimpleName()).println("#xmlDestroy()");
    }
}

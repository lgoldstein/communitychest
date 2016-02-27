/*
 * 
 */
package net.community.chest.spring.test.features;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 3, 2012 8:15:06 AM
 */
@Component
public class FullFeatureBean implements InitializingBean, DisposableBean {
	public FullFeatureBean ()
	{
		super();
	}

	@Override
	@PostConstruct
	public void afterPropertiesSet () throws Exception
	{
		System.out.append(getClass().getSimpleName()).println("#afterPropertiesSet()");
	}

	@Override
	@PreDestroy
	public void destroy () throws Exception
	{
		System.out.append(getClass().getSimpleName()).println("#destroy()");
	}
}

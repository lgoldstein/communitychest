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
 * @since Jan 3, 2012 10:08:56 AM
 */
@Component
public class MismatchedBeanLifecyleAnnotations  implements InitializingBean, DisposableBean  {
	public MismatchedBeanLifecyleAnnotations ()
	{
		super();
	}

	@Override
	public void afterPropertiesSet () throws Exception
	{
		System.out.append(getClass().getSimpleName()).println("#afterPropertiesSet()");
	}

	@PostConstruct
	public void postConstruct ()
	{
		System.out.append(getClass().getSimpleName()).println("#postConstruct()");
	}

	@Override
	public void destroy () throws Exception
	{
		System.out.append(getClass().getSimpleName()).println("#destroy()");
	}

	@PreDestroy
	public void preDestroy ()
	{
		System.out.append(getClass().getSimpleName()).println("#preDestroy()");
	}
}

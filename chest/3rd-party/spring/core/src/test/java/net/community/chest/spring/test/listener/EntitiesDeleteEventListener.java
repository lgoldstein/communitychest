/*
 * 
 */
package net.community.chest.spring.test.listener;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Resource;

import net.community.chest.io.EOLStyle;

import org.hibernate.HibernateException;
import org.hibernate.event.DeleteEvent;
import org.hibernate.event.def.DefaultDeleteEventListener;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Lyor G.
 * @since Jul 22, 2010 2:50:31 PM
 */
public class EntitiesDeleteEventListener extends DefaultDeleteEventListener
		implements BeanNameAware {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2557864448779173038L;
	public EntitiesDeleteEventListener ()
	{
		super();
	}
	/**
	 * The actual {@link Appendable} instance implementing the API
	 */
	private Appendable	_out;
	public Appendable getOutput ()
	{
		return _out;
	}
	/* NOTE: relies on type-based bean resolution - i.e., there
	 * 		is only ONE bean that matches the setter-s argument type
	 */
	@Resource	// see section 3.9.3 tip in Spring documentation 
	@Required
	public void setOutput (Appendable out)
	{
		_out = out;
	}

	private String	_beanName;
	public String getBeanName ()
	{
		return _beanName;
	}
	/*
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	@Override
	public void setBeanName (String name)
	{
		_beanName = name;
	}

	protected void finalizeDeletion (DeleteEvent event)  throws HibernateException
	{
		final Appendable	out=getOutput();
		if (null == out)
			throw new HibernateException("No " + Appendable.class.getSimpleName() + " instance provided");

		final Object	o=(null == event) ? null : event.getObject();
		if (o != null)
		{
			try
			{
				out.append("\t++> ")
				   .append(getBeanName())
				   .append("#finalizeDeletion(): ")
				   .append(o.toString())
				   .append(EOLStyle.LOCAL.getStyleString())
				   ;
				EntitiesContext.marshalObject(o, out)
							   .append(EOLStyle.LOCAL.getStyleString())
							   ;
			}
			catch(IOException e)
			{
				throw new HibernateException("Failed to generated object message", e);
			}
		}
	}
	/*
	 * @see org.hibernate.event.def.DefaultDeleteEventListener#onDelete(org.hibernate.event.DeleteEvent)
	 */
	@Override
	public void onDelete (DeleteEvent event) throws HibernateException
	{
		super.onDelete(event);
		finalizeDeletion(event);
	}
	/*
	 * @see org.hibernate.event.def.DefaultDeleteEventListener#onDelete(org.hibernate.event.DeleteEvent, java.util.Set)
	 */
	@Override
	public void onDelete (DeleteEvent event, @SuppressWarnings("rawtypes") Set transientEntities)
			throws HibernateException
	{
		super.onDelete(event, transientEntities);
		finalizeDeletion(event);
	}
}

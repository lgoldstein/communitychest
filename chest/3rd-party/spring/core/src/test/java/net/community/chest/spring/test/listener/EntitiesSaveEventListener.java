/*
 *
 */
package net.community.chest.spring.test.listener;

import java.io.IOException;

import javax.annotation.Resource;

import net.community.chest.io.EOLStyle;

import org.hibernate.HibernateException;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.def.DefaultSaveEventListener;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Lyor G.
 * @since Jul 22, 2010 2:33:47 PM
 *
 */
public class EntitiesSaveEventListener extends DefaultSaveEventListener
        implements BeanNameAware {
    /**
     *
     */
    private static final long serialVersionUID = 6917490551551475663L;
    public EntitiesSaveEventListener ()
    {
        super();
    }
    /**
     * The actual {@link Appendable} instance implementing the API
     */
    private Appendable    _out;
    public Appendable getOutput ()
    {
        return _out;
    }
    /* NOTE: relies on type-based bean resolution - i.e., there
     *         is only ONE bean that matches the setter-s argument type
     */
    @Resource    // see section 3.9.3 tip in Spring documentation
    @Required
    public void setOutput (Appendable out)
    {
        _out = out;
    }

    private String    _beanName;
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
    /*
     * @see org.hibernate.event.def.DefaultSaveOrUpdateEventListener#onSaveOrUpdate(org.hibernate.event.SaveOrUpdateEvent)
     */
    @Override
    public void onSaveOrUpdate (SaveOrUpdateEvent event)
        throws HibernateException
    {
        super.onSaveOrUpdate(event);

        final Appendable    out=getOutput();
        if (null == out)
            throw new HibernateException("No " + Appendable.class.getSimpleName() + " instance provided");

        final Object[]    oa={
                "object", (null == event) ? null : event.getObject(),
                "entity", (null == event) ? null : event.getEntity()
            };
        for (int    oIndex=0; oIndex < oa.length; oIndex += 2)
        {
            final Object    n=oa[oIndex], o=oa[oIndex + 1];
            if ((null == n) || (null == o))
                continue;

            try
            {
                out.append("\t++> ")
                   .append(getBeanName())
                   .append("#onSaveOrUpdate(")
                   .append(n.toString())
                   .append("): ")
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
        }    }

}

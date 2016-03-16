/*
 *
 */
package net.community.chest.spring.test.beans;

import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.inject.Inject;

import net.community.chest.spring.orm.hibernate3.AbstractGenericHibernateDaoImpl;
import net.community.chest.spring.test.entities.DateTimeEntity;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

/**
 * @author Lyor G.
 * @since Jul 21, 2010 1:17:25 PM
 */
@Repository
// NOTE !!! for some reason annotating this with @Transactional causes @Repository not to be detected
public class DateTimeEntityDao extends AbstractGenericHibernateDaoImpl<DateTimeEntity,Long> {
    public DateTimeEntityDao ()
    {
        super(DateTimeEntity.class, Long.class);
    }
    // Ugly hack since 'setSessionFactory' is 'final'
    @Inject
    public void setDaoSessionFactory (SessionFactory sessionFactory)
    {
        setSessionFactory(sessionFactory);
    }

    @Resource(name="testProperties")
    public void setTestProperties (Properties props)
    {
        if ((props == null) || (props.size() <= 0))
            return;

        for (final Map.Entry<?,?> pe : props.entrySet())
            System.out.append('\t').append(String.valueOf(pe.getKey()))
                      .append('=').append(String.valueOf(pe.getValue()))
                      .println()
                      ;
    }
    /*
     * @see net.community.chest.spring.dao.support.AbstractGenericDaoImpl#getDefaultOrderAttributeName()
     */
    @Override
    public String getDefaultOrderAttributeName ()
    {
        return "dateValue";
    }
}

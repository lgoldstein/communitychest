/*
 *
 */
package net.community.chest.spring.test.beans;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import net.community.chest.spring.orm.hibernate3.AbstractGenericHibernateDaoImpl;
import net.community.chest.spring.test.entities.NodeEntity;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 5, 2011 11:31:11 AM
 */
@Repository
//NOTE !!! for some reason annotating this with @Transactional causes @Repository not to be detected
public class NodeEntityDao extends AbstractGenericHibernateDaoImpl<NodeEntity,Long> {
    public NodeEntityDao ()
    {
        super(NodeEntity.class, Long.class);
    }
    // Ugly hack since 'setSessionFactory' is 'final'
    @Inject
    public void setDaoSessionFactory (SessionFactory sessionFactory)
    {
        setSessionFactory(sessionFactory);
    }
    /*
     * @see net.community.chest.spring.orm.hibernate3.AbstractGenericHibernateDaoImpl#getDefaultOrderAttributeName()
     */
    @Override
    public String getDefaultOrderAttributeName ()
    {
        return "description";
    }
}

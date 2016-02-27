/*
 * 
 */
package net.community.chest.spring.test.beans;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import net.community.chest.spring.orm.hibernate3.AbstractGenericHibernateDaoImpl;
import net.community.chest.spring.test.entities.EmbeddingEntity;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 20, 2011 10:29:51 AM
 */
@Repository
//NOTE !!! for some reason annotating this with @Transactional causes @Repository not to be detected
public class EmbeddingEntityDao extends AbstractGenericHibernateDaoImpl<EmbeddingEntity,Long> {
	public EmbeddingEntityDao ()
	{
		super(EmbeddingEntity.class, Long.class);
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
		return "name";
	}
}

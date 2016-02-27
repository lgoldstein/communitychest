/**
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.samples.rest.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

/**
 * @author lgoldstein
 */
@Repository
public class RestfulRepositoryImpl extends HibernateDaoSupport implements RestfulRepository {
	public RestfulRepositoryImpl() {
		super();
	}

	// Ugly hack since 'setSessionFactory' is 'final'
	@Inject
	public void setDaoSessionFactory (SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
	/*
	 * @see com.springsource.insight.samples.rest.RestfulRepository#findAll()
	 */
	@Override
	public List<RestfulData> findAll() {
		final HibernateTemplate	tmpl=getHibernateTemplate();
		return tmpl.loadAll(RestfulData.class);
	}
	/*
	 * @see com.springsource.insight.samples.rest.RestfulRepository#getData(long)
	 */
	@Override
	public RestfulData getData(long id) {
		final HibernateTemplate	tmpl=getHibernateTemplate();
		return tmpl.get(RestfulData.class, Long.valueOf(id));
	}
	/*
	 * @see com.springsource.insight.samples.rest.RestfulRepository#create(int)
	 */
	@Override
	public RestfulData create(int balance) {
		final RestfulData	data=new RestfulData();
		data.setLastModified(new Date(System.currentTimeMillis()));
		data.setBalance(balance);

		final HibernateTemplate	tmpl=getHibernateTemplate();
		final Serializable		saveId=tmpl.save(data);
		if (null == saveId)
			throw new IllegalStateException("No ID generated for entity=" + data);
		tmpl.flush();

		logger.info("create(" + data + ")[ID=" + saveId + "]");
		return data;
	}
	/*
	 * @see com.springsource.insight.samples.rest.RestfulRepository#setBalance(long, int)
	 */
	@Override
	public RestfulData setBalance (final long id, final int balance) {
		final RestfulData	value=getData(id);
		if (value == null) {
			return null;
		}

		final int	curBalance=value.getBalance();
		if (curBalance != balance) {
			value.setBalance(balance);
			value.setLastModified(new Date(System.currentTimeMillis()));
			
			final HibernateTemplate	tmpl=getHibernateTemplate();
			tmpl.update(value);
			tmpl.flush();

			logger.info("setBalance(" + value + ")[ID=" + id + "] - old=" + curBalance);
		}

		return value;
	}
	/*
	 * @see com.springsource.insight.samples.rest.RestfulRepository#removeData(long)
	 */
	@Override
	public RestfulData removeData(long id) {
		final RestfulData	value=getData(id);
		if (value == null) {
			return null;
		}

		final HibernateTemplate	tmpl=getHibernateTemplate();
		tmpl.delete(value);

		logger.info("removeData(" + value + ")[ID=" + id + "]");
		return value;
	}
	/*
	 * @see com.springsource.insight.samples.rest.RestfulRepository#removeAll()
	 */
	@Override
	public List<RestfulData> removeAll() {
		final List<RestfulData>	items=findAll();
		if (CollectionUtils.isEmpty(items)) {
			return items;
		}

		final HibernateTemplate	tmpl=getHibernateTemplate();
		tmpl.deleteAll(items);

		logger.info("removeAll() - clean up " + items.size() + " entities");
		if (logger.isDebugEnabled()) {
			for (final RestfulData value : items) {
				logger.debug("removeAll(" + value + ")[ID=" + value.getId() + "] deleted");
			}
		}

		return items;
	}
}

package com.vmware.spring.workshop.dao.impl.jpa;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.vmware.spring.workshop.dao.api.UserDao;
import com.vmware.spring.workshop.model.user.User;

/**
 * @author lgoldstein
 */
@Repository("userDao")
@Transactional
public class UserDaoImpl
		extends AbstractIdentifiedJpaDaoImpl<User>
		implements UserDao {
	public UserDaoImpl () {
		super(User.class);
	}

	@Override
	@Transactional(readOnly=true)
	public User findByLoginName (String username)
	{
		Assert.hasText(username, "No username provided");
		final TypedQuery<User>	query=getNamedQuery("findByLoginName")
										 .setParameter("loginName", username)
										 ;
		return getUniqueResult(query);
	}

	@Override
	@Transactional(readOnly=true)
	public List<User> findUserByLocation(String location) {
		Assert.hasText(location, "No location provided");
		final TypedQuery<User>	query=getNamedQuery("findUserByLocation")
										 .setParameter("location", "%" + location.toLowerCase() + "%")
										 ;
		return getQueryResults(query);
	}
}

package com.vmware.spring.workshop.dao.api;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.vmware.spring.workshop.dao.IdentifiedCommonOperationsDao;
import com.vmware.spring.workshop.model.user.User;

/**
 * @author lgoldstein
 */
public interface UserDao extends IdentifiedCommonOperationsDao<User> {
	User findByLoginName (@Param("loginName") String username);
	/**
	 * @param location A sub-string of the location
	 * @return A {@link List} of all matching {@link User}-s whose location
	 * contains the specified parameter (case <U>insensitive</U>)
	 */
	List<User> findUserByLocation (@Param("location") String location);
}

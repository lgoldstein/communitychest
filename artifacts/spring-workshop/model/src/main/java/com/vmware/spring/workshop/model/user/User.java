package com.vmware.spring.workshop.model.user;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import com.vmware.spring.workshop.model.AbstractNamedIdentified;
import com.vmware.spring.workshop.model.GeoPosition;
import com.vmware.spring.workshop.model.Located;

/**
 * @author lgoldstein
 */
@Entity(name="User")
@NamedQueries({
	@NamedQuery(name="User.findUserByName",query="SELECT u FROM User u WHERE u.name = :name"),
	@NamedQuery(name="User.findUserByLocation",query="SELECT u FROM User u WHERE LOWER(u.homeAddress) LIKE LOWER(:location)"),
	@NamedQuery(name="User.findByLoginName",query="SELECT u FROM User u WHERE u.loginName = :loginName"),
	@NamedQuery(name="User.findUsersByRole",query="SELECT u FROM User u WHERE u.role = :role")
})
public class User extends AbstractNamedIdentified implements Located {
	private static final long serialVersionUID = -5781082075779489505L;
	private String	_loginName, _password, _homeAddress;
	private UserRoleType	_role;
	private GeoPosition	_position;

	public User() {
		super();
	}

	public static final int	MAX_LOGIN_NAME_LENGTH=64;
	@Column(name="loginName", unique=true, nullable=false, length=MAX_LOGIN_NAME_LENGTH)
	public String getLoginName() {
		return _loginName;
	}

	public void setLoginName(String loginName) {
		_loginName = loginName;
	}

	public static final int	MAX_PASSWORD_LENGTH=32;
	@Column(name="password", unique=false, nullable=false, length=MAX_PASSWORD_LENGTH)
	public String getPassword() {
		return _password;
	}

	public void setPassword(String password) {
		_password = password;
	}

	@Column(name="role", unique=false, nullable=false, length=16)
	@Enumerated(EnumType.STRING)
	public UserRoleType getRole() {
		return _role;
	}

	public void setRole(UserRoleType role) {
		_role = role;
	}

	public static final int	MAX_HOME_ADDRESS_LENGTH=MAX_LOCATION_LENGTH;
	@Column(name="homeAddress",unique=false,nullable=false,length=MAX_HOME_ADDRESS_LENGTH)
	public String getHomeAddress() {
		return _homeAddress;
	}

	public void setHomeAddress(String homeAddress) {
		_homeAddress = homeAddress;
	}

	@Override
	@Transient
	public String getLocation() {
		return getHomeAddress();
	}

	@Override
	public void setLocation(String location) {
		setHomeAddress(location);
	}

	@Override
	@Embedded
	public GeoPosition getPosition() {
		return _position;
	}

	@Override
	public void setPosition(GeoPosition position) {
		_position = position;
	}
}

/*
 * 
 */
package net.community.chest.db;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Map;

import net.community.chest.CoVariantReturn;
import net.community.chest.Triplet;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 14, 2009 12:36:29 PM
 */
public class DBAccessConfig implements Serializable, PubliclyCloneable<DBAccessConfig> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6270747423992415712L;
	public DBAccessConfig ()
	{
		super();
	}

	private String _driverClass;
	public String getDriverClass ()
	{
		return _driverClass;
	}

	public void setDriverClass (String driverClass)
	{
		_driverClass = driverClass;
	}

	private String	_accessURL;
	public String getURL ()
	{
		return _accessURL;
	}

	public void setURL (String accessURL)
	{
		_accessURL = accessURL;
	}

	private String	_username;
	public String getUser ()
	{
		return _username;
	}

	public void setUser (String user)
	{
		_username = user;
	}

	private String	_password;
	public String getPassword ()
	{
		return _password;
	}

	public void setPassword (String p)
	{
		_password = p;
	}

	public DBAccessConfig (String drvClass, String url, String user, String pass)
	{
		_driverClass = drvClass;
		_accessURL = url;
		_username = user;
		_password = pass;
	}

	public DBAccessConfig (DBAccessConfig cfg)
	{
		this((null == cfg) ? null : cfg.getDriverClass(),
			 (null == cfg) ? null : cfg.getURL(),
			 (null == cfg) ? null : cfg.getUser(),
			 (null == cfg) ? null : cfg.getPassword());
	}

	// returns 'this'
	public DBAccessConfig update (DBAccessConfig cfg)
	{
		setDriverClass((null == cfg) ? null : cfg.getDriverClass());
		setURL((null == cfg) ? null : cfg.getURL());
		setUser((null == cfg) ? null : cfg.getUser());
		setPassword((null == cfg) ? null : cfg.getPassword());

		return this;
	}
	// returns zero if all fields initialized
	public static final int checkDBAccessConfig (final DBAccessConfig cfg)
	{
		if (null == cfg)
			return (-1);

		final String[]	vals={
				cfg.getDriverClass(), cfg.getURL(), cfg.getUser(), cfg.getPassword()
			};
		for (int	vIndex=0; vIndex < vals.length; vIndex++)
		{
			final String	vv=vals[vIndex];
			if ((null == vv) || (vv.length() <= 0))
				return (vIndex + 1);
		}

		return 0;
	}

	public int checkDBAccessConfig ()
	{
		return checkDBAccessConfig(this);
	}
	/**
	 * Registers an instance of the specified {@link Driver} if one not
	 * already registered
	 * @param cfg The {@link DBAccessConfig} to use - may not be <code>null</code>
	 * @return Registration result as a {@link java.util.Map.Entry} whose key=the
	 * {@link Driver} instance and the value=a {@link Boolean} indicating
	 * if a driver instance was created (TRUE) or already registered (FALSE) 
	 * @throws Exception If failed to instantiate the driver instance
	 */
	public static final Map.Entry<Driver,Boolean> registerDriver (
			final DBAccessConfig cfg)
		throws Exception
	{
		return DriverUtils.registerDriver(cfg.getDriverClass());
	}

	public Map.Entry<Driver,Boolean> registerDriver () throws Exception
	{
		return registerDriver(this);
	}
	/**
	 * Creates a connection using the provided {@link DBAccessConfig} data
	 * @param cfg The {@link DBAccessConfig} instance - may not be <code>null</code>
	 * @return A {@link Triplet} containing the {@link Driver} instance, a
	 * {@link Boolean} indicating if the driver was instantiated or pre-registered
	 * and the {@link Connection} value
	 * @throws Exception If unable to create the connection
	 * @see #registerDriver(DBAccessConfig)
	 */
	public static final Triplet<Driver,Boolean,Connection> createConnection (
			final DBAccessConfig cfg)
		throws Exception
	{
		final Map.Entry<Driver,Boolean>	drvRes=registerDriver(cfg);
		final Connection				conn=
			DriverManager.getConnection(cfg.getURL(), cfg.getUser(), cfg.getPassword());
		return new Triplet<Driver,Boolean,Connection>(drvRes.getKey(), drvRes.getValue(), conn);
	}

	public Triplet<Driver,Boolean,Connection> createConnection () throws Exception
	{
		return createConnection(this);
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public DBAccessConfig clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof DBAccessConfig))
			return false;
		if (this == obj)
			return true;

		final DBAccessConfig	cfg=(DBAccessConfig) obj;
		return (0 == StringUtil.compareDataStrings(getDriverClass(), cfg.getDriverClass(), true))
		    && (0 == StringUtil.compareDataStrings(getURL(), cfg.getURL(), true))
			&& (0 == StringUtil.compareDataStrings(getUser(), cfg.getUser(), true))
			&& (0 == StringUtil.compareDataStrings(getPassword(), cfg.getPassword(), true))
			;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return StringUtil.getDataStringHashCode(getDriverClass(), true)
			 + StringUtil.getDataStringHashCode(getURL(), true)
			 + StringUtil.getDataStringHashCode(getUser(), true)
			 + StringUtil.getDataStringHashCode(getPassword(), true)
			 ;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return getDriverClass() + "//" + getURL() + "[" + getUser() + ":" + getPassword() + "]";
	}

	private static Map<String,AttributeAccessor>	_accsMap;
	public static final synchronized Map<String,? extends AttributeAccessor> getAccessorsMap ()
	{
		if (null == _accsMap)
			_accsMap = AttributeMethodType.getAllAccessibleAttributes(DBAccessConfig.class);
		return _accsMap;
	}
}

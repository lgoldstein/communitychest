/*
 * 
 */
package net.community.chest.util;

import java.util.Properties;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 24, 2010 9:18:29 AM
 */
public class PropertiesParametersHolder extends AbstractParametersHolder {
	private Properties	_props;
	public Properties getProperties ()
	{
		return _props;
	}

	public void setProperties (Properties p)
	{
		_props = p;
	}

	public PropertiesParametersHolder (Properties props)
	{
		_props = props;
	}
	
	public PropertiesParametersHolder ()
	{
		this(null);
	}
	/*
	 * @see net.community.chest.util.MappedParametersHolder#getParameter(java.lang.String, java.lang.String)
	 */
	@Override
	public String getParameter (String paramName, String defValue)
	{
		final Properties	p=getProperties();
		if ((p == null) || p.isEmpty() || (paramName == null) || (paramName.length() <= 0))
			return defValue;

		return p.getProperty(paramName, defValue);
	}
}

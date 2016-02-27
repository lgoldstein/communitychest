/*
 * 
 */
package net.community.chest.util;

import net.community.chest.convert.ValueStringInstantiator;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>Provides some basic/default implementation of {@link ParametersHolder}</P>
 * @author Lyor G.
 * @since Nov 24, 2010 8:52:13 AM
 */
public abstract class AbstractParametersHolder implements ParametersHolder {
	protected AbstractParametersHolder ()
	{
		super();
	}
	/*
	 * @see net.community.chest.util.ParametersHolder#getParameter(java.lang.String)
	 */
	@Override
	public String getParameter (final String paramName)
	{
		return getParameter(paramName, null);
	}
	/*
	 * @see net.community.chest.util.ParametersHolder#getFlagParameter(java.lang.String)
	 */
	@Override
	public Boolean getFlagParameter (final String paramName)
	{
    	final String	paramValue=getParameter(paramName);
    	if ((null == paramValue) || (paramValue.length() <= 0))
    		return null;

    	for (final String	tv : TRUE_PARAM_VALUES)
    	{
    		if (paramValue.equalsIgnoreCase(tv))
    			return Boolean.TRUE;
    	}

    	return Boolean.FALSE;
	}
	/*
	 * @see net.community.chest.util.ParametersHolder#getFlagParameter(java.lang.String, boolean)
	 */
	@Override
	public boolean getFlagParameter (final String paramName, final boolean defValue)
	{
    	final Boolean	pv=getFlagParameter(paramName);
    	if (null == pv)
    		return defValue;
   
    	return pv.booleanValue();
	}
	/*
	 * @see net.community.chest.util.ParametersHolder#getIntParameter(java.lang.String)
	 */
	@Override
	public Integer getIntParameter (final String paramName) throws NumberFormatException
	{
    	final String	paramValue=getParameter(paramName);
    	if ((null == paramValue) || (paramValue.length() <= 0))
    		return null;

    	return Integer.valueOf(paramValue);
	}
	/*
	 * @see net.community.chest.util.ParametersHolder#getIntParameter(java.lang.String, int)
	 */
	@Override
	public int getIntParameter (final String paramName, final int defValue) throws NumberFormatException
	{
    	final Integer	pv=getIntParameter(paramName);
    	if (null == pv)
    		return defValue;
   
    	return pv.intValue();
	}
	/*
	 * @see net.community.chest.util.ParametersHolder#getLongParameter(java.lang.String)
	 */
	@Override
	public Long getLongParameter (final String paramName) throws NumberFormatException
	{
    	final String	paramValue=getParameter(paramName);
    	if ((null == paramValue) || (paramValue.length() <= 0))
    		return null;

    	return Long.valueOf(paramValue);
	}
	/*
	 * @see net.community.chest.util.ParametersHolder#getLongParameter(java.lang.String, long)
	 */
	@Override
	public long getLongParameter (final String paramName, final long defValue) throws NumberFormatException
	{
    	final Long	pv=getLongParameter(paramName);
    	if (null == pv)
    		return defValue;
   
    	return pv.longValue();
	}
	/*
	 * @see net.community.chest.util.ParametersHolder#getParameterValue(java.lang.String, net.community.chest.convert.ValueStringInstantiator)
	 */
	@Override
	public <V> V getParameterValue (final String paramName, final ValueStringInstantiator<? extends V> vsi)
		throws Exception
	{
    	final String	paramValue=getParameter(paramName);
    	if ((null == paramValue) || (paramValue.length() <= 0))
    		return null;

    	return vsi.newInstance(paramValue);
	}
	/*
	 * @see net.community.chest.util.ParametersHolder#getParameterValue(java.lang.String, net.community.chest.convert.ValueStringInstantiator, java.lang.Object)
	 */
	@Override
	public <V> V getParameterValue (final String paramName, final ValueStringInstantiator<? extends V> vsi, final V defValue)
		throws Exception
	{
		final V	value=getParameterValue(paramName, vsi);
		if (value == null)
			return defValue;

		return value;
	}
}

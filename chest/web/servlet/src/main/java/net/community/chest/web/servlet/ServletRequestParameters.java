/*
 * 
 */
package net.community.chest.web.servlet;

import javax.servlet.http.HttpServletRequest;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.AbstractParametersHolder;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>Provides useful decoding methods to {@link HttpServletRequest} parameters</P>
 * @author Lyor G.
 * @since Nov 23, 2010 11:18:05 AM
 */
public class ServletRequestParameters extends AbstractParametersHolder {
	private HttpServletRequest	_request;
	public HttpServletRequest getRequest ()
	{
		return _request;
	}

	public void setRequest (HttpServletRequest request)
	{
		_request = request;
	}
	
	public ServletRequestParameters (HttpServletRequest request)
	{
		_request = request;
	}
	
	public ServletRequestParameters ()
	{
		this(null);
	}
	/*
	 * @see net.community.chest.util.ParametersHolder#getParameter(java.lang.String, java.lang.String)
	 */
	@Override
	public String getParameter (final String paramName, final String defValue)
	{
		final HttpServletRequest	req=getRequest();
    	final String				paramValue=
    		((null == req) || (null == paramName) || (paramName.length() <= 0)) ? null : req.getParameter(paramName);
    	if ((null == paramValue) || (paramValue.length() <= 0))
    		return defValue;

    	return StringUtil.stripDelims(paramValue);
	}
}

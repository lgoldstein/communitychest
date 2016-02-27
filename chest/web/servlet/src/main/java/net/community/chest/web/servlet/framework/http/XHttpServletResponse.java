/*
 * 
 */
package net.community.chest.web.servlet.framework.http;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * Adds some set/get-ter(s) that are not available in the original interface
 * @author Lyor G.
 * @since Jun 15, 2010 11:59:56 AM
 */
public interface XHttpServletResponse extends HttpServletResponse {
	Map<String,String> getHeadersMap ();
	String getHeader (String name);
	Collection<Cookie> getCookiesList ();
	// returns current value (if any)
	String removeHeader (String name);
	int getStatusCode ();
	String getStatusMessage ();
}

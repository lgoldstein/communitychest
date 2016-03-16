/*
 *
 */
package net.community.chest.web.servlet.framework.http;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * Adds some set-ter(s) that are not available in the original interface
 * @author Lyor G.
 * @since Jun 15, 2010 10:31:48 AM
 */
public interface XHttpServletRequest extends HttpServletRequest {
    Map<String,String> getHeadersMap ();
    // returns previous value (if any)
    String setHeader (String name, String value);
    // returns previous value (if any)
    String removeHeader (String name);
    void setAuthType (String value);
    void setContextPath (String value);
    Collection<Cookie> getCookiesList ();
    Collection<Cookie> addCookie (Cookie c);
    Collection<Cookie> removeCookie (Cookie c);
    void setMethod (String value);
    void setPathInfo (String value);
    void setQueryString (String value);
    void setRemoteUser (String value);
    void setRequestedSessionId (String value);
    void setRequestURI (String value);
    void setServletPath (String value);
    void setSession (HttpSession value);
    void setUserPrincipal (Principal value);
}

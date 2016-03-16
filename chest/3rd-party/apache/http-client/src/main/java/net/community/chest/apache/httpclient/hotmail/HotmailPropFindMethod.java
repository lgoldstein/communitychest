package net.community.chest.apache.httpclient.hotmail;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderGroup;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.cookie.MalformedCookieException;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.webdav.lib.methods.PropFindMethod;

/**
 * Override class for the PROPFIND method for Hotmail special behavior
 */
public class HotmailPropFindMethod extends PropFindMethod {
    public HotmailPropFindMethod ()
    {
        super();
    }
    /* Override to handle cookies for domains other than current one
     * @see org.apache.commons.httpclient.HttpMethodBase#processResponseHeaders(org.apache.commons.httpclient.HttpState, org.apache.commons.httpclient.HttpConnection)
     */
    @Override
    protected void processResponseHeaders (HttpState state, HttpConnection conn)
    {
        final HeaderGroup    hg=getResponseHeaderGroup();
        Header[]             headers=hg.getHeaders("set-cookie2");
        // Process old style set-cookie headers if new style headers are not present
        if ((null == headers) || (headers.length <= 0))
            headers = hg.getHeaders("set-cookie");

        if ((headers != null) && (headers.length > 0))
        {
            final HttpMethodParams    mp=getParams();
            final CookieSpec        parser=CookiePolicy.getCookieSpec(mp.getCookiePolicy());
            final String            host=conn.getHost(), path=getPath();
            final int                port=conn.getPort();
            final boolean            secure=conn.isSecure();
            for (final Header header : headers)
            {
                /*        NOTE !!! remove the header from the group since we
                 *  handle it here, and then we call the base class
                 */
                hg.removeHeader(header);

                final Cookie[] cookies;
                try
                {
                    if ((null == (cookies=parser.parse(host, port, path, secure, header))) || (cookies.length <= 0))
                        continue;
                }
                catch(MalformedCookieException e)
                {
                    continue; // ignore
                }

                for (final Cookie cookie : cookies)
                {
                    final String    cPath=(null == cookie) ? null : cookie.getPath();
                    if (null == cPath)
                        continue;

                    try
                    {
                        /*         NOTE !!! overridden this check in the
                         * default "validate" since Hotmail sends
                         * such cookies. In this case, we take a chance
                         * and accumulate the cookie anyway
                         */
                        if (path.startsWith(cPath))
                            parser.validate(host, port, path, secure, cookie);

                        state.addCookie(cookie);
                    }
                    catch(MalformedCookieException e)
                    {
                        // ignore
                    }
                }
            }
        }

        /* NOTE: we call the base class even though it is a waste of
         * time since the above code is what the base class does (except
         * for the special adjustments). However, better safe than sorry,
         * just in case future implementations do something else as well.
         */
        super.processResponseHeaders(state, conn);
    }
}

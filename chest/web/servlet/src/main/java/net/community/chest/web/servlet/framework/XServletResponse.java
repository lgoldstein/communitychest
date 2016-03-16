/*
 *
 */
package net.community.chest.web.servlet.framework;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * Adds some set/get-ter(s) that are not available in the original interface
 * @author Lyor G.
 * @since Jun 15, 2010 11:46:43 AM
 */
public interface XServletResponse extends ServletResponse {
    int getContentLength ();
    void setOutputStream (ServletOutputStream value) throws IOException;
    void setCommitted (boolean value);
}

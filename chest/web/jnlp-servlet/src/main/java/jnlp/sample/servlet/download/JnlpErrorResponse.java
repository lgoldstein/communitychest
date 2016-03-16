/*
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */
package jnlp.sample.servlet.download;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletResponse;

import jnlp.sample.servlet.JnlpDownloadServlet;

public class JnlpErrorResponse extends DownloadResponse {
    private String _message;
    public String getMessage ()
    {
        return _message;
    }

    public void setMessage (String m)
    {
        _message = m;
    }

    public String getMessage (int jnlpErrorCode)
    {
        final String msg=Integer.toString(jnlpErrorCode);
        String        dsc="No description";
        try
        {
            final ResourceBundle    rb=JnlpDownloadServlet.getDefaultResourceBundle();
            final String            errKey="servlet.jnlp.err." + msg;
            if ((rb != null) && rb.containsKey(errKey))
                dsc = rb.getString(errKey);
        }
        catch (MissingResourceException mre)
        {
            /* ignore - can happen if some unknown/not-covered error code */
        }

        return msg + " " + dsc;
    }

    public String setMessage (int jnlpErrorCode)
    {
        final String    m=getMessage(jnlpErrorCode);
        setMessage(m);
        return m;
    }

    public JnlpErrorResponse (String m)
    {
        _message = m;
    }

    public JnlpErrorResponse (int jnlpErrorCode)
    {
        setMessage(jnlpErrorCode);
    }

    public JnlpErrorResponse ()
    {
        this(null);
    }
    /*
     * @see jnlp.sample.servlet.DownloadResponse#sendRespond(javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void sendRespond (HttpServletResponse response) throws IOException
    {
        response.setContentType(JNLP_ERROR_MIMETYPE);
        PrintWriter pw=response.getWriter();
        pw.println(getMessage());
    }
    /*
     * @see jnlp.sample.servlet.DownloadResponse#toString()
     */
    @Override
    public String toString () { return super.toString() + "[" + getMessage() + "]"; }
    /*
     * @see jnlp.sample.servlet.download.DownloadResponse#clone()
     */
    @Override
    public JnlpErrorResponse /* co-variant return */ clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
}

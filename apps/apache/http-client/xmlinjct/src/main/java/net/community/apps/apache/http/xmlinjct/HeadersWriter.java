/*
 *
 */
package net.community.apps.apache.http.xmlinjct;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import net.community.chest.dom.DOMUtils;
import net.community.chest.io.EOLStyle;

import org.apache.commons.httpclient.Header;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>&quot;Piggyback&quot; on 1st write in order to write the HTTP {@link Header}-s
 * @author Lyor G.
 * @since Aug 24, 2008 12:22:42 PM
 */
public class HeadersWriter extends FileWriter {
    private Collection<Header>    _hdrs    /* =null */;
    public Collection<Header> getHeaders ()
    {
        return _hdrs;
    }

    private boolean haveHdrs ()
    {
        final Collection<Header>    hl=getHeaders();
        return ((hl != null) && (hl.size() > 0));
    }

    public HeadersWriter (String filePath, Collection<Header> hl) throws IOException
    {
        super(filePath);
        _hdrs = hl;
    }

    public HeadersWriter (File file, Collection<Header> hl) throws IOException
    {
        super(file);
        _hdrs = hl;
    }

    private void writeHeaders (final Collection<Header> hl, final boolean superCall) throws IOException
    {
        if ((null == hl) || (hl.size() <= 0))
            return;

        final String    eol=EOLStyle.LOCAL.getStyleString(),
                        tagName=Header.class.getSimpleName();
        if (superCall)
            super.write(eol, 0, eol.length());
        else
            write(eol, 0, eol.length());

        if (superCall)
            super.write(DOMUtils.XML_COMMENT_START, 0, DOMUtils.XML_COMMENT_START.length());
        else
            write(DOMUtils.XML_COMMENT_START, 0, DOMUtils.XML_COMMENT_START.length());

        StringBuilder    sb=null;
        for (final Header h : hl)
        {
            final String    n=(null == h) ? null : h.getName(),
                            v=(null == h) ? null : h.getValue();
            final int        nLen=(null == n) ? 0 : n.length(),
                            vLen=(null == v) ? 0 : v.length();
            if ((nLen <= 0) || (vLen <= 0))
                continue;

            if (null == sb)
                sb = new StringBuilder(tagName.length() + nLen + vLen + 8 + eol.length());
            else
                sb.setLength(0);

            sb.append(eol)
              .append('\t')    // separate from previous
              .append(DOMUtils.XML_ELEM_START_DELIM)
              .append(tagName)
              .append(" name=\"")
              .append(n)
              .append("\" value=\"")
              .append(v)
              .append('"')
              .append(DOMUtils.XML_ELEM_CLOSURE_DELIM)
              .append(DOMUtils.XML_ELEM_END_DELIM)
              ;

            if (superCall)
                super.write(sb.toString(), 0, sb.length());
            else
                write(sb.toString(), 0, sb.length());
        }

        if (superCall)
            super.write(eol, 0, eol.length());
        else
            write(eol, 0, eol.length());

        if (superCall)
            super.write(DOMUtils.XML_COMMENT_END, 0, DOMUtils.XML_COMMENT_END.length());
        else
            write(DOMUtils.XML_COMMENT_END, 0, DOMUtils.XML_COMMENT_END.length());

        if (superCall)
            super.write(eol, 0, eol.length());
        else
            write(eol, 0, eol.length());

        _writtenHdrs = true;
    }

    private boolean    _writtenHdrs    /* =false */;
    private StringBuilder writeHeaders (final StringBuilder sb) throws IOException
    {
        final int    sbLen=(null == sb) ? 0 : sb.length();
        if (sbLen <= 3)    // expect at least <.../>
            return sb;

        // find end of 1st element or processing instruction and add the headers as comment(s) after it
        for (int    sbPos=0; sbPos < sbLen; sbPos++)
        {
            final char    ch=sb.charAt(sbPos);
            if (ch != DOMUtils.XML_ELEM_END_DELIM)
                continue;

            // write the initial element
            final CharSequence    prefix=sb.subSequence(0, sbPos + 1);
            super.write(prefix.toString(), 0, prefix.length());

            writeHeaders(getHeaders(), true);

            // write whatever is left after the 1st element
            if ((sbPos+2) < sbLen)
            {
                final CharSequence    suffix=sb.subSequence(sbPos + 2, sbLen);
                super.write(suffix.toString(), 0, suffix.length());
            }

            sb.setLength(0);
        }

        return sb;
    }

    private StringBuilder    _sbAcc    /* =null */;
    protected StringBuilder getWorkBuffer (final int len)
    {
        if (null == _sbAcc)
            _sbAcc = new StringBuilder(Math.max(len,128));
        return _sbAcc;
    }
    /*
     * @see java.io.OutputStreamWriter#write(char[], int, int)
     */
    @Override
    public void write (char[] cbuf, int off, int len) throws IOException
    {
        if (len <= 0)
            return;

        if ((!_writtenHdrs) && haveHdrs())
        {
            // efficiency improvement for 1st write
            if ((null == _sbAcc) && (len >= 4))
            {
                final int    maxPos=off + len;
                for (int    curPos=off; curPos < maxPos; curPos++)
                {
                    final char    ch=cbuf[curPos];
                    if (ch != DOMUtils.XML_ELEM_END_DELIM)
                        continue;

                    final int    prfxLen=1 + (curPos - off);
                    super.write(cbuf, off, prfxLen);
                    writeHeaders(getHeaders(), true);

                    final int    remLen=len - prfxLen;
                    if (len > 0)
                        super.write(cbuf, curPos + 1, remLen);

                    return;
                }
            }

            final StringBuilder    sb=getWorkBuffer(len);
            sb.append(cbuf, off, len);
            writeHeaders(sb);
        }
        else
            super.write(cbuf, off, len);
    }
    /*
     * @see java.io.Writer#write(char[])
     */
    @Override
    public void write (char[] cbuf) throws IOException
    {
        write(cbuf, 0, cbuf.length);
    }
    /*
     * @see java.io.OutputStreamWriter#write(int)
     */
    @Override
    public void write (int c) throws IOException
    {
        if ((!_writtenHdrs) && haveHdrs())
            write(new char[] { (char) c });
        else
            super.write(c);
    }
    /*
     * @see java.io.OutputStreamWriter#write(java.lang.String, int, int)
     */
    @Override
    public void write (final String str, final int off, final int len) throws IOException
    {
        if ((!_writtenHdrs) && haveHdrs())
        {
            // efficiency improvement for 1st write
            if ((null == _sbAcc) && (len >= 4))
            {
                final int    maxPos=off + len;
                for (int    curPos=off; curPos < maxPos; curPos++)
                {
                    final char    ch=str.charAt(curPos);
                    if (ch != DOMUtils.XML_ELEM_END_DELIM)
                        continue;

                    final int    prfxLen=1 + (curPos - off);
                    super.write(str, off, prfxLen);
                    writeHeaders(getHeaders(), true);

                    final int    remLen=len - prfxLen;
                    if (len > 0)
                        super.write(str, curPos + 1, remLen);

                    return;
                }
            }

            final StringBuilder    sb=getWorkBuffer(len);
            sb.append(str, off, len);
            writeHeaders(sb);
        }
        else
            super.write(str, off, len);
    }
    /*
     * @see java.io.Writer#write(java.lang.String)
     */
    @Override
    public void write (String str) throws IOException
    {
        write(str, 0, str.length());
    }

    public void setHeaders (final Collection<Header> hdrs) throws IllegalStateException
    {
        if (_sbAcc != null)
            throw new IllegalStateException("Setting headers after 1st write N/A");
        _hdrs = hdrs;
    }
}

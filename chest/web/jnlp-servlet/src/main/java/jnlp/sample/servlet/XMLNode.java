/*
 * @(#)XMLNode.java    1.6 05/11/17
 *
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

package jnlp.sample.servlet;

import java.io.PrintWriter;
import java.io.StringWriter;

import jnlp.sample.util.ObjectUtil;

/** Class that contains information about an XML Node
 */
public class XMLNode {
    private boolean _isElement;     // Element/PCTEXT
    private String _name;
    private XMLAttribute _attr;
    private XMLNode _parent;  // Parent Node
    private XMLNode _nested;  // Nested XML tags
    private XMLNode _next;    // Following XML tag on the same level

    /* Creates a PCTEXT node */
    public XMLNode (String name)
    {
        this(name, null, null, null);
        _isElement = false;
    }

    /*  Creates a ELEMENT node */
    public XMLNode (String name, XMLAttribute attr)
    {
        this(name, attr, null, null);
    }

    /*  Creates a ELEMENT node */
    public XMLNode (String name, XMLAttribute attr, XMLNode nested, XMLNode next)
    {
        _isElement = true;
        _name = name;
        _attr = attr;
        _nested = nested;
        _next = next;
        _parent = null;
    }

    public String getName()  { return _name; }
    public XMLAttribute getAttributes() { return _attr; }
    public XMLNode getNested() { return _nested; }
    public XMLNode getNext() { return _next; }
    public boolean isElement() { return _isElement; }

    public void setParent(XMLNode parent) { _parent = parent; }
    public XMLNode getParent() { return _parent; }

    public void setNext(XMLNode next)     { _next = next; }
    public void setNested(XMLNode nested) { _nested = nested; }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object o)
    {
        if (!(o instanceof XMLNode))
            return false;
        if (this == o)
            return true;

        final XMLNode other=(XMLNode) o;
        return ObjectUtil.match(getName(), other.getName())
            && ObjectUtil.match(getAttributes(), other.getAttributes())
            && ObjectUtil.match(getNested(), other.getNested())
            && ObjectUtil.match(getNext(), other.getNext())
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return ObjectUtil.objectHashCode(getName())
             + ObjectUtil.objectHashCode(getAttributes())
             + ObjectUtil.objectHashCode(getNested())
             + ObjectUtil.objectHashCode(getNext())
             ;
    }

    public String getAttribute (final String name)
    {
        if ((null == name) || (name.length() <= 0))
            return "";

        for (XMLAttribute cur=getAttributes(); cur != null; cur = cur.getNext())
        {
            final String    cn=cur.getName();
            if (name.equalsIgnoreCase(cn))
                return cur.getValue();
        }
        return "";
    }

    public void printToStream (PrintWriter out)
    {
        printToStream(out, 0);
    }

    public void printToStream (PrintWriter out, int n)
    {
        if (!isElement())
        {
            out.print(getName());
        }
        else
        {
            final XMLAttribute    attr=getAttributes();
            final String        attrString = (attr == null) ? "" : (" " + attr.toString());
            final XMLNode        nested=getNested();
            if (nested == null)
            {
                lineln(out, n, "<" + getName() + attrString + "/>");
            }
            else
            {
                final String    nm=getName();
                lineln(out, n, "<" + nm + attrString + ">");
                nested.printToStream(out, n + 1);
                if (nested.isElement())
                    lineln(out, n, "</" + nm + ">");
                else
                    out.print("</" + nm + ">");
            }
        }

        final XMLNode    nxt=getNext();
        if (nxt != null)
            nxt.printToStream(out, n);
    }

    private static void lineln (PrintWriter out, int indent, String s)
    {
        out.println("");
        for(int i = 0; i < indent; i++)
        {
            out.print("  ");
        }
        out.print(s);
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        final StringWriter sw = new StringWriter(1000);
        final PrintWriter pw = new PrintWriter(sw);
        printToStream(pw);
        pw.close();
        return sw.toString();
    }
}



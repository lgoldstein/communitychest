/*
 * @(#)XMLAttribute.java	1.6 05/11/17
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

import jnlp.sample.util.ObjectUtil;

/** Class that contains information about a specific attribute
 */
public class XMLAttribute {
    private String _name;
    private String _value;
    private XMLAttribute _next;
    
    public XMLAttribute(String name, String value) {
	_name = name;
	_value = value;
	_next = null;
    }
    
    public XMLAttribute(String name, String value, XMLAttribute next) {
	_name = name;
	_value = value;
	_next = next;
    }
    
    public String getName()  { return _name; }
    public String getValue() { return _value; }
    public XMLAttribute getNext() { return _next; }
    public void setNext(XMLAttribute next) { _next = next; }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals (Object o)
    {
    	if (!(o instanceof XMLAttribute))
    		return false;
    	if (this == o)
    		return true;

    	final XMLAttribute other = (XMLAttribute)o;
    	return ObjectUtil.match(getName(), other.getName())
    		&& ObjectUtil.match(getValue(), other.getValue())
    		&& ObjectUtil.match(getNext(), other.getNext());
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString ()
    {
    	final XMLAttribute	n=getNext();
    	if (n != null)
    		return getName() + "=\"" + getValue() + "\" " + n.toString();
    	else
    		return getName() + "=\"" + getValue() + "\"";
    }
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return ObjectUtil.objectHashCode(getName())
			 + ObjectUtil.objectHashCode(getValue())
			 + ObjectUtil.objectHashCode(getNext());
	}
}




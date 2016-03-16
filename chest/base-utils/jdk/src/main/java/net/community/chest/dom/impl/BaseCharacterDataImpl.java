/*
 *
 */
package net.community.chest.dom.impl;

import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <N> Type of {@link CharacterData} being implemented
 * @author Lyor G.
 * @since Feb 12, 2009 11:29:01 AM
 */
public abstract class BaseCharacterDataImpl<N extends CharacterData> extends BaseNodeImpl<N> implements CharacterData {
    protected BaseCharacterDataImpl (Class<N> nodeClass, String baseURI, String name, String value)
    {
        super(nodeClass, baseURI, name, value);
    }

    protected BaseCharacterDataImpl (Class<N> nodeClass, String name, String value)
    {
        super(nodeClass, name, value);
    }

    protected BaseCharacterDataImpl (Class<N> nodeClass, String name)
    {
        super(nodeClass, name);
    }

    protected BaseCharacterDataImpl (Class<N> nodeClass)
    {
        super(nodeClass);
    }

    /*
     * @see org.w3c.dom.CharacterData#getData()
     */
    @Override
    public String getData () throws DOMException
    {
        return getNodeValue();
    }
    /*
     * @see org.w3c.dom.CharacterData#setData(java.lang.String)
     */
    @Override
    public void setData (String data) throws DOMException
    {
        setNodeValue(data);
    }
    /*
     * @see org.w3c.dom.CharacterData#appendData(java.lang.String)
     */
    @Override
    public void appendData (String arg) throws DOMException
    {
        if ((null == arg) || (arg.length() <= 0))
            return;

        final String    d=getData();
        if (d != null)
            setData(d + arg);
        else
            setData(arg);
    }
    /*
     * @see org.w3c.dom.CharacterData#deleteData(int, int)
     */
    @Override
    public void deleteData (int offset, int count) throws DOMException
    {
        if (count <= 0)
            return;

        final String    d=getData();
        final int        dLen=(null == d) ? 0 : d.length(),
                        maxPos=offset + count;
        if (offset <= 0)
        {
            if (count >= dLen)
            {
                setData(null);
                return;
            }

            final String    n=d.substring(maxPos);
            setData(n);
        }
        else
        {
            final String    p=d.substring(0, offset);
            if (maxPos < dLen)
            {
                final String    n=d.substring(maxPos);
                setData(p + n);
            }
            else
                setData(p);
        }
    }
    /*
     * @see org.w3c.dom.CharacterData#getLength()
     */
    @Override
    public int getLength ()
    {
        final String    d=getData();
        return (null == d) ? 0 : d.length();
    }
    /*
     * @see org.w3c.dom.CharacterData#insertData(int, java.lang.String)
     */
    @Override
    public void insertData (int offset, String arg) throws DOMException
    {
        if ((null == arg) || (arg.length() <= 0))
            return;

        final String    d=getData();
        final int        dLen=(null == d) ? 0 : d.length();
        if (dLen <= 0)
        {
            if (offset > 0)
                throw new DOMException(DOMException.INDEX_SIZE_ERR, "insertData(" + offset + ")[" + arg + "] cannot insert");

            setData(arg);
        }
        else
        {
            final String    p=(offset > 0) ? d.substring(0, offset) : "",
                            n=(offset < dLen) ? d.substring(offset) : "";
            setData(p + arg + n);
        }
    }
    /*
     * @see org.w3c.dom.CharacterData#replaceData(int, int, java.lang.String)
     */
    @Override
    public void replaceData (int offset, int count, String arg) throws DOMException
    {
        deleteData(offset, count);
        insertData(offset, arg);
    }
    /*
     * @see org.w3c.dom.CharacterData#substringData(int, int)
     */
    @Override
    public String substringData (int offset, int count) throws DOMException
    {
        if (count <= 0)
            return "";

        final String    d=getData();
        return d.substring(offset, offset + count);
    }
}

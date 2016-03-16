/*
 *
 */
package net.community.chest.win32.core.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 19, 2013 3:48:17 PM
 *
 */
public class ArrayInfo extends LogHelper
        implements ElementEncoder<ArrayInfo>,
                   PubliclyCloneable<ArrayInfo>,
                   Serializable,
                   ObjectIdCarrier {
    private static final long serialVersionUID = -2958403024525744601L;

    private long    _objectId=(-1L);
    private int    _length=(-1);

    public ArrayInfo()
    {
        super();
    }

    public ArrayInfo (InputStream in) throws IOException
    {
        Object    result=read(in);
        if (result != this)
            throw new StreamCorruptedException("Mismatched read data instance");
    }

    @Override
    public long getObjectId ()
    {
        return _objectId;
    }

    @Override
    public void setObjectId (long objectId)
    {
        _objectId = objectId;
    }

    public int getLength ()
    {
        return _length;
    }

    public void setLength (int length)
    {
        _length = length;
    }

    @Override
    public ArrayInfo clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    @Override
    public ArrayInfo read (InputStream in) throws IOException
    {
        setObjectId(DataFormatConverter.readUnsignedInt32(in));
        logInternal("objectID=" + getObjectId());

        setLength(DataFormatConverter.readSignedInt32(in));
        logInternal("length=" + getLength());
        return this;
    }

    @Override
    public void write (OutputStream out) throws IOException
    {
        DataFormatConverter.writeUnsignedInt32(out, getObjectId());
        DataFormatConverter.writeSignedInt32(out, getLength());
    }

    @Override
    public int hashCode ()
    {
        return (int) getObjectId() + getLength();
    }

    @Override
    public boolean equals (Object obj)
    {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;

        ArrayInfo    other=(ArrayInfo) obj;
        if ((getObjectId() == other.getObjectId())
         && (getLength() == other.getLength()))
            return true;
        else
            return false;
    }

    @Override
    public String toString ()
    {
        return "id=" + getObjectId() + ";length=" + getLength();
    }
}

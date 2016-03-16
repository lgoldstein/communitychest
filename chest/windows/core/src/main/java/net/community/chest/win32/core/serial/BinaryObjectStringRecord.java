/*
 *
 */
package net.community.chest.win32.core.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 7, 2013 4:29:10 PM
 *
 */
public class BinaryObjectStringRecord extends SerializationRecord
       implements PubliclyCloneable<BinaryObjectStringRecord>,
                    ElementEncoder<BinaryObjectStringRecord>,
                  ObjectIdCarrier {
    private static final long serialVersionUID = -934948845694662686L;

    private long    _objectId;
    private String    _value;

    public BinaryObjectStringRecord ()
    {
        super(RecordTypeEnumeration.BinaryObjectString);
    }

    public BinaryObjectStringRecord (InputStream in) throws IOException
    {
        super(RecordTypeEnumeration.BinaryObjectString);

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

    public String getValue ()
    {
        return _value;
    }

    public void setValue (String value)
    {
        _value = value;
    }

    @Override
    @CoVariantReturn
    public BinaryObjectStringRecord read (InputStream in) throws IOException
    {
        return getClass().cast(super.read(in));
    }

    @Override
    public void readRecordData (InputStream in) throws IOException
    {
        setObjectId(DataFormatConverter.readUnsignedInt32(in));
        setValue(SerializationFormatConverter.readLengthPrefixedString(in));
    }

    @Override
    public void writeRecordData (OutputStream out) throws IOException
    {
        DataFormatConverter.writeUnsignedInt32(out, getObjectId());
        SerializationFormatConverter.writeLengthPrefixedString(out, getValue());
    }

    @Override
    public BinaryObjectStringRecord clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    @Override
    public int hashCode ()
    {
        return super.hashCode()
            + (int) getObjectId()
            + StringUtil.getDataStringHashCode(getValue(), true)
            ;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (!super.equals(obj))
            return false;
        if (this == obj)
            return true;

        BinaryObjectStringRecord    other=(BinaryObjectStringRecord) obj;
        if ((getObjectId() == other.getObjectId())
         && (StringUtil.compareDataStrings(getValue(), other.getValue(), true) == 0))
            return true;
        else
            return false;
    }

    @Override
    public String toString ()
    {
        return super.toString()
           + ": " + getObjectId() + "@" + getValue()
           ;
    }
}

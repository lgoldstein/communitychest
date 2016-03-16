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
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 19, 2013 3:06:47 PM
 *
 */
public class ClassWithIdRecord extends SerializationRecord
            implements PubliclyCloneable<ClassWithIdRecord>,
                       ElementEncoder<ClassWithIdRecord>,
                       ObjectIdCarrier {
    private static final long serialVersionUID = 8579914692805559030L;

    private long    _objectId=(-1L), _metadataId=(-1L);

    public ClassWithIdRecord ()
    {
        super(RecordTypeEnumeration.ClassWithId);
    }

    public ClassWithIdRecord (InputStream in) throws IOException
    {
        super(RecordTypeEnumeration.ClassWithId);

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

    public long getMetadataId ()
    {
        return _metadataId;
    }

    public void setMetadataId (long metadataId)
    {
        _metadataId = metadataId;
    }

    @Override
    @CoVariantReturn
    public ClassWithIdRecord read (InputStream in) throws IOException
    {
        return getClass().cast(super.read(in));
    }

    @Override
    public void readRecordData (InputStream in) throws IOException
    {
        setObjectId(DataFormatConverter.readSignedInt32(in));
        logInternal("objectId=" + getObjectId());
        setMetadataId(DataFormatConverter.readSignedInt32(in));
        logInternal("metadataId=" + getMetadataId());
    }

    @Override
    public void writeRecordData (OutputStream out) throws IOException
    {
        DataFormatConverter.writeSignedInt32(out, (int) getObjectId());
        DataFormatConverter.writeSignedInt32(out, (int) getMetadataId());
    }

    @Override
    @CoVariantReturn
    public ClassWithIdRecord clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    @Override
    public int hashCode ()
    {
        return super.hashCode()
            + (int) getObjectId()
            + (int) getMetadataId()
            ;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (!super.equals(obj))
            return false;
        if (this == obj)
            return true;

        ClassWithIdRecord    other=(ClassWithIdRecord) obj;
        if ((getObjectId() == other.getObjectId())
         && (getMetadataId() == other.getMetadataId()))
            return true;
        else
            return false;
    }

    @Override
    public String toString ()
    {
        return super.toString()
                + ";objectId=" + getObjectId()
                + ";metadataId=" + getMetadataId()
                ;
    }
}

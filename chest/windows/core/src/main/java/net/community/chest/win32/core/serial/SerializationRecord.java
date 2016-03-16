/*
 *
 */
package net.community.chest.win32.core.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import net.community.chest.CoVariantReturn;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 6, 2013 2:36:41 PM
 *
 */
public abstract class SerializationRecord extends LogHelper implements Serializable, Cloneable {
    private static final long serialVersionUID = 4477616174203495545L;
    private final RecordTypeEnumeration    _recordType;

    protected SerializationRecord (RecordTypeEnumeration recordType)
    {
        if ((_recordType=recordType) == null)
            throw new IllegalStateException("No record type provided");
    }

    public final RecordTypeEnumeration getRecordType ()
    {
        return _recordType;
    }

    public void write (OutputStream out) throws IOException
    {
        RecordTypeEnumeration.write(getRecordType(), out);
        writeRecordData(out);
    }

    public SerializationRecord read(InputStream in) throws IOException
    {
        RecordTypeEnumeration    recordType=RecordTypeEnumeration.read(in);
        if (!AbstractComparator.compareObjects(getRecordType(), recordType))
            throw new StreamCorruptedException("Mismatched record type: " + recordType);
        readRecordData(in);
        return this;
    }

    // NOTE: does not read/write the record type
    public abstract void readRecordData(InputStream in) throws IOException;
    public abstract void writeRecordData(OutputStream out) throws IOException;

    @Override
    @CoVariantReturn
    public SerializationRecord clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    @Override
    public int hashCode ()
    {
        return ClassUtil.getObjectHashCode(getRecordType());
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

        SerializationRecord    other=(SerializationRecord) obj;
        if (AbstractComparator.compareObjects(getRecordType(), other.getRecordType()))
            return true;
        else
            return false;
    }

    @Override
    public String toString ()
    {
        return String.valueOf(getRecordType());
    }
}

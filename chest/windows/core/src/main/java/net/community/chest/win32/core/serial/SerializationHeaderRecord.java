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
 * @since Jan 6, 2013 2:10:30 PM
 *
 */
public class SerializationHeaderRecord extends SerializationRecord
        implements PubliclyCloneable<SerializationHeaderRecord>,
                    ElementEncoder<SerializationHeaderRecord> {
    private static final long serialVersionUID = 8593574097207847707L;

    public static final int    DEFAULT_MAJOR_VERSION=1, DEFAULT_MINOR_VERSION=0;
    private int _headerId, _rootId, _majorVersion, _minorVersion;

    public SerializationHeaderRecord ()
    {
        super(RecordTypeEnumeration.SerializedStreamHeader);
    }

    public SerializationHeaderRecord (InputStream in) throws IOException
    {
        super(RecordTypeEnumeration.SerializedStreamHeader);

        Object    result=read(in);
        if (result != this)
            throw new StreamCorruptedException("Mismatched read data instance");
    }

    public int getHeaderId ()
    {
        return _headerId;
    }

    public void setHeaderId (int headerId)
    {
        _headerId = headerId;
    }

    public int getRootId ()
    {
        return _rootId;
    }

    public void setRootId (int rootId)
    {
        _rootId = rootId;
    }

    public int getMajorVersion ()
    {
        return _majorVersion;
    }

    public void setMajorVersion (int majorVersion)
    {
        _majorVersion = majorVersion;
    }

    public int getMinorVersion ()
    {
        return _minorVersion;
    }

    public void setMinorVersion (int minorVersion)
    {
        _minorVersion = minorVersion;
    }

    @Override
    @CoVariantReturn
    public SerializationHeaderRecord read (InputStream in) throws IOException
    {
        return getClass().cast(super.read(in));
    }

    @Override
    public void readRecordData(InputStream in) throws IOException
    {
        setRootId(DataFormatConverter.readSignedInt32(in));
        setHeaderId(DataFormatConverter.readSignedInt32(in));
        setMajorVersion(DataFormatConverter.readSignedInt32(in));
        setMinorVersion(DataFormatConverter.readSignedInt32(in));
    }

    @Override
    public void writeRecordData(OutputStream out) throws IOException
    {
        DataFormatConverter.writeSignedInt32(out, getRootId());
        DataFormatConverter.writeSignedInt32(out, getHeaderId());
        DataFormatConverter.writeSignedInt32(out, getMajorVersion());
        DataFormatConverter.writeSignedInt32(out, getMinorVersion());
    }

    @Override
    public SerializationHeaderRecord clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    @Override
    public int hashCode ()
    {
        return super.hashCode()
            + getRootId()
            + getHeaderId()
            + getMajorVersion()
            + getMinorVersion()
            ;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (!super.equals(obj))
            return false;
        if (this == obj)
            return true;

        SerializationHeaderRecord    other=(SerializationHeaderRecord) obj;
        if ((getRootId() == other.getRootId())
         && (getHeaderId() == other.getHeaderId())
         && (getMajorVersion() == other.getMajorVersion())
         && (getMinorVersion() == other.getMinorVersion()))
            return true;
        else
            return false;
    }

    @Override
    public String toString ()
    {
        return super.toString()
            + ";rootId=" + getRootId()
            + ";headerId=" + getHeaderId()
            + ";version=" + getMajorVersion() + "." + getMinorVersion()
            ;
    }
}

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
 * @since Jan 6, 2013 3:12:48 PM
 *
 */
public class BinaryLibraryRecord extends SerializationRecord
        implements PubliclyCloneable<BinaryLibraryRecord>,
                   ElementEncoder<BinaryLibraryRecord> {
    private static final long serialVersionUID = 8781678972291131743L;

    private long _libraryId=(-1L);    // the value must be positive
    private String _libraryName;

    public BinaryLibraryRecord ()
    {
        super(RecordTypeEnumeration.BinaryLibrary);
    }

    public BinaryLibraryRecord (InputStream in) throws IOException
    {
        super(RecordTypeEnumeration.BinaryLibrary);

        Object    result=read(in);
        if (result != this)
            throw new StreamCorruptedException("Mismatched read data instance");
    }

    public long getLibraryId ()
    {
        return _libraryId;
    }

    public void setLibraryId (long libraryId)
    {
        _libraryId = libraryId;
    }

    public String getLibraryName ()
    {
        return _libraryName;
    }

    public void setLibraryName (String libraryName)
    {
        _libraryName = libraryName;
    }

    @Override
    @CoVariantReturn
    public BinaryLibraryRecord read (InputStream in) throws IOException
    {
        return getClass().cast(super.read(in));
    }

    @Override
    public void readRecordData (InputStream in) throws IOException
    {
        setLibraryId(DataFormatConverter.readUnsignedInt32(in));
        setLibraryName(SerializationFormatConverter.readLengthPrefixedString(in));
    }

    @Override
    public void writeRecordData (OutputStream out) throws IOException
    {
        DataFormatConverter.writeUnsignedInt32(out, getLibraryId());
        SerializationFormatConverter.writeLengthPrefixedString(out, getLibraryName());
    }

    @Override
    public BinaryLibraryRecord clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    @Override
    public int hashCode ()
    {
        return super.hashCode()
            + (int) getLibraryId()
            + StringUtil.getDataStringHashCode(getLibraryName(), true)
            ;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (!super.equals(obj))
            return false;
        if (this == obj)
            return true;

        BinaryLibraryRecord    other=(BinaryLibraryRecord) obj;
        if ((getLibraryId() == other.getLibraryId())
         && (StringUtil.compareDataStrings(getLibraryName(), other.getLibraryName(), true) == 0))
            return true;
        else
            return false;
    }

    @Override
    public String toString ()
    {
        return super.toString()
            + ";libId=" + getLibraryId()
            + ";name=" + getLibraryName()
            ;
    }
}

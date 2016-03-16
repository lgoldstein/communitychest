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
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 19, 2013 3:47:19 PM
 *
 */
public class ArraySingleStringRecord extends SerializationRecord
                implements PubliclyCloneable<ArraySingleStringRecord>,
                           ElementEncoder<ArraySingleStringRecord>,
                           ObjectIdCarrier {
    private static final long serialVersionUID = 7508762712467646193L;
    private ArrayInfo    _arrayInfo;

    public ArraySingleStringRecord ()
    {
        super(RecordTypeEnumeration.ArraySingleString);
    }

    public ArraySingleStringRecord (InputStream in) throws IOException
    {
        super(RecordTypeEnumeration.ArraySingleString);

        Object    result=read(in);
        if (result != this)
            throw new StreamCorruptedException("Mismatched read data instance");
    }

    public ArrayInfo getArrayInfo ()
    {
        return _arrayInfo;
    }

    public void setArrayInfo (ArrayInfo arrayInfo)
    {
        _arrayInfo = arrayInfo;
    }

    @Override
    public long getObjectId ()
    {
        ArrayInfo    info=getArrayInfo();
        if (info == null)
            throw new IllegalStateException("No array info available");
        return info.getObjectId();
    }

    @Override
    public void setObjectId (long objectId)
    {
        ArrayInfo    info=getArrayInfo();
        if (info == null)
            throw new IllegalStateException("No array info available");
        info.setObjectId(objectId);
    }

    @Override
    @CoVariantReturn
    public ArraySingleStringRecord read (InputStream in) throws IOException
    {
        return getClass().cast(super.read(in));
    }

    @Override
    public void readRecordData (InputStream in) throws IOException
    {
        setArrayInfo(new ArrayInfo(in));
        logInternal("arrayInfo=" + getArrayInfo());
    }

    @Override
    public void writeRecordData (OutputStream out) throws IOException
    {
        ArrayInfo    info=getArrayInfo();
        if (info == null)
            throw new StreamCorruptedException("No array info");
        info.write(out);
    }

    @Override
    @CoVariantReturn
    public ArraySingleStringRecord clone () throws CloneNotSupportedException
    {
        ArraySingleStringRecord    other=getClass().cast(super.clone());
        ArrayInfo                info=getArrayInfo();
        if (info != null)
            other.setArrayInfo(info.clone());
        return other;
    }

    @Override
    public int hashCode ()
    {
        return super.hashCode()
            + ClassUtil.getObjectHashCode(getArrayInfo())
            ;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (!super.equals(obj))
            return false;
        if (this == obj)
            return true;

        ArraySingleStringRecord    other=(ArraySingleStringRecord) obj;
        if (AbstractComparator.compareObjects(getArrayInfo(), other.getArrayInfo()))
            return true;
        else
            return false;
    }

    @Override
    public String toString ()
    {
        return super.toString()
                + ";info=" + getArrayInfo()
                ;
    }
}

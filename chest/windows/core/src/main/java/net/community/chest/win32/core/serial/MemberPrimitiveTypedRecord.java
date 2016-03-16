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
 * @since Feb 19, 2013 3:13:42 PM
 *
 */
public class MemberPrimitiveTypedRecord extends SerializationRecord
            implements PubliclyCloneable<MemberPrimitiveTypedRecord>,
                       ElementEncoder<MemberPrimitiveTypedRecord> {
    private static final long serialVersionUID = -5617681741365737337L;

    private PrimitiveTypeEnumeration    _primitiveType;
    private Object    _value;

    public MemberPrimitiveTypedRecord ()
    {
        super(RecordTypeEnumeration.MemberPrimitiveTyped);
    }

    public MemberPrimitiveTypedRecord (InputStream in) throws IOException
    {
        super(RecordTypeEnumeration.MemberPrimitiveTyped);

        Object    result=read(in);
        if (result != this)
            throw new StreamCorruptedException("Mismatched read data instance");
    }

    public PrimitiveTypeEnumeration getPrimitiveType ()
    {
        return _primitiveType;
    }

    public void setPrimitiveType (PrimitiveTypeEnumeration primitiveType)
    {
        _primitiveType = primitiveType;
    }

    public Object getValue ()
    {
        return _value;
    }

    public void setValue (Object value)
    {
        _value = value;
    }

    @Override
    @CoVariantReturn
    public MemberPrimitiveTypedRecord read (InputStream in) throws IOException
    {
        return getClass().cast(super.read(in));
    }

    @Override
    public void readRecordData (InputStream in) throws IOException
    {
        PrimitiveTypeEnumeration    dataType=PrimitiveTypeEnumeration.read(in);
        setPrimitiveType(dataType);
        logInternal("type=" + getPrimitiveType());
        setValue(dataType.readValue(in));
        logInternal("value=" + getValue());
    }

    @Override
    public void writeRecordData (OutputStream out) throws IOException
    {
        PrimitiveTypeEnumeration    dataType=getPrimitiveType();
        if (dataType == null)
            throw new StreamCorruptedException("No data type provided");
        dataType.write(out);
        dataType.writeValue(out, getValue());
    }

    @Override
    @CoVariantReturn
    public MemberPrimitiveTypedRecord clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    @Override
    public int hashCode ()
    {
        return super.hashCode()
            + ClassUtil.getObjectHashCode(getPrimitiveType())
            + ClassUtil.getObjectHashCode(getValue())
            ;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (!super.equals(obj))
            return false;
        if (this == obj)
            return true;

        MemberPrimitiveTypedRecord    other=(MemberPrimitiveTypedRecord) obj;
        if (AbstractComparator.compareObjects(getPrimitiveType(), other.getPrimitiveType())
         && AbstractComparator.compareObjects(getValue(), other.getValue()))
            return true;
        else
            return false;
    }

    @Override
    public String toString ()
    {
        return super.toString()
                + ";type=" + getPrimitiveType()
                + ";value=" + getValue()
                ;
    }
}

/*
 *
 */
package net.community.chest.win32.core.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.List;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.compare.AbstractComparator;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 5, 2013 11:09:33 AM
 */
public class BinaryArrayRecord extends SerializationRecord
            implements PubliclyCloneable<BinaryArrayRecord>,
                       ElementEncoder<BinaryArrayRecord>,
                       ObjectIdCarrier {
    private static final long serialVersionUID = -6018333186509967505L;

    private long    _objectId=(-1L);
    private int _rank=(-1);
    private BinaryArrayTypeEnumeration    _arrayType;
    private List<Integer> _lengths, _lowerBounds;
    private BinaryTypeEnumeration    _remoteType;
    private Object    _additionalInfo;

    public BinaryArrayRecord ()
    {
        super(RecordTypeEnumeration.BinaryArray);
    }

    public BinaryArrayRecord (InputStream in) throws IOException
    {
        super(RecordTypeEnumeration.BinaryArray);

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

    public BinaryArrayTypeEnumeration getArrayType ()
    {
        return _arrayType;
    }

    public void setArrayType (BinaryArrayTypeEnumeration arrayType)
    {
        _arrayType = arrayType;
    }

    public int getRank ()
    {
        return _rank;
    }

    public void setRank (int rank)
    {
        _rank = rank;
    }

    public List<Integer> getLengths ()
    {
        return _lengths;
    }

    public void setLengths (List<Integer> lengths)
    {
        _lengths = lengths;
    }

    public List<Integer> getLowerBounds ()
    {
        return _lowerBounds;
    }

    public void setLowerBounds (List<Integer> lowerBounds)
    {
        _lowerBounds = lowerBounds;
    }

    public BinaryTypeEnumeration getRemoteType ()
    {
        return _remoteType;
    }

    public void setRemoteType (BinaryTypeEnumeration remoteType)
    {
        _remoteType = remoteType;
    }

    public Object getAdditionalInfo ()
    {
        return _additionalInfo;
    }

    public void setAdditionalInfo (Object additionalInfo)
    {
        _additionalInfo = additionalInfo;
    }

    @Override
    @CoVariantReturn
    public BinaryArrayRecord read (InputStream in) throws IOException
    {
        return getClass().cast(super.read(in));
    }

    @Override
    public void readRecordData (InputStream in) throws IOException
    {
        setObjectId(DataFormatConverter.readSignedInt32(in));
        logInternal("objectId=" + getObjectId());

        final BinaryArrayTypeEnumeration    arrayType=BinaryArrayTypeEnumeration.read(in);
        setArrayType(arrayType);
        logInternal("arrayType=" + getArrayType());

        final int    rank=DataFormatConverter.readSignedInt32(in);
        setRank(rank);
        logInternal("rank=" + getRank());
        setLengths(DataFormatConverter.readSignedInt32List(rank, in));
        logInternal("lengths=" + getLengths());

        if (BinaryArrayTypeEnumeration.SingleOffset.equals(arrayType)
         || BinaryArrayTypeEnumeration.JaggedOffset.equals(arrayType)
         || BinaryArrayTypeEnumeration.RectangularOffset.equals(arrayType))
        {
            setLowerBounds(arrayType.readLowerBounds(rank, in));
            logInternal("lowerBounds=" + getLowerBounds());
        }

        final BinaryTypeEnumeration    remoteType=BinaryTypeEnumeration.read(in);
        setRemoteType(remoteType);
        logInternal("remoteType=" + getRemoteType());
        setAdditionalInfo(remoteType.readAdditionalInfo(in));
        logInternal("additionalInfo=" + getAdditionalInfo());
    }

    @Override
    public void writeRecordData (OutputStream out) throws IOException
    {
        DataFormatConverter.writeSignedInt32(out, (int) getObjectId());

        final BinaryArrayTypeEnumeration    arrayType=getArrayType();
        if (arrayType == null)
            throw new StreamCorruptedException("No array type set");
        arrayType.write(out);

        DataFormatConverter.writeSignedInt32(out, getRank());
        DataFormatConverter.writeSignedInt32List(out, getLengths());
        DataFormatConverter.writeSignedInt32List(out, getLowerBounds());

        final BinaryTypeEnumeration    remoteType=getRemoteType();
        if (remoteType == null)
            throw new StreamCorruptedException("No remote type set");
        remoteType.write(out);
        remoteType.writeAdditionalInfo(out, getAdditionalInfo());
    }

    @Override
    @CoVariantReturn
    public BinaryArrayRecord clone () throws CloneNotSupportedException
    {
        BinaryArrayRecord    other=getClass().cast(super.clone());
        // TODO    deep clone
        return other;
    }

    @Override
    public int hashCode ()
    {
        return super.hashCode()
            + (int) getObjectId()
            + ClassUtil.getObjectHashCode(getArrayType())
            + getRank()
            + CollectionsUtils.size(getLengths())
            + CollectionsUtils.size(getLowerBounds())
            + ClassUtil.getObjectHashCode(getRemoteType())
            + ClassUtil.getObjectHashCode(getAdditionalInfo())
            ;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (!super.equals(obj))
            return false;
        if (this == obj)
            return true;

        BinaryArrayRecord    other=(BinaryArrayRecord) obj;
        if ((getObjectId() == other.getObjectId())
         && AbstractComparator.compareObjects(getArrayType(), other.getArrayType())
         && (getRank() == other.getRank())
         && (CollectionsUtils.findFirstNonMatchingIndex(getLengths(), other.getLengths()) < 0)
         && (CollectionsUtils.findFirstNonMatchingIndex(getLowerBounds(), other.getLowerBounds()) < 0)
         && AbstractComparator.compareObjects(getRemoteType(), other.getRemoteType())
         && AbstractComparator.compareObjects(getAdditionalInfo(), other.getAdditionalInfo()))
            return true;
        else
            return false;
    }

    @Override
    public String toString ()
    {
        return super.toString()
           + "@" + getObjectId()
           + ";array-type=" + getArrayType()
           + ";remote-type=" + getRemoteType()
           + ";extra-info=" + getAdditionalInfo()
           + ";rank=" + getRank()
           + ";lengths=" + getLengths()
           + ";lower-bounds=" + getLowerBounds()
           ;
    }
}

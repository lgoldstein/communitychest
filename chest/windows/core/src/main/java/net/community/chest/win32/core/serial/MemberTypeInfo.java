/*
 *
 */
package net.community.chest.win32.core.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.util.ArraysUtils;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 7, 2013 3:16:44 PM
 *
 */
public class MemberTypeInfo extends LogHelper
        implements Serializable,
                   PubliclyCloneable<MemberTypeInfo>,
                   ElementEncoder<MemberTypeInfo> {
    private static final long serialVersionUID = -6970826405803910729L;

    private List<BinaryTypeEnumeration>    _memberTypes;
    private List<Object> _additionalInfos;

    public MemberTypeInfo (BinaryTypeEnumeration... /* order is important + may have duplicates */ memberTypes)
    {
        this((ArraysUtils.length(memberTypes) <= 0) ? null : Arrays.asList(memberTypes));
    }

    public MemberTypeInfo (List<BinaryTypeEnumeration> /* order is important + may have duplicates */ memberTypes)
    {
        _memberTypes = memberTypes;
    }

    public MemberTypeInfo (int numMembers, InputStream in) throws IOException
    {
        Object    result=read(numMembers, in);
        if (result != this)
            throw new StreamCorruptedException("Mismatched read data instance");
    }

    public List<BinaryTypeEnumeration> getMemberTypes ()
    {
        return _memberTypes;
    }

    public void setMemberTypes (List<BinaryTypeEnumeration> memberTypes)
    {
        _memberTypes = memberTypes;
    }

    public List<Object> getAdditionalInfos ()
    {
        return _additionalInfos;
    }

    public void setAdditionalInfos (List<Object> additionalInfos)
    {
        _additionalInfos = additionalInfos;
    }

    @Override
    public MemberTypeInfo read (InputStream in) throws IOException
    {
        return read(CollectionsUtils.size(getMemberTypes()), in);
    }

    public MemberTypeInfo read (int numMembers, InputStream in) throws IOException
    {
        final List<BinaryTypeEnumeration>    memberTypes=(numMembers > 0) ? new ArrayList<BinaryTypeEnumeration>(numMembers) : null;
        for (int index=0; index < numMembers; index++)
        {
            final BinaryTypeEnumeration    typeVal=BinaryTypeEnumeration.read(in);
            memberTypes.add(typeVal);
        }

        setMemberTypes(memberTypes);
        logInternal("memberTypes: " + getMemberTypes());

        if (CollectionsUtils.size(memberTypes) > 0)
        {
            final List<Object>    additionalInfos=new ArrayList<Object>(memberTypes.size());
            for (final BinaryTypeEnumeration typeValue : memberTypes)
            {
                Object    moreInfo=typeValue.readAdditionalInfo(in);
                additionalInfos.add(moreInfo);
            }

            setAdditionalInfos(additionalInfos);
        }
        else
        {
            setAdditionalInfos(null);
        }

        logInternal("additionalInfos=" + getAdditionalInfos());
        return this;
    }

    @Override
    public void write (OutputStream out) throws IOException
    {
        final Collection<BinaryTypeEnumeration>    memberTypes=getMemberTypes();
        if (CollectionsUtils.size(memberTypes) > 0)
        {
            for (final BinaryTypeEnumeration typeVal : memberTypes)
                typeVal.write(out);
        }

        // TODO Auto-generated method stub
        throw new StreamCorruptedException("TODO - write additional infos");
    }

    @Override
    public MemberTypeInfo clone () throws CloneNotSupportedException
    {
        MemberTypeInfo    other=getClass().cast(super.clone());
        Collection<BinaryTypeEnumeration>    memberTypes=getMemberTypes();
        if (CollectionsUtils.size(memberTypes) <= 0)
            other.setMemberTypes(null);
        else
            other.setMemberTypes(new ArrayList<BinaryTypeEnumeration>(memberTypes));

        final List<Object>    additionalInfo=getAdditionalInfos();
        if (CollectionsUtils.size(additionalInfo) <= 0)
            other.setAdditionalInfos(null);
        else
            other.setAdditionalInfos(new ArrayList<Object>(additionalInfo));
        return other;
    }

    @Override
    public String toString ()
    {
        return "types=" + getMemberTypes()
             + ";more=" + getAdditionalInfos()
                ;
    }
}

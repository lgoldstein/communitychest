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
import java.util.Collection;
import java.util.List;

import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 7, 2013 3:16:31 PM
 *
 */
public class ClassInfo extends LogHelper
        implements ElementEncoder<ClassInfo>, PubliclyCloneable<ClassInfo>, Serializable, ObjectIdCarrier {
    private static final long serialVersionUID = 4992140721668903685L;

    public static final int    MAX_MEMBERS_COUNT=0xFFFF;

    private long    _objectId;
    private String    _name;
    private List<String>    _memberNames;

    public ClassInfo ()
    {
        super();
    }

    public ClassInfo (InputStream in) throws IOException
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

    public String getName ()
    {
        return _name;
    }

    public void setName (String name)
    {
        _name = name;
    }

    public List<String> getMemberNames ()
    {
        return _memberNames;
    }

    public void setMemberNames (List<String> memberNames)
    {
        _memberNames = memberNames;
    }

    @Override
    public ClassInfo clone () throws CloneNotSupportedException
    {
        ClassInfo    other=getClass().cast(super.clone());
        Collection<String>    namesList=other.getMemberNames();
        if ((namesList == null) || namesList.isEmpty())
            other.setMemberNames(null);
        else
            other.setMemberNames(new ArrayList<String>(namesList));
        return other;
    }

    @Override
    public ClassInfo read (InputStream in) throws IOException
    {
        setObjectId(DataFormatConverter.readUnsignedInt32(in));
        logInternal("objectID=" + getObjectId());

        String    className=SerializationFormatConverter.readLengthPrefixedString(in);
        if ((className == null) || (className.length() <= 0))
            throw new StreamCorruptedException("Missing class name");
        setName(className);
        logInternal("name=" + getName());

        int    count=DataFormatConverter.readSignedInt32(in);
        if (count < 0)
            throw new StreamCorruptedException("Bad members count: " + count);
        /*
         * NOTE: the standard does not impose this restriction but we
         * place it here in case the received value is corrupted due to
         * malformed parsing of the serialization stream.
         */
        if (count > MAX_MEMBERS_COUNT)
            throw new StreamCorruptedException("Unreasonable members count: " + count);
        logInternal("# members=" + count);

        setMemberNames(SerializationFormatConverter.readLengthPrefixedStringList(in, count));
        logInternal("names=" + getMemberNames());
        return this;
    }

    @Override
    public void write (OutputStream out) throws IOException
    {
        DataFormatConverter.writeUnsignedInt32(out, getObjectId());
        SerializationFormatConverter.writeLengthPrefixedString(out, getName());

        Collection<String>    namesList=getMemberNames();
        DataFormatConverter.writeSignedInt32(out, (namesList == null) ? 0 : namesList.size());
        SerializationFormatConverter.writeLengthPrefixedStringList(out, namesList);
    }

    @Override
    public int hashCode ()
    {
        return (int) getObjectId()
            + StringUtil.getDataStringHashCode(getName(), true)
            + CollectionsUtils.size(getMemberNames())    // order may differ
            ;
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

        ClassInfo    other=(ClassInfo) obj;
        if ((getObjectId() == other.getObjectId())
         && (StringUtil.compareDataStrings(getName(), other.getName(), true) == 0)
         && CollectionsUtils.isSameMembers(getMemberNames(), other.getMemberNames()))
            return true;
        else
            return false;
    }

    @Override
    public String toString ()
    {
        return getName() + "@" + getObjectId()
             + ";members=" + getMemberNames()
             ;
    }
}

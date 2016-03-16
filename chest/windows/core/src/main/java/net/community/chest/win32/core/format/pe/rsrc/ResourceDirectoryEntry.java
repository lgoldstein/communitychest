/*
 *
 */
package net.community.chest.win32.core.format.pe.rsrc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.io.encode.hex.Hex;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.math.NumberTables;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 16, 2009 3:23:13 PM
 */
public class ResourceDirectoryEntry
                    implements Serializable,
                               PubliclyCloneable<ResourceDirectoryEntry>,
                               ElementEncoder<ResourceDirectoryEntry> {
    private static final long serialVersionUID = -7255672801446332873L;

    public ResourceDirectoryEntry ()
    {
        super();
    }

    private long    _nameRVA;
    public long getNameRVA ()
    {
        return _nameRVA;
    }

    public void setNameRVA (long nameRVA)
    {
        _nameRVA = nameRVA;
    }

    private int    _integerID;
    public int getIntegerID ()
    {
        return _integerID;
    }

    public void setIntegerID (int integerID)
    {
        _integerID = integerID;
    }

    private long    _dataEntryRVA;
    public long getDataEntryRVA ()
    {
        return _dataEntryRVA;
    }

    public void setDataEntryRVA (long dataEntryRVA)
    {
        _dataEntryRVA = dataEntryRVA;
    }

    private long    _subDirectoryRVA;
    public long getSubDirectoryRVA ()
    {
        return _subDirectoryRVA;
    }

    public void setSubDirectoryRVA (long subDirectoryRVA)
    {
        _subDirectoryRVA = subDirectoryRVA;
    }
    /*
     * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
     */
    @Override
    public ResourceDirectoryEntry read (InputStream in) throws IOException
    {
        setNameRVA(DataFormatConverter.readUnsignedInt32(in));
        setIntegerID(DataFormatConverter.readSignedInt32(in));
        setDataEntryRVA(DataFormatConverter.readUnsignedInt32(in));
        setSubDirectoryRVA(DataFormatConverter.readUnsignedInt32(in));

        return this;
    }

    public void clear ()
    {
        setNameRVA(0L);
        setIntegerID(0);
        setDataEntryRVA(0L);
        setSubDirectoryRVA(0L);
    }

    public ResourceDirectoryEntry (InputStream in) throws IOException
    {
        final Object    o=read(in);
        if (o != this)
            throw new StreamCorruptedException("Mismatched read entries");
    }
    /*
     * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
     */
    @Override
    public void write (OutputStream out) throws IOException
    {
        DataFormatConverter.writeUnsignedInt32(out, getNameRVA());
        DataFormatConverter.writeSignedInt32(out, getIntegerID());
        DataFormatConverter.writeUnsignedInt32(out, getDataEntryRVA());
        DataFormatConverter.writeUnsignedInt32(out, getSubDirectoryRVA());
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public ResourceDirectoryEntry clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if (!(obj instanceof ResourceDirectoryEntry))
            return false;
        if (this == obj)
            return true;

        final ResourceDirectoryEntry    e=(ResourceDirectoryEntry) obj;
        return (e.getDataEntryRVA() == getDataEntryRVA())
            && (e.getIntegerID() == getIntegerID())
            && (e.getNameRVA() == getNameRVA())
            && (e.getSubDirectoryRVA() == getSubDirectoryRVA())
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return NumberTables.getLongValueHashCode(getDataEntryRVA())
             + getIntegerID()
             + NumberTables.getLongValueHashCode(getNameRVA())
             + NumberTables.getLongValueHashCode(getSubDirectoryRVA())
             ;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return "Name=0x" + Hex.toString((int) getNameRVA(), true)
             + "/ID=0x" + Hex.toString(getIntegerID(), true)
            + "/Data=0x" + Hex.toString((int) getDataEntryRVA(), true)
             + "/Sub=0x" + Hex.toString((int) getSubDirectoryRVA(), true)
             ;
    }

    public static final List<ResourceDirectoryEntry> readEntries (
                    final InputStream in, final int numEntries) throws IOException
    {
        if (numEntries <= 0)
            return null;

        final List<ResourceDirectoryEntry>    ret=new ArrayList<ResourceDirectoryEntry>(numEntries);
        for (int    eIndex=0; eIndex < numEntries; eIndex++)
            ret.add(new ResourceDirectoryEntry(in));
        return ret;
    }

    public static final void writeEntries (
            final OutputStream out, final Collection<? extends ResourceDirectoryEntry> el) throws IOException
    {
        if ((null == el) || (el.size() <= 0))
            return;

        for (final ResourceDirectoryEntry e : el)
            e.write(out);
    }
}

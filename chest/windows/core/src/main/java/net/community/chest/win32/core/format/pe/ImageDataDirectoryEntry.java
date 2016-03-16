/*
 *
 */
package net.community.chest.win32.core.format.pe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.io.encode.hex.Hex;
import net.community.chest.lang.EnumUtil;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 16, 2009 10:29:04 AM
 */
public class ImageDataDirectoryEntry
                implements Serializable,
                           PubliclyCloneable<ImageDataDirectoryEntry>,
                           ElementEncoder<ImageDataDirectoryEntry> {
    private static final long serialVersionUID = -4749246621660966313L;

    public ImageDataDirectoryEntry ()
    {
        super();
    }

    // must be provided externally BEFORE read-ing the data
    private short   _magicNumber;
    public short getMagicNumber ()
    {
        return _magicNumber;
    }

    public void setMagicNumber (short magicNumber)
    {
        _magicNumber = magicNumber;
    }

    private long    _rva;
    public long getRva ()
    {
        return _rva;
    }

    public void setRva (long rva)
    {
        _rva = rva;
    }

    private long    _size;
    public long getSize ()
    {
        return _size;
    }

    public void setSize (long size)
    {
        _size = size;
    }

    public boolean isEmpty ()
    {
        return (getRva() == 0L) && (getSize() <= 0L);
    }

    public void clear ()
    {
        setRva(0L);
        setSize(0L);
    }
    /*
     * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
     */
    @Override
    public ImageDataDirectoryEntry read (InputStream in) throws IOException
    {
        setRva(DataFormatConverter.readUnsignedInt32(in));
        setSize(DataFormatConverter.readUnsignedInt32(in));
        return this;
    }

    public ImageDataDirectoryEntry (InputStream in, short magicNumber) throws IOException
    {
        _magicNumber = magicNumber;

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
        DataFormatConverter.writeUnsignedInt32(out, getRva());
        DataFormatConverter.writeUnsignedInt32(out, getSize());
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public ImageDataDirectoryEntry clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if (!(obj instanceof ImageDataDirectoryEntry))
            return false;
        if (this == obj)
            return true;

        final ImageDataDirectoryEntry    e=(ImageDataDirectoryEntry) obj;
        return (e.getMagicNumber() == getMagicNumber())
            && (e.getRva() == getRva())
            && (e.getSize() == getSize())
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return (int) (getRva() + getSize()) + getMagicNumber();
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        final short    magicNumber=getMagicNumber();
        return "RVA=0x" + ((PEFormatDetails.PE32_MAGIC_NUMBER == magicNumber) ? Hex.toString((int) getRva(), true) : Hex.toString(getRva(), true))
            + "/Size=" + getSize()
            ;
    }
    /**
     * <P>Copyright GPLv2</P>
     *
     * <P>The known entry types</P>
     *
     * @author Lyor G.
     * @since Jun 16, 2009 10:47:04 AM
     */
    public static enum DirEntryType {
        EXPORT(0),
        IMPORT(1),
        RESOURCE(2),
        EXCEPTION(3),
        CERTIFICATE(4),
        BASERELOC(5),
        DEBUG(6),
        ARCHITECTURE(7),
        GLOBALS(8),
        TLS(9),
        LOADCFG(10),
        BOUNDIMPORT(11),
        IAT(12),
        DELAYIMPORT(13),
        CLR(14),
        RESERVED(15);

        private final int    _entryIndex;
        public final int getEntryIndex ()
        {
            return _entryIndex;
        }

        DirEntryType (int i)
        {
            _entryIndex = i;
        }

        public static final List<DirEntryType>    VALUES=
                Collections.unmodifiableList(Arrays.asList(values()));
        public static final DirEntryType fromString (final String s)
        {
            return EnumUtil.fromName(VALUES, s, false);
        }

        public static final DirEntryType fromEntryIndex (final int i)
        {
            if (i < 0)
                return null;

            for (final DirEntryType v : VALUES)
            {
                if ((v != null) && (v.getEntryIndex() == i))
                    return v;
            }

            return null;
        }
    }

    public static final <V extends ImageDataDirectoryEntry> Map<DirEntryType,V> buildEntriesMap (
            final List<? extends V> dirEntries, final boolean ignoreEmpty)
    {
        final int    numEntries=(null == dirEntries) ? 0 : dirEntries.size();
        if (numEntries <= 0)
            return null;

        Map<DirEntryType,V>        ret=null;
        for (final DirEntryType v : DirEntryType.VALUES)
        {
            final int    vIndex=(null == v) ? (-1) : v.getEntryIndex();
            if ((vIndex < 0) || (vIndex >= numEntries))
                continue;

            final V    de=dirEntries.get(vIndex);
            if (null == de)
                continue;

            if (de.isEmpty() && ignoreEmpty)
                continue;

            if (null == ret)
                ret = new EnumMap<DirEntryType,V>(DirEntryType.class);
            ret.put(v, de);
        }

        return ret;
    }

    public static final List<ImageDataDirectoryEntry> readDirEntries (
            final InputStream in, final int numEntries, final short magicNumber) throws IOException
    {
        if (numEntries <= 0)
            return null;

        final List<ImageDataDirectoryEntry>    ret=new ArrayList<ImageDataDirectoryEntry>(numEntries);
        for (int    eIndex=0; eIndex < numEntries; eIndex++)
            ret.add(new ImageDataDirectoryEntry(in, magicNumber));
        return ret;
    }

    public static final void writeDirEntries (
            final OutputStream out, final Collection<? extends ImageDataDirectoryEntry> el) throws IOException
    {
        if ((null == el) || (el.size() <= 0))
            return;

        for (final ImageDataDirectoryEntry e : el)
            e.write(out);
    }
}

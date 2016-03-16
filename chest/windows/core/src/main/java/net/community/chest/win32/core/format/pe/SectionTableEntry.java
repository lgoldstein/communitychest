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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.io.encode.hex.Hex;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.lang.math.LongsComparator;
import net.community.chest.lang.math.NumberTables;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 16, 2009 1:33:18 PM
 */
public class SectionTableEntry
                implements Serializable,
                           PubliclyCloneable<SectionTableEntry>,
                           ElementEncoder<SectionTableEntry> {
    private static final long serialVersionUID = -5836903649819978568L;
    public SectionTableEntry ()
    {
        super();
    }

    private String    _name;
    public String getName ()
    {
        return _name;
    }

    public void setName (String name)
    {
        _name = name;
    }

    public static final int    FIXED_NAME_LEN=8;
    public static final String readDefaultName (final InputStream in) throws IOException
    {
        final byte[]    bytes=new byte[FIXED_NAME_LEN];
        final int        readLen=in.read(bytes);
        if (readLen != FIXED_NAME_LEN)
            throw new StreamCorruptedException("readDefaultName() mismatched read size: expected=" + FIXED_NAME_LEN + "/got=" + readLen);
        return DataFormatConverter.toAsciiString(bytes);
    }

    public static final byte[] writeDefaultName (final OutputStream out, final String n) throws IOException
    {
        final byte[]    bytes=DataFormatConverter.toAsciiByteArray(n);
        final int        numBytes=(null == bytes) ? 0 : bytes.length,
                        num2Copy=Math.min(numBytes, FIXED_NAME_LEN);
        if (numBytes > FIXED_NAME_LEN)
            throw new StreamCorruptedException("writeDefaultName(" + n + ") name length (" + numBytes + ") exceeds max. allowed (" + FIXED_NAME_LEN + ")");

        // reuse the same bytes array if have more than enough data
        final byte[]    data=(numBytes < FIXED_NAME_LEN) ? new byte[FIXED_NAME_LEN] : bytes;
        if ((num2Copy > 0) && (numBytes < FIXED_NAME_LEN))
            System.arraycopy(bytes, 0, data, 0, num2Copy);

        out.write(data, 0, FIXED_NAME_LEN);
        return data;
    }

    protected String readName (final InputStream in) throws IOException
    {
        return readDefaultName(in);
    }

    protected byte[] writeName (final OutputStream out, final String n) throws IOException
    {
        return writeDefaultName(out, n);
    }

    private long    _virtualSize;
    public long getVirtualSize ()
    {
        return _virtualSize;
    }

    public void setVirtualSize (long virtualSize)
    {
        _virtualSize = virtualSize;
    }

    private long    _virtualAddress;
    public long getVirtualAddress ()
    {
        return _virtualAddress;
    }

    public void setVirtualAddress (long virtualAddress)
    {
        _virtualAddress = virtualAddress;
    }

    private long    _sizeOfRawData;
    public long getSizeOfRawData ()
    {
        return _sizeOfRawData;
    }

    public void setSizeOfRawData (long sizeOfRawData)
    {
        _sizeOfRawData = sizeOfRawData;
    }

    public static final Comparator<SectionTableEntry> BY_RAW_DATA_POINTER=
            new Comparator<SectionTableEntry>() {
                @Override
                public int compare (SectionTableEntry e1, SectionTableEntry e2) {
                    long    p1=(e1 == null) ? Long.MAX_VALUE : e1.getPointerToRawData();
                    long    p2=(e2 == null) ? Long.MAX_VALUE : e2.getPointerToRawData();
                    return LongsComparator.compare(p1, p2);
                }
        };

    private long    _pointerToRawData;
    public long getPointerToRawData ()
    {
        return _pointerToRawData;
    }

    public void setPointerToRawData (long pointerToRawData)
    {
        _pointerToRawData = pointerToRawData;
    }

    private long    _pointerToRelocations;
    public long getPointerToRelocations ()
    {
        return _pointerToRelocations;
    }

    public void setPointerToRelocations (long pointerToRelocations)
    {
        _pointerToRelocations = pointerToRelocations;
    }

    private long    _pointerToLineNumbers;
    public long getPointerToLineNumbers ()
    {
        return _pointerToLineNumbers;
    }

    public void setPointerToLineNumbers (long pointerToLineNumbers)
    {
        _pointerToLineNumbers = pointerToLineNumbers;
    }

    private int    _numberOfRelocations;
    public int getNumberOfRelocations ()
    {
        return _numberOfRelocations;
    }

    public void setNumberOfRelocations (int numberOfRelocations)
    {
        _numberOfRelocations = numberOfRelocations;
    }

    private int    _numberOfLineNumbers;
    public int getNumberOfLineNumbers ()
    {
        return _numberOfLineNumbers;
    }

    public void setNumberOfLineNumbers (int numberOfLineNumbers)
    {
        _numberOfLineNumbers = numberOfLineNumbers;
    }

    public static final int    IMAGE_SCN_RESERVED_1=0x00000001;
    public static final int    IMAGE_SCN_RESERVED_2=0x00000002;
    public static final int    IMAGE_SCN_RESERVED_4=0x00000004;
    public static final int    IMAGE_SCN_TYPE_NO_PAD=0x00000008;
    public static final int    IMAGE_SCN_RESERVED_16=0x00000010;
    public static final int    IMAGE_SCN_CNT_CODE=0x00000020;
    public static final int    IMAGE_SCN_CNT_INITIALIZED_DATA=0x00000040;
    public static final int    IMAGE_SCN_CNT_UNINITIALIZED_DATA=0x00000080;
    public static final int    IMAGE_SCN_LNK_OTHER=0x00000100;
    public static final int    IMAGE_SCN_LNK_INFO=0x00000200;
    public static final int    IMAGE_SCN_RESERVED_256=0x00000400;
    public static final int    IMAGE_SCN_LNK_REMOVE=0x00000800;
    public static final int    IMAGE_SCN_LNK_COMDAT=0x00001000;
    public static final int    IMAGE_SCN_GPREL=0x00008000;
    public static final int    IMAGE_SCN_MEM_PURGEABLE=0x00020000;
    public static final int    IMAGE_SCN_MEM_16BIT=0x00020000;
    public static final int    IMAGE_SCN_MEM_LOCKED=0x00040000;
    public static final int    IMAGE_SCN_MEM_PRELOAD=0x00080000;
    public static final int    IMAGE_SCN_ALIGN_1BYTES=0x00100000;
    public static final int    IMAGE_SCN_ALIGN_2BYTES=0x00200000;
    public static final int    IMAGE_SCN_ALIGN_4BYTES=0x00300000;
    public static final int    IMAGE_SCN_ALIGN_8BYTES=0x00400000;
    public static final int    IMAGE_SCN_ALIGN_16BYTES=0x00500000;
    public static final int    IMAGE_SCN_ALIGN_32BYTES=0x00600000;
    public static final int    IMAGE_SCN_ALIGN_64BYTES=0x00700000;
    public static final int    IMAGE_SCN_ALIGN_128BYTES=0x00800000;
    public static final int    IMAGE_SCN_ALIGN_256BYTES=0x00900000;
    public static final int    IMAGE_SCN_ALIGN_512BYTES=0x00A00000;
    public static final int    IMAGE_SCN_ALIGN_1024BYTES=0x00B00000;
    public static final int    IMAGE_SCN_ALIGN_2048BYTES=0x00C00000;
    public static final int    IMAGE_SCN_ALIGN_4096BYTES=0x00D00000;
    public static final int    IMAGE_SCN_ALIGN_8192BYTES=0x00E00000;
    public static final int    IMAGE_SCN_LNK_NRELOC_OVFL=0x01000000;
    public static final int    IMAGE_SCN_MEM_DISCARDABLE=0x02000000;
    public static final int    IMAGE_SCN_MEM_NOT_CACHED=0x04000000;
    public static final int    IMAGE_SCN_MEM_NOT_PAGED=0x08000000;
    public static final int    IMAGE_SCN_MEM_SHARED=0x10000000;
    public static final int    IMAGE_SCN_MEM_EXECUTE=0x20000000;
    public static final int    IMAGE_SCN_MEM_READ=0x40000000;
    public static final int    IMAGE_SCN_MEM_WRITE=0x80000000;

    private int    _characteristics;
    public int getCharacteristics ()
    {
        return _characteristics;
    }

    public void setCharacteristics (int characteristics)
    {
        _characteristics = characteristics;
    }
    /*
     * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
     */
    @Override
    public SectionTableEntry read (InputStream in) throws IOException
    {
        setName(readName(in));

        setVirtualSize(DataFormatConverter.readUnsignedInt32(in));
        setVirtualAddress(DataFormatConverter.readUnsignedInt32(in));
        setSizeOfRawData(DataFormatConverter.readUnsignedInt32(in));
        setPointerToRawData(DataFormatConverter.readUnsignedInt32(in));
        setPointerToRelocations(DataFormatConverter.readUnsignedInt32(in));
        setPointerToLineNumbers(DataFormatConverter.readUnsignedInt32(in));
        setNumberOfRelocations(DataFormatConverter.readUnsignedInt16(in));
        setNumberOfLineNumbers(DataFormatConverter.readUnsignedInt16(in));
        setCharacteristics(DataFormatConverter.readSignedInt32(in));

        return this;
    }

    public void clear ()
    {
        setName(null);
        setVirtualSize(0L);
        setVirtualAddress(0L);
        setSizeOfRawData(0L);
        setPointerToRawData(0L);
        setPointerToRelocations(0L);
        setPointerToLineNumbers(0L);
        setNumberOfRelocations(0);
        setNumberOfLineNumbers(0);
        setCharacteristics(0);
    }

    public SectionTableEntry (InputStream in) throws IOException
    {
        final Object    o=read(in);
        if (o != this)
            throw new StreamCorruptedException("Mismatched read instances");
    }
    /*
     * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
     */
    @Override
    public void write (OutputStream out) throws IOException
    {
        writeName(out, getName());

        DataFormatConverter.writeUnsignedInt32(out, getVirtualSize());
        DataFormatConverter.writeUnsignedInt32(out, getVirtualAddress());
        DataFormatConverter.writeUnsignedInt32(out, getSizeOfRawData());
        DataFormatConverter.writeUnsignedInt32(out, getPointerToRawData());
        DataFormatConverter.writeUnsignedInt32(out, getPointerToRelocations());
        DataFormatConverter.writeUnsignedInt32(out, getPointerToLineNumbers());
        DataFormatConverter.writeUnsignedInt16(out, getNumberOfRelocations());
        DataFormatConverter.writeUnsignedInt16(out, getNumberOfLineNumbers());
        DataFormatConverter.writeSignedInt32(out, getCharacteristics());
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if (!(obj instanceof SectionTableEntry))
            return false;
        if (this == obj)
            return true;

        final SectionTableEntry    e=(SectionTableEntry) obj;
        return (0 == StringUtil.compareDataStrings(getName(), e.getName(), true))
            && (e.getCharacteristics() == getCharacteristics())
            && (e.getNumberOfLineNumbers() == getNumberOfLineNumbers())
            && (e.getNumberOfRelocations() == getNumberOfRelocations())
            && (e.getPointerToLineNumbers() == getPointerToLineNumbers())
            && (e.getPointerToRawData() == getPointerToRawData())
            && (e.getPointerToRelocations() == getPointerToRelocations())
            && (e.getSizeOfRawData() == getSizeOfRawData())
            && (e.getVirtualAddress() == getVirtualAddress())
            && (e.getVirtualSize() == getVirtualSize())
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return StringUtil.getDataStringHashCode(getName(), true)
             + getCharacteristics()
             + getNumberOfLineNumbers()
             + getNumberOfRelocations()
             + NumberTables.getLongValueHashCode(getPointerToLineNumbers())
             + NumberTables.getLongValueHashCode(getPointerToRawData())
             + NumberTables.getLongValueHashCode(getPointerToRelocations())
             + NumberTables.getLongValueHashCode(getSizeOfRawData())
             + NumberTables.getLongValueHashCode(getVirtualAddress())
             + NumberTables.getLongValueHashCode(getVirtualSize())
             ;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        final int  chars=getCharacteristics();
        return "Name=" + getName()
             + "/Flags=0x" + Hex.toString(chars, true) + ": " + SectionTableCharacter.fromSectionCharacteristics(chars)
             + "/#lines=" + getNumberOfLineNumbers()
             + "/#relocs="+ getNumberOfRelocations()
             + "/line#ptr=0x" + Hex.toString((int) getPointerToLineNumbers(), true)
             + "/raw#ptr=0x" + Hex.toString((int) getPointerToRawData(), true)
             + "/relocs#ptr=0x" + Hex.toString((int) getPointerToRelocations(), true)
             + "/raw-size=" + getSizeOfRawData()
             + "/virtaddr=0x" + Hex.toString((int) getVirtualAddress(), true)
             + "/virtsize=" + getVirtualSize()
             ;
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public SectionTableEntry clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    public static final List<SectionTableEntry> readSections (
            final InputStream in, final int numSections) throws IOException
    {
        if (numSections <= 0)
            return null;

        final List<SectionTableEntry>    sl=new ArrayList<SectionTableEntry>(numSections);
        for (int    sIndex=0; sIndex < numSections; sIndex++)
            sl.add(new SectionTableEntry(in));
        return sl;
    }

    public static final void writeSections (
            final OutputStream out, final Collection<? extends SectionTableEntry> sl) throws IOException
    {
        if ((null == sl) || (sl.size() <= 0))
            return;

        for (final SectionTableEntry e : sl)
            e.write(out);
    }

    public static final Map<String,Collection<SectionTableEntry>> buildSectionsMap (
            final Collection<? extends SectionTableEntry> sl, final boolean caseSensitive)
    {
        if ((null == sl) || (sl.size() <= 0))
            return null;

        Map<String,Collection<SectionTableEntry>>    ret=null;
        for (final SectionTableEntry te : sl)
        {
            if (null == te)
                continue;

            if (null == ret)
                ret = caseSensitive
                    ? new HashMap<String,Collection<SectionTableEntry>>(sl.size(), 1.0f)
                    : new TreeMap<String,Collection<SectionTableEntry>>(String.CASE_INSENSITIVE_ORDER)
                    ;
            final String                    sn=te.getName(), kn=(null == sn) ? "" : sn;
            Collection<SectionTableEntry>    ll=ret.get(kn);
            if (null == ll)
            {
                ll = new LinkedList<SectionTableEntry>();
                ret.put(kn, ll);
            }

            ll.add(te);
        }

        return ret;
    }
}

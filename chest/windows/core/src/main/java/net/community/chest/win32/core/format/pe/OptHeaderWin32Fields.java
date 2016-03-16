/*
 *
 */
package net.community.chest.win32.core.format.pe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
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
 * @since Jun 16, 2009 9:06:12 AM
 */
public class OptHeaderWin32Fields
                implements Serializable,
                           PubliclyCloneable<OptHeaderWin32Fields>,
                           ElementEncoder<OptHeaderWin32Fields> {
    private static final long serialVersionUID = 1506954781268992078L;
    // must be provided externally BEFORE read-ing the data
    private short    _magicNumber;
    public short getMagicNumber ()
    {
        return _magicNumber;
    }

    public void setMagicNumber (short magicNumber)
    {
        _magicNumber = magicNumber;
    }
    /**
     * <P>Copyright GPLv2</P>
     *
     * <P>The conditional fields whose size can be 4 or 8</P
     *
     * @author Lyor G.
     * @since Jun 16, 2009 10:19:48 AM
     */
    public static enum CondLongField {
        ImageBase,
        SizeOfStackReserve,
        SizeOfStackCommit,
        SizeOfHeapReserve,
        SizeOfHeapCommit;

        public final long readConditionalLong (InputStream in, short magicNumber) throws IOException {
            if (magicNumber == PEFormatDetails.PE32_MAGIC_NUMBER)
                return DataFormatConverter.readUnsignedInt32(in);
            else if (magicNumber == PEFormatDetails.PE32PLUS_MAGIC_NUMBER)
                return DataFormatConverter.readSignedInt64(in);
            else
                throw new StreamCorruptedException("readConditionalLong(" + name() + ") unknown magic nunber: 0x" + Hex.toString(magicNumber, true));
        }

        public final byte[] writeConditionalLong (OutputStream out, short magicNumber, long val) throws IOException {
            if (magicNumber == PEFormatDetails.PE32_MAGIC_NUMBER)
                return DataFormatConverter.writeUnsignedInt32(out, val);
            else if (magicNumber == PEFormatDetails.PE32PLUS_MAGIC_NUMBER)
                return DataFormatConverter.writeInt64(out, val);
            else
                throw new StreamCorruptedException("writeConditionalLong(" + name() + ")[" + val + "] unknown magic nunber: 0x" + Hex.toString(magicNumber, true));
        }


        public final String getConditionalLongString (final short magicNumber, final long v, final boolean useHex)
        {
            if (magicNumber == PEFormatDetails.PE32_MAGIC_NUMBER)
                return useHex ? Hex.toString((int) (v & 0x00FFFFFFFFL), true) : String.valueOf(v & 0x00FFFFFFFFL);
            else if (magicNumber == PEFormatDetails.PE32PLUS_MAGIC_NUMBER)
                return useHex ? Hex.toString(v, true) : String.valueOf(v);
            else
                throw new IllegalArgumentException("getConditionalLongString(" + name() + ")[" + v + "] unknown magic nunber: 0x" + Hex.toString(magicNumber, true));
        }
    }

    protected long readConditionalLong (InputStream in, CondLongField fld) throws IOException
    {
        return fld.readConditionalLong(in, getMagicNumber());
    }

    protected byte[] writeConditionalLong (OutputStream out, CondLongField fld, long val) throws IOException
    {
        return fld.writeConditionalLong(out, getMagicNumber(), val);
    }

    public OptHeaderWin32Fields (short magicNumber)
    {
        _magicNumber = magicNumber;
    }

    public OptHeaderWin32Fields ()
    {
        this((short) 0);
    }

    private long    _imageBase;
    public long getImageBase ()
    {
        return _imageBase;
    }

    public void setImageBase (long imageBase)
    {
        _imageBase = imageBase;
    }

    private long    _sectionAlignment;
    public long getSectionAlignment ()
    {
        return _sectionAlignment;
    }

    public void setSectionAlignment (long sectionAlignment)
    {
        _sectionAlignment = sectionAlignment;
    }

    private long    _fileAlignment;
    public long getFileAlignment ()
    {
        return _fileAlignment;
    }

    public void setFileAlignment (long fileAlignment)
    {
        _fileAlignment = fileAlignment;
    }

    private int    _majorOperatingSystemVersion;
    public int getMajorOperatingSystemVersion ()
    {
        return _majorOperatingSystemVersion;
    }

    public void setMajorOperatingSystemVersion (int majorOperatingSystemVersion)
    {
        _majorOperatingSystemVersion = majorOperatingSystemVersion;
    }

    private int    _minorOperatingSystemVersion;
    public int getMinorOperatingSystemVersion ()
    {
        return _minorOperatingSystemVersion;
    }

    public void setMinorOperatingSystemVersion (int minorOperatingSystemVersion)
    {
        _minorOperatingSystemVersion = minorOperatingSystemVersion;
    }

    private int    _majorImageVersion;
    public int getMajorImageVersion ()
    {
        return _majorImageVersion;
    }

    public void setMajorImageVersion (int majorImageVersion)
    {
        _majorImageVersion = majorImageVersion;
    }

    private int    _minorImageVersion;
    public int getMinorImageVersion ()
    {
        return _minorImageVersion;
    }

    public void setMinorImageVersion (int minorImageVersion)
    {
        _minorImageVersion = minorImageVersion;
    }

    private int    _majorSubsystemVersion;
    public int getMajorSubsystemVersion ()
    {
        return _majorSubsystemVersion;
    }

    public void setMajorSubsystemVersion (int majorSubsystemVersion)
    {
        _majorSubsystemVersion = majorSubsystemVersion;
    }

    private int    _minorSubsystemVersion;
    public int getMinorSubsystemVersion ()
    {
        return _minorSubsystemVersion;
    }

    public void setMinorSubsystemVersion (int minorSubsystemVersion)
    {
        _minorSubsystemVersion = minorSubsystemVersion;
    }

    private int    _reservedValue;
    public int getReservedValue ()
    {
        return _reservedValue;
    }

    public void setReservedValue (int reservedValue)
    {
        _reservedValue = reservedValue;
    }

    private long    _sizeOfImage;
    public long getSizeOfImage ()
    {
        return _sizeOfImage;
    }

    public void setSizeOfImage (long sizeOfImage)
    {
        _sizeOfImage = sizeOfImage;
    }

    private long    _sizeOfHeaders;
    public long getSizeOfHeaders ()
    {
        return _sizeOfHeaders;
    }

    public void setSizeOfHeaders (long sizeOfHeaders)
    {
        _sizeOfHeaders = sizeOfHeaders;
    }

    private int    _checksum;
    public int getChecksum ()
    {
        return _checksum;
    }

    public void setChecksum (int checksum)
    {
        _checksum = checksum;
    }

    public static final short  IMAGE_SUBSYSTEM_UNKNOWN=0;
    public static final short  IMAGE_SUBSYSTEM_NATIVE=1;
    public static final short  IMAGE_SUBSYSTEM_WINDOWS_GUI=2;
    public static final short  IMAGE_SUBSYSTEM_WINDOWS_CUI=3;
    public static final short  IMAGE_SUBSYSTEM_POSIX_CUI=7;
    public static final short  IMAGE_SUBSYSTEM_WINDOWS_CE_GUI=9;
    public static final short  IMAGE_SUBSYSTEM_EFI_APPLICATION=10;
    public static final short  IMAGE_SUBSYSTEM_EFI_BOOT_SERVICE_DRIVER=11;
    public static final short  IMAGE_SUBSYSTEM_EFI_RUNTIME_DRIVER=12;
    public static final short  IMAGE_SUBSYSTEM_EFI_ROM=13;
    public static final short  IMAGE_SUBSYSTEM_XBOX=14;

    private short    _subsystem;
    public short getSubsystem ()
    {
        return _subsystem;
    }

    public void setSubsystem (short subsystem)
    {
        _subsystem = subsystem;
    }

    public static final short  IMAGE_DLL_CHARACTERISTICS_RESERVED_1=0x0001;
    public static final short  IMAGE_DLL_CHARACTERISTICS_RESERVED_2=0x0002;
    public static final short  IMAGE_DLL_CHARACTERISTICS_RESERVED_4=0x0004;
    public static final short  IMAGE_DLL_CHARACTERISTICS_RESERVED_8=0x0008;
    public static final short  IMAGE_DLL_CHARACTERISTICS_RESERVED_16=0x0010;
    public static final short  IMAGE_DLL_CHARACTERISTICS_RESERVED_32=0x0020;
    public static final short  IMAGE_DLL_CHARACTERISTICS_DYNAMIC_BASE=0x0040;
    public static final short  IMAGE_DLL_CHARACTERISTICS_FORCE_INTEGRITY=0x0080;
    public static final short  IMAGE_DLL_CHARACTERISTICS_NX_COMPAT=0x0100;
    public static final short  IMAGE_DLL_CHARACTERISTICS_NO_ISOLATION=0x0200;
    public static final short  IMAGE_DLL_CHARACTERISTICS_NO_SEH=0x0400;
    public static final short  IMAGE_DLL_CHARACTERISTICS_NO_BIND=0x0800;
    public static final short  IMAGE_DLL_CHARACTERISTICS_RESERVED_1024=0x1000;
    public static final short  IMAGE_DLL_CHARACTERISTICS_WDM_DRIVER=0x2000;
    public static final short  IMAGE_DLL_CHARACTERISTICS_RESERVED_4096=0x4000;
    public static final short  IMAGE_DLL_CHARACTERISTICS_TERMINAL_SERVER_AWARE=(short) 0x8000;

    private short    _dllCharacteristics;
    public short getDllCharacteristics ()
    {
        return _dllCharacteristics;
    }

    public void setDllCharacteristics (short dllCharacteristics)
    {
        _dllCharacteristics = dllCharacteristics;
    }

    private long    _sizeOfStackReserve;
    public long getSizeOfStackReserve ()
    {
        return _sizeOfStackReserve;
    }

    public void setSizeOfStackReserve (long sizeOfStackReserve)
    {
        _sizeOfStackReserve = sizeOfStackReserve;
    }

    private long    _sizeOfStackCommit;
    public long getSizeOfStackCommit ()
    {
        return _sizeOfStackCommit;
    }

    public void setSizeOfStackCommit (long sizeOfStackCommit)
    {
        _sizeOfStackCommit = sizeOfStackCommit;
    }

    private long    _sizeOfHeapReserve;
    public long getSizeOfHeapReserve ()
    {
        return _sizeOfHeapReserve;
    }

    public void setSizeOfHeapReserve (long sizeOfHeapReserve)
    {
        _sizeOfHeapReserve = sizeOfHeapReserve;
    }

    private long    _sizeOfHeapCommit;
    public long getSizeOfHeapCommit ()
    {
        return _sizeOfHeapCommit;
    }

    public void setSizeOfHeapCommit (long sizeOfHeapCommit)
    {
        _sizeOfHeapCommit = sizeOfHeapCommit;
    }

    private int    _loaderFlags;
    public int getLoaderFlags ()
    {
        return _loaderFlags;
    }

    public void setLoaderFlags (int loaderFlags)
    {
        _loaderFlags = loaderFlags;
    }
    // TODO change this to a long if ever expect more than 2GB entries
    private int    _numberOfRvaAndSizes;
    public int getNumberOfRvaAndSizes ()
    {
        return _numberOfRvaAndSizes;
    }

    public void setNumberOfRvaAndSizes (int numberOfRvaAndSizes)
    {
        _numberOfRvaAndSizes = numberOfRvaAndSizes;
    }
    // NOTE !!! the caller must make sure that the number of reported RVA(s) matches the number of directory entries
    private List<ImageDataDirectoryEntry>    _dirEntries;
    public List<ImageDataDirectoryEntry> getDirEntries ()
    {
        return _dirEntries;
    }

    public void setDirEntries (List<ImageDataDirectoryEntry> dirEntries)
    {
        _dirEntries = dirEntries;
    }

    protected List<ImageDataDirectoryEntry> readDirEntries (
            final InputStream in, final int numRVAs) throws IOException
    {
        return ImageDataDirectoryEntry.readDirEntries(in, numRVAs, getMagicNumber());
    }

    public OptHeaderWin32Fields read (InputStream in, boolean readEntries) throws IOException
    {
        setImageBase(readConditionalLong(in, CondLongField.ImageBase));

        setSectionAlignment(DataFormatConverter.readUnsignedInt32(in));
        setFileAlignment(DataFormatConverter.readUnsignedInt32(in));

        setMajorOperatingSystemVersion(DataFormatConverter.readUnsignedInt16(in));
        setMinorOperatingSystemVersion(DataFormatConverter.readUnsignedInt16(in));

        setMajorImageVersion(DataFormatConverter.readUnsignedInt16(in));
        setMinorImageVersion(DataFormatConverter.readUnsignedInt16(in));

        setMajorSubsystemVersion(DataFormatConverter.readUnsignedInt16(in));
        setMinorSubsystemVersion(DataFormatConverter.readUnsignedInt16(in));

        setReservedValue(DataFormatConverter.readSignedInt32(in));
        setSizeOfImage(DataFormatConverter.readUnsignedInt32(in));
        setSizeOfHeaders(DataFormatConverter.readUnsignedInt32(in));
        setChecksum(DataFormatConverter.readSignedInt32(in));
        setSubsystem(DataFormatConverter.readSignedInt16(in));
        setDllCharacteristics(DataFormatConverter.readSignedInt16(in));

        setSizeOfStackReserve(readConditionalLong(in, CondLongField.SizeOfStackReserve));
        setSizeOfStackCommit(readConditionalLong(in, CondLongField.SizeOfStackCommit));

        setSizeOfHeapReserve(readConditionalLong(in, CondLongField.SizeOfHeapReserve));
        setSizeOfHeapCommit(readConditionalLong(in, CondLongField.SizeOfHeapCommit));

        setLoaderFlags(DataFormatConverter.readSignedInt32(in));

        final int    numRVAs=DataFormatConverter.readSignedInt32(in);
        setNumberOfRvaAndSizes(numRVAs);
        if (readEntries)
            setDirEntries(readDirEntries(in, numRVAs));
        return this;
    }
    /*
     * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
     */
    @Override
    public OptHeaderWin32Fields read (InputStream in) throws IOException
    {
        return read(in, true);
    }

    public void clear ()
    {
        setMagicNumber((short) 0);
        setImageBase(0L);

        setSectionAlignment(0L);
        setFileAlignment(0L);

        setMajorOperatingSystemVersion(0);
        setMinorOperatingSystemVersion(0);

        setMajorImageVersion(0);
        setMinorImageVersion(0);

        setMajorSubsystemVersion(0);
        setMinorSubsystemVersion(0);

        setReservedValue(0);
        setSizeOfImage(0);
        setSizeOfHeaders(0);
        setChecksum(0);
        setSubsystem((short) 0);
        setDllCharacteristics((short) 0);

        setSizeOfStackReserve(0L);
        setSizeOfStackCommit(0L);

        setSizeOfHeapReserve(0L);
        setSizeOfHeapCommit(0L);

        setLoaderFlags(0);

        setNumberOfRvaAndSizes(0);
        setDirEntries(null);
    }

    protected void writeDirEntries (final OutputStream out, final List<ImageDataDirectoryEntry> sl) throws IOException
    {
        ImageDataDirectoryEntry.writeDirEntries(out, sl);
    }

    public void write (OutputStream out, boolean writeEntries) throws IOException
    {
        writeConditionalLong(out, CondLongField.ImageBase, getImageBase());

        DataFormatConverter.writeUnsignedInt32(out, getSectionAlignment());
        DataFormatConverter.writeUnsignedInt32(out, getFileAlignment());

        DataFormatConverter.writeUnsignedInt16(out, getMajorOperatingSystemVersion());
        DataFormatConverter.writeUnsignedInt16(out, getMinorOperatingSystemVersion());

        DataFormatConverter.writeUnsignedInt16(out, getMajorImageVersion());
        DataFormatConverter.writeUnsignedInt16(out, getMinorImageVersion());

        DataFormatConverter.writeUnsignedInt16(out, getMajorSubsystemVersion());
        DataFormatConverter.writeUnsignedInt16(out, getMinorSubsystemVersion());

        DataFormatConverter.writeSignedInt32(out, getReservedValue());
        DataFormatConverter.writeUnsignedInt32(out, getSizeOfImage());
        DataFormatConverter.writeUnsignedInt32(out, getSizeOfHeaders());
        DataFormatConverter.writeSignedInt32(out, getChecksum());
        DataFormatConverter.writeSignedInt16(out, getSubsystem());
        DataFormatConverter.writeSignedInt16(out, getDllCharacteristics());

        writeConditionalLong(out, CondLongField.SizeOfStackReserve, getSizeOfStackReserve());
        writeConditionalLong(out, CondLongField.SizeOfStackCommit, getSizeOfStackCommit());

        writeConditionalLong(out, CondLongField.SizeOfHeapReserve, getSizeOfHeapReserve());
        writeConditionalLong(out, CondLongField.SizeOfHeapCommit, getSizeOfHeapCommit());

        DataFormatConverter.writeSignedInt32(out, getLoaderFlags());
        DataFormatConverter.writeSignedInt32(out, getNumberOfRvaAndSizes());
        // NOTE !!! the caller must make sure that the number of reported RVA(s) matches the number of directory entries
        if (writeEntries)
            writeDirEntries(out, getDirEntries());
    }
    /*
     * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
     */
    @Override
    public void write (OutputStream out) throws IOException
    {
        write(out, true);
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;

        final OptHeaderWin32Fields    h=(OptHeaderWin32Fields) obj;
        return (h.getMagicNumber() == getMagicNumber())
            && (h.getChecksum() == getChecksum())
            && (h.getDllCharacteristics() == getDllCharacteristics())
            && (h.getFileAlignment() == getFileAlignment())
            && (h.getImageBase() == getImageBase())
            && (h.getLoaderFlags() == getLoaderFlags())
            && (h.getMajorImageVersion() == getMajorImageVersion())
            && (h.getMajorOperatingSystemVersion() == getMajorOperatingSystemVersion())
            && (h.getMajorSubsystemVersion() == getMajorSubsystemVersion())
            && (h.getMinorImageVersion() == getMinorImageVersion())
            && (h.getMinorOperatingSystemVersion() == getMinorOperatingSystemVersion())
            && (h.getMinorSubsystemVersion() == getMinorSubsystemVersion())
            && (h.getNumberOfRvaAndSizes() == getNumberOfRvaAndSizes())
            && (h.getReservedValue() == getReservedValue())
            && (h.getSectionAlignment() == getSectionAlignment())
            && (h.getSizeOfHeaders() == getSizeOfHeaders())
            && (h.getSizeOfHeapCommit() == getSizeOfHeapCommit())
            && (h.getSizeOfHeapReserve() == getSizeOfHeapReserve())
            && (h.getSizeOfImage() == getSizeOfImage())
            && (h.getSizeOfStackCommit() == getSizeOfStackCommit())
            && (h.getSizeOfStackReserve() == getSizeOfStackReserve())
            && (h.getSubsystem() == getSubsystem())
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return getChecksum()
             + getDllCharacteristics()
             + NumberTables.getLongValueHashCode(getFileAlignment())
             + NumberTables.getLongValueHashCode(getImageBase())
             + getLoaderFlags()
             + getMagicNumber()
             + getMajorImageVersion()
             + getMajorOperatingSystemVersion()
             + getMajorSubsystemVersion()
             + getMinorImageVersion()
             + getMinorOperatingSystemVersion()
             + getMinorSubsystemVersion()
             + getNumberOfRvaAndSizes()
             + getReservedValue()
             + NumberTables.getLongValueHashCode(getSectionAlignment())
             + NumberTables.getLongValueHashCode(getSizeOfHeaders())
             + NumberTables.getLongValueHashCode(getSizeOfHeapCommit())
             + NumberTables.getLongValueHashCode(getSizeOfHeapReserve())
             + NumberTables.getLongValueHashCode(getSizeOfImage())
             + NumberTables.getLongValueHashCode(getSizeOfStackCommit())
             + NumberTables.getLongValueHashCode(getSizeOfStackReserve())
             + getSubsystem()
            ;
    }

    protected String getConditionalLongString (final CondLongField fld, final long v, final boolean useHex)
    {
        if (fld == null) {
            return String.valueOf(v);
        } else {
            return fld.getConditionalLongString(getMagicNumber(), v, useHex);
        }
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        final short   subsys=getSubsystem(), dllChars=getDllCharacteristics();
        return "ImageBase=0x" + getConditionalLongString(CondLongField.ImageBase, getImageBase(), true)
            + "/SectionAlignment=" + getSectionAlignment()
            + "/FileAlignment=" + getFileAlignment()
            + "/OS-version=" + getMajorOperatingSystemVersion() + "." + getMinorOperatingSystemVersion()
            + "/Image-version=" + getMajorImageVersion() + "." + getMinorImageVersion()
            + "/Subsys-version=" + getMajorSubsystemVersion() + "." + getMinorSubsystemVersion()
            + "/Reserved=0x" + Hex.toString(getReservedValue(), true)
            + "/SizeOfImage=" + getSizeOfImage()
            + "/SizeOfHeaders=" + getSizeOfHeaders()
            + "/Checksum=0x" + Hex.toString(getChecksum(), true)
            + "/Subsystem=" + subsys + "[" + OptHeaderSubsystemType.fromTypeValue(subsys) + "]"
            + "/DLL-Characteristics=0x" + Hex.toString(dllChars, true) + ": " + OptHeaderDllCharacter.fromCharacteristics(dllChars)
            + "/SizeOfStackReserve=" + getConditionalLongString(CondLongField.SizeOfStackReserve, getSizeOfStackReserve(), false)
            + "/SizeOfStackCommit=" + getConditionalLongString(CondLongField.SizeOfStackCommit, getSizeOfStackCommit(), false)
            + "/SizeOfHeapReserve=" + getConditionalLongString(CondLongField.SizeOfHeapReserve, getSizeOfHeapReserve(), false)
            + "/SizeOfHeapCommit=" + getConditionalLongString(CondLongField.SizeOfHeapCommit, getSizeOfHeapCommit(), false)
            + "/LoaderFlags=0x" + Hex.toString(getLoaderFlags(), true)
            + "/NumberOfRvaAndSizes=" + getNumberOfRvaAndSizes()
            ;
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public OptHeaderWin32Fields clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
}

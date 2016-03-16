/*
 *
 */
package net.community.chest.win32.core.format.pe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.text.DateFormat;
import java.util.Date;

import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.io.encode.hex.Hex;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.math.NumberTables;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 15, 2009 1:48:47 PM
 */
public class COFFFileHeader
        implements Serializable,
                   PubliclyCloneable<COFFFileHeader>,
                   ElementEncoder<COFFFileHeader> {
    private static final long serialVersionUID = 1L;
    public COFFFileHeader ()
    {
        super();
    }

    public static final short   IMAGE_FILE_MACHINE_UNKNOWN=0x0000;
    public static final short   IMAGE_FILE_MACHINE_AM33=0x01d3;
    public static final short   IMAGE_FILE_MACHINE_AMD64=(short) 0x8664;
    public static final short   IMAGE_FILE_MACHINE_ARM=0x1c0;
    public static final short   IMAGE_FILE_MACHINE_ARMNT=0x1c4;
    public static final short   IMAGE_FILE_MACHINE_ARM64=(short) 0xaa64;
    public static final short   IMAGE_FILE_MACHINE_EBC=0x0ebc;
    public static final short   IMAGE_FILE_MACHINE_I386=0x014c;
    public static final short   IMAGE_FILE_MACHINE_IA64=0x0200;
    public static final short   IMAGE_FILE_MACHINE_M32R=(short) 0x9041;
    public static final short   IMAGE_FILE_MACHINE_MIPS16=0x0266;
    public static final short   IMAGE_FILE_MACHINE_MIPSFPU=0x0366;
    public static final short   IMAGE_FILE_MACHINE_MIPSFPU16=0x0466;
    public static final short   IMAGE_FILE_MACHINE_POWERPC=0x01f0;
    public static final short   IMAGE_FILE_MACHINE_POWERPCFP=0x01f1;
    public static final short   IMAGE_FILE_MACHINE_R4000=0x0166;
    public static final short   IMAGE_FILE_MACHINE_SH3=0x01a2;
    public static final short   IMAGE_FILE_MACHINE_SH3DSP=0x01a3;
    public static final short   IMAGE_FILE_MACHINE_SH4=0x01a6;
    public static final short   IMAGE_FILE_MACHINE_SH5=0x01a8;
    public static final short   IMAGE_FILE_MACHINE_THUMB=0x01c2;
    public static final short   IMAGE_FILE_MACHINE_WCEMIPSV2=0x0169;

    private short    _machine;
    public short getMachine ()
    {
        return _machine;
    }

    public void setMachine (short machine)
    {
        _machine = machine;
    }

    private int    _numSections;
    public int getNumSections ()
    {
        return _numSections;
    }

    public void setNumSections (int numSections)
    {
        _numSections = numSections;
    }

    private long    _timeDateStamp;
    public long getTimeDateStamp ()
    {
        return _timeDateStamp;
    }

    public void setTimeDateStamp (long timeDateStamp)
    {
        _timeDateStamp = timeDateStamp;
    }

    private long    _symTableOffset;
    public long getSymTableOffset ()
    {
        return _symTableOffset;
    }

    public void setSymTableOffset (long symTableOffset)
    {
        _symTableOffset = symTableOffset;
    }

    private int    _numSymbols;
    public int getNumSymbols ()
    {
        return _numSymbols;
    }

    public void setNumSymbols (int numSymbols)
    {
        _numSymbols = numSymbols;
    }

    private int    _optHeaderSize;
    public int getOptHeaderSize ()
    {
        return _optHeaderSize;
    }

    public void setOptHeaderSize (int optHeaderSize)
    {
        _optHeaderSize = optHeaderSize;
    }

    public static final short  IMAGE_FILE_RELOCS_STRIPPED=0x0001;
    public static final short  IMAGE_FILE_EXECUTABLE_IMAGE=0x0002;
    public static final short  IMAGE_FILE_LINE_NUMS_STRIPPED=0x0004;
    public static final short  IMAGE_FILE_LOCAL_SYMS_STRIPPED=0x0008;
    public static final short  IMAGE_FILE_AGGRESSIVE_WS_TRIM=0x0010;
    public static final short  IMAGE_FILE_LARGE_ADDRESS_AWARE=0x0020;
    public static final short  IMAGE_FILE_RESERVED=0x0040;
    public static final short  IMAGE_FILE_BYTES_REVERSED_LO=0x0080;
    public static final short  IMAGE_FILE_32BIT_MACHINE=0x0100;
    public static final short  IMAGE_FILE_DEBUG_STRIPPED=0x0200;
    public static final short  IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP=0x0400;
    public static final short  IMAGE_FILE_NET_RUN_FROM_SWAP=0x0800;
    public static final short  IMAGE_FILE_SYSTEM=0x1000;
    public static final short  IMAGE_FILE_DLL=0x2000;
    public static final short  IMAGE_FILE_UP_SYSTEM_ONLY=0x4000;
    public static final short  IMAGE_FILE_BYTES_REVERSED_HI=(short) 0x8000;

    private short    _characteristics;
    public short getCharacteristics ()
    {
        return _characteristics;
    }

    public void setCharacteristics (short characteristics)
    {
        _characteristics = characteristics;
    }

    public void clear ()
    {
        setMachine((short) 0);
        setCharacteristics((short) 0);
        setNumSections(0);
        setNumSymbols(0);
        setOptHeaderSize(0);
        setSymTableOffset(0L);
        setTimeDateStamp(0L);
    }
    /*
     * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
     */
    @Override
    public COFFFileHeader read (InputStream in) throws IOException
    {
        setMachine(DataFormatConverter.readSignedInt16(in));
        setNumSections(DataFormatConverter.readUnsignedInt16(in));
        setTimeDateStamp(DataFormatConverter.readUnsignedInt32(in));
        setSymTableOffset(DataFormatConverter.readUnsignedInt32(in));
        // TODO change this if >2GB symbols possible
        setNumSymbols(DataFormatConverter.readSignedInt32(in));
        setOptHeaderSize(DataFormatConverter.readUnsignedInt16(in));
        setCharacteristics(DataFormatConverter.readSignedInt16(in));

        return this;
    }

    public COFFFileHeader (InputStream in) throws IOException
    {
        final Object    o=read(in);
        if (o != this)
            throw new StreamCorruptedException("Mismatched read data");
    }
    /*
     * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
     */
    @Override
    public void write (OutputStream out) throws IOException
    {
        DataFormatConverter.writeSignedInt16(out, getMachine());
        DataFormatConverter.writeUnsignedInt16(out, getNumSections());
        DataFormatConverter.writeUnsignedInt32(out, getTimeDateStamp());
        DataFormatConverter.writeUnsignedInt32(out, getSymTableOffset());
        DataFormatConverter.writeSignedInt32(out, getNumSymbols());
        DataFormatConverter.writeUnsignedInt16(out, getOptHeaderSize());
        DataFormatConverter.writeSignedInt16(out, getCharacteristics());
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    public COFFFileHeader clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if (!(obj instanceof COFFFileHeader))
            return  false;
        if (this == obj)
            return true;

        final COFFFileHeader    h=(COFFFileHeader) obj;
        return (h.getMachine() == getMachine())
            && (h.getCharacteristics() == getCharacteristics())
            && (h.getNumSections() == getNumSections())
            && (h.getNumSymbols() == getNumSymbols())
            && (h.getOptHeaderSize() == getOptHeaderSize())
            && (h.getSymTableOffset() == getSymTableOffset())
            && (h.getTimeDateStamp() == getTimeDateStamp())
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return getMachine()
             + getCharacteristics()
             + getNumSections()
             + getNumSymbols()
             + getOptHeaderSize()
             + NumberTables.getLongValueHashCode(getSymTableOffset())
             + NumberTables.getLongValueHashCode(getTimeDateStamp())
             ;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        final long            tsVal=getTimeDateStamp();
        final DateFormat    dtf=(0L == tsVal) ? null : DateFormat.getDateTimeInstance();
        final String        ct;
        if (dtf != null)
        {
            synchronized(dtf)
            {
                ct = dtf.format(new Date(DataFormatConverter.toJVMTimestamp(tsVal)));
            }
        }
        else
            ct = String.valueOf(tsVal);

        final short   typeValue=getMachine(), charsMask=getCharacteristics();
        return "machine=0x" + Hex.toString(typeValue, true) + "[" + COFFMachineType.fromTypeValue(typeValue) + "]"
            + "/chars=0x" + Hex.toString(charsMask, true) + ": " + COFFCharacteristic.fromCharacteristics(charsMask)
            + "/#sections=" + getNumSections()
            + "/#symbols=" + getNumSymbols()
            + "/opthdr-size=" + getOptHeaderSize()
            + "/symtbl-offset=" + getSymTableOffset()
            + "/timestamp=" + ct
            ;
    }
}

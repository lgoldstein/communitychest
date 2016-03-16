/*
 *
 */
package net.community.chest.win32.core.format.pe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 16, 2013 3:33:41 PM
 */
public class DosHeaderStub
                implements ElementEncoder<DosHeaderStub>,
                           Serializable {
    private static final long serialVersionUID = -7771774121940349794L;
    private byte[]  e_magic;
    private int  e_cblp;       /* 02: Bytes on last page of file */
    private int  e_cp;         /* 04: Pages in file */
    private int  e_crlc;       /* 06: Relocations */
    private int  e_cparhdr;    /* 08: Size of header in paragraphs */
    private int  e_minalloc;   /* 0a: Minimum extra paragraphs needed */
    private int  e_maxalloc;   /* 0c: Maximum extra paragraphs needed */
    private int  e_ss;         /* 0e: Initial (relative) SS value */
    private int  e_sp;         /* 10: Initial SP value */
    private int  e_csum;       /* 12: Checksum */
    private int  e_ip;         /* 14: Initial IP value */
    private int  e_cs;         /* 16: Initial (relative) CS value */
    private int  e_lfarlc;     /* 18: File address of relocation table */
    private int  e_ovno;       /* 1a: Overlay number */
    public static final int RESERVED_1_WORDS=4;
    private int  e_res1[]=new int[RESERVED_1_WORDS];     /* 1c: Reserved words */
    private int  e_oemid;      /* 24: OEM identifier (for e_oeminfo) */
    private int  e_oeminfo;    /* 26: OEM information; e_oemid specific */
    public static final int RESERVED_2_WORDS=10;
    private int  e_res2[]=new int[RESERVED_2_WORDS];   /* 28: Reserved words */
    public static final long    HEADER_OFFSET_VALUE=0x003C;
    private long  e_lfanew;     /* 3c: Offset to extended header */

    public DosHeaderStub() {
        super();
    }

    public DosHeaderStub(InputStream in) throws IOException {
        Object  result=read(in);
        if (result != this) {
            throw new StreamCorruptedException("Mismatched re-constructed instance");
        }
    }

    public long getExtendedHeaderOffset() {
        return e_lfanew;
    }

    public void setExtendedHeaderOffset(long v) {
        e_lfanew = v;
    }

    @Override
    public DosHeaderStub read (InputStream in) throws IOException
    {
        e_magic = readStubMagic(in);
        e_cblp = DataFormatConverter.readUnsignedInt16(in);
        e_cp = DataFormatConverter.readUnsignedInt16(in);
        e_crlc = DataFormatConverter.readUnsignedInt16(in);
        e_cparhdr = DataFormatConverter.readUnsignedInt16(in);
        e_minalloc = DataFormatConverter.readUnsignedInt16(in);
        e_maxalloc = DataFormatConverter.readUnsignedInt16(in);
        e_ss = DataFormatConverter.readUnsignedInt16(in);
        e_sp = DataFormatConverter.readUnsignedInt16(in);
        e_csum = DataFormatConverter.readUnsignedInt16(in);
        e_ip = DataFormatConverter.readUnsignedInt16(in);
        e_cs = DataFormatConverter.readUnsignedInt16(in);
        e_lfarlc = DataFormatConverter.readUnsignedInt16(in);
        e_ovno = DataFormatConverter.readUnsignedInt16(in);
        for (int index=0; index < RESERVED_1_WORDS; index++) {
            e_res1[index] = DataFormatConverter.readUnsignedInt16(in);
        }
        e_oemid = DataFormatConverter.readUnsignedInt16(in);
        e_oeminfo = DataFormatConverter.readUnsignedInt16(in);
        for (int index=0; index < RESERVED_2_WORDS; index++) {
            e_res2[index] = DataFormatConverter.readUnsignedInt16(in);
        }

        setExtendedHeaderOffset(DataFormatConverter.readUnsignedInt32(in));
        return this;
    }

    @Override
    public void write (OutputStream out) throws IOException
    {
        throw new StreamCorruptedException("N/A TODO");
    }

    public static final byte[]  MZ_SIGNATURE={ 'M', 'Z' };
    public static final byte[]  readStubMagic(final InputStream in) throws IOException {
        final byte[]    magicVal=new byte[MZ_SIGNATURE.length];
        final int       readLen=in.read(magicVal);
        if (readLen != MZ_SIGNATURE.length)
            throw new IOException("Failed to read PE signature - expected=" + MZ_SIGNATURE.length + "/got=" + readLen + " bytes");

        for (int    sIndex=0; sIndex < MZ_SIGNATURE.length; sIndex++)
        {
            if (magicVal[sIndex] != MZ_SIGNATURE[sIndex])
                throw new StreamCorruptedException("Bad magic value at index=" + sIndex + " - expected=" + MZ_SIGNATURE[sIndex] + "/got=" + magicVal[sIndex]);
        }

        return magicVal;
    }
}

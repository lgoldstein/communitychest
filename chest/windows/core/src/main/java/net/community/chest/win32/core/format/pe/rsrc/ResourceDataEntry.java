/*
 *
 */
package net.community.chest.win32.core.format.pe.rsrc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import net.community.chest.io.encode.ElementEncoder;
import net.community.chest.io.encode.hex.Hex;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.win32.core.DataFormatConverter;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 10, 2013 11:48:53 AM
 */
public class ResourceDataEntry
        implements Serializable,
                   PubliclyCloneable<ResourceDataEntry>,
                   ElementEncoder<ResourceDataEntry> {
    private static final long serialVersionUID = -6698112095383674104L;

    public ResourceDataEntry() {
        super();
    }

    private long    _dataRVA;
    public long getDataRVA ()
    {
        return _dataRVA;
    }

    public void setDataRVA (long dataRVA)
    {
        _dataRVA = dataRVA;
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

    private int    _codePage;
    public int getCodePage ()
    {
        return _codePage;
    }

    public void setCodePage (int codePage)
    {
        _codePage = codePage;
    }

    private int _reserved;
    public int getReserved ()
    {
        return _reserved;
    }

    public void setReserved (int reserved)
    {
        _reserved = reserved;
    }

    @Override
    public ResourceDataEntry read (InputStream in) throws IOException
    {
        setDataRVA(DataFormatConverter.readUnsignedInt32(in));
        setSize(DataFormatConverter.readUnsignedInt32(in));
        setCodePage(DataFormatConverter.readSignedInt32(in));
        setReserved(DataFormatConverter.readSignedInt32(in));
        return this;
    }

    @Override
    public void write (OutputStream out) throws IOException
    {
        DataFormatConverter.writeUnsignedInt32(out, getDataRVA());
        DataFormatConverter.writeUnsignedInt32(out, getSize());
        DataFormatConverter.writeSignedInt32(out, getCodePage());
        DataFormatConverter.writeSignedInt32(out, getReserved());
    }

    @Override
    public int hashCode ()
    {
        return (int) getDataRVA()
             + (int) getSize()
             + getCodePage()
             + getReserved()
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

        ResourceDataEntry   other=(ResourceDataEntry) obj;
        return (getDataRVA() == other.getDataRVA())
            && (getSize() == other.getSize())
            && (getCodePage() == other.getCodePage())
            && (getReserved() == other.getReserved())
            ;
    }

    @Override
    public ResourceDataEntry clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    @Override
    public String toString ()
    {
        return "dataRVA=" + Hex.toString((int) getDataRVA(), true)
             + "/size=" + getSize()
             + "/codePage=" + getCodePage()
             + "/reserved=" + Hex.toString(getReserved(), true)
             ;
    }
}

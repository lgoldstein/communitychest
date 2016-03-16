/*
 *
 */
package net.community.chest.awt.image;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import net.community.chest.lang.PubliclyCloneable;

public class BitmapHeader implements PubliclyCloneable<BitmapHeader>, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 7192902705442092702L;
    //Bitmap file header, 14 bytes
    public short signature;//always "BM"
    public int   fileSize; //Total size of file in bytes
    public short reserved1;
    public short reserved2;
    public int   dataOffSet;
    //Bitmap info header, 40 bytes
    public int   infoHeaderLen; //40 bytes
    public int   imageWidth;
    public int   imageHeight;
    public short planes; //Always = 1
    public short bitCount;
    /**
     * 1 - Colors Used = 2 (Palette)
     * 4 - Colors Used = 16 (Palette)
     * 8 - Colors Used = 256 (Palette)
     * 16 - Colors Used = 0 (RGB)
     * 24 - Colors Used = 0 (RGB)
     * 32 - Colors Used = 0 (RGB)
     */
    public int   compression;
    /**
     * Type of compression:
     * 0 = BI_RGB: No compression
     * 1 = BI_RLE8: 8 bit RLE Compression (8 bit only)
     * 2 = BI_RLE4: 4 bit RLE Compression (4 bit only)
     * 3 = BI_BITFIELDS: No compression (16 & 32 bit only)
     */
    public int   imageSize;
    /**
     * Size of compressed image, can be 0 if Compression = 0
     * In the case that the 'imageSize' field has been set
     *    to zero, the size of the uncompressed data can be
     *    calculated using the following formula:
     * imageSize = int(((imageWidth * planes * bitCount) + 31) / 32) * 4 * imageHeight
     * where int(x) returns the integral part of x.
     */
    public int   xResolution;
    public int   yResolution;
    public int   colorsUsed;
    /**
     * The number of color indexes in the color table used
     * by the bitmap. If set to zero, the bitmap uses maximum
     * number of colors specified by the bitCount.
     */
    public int   colorsImportant; //Number of important colors (0 = all)

    public BitmapHeader ()
    {
        super();
    }

    public void readHeader (final InputStream is) throws IOException
    {
        final byte[]    bhdr=new byte[54];
        final int        numRead=is.read(bhdr, 0, bhdr.length);
        if (numRead != bhdr.length)
            throw new IOException("readHeader - mismatched read size: req=" + bhdr.length + "/got=" + numRead);

        int nindex = 0;
        signature = (short)((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8));
        fileSize = ((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8)|
                ((bhdr[nindex++]&0xff)<<16)|((bhdr[nindex++]&0xff)<<24));
        reserved1 = (short)((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8));
        reserved2 = (short)((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8));
        dataOffSet = ((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8)|
                ((bhdr[nindex++]&0xff)<<16)|((bhdr[nindex++]&0xff)<<24));
        infoHeaderLen = ((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8)|
                ((bhdr[nindex++]&0xff)<<16)|((bhdr[nindex++]&0xff)<<24));
        imageWidth = ((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8)|
                ((bhdr[nindex++]&0xff)<<16)|((bhdr[nindex++]&0xff)<<24));
        imageHeight =((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8)|
                ((bhdr[nindex++]&0xff)<<16)|((bhdr[nindex++]&0xff)<<24));
        planes = (short)((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8));
        bitCount = (short)((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8));
        compression = ((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8)|
                ((bhdr[nindex++]&0xff)<<16)|((bhdr[nindex++]&0xff)<<24));
        imageSize = ((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8)|
                ((bhdr[nindex++]&0xff)<<16)|((bhdr[nindex++]&0xff)<<24));
        xResolution = ((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8)|
                ((bhdr[nindex++]&0xff)<<16)|((bhdr[nindex++]&0xff)<<24));
        yResolution = ((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8)|
                ((bhdr[nindex++]&0xff)<<16)|((bhdr[nindex++]&0xff)<<24));
        colorsUsed = ((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8)|
                ((bhdr[nindex++]&0xff)<<16)|((bhdr[nindex++]&0xff)<<24));
        colorsImportant = ((bhdr[nindex++]&0xff)|((bhdr[nindex++]&0xff)<<8)|
                ((bhdr[nindex++]&0xff)<<16)|((bhdr[nindex++]&0xff)<<24));
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    public BitmapHeader clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
}

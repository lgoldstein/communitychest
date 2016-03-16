/*
 *
 */
package net.community.chest.awt.image;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.community.chest.io.FileUtil;
import net.community.chest.io.output.AutoGrowArrayOutputStream;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Reads a Windows(tm) &quot;.ico&quot; file and converts it into {@link BufferedImage}-s
 * that can be accessed and used as Java icons</P>
 * @author Lyor G. - based on code from <A HREF="http://www.informit.com/articles/article.aspx?p=1186882&seqNum=3">Jeff Friesen</A>
 * @since Nov 13, 2008 11:11:55 AM
 */
public final class ICOReader extends AbstractImageReader {
    private ICOReader ()
    {
        // no instance
    }

    // Convert byte to unsigned byte.
    private static final int ubyte (final byte b)
    {
        return (b < 0) ? 256+b : b;
    }

    private static final int calcScanlineBytes (int width, int bitCount)
    {
        // Calculate minimum number of double-words required to store width
        // pixels where each pixel occupies bitCount bits. XOR and AND bitmaps
        // are stored such that each scan line is aligned on a double-word
        // boundary.
        return (((width*bitCount)+31)/32)*4;
    }

    public final static int     FDE_OFFSET=6, // first directory entry offset
                                DE_LENGTH=16, // directory entry length
                                BMIH_LENGTH=40; // BITMAPINFOHEADER length
    private static final int[]    MASKS={ 128, 64, 32, 16, 8, 4, 2, 1 };

    public static final List<Image> parseICOImages (final byte[] icoimage, final int imgLen) throws IOException
    {
        // Check resource type field.
        if (imgLen <= 3)
            throw new IOException("Not enough data to determine if ICO resource");
        if ((icoimage[2] != 1) || (icoimage[3] != 0))
            throw new StreamCorruptedException("Not an ICO resource: " + icoimage[2] + "/" + icoimage[3]);

        if (imgLen <= 5)
            throw new IOException("Not enough data to determine # images");

        int    numImages=ubyte(icoimage[5]);
        numImages <<= 8;
        numImages |= (icoimage[4] & 0x00FF);

        List<Image>    il=null;
        for (int i = 0; i < numImages; i++)
        {
            final int    baseOffset=FDE_OFFSET+i*DE_LENGTH;
            if ((baseOffset + 15) >= imgLen)
                throw new IOException("Not enough data to read image #" + i);

            int    colorCount=ubyte(icoimage[baseOffset+2]),
                width=ubyte(icoimage[baseOffset]),
                height=ubyte(icoimage[baseOffset+1]);

            int bytesInRes=ubyte(icoimage[baseOffset+11]);
            bytesInRes <<= 8;
            bytesInRes |= ubyte(icoimage[baseOffset+10]);
            bytesInRes <<= 8;
            bytesInRes |= ubyte(icoimage[baseOffset+9]);
            bytesInRes <<= 8;
            bytesInRes |= ubyte(icoimage[baseOffset+8]);

            int imageOffset=ubyte(icoimage[baseOffset+15]);
            imageOffset <<= 8;
            imageOffset |= ubyte(icoimage[baseOffset+14]);
            imageOffset <<= 8;
            imageOffset |= ubyte(icoimage[baseOffset+13]);
            imageOffset <<= 8;
            imageOffset |= ubyte(icoimage[baseOffset+12]);

            final BufferedImage    bi;
            if ((imageOffset + 3) >= imgLen)
                throw new IOException("Not enough data to use image offset for BITMAPINFOHEADER detection");

            // check if BITMAPINFOHEADER detected
            if ((icoimage[imageOffset] == 40)
             && (icoimage[imageOffset+1] == 0)
             && (icoimage[imageOffset+2] == 0)
             && (icoimage[imageOffset+3] == 0))
            {
                if ((imageOffset + 15) >= imgLen)
                    throw new IOException("Not enough data to decode BITMAPINFOHEADER");

                int _width=ubyte(icoimage[imageOffset+7]);
                _width <<= 8;
                _width |= ubyte(icoimage[imageOffset+6]);
                _width <<= 8;
                _width |= ubyte(icoimage[imageOffset+5]);
                _width <<= 8;
                _width |= ubyte (icoimage[imageOffset+4]);

                // If width is 0 (for 256 pixels or higher), _width contains actual width.
                if (width == 0)
                    width = _width;

                int _height=ubyte(icoimage[imageOffset+11]);
                _height <<= 8;
                _height |= ubyte(icoimage[imageOffset+10]);
                _height <<= 8;
                _height |= ubyte(icoimage[imageOffset+9]);
                _height <<= 8;
                _height |= ubyte(icoimage[imageOffset+8]);

                // If height is 0 (for 256 pixels or higher), _height contains actual height times 2.
                if (height == 0)
                    height = _height >> 1; // Divide by 2.

                int planes=ubyte(icoimage[imageOffset+13]);
                planes <<= 8;
                planes |= ubyte(icoimage[imageOffset+12]);

                int bitCount=ubyte(icoimage[imageOffset+15]);
                bitCount <<= 8;
                bitCount |= ubyte(icoimage[imageOffset+14]);

                // If colorCount [i] is 0, the number of colors is determined
                // from the planes and bitCount values. For example, the number
                // of colors is 256 when planes is 1 and bitCount is 8. Leave
                // colorCount [i] set to 0 when planes is 1 and bitCount is 32.
                if (colorCount == 0)
                {
                    if (planes == 1)
                    {
                        if (bitCount == 1)
                            colorCount = 2;
                        else if (bitCount == 4)
                            colorCount = 16;
                        else if (bitCount == 8)
                            colorCount = 256;
                        else if (bitCount != 32)
                            colorCount = (int) Math.pow (2, bitCount);
                    }
                    else
                        colorCount = (int) Math.pow (2, bitCount*planes);
                }

                // Parse image to image buffer.
                bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                int colorTableOffset=imageOffset+BMIH_LENGTH;
                if (colorCount == 0)
                {
                    final int scanlineBytes=calcScanlineBytes(width, 32);

                    for (int row = 0; row < height; row++)
                    {
                        for (int col = 0; col < width; col++)
                        {
                            final int    rgbOffset=colorTableOffset+row*scanlineBytes+col*4;

                            int rgb=ubyte(icoimage[rgbOffset + 3]);
                            rgb <<= 8;
                            rgb |= ubyte(icoimage[rgbOffset + 2]);
                            rgb <<= 8;
                            rgb |= ubyte(icoimage[rgbOffset + 1]);
                            rgb <<= 8;
                            rgb |= ubyte(icoimage[rgbOffset]);

                            bi.setRGB(col, height-1-row, rgb);
                        }
                    }
                }
                else if (colorCount <= 8)
                {
                    final int    bccCount;
                    switch(colorCount)
                    {
                        case 2    : bccCount = 1; break;
                        case 4    : bccCount = 2; break;
                        case 8    : bccCount = 3;    break;
                        default    : throw new IllegalStateException("Illegal bit count for colors=" + colorCount);
                    }

                    final int     xorImageOffset=colorTableOffset+colorCount*4,
                                scanlineBytes=calcScanlineBytes(width, bccCount),
                                andImageOffset=xorImageOffset+scanlineBytes*height;

                    for (int row = 0; row < height; row++)
                    {
                        final int    rowBytesOffset=row * scanlineBytes;
                        for (int col = 0; col < width; col++)
                        {
                            final int    idxValue=ubyte(icoimage[xorImageOffset+ rowBytesOffset + col/8]);
                            final int index;
                            if ((idxValue & MASKS[col%8]) != 0)
                                index = 1;
                            else
                                index = 0;

                            int rgb=0;
                            {
                                final int    rgbBaseOffset=colorTableOffset+index*4;
                                rgb |= ubyte(icoimage[rgbBaseOffset + 2]);
                                rgb <<= 8;
                                rgb |= ubyte(icoimage[rgbBaseOffset + 1]);
                                rgb <<= 8;
                                rgb |= ubyte(icoimage[rgbBaseOffset]);
                            }

                            final int    rgbMaskValue=ubyte(icoimage[andImageOffset + rowBytesOffset + col/8]);
                            if ((rgbMaskValue & MASKS[col%8]) != 0)
                                bi.setRGB(col, height-1-row, rgb);
                            else
                                bi.setRGB(col, height-1-row, 0xff000000 | rgb);
                        }
                    }
                }
                else if (colorCount == 16)
                {
                    final int    xorImageOffset=colorTableOffset+colorCount*4,
                                   scanlineBytes=calcScanlineBytes(width, 4),
                                   andImageOffset=xorImageOffset+scanlineBytes*height,
                                   rowWidth=calcScanlineBytes(width, 1);

                    for (int row = 0; row < height; row++)
                    {
                        final int    rowBytesOffset=row * scanlineBytes,
                                    rgbBytesOffset=row * rowWidth;

                        for (int col = 0; col < width; col++)
                        {
                            final int index;
                            if ((col & 1) == 0) // even
                                index = ubyte(icoimage[xorImageOffset+rowBytesOffset+col/2]) >> 4;
                            else
                                index = ubyte(icoimage[xorImageOffset+rowBytesOffset+col/2]) & 0x0F;

                            int rgb=0;
                            {
                                final int    rgbBaseOffset=colorTableOffset+index*4;
                                rgb |= ubyte(icoimage[rgbBaseOffset + 2]);
                                rgb <<= 8;
                                rgb |= ubyte(icoimage[rgbBaseOffset +1 ]);
                                rgb <<= 8;
                                rgb |= ubyte(icoimage[rgbBaseOffset + 4]);
                            }

                            final int    rgbMaskValue=ubyte(icoimage[andImageOffset+rgbBytesOffset+ col/8]);
                            if ((rgbMaskValue & MASKS[col%8]) != 0)
                                bi.setRGB (col, height-1-row, rgb);
                            else
                                bi.setRGB (col, height-1-row, 0xff000000 | rgb);
                        }
                    }
                }
                else if (colorCount == 256)
                {
                    final int    xorImageOffset=colorTableOffset+colorCount*4,
                                  scanlineBytes=calcScanlineBytes(width, 8),
                                  andImageOffset=xorImageOffset+scanlineBytes*height,
                                  rowWidth=calcScanlineBytes(width, 1);

                    for (int row = 0; row < height; row++)
                    {
                        final int    rowBytesOffset=row * scanlineBytes,
                                    rgbBytesOffset=row * rowWidth;

                        for (int col = 0; col < width; col++)
                        {
                            final int index=ubyte(icoimage[xorImageOffset+rowBytesOffset+col]);

                            int rgb=0;
                            {
                                final int    rgbBaseOffset=colorTableOffset+index*4;
                                rgb |= ubyte(icoimage[rgbBaseOffset + 2]);
                                rgb <<= 8;
                                rgb |= ubyte(icoimage[rgbBaseOffset + 1]);
                                rgb <<= 8;
                                rgb |= ubyte(icoimage[rgbBaseOffset]);
                            }

                            final int    rgbMaskIndex=ubyte(icoimage[andImageOffset+rgbBytesOffset + col/8]);
                            if ((rgbMaskIndex & MASKS[col%8]) != 0)
                                bi.setRGB(col, height-1-row, rgb);
                            else
                                bi.setRGB(col, height-1-row, 0xff000000 | rgb);
                        }
                    }
                }
                else
                {
                    throw new IOException("Unknown BITMAPINFOHEADER color count: " + colorCount);
                }
            }
            else
            {
                   if ((imageOffset + 7) >= imgLen)
                    throw new IOException("Not enough data to use image offset for PNG detection");

                   final int    pngMask=ubyte(icoimage[imageOffset]);
                   if ((pngMask == 0x89)
                    && (icoimage[imageOffset+1] == 0x50)
                    && (icoimage[imageOffset+2] == 0x4e)
                    && (icoimage[imageOffset+3] == 0x47)
                    && (icoimage[imageOffset+4] == 0x0d)
                    && (icoimage[imageOffset+5] == 0x0a)
                    && (icoimage[imageOffset+6] == 0x1a)
                    && (icoimage[imageOffset+7] == 0x0a))
               {
                   // PNG detected
                   final ByteArrayInputStream bais=
                       new ByteArrayInputStream(icoimage, imageOffset, bytesInRes);
                   bi = ImageIO.read(bais);
               }
               else
                   throw new StreamCorruptedException("BITMAPINFOHEADER or PNG expected");
            }

            if (null == il)
                il = new ArrayList<Image>(numImages);
            il.add(bi);
        }

        return il;
    }

    public static final List<Image> parseICOImages (final Map.Entry<byte[],Integer> dp) throws IOException
    {
        return (null == dp) ? null : parseICOImages(dp.getKey(), dp.getValue().intValue());
    }

    public static final List<Image> parseICOImages (File f) throws IOException
    {
        return parseICOImages(AutoGrowArrayOutputStream.readAllData(f));
    }

    public static final List<Image> parseICOImages (String filePath) throws IOException
    {
        return parseICOImages(AutoGrowArrayOutputStream.readAllData(filePath));
    }

    public static final List<Image> parseICOImages (InputStream in) throws IOException
    {
        return parseICOImages(AutoGrowArrayOutputStream.readAllData(in));
    }

    public static final List<Image> parseICOImages (URL url) throws IOException
    {
        return parseICOImages(AutoGrowArrayOutputStream.readAllData(url));
    }

    public static final String    ICO_SUFFIX="ico";
    public static final boolean isIconFile (final String filePath)
    {
        return FileUtil.isMatchingFileSuffix(filePath, ICO_SUFFIX);
    }

    public static final boolean isIconFile (final URL fileURL)
    {
        return (null == fileURL) ? false : isIconFile(fileURL.getPath());
    }
    /*
     * @see net.community.chest.ui.helpers.images.AbstractImageReader#isMatchingFile(java.lang.String)
     */
    @Override
    public boolean isMatchingFile (String filePath)
    {
        return isIconFile(filePath);
    }
    /*
     * @see net.community.chest.ui.helpers.images.AbstractImageReader#readImages(java.io.InputStream)
     */
    @Override
    public List<Image> readImages (InputStream in) throws IOException
    {
        return parseICOImages(in);
    }
    /*
     * @see net.community.chest.ui.helpers.images.AbstractImageReader#readImages(byte[], int, int)
     */
    @Override
    public List<Image> readImages (byte[] data, int offset, int len) throws IOException
    {
        if (0 == offset)
            return parseICOImages(data, len);

        return super.readImages(data, offset, len);
    }
    /*
     * @see net.community.chest.ui.helpers.images.AbstractImageReader#readImages(java.io.File)
     */
    @Override
    public List<Image> readImages (File f) throws IOException
    {
        return parseICOImages(f);
    }
    /*
     * @see net.community.chest.ui.helpers.images.AbstractImageReader#readImages(java.lang.String)
     */
    @Override
    public List<Image> readImages (String f) throws IOException
    {
        return parseICOImages(f);
    }

    public static final ICOReader    DEFAULT=new ICOReader();
}

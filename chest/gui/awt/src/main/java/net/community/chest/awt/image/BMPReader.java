/*
 * 
 */
package net.community.chest.awt.image;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Reads a BMP file as a <code>byte[]</code> from which an {@link Image}
 * can then be created base on code from
 * <A HREF="http://www.geocities.com/yuwen_66/">Wen's Java Image Web</A></P>
 * @author Lyor G.
 * @since Dec 2, 2008 2:15:43 PM
 */
public class BMPReader extends AbstractImageReader {
	//biCompression types
	//	private static final int BI_RGB = 0;//No compression
	private static final int BI_RLE8 = 1;//8 bit RLE Compression (8 bit only)
	private static final int BI_RLE4 = 2;//4 bit RLE Compression (4 bit only)
	//    private static final int BI_BITFIELDS = 3;//No compression (16 & 32 bit only) 

	private int bytePerScanLine;
	private BitmapHeader bitmapHeader;

	//Get the color depth of the image to determine whether a colorPalette exists
	private int bitsPerPixel;
	public int getColorDepth()
	{
		return bitsPerPixel;
	}
	//Returns a colorPalette for an indexed-color image
	private int[] colorPalette;
	public int[] getColorPalette()
	{
		return colorPalette; // call this method if getImageColorDepth() <= 8
	}
	// Is it an indexedColor image
	private boolean indexedColor;
	public boolean isIndexedColor()
	{
		return indexedColor;
	}

	private int width;
	public int getImageWidth ()
	{
		return width;
	}

	private int height;
	public int getImageHeight ()
	{
		return height;
	}
	//Returns the Dimension of the image
	public Dimension getImageSize()
	{
		return new Dimension(getImageWidth(), getImageHeight());
	}

	// Data field, place holder, the real values are to be set by a subclass
	private int pix[];	
	//Returns an integer array representation of the image 
	//in alpha_RGB sequence
	public int[] getImageData()
	{
		return pix;// check about null pointer after method call ... 
	}
	/* The Toolkit.getDefaultToolkit().createImage(ImageProducer) method is
	 * somewhat slow in creating an Image when called in a non-gui class, a 
	 * subclass of JFrame, for example, could do much faster!
	 */
	//Returns the decoded image as a java Image object 
	//check about null pointer after method call!
	public Image getImage (Component c)
	{
		final int[]	data=getImageData();
		if (data != null)
		{
			final ImageProducer	src=new MemoryImageSource(getImageWidth(), getImageHeight(), data, 0, getImageWidth());
			if (null == c)
			{
				final Toolkit	t=Toolkit.getDefaultToolkit();
				return t.createImage(src);
			}
			else
				return c.createImage(src);
		}

		return null; 
	}

	public Image getImage ()
	{
		return getImage(null);
	}

	private void readPalette (final InputStream is) throws IOException
	{
		final int numOfColors=
			(bitmapHeader.colorsUsed == 0)?(1<<bitsPerPixel):bitmapHeader.colorsUsed;
		colorPalette = new int[numOfColors];	
		indexedColor = true;

		final byte[] 	brgb=new byte[numOfColors*4];
		final int		numRead=is.read(brgb,0,brgb.length);
		if (numRead != brgb.length)
			throw new IOException("readHeader - mismatched read size: req=" + brgb.length + "/got=" + numRead);

		for(int i = 0, index = 0,nindex = 0; i < numOfColors; i++)
		{
			colorPalette[index++] = ((0xff<<24)|(brgb[nindex]&0xff)|((brgb[nindex+1]&0xff)<<8)|((brgb[nindex+2]&0xff)<<16));
			nindex += 4;
		}
		//There may be some extra bits between color palette and actual image data
		final long	numToSkip=bitmapHeader.dataOffSet - numOfColors*4 - 54,
					numSkipped=is.skip(numToSkip);
		if (numToSkip != numSkipped)
			throw new IOException("readHeader - mismatched skip size: requested=" + numToSkip + "/skipped=" + numSkipped);
	}

	private void unpackTrueColorBitmapFile (final InputStream is) throws IOException
	{
		final long	numToSkip=bitmapHeader.dataOffSet - 54,
					numSkipped=is.skip(numToSkip);
		if (numToSkip != numSkipped)
			throw new IOException("unpackTrueColorBitmapFile - mismatched skip size: requested=" + numToSkip + "/skipped=" + numSkipped);

		final byte[] brgb=new byte[bytePerScanLine];
		for(int i = 1, index = 0,nindex = 0, npad = bytePerScanLine-3*width; i <= height; i++)
		{
			final int	numRead=is.read(brgb,0,brgb.length);
			if (numRead != brgb.length)
				throw new IOException("unpackTrueColorBitmapFile - mismatched read size: req=" + brgb.length + "/got=" + numRead);

			index = width*(height-i);
			nindex = 0;
			for(int j = 0; j < width; j++)
			{
				pix[index++] = ((0xff<<24)|(brgb[nindex++]&0xff)|((brgb[nindex++]&0xff)<<8)|((brgb[nindex++]&0xff)<<16));
			}
			nindex += npad;	
		}
	}

	private void unpack32bitTrueColorBitmapFile (final InputStream is) throws IOException
	{
		final long	numToSkip=bitmapHeader.dataOffSet - 54,
					numSkipped=is.skip(numToSkip);
		if (numToSkip != numSkipped)
			throw new IOException("unpack32bitTrueColorBitmapFile - mismatched skip size: requested=" + numToSkip + "/skipped=" + numSkipped);

		final byte[] brgb=new byte[bytePerScanLine];
		for(int i = 1, index = 0,nindex = 0; i <= height; i++)
		{
			final int	numRead=is.read(brgb,0,brgb.length);
			if (numRead != brgb.length)
				throw new IOException("unpack32bitTrueColorBitmapFile - mismatched read size: req=" + brgb.length + "/got=" + numRead);

			index = width*(height-i);
			nindex = 0;
			for(int j = 0; j < width; j++)
			{
				pix[index++] = ((brgb[nindex++]&0xff)|((brgb[nindex++]&0xff)<<8)|((brgb[nindex++]&0xff)<<16)|(0xff<<24));
				nindex++;
			}
		}
	}

	private void unpack256ColorBitmapFile (final InputStream is) throws IOException
	{
		readPalette(is);

		final byte[] brgb=new byte[bytePerScanLine];
		for(int i = 1, index = 0, nindex = 0, npad = bytePerScanLine-width; i <= height; i++)
		{
			final int	numRead=is.read(brgb,0,brgb.length);
			if (numRead != brgb.length)
				throw new IOException("unpack256ColorBitmapFile - mismatched read size: req=" + brgb.length + "/got=" + numRead);

			index = width*(height-i);
			nindex = 0;
			for(int j = 0; j < width; j++)
			{
				pix[index++] = colorPalette[brgb[nindex++]&0xff];
			}
			nindex += npad;
		}
	}

	private void unpack16ColorBitmapFile(InputStream is) throws IOException
	{
		readPalette(is);

		int npad = (32-((width*4)%32))/8;
		if (npad == 4)
			npad = 0;

		final byte[]	brgb = new byte[bytePerScanLine];
		for(int i = 1, bit = 0, index = 0, nindex = 0; i <= height; i++)
		{     
			final int	numRead=is.read(brgb,0,brgb.length);
			if (numRead != brgb.length)
				throw new IOException("unpack16ColorBitmapFile - mismatched read size: req=" + brgb.length + "/got=" + numRead);

			index = width*(height-i);
			nindex = 0;
			for(int j = 0; j < width/2; j++)
			{
				bit = brgb[nindex++];
				pix[index++] = colorPalette[((bit>>>4)&0x0F)];
				pix[index++] = colorPalette[bit&(0x0F)];
			}
			if((width%2) != 0)
			{
				bit = brgb[nindex++];
				pix[index++] = colorPalette[((bit>>>4)&0x0F)];
			}
			nindex += npad;
		}
	}	

	private void unpack2ColorBitmapFile(InputStream is) throws IOException 
	{
		readPalette(is);


		int npad = (32-(width%32))/32;
		if (npad == 4)
			npad = 0;

		final byte[]	brgb=new byte[bytePerScanLine];
		for(int i = 1, bit = 0, index = 0, nindex = 0; i <= height; i++)
		{
			final int	numRead=is.read(brgb,0,brgb.length);
			if (numRead != brgb.length)
				throw new IOException("unpack2ColorBitmapFile - mismatched read size: req=" + brgb.length + "/got=" + numRead);

			index = width*(height-i);
			nindex = 0;
			for(int j = 0; j < width/8; j++)
			{
				bit = brgb[nindex++];
				for (int k = 0; k < 8; k++)
				{
					pix[index++] = colorPalette[((bit>>>(7-k))&0x01)];
				}
			}
			if((width%8) != 0)
			{
				bit = brgb[nindex++];
				for (int k = 0; k < width%8; k++)
				{
					pix[index++] = colorPalette[((bit>>>(7-k))&0x01)];
				}
			}
			nindex += npad;
		}
	}

	private void unpack256ColorCompressedBitmapFile (final InputStream is) throws IOException
	{
		readPalette(is);

		int rgb_table_index = 0, index = 0, nindex = 0;
		int len = 0, esc = 0, count = 0;
		int end_of_line = 0, end_of_bitmap = 1, delta = 2;
		int vert_offset = 0, horz_offset = 0, horz = 0, vert = height-1;

		boolean done_with_bitmap = false;

		final byte[] brgb=new byte[IOCopier.DEFAULT_COPY_SIZE];
		int readSize=is.read(brgb,0,brgb.length);

		index = width*vert+horz;

		do
		{
			if (nindex >= readSize)
			{
				//if ((is.read(brgb,0,brgb.length)) == -1) break;
				if ((readSize=is.read(brgb,0,brgb.length)) <= 0)
					throw new EOFException("unpack256ColorCompressedBitmapFile - no more data available");
				nindex = 0;
			}

			len = brgb[nindex++]&0xff;

			if (nindex >= readSize)
			{
				readSize = is.read(brgb,0,brgb.length);
				nindex = 0;
			}
			if(len == 0)
			{
				esc = (brgb[nindex++]&0xff);
				if (nindex >= readSize)
				{
					readSize = is.read(brgb,0,brgb.length);
					nindex = 0;
				}

				if(esc>2)
				{
					count = 0;
					for(int k = 1; k <= esc; k++)
					{
						pix[index++] = colorPalette[(brgb[nindex++]&0xff)];
						if (nindex >= readSize)
						{
							readSize = is.read(brgb,0,brgb.length);
							nindex = 0;
						}
						count++;
						horz++;
						if (horz >= width)
						{
							break;
						}
					}
					if ((count%2) != 0)
						nindex++;//each absolute run must be aligned on a word boundary!
				}
				if (esc == delta)
				{
					horz_offset = brgb[nindex++]&0xff;
					if (nindex >= readSize)
					{
						readSize = is.read(brgb,0,brgb.length);
						nindex = 0;
					}
					vert_offset = brgb[nindex++]&0xff;
					if (nindex >= readSize)
					{
						readSize = is.read(brgb,0,brgb.length);
						nindex = 0;
					}
					horz += horz_offset;//this is to be verified!
					vert -= vert_offset;//this is to be verified!
					index = width*vert+horz;
				}
				if (esc == end_of_line) 
				{
					vert--;
					horz = 0;
					index = width*vert+horz;
				}
				if (esc == end_of_bitmap)
					done_with_bitmap = true;
			}
			else  
			{
				rgb_table_index = brgb[nindex++]&0xff;
				if (nindex >= readSize)
				{
					readSize = is.read(brgb,0,brgb.length);
					nindex = 0;
				}

				for(int l = 0; l < len; l++)
				{
					pix[index++] = colorPalette[rgb_table_index];
					horz++;
					if (horz >= width)
						break;
				}
			}

			if(vert<0)
				done_with_bitmap = true;
		} while(!done_with_bitmap);
	}

	private void unpack16ColorCompressedBitmapFile (InputStream is) throws IOException
	{
		readPalette(is);

		int rgb_table_index = 0, nindex = 0, index = 0;
		int end_of_line = 0, end_of_bitmap = 1, delta = 2;
		int horz = 0, vert = height-1, horz_offset  =  0, vert_offset  =  0;
		int bit = 0, m1 = 0, m2 = 0, count = 0, counter = 0, len = 0, esc = 0;	

		boolean done_with_bitmap = false;

		final byte[] brgb=new byte[IOCopier.DEFAULT_COPY_SIZE];
		int readSize = is.read(brgb,0,brgb.length);

		index = width*vert+horz;

		do {
			if (nindex >= readSize)
			{
				readSize = is.read(brgb,0,brgb.length);
				nindex = 0;
			}

			len = brgb[nindex++]&0xff;

			if (nindex >= readSize)
			{
				readSize = is.read(brgb,0,brgb.length);
				nindex = 0;
			}
			if(len == 0)
			{
				esc = brgb[nindex++]&0xff;
				if (nindex >= readSize)
				{
					readSize = is.read(brgb,0,brgb.length);
					nindex = 0;
				}

				if(esc == end_of_bitmap) done_with_bitmap = true;

				if (esc == delta)
				{
					horz_offset = brgb[nindex++]&0xff;
					if (nindex >= readSize)
					{
						readSize = is.read(brgb,0,brgb.length);
						nindex = 0;
					}
					vert_offset = brgb[nindex++]&0xff;
					if (nindex >= readSize)
					{
						readSize = is.read(brgb,0,brgb.length);
						nindex = 0;
					}
					horz += horz_offset;
					vert -= vert_offset;
					index = width*vert+horz;
				}

				if(esc == end_of_line) 
				{
					vert--;
					horz = 0;
					index = width*vert+horz;
				}

				if(esc>2)
				{
					count = 0;
					do{
						bit = brgb[nindex++]&0xff;
						if (nindex >= readSize)
						{
							readSize = is.read(brgb,0,brgb.length);
							nindex = 0;
						}
						count++;

						m1 = ((bit>>>4)&0x0F);
						pix[index++] = colorPalette[m1];
						counter++;
						horz++;
						//be carefull in this case if it runs out of the line bound, 
						//don't update horz and vert variables at this time,just break 
						//and wait until the end_of_line flag to show up.
						if (horz >= width)
						{
							break;   
						}        

						if (counter<esc)
						{
							m2 = bit&(0x0F);
							pix[index++] = colorPalette[m2];
							counter++;
							horz++;
						}

						if (horz >= width)
						{
							break;
						}

					}while(counter<esc);
					counter = 0;
					if((count%2) != 0) nindex += 1;
				}
			}
			else  
			{
				rgb_table_index = brgb[nindex++]&0xff;
				if (nindex >= readSize)
				{
					readSize = is.read(brgb,0,brgb.length);
					nindex = 0;
				}
				m1 = ((rgb_table_index>>>4)&0x0F);
				m2 = rgb_table_index&(0x0F);

				do{
					pix[index++] = colorPalette[m1];
					counter++;
					horz++;

					if (horz >= width)
					{
						break;
					}

					if (counter<len)
					{
						pix[index++] = colorPalette[m2];
						counter++;
						horz++;

						if (horz >= width)
						{
							break;
						}
					}
				}while(counter<len);
				counter = 0;
			}
			if(vert<0) done_with_bitmap = true;
		}while(!done_with_bitmap);
	}
	/*
	 * This method could have returned an Image object directly using
	 * Toolkit.getDefaultToolkit().createImage(ImageProducer) method, 
	 * but it is somewhat slow when called in a non-gui class to create
	 * an image, a subclass of JFrame would do much faster!
	 */
	public void unpackImage (InputStream is) throws IOException
	{
		bitmapHeader = new BitmapHeader();
		bitmapHeader.readHeader(is);
		width = bitmapHeader.imageWidth;
		height = bitmapHeader.imageHeight;
		pix = new int[width*height];

		bitsPerPixel = bitmapHeader.bitCount;

		int bitPerWidth = width*bitsPerPixel;

		if(bitPerWidth%32 == 0)//to make sure scan lines are padded out to even 4-byte boundaries.
		{
			bytePerScanLine = (bitPerWidth>>>3);
		}
		else
		{
			bytePerScanLine = (bitPerWidth>>>3)+(4-(bitPerWidth>>>3)%4);
			//a different method to do the same thing as above!
			//bytePerScanLine = (((bitPerWidth+31) & ~31 ) >> 3);
		}
		switch (bitmapHeader.bitCount)
		{
			case 1:
			{
				unpack2ColorBitmapFile(is);
				break;           
			}
			case 4:
			{
				if(bitmapHeader.compression == BI_RLE4)
					unpack16ColorCompressedBitmapFile(is);
				else
					unpack16ColorBitmapFile(is);
				break;
			}
			case 8:
			{
				if(bitmapHeader.compression == BI_RLE8)
					unpack256ColorCompressedBitmapFile(is);
				else
					unpack256ColorBitmapFile(is);
				break;
			}
			case 16:
			{
				throw new IllegalStateException("16 bit BMP, decoding not implemented!");
				//unpack16bitTrueColorBitmapFile(is);
				//break;
			}
			case 24:
			{
				unpackTrueColorBitmapFile(is);
				break;
			}
			case 32:
			{
				unpack32bitTrueColorBitmapFile(is);
				break;
			}
			default:
			{
				throw new IllegalStateException("Unsupported bitmap format: " + bitmapHeader.bitCount);
			}
		}
	}

	public BMPReader ()
	{
		super();
	}

	public BMPReader (InputStream is) throws IOException
	{
		unpackImage(is);
	}

	public void unpackImage (URL url) throws IOException
	{
		InputStream	is=null;
		try
		{
			is = url.openStream();
			unpackImage(is);
		}
		finally
		{
			FileUtil.closeAll(is);
		}
	}

	public BMPReader (URL url) throws IOException
	{
		unpackImage(url);
	}

	public void unpackImage (File f) throws IOException
	{
		InputStream	is=null;
		try
		{
			is = new FileInputStream(f);
			unpackImage(is);
		}
		finally
		{
			FileUtil.closeAll(is);
		}
	}

	public BMPReader (File f) throws IOException
	{
		unpackImage(f);
	}

	public void unpackImage (String f) throws IOException
	{
		InputStream	is=null;
		try
		{
			is = new FileInputStream(f);
			unpackImage(is);
		}
		finally
		{
			FileUtil.closeAll(is);
		}
	}

	public BMPReader (String f) throws IOException
	{
		unpackImage(f);
	}

	public BMPReader (byte[] data, int offset, int len) throws IOException
	{
		this(new ByteArrayInputStream(data, offset, len));	// no need to close it

	}
	public BMPReader (byte[] data) throws IOException
	{
		this(data, 0, data.length);
	}

	public static final String	BMP_SUFFIX="bmp";
	public static final boolean isBitmapFile (final String filePath)
	{
		return FileUtil.isMatchingFileSuffix(filePath, BMP_SUFFIX);
	}

	public static final boolean isBitmapFile (final URL fileURL)
	{
		return (null == fileURL) ? false : isBitmapFile(fileURL.getPath());
	}
	/*
	 * @see net.community.chest.ui.helpers.images.AbstractImageReader#isMatchingFile(java.lang.String)
	 */
	@Override
	public boolean isMatchingFile (String filePath)
	{
		return isBitmapFile(filePath);
	}
	/*
	 * @see net.community.chest.ui.helpers.images.AbstractImageReader#readImages(java.io.InputStream)
	 */
	@Override
	public List<Image> readImages (InputStream in) throws IOException
	{
		unpackImage(in);

		final Image	img=getImage();
		return (null == img) ? null : Arrays.asList(img);
	}
}

/*
 *
 */
package net.community.chest.awt.image;

import java.awt.Image;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import net.community.chest.io.FileUtil;
import net.community.chest.io.output.AutoGrowArrayOutputStream;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 10, 2009 11:02:08 AM
 *
 */
public class DefaultImageReader extends AbstractImageReader {
    /**
     * Default/empty constructor
     */
    public DefaultImageReader ()
    {
        super();
    }
    /*
     * @see net.community.chest.awt.image.AbstractImageReader#isMatchingFile(java.lang.String)
     */
    @Override
    public boolean isMatchingFile (String filePath)
    {
        return FileUtil.isMatchingFileSuffix(filePath, GIF_SUFFIX, JPG_SUFFIX, PNG_SUFFIX);
    }

    public List<Image> readImages (ImageIcon icon) throws IOException
    {
        final Image    img=(null == icon) ? null : icon.getImage();
        if (null == img)
            throw new StreamCorruptedException("No Image loaded");

        return Arrays.asList(img);
    }
    /*
     * @see net.community.chest.awt.image.AbstractImageReader#readImages(byte[], int, int)
     */
    @Override
    public List<Image> readImages (byte[] data, int offset, int len) throws IOException
    {
        if ((null == data) || (data.length <= 0)
         || (offset < 0) || (len <= 0)
         || ((offset+len) > data.length))
            throw new StreamCorruptedException("Bad/Illegal image data buffer");

        if ((offset > 0) || (len < data.length))
        {
            final byte[]    newData=new byte[len];
            System.arraycopy(data, offset, newData, 0, len);
            return readImages(new ImageIcon(newData, getClass().getName()));
        }
        else
            return readImages(new ImageIcon(data, getClass().getName()));
    }
    /*
     * @see net.community.chest.awt.image.AbstractImageReader#readImages(java.io.InputStream)
     */
    @Override
    public List<Image> readImages (InputStream in) throws IOException
    {
        final Map.Entry<byte[],Integer>    di=AutoGrowArrayOutputStream.readAllData(in);
        final byte[]                    data=(null == di) ? null : di.getKey();
        return readImages(data);
    }
    /*
     * @see net.community.chest.awt.image.AbstractImageReader#readImages(java.lang.String)
     */
    @Override
    public List<Image> readImages (String f) throws IOException
    {
        if ((null == f) || (f.length() <= 0))
            throw new EOFException("no file path specified");
        return readImages(new ImageIcon(f, getClass().getName()));
    }
    /*
     * @see net.community.chest.awt.image.AbstractImageReader#readImages(java.net.URL)
     */
    @Override
    public List<Image> readImages (URL url) throws IOException
    {
        if (null == url)
            throw new EOFException("No URL provided");

        return readImages(new ImageIcon(url, getClass().getName()));
    }
    /*
     * @see net.community.chest.awt.image.AbstractImageReader#readImages(java.io.File)
     */
    @Override
    public List<Image> readImages (File f) throws IOException
    {
        if ((null == f) || (!f.isFile()))
            throw new FileNotFoundException("readImages(" + f + ") bad file");

        return readImages(f.getAbsolutePath());
    }

    public static final DefaultImageReader    DEFAULT=new DefaultImageReader();
}

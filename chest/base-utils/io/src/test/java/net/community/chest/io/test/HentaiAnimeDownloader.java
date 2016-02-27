/*
 * 
 */
package net.community.chest.io.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import net.community.chest.Triplet;
import net.community.chest.io.IOCopier;
import net.community.chest.net.proto.text.http.HttpUtils;
import net.community.chest.test.TestBase;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Dec 13, 2011 1:13:56 PM
 */
public final class HentaiAnimeDownloader extends TestBase {
	private HentaiAnimeDownloader ()
	{
		// no instance
	}

	private static final HttpURLConnection openConnection (final URL url) throws IOException
	{
		final HttpURLConnection	conn=(HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(30000);
		conn.setDoInput(true);
		conn.setRequestMethod("GET");
		return conn;
	}

	private static final HttpURLConnection openConnection (final String ref) throws IOException
	{
		return openConnection(new URL(ref));
	}

	private static final String loadImagesPage (final URL url) throws IOException
	{
		final StringWriter		w=new StringWriter(IOCopier.DEFAULT_COPY_SIZE);
		final HttpURLConnection	conn=openConnection(url);
		try
		{
			try(Reader	r=new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
				final long			dataSize=IOCopier.copyReaderToWriter(r, w);
				if (dataSize < 0L)
					throw new StreamCorruptedException("Failed (" + dataSize + ") to get raw data");
			}
		}
		finally
		{
			conn.disconnect();
		}
		return w.toString();
	}

	private static final String loadImagesPage (final String ref) throws IOException
	{
		return loadImagesPage(new URL(ref));
	}

	private static final String	ATAG="<a ", ATAG_END="</a>", HREF_ATTR="href=", IMG_TAG="<img ", SRC_ATTR="src=";
	private static final Triplet<String,String,Integer> extractChainLinkData (final String data, final int firstPos)
	{
		for (int	curPos=data.indexOf(ATAG, firstPos); curPos >= 0; )
		{
			final int	nextPos=data.indexOf(ATAG_END, curPos);
			if (nextPos <= curPos)
				break;

			final int		lastPos=nextPos + ATAG_END.length();
			final String	aValue=data.substring(curPos, lastPos);
			curPos = data.indexOf(ATAG, lastPos);

            if (!aValue.contains("id=\"img\"")) {
                continue;
            }

			final int	hrefPos=aValue.indexOf(HREF_ATTR);
			if (hrefPos <= 0)
				continue;

			final int	imgPos=aValue.indexOf(IMG_TAG);
			if (imgPos <= hrefPos)
				continue;

			final int	srcPos=aValue.indexOf(SRC_ATTR);
			if (srcPos <= imgPos)
				continue;

			final int	startPos=srcPos + SRC_ATTR.length() + 1, endPos=aValue.indexOf('"', startPos + 1);
			if (endPos <= startPos)
				continue;

			final String	imgRef=aValue.substring(startPos, endPos);
			if ((!imgRef.endsWith(".jpg")) && (!imgRef.endsWith(".png"))) {
			    continue;
			}

			final int		hrefStart=hrefPos + HREF_ATTR.length() + 1, hrefEnd=aValue.indexOf('"', hrefStart + 1);
			final String	nextRef=aValue.substring(hrefStart, hrefEnd);
			return new Triplet<String,String,Integer>(imgRef, nextRef, Integer.valueOf(lastPos));
		}

		return null;
	}

	private static final File downloadDirect (final String imgRef, final File imgFile) throws IOException
	{
		final HttpURLConnection	conn=openConnection(imgRef);
		try
		{
			try(OutputStream	fout=new FileOutputStream(imgFile)) {
				try(InputStream	in=conn.getInputStream()) {
					final long	bytesCount=IOCopier.copyStreams(in, fout);
					if (bytesCount <= 0L)
						throw new StreamCorruptedException("Failed (" + bytesCount + ") to download image data");
				}
			}
		}
		finally
		{
			conn.disconnect();
		}

		return imgFile;
	}

	private static final long  MIN_FILE_LENGTH=3072L;
	private static final File downloadImage (final PrintStream	out, final File	rootFolder, final String imgRef)
	{
		final int		suffixPos=imgRef.lastIndexOf('.');
		int				namePos=suffixPos;
		for (namePos-- ; namePos > 0; namePos--)
		{
			final char	ch=imgRef.charAt(namePos);
			if (((ch >= 'a') && (ch <= 'z'))
			 || ((ch >= 'A') && (ch <= 'Z'))
			 || ((ch >= '0') && (ch <= '9'))
			 || (ch == '.') || (ch == '-') || (ch == '_'))
				continue;
			else
				break;
		}

		final String	name=imgRef.substring(namePos + 1);
		final File		imgFile=new File(rootFolder, name);
		if (imgFile.exists() && (imgFile.length() > MIN_FILE_LENGTH))
		{
			out.append("\tSkipping ").println(imgFile.getAbsolutePath());
			return imgFile;
		}

		out.append("\tDownloading ").println(imgFile.getAbsolutePath());
		try
		{
			downloadDirect(imgRef, imgFile);
			
			if (imgFile.length() <= MIN_FILE_LENGTH)
			    throw new StreamCorruptedException("Bad file length: " + imgFile.length());
		}
		catch(IOException e)
		{
			System.err.append('\t').append(e.getClass().getSimpleName()).append(": ").println(e.getMessage());
			
			if (imgFile.exists()) {
			    if (imgFile.delete()) {
			        System.out.append('\t').append("Deleted ").println(imgFile.getAbsolutePath());
			    } else {
                    System.err.append('\t').append("Failed to deleted ").println(imgFile.getAbsolutePath());
			    }
			}
		}

		return imgFile;
	}

	// g.e-hentai.org
	protected static final void ghGaleriesDownload  (final PrintStream	out,
												   	 final File	rootFolder, final long chainDelay,
												   	 final String startRef)
											throws IOException, InterruptedException
	{
	    Collection<File>   missingFiles=new LinkedList<File>();
		for (String	ref=startRef, lastName=null; (ref != null) && (ref.length() > 0); )
		{
			out.append("Processing ").println(ref);
			if (chainDelay > 0L)
				Thread.sleep(chainDelay);

			final String							data=loadImagesPage(ref);
			final Triplet<String,String,Integer>	linkRef=extractChainLinkData(data, 0);
			if (linkRef == null)
				throw new FileNotFoundException("No embedded image found");

			final String	imgRef=linkRef.getV1();
			final File		imgFile=downloadImage(out, rootFolder, imgRef);
			if (!imgFile.exists()) {
			    missingFiles.add(imgFile);
			}

			final String	imgName=imgFile.getName();
			if ((lastName != null) && lastName.equals(imgName))
				break;	// same name is repeated for last
			else
				lastName = imgName;

			ref = linkRef.getV2();
		}
		
		if (missingFiles.size() > 0) {
		    for (File f : missingFiles) {
		        System.err.append("\tMissing ").println(f.getAbsolutePath());
		    }
		    throw new StreamCorruptedException("There are " + missingFiles.size() + " missing files");
		}
	}

	private static final String zizkiAdjustImageRef (final String org)
	{
		if ((org == null) || (org.length() <= 0))
			return org;

		final int		lastPos=org.lastIndexOf('/'),
						firstPos=org.lastIndexOf('/', lastPos - 1);
		final String	orgRef=org.substring(firstPos + 1, lastPos);
		final int		sepPos=orgRef.indexOf('-');
		final String	majorValue=orgRef.substring(0, sepPos),
						minorValue=orgRef.substring(sepPos + 1);
		final int		orgMajor=Integer.parseInt(majorValue),
						orgMinor=Integer.parseInt(minorValue);
		return org.substring(0, firstPos) + "/" + (orgMajor - 1) + "-" + (orgMinor - 1) + "/" + org.substring(lastPos + 1);
	}

	// zizki.com
	protected static final void zizkiGaleriesDownload  (final PrintStream	out,
		   	 											final File	rootFolder, final long chainDelay,
		   	 											final String startRef)
		   throws IOException, InterruptedException, URISyntaxException
	{
		final URL		startURL=new URL(startRef);
		final String	data=loadImagesPage(startURL);
		final URI		baseURI=startURL.toURI();
		for (Triplet<String,String,Integer>	linkRef=extractChainLinkData(data, 0);
			 linkRef != null;
			 linkRef = extractChainLinkData(data, linkRef.getV3().intValue()))
		{
			final String	baseRef=zizkiAdjustImageRef(linkRef.getV1());
			final URI		imgRef=HttpUtils.resolveItemURI(baseURI, baseRef);
			out.append("Processing ").println(imgRef);
			if (chainDelay > 0L)
				Thread.sleep(chainDelay);

			final File	imgFile=downloadImage(out, rootFolder, imgRef.toString());
			if (imgFile == null)
				throw new FileNotFoundException("No file downloaded");
		}
	}

	// www.fakku.net
	private static final void fakkuGaleriesDownload (final PrintStream	out,
													 final File	rootFolder, final long chainDelay,
													 final String startRef)
							throws IOException, InterruptedException
	{
		final int			imgNamePos=startRef.lastIndexOf('/'),
							suffixPos=startRef.indexOf('.', imgNamePos + 1);
		final String		baseRef=startRef.substring(0, imgNamePos + 1),
							startNum=startRef.substring(imgNamePos + 1, suffixPos),
							suffix=startRef.substring(suffixPos);
		final int			lastZero=startNum.lastIndexOf('0');
		final String		startIndex=(lastZero < 0) ? startNum : startNum.substring(lastZero + 1);
		final StringBuilder	sb=new StringBuilder(startRef.length()).append(baseRef);
		final int			baseLen=sb.length();
		for (int picIndex=Integer.parseInt(startIndex); ; picIndex++)
		{
			final String	indexValue=String.valueOf(picIndex);
			final int		padLen=3 - indexValue.length();
			sb.setLength(baseLen);
			for (int	padIndex=0; padIndex < padLen; padIndex++)
				sb.append('0');
			sb.append(indexValue);
			sb.append(suffix);
			
			final String	imgRef=sb.toString();
			out.append("Processing ").println(imgRef);
			if (chainDelay > 0L)
				Thread.sleep(chainDelay);

			final File	imgFile=downloadImage(out, rootFolder, imgRef.toString());
			if (imgFile == null)
				throw new FileNotFoundException("No file downloaded");
		}
	}

	private static final void downloadChain (final BufferedReader in, final PrintStream	out,
											 final File	rootFolder, final long chainDelay,
											 final String startRef)
		throws IOException, InterruptedException, URISyntaxException
	{
		if (startRef.contains("g.e-hentai.org"))
			ghGaleriesDownload(out, rootFolder, chainDelay, startRef);
		else if (startRef.contains("zizki.com"))
			zizkiGaleriesDownload(out, rootFolder, chainDelay, startRef);
		else if (startRef.contains("fakku.net"))
			fakkuGaleriesDownload(out, rootFolder, chainDelay, startRef);
		else
			throw new IllegalStateException("Unknown site: " + startRef);
	}

	public static void main (String[] args)
	{
		final BufferedReader	in=getStdin();
		final PrintStream		out=System.out;
		final int				numArgs=(args == null) ? 0 : args.length;
		final String			startRef=(numArgs >= 1) ? args[0] : getval(out, in, "Root URL (or Quit)");
		if (isQuit(startRef))
			return;

		final String	targetFolder=(numArgs >= 2) ? args[1] : getval(out, in, "target folder (or Quit)");
		if (isQuit(targetFolder))
			return;

		final File	rootFolder=new File(targetFolder);
		if (rootFolder.exists())
		{
			if (!rootFolder.isDirectory())
				throw new IllegalStateException("Target is not a folder");
		}
		else
		{
			if (!rootFolder.mkdirs())
				throw new IllegalStateException("Failed to create target folder");
		}

		final String	delay=(numArgs >= 3) ? args[2] : getval(out, in, "delay (sec.) - or Quit");
		if (isQuit(delay))
			return;

		try
		{
			downloadChain(in, out, rootFolder, TimeUnit.SECONDS.toMillis(Integer.parseInt(delay)),startRef);
		}
		catch(Exception e)
		{
			System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}

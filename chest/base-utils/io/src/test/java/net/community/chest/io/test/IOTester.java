package net.community.chest.io.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.ByteOrder;

import javax.xml.transform.stream.StreamSource;

import net.community.chest.Triplet;
import net.community.chest.io.FileUtil;
import net.community.chest.io.dom.PrettyPrintDocumentBuilder;
import net.community.chest.io.encode.OutputDataEncoder;
import net.community.chest.io.encode.hex.Hex;
import net.community.chest.io.encode.hex.HexDumpOutputStream;
import net.community.chest.io.file.FileAttributeType;
import net.community.chest.io.file.FileIOUtils;
import net.community.chest.lang.StringUtil;
import net.community.chest.test.TestBase;
import net.community.chest.xml.transform.TransformerUtil;

import org.w3c.dom.Document;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 17, 2008 7:47:52 AM
 */
public class IOTester extends TestBase {

	public static final ByteOrder toByteOrder (final String s)
	{
		if ("native".equalsIgnoreCase(s))
			return ByteOrder.nativeOrder();

		final ByteOrder[]	orders={ ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN };
		for (final ByteOrder o : orders)
		{
			if (0 == StringUtil.compareDataStrings(s, String.valueOf(o), false))
				return o;
		}

		return null;
	}

	/* -------------------------------------------------------------------- */

	// arguments are written to the hex dump stream
	public static final int testHexDumpStream (final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int		numArgs=(null == args) ? 0 : args.length;
		Writer			w=null;
		StringBuilder	sb=null;
		try
		{
			HexDumpOutputStream<StringBuilder>	ds=null;
			for (int	aIndex=0; ; aIndex++)
			{
				final String	v=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "(S)how/string value/Quit");
				if (isQuit(v)) break;

				try
				{
					if ("S".equalsIgnoreCase(v) || "Show".equalsIgnoreCase(v))
					{
						if (w != null)
							w.flush();

						out.println(sb);
						continue;
					}

					if (null == sb)
						sb = new StringBuilder(1024);

					if (null == ds)
						ds = new HexDumpOutputStream<StringBuilder>(sb, false);

					if (null == w)
						w = new OutputStreamWriter(ds);

					w.write(v);
				}
				catch(Exception e)
				{
					System.err.println(e.getClass().getName() + " while write val=" + v + ": " + e.getMessage());
				}
			}

			return 0;
		}
		finally
		{
			try
			{
				FileUtil.closeAll(w);
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + " while close writer: " + e.getMessage());
			}
			out.println(sb);
		}
	}

	//////////////////////////////////////////////////////////////////////////

	// args[i] a File path
	public static final int testFileProperties (
			final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int					numArgs=(null == args) ? 0 : args.length;
		for (int aIndex=0; ; aIndex++)
		{
			final String	s=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "file path (or Quit)");
			final int		sLen=(null == s) ? 0 : s.length();
			if (sLen <= 0)
				continue;
			if (isQuit(s)) break;

			out.println(s + " attributes:");

			final File	f=new File(s);
			for (final FileAttributeType a : FileAttributeType.VALUES)
			{
				try
				{
					final Object	o=a.getValue(f);
					out.println("\t" + a + "=" + o);
				}
				catch(Exception e)
				{
					System.err.println(e.getClass().getName() + " while get " + s + " attribute=" + a + ": " + e.getMessage());
				}
			}
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	// args[i] even=src file, odd=dst file - if folder(s) then recursive test done
	public static final int testCompareFileContent (
			final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int aIndex=0; ; )
		{
			File	srcFile=null;
			for ( ; srcFile == null; aIndex++)
			{
				final String	f=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "source file path (or Quit)");
				final int		sLen=(null == f) ? 0 : f.length();
				if (sLen <= 0)
					continue;
				if (isQuit(f))
					return 0;

				srcFile = new File(f);
			}

			File	dstFile=null;
			for ( ; dstFile == null; aIndex++)
			{
				final String	f=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "destination file path (or Quit)");
				final int		sLen=(null == f) ? 0 : f.length();
				if (sLen <= 0)
					continue;
				if (isQuit(f))
					return 0;
				dstFile = new File(f);
			}

			if (srcFile.exists() && dstFile.exists() && srcFile.isFile() && dstFile.isFile())
			{
				for ( ; ; )
				{
					out.print("Comparing " + srcFile + " / " + dstFile + " ");
					final long	cmpStart=System.currentTimeMillis();
					try
					{
						final Triplet<Long,Byte,Byte>	cmpOffset=
							FileIOUtils.findDifference(srcFile, dstFile);
						final long						cmpEnd=System.currentTimeMillis(), cmpDuration=cmpEnd - cmpStart;
						if (null == cmpOffset)
							out.print("same");
						else
							out.print("found difference at offset " + cmpOffset.getV1() + ": src=" + Hex.toString(cmpOffset.getV2(), true) + "/dst=" + Hex.toString(cmpOffset.getV3(), true));
						out.println(" after " + cmpDuration + " msec.");
					}
					catch(Exception e)
					{
						final long	cmpEnd=System.currentTimeMillis(), cmpDuration=cmpEnd - cmpStart;
						System.err.println(e.getClass().getName() + " after " + cmpDuration + " msec.: " + e.getMessage());
					}

					final String	ans=getval(out, in, "again [y]/n");
					if ((null == ans) || (ans.length() <= 0) || ('y' == Character.toLowerCase(ans.charAt(0))))
						continue;
					else
						break;
				}
			}
			else
				System.err.println("Source(" + srcFile + ")/Destination(" + dstFile + ") file(s) does not exist/not a file");
		}
	}

	//////////////////////////////////////////////////////////////////////////

	// args[0]=charset, args[1]=byte order, args[2...]=arguments written as UTF to the encoder
	public static final int testDataOutputEncoder (
			final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int		numArgs=(null == args) ? 0 : args.length;
		final String[]	tstArgs=resolveTestParameters(out, in, args, "charset", "byte order");
		if ((null == tstArgs) || (tstArgs.length < 2))
			return 0;

		final String			charsetName=tstArgs[0], o=tstArgs[1];
		final OutputDataEncoder	enc=new EndianOutputEncoderTester(toByteOrder(o), out);
		for (int	aIndex=2; ; aIndex++)
		{
			final String	s=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "input string (or Quit)");
			if (isQuit(s))
				break;

			try
			{
				enc.writeString(s, charsetName);
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + " while write string=" + s + "[" + charsetName + "]: " + e.getMessage());
			}
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	public static final int testPrettyPrintDocumentBuilder (
			final PrintStream out, final BufferedReader in, final File xmlFile)
	{
		for ( ; ; )
		{
			out.println("Processing " + xmlFile);
			Document	doc=null;
			try
			{
				if (null == (doc=PrettyPrintDocumentBuilder.DEFAULT.parse(xmlFile)))
					throw new IllegalStateException("No document created");
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + " while processing file=" + xmlFile + ": " + e.getMessage());
			}

			for ( ; ; )
			{
				final String	ans=getval(out, in, "run [a]gain/dump to (c)onsole/dump to file path/(Q)uit");
				if ((null == ans) || (ans.length() <= 0)
				 || "a".equalsIgnoreCase(ans) || "again".equalsIgnoreCase(ans))
					break;
				if (isQuit(ans)) return 0;

				try
				{
					if ("c".equalsIgnoreCase(ans) || "console".equalsIgnoreCase(ans))
						net.community.chest.io.dom.PrettyPrintTransformer.DEFAULT.transform(doc, out);
					else
						net.community.chest.io.dom.PrettyPrintTransformer.DEFAULT.transform(doc, new File(ans));
				}
				catch(Exception e)
				{
					System.err.println(e.getClass().getName() + " while transforming to " + ans + ": " + e.getMessage());
				}
			}
		}
	}

	// args[i]=XML file path
	public static final int testPrettyPrintDocumentBuilder (
			final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int		numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	xmlPath=
				(aIndex < numArgs) ? args[aIndex] : getval(out, in, "input XML file path (or Quit)");
			if ((null == xmlPath) || (xmlPath.length() <= 0))
				continue;
			if (isQuit(xmlPath))
				break;

			testPrettyPrintDocumentBuilder(out, in, new File(xmlPath));
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	public static final int testSAXPrettyPrinter (
			final PrintStream out, final BufferedReader in, final File xmlFile)
	{
		for ( ; ; )
		{
			out.println("Processing " + xmlFile);

			for ( ; ; )
			{
				final String	ans=getval(out, in, "run [a]gain/dump to (c)onsole/dump to file path/(Q)uit");
				if ((null == ans) || (ans.length() <= 0)
				 || "a".equalsIgnoreCase(ans) || "again".equalsIgnoreCase(ans))
					break;
				if (isQuit(ans)) return 0;

				try
				{
					final StreamSource	src=new StreamSource(xmlFile);
					if ("c".equalsIgnoreCase(ans) || "console".equalsIgnoreCase(ans))
						TransformerUtil.transform(src, out, net.community.chest.io.sax.PrettyPrintTransformer.DEFAULT);
					else
						TransformerUtil.transform(src, new File(ans), net.community.chest.io.sax.PrettyPrintTransformer.DEFAULT);
				}
				catch(Exception e)
				{
					System.err.println(e.getClass().getName() + " while transforming to " + ans + ": " + e.getMessage());
				}
			}
		}
	}

	// args[i]=XML file path
	public static final int testSAXPrettyPrinter (
			final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int		numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	xmlPath=
				(aIndex < numArgs) ? args[aIndex] : getval(out, in, "input XML file path (or Quit)");
			if ((null == xmlPath) || (xmlPath.length() <= 0))
				continue;
			if (isQuit(xmlPath))
				break;

			testSAXPrettyPrinter(out, in, new File(xmlPath));
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	private static final boolean extractDateValue (final String s, final String[] dv)
	{
		final String	v=(s == null) ? null : s.trim();
		final int		vLen=(v == null) ? 0 : v.length(),
						sPos=(vLen >= 3) ? v.indexOf('.') : (-1);
		if ((sPos <= 0) || (sPos >= (vLen -1 )))
			return false;

		dv[0] = v.substring(0, sPos);

		final String	mthVal=v.substring(sPos + 1);
		final int		xPos=mthVal.indexOf(' ');
		dv[1] = (xPos < 0) ? mthVal : mthVal.substring(0, xPos);

		try
		{
			for (final String vv : dv)
			{
				final byte	bv=Byte.parseByte(vv);
				if ((bv <= 0) || (bv >= 32))
					throw new NumberFormatException("Bad day/month value (" + vv + ") in " + s);
			}

			return true;
		}
		catch(NumberFormatException e)
		{
			return false;
		}
	}

	public static final int testRenamePaisFiles (final PrintStream out, final File fldr)
	{
		if ((fldr == null) || (!fldr.exists()) || (!fldr.isDirectory()))
		{
			System.err.println("Not a folder: " + fldr);
			return (-1);
		}

		final String	fldrPath=fldr.getAbsolutePath();
		final int		pLen=(fldrPath == null) ? 0 : fldrPath.length(),
						sPos=(pLen <= 1) ? (-1) : fldrPath.lastIndexOf(File.separatorChar);
		if ((sPos <= 0) || (sPos >= (pLen -1)))
		{
			System.err.println("Bad folder path format: " + fldr);
			return (-2);
		}

		final String	yearVal=fldrPath.substring(sPos + 1);
		try
		{
			final int	yv=Integer.parseInt(yearVal);
			if ((yv <= 2000) || (yv >= 3000))
				throw new NumberFormatException("Invalid range");
		}
		catch(NumberFormatException e)
		{
			System.err.append("Bad year value (").append(yearVal)
					  .append(")[").append(e.getMessage())
					  .append("] for folder=").append(fldrPath)
					  .println()
					  ;
			return (-3);
		}

		final File[]	fa=fldr.listFiles();
		if ((fa == null) || (fa.length <= 0))
		{
			System.out.append("No files found in ").append(fldrPath).println();
			return 0;
		}

		final String[]	dVal=new String[2];
		out.append("Processing ").append(fldrPath).println();
		for (final File	f : fa)
		{
			if (!f.isFile())
				continue;

			final String	fName=f.getName();
			if (!StringUtil.endsWith(fName, ".eml", true, false))
				continue;

			final int	nLen=fName.length(), dPos=fName.lastIndexOf('-');
			if ((dPos <= 0) || (dPos >= (nLen - 1)))
				continue;

			if (!extractDateValue(fName.substring(dPos + 1, nLen - 4), dVal))
				continue;

			String	newName=fName.substring(0, dPos + 1) + " " + yearVal + "." + dVal[1] + "." + dVal[0]
			      	      + (fName.contains("(w)") ? " (w)" : "") + ".eml"
			      	      ;

			final File	newFile=new File(fldr, newName);
			if (!f.renameTo(newFile))
			{
				System.err.println("Failed to rename " + fName + " => " + newName);
				continue;
			}

			out.append('\t').append(fName).append(" => ").append(newName).println();
		}

		return 0;
	}

	// args[i]=root folder
	public static final int testRenamePaisFiles (
			final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	fldrPath=
				(aIndex < numArgs) ? args[aIndex] : getval(out, in, "files root folder (or Quit)");
			if ((null == fldrPath) || (fldrPath.length() <= 0))
				continue;
			if (isQuit(fldrPath))
				break;

			testRenamePaisFiles(out, new File(fldrPath));
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		final BufferedReader	in=getStdin();
//		final int				nErr=testHexDumpStream(System.out, in, args);
//		final int				nErr=testFileProperties(System.out, in, args);
//		final int				nErr=testCompareFileContent(System.out, in, args);
//		final int				nErr=testDataOutputEncoder(System.out, in, args);
//		final int				nErr=testPrettyPrintDocumentBuilder(System.out, in, args);
//		final int				nErr=testSAXPrettyPrinter(System.out, in, args);
		final int				nErr=testRenamePaisFiles(System.out, in, args);
		if (nErr != 0)
			System.err.println("test failed (err=" + nErr + ")");
		else
			System.out.println("OK");
	}
}

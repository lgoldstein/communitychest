package net.community.chest.io.test;

import java.io.BufferedReader;
import java.io.PrintStream;

import net.community.chest.io.encode.hex.Hex;
import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 1, 2008 11:07:07 AM
 */
public final class HexTester extends TestBase {
	// arguments are assumed to be HEX sequences
	public static final int testHexSequences (final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	hs=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "HEX sequence (or Quit)");
			if ((null == hs) || (hs.length() <= 0))
				continue;
			if (isQuit(hs)) break;

			final String	ds=getval(out, in, hs + " delimiter (ENTER=none)");
			if (isQuit(ds)) break;

			final char	delim=((null == ds) || (ds.length() <= 0)) ? '\0' : ds.charAt(0);
			final int	sqLen=Hex.getHexSequenceLength(hs, delim);
			if (sqLen < 0)
				System.err.println("Invalid sequence length: " + sqLen);

			try
			{
				final byte[]	a=Hex.toByteArray(hs, delim);
				if ((a != null) && (a.length > 0))
				{
					final StringBuilder	sb=new StringBuilder(a.length * 3);
					for (int	vIndex=0; vIndex < a.length; vIndex++)
					{
						if ((vIndex > 0) && (delim != '\0'))
							sb.append(delim);
						Hex.appendHex(sb, a[vIndex], true);
					}

					out.println("\t" + hs + " => " + sb);
				}
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + " while process seq.=" + hs + ": " + e.getMessage());
			}
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		final BufferedReader	in=getStdin();
		final int				nErr=testHexSequences(System.out, in, args);
		if (nErr != 0)
			System.err.println("test failed (err=" + nErr + ")");
		else
			System.out.println("OK");
	}
}

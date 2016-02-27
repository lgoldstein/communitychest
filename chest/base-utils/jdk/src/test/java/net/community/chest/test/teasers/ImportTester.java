/*
 * 
 */
package net.community.chest.test.teasers;

import java.io.BufferedReader;
import java.io.PrintStream;

import net.community.chest.reflect.ClassUtil;
import net.community.chest.test.TestBase;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 15, 2009 12:59:42 PM
 */
public class ImportTester extends TestBase {
	public static final void testClassImport (final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	cn=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "class path (or Quit)");
			if ((null == cn) || (cn.length() <= 0))
				continue;
			if (isQuit(cn))
				break;

			try
			{
				final Class<?>	c=ClassUtil.loadClassByName(cn);
				if (null == c)
					throw new ClassNotFoundException(cn);
				else
					out.println("\tloaded " + c.getName());
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + "on load [" + cn + "]: " + e.getMessage());
			}
		}
	}

	public static final void main (String[] args)
	{
		testClassImport(System.out, getStdin(), args);
	}
}

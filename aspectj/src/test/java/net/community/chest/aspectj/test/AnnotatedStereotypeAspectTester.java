/*
 * 
 */
package net.community.chest.aspectj.test;

import java.io.BufferedReader;
import java.io.PrintStream;

import net.community.chest.test.TestBase;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 29, 2010 9:50:52 AM
 */
public class AnnotatedStereotypeAspectTester extends TestBase {
	public AnnotatedStereotypeAspectTester ()
	{
		super();
	}

	@DerivedStereotype1
	public static class Derived1 {
		public Derived1 ()
		{
			super();
		}

		public long getTime ()
		{
			return System.currentTimeMillis();
		}

		public void write (String s)
		{
			System.out.println(s);
		}
	}

	@DerivedStereotype2
	public static class Derived2 {
		public Derived2 ()
		{
			super();
		}

		public long showNanos ()
		{
			return System.nanoTime();
		}

		public void append (String s)
		{
			System.err.println(s);
		}
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		final Derived1			d1=new Derived1();
		final Derived2			d2=new Derived2();
		final BufferedReader	in=getStdin();
		final PrintStream		out=System.out;
		final int				numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	v=
				(aIndex < numArgs) ? args[aIndex] : getval(out, in, "value (or Quit)");
			if ((null == v) || (v.length() <= 0))
				continue;
			if (isQuit(v)) break;

			d1.write(String.valueOf(d1.getTime()) + ": " + v);
			d2.append(String.valueOf(d2.showNanos()) + ": " + v);
		}
	}
}

/*
 * 
 */
package net.community.chest.net.rmi.test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.community.chest.net.rmi.RMIUtils;
import net.community.chest.test.TestBase;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 25, 2009 10:42:47 AM
 */
public class BaseRMITester extends TestBase {
	public static Map.Entry<Integer,Registry> inputRegistry (
			final PrintStream out, final BufferedReader in, final int aIndex, final List<String> args) throws RemoteException
	{
		final int		numArgs=(null == args) ? 0 : args.size();
		final String	rk=
			(aIndex < numArgs) ? args.get(aIndex) : getval(out, in, "host@port/Quit[ENTER=localhost@" + Registry.REGISTRY_PORT + "]");
		if (isQuit(rk))
			return null;

		final Map.Entry<String,Integer>	rkp=RMIUtils.fromRegistryKey(rk);
		final String					host=(null == rkp) ? null : rkp.getKey();
		final Integer					port=(null == rkp) ? null : rkp.getValue();
		final Registry					r=
			RMIUtils.getRegistry(host, (null == port) ? 0 : port.intValue());
		if ((null == r) && (null == port))
			return null;

		return new MapEntryImpl<Integer,Registry>(port, r);
	}

	/* ------------------------------------------------------------------- */

	public static final String inputLookupName (final PrintStream out, final BufferedReader in, final int sIndex, final List<String> args)
	{
		final int		numArgs=(null == args) ? 0 : args.size();
		for (int	aIndex=sIndex; ; aIndex++)
		{
			final String	n=
				(aIndex < numArgs) ? args.get(aIndex) : getval(out, in, "bind lookup name (or Quit)");
			if ((n != null) && (n.length() > 0))
				return n;
		}
	}

	//////////////////////////////////////////////////////////////////////////

	public void testRMIClient (final PrintStream out, final BufferedReader in, final List<String> args)  throws Exception
	{
		if (null == in)
			return;
		out.println("testRMIServer(" + args + ") N/A");
	}

	public void testRMIServer (final PrintStream out, final BufferedReader in, final List<String> args) throws Exception
	{
		if (null == in)
			return;
		out.println("testRMIServer(" + args + ") N/A");
	}

	//////////////////////////////////////////////////////////////////////////

	public static final void testShowRMIBindings (
			final PrintStream out, final BufferedReader in, final Registry r)
	{
		for ( ; ; )
		{
			final String	ans=getval(out, in, "[L]ist/loo(k)up/(q)uit");
			if (isQuit(ans)) break;

			final char	op=((null == ans) || (ans.length() <= 0)) ? '\0' : Character.toLowerCase(ans.charAt(0));
			try
			{
				switch(op)
				{
					case '\0'	:
					case 'l'	:
						{
							final String[]	names=r.list();
							if ((null == names) || (names.length <= 0))
								out.println("No bindings");
							else
							{
								for (final String n : names)
									out.println("\t" + n);
							}
						}
						break;

					case 'k'	:
						for ( ; ; )
						{
							final String	n=getval(out, in, "bind name (or Quit)");
							if (isQuit(n)) break;
							if ((null == n) || (n.length() <= 0))
								continue;

							try
							{
								final Remote	o=r.lookup(n);
								out.println("\tgot " + o);
							}
							catch(Exception e)
							{
								System.err.println(e.getClass().getName() + e.getMessage());
							}
						}
						break;

					default		:	// do nothing
				}
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + e.getMessage());
			}
		}
	}
	// each argument is a "host@port" where either host or port are optional
	public static final void testShowRMIBindings (final PrintStream out, final BufferedReader in, final List<String> args)
	{
		for (int	aIndex=0; ; aIndex++)
		{
			try
			{
				final Map.Entry<Integer,Registry>	rp=inputRegistry(out, in, aIndex, args);
				final Registry						r=(null == rp) ? null : rp.getValue();
				testShowRMIBindings(out, in, r);
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + e.getMessage());
			}
		}
 	}

	//////////////////////////////////////////////////////////////////////////

	public static void testRMIGeneral (final PrintStream out, final BufferedReader in, final List<String> args)
	{
		testShowRMIBindings(out, in, args);
	}

	//////////////////////////////////////////////////////////////////////////

	public void runTest (final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int				numArgs=(null == args) ? 0 : args.length;
		final List<String>		al=(numArgs <= 0) ? null : new ArrayList<String>(Arrays.asList(args));
		final String			tt;
		if (numArgs <= 0)
		{
			for (String	ans=null; ; )
			{
				ans = getval(out, in, "(c)lient/(s)erver/(q)uit/anything else for general test");
				if ((null == ans) || (ans.length() <= 0))
					continue;
				tt = ans;
				break;
			}
		}
		else
			tt = al.remove(0);

		if (isQuit(tt))
			return;

		final char	cc=Character.toLowerCase(tt.charAt(0));
		try
		{
			switch(cc)
			{
				case 'c'	: testRMIClient(out, in, al); break;
				case 's'	: testRMIServer(out, in, al); break;
				default		:
					if (al != null)
						al.add(0, tt);
					testRMIGeneral(out, in, al);
			}
		}
		catch(Exception e)
		{
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}
	// args[0]="client/server"
	public BaseRMITester ()
	{
		super();
	}

	//////////////////////////////////////////////////////////////////////////

	// this is just an example
	public static void main (String[] args)
	{
		final PrintStream		out=System.out;
		final BufferedReader	in=getStdin();
		/*
		if (System.getSecurityManager() == null)
		{
            System.setSecurityManager(new SecurityManager());
            out.println("Initialized security manager");
        }
        */

//		final BaseRMITest		tst=new BaseRMITest();
		final SimpleRMITester		tst=new SimpleRMITester();
		try
		{
			tst.runTest(out, in, args);
		}
		catch(Exception e)
		{
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}

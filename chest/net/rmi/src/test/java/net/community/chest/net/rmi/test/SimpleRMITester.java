/*
 * 
 */
package net.community.chest.net.rmi.test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;

import net.community.chest.net.rmi.RMIUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 25, 2009 11:37:35 AM
 */
public class SimpleRMITester extends BaseRMITester {
	public SimpleRMITester ()
	{
		super();
	}

	public interface RMITester extends Remote {
		void println (String s) throws RemoteException;
	}

	public static class RMITesterServerStub implements RMITester {
		private PrintStream	_out;
		public PrintStream getPrintStream ()
		{
			return _out;
		}

		public void setPrintStream (PrintStream out)
		{
			_out = out;
		}

		public RMITesterServerStub (PrintStream out)
		{
			_out = out;
		}

		public RMITesterServerStub ()
		{
			this(null);
		}
		/*
		 * @see net.community.chest.net.rmi.test.SimpleRMITest.RMITester#println(java.lang.String)
		 */
		@Override
		public void println (String s)
		{
			final PrintStream	out=getPrintStream();
			if (out != null)
				out.println("==> " + s);
			if (isQuit(s))
				System.exit(0);
		}
	}
	/*
	 * @see net.community.chest.net.rmi.test.BaseRMITest#testRMIClient(java.io.PrintStream, java.io.BufferedReader, java.util.List)
	 */
	@Override
	public void testRMIClient (PrintStream out, BufferedReader in, List<String> args) throws Exception
	{
		final Map.Entry<Integer,Registry>	rp=inputRegistry(out, in, 0, args);
		final Registry						r=(null == rp) ? null : rp.getValue();
		if (null == r)
			return;

		final String	n=inputLookupName(out, in, 1, args);
		if (isQuit(n))
			return;

		final RMITester	tester=RMIUtils.lookup(r, RMITester.class, n);
		for ( ; ; )
		{
			final String	s=getval(out, in, "value to send (or Quit)");
			tester.println(s);
			if (isQuit(s)) break;
		}
	}
	/*
	 * @see net.community.chest.net.rmi.test.BaseRMITest#testRMIServer(java.io.PrintStream, java.io.BufferedReader, java.util.List)
	 */
	@Override
	public void testRMIServer (PrintStream out, BufferedReader in, List<String> args) throws Exception
	{
		final Map.Entry<Integer,Registry>	rp=inputRegistry(out, in, 0, args);
		final Registry						r=(null == rp) ? null : rp.getValue();
		if (null == r)
			return;

		final String	n=inputLookupName(out, in, 1, args);
		if (isQuit(n))
			return;

		final Integer	p=rp.getKey();
		final Remote	rem=RMIUtils.ensureBinding(r, (null == p) ? 0 : p.intValue(), n, new RMITesterServerStub(out));
		out.println("registered " + rem + " at port=" + p + " under name=" + n);
	}
}

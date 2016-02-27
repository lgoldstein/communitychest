/*
 * 
 */
package net.community.chest.net.proto.text.ssh.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import net.community.chest.net.proto.text.ssh.SSHAccessor;
import net.community.chest.net.proto.text.ssh.SSHProtocolHandler;
import net.community.chest.net.proto.text.ssh.session.SSHSession;
import net.community.chest.test.TestBase;
import net.community.chest.util.datetime.DateUtil;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 12, 2009 12:59:56 PM
 */
public class SSHTester extends TestBase {
	public static final int testSSHAccessChannels (
			final PrintStream out, final BufferedReader in, final SSHAccessor ssh)
		throws IOException
	{
		if (null == ssh)
			throw new IOException("No SSH accessor provided");

		for ( ; ; )
		{
			final String	ans=getval(out, in, "(E)xec/[S]hell/(C)opy/(Q)uit");
			if (isQuit(ans)) break;

			final char	op=
				((null == ans) || (ans.length() <= 0)) ? '\0' : Character.toUpperCase(ans.charAt(0));
			switch(op)
			{
				case '\0'	:	// default
				case 'S'	:
					break;
				case 'E'	:
					break;
				case 'C'	:
					break;
				default		:	// do nothing
			}
		}

		return 0;
	}

	/*----------------------------------------------------------------------*/

	public static final int testSSHAccess (
			final PrintStream out, final BufferedReader in, final SSHAccessor ssh,
			final String host, final String username, final String password)
		throws IOException
	{
		{
			final long	connStart=System.currentTimeMillis();
			ssh.connect(host);
			final long	connEnd=System.currentTimeMillis(), connDuration=connEnd - connStart;
			out.println("\tConnected to " + host + " in " + connDuration + " msec.");
		}

		{
			final long	lgnStart=System.currentTimeMillis();
			ssh.login(username, password);
			final long	lgnEnd=System.currentTimeMillis(), lgnDuration=lgnEnd - lgnStart;
			out.println("\tLogged in after " + lgnDuration + " msec.");
		}

		return testSSHAccessChannels(out, in, ssh);
	}

	/*----------------------------------------------------------------------*/

	// args[0]=host, args[1]=username, args[2]=password
	public static final int testSSHAccess (
			final PrintStream out, final BufferedReader in, final String ... args)
	{
		final String[]	testArgs=resolveTestParameters(
			out, in, args, "host", "username", "password");
		if ((null == testArgs) || (testArgs.length < 3))
			return 0;

		try
		{
			final SSHAccessor			ssh=new SSHSession();
			final SSHProtocolHandler	hndlr=new TestSSHProtocolHandler(out, in);
			ssh.setProtocolHandler(hndlr);
			ssh.setReadTimeout(30 * DateUtil.MSEC_PER_SECOND);

			try
			{
				return testSSHAccess(out, in, ssh, testArgs[0], testArgs[1], testArgs[2]);
			}
			finally
			{
				ssh.close();
			}
		}
		catch(Exception e)
		{
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			return (-1);
		}
	}
 
	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		testSSHAccess(System.out, getStdin(), args);
	}
}

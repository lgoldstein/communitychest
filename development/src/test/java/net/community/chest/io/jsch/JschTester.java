/*
 * 
 */
package net.community.chest.io.jsch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import net.community.chest.io.file.FileIOUtils;
import net.community.chest.test.TestBase;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 24, 2009 8:43:46 AM
 */
public class JschTester extends TestBase {
	public static final int	IPPORT_SSH=22;

	/* -------------------------------------------------------------------- */

	private static final void runCommands(
			final PrintStream out, final BufferedReader in, final Session session) throws Exception {
		for (byte[]	inBuf=new byte[4096]; ; ) {
			String	cmd=getval(out, in, "CMD(or (q)uit):");
			if (isQuit(cmd)) break;

			final Channel channel=session.openChannel("exec");
			try {
				((ChannelExec)channel).setCommand(cmd);
				channel.setInputStream(null);
				channel.connect(3*1000);
	
				InputStream		inStream=channel.getInputStream();
				while(!channel.isClosed()) {
					while(inStream.available() > 0) {
						int	readLen=inStream.read(inBuf);
						if (readLen <= 0) {
							break;
						}
					
						out.write(inBuf, 0, readLen);
					}
				}

				out.println("exit-status: "+ channel.getExitStatus());
			} finally {
				channel.disconnect();
			}
		}
	}

	/* -------------------------------------------------------------------- */

	private static final void runShell(
			final PrintStream out, final BufferedReader in, final Session session) throws Exception {
		final Channel channel=session.openChannel("shell");
		try {
			final InputStream		inStream=channel.getInputStream();
			Runnable	gobbler=new Runnable() {
					@Override
					public void run ()
					{
						try {
							for (byte[]	inBuf=new byte[4096]; ; ) {
								int	readLen=inStream.read(inBuf);
								if (readLen <= 0) {
									break;
								}
							
								out.write(inBuf, 0, readLen);
							}
						} catch(IOException e) {
							System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
						}
					}
				};
			Thread	t=new Thread(gobbler, "gobbler");

			try {
				OutputStream	outStream=channel.getOutputStream();
				try {
					channel.connect(3*1000);
					out.println("============> Shell initialized <===============");
	
					t.start();
					for ( ; ; ) {
						String	cmd=getval(out, in, ">>:");
						outStream.write(cmd.getBytes());
						outStream.write(0x0a);
						outStream.flush();
		
						if ("exit".equalsIgnoreCase(cmd)) {
							break;
						}
					}
				} finally {
					outStream.close();
				}
			} finally {
				inStream.close();
				t.join(TimeUnit.SECONDS.toMillis(5L));
				if (t.isAlive()) {
					System.err.println("Gobbler still alive !!!");
				}
			}
		} finally {
			channel.disconnect();
		}
	}

	/* -------------------------------------------------------------------- */

	// args[i]=host, args[i+1]=username, args[i+2]=password
	public static final int testShell (
			final PrintStream out, final BufferedReader in, final String ... args)
	{
	    final JSch	jsch=new JSch();
	    JSch.setLogger(new JschTestLogger(out));

		final int	numArgs=(null == args) ? 0 : args.length;
		for (int aIndex=0; ; )
		{
			String	host=null;
			for ( ; (host == null) || (host.length() <= 0); aIndex++)
				host = (aIndex < numArgs) ? args[aIndex] : getval(out, in, "host (or Quit)");
			if (isQuit(host)) break;

			String	user=null;
			for ( ; (user == null) || (user.length() <= 0); aIndex++)
				user = (aIndex < numArgs) ? args[aIndex] : getval(out, in, "user (or Quit)");
			if (isQuit(user)) break;

			String	passwd=null;
			for ( ; (passwd == null) || (passwd.length() <= 0); aIndex++)
				passwd = (aIndex < numArgs) ? args[aIndex] : getval(out, in, "password (or Quit)");
			if (isQuit(passwd)) break;

			try
			{
				final Session session=jsch.getSession(user, host, IPPORT_SSH);
				session.setPassword(passwd);
				//session.setUserInfo(new UserInfoPrompter());
				session.setConfig("StrictHostKeyChecking", "no");
				session.connect(30000);
				
				final Channel channel=session.openChannel("shell");
				//((ChannelShell)channel).setAgentForwarding(true);
				channel.setInputStream(System.in);
				/*
			      // a hack for MS-DOS prompt on Windows.
			      channel.setInputStream(new FilterInputStream(System.in){
			          public int read(byte[] b, int off, int len)throws IOException{
			            return in.read(b, off, (len>1024?1024:len));
			          }
			        });
				 */
				channel.setOutputStream(System.out);
				// Choose the pty-type "vt102".
				//((ChannelShell)channel).setPtyType("vt102");

				// Set environment variable "LANG" as "ja_JP.eucJP".
			    //  ((ChannelShell)channel).setEnv("LANG", "ja_JP.eucJP");
				channel.connect(3*1000);
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + " while access " + user + "@" + host + ": " + e.getMessage());
			}
		}

		return 0;
	}

	/* -------------------------------------------------------------------- */

	// args[i]=host, args[i+1]=username, args[i+2]=key file path
	public static final int testAuthorizedKey (
			final PrintStream out, final BufferedReader in, final String ... args)
	{
	    final JSch	jsch=new JSch();
	    JSch.setLogger(new JschTestLogger(out));
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int aIndex=0; ; )
		{
			String	host=null;
			for ( ; (host == null) || (host.length() <= 0); aIndex++)
				host = (aIndex < numArgs) ? args[aIndex] : getval(out, in, "host (or Quit)");
			if (isQuit(host)) break;

			String	user=null;
			for ( ; (user == null) || (user.length() <= 0); aIndex++)
				user = (aIndex < numArgs) ? args[aIndex] : getval(out, in, "user (or Quit)");
			if (isQuit(user)) break;

			File	keyFile=null;
			for ( ; keyFile == null ; aIndex++) {
				String	path=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "key file path (or Quit)");
				if ((path == null) || (path.length() <= 0)) {
					continue;
				}
				
				if (isQuit(path)) break;
				
				keyFile = new File(path);
				if (keyFile.exists() && keyFile.isFile()) break;

				out.println("Bad file path: " + path);
			}

			if (keyFile == null) break;
			
			try {
				String	prvKey=FileIOUtils.readFileAsString(keyFile);
				jsch.addIdentity(keyFile.getName(), prvKey.getBytes(), null, null);
				// can also use: jsch.addIdentity(keyFile.getAbsolutePath());
				
				final Session session=jsch.getSession(user, host, IPPORT_SSH);
				try {
					session.setConfig("StrictHostKeyChecking", "no");	// avoid UnknownHostKey exception
					session.connect(30000);
					out.println("Session established");
					
					String	ans=getval(out, in, "[S]hell/(E)xec/(Q)uit");
					if (isQuit(ans)) continue;

					final char	tch=((ans == null) || (ans.length() == 0)) ? '\0' : Character.toLowerCase(ans.charAt(0));
					switch(tch) {
						case '\0'	:
						case 's'	:
							runShell(out, in, session);
							break;
						case 'e'	:
							runCommands(out, in, session);
							break;
							
						default	:
					}
				} finally {
					session.disconnect();
				}
			} catch(Exception e) {
				System.err.println(e.getClass().getName() + " while access " + user + "@" + host + ": " + e.getMessage());
			}
		}
		
		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		final BufferedReader	in=getStdin();
//		final int				nErr=testShell(System.out, in, args);
		final int				nErr=testAuthorizedKey(System.out, in, args);
		if (nErr != 0)
			System.err.println("test failed (err=" + nErr + ")");
		else
			System.out.println("OK");
	}
}

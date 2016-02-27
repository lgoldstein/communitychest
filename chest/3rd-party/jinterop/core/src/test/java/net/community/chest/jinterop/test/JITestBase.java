package net.community.chest.jinterop.test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jinterop.dcom.common.IJIAuthInfo;
import org.jinterop.dcom.common.JIDefaultAuthInfoImpl;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIClsid;
import org.jinterop.dcom.core.JIComServer;
import org.jinterop.dcom.core.JIProgId;
import org.jinterop.dcom.core.JISession;

import net.community.chest.Triplet;
import net.community.chest.test.TestBase;
import net.community.chest.util.map.MapEntryImpl;

public class JITestBase extends TestBase {
	protected JITestBase ()
	{
		super();
	}

	// args[0]=domain, args[1]=username, args[2]=password
	// returns null if Quit
	public static final Map.Entry<IJIAuthInfo,List<String>> getAuthenticationParams (
			final PrintStream out, final BufferedReader in, final String ... args)
	{
		final String[]	prompts={ "domain", "username", "password" },
						res=resolveTestParameters(out, in, args, prompts);
		if ((null == res) || (res.length <= 0))
			return null;

		final IJIAuthInfo authInfo=new JIDefaultAuthInfoImpl(res[0], res[1], res[2]);
		final int		numArgs=(null == args) ? 0 : args.length;
		if (numArgs > res.length)
		{
			final List<String>	xArgs=new ArrayList<String>(numArgs - res.length);
			for (int	aIndex=res.length; aIndex < numArgs; aIndex++)
				xArgs.add(args[aIndex]);
			return new MapEntryImpl<IJIAuthInfo,List<String>>(authInfo, xArgs);
		}

		return new MapEntryImpl<IJIAuthInfo,List<String>>(authInfo, null);
	}

	/* -------------------------------------------------------------------- */

	/* Please make sure that you call JISession.destroySession(session) after you are done away with the usage of your session. */
	// args[0]=domain, args[1]=username, args[2]=password
	// returns null if Quit
	public static final Map.Entry<JISession,List<String>> createSession (
			final PrintStream out, final BufferedReader in, final String ... args)
	{
		final Map.Entry<IJIAuthInfo,List<String>>	ap=getAuthenticationParams(out, in, args);
		final IJIAuthInfo							ai=(null == ap) ? null : ap.getKey();
		if (null == ai)
			return null;

		final JISession 	session=JISession.createSession(ai); 
		final List<String>	al=ap.getValue();
		if ((null == al) || (al.size() <= 0))
			return new MapEntryImpl<JISession,List<String>>(session, null);
		else
			return new MapEntryImpl<JISession,List<String>>(session, al);
	}

	/* -------------------------------------------------------------------- */

	/* Please make sure that you call JISession.destroySession(session) after you are done away with the usage of your session. */
	// args: [0]=domain, [1]=username, [2]=password, [3]=server, [4]=prog/cls-id
	// returns null if Quit
	public static final Triplet<JIComServer,JISession,List<String>> connectComServer (
			final PrintStream out, final BufferedReader in, final boolean useProgId, final String ... args) throws UnknownHostException, JIException
	{
		final Map.Entry<JISession,List<String>>	sp=createSession(out, in, args);
		final JISession							session=(null == sp) ? null : sp.getKey();
		if (null == session)
			return null;

		final List<String>		al=sp.getValue();
		final int				numArgs=(null == al) ? 0 : al.size();
		final String[]			prompts={ "server", useProgId ? "prog-ID" : "CLSID" },
								res=
			resolveTestParameters(out, in, (numArgs <= 0) ? null : al.toArray(new String[numArgs]), prompts);
		if ((null == res) || (res.length <= 0))
			return null;
		
		final JIComServer comServer=
			  useProgId
			? new JIComServer(JIProgId.valueOf(res[1]), res[0], session)
			: new JIComServer(JIClsid.valueOf(res[1]), res[0], session)
			;
		for (int	aIndex=res.length; aIndex < numArgs; aIndex++)
			al.remove(0);	// remove used arguments (if any)

		if ((null == al) || (al.size() <= 0))
			return new Triplet<JIComServer,JISession,List<String>>(comServer, session, null);
		else
			return new Triplet<JIComServer,JISession,List<String>>(comServer, session, al);
	}
			
}

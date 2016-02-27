package net.community.chest.net.dns;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.Collection;

import net.community.chest.net.dns.DNSAccess;
import net.community.chest.net.dns.SMTPMxRecord;
import net.community.chest.test.TestBase;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 28, 2007 2:35:56 PM
 */
public class DNSAccessDebugger extends TestBase {

	//////////////////////////////////////////////////////////////////////////

	// args[i]=MX domain to lookup
	private static final int testMxLookup (final DNSAccess	nsa, final PrintStream out, final BufferedReader in, final String[] args)
	{
		for (int	i=0, numArgs=(null == args) ? 0 : args.length; ; i++)
		{
			final String	dmName=(i < numArgs) ? args[i] : getval(out, in, "MX domain");
			if ((null == dmName) || (dmName.length() <= 0))
				continue;
			if (isQuit(dmName)) break;

			try
			{
				final long									mxStart=System.currentTimeMillis();
				final Collection<? extends SMTPMxRecord>	recs=SMTPMxRecord.mxLookup(nsa, dmName);
				final long									mxEnd=System.currentTimeMillis(), mxDuration=(mxEnd - mxStart);
				final int									numRecs=(null == recs) ? 0 : recs.size();
				out.println("Fetched " + numRecs + " records in " + mxDuration + " msec.");
				if (numRecs <= 0)
				{
					out.println("No MX records found");
					continue;
				}
				
				for (final SMTPMxRecord r : recs)
					out.println("\t(" + r + "): " + r.getHost() + "\tpreference=" + r.getPreference());
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
			}
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	// args[i]=IP address to lookup
	private static final int testPtrLookup (final DNSAccess	nsa, final PrintStream out, final BufferedReader in, final String[] args)
	{
		for (int	i=0, numArgs=(null == args) ? 0 : args.length; ; i++)
		{
			final String	addr=(i < numArgs) ? args[i] : getval(out, in, "IP address");
			if ((null == addr) || (addr.length() <= 0))
				continue;
			if (isQuit(addr)) break;

			try
			{
				final long					ptrStart=System.currentTimeMillis();
				final Collection<String>	ptrs=nsa.ptrLookup(addr);
				final long					ptrEnd=System.currentTimeMillis(), ptrDuration=(ptrEnd - ptrStart);
				final int					numRecs=(null == ptrs) ? 0 : ptrs.size();
				out.println("Fetched " + numRecs + " records in " + ptrDuration + " msec.");
				if (numRecs <= 0)
				{
					System.err.println("No attributes returned");
					continue;
				}

				out.println(addr + " names:");
				for (final String p : ptrs)
					out.println("\t" + p);
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
			}
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	// args[i]=name to lookup
	private static final int testALookup (final DNSAccess nsa, final PrintStream out, final BufferedReader in, final String[] args)
	{
		for (int	i=0, numArgs=(null == args) ? 0 : args.length; ; i++)
		{
			final String	addr=(i < numArgs) ? args[i] : getval(out, in, "name");
			if ((null == addr) || (addr.length() <= 0))
				continue;
			if (isQuit(addr)) break;

			try
			{
				final long					ptrStart=System.currentTimeMillis();
				final Collection<String>	al=nsa.aLookup(addr);
				final long					ptrEnd=System.currentTimeMillis(), ptrDuration=(ptrEnd - ptrStart);
				final int					numRecs=(null == al) ? 0 : al.size();
				out.println("Fetched " + numRecs + " records in " + ptrDuration + " msec.");
				if (numRecs <= 0)
				{
					System.err.println("No attributes returned");
					continue;
				}

				out.println(addr + " addresses:");
				for (final String a : al)
					out.println("\t" + a);
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
			}
		}

		return 0;
	}
	
	//////////////////////////////////////////////////////////////////////////

	// args[i]=name to lookup
	private static final int testCnameLookup (final DNSAccess nsa, final PrintStream out, final BufferedReader in, final String[] args)
	{
		for (int	i=0, numArgs=(null == args) ? 0 : args.length; ; i++)
		{
			final String	addr=(i < numArgs) ? args[i] : getval(out, in, "name");
			if ((null == addr) || (addr.length() <= 0))
				continue;
			if (isQuit(addr)) break;

			try
			{
				final long					ptrStart=System.currentTimeMillis();
				final Collection<String>	al=nsa.cnameLookup(addr);
				final long					ptrEnd=System.currentTimeMillis(), ptrDuration=(ptrEnd - ptrStart);
				final int					numRecs=(null == al) ? 0 : al.size();
				out.println("Fetched " + numRecs + " records in " + ptrDuration + " msec.");
				if (numRecs <= 0)
				{
					System.err.println("No attributes returned");
					continue;
				}

				out.println(addr + " names:");
				for (final String a : al)
					out.println("\t" + a);
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
			}
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	// args[i]=name/address to be resolved
	private static final int testInetAddress (final PrintStream out, final BufferedReader in, final String[] args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	aVal=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "host name/address (or (Q)uit)");
			if ((null == aVal) || (aVal.length() <= 0))
				continue;
			if (isQuit(aVal))
				break;

			for ( ; ; )
			{
				final String	oVal=getval(out, in, "resolve " + aVal + " [A]ll/(O)ne/(Q)uit");
				if (isQuit(oVal))
					break;

				final char	oChar=((null == oVal) || (oVal.length() <= 0)) ? '\0' : Character.toUpperCase(oVal.charAt(0));
				try
				{
					switch(oChar)
					{
						case '\0'	:
						case 'A'	:
							{
								final InetAddress[]	aa=InetAddress.getAllByName(aVal);
								final int			numAddrs=(null == aa) ? 0 : aa.length;
								if (numAddrs > 0)
								{
									for (final InetAddress	ia : aa)
									{
										if (ia != null)
											out.println("\t" + ia.getHostAddress());
									}
								}
								else
									out.println("\tNo results");
							}
							break;
						
						case 'O'	:
							{
								final InetAddress	ia=InetAddress.getByName(aVal);
								if (null == ia)
									out.println("\tNo result");
								else
									out.println("\t" + ia.getHostAddress());
							}
							break;

						default		:	// do nothing
					}
				}
				catch(Exception e)
				{
					System.err.println(e.getClass().getName() + ": " + e.getMessage());
				}
			}
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	private static final int testDNSLookup (final PrintStream out, final BufferedReader in, final String tstHost, final String[] args)
	{
		if ("JAVA".equalsIgnoreCase(tstHost))
			return testInetAddress(out, in, args);
			
		final DNSAccess	nsa=new DNSAccess();
		nsa.setServer(tstHost);

		final String	modePrompt="mode (" + DNSAccess.MXAttribute
									  + "/" + DNSAccess.PTRAttribute
									  + "/" + DNSAccess.AAttribute
									  + "/" + DNSAccess.CNAMEAttribute
									  + ")";
		for (int	tIndex=0; ; tIndex++)
		{
			out.println("\tUsing server=" + tstHost);

			String		tstMode=null;
			String[]	realArgs=null;
			if (0 == tIndex)
			{
				if ((args != null) && (args.length > 0))
				{
					tstMode = args[0];

					if (args.length > 1)
					{
						realArgs = new String[args.length-1];
						System.arraycopy(args, 1, realArgs, 0, args.length-1);
					}
				}
			}

			while ((null == tstMode) || (tstMode.length() <= 0))
			{
				tstMode = getval(out, in, modePrompt);
				if (isQuit(tstMode))
					return 0;
			}

			final char	opChar=Character.toUpperCase(tstMode.charAt(0));
			if (opChar == Character.toUpperCase(DNSAccess.MXAttribute.charAt(0)))
				testMxLookup(nsa, out, in, realArgs);
			else if (opChar == Character.toUpperCase(DNSAccess.PTRAttribute.charAt(0)))
				testPtrLookup(nsa, out, in, realArgs);
			else if (opChar == Character.toUpperCase(DNSAccess.AAttribute.charAt(0)))
				testALookup(nsa, out, in, realArgs);
			else if (opChar == Character.toUpperCase(DNSAccess.CNAMEAttribute.charAt(0)))
				testCnameLookup(nsa, out, in, realArgs);
			else
				System.err.println("Unknown option: " + tstMode);
		}
	}

	//////////////////////////////////////////////////////////////////////////

	// arg[0]=DNS server or JAVA for built-in test, arg[1]=PTR/MX/A arg[2,3,...] specific test arguments (if non-Java)
	private static final int testDNSLookup (final PrintStream out, final BufferedReader in, final String[] args)
	{
		for (int tIndex=0; ; tIndex++)
		{
			String 		tstHost=null;
			String[]	realArgs=null;
			if (0 == tIndex)
			{
				if ((args != null) && (args.length > 0))
				{
					tstHost = args[0];

					if (args.length > 1)
					{
						realArgs = new String[args.length-1];
						System.arraycopy(args, 1, realArgs, 0, args.length-1);
					}
				}
			}

			tstHost = getval(out, in, "server (JAVA=local nameserver/ENTER=default/(Q)uit)");
			if (isQuit(tstHost))
				return 0;

			testDNSLookup(out, in, tstHost, realArgs);
		}
	}

	//////////////////////////////////////////////////////////////////////////

	public static final void main (final String args[])
	{
		final BufferedReader	in=getStdin();
		final int				nErr=testDNSLookup(System.out, in, args);
		if (nErr != 0)
			System.err.println("test failed (err=" + nErr + ")");
		else
			System.out.println("OK");
	}
}

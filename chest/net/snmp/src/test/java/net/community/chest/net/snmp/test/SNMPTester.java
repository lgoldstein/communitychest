/*
 * 
 */
package net.community.chest.net.snmp.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import net.community.chest.io.FileUtil;
import net.community.chest.net.snmp.MIBAttributeEntry;
import net.community.chest.net.snmp.MIBGroup;
import net.community.chest.net.snmp.OIDAliasMap;
import net.community.chest.net.snmp.OIDStringsMap;
import net.community.chest.net.snmp.io.MIBEntriesReader;
import net.community.chest.test.TestBase;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 25, 2009 3:04:44 PM
 */
public class SNMPTester extends TestBase {
	protected SNMPTester ()
	{
		super();
	}

	/* -------------------------------------------------------------------- */

	public static final void showTextReplacements (final PrintStream out, final Map<String,String> m)
	{
		final Collection<? extends Map.Entry<String,String>>	pl=
			((null == m) || (m.size() <= 0)) ? null : m.entrySet();
		if ((null == pl) || (pl.size() <= 0))
			return;
		for (final Map.Entry<String,String> ap : pl)
		{
			final String	name=(null == ap) ? null : ap.getKey(),
							value=(null == ap) ? null : ap.getValue();
			out.append('\t').append(name).append(" ==> ").append(value).println();
		}
	}

	/* -------------------------------------------------------------------- */

	public static final void showAliases (final PrintStream out, final Map<String,String> m)
	{
		final Collection<? extends Map.Entry<String,String>>	pl=
			((null == m) || (m.size() <= 0)) ? null : m.entrySet();
		if ((null == pl) || (pl.size() <= 0))
			return;

		final Map<String,String>	oidMap=OIDStringsMap.toOIDMap(m);
		for (final Map.Entry<String,String> ap : pl)
		{
			final String	name=(null == ap) ? null : ap.getKey(),
							oid=(null == ap) ? null : ap.getValue(),
							path=OIDAliasMap.getOIDPath(oid, oidMap);
			out.println("\t[" + name + "](" + oid + ") " + path);
		}
	}

	/* -------------------------------------------------------------------- */

	public static final void showEntries (final PrintStream out, final Collection<? extends MIBAttributeEntry> el)
	{
		if ((el != null) && (el.size() > 0))
		{
			for (final MIBAttributeEntry e : el)
				out.append('\t').append(String.valueOf(e)).println();
		}
	}

	public static final void showEntries (final PrintStream out, final Map<?,? extends MIBAttributeEntry>	eMap)
	{
		showEntries(out, ((null == eMap) || (eMap.size() <= 0)) ? null : eMap.values());
	}
	
	public static final void showEntries (final PrintStream out, final MIBGroup g)
	{
		showEntries(out, (null == g) ? null : g.getEntries());
	}

	//////////////////////////////////////////////////////////////////////////

	public static final OIDAliasMap testMIBEntriesReader (
			final PrintStream out, final BufferedReader in, final OIDAliasMap aMap, final String path)
	{
		OIDAliasMap	m=aMap;
		for (m=aMap; ; m=aMap)
		{
			out.println("Processing " + path);
			MIBEntriesReader	r=null;
			final Collection<MIBAttributeEntry>	el=new LinkedList<MIBAttributeEntry>();	
			try
			{
				Reader	inStream=null;
				try
				{
					inStream = new FileReader(path);

					r = new MIBEntriesReader(m, inStream);
					
					final Collection<MIBAttributeEntry>	cl=r.readMIBEntries(el);
					if (cl != el)
						throw new IllegalStateException("Mismatched entries list");
				}
				finally
				{
					FileUtil.closeAll(r, inStream);
				}

				showEntries(out, el);

				if (((m=r.getAliases()) != null) && (m.size() > 0))
				{
					out.println("aliases");
					showAliases(out, m);
				}

				final Map<String,String>	tm=r.getTextReplacements();
				if ((tm != null) && (tm.size() > 0))
				{
					out.println("Text replacements");
					showTextReplacements(out, tm);
				}
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + ": " + e.getMessage());

				if (r != null)
				{
					final String	ans=getval(out, in, "show intermediate results [y]/n");
					if ((null == ans) || (ans.length() <= 0) || ('y' == Character.toLowerCase(ans.charAt(0))))
					{
						if (el.size() > 0)
						{
							out.println("Entries");
							showEntries(out, el);
						}
	
						final Map<String,String>	tm=r.getTextReplacements();
						if ((tm != null) && (tm.size() > 0))
						{
							out.println("Text replacements");
							showTextReplacements(out, tm);
						}
					}
				}
			}

			final String	ans=getval(out, in, "re-process " + path + " [y]/n");
			if ((null == ans) || (ans.length() <= 0) || ('y' == Character.toLowerCase(ans.charAt(0))))
				continue;
			else
				break;
		}

		final String	ans=getval(out, in, "re-use aliases for next file [y]/n");
		if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y'))
			return null;

		return m;
	}

	// each argument is a MIB file path or an "AnchoredMIBFile" enum value
	public static final void testMIBEntriesReader (
			final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		OIDAliasMap	m=null;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	path=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "MIB file path (or Quit)");
			if ((null == path) || (path.length() <= 0))
				continue;
			if (isQuit(path)) break;

			m = testMIBEntriesReader(out, in, m, path);
		}
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		testMIBEntriesReader(System.out, getStdin(), args);
	}
}

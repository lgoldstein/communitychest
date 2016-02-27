/*
 * 
 */
package net.community.chest.git.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import net.community.chest.git.lib.FileModeType;
import net.community.chest.git.lib.GitlibUtils;
import net.community.chest.git.lib.ref.RefAttributeType;
import net.community.chest.test.TestBase;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.Tree;
import org.eclipse.jgit.lib.TreeEntry;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 14, 2011 10:53:24 AM
 *
 */
public class GitTester extends TestBase {
	public GitTester ()
	{
		super();
	}

	/* -------------------------------------------------------------------- */

	protected static final void showReferences (
		final BufferedReader in, final PrintStream out, final Repository repo, final Map<String,? extends Ref>	refsMap)
	{
		final Collection<? extends Ref>	refsList=
			((refsMap == null) || refsMap.isEmpty()) ? null : refsMap.values();
		if ((refsList == null) || refsList.isEmpty())
		{
			out.println("No references found");
			return;
		}

		for ( ; ; )
		{
			for (final Ref ref : refsList)
			{
				final Map<RefAttributeType,?>								attrsMap=
					RefAttributeType.getAttributes(ref);
				final Collection<? extends Map.Entry<RefAttributeType,?>>	attrsVals=
					((attrsMap == null) || attrsMap.isEmpty()) ? null : attrsMap.entrySet();
				if ((attrsVals == null) || attrsVals.isEmpty())
					continue;

				boolean	aFirst=true;
				for (final Map.Entry<RefAttributeType,?> aEntry : attrsVals)
				{
					final RefAttributeType	aType=(aEntry == null) ? null : aEntry.getKey();
					final Object			aValue=(aEntry == null) ? null : aEntry.getValue();
					if ((aType == null) || RefAttributeType.REF.equals(aType) || (aValue == null))
						continue;

					if (aFirst)
					{
						out.append('\t');
						aFirst = false;
					}
					else
						out.append(',');
					out.append(aType.name()).append('=').append(aValue.toString());

					if (aValue instanceof AnyObjectId)
					{
						final File	f=repo.toFile((AnyObjectId) aValue);
						if (f != null)
							out.append('{').append(f.getAbsolutePath()).append('}');
					}
					
				}
				out.println();
			}

			final String	ans=getval(out, in, "again [y]/n");
			if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y'))
				break;
		}
	}
	/* -------------------------------------------------------------------- */

	protected static final void showRepositoryTags (final BufferedReader in, final PrintStream out, final Repository repo)
	{
		showReferences(in, out, repo, (repo == null) ? null : repo.getTags());
	}

	protected static final void showRepositoryRefs (final BufferedReader in, final PrintStream out, final Repository repo)
	{
		showReferences(in, out, repo, (repo == null) ? null : repo.getAllRefs());
	}

	/* -------------------------------------------------------------------- */

	protected static final void doFindFile (final BufferedReader in, final PrintStream out, final Tree tree)
	{
		for ( ; ; )
		{
			final String	subPath=getval(out, in, "file relative path (or Quit)");
			if (isQuit(subPath)) break;

			final TreeEntry	entry;
			try
			{
				entry = GitlibUtils.findEntry(tree, subPath);
			}
			catch(IOException e)
			{
				System.err.println("Failed (" + e.getClass().getName() + "): " + e.getMessage());
				continue;
			}

			if (entry != null)
				out.append('\t')
				   .append(entry.getFullName())
				   .append(" - ")
				   .append(String.valueOf(FileModeType.fromMode(entry.getMode())))
				   .println()
				   ;
			else
				out.append("No match found.");
		}
	}

	/* -------------------------------------------------------------------- */

	protected static final void showTree (
			final BufferedReader in, final PrintStream out, final Tree tree)
	 	throws IOException
    {
		final TreeEntriesDisplayer	visitor=new TreeEntriesDisplayer(in, out);
		for ( ; ; )
		{
			final String	op=getval(out, in, "[L]ist/(F)ind/(Q)uit");
			if (isQuit(op)) break;

			final char	opchar=
				((op == null) || (op.length() <= 0)) ? '\0' : Character.toUpperCase(op.charAt(0));
			switch(opchar)
			{
				case '\0'	:
				case 'L'	:
					tree.accept(visitor);
					break;

				case 'F'	:
					doFindFile(in, out, tree);
					break;

				default		: // ignored
			}
		}
    }

	protected static final void showTree (
			final BufferedReader in, final PrintStream out, final Repository repo, final ObjectId objId)
	 	throws IOException
	{
		if ((repo == null) || (objId == null))
			return;
		
		showTree(in, out, repo.mapTree(objId));
	}

	protected static final void showTree (
			final BufferedReader in, final PrintStream out, final Repository repo, final Ref ref)
	 	throws IOException
	{
		if ((repo == null) || (ref == null))
			return;

		showTree(in, out, repo, ref.getObjectId());
	}

	protected static final void showTree (
			final BufferedReader in, final PrintStream out, final Repository repo, final String name)
	 	throws IOException
	 {
		if ((repo == null) || (name == null) || (name.length() <= 0))
			return;
		showTree(in, out, repo, repo.getRef(name));
	 }
	/* -------------------------------------------------------------------- */

	protected static final void testRepository (
			final BufferedReader in, final PrintStream out, final Repository repo)
		throws IOException
	{
		out.append(repo.toString())
		   .append(": ").append(repo.getBranch())
		   .append('[').append(repo.getFullBranch()).append(']')
		   .println()
		   ;

		for ( ; ; )
		{
			final String	op=getval(out, in, "[L]ist refs/(S)how Tree/List (T)ags/(Q)uit");
			if (isQuit(op)) break;

			final char	opchar=
				((op == null) || (op.length() <= 0)) ? '\0' : Character.toUpperCase(op.charAt(0));
			switch(opchar)
			{
				case '\0'	:
				case 'L'	:
					showRepositoryRefs(in, out, repo);
					break;

				case 'T'	:
					showRepositoryTags(in, out, repo);
					break;

				case 'S'	:
					{
						final String	refName=getval(out, in, "ref. name [ENTER=" + Constants.HEAD + "]/(Q)uit");
						if (isQuit(refName)) break;
						showTree(in, out, repo, ((refName == null) || (refName.length() <= 0)) ? Constants.HEAD : refName);
					}
					break;

				default		:	// ignored
			}
		}
	}

	protected static final void testRepository (
			final BufferedReader in, final PrintStream out, final String ... args)
		throws IOException
	{
		final String[]	tstArgs=resolveTestParameters(out, in, args, "GIT controlled location");
		if (tstArgs == null)
			return;

		final Repository	repo=new Repository(null, new File(tstArgs[0]));
		try
		{
			testRepository(in, out, repo);
		}
		finally
		{
			repo.close();
		}
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		try
		{
			testRepository(getStdin(), System.out, args);
		}
		catch(Throwable t)
		{
			System.err.append("Failed (").append(t.getClass().getName()).append("): ").println(t.getMessage());
			t.printStackTrace();
		}
	}
}

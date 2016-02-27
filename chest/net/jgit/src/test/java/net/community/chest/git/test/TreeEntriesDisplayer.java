/*
 * 
 */
package net.community.chest.git.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import net.community.chest.git.lib.FileModeType;
import net.community.chest.git.lib.GitlibUtils;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.FileTreeEntry;
import org.eclipse.jgit.lib.GitlinkTreeEntry;
import org.eclipse.jgit.lib.SymlinkTreeEntry;
import org.eclipse.jgit.lib.Tree;
import org.eclipse.jgit.lib.TreeEntry;
import org.eclipse.jgit.lib.TreeVisitor;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 15, 2011 10:25:50 AM
 *
 */
public class TreeEntriesDisplayer implements TreeVisitor {
	private final BufferedReader _in;
	public final BufferedReader getStdin ()
	{
		return _in;
	}

	private final PrintStream	_out;
	public final PrintStream getStdout ()
	{
		return _out;
	}

	private final StringBuilder	_indent;
	public final StringBuilder getIndent ()
	{
		return _indent;
	}

	public TreeEntriesDisplayer (final BufferedReader in, final PrintStream out)
	{
		_in = in;
		_out = out;
		_indent = new StringBuilder().append('\t');
	}

	protected PrintStream appendEntryData (TreeEntry entry)
	{
		final StringBuilder	indent=getIndent();
		final PrintStream	out=getStdout();
		final FileMode		mode=entry.getMode();
		final FileModeType	type=FileModeType.fromMode(mode);
		final File			loc=GitlibUtils.getTreeEntryLocation(entry);
		return out.append(indent.toString())
		   		  .append(entry.getName())
		   		  .append('{').append(entry.isModified() ? 'M' : '-').append('}')
		   		  .append('[').append(String.valueOf(loc)).append(']')
		   		  .append(" - mode=").append(String.valueOf(type))
		   		  ;
	}
	/*
	 * @see org.eclipse.jgit.lib.TreeVisitor#startVisitTree(org.eclipse.jgit.lib.Tree)
	 */
	@Override
	public void startVisitTree (Tree t) throws IOException
	{
		final StringBuilder	indent=getIndent();
		appendEntryData(t).println();
		indent.append('\t');
	}
	/*
	 * @see org.eclipse.jgit.lib.TreeVisitor#endVisitTree(org.eclipse.jgit.lib.Tree)
	 */
	@Override
	public void endVisitTree (Tree t) throws IOException
	{
		final StringBuilder	indent=getIndent();
		final int			curLen=indent.length();
		if (curLen > 0)
			indent.setLength(curLen - 1);
	}
	/*
	 * @see org.eclipse.jgit.lib.TreeVisitor#visitFile(org.eclipse.jgit.lib.FileTreeEntry)
	 */
	@Override
	public void visitFile (FileTreeEntry f) throws IOException
	{
		appendEntryData(f).println();
	}
	/*
	 * @see org.eclipse.jgit.lib.TreeVisitor#visitSymlink(org.eclipse.jgit.lib.SymlinkTreeEntry)
	 */
	@Override
	public void visitSymlink (SymlinkTreeEntry s) throws IOException
	{
		appendEntryData(s).println();
	}
	/*
	 * @see org.eclipse.jgit.lib.TreeVisitor#visitGitlink(org.eclipse.jgit.lib.GitlinkTreeEntry)
	 */
	@Override
	public void visitGitlink (GitlinkTreeEntry s) throws IOException
	{
		appendEntryData(s).println();
	}
}

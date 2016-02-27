/*
 * 
 */
package net.community.apps.tools.svn;

import net.community.apps.common.BaseMain;
import net.community.apps.common.resources.BaseAnchor;
import net.community.chest.svnkit.SVNAccessor;

import org.tmatesoft.svn.core.wc.SVNClientManager;

/**
 * <P>Copyright as per GPLv2</P>
 * @param <A> Type of {@link BaseAnchor}
 * @param <F> Type of {@link SVNBaseMainFrame}
 * @author Lyor G.
 * @since Aug 19, 2010 11:33:12 AM
 *
 */
public abstract class SVNBaseMain<A extends BaseAnchor, F extends SVNBaseMainFrame<A>> extends BaseMain {
	protected SVNBaseMain (String... args)
	{
		super(args);
	}

	private static final SVNAccessor	_svnAcc=new SVNAccessor();
	public static final SVNAccessor getSVNAccessor ()
	{
		return _svnAcc;
	}

	public static final SVNClientManager getSVNClientManager (boolean createIfNotExist)
	{
		final SVNAccessor	acc=getSVNAccessor();
		return (null == acc) ? null : acc.getSVNClientManager(createIfNotExist);
	}

	protected int processArgument (
			final F f, final String a, final int oIndex, final int numArgs, final String ... args)
	{
		int	aIndex=oIndex;
		if ("-u".equals(a) || "--user".equals(a))
		{
			aIndex++;
			final SVNAccessor	acc=getSVNAccessor();
			acc.setUsername(resolveStringArg(a, args, numArgs, aIndex, acc.getUsername()));
		}
		else if ("-p".equals(a) || "--password".equals(a))
		{
			aIndex++;
			final SVNAccessor	acc=getSVNAccessor();
			acc.setPassword(resolveStringArg(a, args, numArgs, aIndex, acc.getPassword()));
		}
		else
			aIndex = (f == null) ? Integer.MIN_VALUE : (-1);

		return aIndex;
	}

	public F processMainArgs (final F f, final String ... args)
	{
		final int			numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; aIndex < numArgs; aIndex++)
		{
			final String	a=args[aIndex];
			final int		nIndex=processArgument(f, a, aIndex, numArgs, args);
			if (nIndex < aIndex)
				throw new UnsupportedOperationException("Unknown command line option: " + a);
			aIndex = nIndex;
		}

		return f;
	}
}

/*
 * 
 */
package net.community.chest.git.lib.ref;

import java.util.List;

import net.community.chest.git.lib.GitlibUtils;
import net.community.chest.lang.StringUtil;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.ObjectIdRef.PeeledTag;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 16, 2011 11:48:44 AM
 */
public final class RefUtils {
	private RefUtils ()
	{
		// no instance
	}
	/**
	 * @param ref The {@link Ref}-erence instance
	 * @return The &quot;pure&quot; name without any leading '/'
	 */
	public static final String stripRefPath (final Ref ref)
	{
		return (ref == null) ? null : stripRefPath(ref.getName());
	}
	/**
	 * @param name Original {@link Ref}-erence name
	 * @return The &quot;pure&quot; name without any leading '/'
	 */
	public static final String stripRefPath (final String name)
	{
		final int	lastPos=((name == null) || (name.length() <= 1)) ? (-1) : name.lastIndexOf(GitlibUtils.GITPATH_SEPCHAR);
		if (lastPos < 0)
			return name;
		else
			return name.substring(lastPos + 1);
	}
	/**
	 * @param ref The {@link Ref}-erence instance
	 * @return TRUE If the reference represents a tag
	 */
	public static final boolean isTagReference (final Ref ref)
	{
		return (ref instanceof PeeledTag);
	}
	/**
	 * @param ref The {@link Ref}-erence instance
	 * @return A {@link List} of the {@link Ref#getName()} components split
	 * according to the {@link GitlibUtils#GITPATH_SEPCHAR}
	 */
	public static final List<String> getRefPathComponents (final Ref ref)
	{
		return (ref == null) ? null : getRefPathComponents(ref.getName());
	}
	/**
	 * @param name A {@link Ref#getName()} result
	 * @return A {@link List} of the {@link Ref#getName()} components split
	 * according to the {@link GitlibUtils#GITPATH_SEPCHAR}
	 */
	public static final List<String> getRefPathComponents (final String name)
	{
		return StringUtil.splitString(name, GitlibUtils.GITPATH_SEPCHAR);
	}
	/**
	 * @param ref A {@link Ref}-erence instance
	 * @return A &quot;long&quot; name for the branch - i.e., one where the
	 * known standard name prefixes have been left as-is
	 * @see #getBranchName(Ref, boolean)
	 * @see #adjustBranchName(String, boolean)
	 */
	public static final String getFullBranchName (final Ref ref)
	{
		return getBranchName(ref, true);
	}
	/**
	 * @param ref A {@link Ref}-erence instance
	 * @return A &quot;short&quot; name for the branch - i.e., one where the
	 * known standard name prefixes have been stripped
	 * @see #getBranchName(Ref, boolean)
	 * @see #adjustBranchName(String, boolean)
	 */
	public static final String getShortBranchName (final Ref ref)
	{
		return getBranchName(ref, false);
	}
	/**
	 * @param ref A {@link Ref}-erence instance
	 * @param fullName If <code>true</code> then no &quot;peeling&quot; of the
	 * known prefixes is executed.
	 * @return The branch name - <code>null</code> if cannot be determined
	 * @see #adjustBranchName(String, boolean)
	 */
	public static final String getBranchName (final Ref ref, final boolean fullName)
	{
		// see Repository#getFullBranchName/getBranchName
		if (ref == null)
			return null;

		if (ref.isSymbolic())
		{
			final Ref	target=ref.getTarget();
			if (target == null)
				return null;
			else
				return adjustBranchName(target.getName(), fullName);
		}

		final ObjectId	id=ref.getObjectId();
		if (id != null)
			return adjustBranchName(id.name(), fullName);

		return null;
	}
	/**
	 * Trims (if required) &quot;known&quot; reference prefixes 
	 * @param name The original branch name
	 * @param fullName If <code>true</code> then no &quot;peeling&quot; of the
	 * known prefixes is executed.
	 * @return The adjusted branch name - may be same as input if no peeling
	 * @see #getBranchName(Ref, boolean)
	 */
	public static final String adjustBranchName (final String name, final boolean fullName)
	{
		// see Repository#shortenRefName
		if ((name == null) || (name.length() <= 0) || fullName)
			return name;
		
		if (name.startsWith(Constants.R_HEADS))
			return name.substring(Constants.R_HEADS.length());
		else if (name.startsWith(Constants.R_TAGS))
			return name.substring(Constants.R_TAGS.length());
		else if (name.startsWith(Constants.R_REMOTES))
			return name.substring(Constants.R_REMOTES.length());
		else
			return name;
	}
}

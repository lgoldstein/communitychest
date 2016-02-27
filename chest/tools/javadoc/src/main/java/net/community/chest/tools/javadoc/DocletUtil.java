package net.community.chest.tools.javadoc;

import java.io.File;

import com.sun.javadoc.Doc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 16, 2007 11:15:26 AM
 */
public final class DocletUtil {
	private DocletUtil ()
	{
		// no instance
	}
	/**
	 * @param pkgName dot-separate package name - may NOT be null/empty
	 * @return sub-path for the expected location of (source) classes for
	 * this package 
	 */
	public static final String getPackageSubPath (final String pkgName)
	{
		final int	pnLen=(null == pkgName) ? 0 : pkgName.length();
		if (pnLen <= 0)
			return null;	// should not happen
		final StringBuilder	sb=new StringBuilder(pnLen);

		int	lastPos=0, pIndex=0;
		for (int	dotPos=pkgName.indexOf('.', lastPos);
			 (dotPos > lastPos) && (dotPos < pnLen);
			 lastPos=dotPos + 1, dotPos=pkgName.indexOf('.', lastPos), pIndex++)
		{
			final String	subName=pkgName.substring(lastPos, dotPos);
			if (pIndex > 0)
				sb.append(File.separatorChar);

			sb.append(subName);
		}

		if (lastPos >= pnLen)
			return null;

		if (pIndex > 0)
			sb.append(File.separatorChar);

		sb.append(pkgName.substring(lastPos));
		return sb.toString();
	}
	/**
	 * @param pd package information - if null then null/empty result
	 * @return sub-path for the expected location of (source) classes for
	 * this package 
	 */
	public static final String getPackageSubPath (final PackageDoc pd)
	{
		return (null == pd) ? null : getPackageSubPath(pd.name()); 
	}
	/**
	 * @param cd program element - if null then null/empty result
	 * @return sub-path for the expected location of (source) classes for
	 * this package 
	 */
	public static final String getPackageSubPath (final ProgramElementDoc cd)
	{
		return (null == cd) ? null : getPackageSubPath(cd.containingPackage());
	}
	/**
	 * @param srcDir source root to which package(s) sub-folder(s)
	 * are expected - may NOT be null
	 * @param pd package descriptor - may NOT be null
	 * @param name class name ("pure" unqualified) - may NOT be null/empty
	 * @return expected location - null/empty if error 
	 */
	public static final String getSourceFilePath (final File srcDir, final PackageDoc pd, final String name)
	{
		if ((null == srcDir) || (null == pd)
		 || (null == name) || (name.length() <= 0))
			return null;

		final String	sp=getPackageSubPath(pd);
		if ((null == sp) || (sp.length() <= 0))
			return null;

		return srcDir.getAbsolutePath()
			+ File.separator + sp
			+ File.separator + name
			+ ".java"
			;
	}
	/**
	 * @param srcDir source directory relative to which package(s) sub-folder(s)
	 * are expected - may NOT be null
	 * @param cd program element - may NOT be null
	 * @return expected location - null/empty if error 
	 */
	public static final String getSourceFilePath (final File srcDir, final ProgramElementDoc cd)
	{
		if ((null == srcDir) || (null == cd))
			return null;
		else
			return getSourceFilePath(srcDir, cd.containingPackage(), cd.name());
	}
	/**
	 * @param cd class (element) information - may NOT be null
	 * @return {@link File} object representing the source file path for
	 * the requested class (null if error)
	 */
	public static final File getSourceFilePath (final ProgramElementDoc cd)
	{
		final SourcePosition	sp=(null == cd) /* should not happen */ ? null : cd.position();
		return (null == sp) /* should not happen */ ? null : sp.file();
	}
	/**
	 * @param rawComment comment with (optional) "return/param" tags
	 * @return comment with "return/param" tags removed - null/empty if null/emptu comment
	 */
	public static final String removeCommentTags (final String rawComment)
	{
		final int	rcLen=(null == rawComment) ? 0 : rawComment.length();
		if (rcLen <= 0)
			return rawComment;

		StringBuilder	sb=null;
		int				lastPos=0;
		for (int	curPos=rawComment.indexOf('@'); (curPos >= lastPos) && (curPos < rcLen); curPos=rawComment.indexOf('@', lastPos))
		{
			if (curPos > lastPos)
			{
				final String	prevText=rawComment.substring(lastPos, curPos);
				if (null == sb)
					sb = new StringBuilder(rcLen);
				sb.append(prevText);
			}

			// find end of '@' tag - skip till first character
			for (lastPos=curPos, curPos++; curPos < rcLen; curPos++)
				if (' ' == rawComment.charAt(curPos))
					break;

			final String	tagName=rawComment.substring(lastPos, curPos);
			// for non-"return/param" tags set value to "@tag=value"
			if ((!"@return".equals(tagName)) && (!"@param".equalsIgnoreCase(tagName)))
			{
				if (null == sb)
					sb = new StringBuilder(rcLen);
				sb.append(tagName);
				sb.append('=');
			}

			if ((lastPos=curPos+1) >= rcLen)
				break;
		}

		if (sb != null)	// check if anything replaced
		{
			if (lastPos < rcLen)
				sb.append(rawComment.substring(lastPos));
			return sb.toString().trim();
		}

		// this location is reached if no '@' replaced
		return rawComment.trim();
	}
	/**
	 * @param d document element whose comment tags we want to remove - if
	 * null then null returned
	 * @return comment with "return/param" tags removed - null/empty if no such tags and
	 * no document element
	 */
	public static final String removeCommentTags (final Doc d)
	{
		return (null == d) ? null : removeCommentTags(d.getRawCommentText());
	}

	public static final String	AMP_REPLACEMENT="&amp;";
	/**
	 * Replaces all '&' signs with "&amp;" if not already such
	 * @param src original source string - may be null/empty
	 * @return replace string - may be same as input if nothing replaced
	 */
	public static final String replaceAmpersand (final String src)
	{
		final int	srcLen=(null == src) ? 0 : src.length();
		if (srcLen <= 0)
			return src;

		StringBuilder	sb=null;
		int				curPos=0;
		for (int	nextPos=src.indexOf('&'); (nextPos >= curPos) && (nextPos < srcLen); )
		{
			final int	remLen=(srcLen - nextPos);
			// if already replaced do nothing
			if ((remLen >= AMP_REPLACEMENT.length()) && src.regionMatches(true, nextPos, AMP_REPLACEMENT, 0, AMP_REPLACEMENT.length()))
			{
				nextPos = src.indexOf('&', nextPos + AMP_REPLACEMENT.length());
				continue;
			}

			if (null == sb)
				sb = new StringBuilder(srcLen);

			// add the text preceding the '&'
			if (curPos < nextPos)
			{
				final String	clrText=src.substring(curPos, nextPos);
				sb.append(clrText);
			}

			sb.append(AMP_REPLACEMENT);

			if ((curPos=(nextPos+1)) >= srcLen)
				break;

			nextPos = src.indexOf('&', curPos);
		}

		if ((null == sb) || (sb.length() <= 0))
			return src;	// OK if nothing replaced

		// check if any "leftovers"
		if (curPos < srcLen)
		{
			final String	lastData=src.substring(curPos);
			if ((lastData != null) && (lastData.length() > 0))	// should not be otherwise
				sb.append(lastData);
		}

		return sb.toString();
	}
	/**
	 * @param aDesc description string - may be null/empty
	 * @return description with CR/LF/TAB replaced by single space (null/empty
	 * if originally null/empty) and with double-quote(s) replaced
	 * by single one(s)
	 */
	public static final String compactDescription (final String aDesc)
	{
		String	retVal=aDesc;

		// replace whitespace with SPACE
		if ((retVal != null) && (retVal.length() > 0))
			retVal = retVal.replaceAll("[\t\r\n\b\f]", " ").trim();

		// replace the double quote with single
		if ((retVal != null) && (retVal.length() > 0))
			retVal = retVal.replace('"', '\'').trim();

		// replace multiple spaces with single one
		if ((retVal != null) && (retVal.length() > 0))
			retVal = retVal.replaceAll("[ ]+", " ").trim();

		// NOTE !!! this replacement MUST be first so as NOT to replace the '&' in '&lt;' 
		if ((retVal != null) && (retVal.length() > 0))
			retVal = replaceAmpersand(retVal).trim();

		if ((retVal != null) && (retVal.length() > 0))
			retVal = retVal.replaceAll("<", "&lt;").trim();

		if ((retVal != null) && (retVal.length() > 0))
			retVal = retVal.replaceAll(">", "&gt;").trim();
	
		return retVal;
	}
	/**
	 * @param d method element - if null then null string returned
	 * @return description with all tags stripped, CR/LF/TAB replaced by single
	 * space (null/empty if originally null/empty) and with double-quote(s)
	 * replaced by single one(s)
	 */
	public static final String compactAttributeDescription (final Doc d)
	{
		return compactDescription(DocletUtil.removeCommentTags(d));
	}
	/**
	 * @param d method element - if null then null string returned
	 * @return description with all "param" tags stripped, CR/LF/TAB replaced by single
	 * space (null/empty if originally null/empty) and with double-quote(s)
	 * replaced by single one(s)
	 */
	public static final String compactOperationDescription (final Doc d)
	{
		if (null == d)
			return null;

		final String	dComment=d.commentText();
		final int		dcLen=(null == dComment) ? 0 : dComment.length();
		final Tag[]		dTags=d.tags();
		final int		numTags=(null == dTags) ? 0 : dTags.length;
		if ((dcLen <= 0) && (numTags <= 0))
			return null;	// nothing to do if no comment and/or tags

		StringBuilder	sb=null;
		if (dcLen > 0)
		{
			sb = new StringBuilder(dcLen + 4 + Math.max(0, numTags) * 32);
			sb.append(dComment).append("\r\n");
		}

		for (final Tag t : dTags)
		{
			if ((null == t) /* should not happen */
			 || (t instanceof ParamTag) // skip @param tags - they are part of the parameter description
			 || isTagExtension(t)) // skip extension tags - assume they are handled separately
				continue;	

			final String	ttx=t.text();
			final int		ttxLen=(null == ttx) ? 0 : ttx.length();
			if (ttxLen <= 0)
				continue;	// should not happen

			final String	ktx=t.kind();
			final int		ktxLen=(null == ktx) /* should not happen */ ? 0 : ktx.length();
			if (null == sb)
				sb = new StringBuilder(Math.max(0, ktxLen) + 1 + ttxLen + 4);
			if (ktxLen > 0)
				sb.append(ktx).append(' ');
			sb.append(ttx).append("\r\n");
		}

		if ((null == sb) || (sb.length() <= 0))
			return null;

		return compactDescription(sb.toString());
	}
	/**
	 * Checks if a given tag (name) is an "extension" - i.e., if its name
	 * contains "sub-components" - e.g. <I>jmx.operation</I>, <I>snmp.attribute</I>
	 * @param name tag name to be checked if "extension"
	 * @param hasStartDelim if TRUE then name string must also start with '@'
	 * @return TRUE if non-null/empty and is extended tag name (including start with
	 * '@' if so signaled)
	 */
	public static final boolean isTagExtension (final String name, final boolean hasStartDelim)
	{
		final int	nLen=(null == name) ? 0 : name.length(),
					dPos=(nLen <= 2) /* minimum is "a.b" */ ? (-1) : name.indexOf('.');
		if ((dPos <= 0) // cannot start with '.'
		 || (dPos >= (nLen-1))	// cannot end with '.'
		 || (hasStartDelim && (name.charAt(0) != '@')))	// check if needs to start with a '@' 
		 return false;

		return true;
	}
	/**
	 * @param t {@link Tag} to be checked if it is an extension
	 * @return TRUE if non-null and tag name contains a "sub-component"
	 */
	public static final boolean isTagExtension (final Tag t)
	{
		return (null == t) ? false : isTagExtension(t.name(), true);
	}
}

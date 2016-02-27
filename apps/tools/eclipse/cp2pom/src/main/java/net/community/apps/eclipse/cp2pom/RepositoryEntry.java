/*
 * 
 */
package net.community.apps.eclipse.cp2pom;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.xml.parsers.ParserConfigurationException;

import net.community.chest.apache.maven.helpers.BuildTargetFile;
import net.community.chest.dom.DOMUtils;
import net.community.chest.eclipse.classpath.ClasspathUtils;
import net.community.chest.eclipse.wst.WstUtils;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 27, 2009 10:16:44 AM
 */
public class RepositoryEntry extends BuildTargetFile {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5444254311631050903L;
	private static final LoggerWrapper	_logger=WrapperFactoryManager.getLogger(RepositoryEntry.class);

	public RepositoryEntry ()
	{
		super();
	}

	public RepositoryEntry (Element elem) throws Exception
	{
		super(elem);
	}

	public static final RepositoryEntry fromClasspathEntry (
					final String repoPrefix, final Element elem)
		throws IllegalArgumentException
	{
		final int	pLen=(null == repoPrefix) ? 0 : repoPrefix.length();
		if (pLen <= 0)
			throw new IllegalArgumentException("fromClasspathEntry(" + DOMUtils.toString(elem) + ") no repository prefix provided");

		final String	tagName=(null == elem) ? null : elem.getTagName();
		if (!ClasspathUtils.CLASSPATHENTRY_ELEM_NAME.equalsIgnoreCase(tagName))
			return null;	// we are interested only in classpath entries

		final String	kVal=elem.getAttribute(ClasspathUtils.CLASSPATHENTRY_KIND_ATTR);
		if (!ClasspathUtils.VAR_ENTRY_KIND.equalsIgnoreCase(kVal))
			return null;	// we are interested only in 'var' type entries

		final String	ePath=elem.getAttribute(ClasspathUtils.CLASSPATHENTRY_PATH_ATTR);
		final int		epLen=(null == ePath) ? 0 : ePath.length();
		if (epLen <= pLen)	// ignore if not starting with the repository prefix
			return null;

		if (!StringUtil.startsWith(ePath, repoPrefix, true, false))
			return null;	// ignore if not starting with the repository prefix

		final String	aPath;
		if (repoPrefix.charAt(pLen-1) != '/')
		{
			if (ePath.charAt(pLen) != '/')
				throw new IllegalArgumentException("fromClasspathEntry(" + DOMUtils.toString(elem) + ")[" + repoPrefix + "] invalid path prefix in " + ePath); 

			aPath = ePath.substring(pLen + 1 /* skip separator */);
		}
		else
			aPath = ePath.substring(pLen);

		return BuildTargetFile.updateRelativeArtifactPath(new RepositoryEntry(), aPath, '/');
	}

	public static final Collection<RepositoryEntry> loadClasspathEntries (
			final String  repoPrefix, final File f, final boolean verbose, final Collection<? extends Element> el)
	{
		if ((null == el) || (el.size() <= 0))
			return null;

		Collection<RepositoryEntry>	ret=null;
		for (final Element elem : el)
		{
			try
			{
				final RepositoryEntry	re=fromClasspathEntry(repoPrefix, elem);
				if (null == re)
					continue;

				if (null == ret)
					ret = new LinkedList<RepositoryEntry>();
				ret.add(re);

				if (verbose)
					_logger.info("loadClasspathEntries(" + f + ")[" + DOMUtils.toString(elem) + "] " + re);
			}
			catch(RuntimeException e)
			{
				_logger.error("loadClasspathEntries(" + f + ")[" + DOMUtils.toString(elem) + "] " + e.getClass().getName() + ": " + e.getMessage());
			}
 		}

		return ret;
	}

	public static final RepositoryEntry fromWebDependencyModule (
			final String repoPrefix, final Element elem)
	{
		final int	pLen=(null == repoPrefix) ? 0 : repoPrefix.length();
		if (pLen <= 0)
			throw new IllegalArgumentException("fromClasspathEntry(" + DOMUtils.toString(elem) + ") no repository prefix provided");

		final String	tagName=(null == elem) ? null : elem.getTagName();
		if (!WstUtils.DEPMODULE_ELEM_NAME.equalsIgnoreCase(tagName))
			return null;	// we are interested only in classpath entries

		final String	hndlVal=elem.getAttribute(WstUtils.DEPMODULE_HANDLE_ATTR);
		if (!StringUtil.startsWith(hndlVal, WstUtils.VARVALUE_HANDLE_VALUE_PREFIX, true, false))
			return null;

		final String	ePath=hndlVal.substring(WstUtils.VARVALUE_HANDLE_VALUE_PREFIX.length());
		if ((null == ePath) || (ePath.length() <= pLen))
			return null;

		final String	aPath;
		if (repoPrefix.charAt(pLen-1) != '/')
		{
			if (ePath.charAt(pLen) != '/')
				throw new IllegalArgumentException("fromWebDependencyModule(" + DOMUtils.toString(elem) + ")[" + repoPrefix + "] invalid path prefix in " + ePath); 

			aPath = ePath.substring(pLen + 1 /* skip separator */);
		}
		else
			aPath = ePath.substring(pLen);

		return BuildTargetFile.updateRelativeArtifactPath(new RepositoryEntry(), aPath, '/');
	}

	public static final Collection<RepositoryEntry> fromWebModuleEntry (
			final Collection<RepositoryEntry>		org,
			final String  							repoPrefix,
			final File								f,
			final boolean 							verbose,
			final Element							root)
	{
		final String	tagName=(null == root) ? null : root.getTagName();
		if (!WstUtils.WEB_MODULE_ELEM_NAME.equalsIgnoreCase(tagName))
			throw new NoSuchElementException("Unknown/unexpected tag name: got="  + tagName + "/expected=" + WstUtils.WEB_MODULE_ELEM_NAME);

		final Collection<? extends Element>	el=
			DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
		if ((null == el) || (el.size() <= 0))
			return org;

		Collection<RepositoryEntry>	ret=org;
		for (final Element elem : el)
		{
			try
			{
				final RepositoryEntry	re=fromWebDependencyModule(repoPrefix, elem);
				if (null == re)
					continue;

				if (null == ret)
					ret = new LinkedList<RepositoryEntry>();
				ret.add(re);

				if (verbose)
					_logger.info("loadWstEntries(" + f + ")[" + DOMUtils.toString(elem) + "] " + re);
			}
			catch(RuntimeException e)
			{
				_logger.error("loadWstEntries(" + f + ")[" + DOMUtils.toString(elem) + "] " + e.getClass().getName() + ": " + e.getMessage());
			}
		}

		return ret;
	}		

	public static final Collection<RepositoryEntry> loadWstEntries (
			final String  repoPrefix, final File f, final boolean verbose, final Collection<? extends Element> el)
	{
		if ((null == el) || (el.size() <= 0))
			return null;

		Collection<RepositoryEntry>	ret=null;
		for (final Element elem : el)
		{
			try
			{
				ret = fromWebModuleEntry(ret, repoPrefix, f, verbose, elem);
			}
			catch(RuntimeException e)
			{
				_logger.error("loadWstEntries(" + f + ")[" + DOMUtils.toString(elem) + "] " + e.getClass().getName() + ": " + e.getMessage());
			}
 		}

		return ret;
	}

	public static final Collection<RepositoryEntry> loadEntries (
			final String  repoPrefix, final File f, final boolean isCPFile, final boolean verbose)
		throws ParserConfigurationException, SAXException, IOException
	{
		final Document						doc=DOMUtils.loadDocument(f);
		final Element						root=doc.getDocumentElement();
		final Collection<? extends Element>	el=
			DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);

		return isCPFile
			 ? loadClasspathEntries(repoPrefix, f, verbose, el)
			 : loadWstEntries(repoPrefix, f, verbose, el)
			 ;
	}
}

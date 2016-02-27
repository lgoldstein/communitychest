/*
 * 
 */
package net.community.apps.apache.maven.pom2cpsync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.apache.maven.helpers.BaseTargetDetails;
import net.community.chest.apache.maven.helpers.BuildTargetFile;
import net.community.chest.dom.DOMUtils;
import net.community.chest.eclipse.classpath.ClasspathUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 14, 2008 2:48:23 PM
 */
public class ClasspathDependenciesPanel extends DependencyDetailsPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7254871964320417299L;
	public ClasspathDependenciesPanel ()
	{
		super();
	}
	// key='kind' value=all elements with that kind
	private static final Map<String,Collection<Element>> loadEntries (final String path) throws Exception
	{
		final Document						doc=DOMUtils.loadDocument(path);
		final Element						root=doc.getDocumentElement();
		final Collection<? extends Element>	el=DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
		if ((null == el) || (el.size() <= 0))
			return null;

		Map<String,Collection<Element>>	ret=null;
		for (final Element elem : el)
		{
			final String	tagName=(null == elem) ? null : elem.getTagName();
			if (!ClasspathUtils.CLASSPATHENTRY_ELEM_NAME.equalsIgnoreCase(tagName))
				continue;

			final String	eKind=elem.getAttribute(ClasspathUtils.CLASSPATHENTRY_KIND_ATTR);
			if ((null == eKind) || (eKind.length() <= 0))
				continue;

			Collection<Element>	kl=(null == ret) ? null : ret.get(eKind);
			if (null == ret)
				ret = new TreeMap<String,Collection<Element>>(String.CASE_INSENSITIVE_ORDER);
			if (null == kl)
			{
				kl = new LinkedList<Element>();
				ret.put(eKind, kl);
			}

			kl.add(elem);
		}

		return ret;
	}

	public static final String	M2_REPO_PREFIX="M2_REPO/";
	private static final Collection<BuildTargetFile> updateDependenciesList (
			final Collection<BuildTargetFile>	org,
			final Collection<? extends Element>	el)
	{
		final int	numElems=(null == el) ? 0 : el.size();
		if (numElems <= 0)
			return org;

		Collection<BuildTargetFile>	ret=org;
		for (final Element elem : el)
		{
			final String	tagName=(null == elem) ? null : elem.getTagName();
			if (!ClasspathUtils.CLASSPATHENTRY_ELEM_NAME.equalsIgnoreCase(tagName))
				continue;

			final String	jarPath=elem.getAttribute(ClasspathUtils.CLASSPATHENTRY_PATH_ATTR);
			if ((null == jarPath) || (jarPath.length() <= 0)
			// TODO allow to configure the prefix
			 || (!jarPath.startsWith(M2_REPO_PREFIX)))
				continue;

			final String			relPath=jarPath.substring(M2_REPO_PREFIX.length());
			final BuildTargetFile	tgt=BuildTargetFile.fromRelativeArtifactPath(relPath, '/');
			if (null == ret)
				ret = new ArrayList<BuildTargetFile>(numElems);
			ret.add(tgt);
		}

		return ret;
	}
	/*
	 * @see net.community.apps.apache.maven.pom2cpsync.DependencyDetailsPanel#loadDependencies(java.lang.String)
	 */
	@Override
	public Collection<? extends BaseTargetDetails> loadDependencies (final String path) throws Exception
	{
		final Map<String,? extends Collection<? extends Element>>								em=
			loadEntries(path);
		final Collection<? extends Map.Entry<String,? extends Collection<? extends Element>>>	ell=
			((null == em) || (em.size() <= 0)) ? null : em.entrySet();
		if ((null == ell) || (ell.size() <= 0))
			return null;

		Collection<BuildTargetFile>	deps=null;
		for (final Map.Entry<String,? extends Collection<? extends Element>> ee : ell)
			deps = updateDependenciesList(deps, (null == ee) ? null : ee.getValue());

		return deps;
	}
}

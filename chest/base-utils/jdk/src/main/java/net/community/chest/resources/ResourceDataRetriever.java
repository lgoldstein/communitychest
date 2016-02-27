package net.community.chest.resources;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Locale;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.io.FileUtil;
import net.community.chest.util.locale.LocaleUtils;
import net.community.chest.util.map.ClassNameMap;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Base class used to read resource file data and convert it into some
 * internal data structure. The retriever looks for the resource file
 * first in the specified {@link Locale#toString()} sub-folder, and if
 * not found then at the root location (i.e., non-locale dependent)
 * 
 * @param <D> The retrieved resource generic type
 * @author Lyor G.
 * @since Jul 19, 2007 10:40:41 AM
 */
public abstract class ResourceDataRetriever<D> extends BaseTypedValuesContainer<D> {
	private final Class<?>	_anchor;
	/**
	 * @return {@link Class} used as "anchor" to retrieve the data
	 */
	public final /* no cheating */ Class<?> getAnchorClass ()
	{
		return _anchor;
	}

	private final URL	_resURL;
	/**
	 * @return resource {@link URL} - can be used to access the resource
	 * file/data outside the constructor or for logging purposes
	 */
	public final /* no cheating */ URL getResourceURL ()
	{
		return _resURL;
	}

	public InputStream openResourceData (final URL resURL) throws IOException
	{
		if (null == resURL)
			throw new EOFException("openResourceData() no URL specified");
		return resURL.openStream(); 
	}
	/**
	 * @return {@link InputStream} that can be used to access the resource
	 * file data. <B>Note:</B> it is up to the caller to close the stream
	 * after using it
	 * @throws IOException if unable to open the resource data stream
	 */
	public InputStream openResourceData () throws IOException
	{
		return openResourceData(getResourceURL());
	}

	private final D	_resData;
	public final /* no cheating */ D getResourceData ()
	{
		return _resData;
	}
	/**
	 * Called by constructor in order to load up the resource data unless
	 * {@link #loadResourceData(String)} is overridden. Default implementation
	 * throws 
	 * @param in <U>Open</U> {@link InputStream} from which to load data.</P>
	 * <B>Note:</B> it is/should be closed by the <U>caller</U> of this
	 * method - the method itself should <U>not</U> close the stream upon
	 * completion of its execution.
	 * @return loaded resource data - may NOT be null
	 * @throws IOException if unable to load resource data.</P>
	 */
	public D loadResourceData (InputStream in) throws IOException
	{
		throw new StreamCorruptedException(getArgumentsExceptionLocation("loadResourceData", in) + " N/A");
	}

	public D loadResourceData (final URL resURL) throws IOException
	{
		InputStream	in=null;
		try
		{
			if (null == (in=openResourceData(resURL)))	// should not happen
				throw new EOFException(getArgumentsExceptionLocation("loadResourceData", resURL) + " no " + InputStream.class.getSimpleName());

			return loadResourceData(in);
		}
		finally
		{
			FileUtil.closeAll(in);
		}
	}
	/**
	 * @param resSubPath resource relative location to the anchor
	 * @return loaded resource data - by default calls {@link #openResourceData(URL)}
	 * followed by {@link #loadResourceData(InputStream)} (which throws an
	 * exception unless overridden)
	 * @throws IOException if cannot load the resource data
	 * its {@link URL}
	 */
	public D loadResourceData (final String resSubPath) throws IOException
	{
		final Class<?>	a=getAnchorClass();
		final URL		resURL=a.getResource(resSubPath);
		return loadResourceData(resURL);
	}

	public static final char	RES_FOLDER_SEP_CHAR='/';
	public static final String	RES_FOLDER_SEP_VALUE=String.valueOf(RES_FOLDER_SEP_CHAR);

	public static final URL lookupAnchoredLocalizedResource (
			final String basePath, final AnchoredResourceAccessor anchor, final Locale l, final String resName)
	{
		final int	rnLen=(null == resName)? 0 : resName.length();
		if ((null == anchor) || (rnLen <= 0))
			throw new IllegalArgumentException("lookupAnchoredLocalizedResource(" + basePath + ")[" + l + "]/" + resName + " bad/illegal anchor/resource");

		final int	baseLen=(null == basePath) ? 0 : basePath.length();
		if ((baseLen > 0) && (basePath.charAt(0) != RES_FOLDER_SEP_CHAR))
			throw new IllegalArgumentException("lookupAnchoredLocalizedResource(" + basePath + ")[" + l + "]/" + resName + " non absolute base path");

		final List<String>	ls=LocaleUtils.getLocaleFormatting((null == l) ? Locale.getDefault() : l);
		final int			numValues=(null == ls) ? 0 : ls.size();
		if (numValues <= 0)
			throw new IllegalStateException("lookupAnchoredLocalizedResource(" + basePath + ")[" + l + "]/" + resName + " no locale data");

		final StringBuilder	sb=new StringBuilder(Math.max(baseLen,4) + rnLen + numValues * 8 + 4)
									.append((baseLen <= 0) ? RES_FOLDER_SEP_VALUE : basePath)
									;

		final int	sbLen=sb.length();
		for (int	maxVals=numValues; maxVals >= 0; maxVals--)
		{
			sb.setLength(sbLen);

			for (int vIndex=0; vIndex < maxVals; vIndex++)
			{
				final String	vs=ls.get(vIndex).toLowerCase();
				sb.append(RES_FOLDER_SEP_CHAR).append(vs);
			}

			sb.append(RES_FOLDER_SEP_CHAR).append(resName);

			final String	resPath=sb.toString();
			final URL		resURL=anchor.getResource(resPath);
			if (resURL != null)
				return resURL;
		}

		return null;
	}

	private static ClassNameMap<AnchoredResourceAccessor>	_accsMap;
	private static final synchronized ClassNameMap<AnchoredResourceAccessor> getAccessorsMap ()
	{
		if (null == _accsMap)
			_accsMap = new ClassNameMap<AnchoredResourceAccessor>();
		return _accsMap;
	}

	public static final AnchoredResourceAccessor getAnchoredResourceAccessor (final Class<?> anchor)
	{
		if (null == anchor)
			return null;

		final ClassNameMap<AnchoredResourceAccessor>	am=getAccessorsMap();
		AnchoredResourceAccessor						a=null;
		synchronized(am)
		{
			if (null == (a=am.get(anchor)))
			{
				a = new AnchorClassAccessorEmbedder(anchor);
				am.put(anchor, a);
			}
		}

		return a;
	}

	public static final URL lookupAnchoredLocalizedResource (
			final String basePath, final Class<?> anchor, final Locale l, final String resName)
	{
		return lookupAnchoredLocalizedResource(basePath, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor), l, resName);
	}

	public static final String getResourceBaseLocation (final Package p)
	{
		final String	pkgName=(null == p) ? null : p.getName(),
						pkgSubFolder=
			((null == pkgName) || (pkgName.length() <= 0)) ? null :  pkgName.replace('.', RES_FOLDER_SEP_CHAR);
		if ((null == pkgSubFolder) || (pkgSubFolder.length() <= 0))
			return null;

		return RES_FOLDER_SEP_VALUE + pkgSubFolder;
	}

	public static final URL lookupAnchoredLocalizedResource (
			final Package p, final AnchoredResourceAccessor anchor, final Locale l, final String resName)
	{
		return lookupAnchoredLocalizedResource(getResourceBaseLocation(p), anchor, l, resName);
	}

	public static final URL lookupAnchoredLocalizedResource (
			final Package p, final Class<?> anchor, final Locale l, final String resName)
	{
		return lookupAnchoredLocalizedResource(p, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor), l, resName);
	}

	public static final String getResourceBaseLocation (final Class<?> c)
	{
		return (null == c) ? null : getResourceBaseLocation(c.getPackage());
	}

	public static final URL lookupAnchoredLocalizedResource (
			final Class<?> c, final AnchoredResourceAccessor anchor, final Locale l, final String resName)
	{
		return (null == c) ? null : lookupAnchoredLocalizedResource(c.getPackage(), anchor, l, resName);
	}

	public static final URL lookupAnchoredLocalizedResource (final Class<?> c, final Class<?> anchor, final Locale l, final String resName)
	{
		return (null == c) ? null : lookupAnchoredLocalizedResource(c, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor), l, resName);
	}
	/**
	 * @param resClass data class - may NOT be <code>null</code>
	 * @param anchor anchor {@link Class} that is assumed to reside in the
	 * same package/folder as the sought after resource file - may NOT be
	 * null
	 * @param lcl {@link Locale} for the file - if null then same as if file
	 * not found at the specified locale-specific sub-folder (i.e., look up
	 * the file at the "root" location
	 * @param name resource file name
	 * @throws IOException if unable to access the file
	 */
	protected ResourceDataRetriever (final Class<D> resClass, final Class<?> anchor, final Locale lcl, final String name) throws IOException
	{
		super(resClass);

		if ((null == name) || (name.length() <= 0))
			throw new IOException("No file name to retrieve");
		if (null == (_anchor=anchor))
			throw new IOException("Missing anchor class for name=" + name);

		URL	resURL=null;
		try
		{
			if (null == (resURL=lookupAnchoredLocalizedResource(anchor, anchor, lcl, name)))
				throw new FileNotFoundException("No resource=" + name + " at anchor=" + anchor.getName());

			_resURL = resURL;
	
			if (null == (_resData=loadResourceData(resURL)))
				throw new StreamCorruptedException("No data extracted from URL=" + resURL);
		}
		catch(Exception e)
		{
			if (e instanceof IOException)
				throw (IOException) e;

			throw new StreamCorruptedException(e.getClass().getName() + " on loadData(" + resURL + "): " + e.getMessage());
		}
	}
	
	protected ResourceDataRetriever (final Class<D> resClass, final Class<?> anchor, final String name) throws IOException
	{
		this(resClass, anchor, Locale.getDefault(), name);
	}
	/**
	 * @param anchor "Anchor" class whose URL we want - may NOT be null
	 * @return {@link URL} indicating the location from which this class
	 * was loaded
	 * @throws IOException if unable to extract location
	 */
	public static final URL getAnchorClassLocation (final Class<?> anchor) throws IOException
	{
		final ProtectionDomain	pd=(null == anchor) /* should not happen */ ? null : anchor.getProtectionDomain();
		final CodeSource		cs=(null == pd) /* should not happen */ ? null : pd.getCodeSource();
		final URL				ul=(null == cs) /* should not happen */ ? null : cs.getLocation();
		if (null == ul)
			throw new IOException("getAnchorClassLocation(" + ((null == anchor) ? null : anchor.getName()) + ") no location returned");

		return ul;
	}
	/**
	 * @param anchor "Anchor" class whose (JAR) container location we want
	 * (may NOT be null)
	 * @return {@link File} indicating the (JAR) location from which this
	 * class was loaded
	 * @throws IOException unable to extract location
	 * @throws URISyntaxException cannot convert to a container location
	 */
	public static final File getAnchorClassContainerLocation (final Class<?> anchor) throws IOException, URISyntaxException
	{
		final URL	ul=getAnchorClassLocation(anchor);
		final URI	ui=(null == ul) /* should not happen */ ? null : ul.toURI();
		if (null == ui)
			throw new IOException("getAnchorClassContainerLocation(" + ((null == anchor) ? null : anchor.getName()) + ") no URI indicator");

		return new File(ui);
	}

	public static final URL lookupAnchoredLocalizedResource (
			final String resPath, final String basePath, final AnchoredResourceAccessor anchor, final Locale l, final String resName)
	{
		final int	pLen=(null == resPath) ? 0 : resPath.length();
		final URL	resURL=lookupAnchoredLocalizedResource(resPath, anchor, l, resName);
		if ((resURL != null) /* found it */ || (pLen <= 1) /* already at root */)
			return resURL;

		final int		bLen=(null == basePath) ? 0 : basePath.length();
		if (bLen > 0)
		{
			// make sure proper sub-package specified
			if ((!resPath.startsWith(basePath))
			 || (resPath.charAt(bLen) != RES_FOLDER_SEP_CHAR))
				throw new IllegalArgumentException("lookupAnchoredLocalizedResource(" + resPath + ")[" + l + "]/" + resName + " not proper base path: " + basePath);

			// if "below" the base package then stop
			if (pLen <= bLen)
				return null;
		}

		final int		sPos=resPath.lastIndexOf(RES_FOLDER_SEP_CHAR);
		final String	nextPath=(sPos > 1) ? resPath.substring(0, sPos) : RES_FOLDER_SEP_VALUE;
		return lookupAnchoredLocalizedResource(nextPath, basePath, anchor, l, resName);
	}

	public static final URL lookupAnchoredLocalizedResource (
			final String resPath, final String basePath, final Class<?> anchor, final Locale l, final String resName)
	{
		return lookupAnchoredLocalizedResource(resPath, basePath, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor), l, resName);
	}

	public static final URL lookupAnchoredLocalizedResource (
			final Package p, final Package basePkg, final AnchoredResourceAccessor anchor, final Locale l, final String resName)
	{
		return lookupAnchoredLocalizedResource(getResourceBaseLocation(p), getResourceBaseLocation(basePkg), anchor, l, resName);
	}

	public static final URL lookupAnchoredLocalizedResource (
			final Package p, final Package basePkg, final Class<?> anchor, final Locale l, final String resName)
	{
		return lookupAnchoredLocalizedResource(p, basePkg, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor), l, resName);
	}

	public static final URL lookupAnchoredClassResource (
			final Class<?> c, final Class<?> baseClass, final AnchoredResourceAccessor anchor, final Locale l, final String resName)
	{
		if ((null == c) || (null == anchor) || (null == resName) || (resName.length() <= 0))
			throw new IllegalArgumentException("lookupAnchoredClassResource(" + ((null == c) ? null : c.getName()) + ")[" + resName + "] missing arguments");

		return lookupAnchoredLocalizedResource(c.getPackage(), (null == baseClass) ? null : baseClass.getPackage(), anchor, l, resName);
	}

	public static final URL lookupAnchoredClassResource (
			final Class<?> c, final Class<?> baseClass, final Class<?> anchor, final Locale l, final String resName)
	{
		return lookupAnchoredClassResource(c, baseClass, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor), l, resName);
	}
	/**
	 * @param c The {@link Class#getSimpleName()} from which to derive the
	 * resource name
	 * @param baseClass The base {@link Class} to go up to in the hierarchy
	 * while looking for the resource (inclusive) - if <code>null</code> then
	 * {@link Object} is assumed
	 * @param anchor The anchor {@link AnchoredResourceAccessor} to use to
	 * resolve the URL of the resource
	 * @param lcl The {@link Locale} to use - <code>null</code> is same as
	 * {@link Locale#getDefault()} 
	 * @param resSuffix The suffix (with or without dot - does not matter) of
	 * the derived resource - may not be null/empty
	 * @return The URL of the located resource - <code>null</code> if not found
	 */
	public static final URL lookupDerivedClassResource (
			final Class<?> c, final Class<?> baseClass, final AnchoredResourceAccessor anchor, final Locale lcl, final String resSuffix)
	{
		final String	effSuffix=FileUtil.adjustExtension(resSuffix, true);
		if ((null == c) || (null == anchor) || (null == effSuffix) || (effSuffix.length() <= 0))
			throw new IllegalArgumentException("lookupDerivedClassResource(" + ((null == c) ? null : c.getName()) + ")[" + resSuffix + "] missing arguments");

		final String	cName=c.getSimpleName();
		final Locale	l=(null == lcl) ? Locale.getDefault() : lcl;
		// anonymous classes have no simple name - so skip them and go to their parent
		if ((cName != null) && (cName.length() > 0))
		{
			final String	resName=cName + effSuffix;
			final URL		resURL=lookupAnchoredLocalizedResource(c, anchor, l, resName);
			if (resURL != null)
				return resURL;
		}

		// either URL not found or anonymous class
		final Class<?>	sc=c.getSuperclass();
		// check if reached end of hierarchy
		if ((null == sc) || ((baseClass != null) && (!baseClass.isAssignableFrom(sc))))
			return null;

		return lookupDerivedClassResource(sc, baseClass, anchor, l, effSuffix);
	}
	/**
	 * @param c The {@link Class#getSimpleName()} from which to derive the
	 * resource name
	 * @param baseClass The base {@link Class} to go up to in the hierarchy
	 * while looking for the resource (inclusive) - if <code>null</code> then
	 * {@link Object} is assumed
	 * @param anchor The anchor {@link Class#getResource(String)} to use to
	 * resolve the URL of the resource
	 * @param lcl The {@link Locale} to use - <code>null</code> is same as
	 * {@link Locale#getDefault()} 
	 * @param resSuffix The suffix (with or without dot - does not matter) of
	 * the derived resource - may not be null/empty
	 * @return The URL of the located resource - <code>null</code> if not found
	 */
	public static final URL lookupDerivedClassResource (
			final Class<?> c, final Class<?> baseClass, final Class<?> anchor, final Locale lcl, final String resSuffix)
	{
		return lookupDerivedClassResource(c, baseClass, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor), lcl, resSuffix);
	}
	/**
	 * <P>Looks up a resource file derived from the <U>fully qualified</U> class
	 * name and an associated suffix (e.g., "foo.bar.pkg.MyClass" + ".txt").
	 * The lookup process goes over the supplied anchor classes attempting to
	 * find one whose containing package is a superset of the class. Once it
	 * is located, then {@link Class#getResourceAsStream(String)} is called on
	 * the sub-path (+ suffix).</P>
	 * <P><B>Note:</B> a <I>superset</I> anchor class is one that is located
	 * somewhere along the same hierarchy of the looked up resource. E.g., in
	 * the above mentioned example - if we look up <code>foo.bar.pkg.MyClass</code>
	 * then we can use as anchor(s) any class in <code>foo</code>, <code>foo.bar</code>
	 * or <code>foo.bar.pkg</code> packages.</P>
	 * @param clsName <U>fully qualified</U> class name (may not be null/empty)
	 * @param sfx suffix (may not be null/empty)
	 * @param anchors Anchor classes to be used to locate the superset
	 * container - <B>Note:</B> first resource found is returned.
	 * @return open {@link InputStream} for the specified resource
	 * @throws IOException no resource found at any location
	 */
	public static final InputStream lookupClassResource (
			final String clsName, final String sfx, final Class<?>... anchors) throws IOException
	{
		final int	cnLen=(null == clsName) ? 0 : clsName.length();
		if ((cnLen <= 0)
		 || (null == sfx) || (sfx.length() <= 0)
		 || (null == anchors) || (anchors.length <= 0))
			throw new FileNotFoundException("lookupClassResource(" + clsName + ";" + sfx + ") null/empty class path/suffix/anchors");

		for (final Class<?> anchor : anchors)
		{
			if (null == anchor)	// should not happen
				continue;

			// make sure requested class name is sub-path of anchor 
			final Package	ancPkg=anchor.getPackage();
			final String	ancRoot=ancPkg.getName();
			final int		arLen=(null == ancRoot) ? 0 : ancRoot.length();
			if ((cnLen <= arLen)				// length must be at least as root package
			 || (clsName.charAt(arLen) != '.')	// must be proper sub-package
			 || (!clsName.startsWith(ancRoot)))
				continue;

			// "peel" of the anchor package start and create a relative sub-path from it
			final String		subPackage=clsName.substring(arLen+1),
								subFolder=subPackage.replace('.', '/'),
								subPath=subFolder + sfx;
			final InputStream	in=anchor.getResourceAsStream(subPath);
			if (in != null)
				return in;
		}
			
		throw new FileNotFoundException("lookupClassResource(" + clsName + ";" + sfx + ") all anchors exhausted");
	}
}

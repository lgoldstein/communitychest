package net.community.chest.dom.proxy;

import java.net.URL;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>The default {@link #loadAttributeResource(Class, Object, String, String)}
 * implementation assumes that the value is a resource identifier formatted
 * according to the {@link Class#getResource(String)} API. This URL is
 * used in conjunction with the {@link #getResourcesAnchor()} result in order
 * to determine the actual "path" of the resource to be loaded</P>
 * 
 * @author Lyor G.
 * @since Mar 30, 2008 11:22:15 AM
 */
public abstract class AnchoredReflectiveResourceLoader implements ReflectiveResourceLoader {
	protected AnchoredReflectiveResourceLoader ()
	{
		super();
	}
	/**
	 * @return The {@link Class} that is used as resources "anchor" and via
	 * whose {@link ClassLoader} the resources will be loaded
	 */
	public abstract Class<?> getResourcesAnchor ();
	/**
	 * Called by default {@link #loadAttributeResource(Class, Object, String, String)}
	 * implementation after resolving the resource URL
	 * @param <V> The type of loaded resource
	 * @param resClass The expected resource class
	 * @param src The object whose attribute resource is being loaded
	 * @param aName The attribute name
	 * @param resURL The resource {@link URL} - usually non-null
	 * @return The loaded resource (if null, then usually ignored)
	 * @throws Exception If unable to load the resource
	 */
	public abstract <V> V loadAttributeResource (Class<V> resClass, Object src, String aName, URL resURL) throws Exception;
	/*
	 * @see net.community.chest.dom.transform.ReflectiveResourceLoader#loadAttributeResource(java.lang.Class, java.lang.Object, java.lang.String, java.lang.String)
	 */
	@Override
	public <V> V loadAttributeResource (final Class<V> resClass, final Object src, final String aName, final String aValue) throws Exception
	{
		final Class<?>	resAnchor=getResourcesAnchor();
		final URL		resURL=
			((null == resAnchor) || (null == aValue) || (aValue.length() <= 0)) ? null : resAnchor.getResource(aValue);
		if (null == resURL)
			return null;

		return loadAttributeResource(resClass, src, aName, resURL);
	}
}

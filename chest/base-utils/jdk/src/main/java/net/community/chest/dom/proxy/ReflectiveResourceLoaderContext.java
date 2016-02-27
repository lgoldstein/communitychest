package net.community.chest.dom.proxy;

import net.community.chest.resources.AnchoredResourceAccessor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 19, 2008 3:21:06 PM
 */
public interface ReflectiveResourceLoaderContext extends AnchoredResourceAccessor {
	/**
	 * Called in order to retrieve a {@link ReflectiveResourceLoader} instance
	 * to be used in order to initialize an object's attribute 
	 * @param resClass The expected resource {@link Class}
	 * @param src The object whose resource attribute is requested
	 * @param aName The requested attribute name
	 * @param aValue The requested attribute value - usually an indication
	 * as to the resource ID to be loaded
	 * @return The resource to be set as the attribute's value - if null
	 * then attribute value is not set.
	 * @throws Exception If failed to resolve used loader
	 */
	ReflectiveResourceLoader getResourceLoader (Class<?> resClass, Object src, String aName, String aValue) throws Exception;
}

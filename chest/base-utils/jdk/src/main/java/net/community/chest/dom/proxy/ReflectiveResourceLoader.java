package net.community.chest.dom.proxy;

import net.community.chest.resources.AnchoredResourceAccessor;


/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>An interface used to load resources for reflective XML proxies</P>
 *
 * @author Lyor G.
 * @since Mar 25, 2008 11:35:25 AM
 */
public interface ReflectiveResourceLoader extends AnchoredResourceAccessor {
    /**
     * Called in order to load a resource for an object's attribute
     * @param <V> Type of resource object(s)
     * @param resClass The expected resource {@link Class}
     * @param src The object whose resource attribute is requested
     * @param aName The requested attribute name
     * @param aValue The requested attribute value - usually an indication
     * as to the resource ID to be loaded
     * @return The resource to be set as the attribute's value - if null
     * then attribute value is not set.
     * @throws Exception If failed to load requested resource
     */
    <V> V loadAttributeResource (Class<V> resClass, Object src, String aName, String aValue) throws Exception;
}

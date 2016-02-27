/*
 * 
 */
package net.community.apps.common;

import java.awt.Font;
import java.util.Map;

import net.community.apps.common.resources.BaseAnchor;
import net.community.chest.dom.proxy.ReflectiveResourceLoaderContext;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Interface implemented by most "main" default components</P>
 * 
 * @param <A> The generic {@link BaseAnchor} type
 * @author Lyor G.
 * @since Dec 31, 2008 9:05:30 AM
 */
public interface MainComponent<A extends BaseAnchor>
			extends BaseMainComponent,
					ToolbaredComponent {

	A getResourcesAnchor ();

	ReflectiveResourceLoaderContext getDefaultResourcesLoader ();

	// some "well-known" sections
	public static final String	MAIN_FONTS_SECTION_NAME="main-fonts";

	Element getMainFontsElement ();
	Map<String,Font> getMainFontsMap ();
	Font getMainFont (String id);
}

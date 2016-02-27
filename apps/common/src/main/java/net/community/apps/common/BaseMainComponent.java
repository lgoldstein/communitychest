/*
 * 
 */
package net.community.apps.common;

import java.awt.Component;

import org.w3c.dom.Element;

import net.community.chest.ui.helpers.XmlDocumentComponentInitializer;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 11:12:02 AM
 */
public interface BaseMainComponent
			extends XmlDocumentComponentInitializer,
					FileLoadComponent{
	Component getMainFrame ();

	public static final String	MANIFEST_SECTION_NAME="show-manifest-dialog";
	Element getManifestDialogElement ();
	void showManifest (Object anchor, Element dlgElem) throws Exception;
	void showManifest () throws Exception;

	void exitApplication ();
}

/*
 *
 */
package net.community.apps.common;

import java.io.File;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 11:30:39 AM
 */
public interface FileLoadComponent {

    public static final String    LOAD_FILE_SECTION_NAME="load-file-dialog";
    Element getLoadDialogElement ();
    void loadFile (File f, String cmd, Element dlgElement);
    void loadFile ();

    public static final String    SAVE_FILE_SECTION_NAME="save-file-dialog";
    Element getSaveDialogElement ();
    void saveFile (File f, Element dlgElement);
    void saveFile ();
}

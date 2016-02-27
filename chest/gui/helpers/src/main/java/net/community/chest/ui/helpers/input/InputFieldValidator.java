/*
 * 
 */
package net.community.chest.ui.helpers.input;

import java.awt.Color;

import javax.swing.event.ChangeListener;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used by UI fields used for input of data from the user</P>
 * 
 * @author Lyor G.
 * @since Jan 12, 2009 11:23:45 AM
 */
public interface InputFieldValidator {
	/**
	 * @return TRUE if current input data is valid
	 */
	boolean isValidData ();
	/**
	 * @param l The {@link ChangeListener} instance to be added - ignored
	 * if <code>null</code> or already registered
	 * @return <code>true</code> if successfully added the listener
	 */
	boolean addDataChangeListener (ChangeListener l);
	/**
	 * @param l The {@link ChangeListener} instance to be removed - ignored
	 * if <code>null</code> or not registered
	 * @return <code>true</code> if successfully removed the listener
	 */
	boolean removeDataChangeListener (ChangeListener l);
	/**
	 * Force a refresh of the UI. <B>Note:</B> the <U>correct</U> way to
	 * react to changes is via registration of a {@link ChangeListener} and
	 * <B><U>not</U></B> by overriding this method.
	 * @param fireEvent <code>true</code> if to also fire the registered
	 * registered {@link ChangeListener}-s
	 * @return Number of fired events
	 */
	int signalDataChanged (boolean fireEvent);
	// colors used usually for the border color of OK/ERR fields
	public static final Color	DEFAULT_OK_COLOR=Color.BLACK,
								DEFAULT_ERR_COLOR=Color.RED;
	// line used to indicate bad input
	public static final int DEFAULT_LINE_THICKNESS=2;
	/**
	 * @return {@link Color} to be used if the data displayed in the input
	 * field is OK (default={@link #DEFAULT_OK_COLOR})
	 */
	Color getOkFieldColor ();
	// NOTE: ignored if null
	void setOkFieldColor (Color okFieldColor);
	/**
	 * @return {@link Color} to be used if the data displayed in the field
	 * field(s) is malformed (default={@link #DEFAULT_ERR_COLOR})
	 */
	Color getErrFieldColor ();
	// NOTE: ignored if null
	void setErrFieldColor (Color errFieldColor);

}

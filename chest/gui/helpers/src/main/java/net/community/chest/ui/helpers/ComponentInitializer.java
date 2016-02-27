/*
 * 
 */
package net.community.chest.ui.helpers;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Useful interface usually implemented by multi-control components
 * (e.g., panel, dialog, etc).</P>
 * @author Lyor G.
 * @since Oct 30, 2008 11:34:30 AM
 */
public interface ComponentInitializer {
	/**
	 * Initializes the component
	 * @throws RuntimeException If failed to initialize
	 */
	void layoutComponent () throws RuntimeException;
}

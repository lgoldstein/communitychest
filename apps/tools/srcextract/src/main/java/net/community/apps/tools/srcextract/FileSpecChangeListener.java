package net.community.apps.tools.srcextract;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Nov 25, 2007 12:43:33 PM
 */
public interface FileSpecChangeListener {
	/**
	 * Called whenever the user changes somethinf in a {@link FileSpecPanel}
	 * @param fsp The changed {@link FileSpecPanel}
	 */
	void handleSelectionChanged (FileSpecPanel fsp);
}

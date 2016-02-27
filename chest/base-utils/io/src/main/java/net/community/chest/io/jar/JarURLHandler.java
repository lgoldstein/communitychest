/*
 * 
 */
package net.community.chest.io.jar;

import java.net.URL;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 21, 2009 10:20:13 AM
 */
public interface JarURLHandler extends JarEntryHandler {
	/**
	 * @param url The URL of the JAR whose entries are about to be enumerated
	 * @param starting TRUE if about to start enumeration, FALSE if finished
	 * enumerating
	 * @param errCode Last {@link #handleJAREntry(java.util.jar.JarEntry)}
	 * return code (due to which enumeration stopped).
	 * @return One of the following:</BR>
	 * <P>If starting</P></BR>
	 * <UL>
	 * 		<LI>0 - go on with the enumeration</LI>
	 * 		<LI>1 - skip to next URL</LI>
	 * 		<LI>positive (other than 1) - abort enumeration (no error)</LI>
	 * 		<LI>negative - abort enumeration (error)</LI>
	 * </UL>
	 * <P>If ending</P></BR>
	 * <UL>
	 * 		<LI>0 - go on to next URL</LI>
	 * 		<LI>positive - abort enumeration (no error)</LI>
	 * 		<LI>negative - abort enumeration (error)</LI>
	 * </UL>
	 */
	int handleJarURL (URL url, boolean starting, int errCode);
}

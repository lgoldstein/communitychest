/*
 *
 */
package net.community.chest.net.proto.text;

import java.util.Set;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Generic interface for textual protocols that report their capabilities</P>
 *
 * @author Lyor G.
 * @since Jun 23, 2009 8:12:08 AM
 */
public interface ProtocolCapabilityResponse {
    // should be case insensitive
    Set<String> getCapabilities ();
    void setCapabilities (Set<String> caps);
    /**
     * Adds the specified capability
     * @param c The capability to add - ignored if null/empty
     * @return The updated capabilities {@link Set} - may be
     * null/empty if no current capabilities set and none added
     */
    Set<String> addCapability (String c);
    /**
     * @return TRUE if any capabilities set
     */
    boolean hasCapabilities ();
    /**
     * Checks if current capabilities include specified one
     * @param cap capability to be checked
     * @return TRUE if specified capability found (case-insensitive search)
     */
    boolean hasCapability (String cap);

}

/*
 * 
 */
package net.community.chest.net.proto.text.ssh;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Represents value codes encapsulated inside some other classes
 * (usually {@link Enum}-s).</P>
 * 
 * @author Lyor G.
 * @since Jul 2, 2009 7:52:53 AM
 */
public interface CodeValueEncapsulator {
	/**
	 * @return The numerical reason code
	 */
	int getCodeValue ();
	/**
	 * @return Some non-<code>null</code>/empty "mnemonic" name for the
	 * reason code - intended for usage in exceptions and log messages.
	 */
	String getMnemonic ();
}

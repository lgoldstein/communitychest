/*
 * 
 */
package net.community.chest.groovy

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 23, 2011 10:59:22 AM
 */
public class BasicTester {
	public static void main (String[] args) {
		try {
			throw new IllegalArgumentException("Blah blah")
		} catch(Exception e) {
			println "The message is: " + e.getMessage()
			println "And it still is ${e.getMessage()}"
		}
	}
}
